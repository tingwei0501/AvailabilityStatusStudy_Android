package com.example.mystudy;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import com.larswerkman.lobsterpicker.OnColorListener;
import com.larswerkman.lobsterpicker.sliders.LobsterShadeSlider;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

public class EditProfilePage extends Activity {

    private static final String TAG = "EditProfilePage";
    private Context mContext;

    // present way
    private RadioGroup radioGroupPresentWay;
    private String selectedPresentWay;
    //
    // Status: text

    private int selectedTextStatus;
    //
    // Status: digit
    private int selectedDigitStatus;
    //
    // Status: graphic
    private ConstraintLayout constraintLayoutGraphic;
    private CircularProgressBar circularProgressBar;
    private float initialProgress;
    private int selectedGraphicStatus;
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);
        mContext = getApplicationContext();

        // choose present way
        radioGroupPresentWay = findViewById(R.id.editPage_presentWay);
        radioGroupPresentWay.setOnCheckedChangeListener(new presentWayListener());

        // progress circle
        constraintLayoutGraphic = findViewById(R.id.constraintLayoutGraphic);
        initialProgress = 65;
        circularProgressBar = findViewById(R.id.progressCircle);
        circularProgressBar.setProgress(initialProgress);

        //progress seek bar
        SeekBar seekBar = findViewById(R.id.seekBarProgress);
        seekBar.setProgress((int) initialProgress);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                circularProgressBar.setProgress(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

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
                circularProgressBar.setBackgroundColor(adjustAlpha(color, 0.3f));
            }

            @Override
            public void onColorSelected(int color) {

            }
        });
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
                    constraintLayoutGraphic.setVisibility(View.INVISIBLE);
                    break;
                case R.id.present_digit:
                    selectedPresentWay = "digit";
                    constraintLayoutGraphic.setVisibility(View.INVISIBLE);
                    break;
                case R.id.present_graphic:
                    selectedPresentWay = "graphic";
                    constraintLayoutGraphic.setVisibility(View.VISIBLE);
            }
        }
    }
}
