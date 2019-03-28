package com.example.mystudy;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class Questionnaire extends Activity {
    private static final String TAG = "Questionnaire";
    private Context mContext;

    private String selectedLocation;
    private String selectedActivity;

    // Q1
    private RadioGroup radioGroupLocation1;
    private RadioButton home;
    private RadioButton dorm;
    private RadioButton lab;

    private RadioGroup radioGroupLocation2;
    private RadioButton classroom;
    private RadioButton library;
    private RadioButton outdoor;
    //
    //Q2
    private RadioGroup radioGroupActivity1;
    private RadioButton work;
    private RadioButton study;
    private RadioButton inclass;

    private RadioGroup radioGroupActivity2;
    private RadioButton meeting;
    private RadioButton eat;
    private RadioButton exercising;

    private RadioGroup radioGroupActivity3;
    private RadioButton phone;
    //
    // Q5
    private RadioGroup radioGroupChangeOrNot;
    private RadioButton yes;
    private RadioButton no;
    //
    //Q6: change
    private TextView change_question6;
    private RadioGroup radioGroupChange;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.questionnaire);
        mContext = getApplicationContext();

//        Log.d(TAG, String.valueOf(TextUtils.isEmpty(selectedLocation)));
//        if(!TextUtils.isEmpty(selectedLocation)){
//            setRadioButtonState();
//        }
        // Q1 1. 請問您當時在哪裡?
        radioGroupLocation1 = findViewById(R.id.location1);
        radioGroupLocation2 = findViewById(R.id.location2);
        home = findViewById(R.id.home);
        dorm = findViewById(R.id.dorm);
        lab = findViewById(R.id.lab);
        classroom = findViewById(R.id.classroom);
        library = findViewById(R.id.library);
        outdoor = findViewById(R.id.outdoor);

        radioGroupLocation1.setOnCheckedChangeListener(new locationListener1());
        radioGroupLocation2.setOnCheckedChangeListener(new locationListener2());
        //

        // Q2 2. 請問接到通知前，您當時正在做甚麼?
        radioGroupActivity1 = findViewById(R.id.activity1);
        radioGroupActivity2 = findViewById(R.id.activity2);
        radioGroupActivity3 = findViewById(R.id.activity3);
        work = findViewById(R.id.work);
        study = findViewById(R.id.study);
        inclass = findViewById(R.id.inclass);
        meeting = findViewById(R.id.meeting);
        eat = findViewById(R.id.eat);
        exercising = findViewById(R.id.exercising);
        phone = findViewById(R.id.phone);

        radioGroupActivity1.setOnCheckedChangeListener(new activityListener1());
        radioGroupActivity2.setOnCheckedChangeListener(new activityListener2());
        radioGroupActivity3.setOnCheckedChangeListener(new activityListener3());

        // Q5 5. 請問您想要改變您目前的呈現方式嗎?
        radioGroupChangeOrNot = findViewById(R.id.change_status_or_not);
        yes = findViewById(R.id.go_change_status);
        no = findViewById(R.id.no_thanks);

        radioGroupChangeOrNot.setOnCheckedChangeListener(new changeOrNotListener());

        // Q6: change
        change_question6 = findViewById(R.id.change_question6);
        radioGroupChange = findViewById(R.id.change_reason);
//        radioGroupChange.setOnCheckedChangeListener(new changeListener());
//        button1to2 = findViewById(R.id.button_1to2);
//        button1to2.setOnClickListener(button1to2Listener);


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

    private class changeOrNotListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.go_change_status:
                    change_question6.setVisibility(View.VISIBLE);
                    radioGroupChange.setVisibility(View.VISIBLE);
                    break;
                case R.id.no_thanks:

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
                        selectedActivity = "work";
                        radioGroupActivity2.clearCheck();
                        radioGroupActivity3.clearCheck();
                    }
                    break;
                case R.id.study:
                    if (study.isChecked()) {
                        selectedActivity = "study";
                        radioGroupActivity2.clearCheck();
                        radioGroupActivity3.clearCheck();
                    }
                    break;
                case R.id.inclass:
                    if (inclass.isChecked()) {
                        selectedActivity = "inclass";
                        radioGroupActivity2.clearCheck();
                        radioGroupActivity3.clearCheck();
                    }
                    break;
            }
        }
    }

    private class activityListener2 implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.meeting:
                    if (meeting.isChecked()) {
                        selectedActivity = "meeting";
                        radioGroupActivity1.clearCheck();
                        radioGroupActivity3.clearCheck();
                    }
                    break;
                case R.id.eat:
                    if (eat.isChecked()) {
                        selectedActivity = "eat";
                        radioGroupActivity1.clearCheck();
                        radioGroupActivity3.clearCheck();
                    }
                    break;
                case R.id.exercising:
                    if (exercising.isChecked()) {
                        selectedActivity = "exercising";
                        radioGroupActivity1.clearCheck();
                        radioGroupActivity3.clearCheck();
                    }
                    break;
            }
        }
    }

    private class activityListener3 implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.phone:
                    if (phone.isChecked()) {
                        selectedActivity = "phone";
                        radioGroupActivity1.clearCheck();
                        radioGroupActivity2.clearCheck();
                    }
                    break;
//                case R.id.eat:
//                    if (eat.isChecked()) {
//                        radioGroupActivity1.clearCheck();
//                        radioGroupActivity2.clearCheck();
//                    }
//                    break;
//                case R.id.exercising:
//                    if (exercising.isChecked()) {
//                        radioGroupActivity1.clearCheck();
//                        radioGroupActivity2.clearCheck();
//                    }
//                    break;
            }
        }
    }

    private class locationListener1 implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.home:
                    if (home.isChecked()) {
                        Log.d(TAG, String.valueOf(home.getId())); // 2131165260
                        Log.d(TAG, (String) home.getText()); // 家裡
                        selectedLocation = "home";
                        radioGroupLocation2.clearCheck();
                    }
                    break;
                case R.id.dorm:
                    if (dorm.isChecked()) {
                        selectedLocation = "dorm";
                        radioGroupLocation2.clearCheck();
                    }
                    break;
                case R.id.lab:
                    if (lab.isChecked()) {
                        selectedLocation = "lab";
                        radioGroupLocation2.clearCheck();
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
                        selectedLocation = "classroom";
                        radioGroupLocation1.clearCheck();
                    }
                    break;
                case R.id.library:
                    if (library.isChecked()) {
                        selectedLocation = "library";
                        radioGroupLocation1.clearCheck();
                    }
                    break;
                case R.id.outdoor:
                    if (outdoor.isChecked()) {
                        selectedLocation = "outdoor";
                        radioGroupLocation1.clearCheck();
                    }
                    break;
            }
        }
    }
}
