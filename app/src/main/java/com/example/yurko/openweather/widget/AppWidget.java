package com.example.yurko.openweather.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.example.yurko.openweather.JSONParseCurrent;
import com.example.yurko.openweather.R;
import com.example.yurko.openweather.Utilities;
import com.example.yurko.openweather.model.CCOpenWeatherPojo.CCOpenWeatherPojo;
import com.example.yurko.openweather.model.MyApplication;
import com.example.yurko.openweather.presenter.MainPresenter;
import com.example.yurko.openweather.view.MainView;
import com.example.yurko.openweather.view.MainViewActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

import static com.example.yurko.openweather.widget.AppWidget.updateAppWidget;

/**
 * Implementation of App Widget functionality.
 */
public class AppWidget extends AppWidgetProvider {

    private static final String LOG_TAG = "AppWidget";
    final static String ACTION_OPEN = "com.example.yurko.openweather.widget.openclock";
    private final static String CURRENT_UPDATETIME = "current_updatetime";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        Log.i(LOG_TAG,"updateAppWidget");
        RemoteViews views = new RemoteViews(MyApplication.getAppContext().getPackageName(), R.layout.app_widget);
        CCOpenWeatherPojo openWeatherPojo = Utilities.getObjectFromPreferences();
        String currTemper = openWeatherPojo.getMain().getTempCels().toString();
        //Log.i(LOG_TAG,currTemp);
        Date date = openWeatherPojo.getTimestamp();

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        String updateTime = dateFormat.format(date);

        if (currTemper != null) {
            views.setTextViewText(R.id.widget_temp, currTemper);
        }
        if (updateTime != null) {
            Log.i(LOG_TAG,updateTime);
            views.setTextViewText(R.id.widget_updatetime,updateTime);
        }

        PendingIntent pIntentClock = foundClockApp(context);
        if (pIntentClock != null){
            views.setOnClickPendingIntent(R.id.timeText, pIntentClock);
        }

        Intent intentActivity = new Intent(context, MainViewActivity.class);
        //intentActivity.setAction(ACTION_OPEN);
        PendingIntent pIntentAct = PendingIntent.getActivity(context, appWidgetId, intentActivity,0);
        views.setOnClickPendingIntent(R.id.widget_temp, pIntentAct);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.i(LOG_TAG,"onUpdate");
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            Log.i(LOG_TAG,String.valueOf(appWidgetId));
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        Log.i(LOG_TAG,"onEnabled");
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        Log.i(LOG_TAG,"onDisabled");
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);



//        if (intent.getAction().equalsIgnoreCase(ACTION_OPEN)) {
//
//            PendingIntent pendingIntent = foundClockApp(context);
//            if (pendingIntent != null) {
//                try {
//                    pendingIntent.send();
//                } catch (PendingIntent.CanceledException e) {
//                    Log.i(LOG_TAG, e.getMessage());
//                }
//
//            }
//        }

    }

    private static PendingIntent foundClockApp(Context context){
        PendingIntent intent = null;

        PackageManager packageManager = context.getPackageManager();
        Intent alarmClockIntent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);

// Verify clock implementation
        String clockImpls[][] = {
                {"HTC Alarm Clock", "com.htc.android.worldclock", "com.htc.android.worldclock.WorldClockTabControl" },
                {"Standar Alarm Clock", "com.android.deskclock", "com.android.deskclock.AlarmClock"},
                {"Froyo Nexus Alarm Clock", "com.google.android.deskclock", "com.android.deskclock.DeskClock"},
                {"Moto Blur Alarm Clock", "com.motorola.blur.alarmclock",  "com.motorola.blur.alarmclock.AlarmClock"},
                {"Samsung Galaxy Clock", "com.sec.android.app.clockpackage","com.sec.android.app.clockpackage.ClockPackage"} ,
                {"Sony Ericsson Xperia Z", "com.sonyericsson.organizer", "com.sonyericsson.organizer.Organizer_WorldClock" },
                {"ASUS Tablets", "com.asus.deskclock", "com.asus.deskclock.DeskClock"},
                {"Google Emulator","com.android.deskclock","com.android.deskclock.DeskClock"}


        };

        boolean foundClockImpl = false;

        for(int i=0; i<clockImpls.length; i++) {
            String vendor = clockImpls[i][0];
            String packageName = clockImpls[i][1];
            String className = clockImpls[i][2];
            try {
                ComponentName cn = new ComponentName(packageName, className);
                ActivityInfo aInfo = packageManager.getActivityInfo(cn, PackageManager.GET_META_DATA);
                alarmClockIntent.setComponent(cn);
                Log.i(LOG_TAG,"Found " + vendor + " --> " + packageName + "/" + className);
                foundClockImpl = true;
            } catch (PackageManager.NameNotFoundException e) {
                Log.i(LOG_TAG,vendor + " does not exists");
            }
        }

        if (foundClockImpl) {
            intent = PendingIntent.getActivity(context, 0, alarmClockIntent, 0);
            // add pending intent to your component
            // ....
        }
            // add pending intent to your component
            // ....

        return intent;
    }
}

