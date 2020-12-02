package com.dominicsilveira.parkingsystem.utils.pdf;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.dominicsilveira.parkingsystem.NormalUser.BookParkingAreaActivity;
import com.dominicsilveira.parkingsystem.classes.BookedSlots;
import com.dominicsilveira.parkingsystem.classes.ParkingArea;
import com.dominicsilveira.parkingsystem.classes.User;
import com.dominicsilveira.parkingsystem.utils.BasicUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class InvoiceGenerator {
    BookedSlots bookingSlot;
    ParkingArea parkingArea;
    String bookingKey;
    User userObj;
    File file;
    BasicUtils utils=new BasicUtils();

    public InvoiceGenerator(){}

    public InvoiceGenerator(BookedSlots bookingSlot,ParkingArea parkingArea,String key,User userObj,File file){
        this.bookingSlot=bookingSlot;
        this.parkingArea=parkingArea;
        this.bookingKey=key;
        this.userObj=userObj;
        this.file=file;
    }


    public void create(){
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM, yyyy");
        SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm a");
        SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("dd-MMM-yyyy, hh:mm a");

        PdfDocument pdfDocument=new PdfDocument();
        Paint paint=new Paint();
        PdfDocument.PageInfo pageInfo=new PdfDocument.PageInfo.Builder(1000,725,1).create();
        PdfDocument.Page page=pdfDocument.startPage(pageInfo);
        Canvas canvas=page.getCanvas();

        paint.setTextSize(50);
        canvas.drawText("Smart Parking System",30,60,paint);

        paint.setTextSize(25);
        canvas.drawText(parkingArea.name,30,90,paint);

        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Invoice no",canvas.getWidth()-40,40,paint);
        canvas.drawText(bookingKey,canvas.getWidth()-40,80,paint);

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.rgb(150,150,150));
        canvas.drawRect(30,120,canvas.getWidth()-30,130,paint);

        paint.setColor(Color.BLACK);
        canvas.drawText("Date: ",50,170,paint);
        canvas.drawText(dateFormatter.format(bookingSlot.startTime),250,170,paint);
        canvas.drawText("Time: ",620,170,paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(timeFormatter.format(bookingSlot.startTime),canvas.getWidth()-50,170,paint);

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.rgb(150,150,150));
        canvas.drawRect(30,220,canvas.getWidth()-30,270,paint);

        paint.setColor(Color.WHITE);
        canvas.drawText("Bill To: ",50,255,paint);
        canvas.drawText("User ID: ",450,255,paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(bookingSlot.userID,canvas.getWidth()-50,255,paint);

        paint.setColor(Color.BLACK);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Customer Name: ",50,320,paint);
        canvas.drawText(userObj.name,250,320,paint);
        canvas.drawText("Phone No: ",620,320,paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(userObj.contact_no,canvas.getWidth()-50,320,paint);

        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Email ID: ",50,365,paint);
        canvas.drawText(userObj.email,250,365,paint);
        canvas.drawText("Slot No: ",620,365,paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(bookingSlot.slotNo,canvas.getWidth()-50,365,paint);

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.rgb(150,150,150));
        canvas.drawRect(30,415,canvas.getWidth()-30,465,paint);

        paint.setColor(Color.WHITE);
        canvas.drawText("Plate-Number",50,450,paint);
        canvas.drawText("Wheeler-Type",240,450,paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Start-Time",canvas.getWidth()-320,450,paint);
        canvas.drawText("End-Time",canvas.getWidth()-50,450,paint);

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.BLACK);
        canvas.drawText(bookingSlot.numberPlate,50,495,paint);
        canvas.drawText(String.valueOf(bookingSlot.wheelerType),240,495,paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(dateTimeFormatter.format(bookingSlot.startTime),canvas.getWidth()-320,495,paint);
        canvas.drawText(dateTimeFormatter.format(bookingSlot.endTime),canvas.getWidth()-50,495,paint);
        paint.setTextAlign(Paint.Align.LEFT);

        paint.setColor(Color.rgb(150,150,150));
        canvas.drawRect(30,565,canvas.getWidth()-40,575,paint);

        paint.setColor(Color.BLACK);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
//        canvas.drawText("",550,615,paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Total Cost (Rs.):- "+String.valueOf(bookingSlot.amount),canvas.getWidth()-50,615,paint);
        String paid=(bookingSlot.hasPaid==1)?"YES":"NO";
        canvas.drawText("Paid:- "+paid,canvas.getWidth()-50,660,paint);

        pdfDocument.finishPage(page);
        try {
            pdfDocument.writeTo(new FileOutputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        pdfDocument.close();
    }


    //this method will upload the file
    public void uploadFile(final Context context,Application application) {
        if(utils.isNetworkAvailable(application)) {
            //if there is a file to upload
            Uri filePath = Uri.fromFile(file);
            if (filePath != null) {
                //displaying a progress dialog while upload is going on
                final ProgressDialog progressDialog = new ProgressDialog(context);
                progressDialog.setTitle("Uploading");
                progressDialog.show();

                FirebaseStorage storage = FirebaseStorage.getInstance();

                StorageReference invoiceRef = storage.getReference().child("invoice/".concat(bookingSlot.userID).concat("/").concat(bookingKey).concat(".pdf"));
                invoiceRef.putFile(filePath)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                //if the upload is successfull
                                //hiding the progress dialog
                                try {
                                    progressDialog.dismiss();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                //and displaying a success toast
                                Toast.makeText(context, "File Uploaded ", Toast.LENGTH_LONG).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                //if the upload is not successfull
                                //hiding the progress dialog
                                try {
                                    progressDialog.dismiss();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                //and displaying error message
                                Toast.makeText(context, exception.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                //calculating progress percentage
                                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                                //displaying percentage in progress dialog
                                progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                            }
                        });
            }
            //if there is not any file
            else {
                //you can display an error toast
                Toast.makeText(context, "No file Available!", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(context, "No Network Available!", Toast.LENGTH_SHORT).show();
        }
    }

    public void downloadFile(String userID, String bookingKey, Context context, Application application) {
        if(utils.isNetworkAvailable(application)){
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference invoiceRef = storage.getReference().child("invoice/".concat(userID).concat("/").concat(bookingKey).concat(".pdf"));

            final File localFile = new File(context.getExternalCacheDir(), File.separator + "invoice.pdf");
            invoiceRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Log.e("firebase ",";local tem file created  created " +localFile.toString());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.e("firebase ",";local tem file not created  created " +exception.toString());
                }
            });
        }else{
            Toast.makeText(context, "No Network Available!", Toast.LENGTH_SHORT).show();
        }

    }

    public void openFile(Context context) {
        final File localFile = new File(context.getExternalCacheDir(), File.separator + "invoice.pdf");
        Intent target = new Intent(Intent.ACTION_VIEW);
        target.setDataAndType(Uri.fromFile(localFile),"application/pdf");
        target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        Intent intent = Intent.createChooser(target, "Open File");
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // Instruct the user to install a PDF reader here, or something
        }
    }

    public void shareFile(Context context) {
        final File localFile = new File(context.getExternalCacheDir(), File.separator + "invoice.pdf");
        Intent share = new Intent(Intent.ACTION_SEND);
        if(localFile.exists()) {
            share.setType("application/pdf");
            share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(localFile));
            share.putExtra(Intent.EXTRA_SUBJECT, "Sharing File...");
            share.putExtra(Intent.EXTRA_TEXT, "Sharing File...");
            context.startActivity(Intent.createChooser(share, "Share File"));
        }
    }
}
