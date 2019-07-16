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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
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
import labelingStudy.nctu.minuku.service.MobileAccessibilityService;
import labelingStudy.nctu.minuku.streamgenerator.AppUsageStreamGenerator;
import labelingStudy.nctu.minuku.streamgenerator.RingerStreamGenerator;
import labelingStudy.nctu.minuku_2.HomePage;
import labelingStudy.nctu.minuku_2.Questionnaire.SelfQuestionnaire;
import labelingStudy.nctu.minuku_2.R;
import labelingStudy.nctu.minuku_2.Receiver.RestarterBroadcastReceiver;
import labelingStudy.nctu.minuku_2.Utils;
import labelingStudy.nctu.minuku_2.controller.Dispatch;
import labelingStudy.nctu.minuku_2.manager.InstanceManager;

import static labelingStudy.nctu.minuku_2.manager.renderManager.randomPresentWay;
import static labelingStudy.nctu.minuku_2.manager.renderManager.randomStatusForm;
import static labelingStudy.nctu.minuku_2.manager.renderManager.rateGetText;

public class BackgroundService extends Service {


    private static final String TAG = "BackgroundService";
    private Context mContext;

    final static String CHECK_RUNNABLE_ACTION = "checkRunnable";
    final static String CONNECTIVITY_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";

//    WifiReceiver mWifiReceiver;
    IntentFilter intentFilter;

    MinukuStreamManager streamManager;

    private ScheduledExecutorService mScheduledExecutorService;
    ScheduledFuture<?> mScheduledFuture, mScheduledFutureData, mScheduledSendToServer, mScheduledAfterEdit;

    private int ongoingNotificationID = 42;
    private int notifyNotificationID = 11;
    private int notifyNotificationCode = 300;
    private String ongoingNotificationText = Constants.RUNNING_APP_DECLARATION;

    NotificationManager mNotificationManager;

    public static boolean isBackgroundServiceRunning = false;
    public static boolean isBackgroundRunnableRunning = false;
    public static boolean isHandleAfterEdit = false;
    public static int afterEditTimePeriod;

    private int lastStatus;
    // data
    private long typingTime;
    private long latestUseIMTime;
    private long screenInteractiveTime;
    private String ringerMode;
    private float imUsage = 50;

    private ArrayList<Integer> statusList;
    private int index = 0;

//    private String networkType;
//    private Queue<String> networkTypes;
//
//    private int batteryLevel;
//    private Queue<Integer> batteryLevels;
//
//    private float sensorProximity;
//    private Queue<Float> sensorProximities;

//    private long timestampOfOpenIMApp;
//    private long periodOfOpenIMApp;
//    private boolean isOpenIMApp1;
//    private boolean isOpenIMApp5;
//    private boolean isOpenIMApp10;
//    private boolean isOpenIMApp30;
//    private boolean isOpenIMApp60;

    private RequestQueue mQueue;
    private JsonObjectRequest mJsonObjectRequest;

    private SharedPreferences sharedPrefs;

    // user status: 系統算ㄉ
    private int userStatus;
    private long userShowTime;
    private String userShowTimeString;
    private String userWay;
    private String userStatusForm;
    private String userStatusText;
    private int userColor;
    // user status: 自行更改的狀態
    private boolean afterEdit;
    private int userStatusEdit;
    private long userShowTimeEdit;
    private String userShowTimeStringEdit;
    private String userWayEdit;
    private String userStatusFormEdit;
    private String userStatusTextEdit;
    private int userColorEdit;

    public BackgroundService() {
        super();
    }

