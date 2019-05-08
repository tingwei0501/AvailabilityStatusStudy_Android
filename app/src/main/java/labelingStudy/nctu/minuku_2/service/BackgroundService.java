/*
 * Copyright (c) 2016.
 *
 * DReflect and Minuku Libraries by Shriti Raj (shritir@umich.edu) and Neeraj Kumar(neerajk@uci.edu) is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * Based on a work at https://github.com/Shriti-UCI/Minuku-2.
 *
 *
 * You are free to (only if you meet the terms mentioned below) :
 *
 * Share — copy and redistribute the material in any medium or format
 * Adapt — remix, transform, and build upon the material
 *
 * The licensor cannot revoke these freedoms as long as you follow the license terms.
 *
 * Under the following terms:
 *
 * Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made. You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 * NonCommercial — You may not use the material for commercial purposes.
 * ShareAlike — If you remix, transform, or build upon the material, you must distribute your contributions under the same license as the original.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 */

package labelingStudy.nctu.minuku_2.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import labelingStudy.nctu.minuku.Utilities.CSVHelper;
import labelingStudy.nctu.minuku.Utilities.ScheduleAndSampleManager;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.manager.MobilityManager;
import labelingStudy.nctu.minuku.manager.SessionManager;
import labelingStudy.nctu.minuku.model.DataRecord.ActivityRecognitionDataRecord;
import labelingStudy.nctu.minuku.model.Session;
import labelingStudy.nctu.minuku.stream.AppUsageStream;
import labelingStudy.nctu.minuku.streamgenerator.AccessibilityStreamGenerator;
import labelingStudy.nctu.minuku.streamgenerator.ActivityRecognitionStreamGenerator;
import labelingStudy.nctu.minuku.streamgenerator.AppUsageStreamGenerator;
import labelingStudy.nctu.minuku.streamgenerator.BatteryStreamGenerator;
import labelingStudy.nctu.minuku.streamgenerator.ConnectivityStreamGenerator;
import labelingStudy.nctu.minuku.streamgenerator.NotificationStreamGenerator;
import labelingStudy.nctu.minuku.streamgenerator.RingerStreamGenerator;
import labelingStudy.nctu.minuku.streamgenerator.SensorStreamGenerator;
import labelingStudy.nctu.minuku.streamgenerator.TransportationModeStreamGenerator;
import labelingStudy.nctu.minuku_2.R;
import labelingStudy.nctu.minuku_2.Receiver.RestarterBroadcastReceiver;
//import labelingStudy.nctu.minuku_2.Receiver.WifiReceiver;
import labelingStudy.nctu.minuku_2.Utils;
import labelingStudy.nctu.minuku_2.controller.Dispatch;
import labelingStudy.nctu.minuku_2.manager.InstanceManager;

public class BackgroundService extends Service {


    private static final String TAG = "BackgroundService";
    private static final String getStatusUrl = "http://13.59.255.194:5000/getSelfStatus";
    private Context mContext;

    final static String CHECK_RUNNABLE_ACTION = "checkRunnable";
    final static String CONNECTIVITY_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";

//    WifiReceiver mWifiReceiver;
    IntentFilter intentFilter;

    MinukuStreamManager streamManager;

    private ScheduledExecutorService mScheduledExecutorService;
    ScheduledFuture<?> mScheduledFuture, mScheduledFutureData, mScheduledSendToServer;

    private int ongoingNotificationID = 42;
    private String ongoingNotificationText = Constants.RUNNING_APP_DECLARATION;

    NotificationManager mNotificationManager;

    public static boolean isBackgroundServiceRunning = false;
    public static boolean isBackgroundRunnableRunning = false;

    // data
    private String ringerMode;
    private Queue<String> ringerModes;

    private String networkType;
    private Queue<String> networkTypes;

    private String activityRecognition;
    private Queue<String> activityRecognitions;

    private int batteryLevel;
    private Queue<Integer> batteryLevels;
    private String batteryChargingState;
    private Queue<String> batteryChargingStates;

    private float sensorProximity;
    private Queue<Float> sensorProximities;

    private long timestampOfScreenInteraction;
    private long periodOfScreenInteraction;
    private boolean isScreenInteractive1;
    private boolean isScreenInteractive5;
    private boolean isScreenInteractive10;
    private boolean isScreenInteractive30;
    private boolean isScreenInteractive60;

    private long timestampOfOpenIMApp;
    private long periodOfOpenIMApp;
    private boolean isOpenIMApp1;
    private boolean isOpenIMApp5;
    private boolean isOpenIMApp10;
    private boolean isOpenIMApp30;
    private boolean isOpenIMApp60;

