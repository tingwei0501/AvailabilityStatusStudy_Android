package com.example.mystudy;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import me.itangqi.waveloadingview.WaveLoadingView;


public class StatusExample extends Activity {
    private static final String TAG = "StatusExample";
    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.status_example);
        mContext = getApplicationContext();

        /** circle progress */
        CircularProgressBar circularProgressBar = findViewById(R.id.progresscircle);
        CircularProgressBar circularProgressBar2 = findViewById(R.id.progresscircle2);

        float progress = 30;
        float progress2 = 80;

        circularProgressBar.setColor(ContextCompat.getColor(mContext, R.color.colorDarkPink));
        circularProgressBar.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorLightPink));
        circularProgressBar.setProgressWithAnimation(progress, 1500);
        TextView textView2 = findViewById(R.id.progressbar_text);
        textView2.setText(String.valueOf(progress));

        circularProgressBar2.setColor(ContextCompat.getColor(mContext, R.color.colorDarkGreen));
        circularProgressBar2.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorLightGreen));
        circularProgressBar2.setProgressWithAnimation(progress2, 1500);
//        TextView textView = findViewById(R.id.progressbar_text2);
//        textView.setText(String.valueOf(progress2));

        /** wave circle */
        WaveLoadingView waveLoadingView = findViewById(R.id.waveloading);
        waveLoadingView.setProgressValue((int) progress);
        waveLoadingView.setWaveColor(ContextCompat.getColor(mContext, R.color.colorDarkPink));
        waveLoadingView.setBorderColor(ContextCompat.getColor(mContext, R.color.colorLightPink));
        waveLoadingView.setAnimDuration(2000);
//        if (progress<80) {
//            waveLoadingView.setCenterTitle("");
//            waveLoadingView.setBottomTitle(String.valueOf(progress));
//            waveLoadingView.setTopTitle("");
//        }
        WaveLoadingView waveLoadingView2 = findViewById(R.id.waveloading2);
        waveLoadingView2.setProgressValue((int) progress2);
        waveLoadingView2.setWaveColor(ContextCompat.getColor(mContext, R.color.colorDarkGreen));
        waveLoadingView2.setBorderColor(ContextCompat.getColor(mContext, R.color.colorLightGreen));
        waveLoadingView2.setAnimDuration(2000);
        if (progress2<100) {
            waveLoadingView2.setCenterTitle("");
            waveLoadingView2.setBottomTitle("");
            waveLoadingView2.setTopTitle(String.valueOf(progress2));
        }

        /** progress bar */
        ProgressBar progressBar = findViewById(R.id.progressBar1);
        progressBar.setProgress((int) progress);

        /** text */
        TextView textView1 = findViewById(R.id.textView9);
        textView1.setText("目前回覆率低");

        TextView textView3 = findViewById(R.id.textView10);
        textView3.setText("目前回覆率 30%");
    }
}
