package labelingStudy.nctu.minuku_2;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

import static labelingStudy.nctu.minuku_2.manager.renderManager.randomStatusForm;
import static labelingStudy.nctu.minuku_2.manager.renderManager.rateGetText;
import static labelingStudy.nctu.minuku_2.manager.renderManager.statusGetRate;

public class EditProfilePage extends Activity {

    private static final String TAG = "EditProfilePage";
    private SharedPreferences sharedPreferences;
    private Context mContext;
    private RequestQueue mQueue;
    private JsonObjectRequest mJsonObjectRequest;
    private int afterEditNotificationID = 13;
    private int afterEditNotificationCode = 200;

    // Tabs
    private TabLayout mTabs;
    private ViewPager mViewPager;
    // present way
    private String selectedPresentWay;
    //
    // Status: text
    private String selectedTextStatus = "";
    private Spinner textSpinner;
    //
    // Status: digit & graphic
    private int selectedStatusRate;
    private String selectedStatusForm = "";
    private TextView editPage_digit_rate;
    private Spinner editPage_digit_form;
    //
    // Status: graphic
    private CircularProgressBar circularProgressBar;
    private Spinner editPage_graphic_form;
    private float initialProgress;
    private int selectedColor;
    //
    // time period
    private Spinner hourSpinner, minuteSpinner;
    private int hour, minute;
    //
    // done button
    private Button done;
    private Button cancel;

