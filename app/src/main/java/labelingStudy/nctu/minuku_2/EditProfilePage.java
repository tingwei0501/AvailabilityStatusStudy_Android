package labelingStudy.nctu.minuku_2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

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
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku_2.Questionnaire.SelfEditQuestionnaire;
import labelingStudy.nctu.minuku_2.service.BackgroundService;

import static labelingStudy.nctu.minuku_2.manager.renderManager.statusGetRate;
import static labelingStudy.nctu.minuku_2.manager.renderManager.statusGetText;

public class EditProfilePage extends Activity {

    private static final String TAG = "EditProfilePage";
    private SharedPreferences sharedPreferences;
    private Context mContext;
    private RequestQueue mQueue;
    private JsonObjectRequest mJsonObjectRequest;

    // Tabs
    private TabLayout mTabs;
    private ViewPager mViewPager;
    // present way
    private RadioGroup radioGroupPresentWay;
    private String selectedPresentWay;
    //
    // Status: text
    private ConstraintLayout constraintLayoutText;
    private String selectedTextStatus;
    private Spinner textSpinner;
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
    // time period
    private Spinner hourSpinner, minuteSpinner;
    private int hour, minute;
//    private int timePeriod;
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
        mTabs = findViewById(R.id.tabs);
        mViewPager = findViewById(R.id.viewPager);
        mViewPager.setAdapter(new SimplePagerAdapter());
        mTabs.setupWithViewPager(mViewPager);
        initListener();

//        radioGroupPresentWay = findViewById(R.id.editPage_presentWay);
//        radioGroupPresentWay.setOnCheckedChangeListener(new presentWayListener());

        // done button & cancel button
        done = findViewById(R.id.edit_profile_done);
        done.setOnClickListener(doneListener);
        cancel = findViewById(R.id.edit_profile_cancel);
        cancel.setOnClickListener(cancelListener);

//        // time selector
//        hourSpinner = findViewById(R.id.hour_spinner);
//        ArrayAdapter<CharSequence> hourLunchList = ArrayAdapter.createFromResource(EditProfilePage.this,
//                R.array.hour,
//                android.R.layout.simple_spinner_dropdown_item);
//        hourSpinner.setAdapter(hourLunchList);
//        hourSpinner.setOnItemSelectedListener(new hourSpinnerListener());
//
//        minuteSpinner = findViewById(R.id.min_spinner);
//        ArrayAdapter<CharSequence> minLunchList = ArrayAdapter.createFromResource(EditProfilePage.this,
//                R.array.minute,
//                android.R.layout.simple_spinner_dropdown_item);
//        minuteSpinner.setAdapter(minLunchList);
//        minuteSpinner.setOnItemSelectedListener(new minuteSpinnerListener());

        // text:
//        constraintLayoutText = findViewById(R.id.constraintLayoutText);
//        textSpinner = findViewById(R.id.editPage_textSpinner);
//        ArrayAdapter<CharSequence> textLunchList = ArrayAdapter.createFromResource(EditProfilePage.this,
//                R.array.textStatus,
//                android.R.layout.simple_spinner_dropdown_item);
//        textSpinner.setAdapter(textLunchList);
//        textSpinner.setOnItemSelectedListener(new textStatusSpinnerListener());

        // digit: progress seek bar
//        constraintLayoutDigit = findViewById(R.id.constraintLayoutDigit);
//        editPage_digit_rate = findViewById(R.id.editPage_digit_rate);
//        editPage_digit_rate.setText(String.valueOf(initialProgress));
//        IndicatorSeekBar digitSeekBar = findViewById(R.id.seekBarProgressDigit);
//        digitSeekBar.setProgress((int) initialProgress);
//        digitSeekBar.setOnSeekChangeListener(new OnSeekChangeListener() {
//            @Override
//            public void onSeeking(SeekParams seekParams) {
//                Log.d(TAG, "test: " + seekParams.progress);
//                editPage_digit_rate.setText(String.valueOf(seekParams.progress));
//            }
//
//            @Override
//            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {}
//
//            @Override
//            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
//                Log.d(TAG, "progress: " + seekBar.getProgress());
//                selectedDigitStatus = seekBar.getProgress();
//            }
//        });

        // graphic: progress circle
//        constraintLayoutGraphic = findViewById(R.id.constraintLayoutGraphic);
//        circularProgressBar = findViewById(R.id.progressCircle);
//        circularProgressBar.setProgress(initialProgress);