    private long timestampOfIMNotification;
    private long periodOfIMNotification;
    private boolean isIMNoti1;
    private boolean isIMNoti5;
    private boolean isIMNoti10;
    private boolean isIMNoti30;
    private boolean isIMNoti60;

    private long timestampOfActions;
    private long periodOfActions;
    private boolean hasAction1;
    private boolean hasAction5;
    private boolean hasAction10;
    private boolean hasAction30;
    private boolean hasAction60;

    private RequestQueue mQueue;
    private JsonObjectRequest mJsonObjectRequest;

    private int count = 0;

    private SharedPreferences sharedPrefs;

    public BackgroundService() {
        super();
    }

    @Override
    public void onCreate() {

        sharedPrefs = getSharedPreferences(Constants.sharedPrefString_User, MODE_PRIVATE);
        mContext = this;
        isBackgroundServiceRunning = false;
        isBackgroundRunnableRunning = false;

        streamManager = MinukuStreamManager.getInstance();
        mScheduledExecutorService = Executors.newScheduledThreadPool(Constants.NOTIFICATION_UPDATE_THREAD_SIZE);

        intentFilter = new IntentFilter();
        intentFilter.addAction(CONNECTIVITY_ACTION);
        intentFilter.addAction(Constants.ACTION_CONNECTIVITY_CHANGE);
//        mWifiReceiver = new WifiReceiver();

        ringerModes = new LinkedList<>();
        ringerMode = Constants.INVALID_STRING_VALUE; // NA
        networkTypes = new LinkedList<>();
        networkType = Constants.INVALID_STRING_VALUE; // NA
        activityRecognitions = new LinkedList<>();
        activityRecognition = Constants.INVALID_STRING_VALUE; // NA
        batteryLevels = new LinkedList<>();
        batteryLevel = Constants.INVALID_INT_VALUE; // -1
        batteryChargingStates = new LinkedList<>();
        batteryChargingState = Constants.INVALID_STRING_VALUE; // NA
        sensorProximities = new LinkedList<>();
        sensorProximity = Constants.INVAILD_FLOAT_VALUE; // -1
        timestampOfScreenInteraction = Constants.INVALID_TIME_VALUE; // -1
        periodOfScreenInteraction = Constants.INVALID_TIME_VALUE; // -1
        isScreenInteractive1 = isScreenInteractive5 = isScreenInteractive10 = isScreenInteractive30 = isScreenInteractive60 = false;

        timestampOfOpenIMApp = Constants.INVALID_TIME_VALUE; // -1
        periodOfOpenIMApp = Constants.INVALID_TIME_VALUE; // -1
        isOpenIMApp1 = isOpenIMApp5 = isOpenIMApp10 = isOpenIMApp30 = isOpenIMApp60 = false;

        timestampOfIMNotification = Constants.INVALID_TIME_VALUE; // -1
        periodOfIMNotification = Constants.INVALID_TIME_VALUE; // -1
        isIMNoti1 = isIMNoti5 = isIMNoti10 = isIMNoti30 = isIMNoti60 = false;

        timestampOfActions = Constants.INVALID_TIME_VALUE; // -1
        periodOfActions = Constants.INVALID_TIME_VALUE; // -1
        hasAction1 = hasAction5 = hasAction10 = hasAction30 = hasAction60 = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        CSVHelper.storeToCSV(CSVHelper.CSV_RUNNABLE_CHECK, "isBackgroundServiceRunning ? "+isBackgroundServiceRunning);
        CSVHelper.storeToCSV(CSVHelper.CSV_RUNNABLE_CHECK, "isBackgroundRunnableRunning ? "+isBackgroundRunnableRunning);

        String onStart = "BackGround, start service";
        CSVHelper.storeToCSV(CSVHelper.CSV_ESM, onStart);
        CSVHelper.storeToCSV(CSVHelper.CSV_CAR, onStart);

        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        createSurveyNotificationChannel();
        createOngoingNotificationChannel();

        //make the WifiReceiver start sending availSite to the server.
//        registerReceiver(mWifiReceiver, intentFilter);
        registerConnectivityNetworkMonitorForAPI21AndUp();

        IntentFilter checkRunnableFilter = new IntentFilter(CHECK_RUNNABLE_ACTION);
        registerReceiver(CheckRunnableReceiver, checkRunnableFilter);

        //building the ongoing notification to the foreground
        startForeground(ongoingNotificationID, getOngoingNotification(ongoingNotificationText));

        if (!isBackgroundServiceRunning) {

            Log.d(TAG, "Initialize the Manager");

            isBackgroundServiceRunning = true;

            CSVHelper.storeToCSV(CSVHelper.CSV_RUNNABLE_CHECK, "Going to judge the condition is ? "+(!InstanceManager.isInitialized()));

            if (!InstanceManager.isInitialized()) {

                CSVHelper.storeToCSV(CSVHelper.CSV_RUNNABLE_CHECK, "Going to start the runnable.");

                InstanceManager.getInstance(this);
                SessionManager.getInstance(this);
                MobilityManager.getInstance(this);

                updateNotificationAndStreamManagerThread();
            }
        }

        return START_REDELIVER_INTENT; //START_STICKY_COMPATIBILITY;
    }