    @Override
    public void onCreate() {

        sharedPrefs = getSharedPreferences(Constants.sharedPrefString_User, MODE_PRIVATE);
        mContext = this;
        isBackgroundServiceRunning = false;
        isBackgroundRunnableRunning = false;
        lastStatus = -1;

        streamManager = MinukuStreamManager.getInstance();
        mScheduledExecutorService = Executors.newScheduledThreadPool(Constants.NOTIFICATION_UPDATE_THREAD_SIZE);

//        intentFilter = new IntentFilter();
//        intentFilter.addAction(CONNECTIVITY_ACTION);
//        intentFilter.addAction(Constants.ACTION_CONNECTIVITY_CHANGE);
//        mWifiReceiver = new WifiReceiver();

        latestUseIMTime = Constants.INVALID_TIME_VALUE;
        typingTime = Constants.INVALID_TIME_VALUE;
        screenInteractiveTime = Constants.INVALID_TIME_VALUE;
        ringerMode = Constants.INVALID_STRING_VALUE;

        statusList = new ArrayList<>(Collections.nCopies(Constants.STATUS_CAPACITY, 50));
//        ringerModes = new LinkedList<>();
//        ringerMode = Constants.INVALID_STRING_VALUE; // NA
//        networkTypes = new LinkedList<>();
//        networkType = Constants.INVALID_STRING_VALUE; // NA
//        batteryLevels = new LinkedList<>();
//        batteryLevel = Constants.INVALID_INT_VALUE; // -1
//        batteryChargingStates = new LinkedList<>();
//        batteryChargingState = Constants.INVALID_STRING_VALUE; // NA
//        sensorProximities = new LinkedList<>();
//        sensorProximity = Constants.INVAILD_FLOAT_VALUE; // -1
//        timestampOfScreenInteraction = Constants.INVALID_TIME_VALUE; // -1
//        periodOfScreenInteraction = Constants.INVALID_TIME_VALUE; // -1
//        isScreenInteractive1 = isScreenInteractive5 = isScreenInteractive10 = isScreenInteractive30 = isScreenInteractive60 = false;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        CSVHelper.storeToCSV(CSVHelper.CSV_RUNNABLE_CHECK, "isBackgroundServiceRunning ? "+isBackgroundServiceRunning);
        CSVHelper.storeToCSV(CSVHelper.CSV_RUNNABLE_CHECK, "isBackgroundRunnableRunning ? "+isBackgroundRunnableRunning);

        String onStart = "BackGround, start service";
        CSVHelper.storeToCSV(CSVHelper.CSV_ESM, onStart);
//        CSVHelper.storeToCSV(CSVHelper.CSV_CAR, onStart);

        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        createSurveyNotificationChannel();
        createOngoingNotificationChannel();
        createEditNotificationChannel();

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
            //沒有進 runnable (fixed)
            if (!InstanceManager.isInitialized()) {
                Log.d(TAG, "not Initialized");
                CSVHelper.storeToCSV(CSVHelper.CSV_RUNNABLE_CHECK, "Going to start the runnable.");

                InstanceManager.getInstance(this);
                SessionManager.getInstance(this);
                MobilityManager.getInstance(this);

            }
            updateNotificationAndStreamManagerThread(); //
        }

