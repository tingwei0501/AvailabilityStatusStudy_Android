package labelingStudy.nctu.minuku_2.Questionnaire;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import labelingStudy.nctu.minuku.Utilities.ScheduleAndSampleManager;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku_2.ContactList;
import labelingStudy.nctu.minuku_2.R;
import labelingStudy.nctu.minuku_2.service.BackgroundService;

import static android.content.Context.MODE_PRIVATE;
import static labelingStudy.nctu.minuku_2.manager.renderManager.renderStatus;

public class SelfEditQuestionnaire extends Activity {
    private static final String TAG = "SelfEditQuestionnaire";
    private Context mContext;
    private RequestQueue mQueue;
    private JsonObjectRequest mJsonObjectRequest;
    private SharedPreferences sharedPreferences;
    private InputMethodManager mInputMethodManager;

    // collected data
    private String selectedLocation = "";
    private String changeStatusOtherReason;

    // 原本的狀態
    private String originSelfStatusWay;
    private String originSelfStatusString;
    private int originSelfStatusRate;
    private int originSelfStatusColor;
    private long originShowTime;
    private int timePeriod;

    // show your status (更改過)
    private TextView time;
    private TextView hour, minute;
    private CircularProgressBar selfStatusCircle;
    private TextView selfStatusText;
    private String selfStatusWay;
    private String selfStatusString;
    private int selfStatusRate;
    private int selfStatusColor;
    private long showTime;

    // Q1
    private RadioGroup radioGroupLocation1;
    private RadioButton home;
    private RadioButton dorm;
    private RadioButton lab;

    private RadioGroup radioGroupLocation2;
    private RadioButton classroom;
    private RadioButton library;
    private RadioButton outdoor;
    private RadioButton restaurant;

    private RadioGroup radioGroupLocation3;
    private RadioButton transportation;
    private RadioButton mall;

    private RadioGroup radioGroupLocation4;
    private RadioButton locationOther;
    private EditText locationOtherText;
    //

    // Q2
    private CheckBox notFitReasonWay;
    private CheckBox notFitReasonRatio;
    private CheckBox notFitReasonAccurate;
    private CheckBox notFitReasonBlurred;
    private CheckBox notFitReasonHide;
    private CheckBox notFitReasonOther;
    private EditText notFitReasonOtherText;

