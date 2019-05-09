package labelingStudy.nctu.minuku_2;

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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.larswerkman.lobsterpicker.OnColorListener;
import com.larswerkman.lobsterpicker.sliders.LobsterShadeSlider;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import org.json.JSONException;
import org.json.JSONObject;

import labelingStudy.nctu.minuku.Utilities.ScheduleAndSampleManager;

public class EditProfilePage extends Activity {

    private static final String TAG = "EditProfilePage";
    private static final String storeSelfStatusUrl = "http://13.59.255.194:5000/storeSelfStatus";
    private SharedPreferences sharedPreferences;
    private Context mContext;
    private RequestQueue mQueue;
    private JsonObjectRequest mJsonObjectRequest;

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
        sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        initialProgress = 50;
        selectedPresentWay = "text";
        selectedGraphicStatus = selectedDigitStatus = (int) initialProgress;
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

            // TODO: send self_questionnaire

            final Intent intent = new Intent(EditProfilePage.this, ContactList.class);

            long currentTime = ScheduleAndSampleManager.getCurrentTimeInMillis();

            // TODO: send data to database
            JSONObject data = new JSONObject();
            try {
                data.put("id", sharedPreferences.getString("id", ""));
                data.put("presentWay", selectedPresentWay);
                data.put("createdTime", currentTime);

                if (selectedPresentWay.equals("text")) {
                    data.put("status", -1);
                    data.put("statusText", selectedTextStatus);
                    data.put("statusColor", -1);
                    sharedPreferences.edit()
                            .putString("way", "text")
                            .putInt("status", -1)
                            .putString("statusText", selectedTextStatus)
                            .putLong("updateTime", currentTime)
                            .putInt("statusColor", -1)
                            .commit();
                } else if (selectedPresentWay.equals("digit")) {
                    data.put("status", selectedDigitStatus);
                    data.put("statusText", "NA");
                    data.put("statusColor", -1);
                    sharedPreferences.edit()
                            .putString("way", "digit")
                            .putInt("status", selectedDigitStatus)
                            .putString("statusText", "NA")
                            .putLong("updateTime", currentTime)
                            .putInt("statusColor", -1)
                            .commit();
                } else if (selectedPresentWay.equals("graphic")) {
                    data.put("status", selectedGraphicStatus);
                    data.put("statusText", "NA");
                    data.put("statusColor", selectedColor);
                    sharedPreferences.edit()
                            .putString("way", "graphic")
                            .putInt("status", selectedGraphicStatus)
                            .putString("statusText", "NA")
                            .putLong("updateTime", currentTime)
                            .putInt("statusColor", selectedColor)
                            .commit();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            mQueue = Volley.newRequestQueue(mContext);
            mJsonObjectRequest = new JsonObjectRequest(Request.Method.POST, storeSelfStatusUrl, data,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(TAG, response.toString());
                            // TODO: start activity here
                            try {
                                if (response.getString("response").equals("success insert")) {
                                    startActivity(intent);
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
