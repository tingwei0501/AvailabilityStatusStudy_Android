package com.example.mystudy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.larswerkman.lobsterpicker.OnColorListener;
import com.larswerkman.lobsterpicker.sliders.LobsterShadeSlider;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

public class EditProfilePage extends Activity {

    private static final String TAG = "EditProfilePage";
    private Context mContext;

    // present way
    private RadioGroup radioGroupPresentWay;
    private String selectedPresentWay;
    //
    // Status: text
    private ConstraintLayout constraintLayoutText;
    private String selectedTextStatus;
    private RadioGroup radioGroupTextStatus;
    //
    // Status: digit
    private ConstraintLayout constraintLayoutDigit;
    private int selectedDigitStatus;
    private TextView editPage_digit_rate;
    //
    // Status: graphic
    private ConstraintLayout constraintLayoutGraphic;
    private CircularProgressBar circularProgressBar;
    private float initialProgress;
    private int selectedGraphicStatus;
    private int selectedColor;
//    private int selectedBackgroundColor;
    //
    // done button
    private Button done;
    private Button cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);
        mContext = getApplicationContext();
        initialProgress = 65;
        selectedPresentWay = "text";

        // choose present way
        radioGroupPresentWay = findViewById(R.id.editPage_presentWay);
        radioGroupPresentWay.setOnCheckedChangeListener(new presentWayListener());

        // done button & cancel button
        done = findViewById(R.id.edit_profile_done);
        done.setOnClickListener(doneListener);
        cancel = findViewById(R.id.edit_profile_cancel);
        cancel.setOnClickListener(cancelListener);

        // text:
        constraintLayoutText = findViewById(R.id.constraintLayoutText);
        radioGroupTextStatus = findViewById(R.id.radioGroupTextStatus);
        radioGroupTextStatus.setOnCheckedChangeListener(new textStatusListener());

        // digit: progress seek bar
        constraintLayoutDigit = findViewById(R.id.constraintLayoutDigit);
        editPage_digit_rate = findViewById(R.id.editPage_digit_rate);
        editPage_digit_rate.setText(String.valueOf(initialProgress));
        IndicatorSeekBar digitSeekBar = findViewById(R.id.seekBarProgressDigit);
        digitSeekBar.setProgress((int) initialProgress);
        digitSeekBar.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                Log.d(TAG, "test: " + seekParams.progress);
                editPage_digit_rate.setText(String.valueOf(seekParams.progress));
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar  seekBar) {}

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar  seekBar) {
                Log.d(TAG, "progress: " + seekBar.getProgress());
                selectedDigitStatus = seekBar.getProgress();
            }
        });

        // graphic: progress circle
        constraintLayoutGraphic = findViewById(R.id.constraintLayoutGraphic);
        circularProgressBar = findViewById(R.id.progressCircle);
        circularProgressBar.setProgress(initialProgress);

        // graphic: progress seek bar
        SeekBar graphicSeekBar = findViewById(R.id.seekBarProgressGraphic);
        graphicSeekBar.setProgress((int) initialProgress);
        graphicSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                circularProgressBar.setProgress(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "progress: " + seekBar.getProgress());
                selectedGraphicStatus = seekBar.getProgress();
            }
        });

        ((LobsterShadeSlider) findViewById(R.id.shadeslider)).addOnColorListener(new OnColorListener() {
            @Override
            public void onColorChanged(int color) {
                circularProgressBar.setColor(color);
//                circularProgressBar.setBackgroundColor(adjustAlpha(color, 0.3f));
            }

            @Override
            public void onColorSelected(int color) {
                Log.d(TAG, "selected color: " + color);
                selectedColor = color;
//                selectedBackgroundColor = adjustAlpha(color, 0.3f);
//                Log.d(TAG, "selectedBackgroundColor: " + selectedBackgroundColor);
//                switch (color) {
//                    case -7617718:
//                        selectedColor = "green";
//                        break;
//                    case -16728876:
//                        selectedColor = "lightBlue";
//                        break;
//                    case -5317:
//                        selectedColor = "yellow";
//                        break;
//                    case -2937298:
//                        selectedColor = "red";
//                        break;
//                    case -10011977:
//                        selectedColor = "purple";
//                        break;
//                    case -12627531:
//                        selectedColor = "darkBlue";
//                        break;
//                }
            }
        });
    }

    private Button.OnClickListener doneListener = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO: send data to database
            // TODO: send questionnaire

            Intent intent = new Intent(EditProfilePage.this, ContactList.class);
//            intent.putExtra("from", "EditProfilePage");
//            intent.putExtra("presentWay", selectedPresentWay);

            SharedPreferences preferences = getSharedPreferences("user", MODE_PRIVATE);

            if (selectedPresentWay.equals("text")) {
                Log.d(TAG, "selectedTextStatus: " + selectedTextStatus);
                preferences.edit()
                        .putString("way", "text")
                        .putString("status", selectedTextStatus)
                        .commit();
//                intent.putExtra("selectedTextStatus", selectedTextStatus);
            } else if (selectedPresentWay.equals("digit")) {
                Log.d(TAG, "selectedDigitStatus: " + selectedDigitStatus);
                preferences.edit()
                        .putString("way", "digit")
                        .putString("status", String.valueOf(selectedDigitStatus))
                        .commit();
//                intent.putExtra("selectedDigitStatus", String.valueOf(selectedDigitStatus));
            } else if (selectedPresentWay.equals("graphic")) {
                Log.d(TAG, "selectedGraphicStatus: " + selectedGraphicStatus);
                Log.d(TAG, "selectedColor: " + selectedColor);
                preferences.edit()
                        .putString("way", "graphic")
                        .putString("status", String.valueOf(selectedGraphicStatus))
                        .putInt("color", selectedColor)
//                        .putInt("backgroundColor", selectedBackgroundColor)
                        .commit();
//                intent.putExtra("selectedGraphicStatus", String.valueOf(selectedGraphicStatus));
//                intent.putExtra("selectedColor", String.valueOf(selectedColor));
//                intent.putExtra("selectedBackgroundColor", String.valueOf(selectedBackgroundColor));
            }
            startActivity(intent);
        }
    };

    private Button.OnClickListener cancelListener = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(EditProfilePage.this, ContactList.class);
            intent.putExtra("from", "EditProfilePage");

            startActivity(intent);
        }
    };

    private class textStatusListener implements RadioGroup.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.textStatus_ratio_low:
                    Log.d(TAG, "textStatus_ratio_low");
                    selectedTextStatus = "回覆率低";
                    break;
                case R.id.textStatus_ratio_high:
                    Log.d(TAG, "textStatus_ratio_high");
                    selectedTextStatus = "回覆率高";
                    break;
            }
        }
    }

    private int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    private class presentWayListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.present_text:
                    selectedPresentWay = "text";
                    constraintLayoutText.setVisibility(View.VISIBLE);
                    constraintLayoutDigit.setVisibility(View.INVISIBLE);
                    constraintLayoutGraphic.setVisibility(View.INVISIBLE);
                    break;
                case R.id.present_digit:
                    selectedPresentWay = "digit";
                    constraintLayoutText.setVisibility(View.INVISIBLE);
                    constraintLayoutDigit.setVisibility(View.VISIBLE);
                    constraintLayoutGraphic.setVisibility(View.INVISIBLE);
                    break;
                case R.id.present_graphic:
                    selectedPresentWay = "graphic";
                    constraintLayoutText.setVisibility(View.INVISIBLE);
                    constraintLayoutDigit.setVisibility(View.INVISIBLE);
                    constraintLayoutGraphic.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }
}
