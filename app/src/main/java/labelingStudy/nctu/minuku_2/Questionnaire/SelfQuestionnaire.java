package labelingStudy.nctu.minuku_2.Questionnaire;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import labelingStudy.nctu.minuku.Utilities.ScheduleAndSampleManager;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku_2.ContactList;
import labelingStudy.nctu.minuku_2.EditProfilePage;
import labelingStudy.nctu.minuku_2.R;

import static labelingStudy.nctu.minuku_2.manager.renderManager.renderStatus;
import static labelingStudy.nctu.minuku_2.manager.renderManager.statusGetRate;
import static labelingStudy.nctu.minuku_2.manager.renderManager.statusGetText;


public class SelfQuestionnaire extends Activity {
    private static final String TAG = "SelfQuestionnaire";
    private Context mContext;
    private RequestQueue mQueue;
    private JsonObjectRequest mJsonObjectRequest;
    private SharedPreferences sharedPreferences;
//    private IBinder mIBinder;
    private InputMethodManager mInputMethodManager;
    private boolean change = true;

    // collected data
    private String selectedLocation = "";
    private String selectedActivity = "";
    private int idealPresentationRate = 0;
    private String statusNotFitOtherReason;
    private String showStatusWay = "text"; // origin or
    private int showStatusRate; // digit or graphic
    private String showStatusString; // text
    private String showStatusDifferentOtherReason;

    // show your status
    private TextView time;
    private CircularProgressBar selfStatusCircle;
    private TextView selfStatusText;
    private String selfStatusWay = "text"; // TODO: to be collected
    private String selfStatusString; // collected
    private int selfStatusRate; // collected
    private int selfStatusColor; // collected
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
    //Q2
    private RadioGroup radioGroupActivity1;
    private RadioButton work;
    private RadioButton study;
    private RadioButton classMeeting;

    private RadioGroup radioGroupActivity2;
    private RadioButton eat;
    private RadioButton sleep;
    private RadioButton exercising;

    private RadioGroup radioGroupActivity3;
    private RadioButton takeABreak;

    private RadioGroup radioGroupActivity4;
    private RadioButton shopping;
    private RadioButton moving;

