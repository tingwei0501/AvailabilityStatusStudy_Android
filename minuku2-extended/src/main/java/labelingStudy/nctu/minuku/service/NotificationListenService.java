package labelingStudy.nctu.minuku.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;
import android.util.Log;

import labelingStudy.nctu.minuku.Utilities.ScheduleAndSampleManager;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.model.DataRecord.NotificationDataRecord;
import labelingStudy.nctu.minuku.streamgenerator.NotificationStreamGenerator;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;

/**
 * Created by chiaenchiang on 18/11/2018.
 */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotificationListenService extends NotificationListenerService {
    private static final String TAG = "NotificationListener";

    private String title = "NA";
    private String text = "NA";
    private String subText = "NA";
    private String tickerText = "NA";
    public String pack = "NA";
    private long detectedtime = -1;
    private static long removeNotiTime = -1;

    private static NotificationStreamGenerator notificationStreamGenerator;

    public NotificationListenService(NotificationStreamGenerator notiStreamGenerator){
        try {
            Log.d(TAG,"call notificationlistener service2");
            this.notificationStreamGenerator = (NotificationStreamGenerator) MinukuStreamManager.getInstance().getStreamGeneratorFor(NotificationDataRecord.class);
        } catch (StreamNotFoundException e) {
            this.notificationStreamGenerator = notiStreamGenerator;
            Log.d(TAG,"call notificationlistener service3");}
    }

    public NotificationListenService(){
        super();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Received start id ");

        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Notification bind");

        return super.onBind(intent);
    }

    @TargetApi(Build.VERSION_CODES.O)

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.d(TAG,"in posted");

        Log.d(TAG, "Notification received: "+sbn.getPackageName()+":"+sbn.getNotification().tickerText);

        Notification notification = sbn.getNotification();

        try {
            title = notification.extras.get("android.title").toString();
        } catch (Exception e){
            title = "";
        }
        try {
            text = notification.extras.get("android.text").toString();
        } catch (Exception e){
            text = "";
        }

        try {
            subText = notification.extras.get("android.subText").toString();
        } catch (Exception e){
            subText = "";
        }

        try {
            tickerText = notification.tickerText.toString();
        } catch (Exception e){
            tickerText = "";
        }
        try {
            pack = sbn.getPackageName();
        } catch (Exception e){
            pack = "";
        }
        String extra = "";
        for (String key : notification.extras.keySet()) {
            Object value = notification.extras.get(key);
            try {
                Log.d(TAG, String.format("%s %s (%s)", key,
                        value.toString(), value.getClass().getName()));
                extra += value.toString();
            } catch (Exception e) {

            }
        }

//        Log.d(TAG,title + " " + text + " " + subText + " " + tickerText + " " + extra);
        try {
            this.notificationStreamGenerator = (NotificationStreamGenerator) MinukuStreamManager.getInstance().getStreamGeneratorFor(NotificationDataRecord.class);
        } catch (StreamNotFoundException e) {
            e.printStackTrace();
        }
        try {
            detectedtime = ScheduleAndSampleManager.getCurrentTimeInMillis();
            notificationStreamGenerator.setNotificationDataRecord(title, text, subText, tickerText, pack, -1, detectedtime);
            notificationStreamGenerator.updateStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
        Log.d(TAG, ">>>>> something removed. <<<<<");
        if (pack.equals("com.facebook.orca") || pack.equals("jp.naver.line.android") ||
            pack.equals("com.tencent.mm")) {
            removeNotiTime = ScheduleAndSampleManager.getCurrentTimeInMillis();
        }
        Log.d(TAG, "removeNotiTime: " + removeNotiTime);
    }

//    public static long getRemoveNotiTime() {
//        return removeNotiTime;
//    }
}
