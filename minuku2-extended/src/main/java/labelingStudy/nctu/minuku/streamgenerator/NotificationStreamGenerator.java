package labelingStudy.nctu.minuku.streamgenerator;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import labelingStudy.nctu.minuku.Data.appDatabase;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.dao.NotificationDataRecordDAO;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.model.DataRecord.NotificationDataRecord;
import labelingStudy.nctu.minuku.service.NotificationListenService;
import labelingStudy.nctu.minuku.stream.NotificationStream;
import labelingStudy.nctu.minukucore.exception.StreamAlreadyExistsException;
import labelingStudy.nctu.minukucore.exception.StreamNotFoundException;
import labelingStudy.nctu.minukucore.stream.Stream;

/**
 * Created by chiaenchiang on 18/11/2018.
 */

public class NotificationStreamGenerator extends AndroidStreamGenerator<NotificationDataRecord> {

    private Context mContext;
    String TAG = "NotificationStreamGenerator";
    String room = "room";
    private NotificationDataRecordDAO notificationDataRecordDAO;
    private NotificationStream mStream;
    private static NotificationManager notificationManager;
    public static String mNotificaitonTitle = "";
    public static String mNotificaitonText = "";
    public static String mNotificaitonSubText = "";
    public static String mNotificationTickerText = "";
    public static  String mNotificaitonPackageName ="";
    private NotificationListenService notificationlistener;
    public static Integer accessid=-1;

    public static long detectedTime;

    public NotificationStreamGenerator(){
        super();
    }

    @Override
    public void register() {
        Log.d(TAG, "Registering with Stream Manager");
        try {
            MinukuStreamManager.getInstance().register(mStream, NotificationDataRecord.class, this);
        } catch (StreamNotFoundException streamNotFoundException) {
            Log.e(TAG, "One of the streams on which NotificationDataRecord/NotificationStream depends in not found.");
        } catch (StreamAlreadyExistsException streamAlreadyExsistsException) {
            Log.e(TAG, "Another stream which provides NotificationDataRecord/NotificationStream is already registered.");
        }

    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @SuppressLint("ServiceCast")
    public NotificationStreamGenerator(Context applicationContext) {
        super(applicationContext);

        notificationDataRecordDAO = appDatabase.getDatabase(applicationContext).notificationDataRecordDao();
        mContext = applicationContext;
        notificationlistener = new NotificationListenService(this);
        this.mStream = new NotificationStream(Constants.DEFAULT_QUEUE_SIZE);

        //  notificationManager = (android.app.NotificationManager)mContext.getSystemService(mContext.NOTIFICATION_SERVICE);

        mNotificaitonTitle = "Default";
        mNotificaitonText = "Default";
        mNotificaitonSubText = "Default";
        mNotificationTickerText = "Default";
        mNotificaitonPackageName ="Default";
        accessid = -1;
        detectedTime = Constants.INVALID_TIME_VALUE; // -1
        this.register();
    }



    @Override
    public Stream<NotificationDataRecord> generateNewStream() {
        return null;
    }

    @Override
    public boolean updateStream() {

        NotificationDataRecord notificationDataRecord =
                new NotificationDataRecord(mNotificaitonTitle, mNotificaitonText, mNotificaitonSubText
                        , mNotificationTickerText, mNotificaitonPackageName ,accessid);

        mStream.add(notificationDataRecord);
        Log.d(TAG, "Check notification to be sent to event bus " + notificationDataRecord);
        // also post an event.
//        Log.d("creationTime : ", "notiData : "+notificationDataRecord.getCreationTime());
        Log.d(TAG, "NotificationTitle: " + mNotificaitonTitle + ", NotificationText: " + mNotificaitonText + ", NotificationPackageName" + mNotificaitonPackageName);
        EventBus.getDefault().post(notificationDataRecord);

        try {
            Log.d(TAG, "package name: " + mNotificaitonPackageName);
            Log.d(TAG, "time stamp: " + detectedTime);
//            notificationDataRecordDAO.insertAll(notificationDataRecord);
//            List<NotificationDataRecord> notificationDataRecords = notificationDataRecordDAO.getAll();
//            for (NotificationDataRecord l : notificationDataRecords) {
//                Log.d(TAG, " NotificationPackageName: " + String.valueOf(l.getNotificaitonPackageName()));
//                Log.d(TAG, " NotificationTitle: " + String.valueOf(l.getNotificaitonTitle()));
//                Log.d(TAG, " NotificationText: " + String.valueOf(l.getNotificaitonText()));
//            }

        } catch (NullPointerException e) { //Sometimes no data is normal
            e.printStackTrace();
            return false;
        }
        return false;
    }

    @Override
    public long getUpdateFrequency() {
        return 1;
    }

    @Override
    public void sendStateChangeEvent() {}

    @Override
    public void onStreamRegistration() {}

    public void setNotificationDataRecord(String title, String text, String subText, String tickerText,String pack,Integer accessid, long detectedTime){

        this.mNotificaitonTitle = title;
        this.mNotificaitonText = text;
        this.mNotificaitonSubText = subText;
        this.mNotificationTickerText = tickerText;
        this.mNotificaitonPackageName = pack;
        this.accessid = accessid;
        this.detectedTime = detectedTime;
        Log.d(TAG, "title: " + title + " text: " + text + " subText: " + subText + " ticker: " + tickerText + " pack: " + pack);
        Log.d(TAG, "time stamp: " + detectedTime);
    }

    @Override
    public void offer(NotificationDataRecord dataRecord) {}
}