    private Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.self_edit_questionnaire);
        mContext = getApplicationContext();
        sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);

        mInputMethodManager = (InputMethodManager) getSystemService(mContext.INPUT_METHOD_SERVICE);

        time = findViewById(R.id.self_edit_questionnaire_time);
        hour = findViewById(R.id.self_edit_questionnaire_time_hour);
        minute = findViewById(R.id.self_edit_questionnaire_time_min);
        selfStatusCircle = findViewById(R.id.self_edit_questionnaire_circle);
        selfStatusText = findViewById(R.id.self_edit_questionnaire_rate);

        // Q1 1. 請問您當時主要地點在哪裡?
        radioGroupLocation1 = findViewById(R.id.self_edit_location1);
        radioGroupLocation2 = findViewById(R.id.self_edit_location2);
        radioGroupLocation3 = findViewById(R.id.self_edit_location3);
        radioGroupLocation4 = findViewById(R.id.self_edit_location4);
        home = findViewById(R.id.self_edit_home);
        dorm = findViewById(R.id.self_edit_dorm);
        lab = findViewById(R.id.self_edit_lab);
        classroom = findViewById(R.id.self_edit_classroom);
        library = findViewById(R.id.self_edit_library);
        outdoor = findViewById(R.id.self_edit_outdoor);
        restaurant = findViewById(R.id.self_edit_restaurant);
        transportation = findViewById(R.id.self_edit_transportation);
        mall = findViewById(R.id.self_edit_mall);
        locationOther = findViewById(R.id.self_edit_location_other);
        locationOtherText = findViewById(R.id.self_edit_location_other_text);
        locationOtherText.setOnEditorActionListener(new locationEditTextListener());
        radioGroupLocation1.setOnCheckedChangeListener(new locationListener1());
        radioGroupLocation2.setOnCheckedChangeListener(new locationListener2());
        radioGroupLocation3.setOnCheckedChangeListener(new locationListener3());
        radioGroupLocation4.setOnCheckedChangeListener(new locationListener4());
        //
        // Q2
        notFitReasonWay = findViewById(R.id.self_edit_notFitReason_way_text);
        notFitReasonRatio = findViewById(R.id.self_edit_notFitReason_ratio_text);
        notFitReasonAccurate = findViewById(R.id.self_edit_notFitReason_accurate);
        notFitReasonBlurred = findViewById(R.id.self_edit_notFitReason_blurred);
        notFitReasonHide = findViewById(R.id.self_edit_notFitReason_hide);
        notFitReasonOther = findViewById(R.id.self_edit_notFitReason_other_text);
        notFitReasonOtherText = findViewById(R.id.self_edit_notFitReason_otherText_text);
        //

        // submit button
        submit = findViewById(R.id.self_edit_questionnaire_submit);
        submit.setOnClickListener(submitListener);
    }

    @Override
    protected void onStart() {
        super.onStart();

        originShowTime = sharedPreferences.getLong("updateTime", 0);
        originSelfStatusRate = sharedPreferences.getInt("status", 0);
        originSelfStatusWay = sharedPreferences.getString("way", "NA");
        originSelfStatusColor = sharedPreferences.getInt("statusColor", -1);
        originSelfStatusString = sharedPreferences.getString("statusText", "NA");

        Bundle bundle = getIntent().getExtras();
        showTime = bundle.getLong("createdTime");
        selfStatusRate = bundle.getInt("status");
        selfStatusWay = bundle.getString("presentWay");
        selfStatusColor = bundle.getInt("statusColor");
        selfStatusString = bundle.getString("statusText");
        timePeriod = bundle.getInt("timePeriod");
        Log.d(TAG, "time period: " + timePeriod/60);
        Log.d(TAG, "time in min: " + timePeriod%60);

//        Log.d(TAG, "selfStatusRate: " + selfStatusRate);
//        Log.d(TAG, "selfStatusWay: " + selfStatusWay);
        if (showTime != 0) {
            time.setText(ScheduleAndSampleManager.getMinSecStringFromMillis(showTime));
            renderStatus(selfStatusText, selfStatusCircle,
                    selfStatusRate, selfStatusWay, selfStatusString, selfStatusColor);
            hour.setText(String.valueOf(timePeriod/60));
            minute.setText(String.valueOf(timePeriod%60));
        }
    }

    private Button.OnClickListener submitListener = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {

            if (!selectedLocation.equals("")) {
                Log.d(TAG, "selectedLocation:  "+ selectedLocation);

                JSONObject data = new JSONObject();
                JSONArray arr = new JSONArray();
                try {
                    data.put("id", sharedPreferences.getString("id", ""));
                    // 更新前的狀態
                    data.put("originPresentWay", originSelfStatusWay);
                    data.put("originCreatedTime", originShowTime);
                    data.put("originStatus", originSelfStatusRate);
                    data.put("originStatusText", originSelfStatusString);
                    data.put("originStatusColor", originSelfStatusColor);
                    // 更新後
                    data.put("presentWay", selfStatusWay);
                    data.put("createdTime", showTime);
                    data.put("status", selfStatusRate);
                    data.put("statusText", selfStatusString);
                    data.put("statusColor", selfStatusColor);

                    data.put("selectedLocation", selectedLocation);
                    CheckBox[] id = {notFitReasonWay, notFitReasonRatio, notFitReasonAccurate,
                            notFitReasonBlurred, notFitReasonHide};

                    for (CheckBox i:id) {
                        if (i.isChecked()) arr.put(true);
                        else arr.put(false);
                    }

                    data.put("changeReasons", arr);

                    if (notFitReasonOther.isChecked()) {
                        changeStatusOtherReason = notFitReasonOtherText.getText().toString();
                        data.put("changeOtherReason", changeStatusOtherReason);
                        Log.d(TAG, "other reasons: " + changeStatusOtherReason);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                sharedPreferences.edit()
                        .putString("way", selfStatusWay)
                        .putInt("status", selfStatusRate)
                        .putString("statusText", selfStatusString)
                        .putLong("updateTime", showTime)
                        .putInt("statusColor", selfStatusColor)
                        .putBoolean("afterEdit", true)
                        .commit();

                BackgroundService.isHandleAfterEdit = true;
                BackgroundService.afterEditTimePeriod = timePeriod;

                // send to database
                mQueue = Volley.newRequestQueue(mContext);
                mJsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Constants.storeSelfQuestionnaire, data,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d(TAG, response.toString());
                                // start activity here
                                try {
                                    if (response.getString("response").equals("success insert")) {
                                        Intent intent = new Intent(SelfEditQuestionnaire.this, ContactList.class);
                                        Toast.makeText(SelfEditQuestionnaire.this,
                                                "填寫成功!", Toast.LENGTH_SHORT).show();
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP); //加這行
                                        startActivity(intent);
                                        SelfEditQuestionnaire.this.finish();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "Error: " + error.getMessage());
                    }
                });
                mQueue.add(mJsonObjectRequest);
            } else {
                Toast.makeText(SelfEditQuestionnaire.this,
                        "請每題都要填寫!", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private class locationListener1 implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            Log.d(TAG, "checkedId: " + checkedId);

            if (checkedId != -1) {
                if (home.isChecked()) {
                    selectedLocation = "home";
                    Log.d(TAG, (String) home.getText()); // 家裡
                } else if (dorm.isChecked()) {
                    selectedLocation = "dorm";
                } else if (lab.isChecked()) {
                    selectedLocation = "lab";
                }
                if (home.isChecked() || dorm.isChecked() || lab.isChecked()) {
                    locationOtherText.clearFocus();
                    radioGroupLocation2.clearCheck();
                    radioGroupLocation3.clearCheck();
                    radioGroupLocation4.clearCheck();
                }
            }
        }
    }

    private class locationListener2 implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            Log.d(TAG, "checkedId: " + checkedId);

            if (checkedId != -1) {
                if (classroom.isChecked()) {
                    selectedLocation = "classroom";
                    Log.d(TAG, (String) home.getText()); // 家裡
                } else if (library.isChecked()) {
                    selectedLocation = "library";
                } else if (outdoor.isChecked()) {
                    selectedLocation = "outdoor";
                } else if (restaurant.isChecked()) {
                    selectedLocation = "restaurant";
                }
                if (classroom.isChecked() || library.isChecked() || outdoor.isChecked() || restaurant.isChecked()) {
                    locationOtherText.clearFocus();
                    radioGroupLocation1.clearCheck();
                    radioGroupLocation3.clearCheck();
                    radioGroupLocation4.clearCheck();
                }
            }
        }
    }

    private class locationListener3 implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            Log.d(TAG, "checkedId: " + checkedId);

            if (checkedId != -1) {
                if (transportation.isChecked()) {
                    selectedLocation = "transportation";
                } else if (mall.isChecked()) {
                    selectedLocation = "mall";
                }
                if (transportation.isChecked() || mall.isChecked()) {
                    locationOtherText.clearFocus();
                    radioGroupLocation1.clearCheck();
                    radioGroupLocation2.clearCheck();
                    radioGroupLocation4.clearCheck();
                }
            }
        }
    }

    private class locationListener4 implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {

            if (checkedId != -1) {
                if (locationOther.isChecked()) {
                    locationOtherText.requestFocus();
                    // show keyboard
                    mInputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                    radioGroupLocation1.clearCheck();
                    radioGroupLocation2.clearCheck();
                    radioGroupLocation3.clearCheck();
                }
            }
//            switch (checkedId) {
//                case R.id.location_other:
//                    if (locationOther.isChecked()) {
//                        locationOtherText.requestFocus();
//                        // show keyboard
//                        mInputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
//                        radioGroupLocation1.clearCheck();
//                        radioGroupLocation2.clearCheck();
//                        radioGroupLocation3.clearCheck();
//                    }
//                    break;
//            }
        }
    }

    private class locationEditTextListener implements EditText.OnEditorActionListener {
        @Override
        public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
            mInputMethodManager.hideSoftInputFromWindow(SelfEditQuestionnaire.this.getWindow().getDecorView().getRootView().getWindowToken(), 0);
//            mIBinder = SelfQuestionnaire.this.getCurrentFocus().getWindowToken();
//            mInputMethodManager.hideSoftInputFromWindow(mIBinder, InputMethodManager.HIDE_NOT_ALWAYS);
            selectedLocation = locationOtherText.getText().toString();
            locationOtherText.clearFocus();
            return false;
        }
    }
}
