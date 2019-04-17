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


public class SelfQuestionnaire extends Activity {
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
    private RadioGroup radioGroupChangeOrNot;
    //
    //Q5: change
    private TextView changeQuestion6;
    private CheckBox changeStatusRatio;
    private CheckBox changeStatusWay;
    private CheckBox changeStatusBlurred;
    private CheckBox changeStatusAccurate;
    private CheckBox changeStatusHide;
    private CheckBox changeOther;
    private EditText changeOtherText;
    //
    // Q5: not change
    private TextView notChangeQuestion6;
    private CheckBox notChangeStatusFit;
    private CheckBox notChangeNotime;
    private CheckBox notChangeHide;
    private CheckBox notChangeOther;
    private EditText notChangeOtherText;
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

        // Q3 目前的狀態，是我理想中的呈現方式?
        radioGroupIdealRate = findViewById(R.id.ideal_or_not);
        radioGroupIdealRate.setOnCheckedChangeListener(new idealRateListener());
        //

        // Q4 4. 請問您想要改變您目前的呈現方式嗎?
        radioGroupChangeOrNot = findViewById(R.id.change_status_or_not);
        radioGroupChangeOrNot.setOnCheckedChangeListener(new changeOrNotListener());
        //

        // Q5: change
        changeQuestion6 = findViewById(R.id.change_question6);
        changeStatusRatio = findViewById(R.id.change_status_ratio);
        changeStatusWay = findViewById(R.id.change_status_way);
        changeStatusBlurred = findViewById(R.id.change_status_blurred);
        changeStatusAccurate = findViewById(R.id.change_status_accurate);
        changeStatusHide = findViewById(R.id.change_status_hide);
        changeOther = findViewById(R.id.change_other);
        changeOtherText = findViewById(R.id.change_other_text);
        // initialize
        changeReasons = new boolean[5];
        changeOther.setOnCheckedChangeListener(new changeReasonListener());

        //
        // Q5: not change
        notChangeQuestion6 = findViewById(R.id.not_change_question6);
        notChangeStatusFit = findViewById(R.id.not_change_status_fit);
        notChangeNotime = findViewById(R.id.not_change_notime);
        notChangeHide = findViewById(R.id.not_change_hide);
        notChangeOther = findViewById(R.id.not_change_other);
        notChangeOtherText = findViewById(R.id.not_change_other_text);
        // initialize
        notChangeReasons = new boolean[3];
        notChangeOther.setOnCheckedChangeListener(new notChangeReasonListener());

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
                    CheckBox[] id = {changeStatusRatio, changeStatusWay, changeStatusBlurred,
                            changeStatusAccurate, changeStatusHide};
                    int index = 0;
                    for (CheckBox i:id) {
                        if (i.isChecked()) changeReasons[index] = true;
                        else changeReasons[index] = false;
                        index++;
                    }
                    if (changeOther.isChecked()) {
                        changeOtherReason = changeOtherText.getText().toString();
                        Log.d(TAG, "other reasons: " + changeOtherReason);
                    }

                } else {
                    Log.d(TAG, "Not change Reasons: ");
                    CheckBox[] id = {notChangeStatusFit, notChangeNotime, notChangeHide};
                    int index = 0;
                    for (CheckBox i:id) {
                        if (i.isChecked()) notChangeReasons[index] = true;
                        else notChangeReasons[index] = false;
                        index++;
                    }
                    if (notChangeOther.isChecked()) {
                        notChangeOtherReason = notChangeOtherText.getText().toString();
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
            if (changeOther.isChecked()) {
                changeOtherText.requestFocus();
                // show keyboard
                mInputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

            }
        }
    }

    private class notChangeReasonListener implements CheckBox.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (notChangeOther.isChecked()) {
                notChangeOtherText.requestFocus();
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
                    changeQuestion6.setVisibility(View.VISIBLE);
                    changeStatusRatio.setVisibility(View.VISIBLE);
                    changeStatusWay.setVisibility(View.VISIBLE);
                    changeStatusBlurred.setVisibility(View.VISIBLE);
                    changeStatusAccurate.setVisibility(View.VISIBLE);
                    changeStatusHide.setVisibility(View.VISIBLE);
                    changeOther.setVisibility(View.VISIBLE);
                    changeOtherText.setVisibility(View.VISIBLE);

                    notChangeQuestion6.setVisibility(View.INVISIBLE);
                    notChangeStatusFit.setVisibility(View.INVISIBLE);
                    notChangeNotime.setVisibility(View.INVISIBLE);
                    notChangeHide.setVisibility(View.INVISIBLE);
                    notChangeOther.setVisibility(View.INVISIBLE);
                    notChangeOtherText.setVisibility(View.INVISIBLE);
                    break;
                case R.id.no_thanks:
                    change = false;
                    notChangeQuestion6.setVisibility(View.VISIBLE);
                    notChangeStatusFit.setVisibility(View.VISIBLE);
                    notChangeNotime.setVisibility(View.VISIBLE);
                    notChangeHide.setVisibility(View.VISIBLE);
                    notChangeOther.setVisibility(View.VISIBLE);
                    notChangeOtherText .setVisibility(View.VISIBLE);

                    changeQuestion6.setVisibility(View.INVISIBLE);
                    changeStatusRatio.setVisibility(View.INVISIBLE);
                    changeStatusWay.setVisibility(View.INVISIBLE);
                    changeStatusBlurred.setVisibility(View.INVISIBLE);
                    changeStatusAccurate.setVisibility(View.INVISIBLE);
                    changeStatusHide.setVisibility(View.INVISIBLE);
                    changeOther.setVisibility(View.INVISIBLE);
                    changeOtherText.setVisibility(View.INVISIBLE);
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
            mIBinder = SelfQuestionnaire.this.getCurrentFocus().getWindowToken();
            mInputMethodManager.hideSoftInputFromWindow(mIBinder, InputMethodManager.HIDE_NOT_ALWAYS);
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
                }
                if (classroom.isChecked() || library.isChecked() || outdoor.isChecked()) {
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
            mIBinder = SelfQuestionnaire.this.getCurrentFocus().getWindowToken();
            mInputMethodManager.hideSoftInputFromWindow(mIBinder, InputMethodManager.HIDE_NOT_ALWAYS);
            selectedLocation = locationOtherText.getText().toString();
            locationOtherText.clearFocus();
            return false;
        }
    }
}