    private long currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);
        mContext = getApplicationContext();
        sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        initialProgress = 50;
        selectedPresentWay = "text";
        selectedStatusRate = (int) initialProgress;
        // choose present way
        mTabs = findViewById(R.id.tabs);
        mViewPager = findViewById(R.id.viewPager);
        mViewPager.setAdapter(new SimplePagerAdapter());
        mTabs.setupWithViewPager(mViewPager);
        initListener();

        // done button & cancel button
        done = findViewById(R.id.edit_profile_done);
        done.setOnClickListener(doneListener);
        cancel = findViewById(R.id.edit_profile_cancel);
        cancel.setOnClickListener(cancelListener);
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

            final Intent intent = new Intent(EditProfilePage.this, SelfEditQuestionnaire.class);

            currentTime = ScheduleAndSampleManager.getCurrentTimeInMillis();
            boolean pass = false;
            // store status to database
            JSONObject data = new JSONObject();

            // send data to notification intent
            Bundle bundle = new Bundle();
            bundle.putString("presentWay", selectedPresentWay);
            bundle.putLong("createdTime", currentTime);
            final int t = hour*60 + minute;
            bundle.putInt("timePeriod", t);
            Log.d(TAG, "time period: " + t);
            try {
                data.put("user_id", sharedPreferences.getString("id", "NA"));
                data.put("id", currentTime); // use Questionnaire to query dump data id(self edit data)
                data.put("group", sharedPreferences.getString("group", "NA"));
                data.put("createdTime", currentTime);
                data.put("createdTimeString", ScheduleAndSampleManager.getTimeString(currentTime));
                data.put("timePeriod", t);
                data.put("presentWay", selectedPresentWay);
                data.put("afterEdit", true);
                sharedPreferences.edit().putString("way", selectedPresentWay).commit();
                if (selectedPresentWay.equals("text") && !selectedTextStatus.equals("")) {
                    String randomForm = randomStatusForm();
                    data.put("statusForm", randomForm);
                    data.put("statusText", selectedTextStatus);
                    data.put("status", statusGetRate(selectedTextStatus));
                    data.put("statusColor", Constants.DEFAULT_COLOR);

                    bundle.putString("statusForm", randomForm);
                    bundle.putInt("status", statusGetRate(selectedTextStatus));
                    bundle.putString("statusText", selectedTextStatus);
                    bundle.putInt("statusColor", Constants.DEFAULT_COLOR);
                    // 改狀態
                    sharedPreferences.edit()
                            .putString("statusForm", randomForm)
                            .putInt("status", statusGetRate(selectedTextStatus))
                            .putString("statusText", selectedTextStatus)
                            .putLong("updateTime", currentTime)
                            .putInt("statusColor", -1)
                            .putBoolean("afterEdit", true)
                            .commit();
                    pass = true;
                } else {
                    if (selectedStatusForm.equals(Constants.STATUS_FORM_DISTURB)) {
                        selectedStatusRate = 100 - selectedStatusRate;
                    }
                    if (!selectedStatusForm.equals("")) {
                        data.put("statusForm", selectedStatusForm);
                        data.put("status", selectedStatusRate);
                        data.put("statusText", rateGetText(selectedStatusRate));

                        bundle.putString("statusForm", selectedStatusForm);
                        bundle.putInt("status", selectedStatusRate);
                        bundle.putString("statusText", rateGetText(selectedStatusRate));
                        if (selectedPresentWay.equals("digit")) {
                            data.put("statusColor", Constants.DEFAULT_COLOR);
                            bundle.putInt("statusColor", -1);
                        } else if (selectedPresentWay.equals("graphic")){
                            data.put("statusColor", selectedColor);
                            bundle.putInt("statusColor", selectedColor);
                        }
                        sharedPreferences.edit()
                                .putString("statusForm", selectedStatusForm)
                                .putInt("status", selectedStatusRate)
                                .putString("statusText", rateGetText(selectedStatusRate))
                                .putLong("updateTime", currentTime)
                                .putInt("statusColor", selectedColor)
                                .putBoolean("afterEdit", true)
                                .commit();
                        pass = true;
                    }
                }
                // contact list 傳來的當下狀態
                Bundle origin = getIntent().getExtras();
                String way = origin.getString("presentWay");
                String form = origin.getString("statusForm");
                long time = origin.getLong("updateTime");
                int s = origin.getInt("status");
                String text = origin.getString("statusText");
                int color = origin.getInt("statusColor");

//                bundle.putString("presentWayOrigin", way); //TODO 不需要傳這個過去了
//                bundle.putString("statusFormOrigin", form);
//                bundle.putLong("updateTimeOrigin", time);
//                bundle.putInt("statusOrigin", s);
//                bundle.putString("statusTextOrigin", text);
//                bundle.putInt("statusColorOrigin", color);

                data.put("OriginPresentWay", way);
                data.put("OriginStatusForm", form);
                data.put("OriginUpdateTime", time);
                data.put("OriginStatus", s);
                data.put("OriginStatusText", text);
                data.put("OriginStatusColor", color);

                if (pass) {
                    intent.putExtras(bundle);

                    // send notification (with questionnaire)
                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    String notificationText = ScheduleAndSampleManager.getMinSecStringFromMillis(currentTime) + " 您編輯了狀態\n請點選通知填寫問卷!";
                    Notification notification = getNotification(notificationText, bundle);
                    notification.defaults |= Notification.DEFAULT_VIBRATE;
                    mNotificationManager.notify(afterEditNotificationID, notification);

                    // store status data to database
                    mQueue = Volley.newRequestQueue(mContext);
                    mJsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Constants.storeSelfStatusUrl, data,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.d(TAG, response.toString());
                                    // start activity here
                                    try {
                                        if (response.getString("response").equals("success insert")) {
                                            BackgroundService.isHandleAfterEdit = true;
                                            BackgroundService.afterEditTimePeriod = t;
                                            Intent intent = new Intent(EditProfilePage.this, ContactList.class);
                                            Toast.makeText(EditProfilePage.this,
                                                    "更改成功! 請記得填寫問卷", Toast.LENGTH_SHORT).show();
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP); //加這行
                                            startActivity(intent);
                                            EditProfilePage.this.finish();
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
                    Toast.makeText(EditProfilePage.this,
                            "請確認您要呈現的狀態!", Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    };

    private Notification getNotification(String text, Bundle bundle) {
        Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle();
        bigTextStyle.setBigContentTitle(Constants.EDIT_CHANNEL_NAME);
        bigTextStyle.bigText(text);

        Intent resultIntent = new Intent();
        resultIntent.setComponent(new ComponentName(this,SelfEditQuestionnaire.class));
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        resultIntent.putExtras(bundle);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, afterEditNotificationCode,
                resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder noti = new Notification.Builder(this)
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.edit)
                .setStyle(bigTextStyle)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return noti
                    .setChannelId(Constants.SURVEY_CHANNEL_ID)
                    .build();
        } else {
            return noti
                    .build();
        }
    }

    private Button.OnClickListener cancelListener = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(EditProfilePage.this, ContactList.class);
            intent.putExtra("from", "EditProfilePage");

            startActivity(intent);
            EditProfilePage.this.finish();
        }
    };

    private class textStatusSpinnerListener implements android.widget.AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if (i == 0) {
                selectedTextStatus = "";
            } else {
                selectedTextStatus = textSpinner.getSelectedItem().toString();
                Log.d(TAG, "selectedTextStatus: " + selectedTextStatus);
            }
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

    private class digitFormSpinnerListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if (i == 0) {
                selectedStatusForm = "";
            } else {
                selectedStatusForm = adapterView.getItemAtPosition(i).toString();
                Log.d(TAG, "selectedDigitForm: " + selectedStatusForm);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    private class graphicFormSpinnerListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if (i == 0) {
                selectedStatusForm = "";
            } else {
                selectedStatusForm = adapterView.getItemAtPosition(i).toString();
                Log.d(TAG, "selectedGraphicForm: " + selectedStatusForm);
            }
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
            editPage_graphic_form = view.findViewById(R.id.editPage_form_graphic);
            ArrayAdapter<CharSequence> graphicFormLunchList = ArrayAdapter.createFromResource(EditProfilePage.this,
                    R.array.editPage_showContactForm_withhint,
                    android.R.layout.simple_spinner_dropdown_item);
            editPage_graphic_form.setAdapter(graphicFormLunchList);
            editPage_graphic_form.setOnItemSelectedListener(new graphicFormSpinnerListener());

            circularProgressBar = view.findViewById(R.id.progressCircle);
            circularProgressBar.setProgress(initialProgress);
            circularProgressBar.setColor(Constants.DEFAULT_COLOR);
            selectedColor = Constants.DEFAULT_COLOR;

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
                    selectedStatusRate = seekBar.getProgress();
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
            editPage_digit_form = view.findViewById(R.id.editPage_form_digit);
            ArrayAdapter<CharSequence> digitFormLunchList = ArrayAdapter.createFromResource(EditProfilePage.this,
                    R.array.editPage_showContactForm_withhint,
                    android.R.layout.simple_spinner_dropdown_item);
            editPage_digit_form.setAdapter(digitFormLunchList);
            editPage_digit_form.setOnItemSelectedListener(new digitFormSpinnerListener());

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
                    selectedStatusRate = seekBar.getProgress();
                }
            });
        }

        private void initTextTab(View view) {

            textSpinner = view.findViewById(R.id.editPage_textSpinner);
            ArrayAdapter<CharSequence> textLunchList = ArrayAdapter.createFromResource(EditProfilePage.this,
                    R.array.textStatus_withhint,
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
