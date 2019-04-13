package com.example.mystudy;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;


public class Questionnaire extends Activity {
    private static final String TAG = "Questionnaire";
    private Context mContext;
    private IBinder mIBinder;
    private InputMethodManager mInputMethodManager;

    // collected data
    private String selectedLocation;
    private String selectedActivity;
    private int idealPresentationRate;
    private boolean change;
    private boolean[] changeReasons;
    private String changeOtherReason;
    private boolean[] notChangeReasons;
    private String notChangeOtherReason;

    // Q1
    private RadioGroup radioGroupLocation1;
    private RadioButton home;
    private RadioButton dorm;
    private RadioButton lab;

    private RadioGroup radioGroupLocation2;
    private RadioButton classroom;
    private RadioButton library;
    private RadioButton outdoor;

    private RadioGroup radioGroupLocation3;
    private RadioButton transportation;
    private RadioButton mall;

    private RadioGroup radioGroupLocation4;
    private RadioButton location_other;
    private EditText location_other_text;
    //
    //Q2
    private RadioGroup radioGroupActivity1;
    private RadioButton work;
    private RadioButton study;
    private RadioButton class_meeting;

    private RadioGroup radioGroupActivity2;
    private RadioButton eat;
    private RadioButton sleep;
    private RadioButton exercising;

    private RadioGroup radioGroupActivity3;
    private RadioButton take_a_break;

    private RadioGroup radioGroupActivity4;
    private RadioButton shopping;
    private RadioButton moving;

    private RadioGroup radioGroupActivity5;
    private RadioButton activity_other;
    private EditText activity_other_text;
    //
    // Q3
    private RadioGroup radioGroupIdealRate;
    //
    // Q4
    private RadioGroup radioGroupChangeOrNot;
    //
    //Q5: change
    private TextView change_question6;
    private CheckBox change_status_ratio;
    private CheckBox change_status_way;
    private CheckBox change_status_blurred;
    private CheckBox change_status_accurate;
    private CheckBox change_status_hide;
    private CheckBox change_other;
    private EditText change_other_text;
    //
    // Q5: not change
    private TextView not_change_question6;
    private CheckBox not_change_status_fit;
    private CheckBox not_change_notime;
    private CheckBox not_change_hide;
    private CheckBox not_change_other;
    private EditText not_change_other_text;
    //
    private Button submit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.questionnaire);
        mContext = getApplicationContext();

        mInputMethodManager = (InputMethodManager) getSystemService(mContext.INPUT_METHOD_SERVICE);

//        Log.d(TAG, String.valueOf(TextUtils.isEmpty(selectedLocation)));
//        if(!TextUtils.isEmpty(selectedLocation)){
//            setRadioButtonState();
//        }
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
        transportation = findViewById(R.id.transportation);
        mall = findViewById(R.id.mall);
        location_other = findViewById(R.id.location_other);
        location_other_text = findViewById(R.id.location_other_text);
        location_other_text.setOnEditorActionListener(new locationEditTextListener());

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
        class_meeting = findViewById(R.id.class_meeting);
        eat = findViewById(R.id.eat);
        sleep = findViewById(R.id.sleep);
        exercising = findViewById(R.id.exercising);
        take_a_break = findViewById(R.id.take_a_break);
        shopping = findViewById(R.id.shopping);
        moving = findViewById(R.id.moving);
        activity_other = findViewById(R.id.activity_other);
        activity_other_text = findViewById(R.id.activity_other_text);
        activity_other_text.setOnEditorActionListener(new activityEditTextListener());

        radioGroupActivity1.setOnCheckedChangeListener(new activityListener1());
        radioGroupActivity2.setOnCheckedChangeListener(new activityListener2());
        radioGroupActivity3.setOnCheckedChangeListener(new activityListener3());
        radioGroupActivity4.setOnCheckedChangeListener(new activityListener4());
        radioGroupActivity5.setOnCheckedChangeListener(new activityListener5());
        //

        // Q3 目前的狀態，是我理想中的呈現方式?
        radioGroupIdealRate = findViewById(R.id.ideal_or_not);
        radioGroupIdealRate.setOnCheckedChangeListener(new idealRateListener());
        //

        // Q4 4. 請問您想要改變您目前的呈現方式嗎?
        radioGroupChangeOrNot = findViewById(R.id.change_status_or_not);

        radioGroupChangeOrNot.setOnCheckedChangeListener(new changeOrNotListener());
        //

        // Q5: change
        change_question6 = findViewById(R.id.change_question6);
        change_status_ratio = findViewById(R.id.change_status_ratio);
        change_status_way = findViewById(R.id.change_status_way);
        change_status_blurred = findViewById(R.id.change_status_blurred);
        change_status_accurate = findViewById(R.id.change_status_accurate);
        change_status_hide = findViewById(R.id.change_status_hide);
        change_other = findViewById(R.id.change_other);
        change_other_text = findViewById(R.id.change_other_text);
        // initialize
        changeReasons = new boolean[5];
        change_other.setOnCheckedChangeListener(new changeReasonListener());

        //
        // Q5: not change
        not_change_question6 = findViewById(R.id.not_change_question6);
        not_change_status_fit = findViewById(R.id.not_change_status_fit);
        not_change_notime = findViewById(R.id.not_change_notime);
        not_change_hide = findViewById(R.id.not_change_hide);
        not_change_other = findViewById(R.id.not_change_other);
        not_change_other_text = findViewById(R.id.not_change_other_text);
        // initialize
        notChangeReasons = new boolean[3];
        not_change_other.setOnCheckedChangeListener(new notChangeReasonListener());

        // submit button
        submit = findViewById(R.id.self_questionnaire_submit);
        submit.setOnClickListener(submitListener);
    }