    private RadioGroup radioGroupActivity5;
    private RadioButton activityOther;
    private EditText activityOtherText;
    //
    // Q3
    private RadioGroup radioGroupIdealRate;
    //
    // Q4
    private ConstraintLayout statusNotFitLayout;
    private ConstraintLayout notFitReasonDigit;
    private ConstraintLayout notFitReasonText;
    // text //
    private CheckBox notFitReasonTextWay;
    private CheckBox notFitReasonTextRatio;
    private CheckBox notFitReasonTextOther;
    private EditText notFitReasonTextOtherText;
    // digit & graphic //
    private CheckBox notFitReasonDigitHigh;
    private CheckBox notFitReasonDigitLow;
    private CheckBox notFitReasonDigitWay;
    private CheckBox notFitReasonDigitOther;
    private EditText notFitReasonDigitOtherText;
    //
    //Q5: 想要怎麼呈現給聯絡人看
    private CheckBox asOriginalStatus;
    private Spinner waySpinner;
    private Spinner textSpinner;
    private IndicatorSeekBar digitSeekBar;
    private CircularProgressBar graphicCircle;
    private SeekBar graphicSeekBar;
    //
    // Q6: 想呈現的跟系統給的不同: 理由
    private ConstraintLayout showStatusDifferentReasonLayout;
    private CheckBox showStatusDifferentHigh;
    private CheckBox showStatusDifferentLow;
    private CheckBox showStatusDifferentAccurate;
    private CheckBox showStatusDifferentBlurred;
    private CheckBox showStatusDifferentHide;
    private CheckBox showStatusDifferentOther;
    private EditText showStatusDifferentOtherText;
    //
    private Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.self_questionnaire);
        mContext = getApplicationContext();
        sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);

        mInputMethodManager = (InputMethodManager) getSystemService(mContext.INPUT_METHOD_SERVICE);

        time = findViewById(R.id.self_questionnaire_time);
        selfStatusCircle = findViewById(R.id.self_questionnaire_circle);
        selfStatusText = findViewById(R.id.self_questionnaire_rate);

        // Q1 1. 請問您當時主要地點在哪裡?
        radioGroupLocation1 = findViewById(R.id.location1);
        radioGroupLocation2 = findViewById(R.id.location2);
        radioGroupLocation3 = findViewById(R.id.location3);
        radioGroupLocation4 = findViewById(R.id.location4);
        home = findViewById(R.id.home);
        dorm = findViewById(R.id.dorm);
        lab = findViewById(R.id.lab);
        classroom = findViewById(R.id.classroom);
        library = findViewById(R.id.library);
        outdoor = findViewById(R.id.outdoor);
        restaurant = findViewById(R.id.restaurant);
        transportation = findViewById(R.id.transportation);
        mall = findViewById(R.id.mall);
        locationOther = findViewById(R.id.location_other);
        locationOtherText = findViewById(R.id.location_other_text);
        locationOtherText.setOnEditorActionListener(new locationEditTextListener());

        radioGroupLocation1.setOnCheckedChangeListener(new locationListener1());
        radioGroupLocation2.setOnCheckedChangeListener(new locationListener2());
        radioGroupLocation3.setOnCheckedChangeListener(new locationListener3());
        radioGroupLocation4.setOnCheckedChangeListener(new locationListener4());
        //

        // Q2 2. 請問您當時主要的活動是?
        radioGroupActivity1 = findViewById(R.id.activity1);
        radioGroupActivity2 = findViewById(R.id.activity2);
        radioGroupActivity3 = findViewById(R.id.activity3);
        radioGroupActivity4 = findViewById(R.id.activity4);
        radioGroupActivity5 = findViewById(R.id.activity5);
        work = findViewById(R.id.work);
        study = findViewById(R.id.study);
        classMeeting = findViewById(R.id.class_meeting);
        eat = findViewById(R.id.eat);
        sleep = findViewById(R.id.sleep);
        exercising = findViewById(R.id.exercising);
        takeABreak = findViewById(R.id.take_a_break);
        shopping = findViewById(R.id.shopping);
        moving = findViewById(R.id.moving);
        activityOther = findViewById(R.id.activity_other);
        activityOtherText = findViewById(R.id.activity_other_text);
        activityOtherText.setOnEditorActionListener(new activityEditTextListener());

        radioGroupActivity1.setOnCheckedChangeListener(new activityListener1());
        radioGroupActivity2.setOnCheckedChangeListener(new activityListener2());
        radioGroupActivity3.setOnCheckedChangeListener(new activityListener3());
        radioGroupActivity4.setOnCheckedChangeListener(new activityListener4());
        radioGroupActivity5.setOnCheckedChangeListener(new activityListener5());
        //
        // Q3 3. 目前所呈現的狀態，有多符合你真實的狀態?
        radioGroupIdealRate = findViewById(R.id.ideal_or_not);
        radioGroupIdealRate.setOnCheckedChangeListener(new idealRateListener());
        //
        // Q4 4. 我們發現你覺得呈現的狀態與本身不符合，原因是?
        statusNotFitLayout = findViewById(R.id.statusNotFitLayout);
        notFitReasonDigit = findViewById(R.id.notFitReason_digit);
        notFitReasonText = findViewById(R.id.notFitReason_text);
        notFitReasonTextWay = findViewById(R.id.notFitReason_way_text);
        notFitReasonTextRatio = findViewById(R.id.notFitReason_ratio_text);
        notFitReasonTextOther = findViewById(R.id.notFitReason_other_text);
        notFitReasonTextOtherText = findViewById(R.id.notFitReason_otherText_text);
        notFitReasonTextOtherText.setOnEditorActionListener(new notFitReasonTextEditListener());

        notFitReasonDigitHigh = findViewById(R.id.notFitReason_high_digit);
        notFitReasonDigitLow = findViewById(R.id.notFitReason_low_digit);
        notFitReasonDigitWay = findViewById(R.id.notFitReason_way_digit);
        notFitReasonDigitOther = findViewById(R.id.notFitReason_other_digit);
        notFitReasonDigitOtherText = findViewById(R.id.notFitReason_otherText_digit);
        notFitReasonDigitOtherText.setOnEditorActionListener(new notFitReasonDigitEditListener());
        //
        // Q5: 請問你想要怎麼呈現狀態給聯絡人看?
        asOriginalStatus = findViewById(R.id.statusOriginalCheckbox);
        asOriginalStatus.setOnCheckedChangeListener(new asOriginalStatusListener());
        waySpinner = findViewById(R.id.way_spinner);
        ArrayAdapter<CharSequence> wayLunchList = ArrayAdapter.createFromResource(SelfQuestionnaire.this,
                R.array.showContactWay,
                android.R.layout.simple_spinner_dropdown_item);
        waySpinner.setAdapter(wayLunchList);
        waySpinner.setOnItemSelectedListener(new presentWaySpinnerListener());

        textSpinner = findViewById(R.id.textStatus_spinner);
        ArrayAdapter<CharSequence> textLunchList = ArrayAdapter.createFromResource(SelfQuestionnaire.this,
                R.array.textStatus,
                android.R.layout.simple_spinner_dropdown_item);
        textSpinner.setAdapter(textLunchList);
        textSpinner.setOnItemSelectedListener(new textStatusSpinnerListener());

        digitSeekBar = findViewById(R.id.digitStatus_seekbar);
        digitSeekBar.setOnSeekChangeListener(new digitSeekBarListener());

        graphicCircle = findViewById(R.id.graphStatus_circle);
        graphicCircle.setColor(Constants.DEFAULT_COLOR);
        graphicSeekBar = findViewById(R.id.graphStatus_circleSeekBar);
        graphicSeekBar.setOnSeekBarChangeListener(new graphicSeekBarListener());
        //
        // Q6: 想呈現的跟提供的不同: 為甚麼?
        showStatusDifferentReasonLayout = findViewById(R.id.showStatusDifferentReasonLayout);
        showStatusDifferentHigh = findViewById(R.id.showDifferent_high);
        showStatusDifferentLow = findViewById(R.id.showDifferent_low);
        showStatusDifferentAccurate = findViewById(R.id.showDifferent_accurate);
        showStatusDifferentBlurred = findViewById(R.id.showDifferent_blurred);
        showStatusDifferentHide = findViewById(R.id.showDifferent_hide);
        showStatusDifferentOther = findViewById(R.id.showDifferent_other);
        showStatusDifferentOtherText = findViewById(R.id.showDifferent_otherText);
        showStatusDifferentOtherText.setOnEditorActionListener(new showStatusDifferentEditListener());
        // submit button
        submit = findViewById(R.id.self_questionnaire_submit);
        submit.setOnClickListener(submitListener);
    }

    @Override
    protected void onStart() {
        super.onStart();

        try {
            Bundle bundle = getIntent().getExtras();
            showTime = bundle.getLong("showTime", 0);
            selfStatusRate = bundle.getInt("status", 0);
            selfStatusWay = bundle.getString("way", "NA");
            selfStatusColor = bundle.getInt("color", 0);
            selfStatusString = bundle.getString("statusString", "NA");

            Log.d(TAG, "in bundle");

        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.d(TAG, "not from notification.");
            showTime = sharedPreferences.getLong("updateTime", 0);
            selfStatusRate = sharedPreferences.getInt("status", 0);
            selfStatusWay = sharedPreferences.getString("way", "NA");
            selfStatusColor = sharedPreferences.getInt("statusColor", -1);
            selfStatusString = sharedPreferences.getString("statusText", "NA");
        }
        if (showTime != 0) {
            // has data (from notification)
            //顯示在畫面上
            time.setText(ScheduleAndSampleManager.getMinSecStringFromMillis(showTime));
            renderStatus(selfStatusText, selfStatusCircle, selfStatusRate, selfStatusWay, selfStatusString, selfStatusColor);
        }
    }

    private Button.OnClickListener submitListener = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {
            Log.d(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            Log.d(TAG, "selectedLocation:  "+ selectedLocation);
//            Log.d(TAG, "selectedActivity: " + selectedActivity);
//            Log.d(TAG, "idealPresentationRate: " + idealPresentationRate);
            long currentTime = ScheduleAndSampleManager.getCurrentTimeInMillis();

            boolean pass = false;
            JSONObject data = new JSONObject();
            JSONArray notFitReasonArr = new JSONArray();
            if (!selectedLocation.equals("") && !selectedActivity.equals("") && idealPresentationRate != 0) {
                try {
                    data.put("id", sharedPreferences.getString("id", ""));
                    data.put("notificationSendTime", showTime);
                    data.put("notificationSendTimeString", ScheduleAndSampleManager.getTimeString(showTime));
                    data.put("createdTimeString", ScheduleAndSampleManager.getTimeString(currentTime));
                    data.put("createdTime", currentTime);
                    data.put("selectedLocation", selectedLocation);
                    data.put("selectedActivity", selectedActivity);
                    data.put("idealPresentationRate", idealPresentationRate);
                    if (idealPresentationRate == 1 || idealPresentationRate == 2) {
                        if (selfStatusWay.equals(Constants.PRESENT_IN_TEXT)) {
                            Log.d(TAG, "present in text ");
                            CheckBox[] id = {notFitReasonTextWay, notFitReasonTextRatio};
                            for (CheckBox i:id) {
                                if (i.isChecked()) notFitReasonArr.put(true);
                                else notFitReasonArr.put(false);
                            }
//                            data.put("notFitReasons", arr);
                            if (notFitReasonTextOther.isChecked()) {
                                data.put("statusNotFitOtherReason", statusNotFitOtherReason);
                                Log.d(TAG, "other reason: " + statusNotFitOtherReason);
                            }
                        } else {
                            Log.d(TAG, "present in digit or graphic");
                            CheckBox[] id = {notFitReasonDigitHigh, notFitReasonDigitLow, notFitReasonDigitWay};
                            for (CheckBox i:id) {
                                if (i.isChecked()) notFitReasonArr.put(true);
                                else notFitReasonArr.put(false);
                            }
                            if (notFitReasonDigitOther.isChecked()) {
                                data.put("statusNotFitOtherReason", statusNotFitOtherReason);
                                Log.d(TAG, "other reason: " + statusNotFitOtherReason);
                            }
                        }
                        data.put("notFitReasons", notFitReasonArr);
                        pass = true;
                    } else pass = true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            try {
                data.put("showStatusWay", showStatusWay);
                if (change) {
                    Log.d(TAG, "in change");
                    if (showStatusWay.equals(Constants.PRESENT_IN_TEXT)) {
                        data.put("showStatusString", showStatusString);
                        data.put("showStatusRate", statusGetRate(showStatusString));
                        sharedPreferences.edit()
                                .putString("way", "text")
                                .putInt("status", statusGetRate(showStatusString))
                                .putString("statusText", showStatusString)
                                .putLong("updateTime", currentTime)
                                .putInt("statusColor", -1)
                                .commit();
                    } else {
                        data.put("showStatusString", statusGetText(showStatusRate));
                        data.put("showStatusRate", showStatusRate);
                        if (showStatusWay.equals(Constants.PRESENT_IN_DIGIT)){
                            sharedPreferences.edit()
                                    .putString("way", "digit")
                                    .commit();
                        } else {
                                sharedPreferences.edit()
                                        .putString("way", "graphic")
                                        .commit();
                        }
                        sharedPreferences.edit()
                                .putInt("status", showStatusRate)
                                .putString("statusText", statusGetText(showStatusRate))
                                .putLong("updateTime", currentTime)
                                .putInt("statusColor", Constants.DEFAULT_COLOR)
                                .commit();
                    }
                    // save show different reasons
                    JSONArray showStatusDifferentReasonArr = new JSONArray();
                    CheckBox[] id = {showStatusDifferentHigh, showStatusDifferentLow,
                             showStatusDifferentAccurate, showStatusDifferentBlurred, showStatusDifferentHide};
                    for (CheckBox i:id) {
                        if (i.isChecked()) showStatusDifferentReasonArr.put(true);
                        else showStatusDifferentReasonArr.put(false);
                    }
                    if (showStatusDifferentOther.isChecked()) {
                        data.put("showStatusDifferent OtherReason", showStatusDifferentOtherReason);
                        Log.d(TAG, "other reason: " + showStatusDifferentOtherReason);
                    }
                    data.put("showStatusDifferentReasons", showStatusDifferentReasonArr);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (pass) {
                // send to database
                // collect user 當前狀態(通知送出時的狀態)
                try {
                    data.put("status", selfStatusRate);
                    data.put("presentWay", selfStatusWay);
                    data.put("statusText", selfStatusString);
                    data.put("statusColor", selfStatusColor);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                mQueue = Volley.newRequestQueue(mContext);
                mJsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Constants.storeSelfQuestionnaire, data,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d(TAG, response.toString());
                                // start activity here
                                try {
                                    if (response.getString("response").equals("success insert")) {
                                        Intent intent = new Intent();
                                        intent.setClass(SelfQuestionnaire.this, ContactList.class);
                                        Toast.makeText(SelfQuestionnaire.this,
                                                "感謝您填寫問卷!", Toast.LENGTH_SHORT).show();
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                        startActivity(intent);
                                        SelfQuestionnaire.this.finish();
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
                Toast.makeText(SelfQuestionnaire.this,
                        "請每題都要填寫!", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private class asOriginalStatusListener implements CheckBox.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (asOriginalStatus.isChecked()) {
                showStatusWay = "original";
                change = false;
                reviseStatusOrNot(false);
                showStatusDifferentReasonLayout.setVisibility(View.GONE);
                // show keyboard
//                mInputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            } else {
                change = true;
                reviseStatusOrNot(true);
                showStatusDifferentReasonLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    private void reviseStatusOrNot(boolean revise) {
        waySpinner.setEnabled(revise);
        textSpinner.setEnabled(revise);
        digitSeekBar.setEnabled(revise);
        graphicSeekBar.setEnabled(revise);
    }

    private class idealRateListener implements RadioGroup.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.strongly_agree:
                    idealPresentationRate = 5;
                    break;
                case R.id.agree:
                    idealPresentationRate = 4;
                    break;
                case R.id.neither:
                    idealPresentationRate = 3;
                    break;
                case R.id.disagree:
                    idealPresentationRate = 2;
                    break;
                case R.id.strongly_disagree:
                    idealPresentationRate = 1;
                    break;
            }
            if (idealPresentationRate == 5 || idealPresentationRate == 4 || idealPresentationRate == 3) {
                statusNotFitLayout.setVisibility(View.GONE);
            } else {
                statusNotFitLayout.setVisibility(View.VISIBLE);
                if (selfStatusWay.equals(Constants.PRESENT_IN_TEXT)) {
                    notFitReasonText.setVisibility(View.VISIBLE);
                    notFitReasonDigit.setVisibility(View.GONE);
                } else {
                    notFitReasonDigit.setVisibility(View.VISIBLE);
                    notFitReasonText.setVisibility(View.GONE);
                }

            }

        }
    }

    private class activityListener1 implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            Log.d(TAG, "checkedId: " + checkedId);
            if (checkedId != -1) {
                if (work.isChecked()) {
                    selectedActivity = "work";
                } else if (study.isChecked()) {
                    selectedActivity = "study";
                } else if (classMeeting.isChecked()) {
                    selectedActivity = "inclass";
                }
                if (work.isChecked() || study.isChecked() || classMeeting.isChecked()) {
                    activityOtherText.clearFocus();
                    radioGroupActivity2.clearCheck();
                    radioGroupActivity3.clearCheck();
                    radioGroupActivity4.clearCheck();
                    radioGroupActivity5.clearCheck();
                }
            }
        }
    }

    private class activityListener2 implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            Log.d(TAG, "checkedId: " + checkedId);
            if (checkedId != -1) {

                if (sleep.isChecked()) {
                    selectedActivity = "sleep";
                } else if (eat.isChecked()) {
                    selectedActivity = "eat";
                } else if (exercising.isChecked()) {
                    selectedActivity = "exercising";
                }
                if (sleep.isChecked() || eat.isChecked() || exercising.isChecked()) {
                    activityOtherText.clearFocus();
                    radioGroupActivity1.clearCheck();
                    radioGroupActivity3.clearCheck();
                    radioGroupActivity4.clearCheck();
                    radioGroupActivity5.clearCheck();
                }
            }
        }
    }

    private class activityListener3 implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.take_a_break:
                    if (takeABreak.isChecked()) {
                        activityOtherText.clearFocus();
                        selectedActivity = "take_a_break";
                        radioGroupActivity1.clearCheck();
                        radioGroupActivity2.clearCheck();
                        radioGroupActivity4.clearCheck();
                        radioGroupActivity5.clearCheck();
                    }
                    break;
            }
        }
    }

    private class activityListener4 implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            Log.d(TAG, "checkedId: " + checkedId);
            if (checkedId != -1) {
                if (shopping.isChecked()) {
                    selectedActivity = "shopping";
                } else if (moving.isChecked()) {
                    selectedActivity = "moving";
                }
                if (shopping.isChecked() || moving.isChecked()) {
                    activityOtherText.clearFocus();
                    radioGroupActivity1.clearCheck();
                    radioGroupActivity2.clearCheck();
                    radioGroupActivity3.clearCheck();
                    radioGroupActivity5.clearCheck();
                }
            }
        }
    }

    private class activityListener5 implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.activity_other:
                    if (activityOther.isChecked()) {
                        activityOtherText.requestFocus();
                        // show keyboard
                        mInputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                        radioGroupActivity1.clearCheck();
                        radioGroupActivity2.clearCheck();
                        radioGroupActivity3.clearCheck();
                        radioGroupActivity4.clearCheck();
                    }
                    break;
            }
        }
    }

    private class activityEditTextListener implements EditText.OnEditorActionListener {
        @Override
        public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
            mInputMethodManager.hideSoftInputFromWindow(SelfQuestionnaire.this.getWindow().getDecorView().getRootView().getWindowToken(), 0);
//            mIBinder = SelfQuestionnaire.this.getCurrentFocus().getWindowToken();
//            mInputMethodManager.hideSoftInputFromWindow(mIBinder, InputMethodManager.HIDE_NOT_ALWAYS);
            selectedActivity = activityOtherText.getText().toString();
            activityOtherText.clearFocus();
            return true;
        }
    }

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
            switch (checkedId) {
                case R.id.location_other:
                    if (locationOther.isChecked()) {
                        locationOtherText.requestFocus();
                        // show keyboard
                        mInputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                        radioGroupLocation1.clearCheck();
                        radioGroupLocation2.clearCheck();
                        radioGroupLocation3.clearCheck();
                    }
                    break;
            }
        }
    }

    private class locationEditTextListener implements EditText.OnEditorActionListener {
        @Override
        public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
            mInputMethodManager.hideSoftInputFromWindow(SelfQuestionnaire.this.getWindow().getDecorView().getRootView().getWindowToken(), 0);
            selectedLocation = locationOtherText.getText().toString();
            locationOtherText.clearFocus();
            return false;
        }
    }

    private class presentWaySpinnerListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            Log.d(TAG, "position: " + position);
            switch (position) {
                case 0: // 文字
                    showStatusWay = Constants.PRESENT_IN_TEXT;
                    textSpinner.setVisibility(View.VISIBLE);
                    digitSeekBar.setVisibility(View.INVISIBLE);
                    graphicCircle.setVisibility(View.INVISIBLE);
                    graphicSeekBar.setVisibility(View.INVISIBLE);
                    break;
                case 1: // 數字
                    showStatusWay = Constants.PRESENT_IN_DIGIT;
                    textSpinner.setVisibility(View.INVISIBLE);
                    digitSeekBar.setVisibility(View.VISIBLE);
                    graphicCircle.setVisibility(View.INVISIBLE);
                    graphicSeekBar.setVisibility(View.INVISIBLE);
                    break;
                case 2:
                    showStatusWay = Constants.PRESENT_IN_GRAPHIC;
                    textSpinner.setVisibility(View.INVISIBLE);
                    digitSeekBar.setVisibility(View.INVISIBLE);
                    graphicCircle.setVisibility(View.VISIBLE);
                    graphicSeekBar.setVisibility(View.VISIBLE);
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    private class textStatusSpinnerListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
            Log.d(TAG, "position: " + position);
            showStatusString = textSpinner.getSelectedItem().toString();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    private class digitSeekBarListener implements OnSeekChangeListener {
        @Override
        public void onSeeking(SeekParams seekParams) {

        }

        @Override
        public void onStartTrackingTouch(IndicatorSeekBar indicatorSeekBar) {

        }

        @Override
        public void onStopTrackingTouch(IndicatorSeekBar indicatorSeekBar) {
            // 存值
            showStatusRate = indicatorSeekBar.getProgress();
        }
    }

    private class graphicSeekBarListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            graphicCircle.setProgress(i);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // 存值
            showStatusRate = seekBar.getProgress();
        }
    }

    private class notFitReasonTextEditListener implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            mInputMethodManager.hideSoftInputFromWindow(SelfQuestionnaire.this.getWindow().getDecorView().getRootView().getWindowToken(), 0);
            statusNotFitOtherReason = notFitReasonTextOtherText.getText().toString();
            notFitReasonTextOtherText.clearFocus();
            return false;
        }
    }

    private class notFitReasonDigitEditListener implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            mInputMethodManager.hideSoftInputFromWindow(SelfQuestionnaire.this.getWindow().getDecorView().getRootView().getWindowToken(), 0);
            statusNotFitOtherReason = notFitReasonDigitOtherText.getText().toString();
            notFitReasonDigitOtherText.clearFocus();
            return false;
        }
    }

    private class showStatusDifferentEditListener implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            mInputMethodManager.hideSoftInputFromWindow(SelfQuestionnaire.this.getWindow().getDecorView().getRootView().getWindowToken(), 0);
            showStatusDifferentOtherReason = showStatusDifferentOtherText.getText().toString();
            showStatusDifferentOtherText.clearFocus();
            return false;
        }
    }
}