        return START_REDELIVER_INTENT; //START_STICKY_COMPATIBILITY;
    }

    private void updateNotificationAndStreamManagerThread(){
        Log.d(TAG, "in updateNotificationAndStreamManagerThread");

        mScheduledFuture = mScheduledExecutorService.scheduleAtFixedRate(
                updateStreamManagerRunnable,
                10,
                Constants.STREAM_UPDATE_FREQUENCY, // 10 sec
                TimeUnit.SECONDS);

        mScheduledFutureData = mScheduledExecutorService.scheduleAtFixedRate(
                getIMUsageDataRunnable,
                Constants.GET_STREAM_DATA_DELAY,
                Constants.GET_STREAM_DATA_FREQUENCY, // 20 sec
                TimeUnit.SECONDS
        );

        mScheduledSendToServer = mScheduledExecutorService.scheduleAtFixedRate(
                updateAvailabilityStatusRunnable,
                Constants.GET_AVAILABILITY_FROM_SERVER_DELAY, //TODO: should be longer:user need to register & login
//                Constants.GET_AVAILABILITY_FROM_SERVER_FREQUENCY, // 2 min
                Constants.GET_AVAILABILITY_FROM_SERVER_FREQUENCY, // TODO: 測試時間會跟實際不同
                TimeUnit.SECONDS
        );
    }

    private void handleAfterEditThread(int afterEditTimePeriod) {
        Log.d(TAG, "in handleAfterEditThread");
        Log.d(TAG, "afterEditTimePeriod: " + afterEditTimePeriod);
        mScheduledAfterEdit = mScheduledExecutorService.schedule(
                handleAfterEditRunnable,
                afterEditTimePeriod,
                TimeUnit.MINUTES
        );
    }

    Runnable handleAfterEditRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "in handleAfterEditRunnable");
            sharedPrefs.edit()
                    .putBoolean("afterEdit", false)
                    .commit();
            mScheduledAfterEdit.cancel(true);
        }
    };

    Runnable updateStreamManagerRunnable = new Runnable() {
        @Override
        public void run() {

//            Log.d(TAG, "--------- start updateStreamManagerRunnable ---------");
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

    Runnable getIMUsageDataRunnable = new Runnable() {
        @Override
        public void run() {
            long currentTime = ScheduleAndSampleManager.getCurrentTimeInMillis();
//            Log.d(TAG, ".........get IM Usage runnable.........");
            latestUseIMTime = AppUsageStreamGenerator.getLatestUseIMTime();
            typingTime = MobileAccessibilityService.getTypingTime();
            Log.d(TAG, ">> typing time >> " + ScheduleAndSampleManager.getTimeString(typingTime));
            Log.d(TAG, ">> latest use IM App time >> " + ScheduleAndSampleManager.getTimeString(latestUseIMTime));

            typingTime = (currentTime - typingTime) / 1000;
            latestUseIMTime = (currentTime - latestUseIMTime) / 1000;

//            Log.d(TAG, "typing time: " + typingTime);
//            Log.d(TAG, "use IM time: " + latestUseIMTime);
            if (typingTime <= 30 && latestUseIMTime <= 30) {
                if (imUsage < 100) {
                    Log.d(TAG, "+ + +1.12");
                    imUsage += 1.12;
                }
            } else if (latestUseIMTime <= 30) {
                if (imUsage < 100) {
                    Log.d(TAG, "+ + +0.56");
                    imUsage += 0.56;
                }
            } else {
                if (imUsage > 0) {
                    Log.d(TAG, "- - -0.56");
                    imUsage -= 0.56;
                }
            }
//            Log.d(TAG, "IM Usage: " + imUsage);
        }
    };

    Runnable updateAvailabilityStatusRunnable = new Runnable() {
        @Override
        public void run() {
//            Log.d(TAG, "!!!!! Update Status !!!!!");
            long currentTime = ScheduleAndSampleManager.getCurrentTimeInMillis();
            userShowTime = currentTime;
            afterEdit = sharedPrefs.getBoolean("afterEdit", false);
//            Log.d(TAG, "afterEdit: " + afterEdit);

            userShowTimeString = ScheduleAndSampleManager.getTimeString(userShowTime);
//            Log.d(TAG, "time string: " + userShowTimeString);

//            int imUsage = getIMUsage();
            int screenStatus = getScreenStatus();
            int ringerMode = getRingerMode();

            // 加上ringer mode
            userStatus = (int) ((imUsage * 0.4) + (screenStatus * 0.5) + (ringerMode * 0.1));

            Log.d(TAG, "After [PART 1] status: " + userStatus);

            // random present way & color & status string
            userWay = randomPresentWay();
            userStatusForm = randomStatusForm();
            Log.d(TAG, "userStatusForm: " + userStatusForm);

//            if (!userWay.equals(Constants.PRESENT_IN_TEXT) &&
//                    userStatusForm.equals(Constants.STATUS_FORM_DISTURB)) userStatus = 100 - userStatus;
            userColor =  Color.rgb(51, 102, 153); // default color

            userStatusText = rateGetText(userStatus);

            if (isHandleAfterEdit) {
                handleAfterEditThread(afterEditTimePeriod);
                isHandleAfterEdit = false;
            }
            if (!afterEdit) {
                // save to shared preference
                sharedPrefs.edit()
                        .putInt("status", userStatus)
                        .putLong("updateTime", userShowTime)
                        .putString("statusForm", userStatusForm)
                        .putString("way", userWay)
                        .putString("statusText", userStatusText)
                        .putInt("statusColor", userColor)
                        .commit();
            } else {
                // 如果有編輯過
                userWayEdit = sharedPrefs.getString("way", "NA");
                userStatusFormEdit = sharedPrefs.getString("statusForm", "NA");
                userStatusEdit = sharedPrefs.getInt("status", -1);
                userStatusTextEdit = sharedPrefs.getString("statusText", "NA");
                userShowTimeEdit = sharedPrefs.getLong("updateTime", -1);
                userShowTimeStringEdit = ScheduleAndSampleManager.getTimeString(userShowTimeEdit);
                userColorEdit = sharedPrefs.getInt("statusColor", Constants.DEFAULT_COLOR);
            }

            // send to database
            JSONObject data = getDataToServer();
            mQueue = Volley.newRequestQueue(mContext);
            mJsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Constants.storeSelfStatusUrl, data,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
//                            Log.d(TAG, response.toString());

                            try {
                                if (response.getString("response").equals("success insert")) {
                                    Log.d(TAG, "insert SUCCESSFUL.");
                                } else {
                                    Log.d(TAG, "insert FAILED.");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, "error");
                }
            });
            mQueue.add(mJsonObjectRequest);

            // TODO: if 差距過大，跳出通知

            if (index == Constants.STATUS_CAPACITY) index = 0;

            statusList.set(index, userStatus);
            int max = Collections.max(statusList);
            int min = Collections.min(statusList);

            Log.d(TAG, "MAX: " + max);
            Log.d(TAG, "min: " + min);

            /** For Partial Subject */
            // 有開想要接收通知
//            Log.d(TAG, "open Notification: " + sharedPrefs.getBoolean("openNotification", false));
//            if (sharedPrefs.getBoolean("openNotification", false)) {
//                if ((max-min) >= Constants.STATUS_THRESHOLD || userStatus < 20 || userStatus > 90) { // 25
//                    Log.d(TAG, "in send Notification");
//                    statusList.clear();
//                    statusList = new ArrayList<>(Collections.nCopies(Constants.STATUS_CAPACITY, userStatus));
//                    index = -1;
//                    sendNotification();
//                }
//            }
            /** For Partial Subject */

            /** For Main Subject */
            if ((max-min) >= Constants.STATUS_THRESHOLD || userStatus < 20 || userStatus > 90) { // 25
                Log.d(TAG, "in send Notification");
                statusList.clear();
                statusList = new ArrayList<>(Collections.nCopies(Constants.STATUS_CAPACITY, userStatus));
                index = -1;
                sendNotification();
            }
            /** For Main Subject */

            index += 1;
        }
    };

    private int getRingerMode() {
        ringerMode = RingerStreamGenerator.getRingerMode();

        if (ringerMode.equals("Vibrate")) return (int) (Math.random()*11 + 90); // 90-100
        else if (ringerMode.equals("Normal")) return (int) (Math.random()*11 + 50); // 50-60
        else return (int) (Math.random()*11 + 10); // 10-20
    }