    private void updateNotificationAndStreamManagerThread(){

        mScheduledFuture = mScheduledExecutorService.scheduleAtFixedRate(
                updateStreamManagerRunnable,
                10,
                Constants.STREAM_UPDATE_FREQUENCY, // 10 sec
                TimeUnit.SECONDS);

        mScheduledFutureData = mScheduledExecutorService.scheduleAtFixedRate(
                getStreamDataRunnable,
                Constants.GET_STREAM_DATA_DELAY,
                Constants.GET_STREAM_DATA_FREQUENCY, // 20 sec
                TimeUnit.SECONDS
        );

        mScheduledSendToServer = mScheduledExecutorService.scheduleAtFixedRate(
                getAvailabilityFromServerRunnable,
                Constants.GET_AVAILABILITY_FROM_SERVER_DELAY, //TODO: should be longer:user need to register & login
                Constants.GET_AVAILABILITY_FROM_SERVER_FREQUENCY, // 1 min
                TimeUnit.SECONDS
        );
    }

    Runnable updateStreamManagerRunnable = new Runnable() {
        @Override
        public void run() {

            Log.d(TAG, "--------- start updateStreamManagerRunnable ---------");
            try {
                CSVHelper.storeToCSV(CSVHelper.CSV_RUNNABLE_CHECK, "isBackgroundServiceRunning ? "+isBackgroundServiceRunning);
                CSVHelper.storeToCSV(CSVHelper.CSV_RUNNABLE_CHECK, "isBackgroundRunnableRunning ? "+isBackgroundRunnableRunning);

                streamManager.updateStreamGenerators();
            } catch (Exception e){

                CSVHelper.storeToCSV(CSVHelper.CSV_RUNNABLE_CHECK, "Background, service update, stream, Exception");
                CSVHelper.storeToCSV(CSVHelper.CSV_RUNNABLE_CHECK, Utils.getStackTrace(e));
            }
        }
    };

