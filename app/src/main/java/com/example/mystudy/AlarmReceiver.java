package com.example.mystudy;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";
    private static final String TEST_NOTIFY_ID = "notify";
    private static final int NOTIFY_REQUEST_ID = 300;
    private int testNotifyId = 11;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "rexeive 111111111111111111111111111111111111111111111");
        Intent questionnaire_intent = new Intent(context, SelfQuestionnaire.class);
        PendingIntent pending = PendingIntent.getActivity(context, NOTIFY_REQUEST_ID, questionnaire_intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        //TODO: minSdkVersion 26 (originally: 21)
//            Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        Notification.Builder noti = new Notification.Builder(context, TEST_NOTIFY_ID)
                .setContentTitle("狀態已更新")
                .setContentText("!!!點選通知查看您目前的狀態及填寫問卷!!!")
                .setContentIntent(pending)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true);

        NotificationChannel channel;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(TEST_NOTIFY_ID, TEST_NOTIFY_ID, NotificationManager.IMPORTANCE_HIGH);
            noti.setChannelId(TEST_NOTIFY_ID);
            notificationManager.createNotificationChannel(channel);
        } else {
            noti.setDefaults(Notification.DEFAULT_ALL)
                    .setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        notificationManager.cancel(testNotifyId);
        notificationManager.notify(testNotifyId, noti.build());
    }
}