        // graphic: progress seek bar
//        SeekBar graphicSeekBar = findViewById(R.id.seekBarProgressGraphic);
//        graphicSeekBar.setProgress((int) initialProgress);
//        graphicSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                circularProgressBar.setProgress(progress);
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {}
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                Log.d(TAG, "progress: " + seekBar.getProgress());
//                selectedGraphicStatus = seekBar.getProgress();
//            }
//        });
//
//        ((LobsterShadeSlider) findViewById(R.id.shadeslider)).addOnColorListener(new OnColorListener() {
//            @Override
//            public void onColorChanged(int color) {
//                circularProgressBar.setColor(color);
//////                circularProgressBar.setBackgroundColor(adjustAlpha(color, 0.3f));
//            }
//
//            @Override
//            public void onColorSelected(int color) {
//                Log.d(TAG, "selected color: " + color);
//                selectedColor = color;
////                selectedBackgroundColor = adjustAlpha(color, 0.3f);
////                Log.d(TAG, "selectedBackgroundColor: " + selectedBackgroundColor);
////                switch (color) {
////                    case -7617718:
////                        selectedColor = "green";
////                        break;
////                    case -16728876:
////                        selectedColor = "lightBlue";
////                        break;
////                    case -5317:
////                        selectedColor = "yellow";
////                        break;
////                    case -2937298:
////                        selectedColor = "red";
////                        break;
////                    case -10011977:
////                        selectedColor = "purple";
////                        break;
////                    case -12627531:
////                        selectedColor = "darkBlue";
////                        break;
////                }
//            }
//        });
    }

    private void initListener() {
        mTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                Log.d(TAG, "tab position: " + position);
                if (position == 0) {
                    selectedPresentWay = Constants.PRESENT_IN_TEXT;
                    Log.d(TAG, "in text");
                } else if (position == 1) {
                    selectedPresentWay = Constants.PRESENT_IN_DIGIT;
                    Log.d(TAG, "in digit");
                } else if (position == 2) {
                    selectedPresentWay = Constants.PRESENT_IN_GRAPHIC;
                    Log.d(TAG, "in graphic");
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private Button.OnClickListener doneListener = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {

            // send self_questionnaire

            final Intent intent = new Intent(EditProfilePage.this, SelfEditQuestionnaire.class);

            long currentTime = ScheduleAndSampleManager.getCurrentTimeInMillis();

            // send data to questionnaire activity
            Bundle bundle = new Bundle();
            bundle.putString("presentWay", selectedPresentWay);
            bundle.putLong("createdTime", currentTime);
            int t = hour*60 + minute;
            bundle.putInt("timePeriod", t);
            Log.d(TAG, "time period: " + t);

            if (selectedPresentWay.equals("text")) {
                bundle.putInt("status", statusGetRate(selectedTextStatus));
                bundle.putString("statusText", selectedTextStatus);
                bundle.putInt("statusColor", -1);
            } else if (selectedPresentWay.equals("digit")) {
                bundle.putInt("status", selectedDigitStatus);
                bundle.putString("statusText", statusGetText(selectedDigitStatus));
                bundle.putInt("statusColor", -1);
            } else if (selectedPresentWay.equals("graphic")){
                bundle.putInt("status", selectedGraphicStatus);
                bundle.putString("statusText", statusGetText(selectedGraphicStatus));
                bundle.putInt("statusColor", selectedColor);
            }
            intent.putExtras(bundle);
            startActivity(intent);
//            EditProfilePage.this.finish(); //

//            mQueue = Volley.newRequestQueue(mContext);
//            mJsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Constants.storeSelfStatusUrl, data,
//                    new Response.Listener<JSONObject>() {
//                        @Override
//                        public void onResponse(JSONObject response) {
//                            Log.d(TAG, response.toString());
//                            // start activity here
//                            try {
//                                if (response.getString("response").equals("success insert")) {
//                                    startActivity(intent);
//                                }
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//
//                        }
//                    }, new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError error) {
//                    Log.d(TAG, "Error: " + error.getMessage());
//                    Toast.makeText(EditProfilePage.this,
//                            "請檢察網路連線!", Toast.LENGTH_SHORT).show();
//                }
//            });
//            mQueue.add(mJsonObjectRequest);

        }
    };

    private Button.OnClickListener cancelListener = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(EditProfilePage.this, ContactList.class);
            intent.putExtra("from", "EditProfilePage");

            startActivity(intent);
            EditProfilePage.this.finish();
        }
    };

    private int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

//    private class presentWayListener implements RadioGroup.OnCheckedChangeListener {
//        @Override
//        public void onCheckedChanged(RadioGroup group, int checkedId) {
//            switch (checkedId) {
//                case R.id.present_text:
//                    selectedPresentWay = "text";
//                    constraintLayoutText.setVisibility(View.VISIBLE);
//                    constraintLayoutDigit.setVisibility(View.INVISIBLE);
//                    constraintLayoutGraphic.setVisibility(View.INVISIBLE);
//                    break;
//                case R.id.present_digit:
//                    selectedPresentWay = "digit";
//                    constraintLayoutText.setVisibility(View.INVISIBLE);
//                    constraintLayoutDigit.setVisibility(View.VISIBLE);
//                    constraintLayoutGraphic.setVisibility(View.INVISIBLE);
//                    break;
//                case R.id.present_graphic:
//                    selectedPresentWay = "graphic";
//                    constraintLayoutText.setVisibility(View.INVISIBLE);
//                    constraintLayoutDigit.setVisibility(View.INVISIBLE);
//                    constraintLayoutGraphic.setVisibility(View.VISIBLE);
//                    break;
//            }
//        }
//    }

    private class textStatusSpinnerListener implements android.widget.AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            selectedTextStatus = textSpinner.getSelectedItem().toString();
            Log.d(TAG, "selectedTextStatus: " + selectedTextStatus);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
