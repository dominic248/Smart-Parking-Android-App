package com.dominicsilveira.parkingsystem.utils.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.dominicsilveira.parkingsystem.NormalUser.GPSMapActivity;
import com.dominicsilveira.parkingsystem.NormalUser.NearByAreaActivity;
import com.dominicsilveira.parkingsystem.NormalUser.UserHistoryActivity;
import com.dominicsilveira.parkingsystem.R;
import com.dominicsilveira.parkingsystem.RegisterLogin.SplashScreen;
import com.dominicsilveira.parkingsystem.ui.scan.ScanFragment;

/**
 * Implementation of App Widget functionality.
 */
public class ParkingNormalUserWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.parking_normal_user_widget);

        Intent mapsIntent=new Intent(context, SplashScreen.class);
        mapsIntent.putExtra("ACTIVITY_NO", 31);
        PendingIntent mapsPendingIntent = PendingIntent.getActivity(context, 1000, mapsIntent, 0);

        Intent nearByIntent=new Intent(context, SplashScreen.class);
        nearByIntent.putExtra("ACTIVITY_NO", 32);
        PendingIntent nearByPendingIntent = PendingIntent.getActivity(context, 1001, nearByIntent, 0);

        Intent historyIntent=new Intent(context, SplashScreen.class);
        historyIntent.putExtra("ACTIVITY_NO", 33);
        PendingIntent historyPendingIntent = PendingIntent.getActivity(context, 1002, historyIntent, 0);

        views.setOnClickPendingIntent(R.id.mapsBtn, mapsPendingIntent);
        views.setOnClickPendingIntent(R.id.nearByBtn, nearByPendingIntent);
        views.setOnClickPendingIntent(R.id.historyBtn, historyPendingIntent);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}