//    private int getIMUsage() {
////        Log.d(TAG, "......... get IM Usage .......");
//        long currentTime = ScheduleAndSampleManager.getCurrentTimeInMillis();
//        latestUseIMTime = AppUsageStreamGenerator.getLatestUseIMTime();
//        typingTime = MobileAccessibilityService.getTypingTime();
//        Log.d(TAG, ">> typing time >> " + ScheduleAndSampleManager.getTimeString(typingTime));
//        Log.d(TAG, ">> latest use IM App time >> " + ScheduleAndSampleManager.getTimeString(latestUseIMTime));
//
//        // second
//        typingTime = (currentTime - typingTime) / 1000;
//        latestUseIMTime = (currentTime - latestUseIMTime) / 1000;
//
//        if (latestUseIMTime <= 60) { // < 1 min // 60-100
//            // 區間: 40 20 15 12 6 6 /
//            if (typingTime <= 60) return (int) (Math.random()*16 + 85); // 85-100 // < 1 min
//            else if (typingTime <= 300) return (int) (Math.random()*11 + 75); // 75-85
//            else if (typingTime <= 600) return (int) (Math.random()*6 + 70); // 70-75
//            else if (typingTime <= 1800) return (int) (Math.random()*6 + 65); // 65-70
//            else if (typingTime <= 3600) return (int) (Math.random()*3 + 63); // 63-65
//            else return (int) (Math.random()*3 + 60); // 60-62
//        } else if (latestUseIMTime <= 300) { // < 5 min // 40-60
//            if (typingTime <= 60) return (int) (Math.random()*8 + 53); // 53-60 // < 1 min
//            else if (typingTime <= 300) return (int) (Math.random()*4 + 50); // 50-53
//            else if (typingTime <= 600) return (int) (Math.random()*4 + 47); // 47-50
//            else if (typingTime <= 1800) return (int) (Math.random()*3 + 45); // 45-47
//            else if (typingTime <= 3600) return (int) (Math.random()*3 + 43); // 43-45
//            else return (int) (Math.random()*3 + 40); // 40-42
//        } else if (latestUseIMTime <= 600) { // < 10 min // 25-40
//            if (typingTime <= 60) return (int) (Math.random()*6 + 35); // 35-40 // < 1 min
//            else if (typingTime <= 300) return (int) (Math.random()*4 + 32); // 32-35
//            else if (typingTime <= 600) return (int) (Math.random()*3 + 30); // 30-32
//            else if (typingTime <= 1800) return (int) (Math.random()*3 + 28); // 28-30
////            else if (typingTime <= 3600) return (int) (Math.random()* + ); // 83-100
//            else return (int) (Math.random()*4 + 25); // 25-28
//        } else if (latestUseIMTime <= 1800) { // < 30 min // 12-25
//            if (typingTime <= 60) return (int) (Math.random()*6 + 20); // 20-25 // < 1 min
//            else if (typingTime <= 300) return (int) (Math.random()*4 + 17); // 17-20
//            else if (typingTime <= 600) return (int) (Math.random()*3 + 15); // 15-17
////            else if (typingTime <= 1800) return (int) (Math.random()* + ); // 83-100
//            else return (int) (Math.random()*3 + 12); // 12-14
//        } else if (latestUseIMTime <= 3600) { // < 60 min // 6-12
//            if (typingTime <= 60) return (int) (Math.random()*3 + 10); // 10-12 // < 1 min
//            else if (typingTime <= 300) return (int) (Math.random()*3 + 8); // 8-10
////            else if (typingTime <= 600) return (int) (Math.random()* + ); // 83-100
//            else return (int) (Math.random()*2 + 6); // 6-7
//        } else { // 0-6
//            return (int) (Math.random()*7); // 0-6
//        }
//    }

    private int getScreenStatus() {
        long currentTime = ScheduleAndSampleManager.getCurrentTimeInMillis();
        screenInteractiveTime = AppUsageStreamGenerator.getScreenInteractiveTime();
        Log.d(TAG, ">> Screen interactive time >> " + ScheduleAndSampleManager.getTimeString(screenInteractiveTime));

        screenInteractiveTime = (currentTime - screenInteractiveTime) / 1000;
        if (screenInteractiveTime <= 60) return (int) (Math.random()*21 + 80); // 80-100
        else if (screenInteractiveTime <= 300) return (int) (Math.random()*21 + 60); // 60-80
        else if (screenInteractiveTime <= 600) return (int) (Math.random()*21 + 40); // 40-60
        else if (screenInteractiveTime <= 1800) return (int) (Math.random()*21 + 20); // 20-40
        else if (screenInteractiveTime <= 3600) return (int) (Math.random()*11 + 10); // 10-20
        else return (int) (Math.random()*11); // 0-10
    }

    private JSONObject getDataToServer() {
        JSONObject data = new JSONObject();
        try {
            data.put("id", sharedPrefs.getString("id", "NA"));
            data.put("group", sharedPrefs.getString("group", "NA"));
            data.put("afterEdit", sharedPrefs.getBoolean("afterEdit", false));
            if (afterEdit) {
                data.put("statusEdit", userStatusEdit);
                data.put("createdTimeEdit", userShowTimeEdit);
                data.put("timeStringEdit", userShowTimeStringEdit);
                data.put("presentWayEdit", userWayEdit);
                data.put("statusFormEdit", userStatusFormEdit);
                data.put("statusTextEdit", userStatusTextEdit);
                data.put("statusColorEdit", userColorEdit);
            }
            data.put("typingTime", typingTime);
            data.put("latestUseIMTime", latestUseIMTime);
            data.put("screenInteractiveTime", screenInteractiveTime);
            data.put("ringerMode", ringerMode);

//            data.put("networkType", networkType);
//            data.put("batteryLevel", batteryLevel);
//            data.put("sensorProximity", sensorProximity);

            data.put("status", userStatus);
            data.put("createdTime", userShowTime);
            data.put("createdTimeString", userShowTimeString);
            data.put("presentWay", userWay);
            data.put("statusForm", userStatusForm);
            data.put("statusText", userStatusText);
            data.put("statusColor", userColor);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }

//    private void getNetworkType() {
////        Log.d(TAG, "NetworkType: " + ConnectivityStreamGenerator.mNetworkType);
//        if (networkTypes.size() == Constants.STREAM_DATA_CAPACITY) {
//            networkTypes.poll();
//        }
////        Log.d(TAG, "size1: " + networkTypes.size());
//        if (networkType.equals(ConnectivityStreamGenerator.mNetworkType)) {
//            networkTypes.add(networkType);
////            for (String n : networkTypes) {
////                Log.d(TAG, "count: " + count++ + " " + n);
////            }
//        } else {
//            networkTypes.add(ConnectivityStreamGenerator.mNetworkType);
////            for (String n : networkTypes) {
////                Log.d(TAG, "count: " + count++ + " " + n);
////            }
//        }
////        Log.d(TAG, "size2: " + networkTypes.size());
//        networkType = ConnectivityStreamGenerator.mNetworkType;
//    }
//
//    private void getBatteryChargingState() {
////        Log.d(TAG, "Battery Charging State: " + BatteryStreamGenerator.mBatteryChargingState);
//        if (batteryChargingStates.size() == Constants.STREAM_DATA_CAPACITY) {
//            batteryChargingStates.poll();
//        }
////        Log.d(TAG, "size1: " + batteryChargingStates.size());
//        if (batteryChargingState == BatteryStreamGenerator.mBatteryChargingState) {
//            batteryChargingStates.add(batteryChargingState);
////            for (String b : batteryChargingStates) {
////                Log.d(TAG, "count " + count++ + " " + b);
////            }
//        } else {
//            batteryChargingStates.add(BatteryStreamGenerator.mBatteryChargingState);
////            for (String b : batteryChargingStates) {
////                Log.d(TAG, "count " + count++ + " " + b);
////            }
//        }
//
////        Log.d(TAG, "size2: " + batteryChargingStates.size());
//        batteryChargingState = BatteryStreamGenerator.mBatteryChargingState;
//
//    }

    private void sendNotification() {
        boolean send = false;

        long lastNotiTime = sharedPrefs.getLong("lastNotificationTime", -1);

        Log.d(TAG, "lastNotiTime: " + ScheduleAndSampleManager.getTimeString(lastNotiTime));

        Calendar current = Calendar.getInstance();
        int currentHourIn24Format = current.get(Calendar.HOUR_OF_DAY);
        int currentMinute = current.get(Calendar.MINUTE);

        if ((currentHourIn24Format >= 10 && currentHourIn24Format < 22) ||
            (currentHourIn24Format == 22 && currentMinute <= 30)) {
            if (lastNotiTime != 0 && ((userShowTime - lastNotiTime) > 3600000)) { // 1 hr
                send = true;
            }
        }
//        send = true; //////////TODO: for test //////////
        if (send) {
            Log.d(TAG, "to send notification");
            sharedPrefs.edit()
                    .putLong("lastNotificationTime", userShowTime)
                    .commit();

            NotificationManager mNotificationManager =
                    (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            String notificationText = ScheduleAndSampleManager.getMinSecStringFromMillis(userShowTime) + "\n"
                    + " 請點選通知查看您的狀態及填寫問卷!!";
            Notification notification = getNotification(notificationText);
            notification.defaults |= Notification.DEFAULT_VIBRATE;

            mNotificationManager.notify(notifyNotificationID, notification);

            // 紀錄送了多少通知
            JSONObject noti = new JSONObject();
            try {
                noti.put("createdTime", userShowTime);
                noti.put("createdTimeString", userShowTimeString);
                noti.put("id", sharedPrefs.getString("id", "NA"));
                noti.put("presentWay", userWay);
                noti.put("statusForm", userStatusForm);
                noti.put("statusText", userStatusText);
                noti.put("status", userStatus);
                noti.put("statusColor", userColor);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            mQueue = Volley.newRequestQueue(mContext);
            mJsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Constants.storeSelfQuestionnaire, noti,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
//                            Log.d(TAG, response.toString());

                            try {
                                if (response.getString("response").equals("success insert")) {
                                    Log.d(TAG, "record notification SUCCESSFUL.");
                                } else {
                                    Log.d(TAG, "insert FAILED.");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, "error");
                }
            });
            mQueue.add(mJsonObjectRequest);
        }
    }

    private Notification getNotification(String text) {
        Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle();
        bigTextStyle.setBigContentTitle(Constants.SURVEY_CHANNEL_NAME);
        bigTextStyle.bigText(text);

        Intent resultIntent = new Intent();
        resultIntent.setComponent(new ComponentName(this,SelfQuestionnaire.class));
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        Bundle bundle = new Bundle();
        bundle.putLong("showTime", userShowTime);
        bundle.putInt("status", userStatus);
        bundle.putString("way", userWay);
        bundle.putString("statusForm", userStatusForm);
        bundle.putString("statusString", userStatusText);
        bundle.putInt("color", userColor);

        resultIntent.putExtras(bundle);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, notifyNotificationCode,
                resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder noti = new Notification.Builder(this)
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.smile)
                .setStyle(bigTextStyle)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return noti
                    .setChannelId(Constants.SURVEY_CHANNEL_ID)
                    .build();
        } else {
            return noti
                    .build();
        }

    }

    private Notification getOngoingNotification(String text){

        Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle();
        bigTextStyle.setBigContentTitle(Constants.APP_FULL_NAME);
        bigTextStyle.bigText(text);

        Intent resultIntent = new Intent(this, HomePage.class);
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

//        stopTheSessionByServiceClose();

        Log.d(TAG, "onDestory");
        String onDestroy = "BackGround, onDestroy";
        CSVHelper.storeToCSV(CSVHelper.CSV_ESM, onDestroy);
//        CSVHelper.storeToCSV(CSVHelper.CSV_CAR, onDestroy);

//        checkingRemovedFromForeground();
        isBackgroundServiceRunning = false;
        isBackgroundRunnableRunning = false;

        mNotificationManager.cancel(ongoingNotificationID);

//        Log.d(TAG, "Destroying service. Your state might be lost!");

        removeRunnable();
        sendBroadcastToStartService();
//        unregisterReceiver(mWifiReceiver);
        unregisterReceiver(CheckRunnableReceiver);
    }

    @Override
    public void onTaskRemoved(Intent intent){
        Log.d(TAG, "task removed");
        super.onTaskRemoved(intent);
        mNotificationManager.cancel(ongoingNotificationID);

//        checkingRemovedFromForeground();

        isBackgroundServiceRunning = false;
        isBackgroundRunnableRunning = false;

        mNotificationManager.cancel(ongoingNotificationID);

        String onTaskRemoved = "BackGround, onTaskRemoved";
        CSVHelper.storeToCSV(CSVHelper.CSV_CheckService_alive, onTaskRemoved);
        removeRunnable();
        sendBroadcastToStartService();
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
                    public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities){
                        sendBroadcast(
                                getConnectivityIntent("onCapabilitiesChanged : "+networkCapabilities.toString())
                        );
                    }
                }
        );

    }

//    private void checkingRemovedFromForeground(){
//
//        Log.d(TAG,"stopForeground");
//
//        stopForeground(true);
//
//        try {
//
//            unregisterReceiver(CheckRunnableReceiver);
//        }catch (IllegalArgumentException e){
//
//        }
//
//        mScheduledExecutorService.shutdown();
//    }

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

        Log.d(TAG, "remove");
        mScheduledFuture.cancel(true);
        mScheduledSendToServer.cancel(true);
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

    private void createEditNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = Constants.EDIT_CHANNEL_NAME;
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(Constants.EDIT_CHANNEL_ID, name, importance);
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarm.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            System.currentTimeMillis() + Constants.PROMPT_SERVICE_REPEAT_MILLISECONDS,
                            pi);
                } else {
                    alarm.set(
                            AlarmManager.RTC_WAKEUP,
                            System.currentTimeMillis() + Constants.PROMPT_SERVICE_REPEAT_MILLISECONDS,
                            pi
                    );
                }

            }
        }
    };

}