//            Log.d(TAG, "selected: " + textSpinner.getSelectedItem().toString())
        }
    }

    private class hourSpinnerListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//            Log.d(TAG, "i: " + i);
            hour = i;
            Log.d(TAG, "selected hour: " + hour);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    private class minuteSpinnerListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            // 0 5 10 15 20 25 30
            // 0 1 2  3  4  5  6
            minute = i*5;
            Log.d(TAG, "selected minute: " + minute);
//            Log.d(TAG, "i: " + i);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    private class SimplePagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return o == view;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            View view;
            if (position == 0) {
                view = getLayoutInflater().inflate(R.layout.edit_page_text,
                        container, false);
                initTextTab(view);

            } else if (position == 1) {
                view = getLayoutInflater().inflate(R.layout.edit_page_digit,
                        container, false);
                initDigitTab(view);
            } else {
                view = getLayoutInflater().inflate(R.layout.edit_page_graphic,
                        container, false);
                initGraphicTab(view);
            }

            initTimeSpinner(view);
            container.addView(view);

            return view;

        }

        private void initGraphicTab(View view) {
            circularProgressBar = view.findViewById(R.id.progressCircle);
            circularProgressBar.setProgress(initialProgress);

            SeekBar graphicSeekBar = view.findViewById(R.id.seekBarProgressGraphic);
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

            ((LobsterShadeSlider) view.findViewById(R.id.shadeslider)).addOnColorListener(new OnColorListener() {
                @Override
                public void onColorChanged(int color) {
                    circularProgressBar.setColor(color);
                }

                @Override
                public void onColorSelected(int color) {
                    Log.d(TAG, "selected color: " + color);
                    selectedColor = color;
                }
            });
        }

        private void initDigitTab(View view) {
            editPage_digit_rate = view.findViewById(R.id.editPage_digit_rate);
            editPage_digit_rate.setText(String.valueOf(initialProgress));
            IndicatorSeekBar digitSeekBar = view.findViewById(R.id.seekBarProgressDigit);
            digitSeekBar.setProgress((int) initialProgress);
            digitSeekBar.setOnSeekChangeListener(new OnSeekChangeListener() {
                @Override
                public void onSeeking(SeekParams seekParams) {
                    Log.d(TAG, "test: " + seekParams.progress);
                    editPage_digit_rate.setText(String.valueOf(seekParams.progress));
                }

                @Override
                public void onStartTrackingTouch(IndicatorSeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                    Log.d(TAG, "progress: " + seekBar.getProgress());
                    selectedDigitStatus = seekBar.getProgress();
                }
            });
//            initTimeSpinner(view);
        }



        private void initTextTab(View view) {
            // time selector
//            hourSpinner = view.findViewById(R.id.hour_spinner);
//            ArrayAdapter<CharSequence> hourLunchList = ArrayAdapter.createFromResource(EditProfilePage.this,
//                    R.array.hour,
//                    android.R.layout.simple_spinner_dropdown_item);
//            hourSpinner.setAdapter(hourLunchList);
//            hourSpinner.setOnItemSelectedListener(new hourSpinnerListener());
//
//            minuteSpinner = view.findViewById(R.id.min_spinner);
//            ArrayAdapter<CharSequence> minLunchList = ArrayAdapter.createFromResource(EditProfilePage.this,
//                    R.array.minute,
//                    android.R.layout.simple_spinner_dropdown_item);
//            minuteSpinner.setAdapter(minLunchList);
//            minuteSpinner.setOnItemSelectedListener(new minuteSpinnerListener());

            textSpinner = view.findViewById(R.id.editPage_textSpinner);
            ArrayAdapter<CharSequence> textLunchList = ArrayAdapter.createFromResource(EditProfilePage.this,
                    R.array.textStatus,
                    android.R.layout.simple_spinner_dropdown_item);
            textSpinner.setAdapter(textLunchList);
            textSpinner.setOnItemSelectedListener(new textStatusSpinnerListener());
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String[] arr = {"文字", "數字", "圖像"};
            return arr[position];
        }

        private void initTimeSpinner(View view) {
            hourSpinner = view.findViewById(R.id.hour_spinner);
            ArrayAdapter<CharSequence> hourLunchList = ArrayAdapter.createFromResource(EditProfilePage.this,
                    R.array.hour,
                    android.R.layout.simple_spinner_dropdown_item);
            hourSpinner.setAdapter(hourLunchList);
            hourSpinner.setOnItemSelectedListener(new hourSpinnerListener());

            minuteSpinner = view.findViewById(R.id.min_spinner);
            ArrayAdapter<CharSequence> minLunchList = ArrayAdapter.createFromResource(EditProfilePage.this,
                    R.array.minute,
                    android.R.layout.simple_spinner_dropdown_item);
            minuteSpinner.setAdapter(minLunchList);
            minuteSpinner.setOnItemSelectedListener(new minuteSpinnerListener());
        }
    }
}
