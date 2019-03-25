package com.example.mystudy;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;


public class StatusExample extends Activity {
    private static final String TAG = "StatusExample";
    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.status_example);
        mContext = getApplicationContext();

        CircularProgressBar circularProgressBar = findViewById(R.id.progressbar);
//        circularProgressBar.setColor(Integer.parseInt("#f38967"));
        float progress = 60;
        circularProgressBar.setProgress(0);
        circularProgressBar.setColor(ContextCompat.getColor(mContext, R.color.colorDarkGreen));
        circularProgressBar.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorLightGreen));
        circularProgressBar.setProgressBarWidth(50);
        circularProgressBar.setBackgroundProgressBarWidth(20);
        circularProgressBar.setProgressMax(100);
        circularProgressBar.setProgressWithAnimation(progress, 1500);
        TextView textView = findViewById(R.id.progressbar_text);
        textView.setText(String.valueOf(progress));

    }
}