    Runnable getStreamDataRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "++++++++++ start getStreamDataRunnable +++++++++");
            getRingerData();
            getNetworkType();
            getActivityRecognition();
            getBatteryLevel();
            getBatteryChargingState();
            getSensorProximity();
            getScreenInteraction();
            getOpenIMApp();
            getIsIMNoti();
            getActions();
        }
    };

    Runnable getAvailabilityFromServerRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "!!!!!!!! Get Availability Status From Server !!!!!!!!");
            if (!ringerMode.equals("NA") && !networkType.equals("NA") &&
                !activityRecognition.equals("NA") && batteryLevel != -1 &&
                !batteryChargingState.equals("NA") && sensorProximity != -1) {

                JSONObject data = getDataToServer();

                mQueue = Volley.newRequestQueue(mContext);

                mJsonObjectRequest = new JsonObjectRequest(Request.Method.POST, getStatusUrl, data,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d(TAG, response.toString());

                                try {
                                    sharedPrefs.edit()
                                            .putInt("status", response.getInt("status"))
                                            .putLong("updateTime", response.getLong("createdTime"))
                                            .putString("way", response.getString("presentWay"))
                                            .putString("statusText", response.getString("statusText"))
                                            .putInt("statusColor", response.getInt("statusColor"))
                                            .commit();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Log.d(TAG, "error response");
                        Log.d(TAG, error.getMessage());
                    }
                });
                mQueue.add(mJsonObjectRequest);
            }

        }
    };

    private JSONObject getDataToServer() {
        JSONObject data = new JSONObject();
        try {
            data.put("id", sharedPrefs.getString("id", ""));
            data.put("ringerMode", ringerMode);
            data.put("networkType", networkType);
            data.put("activityRecognition", activityRecognition);
            data.put("batteryLevel", batteryLevel);
            data.put("batteryChargingState", batteryChargingState);
            data.put("sensorProximity", sensorProximity);
            data.put("isScreenInteractive1", isScreenInteractive1);
            data.put("isScreenInteractive5", isScreenInteractive5);
            data.put("isScreenInteractive10", isScreenInteractive10);
            data.put("isScreenInteractive30", isScreenInteractive30);
            data.put("isScreenInteractive60", isScreenInteractive60);

            data.put("isOpenIMApp1", isOpenIMApp1);
            data.put("isOpenIMApp5", isOpenIMApp5);
            data.put("isOpenIMApp10", isOpenIMApp10);
            data.put("isOpenIMApp30", isOpenIMApp30);
            data.put("isOpenIMApp60", isOpenIMApp60);

            data.put("isIMNoti1", isIMNoti1);
            data.put("isIMNoti5", isIMNoti5);
            data.put("isIMNoti10", isIMNoti10);
            data.put("isIMNoti30", isIMNoti30);
            data.put("isIMNoti60", isIMNoti60);

            data.put("hasAction1", hasAction1);
            data.put("hasAction5", hasAction5);
            data.put("hasAction10", hasAction10);
            data.put("hasAction30", hasAction30);
            data.put("hasAction60", hasAction60);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }

    private void getRingerData() {

//        Log.d(TAG, "ringerMode: " + RingerStreamGenerator.ringerMode);

//        Log.d(TAG, "size1: " + ringerModes.size());
        if (ringerModes.size() == Constants.STREAM_DATA_CAPACITY) {
            ringerModes.poll();
        }

        if (ringerMode.equals(RingerStreamGenerator.ringerMode)) {
            ringerModes.add(ringerMode);
//            for (String r : ringerModes) {
//                Log.d(TAG, "count: " + count++ + " " + r);
//            }
        } else {
            ringerModes.add(RingerStreamGenerator.ringerMode);
//            for (String r : ringerModes) {
//                Log.d(TAG, "count: " + count++ + " " + r);
//            }
        }

//        Log.d(TAG, "size2: " + ringerModes.size());
        ringerMode = RingerStreamGenerator.ringerMode;
    }

    private void getNetworkType() {
//        Log.d(TAG, "NetworkType: " + ConnectivityStreamGenerator.mNetworkType);
        if (networkTypes.size() == Constants.STREAM_DATA_CAPACITY) {
            networkTypes.poll();
        }
//        Log.d(TAG, "size1: " + networkTypes.size());
        if (networkType.equals(ConnectivityStreamGenerator.mNetworkType)) {
            networkTypes.add(networkType);
//            for (String n : networkTypes) {
//                Log.d(TAG, "count: " + count++ + " " + n);
//            }
        } else {
            networkTypes.add(ConnectivityStreamGenerator.mNetworkType);
//            for (String n : networkTypes) {
//                Log.d(TAG, "count: " + count++ + " " + n);
//            }
        }
//        Log.d(TAG, "size2: " + networkTypes.size());
        networkType = ConnectivityStreamGenerator.mNetworkType;
    }

    private void getActivityRecognition() {
//        Log.d(TAG, "Activity Recognition: " + ActivityRecognitionStreamGenerator.getActivityNameFromType(ActivityRecognitionStreamGenerator.sMostProbableActivity.getType()));
        if (activityRecognitions.size() == Constants.STREAM_DATA_CAPACITY) {
            activityRecognitions.poll();
        }
//        Log.d(TAG, "size1: " + activityRecognitions.size());
        if (activityRecognition.equals(ActivityRecognitionStreamGenerator.getActivityNameFromType(ActivityRecognitionStreamGenerator.sMostProbableActivity.getType()))) {
            activityRecognitions.add(activityRecognition);
//            for (String a : activityRecognitions) {
//                Log.d(TAG, "count " + count++ + " " + a);
//            }
        } else {
            activityRecognitions.add(ActivityRecognitionStreamGenerator.getActivityNameFromType(ActivityRecognitionStreamGenerator.sMostProbableActivity.getType()));
//            for (String a : activityRecognitions) {
//                Log.d(TAG, "count " + count++ + " " + a);
//            }
        }

//        Log.d(TAG, "size2: " + activityRecognitions.size());
        activityRecognition = ActivityRecognitionStreamGenerator.getActivityNameFromType(ActivityRecognitionStreamGenerator.sMostProbableActivity.getType());
    }

    private void getBatteryLevel() {
//        Log.d(TAG, "Battery Level: " + BatteryStreamGenerator.mBatteryLevel);
        if (batteryLevels.size() == Constants.STREAM_DATA_CAPACITY) {
            batteryLevels.poll();
        }
//        Log.d(TAG, "size1: " + batteryLevels.size());
        if (batteryLevel == BatteryStreamGenerator.mBatteryLevel) {
            batteryLevels.add(batteryLevel);
//            for (Integer b : batteryLevels) {
//                Log.d(TAG, "count " + count++ + " " + b);
//            }
        } else {
            batteryLevels.add(BatteryStreamGenerator.mBatteryLevel);
//            for (Integer b : batteryLevels) {
//                Log.d(TAG, "count " + count++ + " " + b);
//            }
        }

//        Log.d(TAG, "size2: " + batteryLevels.size());
        batteryLevel = BatteryStreamGenerator.mBatteryLevel;
    }

    private void getBatteryChargingState() {
//        Log.d(TAG, "Battery Charging State: " + BatteryStreamGenerator.mBatteryChargingState);
        if (batteryChargingStates.size() == Constants.STREAM_DATA_CAPACITY) {
            batteryChargingStates.poll();
        }
//        Log.d(TAG, "size1: " + batteryChargingStates.size());
        if (batteryChargingState == BatteryStreamGenerator.mBatteryChargingState) {
            batteryChargingStates.add(batteryChargingState);
//            for (String b : batteryChargingStates) {
//                Log.d(TAG, "count " + count++ + " " + b);
//            }
        } else {
            batteryChargingStates.add(BatteryStreamGenerator.mBatteryChargingState);
//            for (String b : batteryChargingStates) {
//                Log.d(TAG, "count " + count++ + " " + b);
//            }
        }

//        Log.d(TAG, "size2: " + batteryChargingStates.size());
        batteryChargingState = BatteryStreamGenerator.mBatteryChargingState;

    }

    private void getSensorProximity() {
//        Log.d(TAG, "Sensor Proximity: " + SensorStreamGenerator.proximity);
        if (sensorProximities.size() == Constants.STREAM_DATA_CAPACITY) {
            sensorProximities.poll();
        }
//        Log.d(TAG, "size1: " + sensorProximitys.size());
        if (sensorProximity == SensorStreamGenerator.proximity) {
            sensorProximities.add(sensorProximity);
//            for (float s : sensorProximitys) {
//                Log.d(TAG, "count " + count++ + " " + s);
//            }
        } else {
            sensorProximities.add(SensorStreamGenerator.proximity);
//            for (float s : sensorProximitys) {
//                Log.d(TAG, "count " + count++ + " " + s);
//            }
        }

//        Log.d(TAG, "size2: " + sensorProximitys.size());
        sensorProximity = SensorStreamGenerator.proximity;
    }

    private void getScreenInteraction() {
//        Log.d(TAG, "Screen Interaction: " + AppUsageStreamGenerator.Screen_Status);

//
//        if (timestampOfScreenInteraction != -1) {
//            long currentTime = ScheduleAndSampleManager.getCurrentTimeInMillis();
//            periodOfScreenInteraction = (currentTime - timestampOfScreenInteraction)/1000; // seconds
//            Log.d(TAG, "time period: " + periodOfScreenInteraction);
//        }
        if (AppUsageStreamGenerator.Screen_Status.equals("Interactive")) {
//            Log.d(TAG, "timestamp: " + ScheduleAndSampleManager.getTimeString(AppUsageStreamGenerator.detectedTime));
            timestampOfScreenInteraction = AppUsageStreamGenerator.detectedTime;
        }
        if (timestampOfScreenInteraction != -1) {
            long currentTime = ScheduleAndSampleManager.getCurrentTimeInMillis();
            periodOfScreenInteraction = (currentTime - timestampOfScreenInteraction) / 1000; // seconds
//            Log.d(TAG, "time period: " + periodOfScreenInteraction);
        }
        isScreenInteractive1 = isScreenInteractive5 = isScreenInteractive10 = isScreenInteractive30 = isScreenInteractive60 = false;
        if (periodOfScreenInteraction != -1) {
            if (periodOfScreenInteraction <= 60) {
                isScreenInteractive1 = true;
                isScreenInteractive5 = true;
                isScreenInteractive10 = true;
                isScreenInteractive30 = true;
                isScreenInteractive60 = true;
            } else if (periodOfScreenInteraction <= 300) {
                isScreenInteractive5 = true;
                isScreenInteractive10 = true;
                isScreenInteractive30 = true;
                isScreenInteractive60 = true;
            } else if (periodOfScreenInteraction <= 600) {
                isScreenInteractive10 = true;
                isScreenInteractive30 = true;
                isScreenInteractive60 = true;
            } else if (periodOfScreenInteraction <= 1800) {
                isScreenInteractive30 = true;
                isScreenInteractive60 = true;
            } else if (periodOfScreenInteraction <= 3600) {
                isScreenInteractive60 = true;
            }
        }

//        Log.d(TAG, "isScreenInteractive1: " + isScreenInteractive1);
//        Log.d(TAG, "isScreenInteractive5: " + isScreenInteractive5);
//        Log.d(TAG, "isScreenInteractive10: " + isScreenInteractive10);
//        Log.d(TAG, "isScreenInteractive30: " + isScreenInteractive30);
//        Log.d(TAG, "isScreenInteractive60: " + isScreenInteractive60);
    }

    private void getOpenIMApp() {
//        Log.d(TAG, "Latest Used App: " + AppUsageStreamGenerator.mLastestForegroundPackage);
        if (AppUsageStreamGenerator.mLastestForegroundPackage.equals("com.facebook.orca") ||
            AppUsageStreamGenerator.mLastestForegroundPackage.equals("jp.naver.line.android")) {
//            Log.d(TAG, "timestamp: " + ScheduleAndSampleManager.getTimeString(AppUsageStreamGenerator.detectedTime));
            timestampOfOpenIMApp = AppUsageStreamGenerator.detectedTime;
        }
        if (timestampOfOpenIMApp != -1) {
            long currentTime = ScheduleAndSampleManager.getCurrentTimeInMillis();
            periodOfOpenIMApp = (currentTime - timestampOfOpenIMApp) / 1000;
//            Log.d(TAG, "time period: " + periodOfOpenIMApp);
        }
        isOpenIMApp1 = isOpenIMApp5 = isOpenIMApp10 = isOpenIMApp30 = isOpenIMApp60 = false;
        if (periodOfOpenIMApp != -1) {
            if (periodOfOpenIMApp <= 60) {
                isOpenIMApp1 = true;
                isOpenIMApp5 = true;
                isOpenIMApp10 = true;
                isOpenIMApp30 = true;
                isOpenIMApp60 = true;
            } else if (periodOfOpenIMApp <= 300) {
                isOpenIMApp5 = true;
                isOpenIMApp10 = true;
                isOpenIMApp30 = true;
                isOpenIMApp60 = true;
            } else if (periodOfOpenIMApp <= 600) {
                isOpenIMApp10 = true;
                isOpenIMApp30 = true;
                isOpenIMApp60 = true;
            } else if (periodOfOpenIMApp <= 1800) {
                isOpenIMApp30 = true;
                isOpenIMApp60 = true;
            } else if (periodOfOpenIMApp <= 3600) {
                isOpenIMApp60 = true;
            }
//            Log.d(TAG, "isOpenIMApp1: " + isOpenIMApp1);
//            Log.d(TAG, "isOpenIMApp5: " + isOpenIMApp5);
//            Log.d(TAG, "isOpenIMApp10: " + isOpenIMApp10);
//            Log.d(TAG, "isOpenIMApp30: " + isOpenIMApp30);
//            Log.d(TAG, "isOpenIMApp60: " + isOpenIMApp60);
        }
    }

    private void getIsIMNoti() {
//        Log.d(TAG, "Im notification: " + NotificationStreamGenerator.mNotificaitonPackageName);
        if (NotificationStreamGenerator.mNotificaitonPackageName.equals("com.facebook.orca") ||
                AppUsageStreamGenerator.mLastestForegroundPackage.equals("jp.naver.line.android")) {
//            Log.d(TAG, "timestamp: " + ScheduleAndSampleManager.getTimeString(NotificationStreamGenerator.detectedTime));
            timestampOfIMNotification = NotificationStreamGenerator.detectedTime;
        }
        if (timestampOfIMNotification != -1) {
            long currentTime = ScheduleAndSampleManager.getCurrentTimeInMillis();
            periodOfIMNotification = (currentTime - timestampOfIMNotification) / 1000;
//            Log.d(TAG, "time period: " + periodOfIMNotification);
        }
        isIMNoti1 = isIMNoti5 = isIMNoti10 = isIMNoti30 = isIMNoti60 = false;
        if (periodOfIMNotification != -1) {
            if (periodOfIMNotification <= 60) {
                isIMNoti1 = true;
                isIMNoti5 = true;
                isIMNoti10 = true;
                isIMNoti30 = true;
                isIMNoti60 = true;
            } else if (periodOfIMNotification <= 300) {
                isIMNoti5 = true;
                isIMNoti10 = true;
                isIMNoti30 = true;
                isIMNoti60 = true;
            } else if (periodOfIMNotification <= 600) {
                isIMNoti10 = true;
                isIMNoti30 = true;
                isIMNoti60 = true;
            } else if (periodOfIMNotification <= 1800) {
                isIMNoti30 = true;
                isIMNoti60 = true;
            } else if (periodOfIMNotification <= 3600) {
                isIMNoti60 = true;
            }
//            Log.d(TAG, "isIMNoti1: " + isIMNoti1);
//            Log.d(TAG, "isIMNoti5: " + isIMNoti5);
//            Log.d(TAG, "isIMNoti10: " + isIMNoti10);
//            Log.d(TAG, "isIMNoti30: " + isIMNoti30);
//            Log.d(TAG, "isIMNoti60: " + isIMNoti60);
        }
    }

    private void getActions() {
//        Log.d(TAG, "Actions on phone: ");
        if ((!AccessibilityStreamGenerator.type.equals("") && !AccessibilityStreamGenerator.type.equals("NA")) || 
            (!AccessibilityStreamGenerator.extra.equals("") && !AccessibilityStreamGenerator.extra.equals("NA"))) {
            timestampOfActions = AccessibilityStreamGenerator.detectedTime;
        } else if (AppUsageStreamGenerator.Screen_Status.equals("Interactive")) {
            timestampOfActions = AppUsageStreamGenerator.detectedTime;
        }
        if (timestampOfActions != -1) {
            long currentTime = ScheduleAndSampleManager.getCurrentTimeInMillis();
            periodOfActions = (currentTime - timestampOfActions) / 1000;
//            Log.d(TAG, "time period: " + periodOfActions);
        }
        hasAction1 = hasAction5 = hasAction10 = hasAction30 = hasAction60 = false;
        if (periodOfActions != -1) {
            if (periodOfActions <= 60) {
                hasAction1 = true;
                hasAction5 = true;
                hasAction10 = true;
                hasAction30 = true;
                hasAction60 = true;
            } else if (periodOfActions <= 300) {
                hasAction5 = true;
                hasAction10 = true;
                hasAction30 = true;
                hasAction60 = true;
            } else if (periodOfActions <= 600) {
                hasAction10 = true;
                hasAction30 = true;
                hasAction60 = true;
            } else if (periodOfActions <= 1800) {
                hasAction30 = true;
                hasAction60 = true;
            } else if (periodOfActions <= 3600) {
                hasAction60 = true;
            }
//            Log.d(TAG, "hasAction1: " + hasAction1);
//            Log.d(TAG, "hasAction5: " + hasAction5);
//            Log.d(TAG, "hasAction10: " + hasAction10);
//            Log.d(TAG, "hasAction30: " + hasAction30);
//            Log.d(TAG, "hasAction60: " + hasAction60);
        }
    }

    private Notification getOngoingNotification(String text){

        Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle();
        bigTextStyle.setBigContentTitle(Constants.APP_NAME);
        bigTextStyle.bigText(text);

        Intent resultIntent = new Intent(this, Dispatch.class);
        PendingIntent pending = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder noti = new Notification.Builder(this)
                .setContentTitle(Constants.APP_FULL_NAME)
                .setContentText(text)
                .setStyle(bigTextStyle)
                .setContentIntent(pending)
                .setAutoCancel(true)
                .setOngoing(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return noti
                    .setSmallIcon(getNotificationIcon(noti))
                    .setChannelId(Constants.ONGOING_CHANNEL_ID)
                    .build();
        } else {
            return noti
                    .setSmallIcon(getNotificationIcon(noti))
                    .build();
        }
    }

    private int getNotificationIcon(Notification.Builder notificationBuilder) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            notificationBuilder.setColor(Color.TRANSPARENT);
            return R.drawable.muilab_icon_noti;

        }
        return R.drawable.muilab_icon;
    }

    @Override
    public void onDestroy() {
//        super.onDestroy();

//        stopTheSessionByServiceClose();

        String onDestroy = "BackGround, onDestroy";
        CSVHelper.storeToCSV(CSVHelper.CSV_ESM, onDestroy);
        CSVHelper.storeToCSV(CSVHelper.CSV_CAR, onDestroy);

        sendBroadcastToStartService();

        checkingRemovedFromForeground();
        removeRunnable();

        isBackgroundServiceRunning = false;

        mNotificationManager.cancel(ongoingNotificationID);

        Log.d(TAG, "Destroying service. Your state might be lost!");


//        unregisterReceiver(mWifiReceiver);
        unregisterReceiver(CheckRunnableReceiver);
    }

    @Override
    public void onTaskRemoved(Intent intent){
        super.onTaskRemoved(intent);

        sendBroadcastToStartService();

        checkingRemovedFromForeground();
        removeRunnable();

        isBackgroundServiceRunning = false;

        mNotificationManager.cancel(ongoingNotificationID);

        String onTaskRemoved = "BackGround, onTaskRemoved";
        CSVHelper.storeToCSV(CSVHelper.CSV_CheckService_alive, onTaskRemoved);

    }

    private void registerConnectivityNetworkMonitorForAPI21AndUp() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkRequest.Builder builder = new NetworkRequest.Builder();

        connectivityManager.registerNetworkCallback(
                builder.build(),
                new ConnectivityManager.NetworkCallback() {

                    @Override
                    public void onAvailable(Network network) {
                        /*sendBroadcast(
                                getConnectivityIntent("onAvailable")
                        );*/
                    }

                    @Override
                    public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities){
                        sendBroadcast(
                                getConnectivityIntent("onCapabilitiesChanged : "+networkCapabilities.toString())
                        );
                    }

                    @Override
                    public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
                        /*sendBroadcast(
                                getConnectivityIntent("onLinkPropertiesChanged : "+linkProperties.toString())
                        );*/
                    }

                    @Override
                    public void onLosing(Network network, int maxMsToLive) {
                        /*sendBroadcast(
                                getConnectivityIntent("onLosing")
                        );*/
                    }

                    @Override
                    public void onLost(Network network) {
                        /*sendBroadcast(
                                getConnectivityIntent("onLost")
                        );*/
                    }
                }
        );

    }

    private void checkingRemovedFromForeground(){

        Log.d(TAG,"stopForeground");

        stopForeground(true);

        try {

            unregisterReceiver(CheckRunnableReceiver);
        }catch (IllegalArgumentException e){

        }

        mScheduledExecutorService.shutdown();
    }

