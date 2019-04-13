package com.example.mystudy;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.content.Context.ALARM_SERVICE;

public class NotificationHelper {
    private static final String TAG = "NotificationHelper";

    public static void scheduleDailyNotification(Context mContext) {
        Log.d(TAG, "schedule daily notifications");
        // schedule alarms of tomorrow
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 30);

        Intent intent = new Intent(mContext, DailyAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

    }

    public static void scheduleRepeatingRTCNotification(Context mContext) {
        Log.d(TAG, "scheduleRepeatingRTCNotification");

//        Calendar calendar1 = Calendar.getInstance();
//        calendar1.setTimeInMillis(System.currentTimeMillis());

//        calendar1.set(Calendar.HOUR_OF_DAY, 13);
//        calendar1.set(Calendar.MINUTE, 36);
//
//        Calendar calendar2 = Calendar.getInstance();
//        calendar2.set(Calendar.HOUR_OF_DAY, 13);
//        calendar2.set(Calendar.MINUTE, 40);

        // TODO: random time
        ArrayList<Integer> minute = new ArrayList<>();
        minute.add(47);
        minute.add(48);

        // TODO: 要改取隔天的時間 (因為是前一天schedule的)
        for (int i=0;i<2;i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 13);
            calendar.set(Calendar.MINUTE, minute.get(i));

            Intent intent = new Intent();
            intent.setClass(mContext, AlarmReceiver.class);
            PendingIntent pending = PendingIntent.getBroadcast(mContext, i+1, intent, FLAG_UPDATE_CURRENT);
            AlarmManager alarm = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);
            alarm.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pending);
        }

//        AlarmManager alarm = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);
//        alarm.set(AlarmManager.RTC_WAKEUP, calendar1.getTimeInMillis(), pending);
//        alarm.set(AlarmManager.RTC_WAKEUP, calendar2.getTimeInMillis(), pending);
    }
}
