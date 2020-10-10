package com.dominicsilveira.parkingsystem.utils.pdf;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;

import com.dominicsilveira.parkingsystem.classes.BookedSlots;
import com.dominicsilveira.parkingsystem.classes.ParkingArea;
import com.dominicsilveira.parkingsystem.classes.User;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class InvoiceGenerator {
    BookedSlots bookingSlot;
    ParkingArea parkingArea;
    String key;
    User userObj;
    File file;

    public InvoiceGenerator(BookedSlots bookingSlot,ParkingArea parkingArea,String key,User userObj,File file){
        this.bookingSlot=bookingSlot;
        this.parkingArea=parkingArea;
        this.key=key;
        this.userObj=userObj;
        this.file=file;
    }


    public void create(){
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM, yyyy");
        SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm a");
        SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("dd-MMM-yyyy, hh:mm a");

        PdfDocument pdfDocument=new PdfDocument();
        Paint paint=new Paint();
        PdfDocument.PageInfo pageInfo=new PdfDocument.PageInfo.Builder(1000,700,1).create();
        PdfDocument.Page page=pdfDocument.startPage(pageInfo);
        Canvas canvas=page.getCanvas();

        paint.setTextSize(50);
        canvas.drawText("Smart Parking System",30,60,paint);

        paint.setTextSize(25);
        canvas.drawText(parkingArea.name,30,90,paint);

        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Invoice no",canvas.getWidth()-40,40,paint);
        canvas.drawText(key,canvas.getWidth()-40,80,paint);

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.rgb(150,150,150));
        canvas.drawRect(30,120,canvas.getWidth()-30,130,paint);

        paint.setColor(Color.BLACK);
        canvas.drawText("Date: ",50,170,paint);
        canvas.drawText(dateFormatter.format(bookingSlot.startTime),250,170,paint);
        canvas.drawText("Time: ",620,170,paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(timeFormatter.format(bookingSlot.startTime),canvas.getWidth()-40,170,paint);

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.rgb(150,150,150));
        canvas.drawRect(30,220,canvas.getWidth()-40,270,paint);

        paint.setColor(Color.WHITE);
        canvas.drawText("Bill To: ",50,255,paint);

        paint.setColor(Color.BLACK);
        canvas.drawText("Customer Name: ",30,320,paint);
        canvas.drawText(userObj.name,250,320,paint);
        canvas.drawText("Phone No: ",620,320,paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(userObj.contact_no,canvas.getWidth()-40,320,paint);

        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Email ID: ",30,365,paint);
        canvas.drawText(userObj.email,250,365,paint);

        paint.setColor(Color.rgb(150,150,150));
        canvas.drawRect(30,415,canvas.getWidth()-40,465,paint);

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
        canvas.drawText("Total",550,615,paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(String.valueOf(bookingSlot.amount),970,615,paint);

        pdfDocument.finishPage(page);
        try {
            pdfDocument.writeTo(new FileOutputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        pdfDocument.close();
    }
}