//    private void stopTheSessionByServiceClose(){
//
//        //if the background service is killed, set the end time of the ongoing trip (if any) using the current timestamp
//        if (SessionManager.getOngoingSessionIdList().size()>0){
//
//            Session session = SessionManager.getSession(SessionManager.getOngoingSessionIdList().get(0)) ;
//
//            //if we end the current session, we should update its time and set a long enough flag
//            if (session.getEndTime()==0){
//                long endTime = ScheduleAndSampleManager.getCurrentTimeInMillis();
//                session.setEndTime(endTime);
//            }
//
//            //end the current session
//            SessionManager.endCurSession(session);
//
//        }
//    }

    private void removeRunnable(){

        mScheduledFuture.cancel(true);
    }

    private void sendBroadcastToStartService(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            sendBroadcast(new Intent(this, RestarterBroadcastReceiver.class).setAction(Constants.CHECK_SERVICE_ACTION));
        } else {

            Intent checkServiceIntent = new Intent(Constants.CHECK_SERVICE_ACTION);
            sendBroadcast(checkServiceIntent);
        }
    }

    private Intent getConnectivityIntent(String message) {

        Intent intent = new Intent();

        intent.setAction(Constants.ACTION_CONNECTIVITY_CHANGE);

        intent.putExtra("message", message);

        return intent;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void createOngoingNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = Constants.ONGOING_CHANNEL_NAME;
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(Constants.ONGOING_CHANNEL_ID, name, importance);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void createSurveyNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = Constants.SURVEY_CHANNEL_NAME;
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(Constants.SURVEY_CHANNEL_ID, name, importance);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    BroadcastReceiver CheckRunnableReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(CHECK_RUNNABLE_ACTION)) {

                Log.d(TAG, "[check runnable] going to check if the runnable is running");

                CSVHelper.storeToCSV(CSVHelper.CSV_RUNNABLE_CHECK, "going to check if the runnable is running");
                CSVHelper.storeToCSV(CSVHelper.CSV_RUNNABLE_CHECK, "is the runnable running ? " + isBackgroundRunnableRunning);

                if (!isBackgroundRunnableRunning) {

                    Log.d(TAG, "[check runnable] the runnable is not running, going to restart it.");

                    CSVHelper.storeToCSV(CSVHelper.CSV_RUNNABLE_CHECK, "the runnable is not running, going to restart it");

                    updateNotificationAndStreamManagerThread();

                    Log.d(TAG, "[check runnable] the runnable is restarted.");

                    CSVHelper.storeToCSV(CSVHelper.CSV_RUNNABLE_CHECK, "the runnable is restarted");
                }

                PendingIntent pi = PendingIntent.getBroadcast(BackgroundService.this, 0, new Intent(CHECK_RUNNABLE_ACTION), 0);

                AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
                alarm.set(
                        AlarmManager.RTC_WAKEUP,
                        System.currentTimeMillis() + Constants.PROMPT_SERVICE_REPEAT_MILLISECONDS,
                        pi
                );
            }
        }
    };

}