//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        selectedLocation = savedInstanceState.getString("Location");
//        Log.d(TAG, "onRestoreInstanceState");
//        Log.d(TAG, selectedLocation);
//    }

//    @Override
//    protected void onSaveInstanceState(Bundle savedInstanceState) {
//        // Save custom values into the bundle
//
//        if(!TextUtils.isEmpty(selectedLocation)){
//            savedInstanceState.putString("Location", selectedLocation);
//            Log.d(TAG, selectedLocation);
//        }
////        savedInstanceState.putString("Location", selectedLocation);
//
//        // Always call the superclass so it can save the view hierarchy state
//        super.onSaveInstanceState(savedInstanceState);
//    }

//    private void setRadioButtonState(){
//        if(selectedLocation.equals("home")){
//            home.setChecked(true);
//        } else if (selectedLocation.equals("dorm")) {
//            dorm.setChecked(true);
//        } else if (selectedLocation.equals("lab")) {
//            lab.setChecked(true);
//        } else if (selectedLocation.equals("classroom")) {
//            classroom.setChecked(true);
//        } else if (selectedLocation.equals("library")) {
//            library.setChecked(true);
//        } else if (selectedLocation.equals("outdoor")) {
//            outdoor.setChecked(true);
//        }
//
//    }

    private Button.OnClickListener submitListener = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO: send to database
            if (selectedLocation != null && selectedActivity != null && idealPresentationRate != 0) {
                Log.d(TAG, "selectedLocation:  "+ selectedLocation);
                Log.d(TAG, "selectedActivity: " + selectedActivity);
                Log.d(TAG, "idealPresentationRate: " + idealPresentationRate);
                Log.d(TAG, "change: " + change);

                if (change) {
                    Log.d(TAG, "changeReasons: ");
                    CheckBox[] id = {change_status_ratio, change_status_way, change_status_blurred,
                                change_status_accurate, change_status_hide};
                    int index = 0;
                    for (CheckBox i:id) {
                        if (i.isChecked()) changeReasons[index] = true;
                        else changeReasons[index] = false;
                        index++;
                    }
                    if (change_other.isChecked()) {
                        changeOtherReason = change_other_text.getText().toString();
                        Log.d(TAG, "other reasons: " + changeOtherReason);
                    }

                } else {
                    Log.d(TAG, "Not change Reasons: ");
                    CheckBox[] id = {not_change_status_fit, not_change_notime, not_change_hide};
                    int index = 0;
                    for (CheckBox i:id) {
                        if (i.isChecked()) notChangeReasons[index] = true;
                        else notChangeReasons[index] = false;
                        index++;
                    }
                    if (not_change_other.isChecked()) {
                        notChangeOtherReason = not_change_other_text.getText().toString();
                        Log.d(TAG, "other reason: " + notChangeOtherReason);
                    }
                    // initially notChangeOtherReason string is null
//                    Log.d(TAG, "other reason: " + notChangeOtherReason);
                    for (int i=0;i<notChangeReasons.length;i++) {
                        Log.d(TAG, String.valueOf(notChangeReasons[i]));
                    }
                }
//                for (int i=0;i<changeReasons.length;i++) {
//                    Log.d(TAG, String.valueOf(changeReasons[i]));
//                }
            }
        }
    };

    private class changeReasonListener implements CheckBox.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (change_other.isChecked()) {

                change_other_text.requestFocus();
                // show keyboard
                mInputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

            }
        }
    }

    private class notChangeReasonListener implements CheckBox.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (not_change_other.isChecked()) {
                not_change_other_text.requestFocus();
                // show keyboard
                mInputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    private class changeOrNotListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.go_change_status:
                    change = true;
                    change_question6.setVisibility(View.VISIBLE);
                    change_status_ratio.setVisibility(View.VISIBLE);
                    change_status_way.setVisibility(View.VISIBLE);
                    change_status_blurred.setVisibility(View.VISIBLE);
                    change_status_accurate.setVisibility(View.VISIBLE);
                    change_status_hide.setVisibility(View.VISIBLE);
                    change_other.setVisibility(View.VISIBLE);
                    change_other_text.setVisibility(View.VISIBLE);

                    not_change_question6.setVisibility(View.INVISIBLE);
                    not_change_status_fit.setVisibility(View.INVISIBLE);
                    not_change_notime.setVisibility(View.INVISIBLE);
                    not_change_hide.setVisibility(View.INVISIBLE);
                    not_change_other.setVisibility(View.INVISIBLE);
                    not_change_other_text.setVisibility(View.INVISIBLE);
                    break;
                case R.id.no_thanks:
                    change = false;
                    not_change_question6.setVisibility(View.VISIBLE);
                    not_change_status_fit.setVisibility(View.VISIBLE);
                    not_change_notime.setVisibility(View.VISIBLE);
                    not_change_hide.setVisibility(View.VISIBLE);
                    not_change_other.setVisibility(View.VISIBLE);
                    not_change_other_text.setVisibility(View.VISIBLE);

                    change_question6.setVisibility(View.INVISIBLE);
                    change_status_ratio.setVisibility(View.INVISIBLE);
                    change_status_way.setVisibility(View.INVISIBLE);
                    change_status_blurred.setVisibility(View.INVISIBLE);
                    change_status_accurate.setVisibility(View.INVISIBLE);
                    change_status_hide.setVisibility(View.INVISIBLE);
                    change_other.setVisibility(View.INVISIBLE);
                    change_other_text.setVisibility(View.INVISIBLE);
                    break;
            }
        }
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
        }
    }

    private class activityListener1 implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.work:
                    if (work.isChecked()) {
                        activity_other_text.clearFocus();
                        selectedActivity = "work";
                        radioGroupActivity2.clearCheck();
                        radioGroupActivity3.clearCheck();
                        radioGroupActivity4.clearCheck();
                        radioGroupActivity5.clearCheck();
                    }
                    break;
                case R.id.study:
                    if (study.isChecked()) {
                        activity_other_text.clearFocus();
                        selectedActivity = "study";
                        radioGroupActivity2.clearCheck();
                        radioGroupActivity3.clearCheck();
                        radioGroupActivity4.clearCheck();
                        radioGroupActivity5.clearCheck();
                    }
                    break;
                case R.id.class_meeting:
                    if (class_meeting.isChecked()) {
                        activity_other_text.clearFocus();
                        selectedActivity = "inclass";
                        radioGroupActivity2.clearCheck();
                        radioGroupActivity3.clearCheck();
                        radioGroupActivity4.clearCheck();
                        radioGroupActivity5.clearCheck();
                    }
                    break;
            }
        }
    }

    private class activityListener2 implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.sleep:
                    if (sleep.isChecked()) {
                        selectedActivity = "sleep";
                        radioGroupActivity1.clearCheck();
                        radioGroupActivity3.clearCheck();
                        radioGroupActivity4.clearCheck();
                        radioGroupActivity5.clearCheck();
                    }
                    break;
                case R.id.eat:
                    if (eat.isChecked()) {
                        activity_other_text.clearFocus();
                        selectedActivity = "eat";
                        radioGroupActivity1.clearCheck();
                        radioGroupActivity3.clearCheck();
                        radioGroupActivity4.clearCheck();
                        radioGroupActivity5.clearCheck();
                    }
                    break;
                case R.id.exercising:
                    if (exercising.isChecked()) {
                        activity_other_text.clearFocus();
                        selectedActivity = "exercising";
                        radioGroupActivity1.clearCheck();
                        radioGroupActivity3.clearCheck();
                        radioGroupActivity4.clearCheck();
                        radioGroupActivity5.clearCheck();
                    }
                    break;
            }
        }
    }

    private class activityListener3 implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.take_a_break:
                    if (take_a_break.isChecked()) {
                        activity_other_text.clearFocus();
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
            switch (checkedId) {
                case R.id.shopping:
                    if (shopping.isChecked()) {
                        activity_other_text.clearFocus();
                        selectedActivity = "shopping";
                        radioGroupActivity1.clearCheck();
                        radioGroupActivity2.clearCheck();
                        radioGroupActivity3.clearCheck();
                        radioGroupActivity5.clearCheck();
                    }
                    break;
                case R.id.moving:
                    if (moving.isChecked()) {
                        activity_other_text.clearFocus();
                        selectedActivity = "moving";
                        radioGroupActivity1.clearCheck();
                        radioGroupActivity2.clearCheck();
                        radioGroupActivity3.clearCheck();
                        radioGroupActivity5.clearCheck();
                    }
                    break;
            }
        }
    }

    private class activityListener5 implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.activity_other:
                    if (activity_other.isChecked()) {
                        activity_other_text.requestFocus();
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
            mIBinder = Questionnaire.this.getCurrentFocus().getWindowToken();
            mInputMethodManager.hideSoftInputFromWindow(mIBinder, InputMethodManager.HIDE_NOT_ALWAYS);
            selectedActivity = activity_other_text.getText().toString();
            activity_other_text.clearFocus();
            return true;
        }
    }

    private class locationListener1 implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.home:
                    if (home.isChecked()) {
                        location_other_text.clearFocus();
                        Log.d(TAG, String.valueOf(home.getId())); // 2131165260
                        Log.d(TAG, (String) home.getText()); // 家裡
                        selectedLocation = "home";
                        radioGroupLocation2.clearCheck();
                        radioGroupLocation3.clearCheck();
                        radioGroupLocation4.clearCheck();
                    }
                    break;
                case R.id.dorm:
                    if (dorm.isChecked()) {
                        location_other_text.clearFocus();
                        selectedLocation = "dorm";
                        radioGroupLocation2.clearCheck();
                        radioGroupLocation3.clearCheck();
                        radioGroupLocation4.clearCheck();
                    }
                    break;
                case R.id.lab:
                    if (lab.isChecked()) {
                        location_other_text.clearFocus();
                        selectedLocation = "lab";
                        radioGroupLocation2.clearCheck();
                        radioGroupLocation3.clearCheck();
                        radioGroupLocation4.clearCheck();
                    }
                    break;
            }
        }
    }

    private class locationListener2 implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.classroom:
                    if (classroom.isChecked()) {
                        location_other_text.clearFocus();
                        selectedLocation = "classroom";
                        radioGroupLocation1.clearCheck();
                        radioGroupLocation3.clearCheck();
                        radioGroupLocation4.clearCheck();
                    }
                    break;
                case R.id.library:
                    if (library.isChecked()) {
                        location_other_text.clearFocus();
                        selectedLocation = "library";
                        radioGroupLocation1.clearCheck();
                        radioGroupLocation3.clearCheck();
                        radioGroupLocation4.clearCheck();
                    }
                    break;
                case R.id.outdoor:
                    if (outdoor.isChecked()) {
                        location_other_text.clearFocus();
                        selectedLocation = "outdoor";
                        radioGroupLocation1.clearCheck();
                        radioGroupLocation3.clearCheck();
                        radioGroupLocation4.clearCheck();
                    }
                    break;
            }
        }
    }

    private class locationListener3 implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.transportation:
                    if (transportation.isChecked()) {
                        location_other_text.clearFocus();
                        selectedLocation = "transportation";
                        radioGroupLocation1.clearCheck();
                        radioGroupLocation2.clearCheck();
                        radioGroupLocation4.clearCheck();
                    }
                    break;
                case R.id.mall:
                    if (mall.isChecked()) {
                        location_other_text.clearFocus();
                        selectedLocation = "mall";
                        radioGroupLocation1.clearCheck();
                        radioGroupLocation2.clearCheck();
                        radioGroupLocation4.clearCheck();
                    }
                    break;
            }
        }
    }

    private class locationListener4 implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.location_other:
                    if (location_other.isChecked()) {
                        location_other_text.requestFocus();
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
            mIBinder = Questionnaire.this.getCurrentFocus().getWindowToken();
            mInputMethodManager.hideSoftInputFromWindow(mIBinder, InputMethodManager.HIDE_NOT_ALWAYS);
//            Log.d(TAG, "in onEditorAction");
            selectedLocation = location_other_text.getText().toString();
            location_other_text.clearFocus();
            return false;
        }
    }
}
