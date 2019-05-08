package labelingStudy.nctu.minuku_2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonObject;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import labelingStudy.nctu.minuku.Utilities.ScheduleAndSampleManager;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku_2.Questionnaire.SelfQuestionnaire;

public class ContactList extends Activity {

    private static final String TAG = "ContactList";
    private static final String fetchDataUrl = "http://13.59.255.194:5000/getList";
    private static final String getContactStatusUrl = "http://13.59.255.194:5000/getContactStatus";
    private Context mContext;
    private IBinder mIBinder;
    private InputMethodManager mInputMethodManager;
    private RequestQueue mQueue;
    private JsonObjectRequest mJsonObjectRequest;
    private ArrayList<String> userList;
    private ArrayList<String> contactList;
    private String id;
    private SharedPreferences sharedPrefs;

    // collected data
    private String selectedContactPerson;
    private String selectedGoodTiming;
    private String selectedIsBother;
    private String selectedLocation;
    private String usingPurpose;
    private String communicatePurpose;

    // present way
    private String presentWay;
    private int selfStatus;
    private long updateTime;
    private int circleProgressColor;
    private TextView selfStatusText;
    private TextView selfStatusUpdateText;
    private CircularProgressBar selfStatusProgressBar;

    // contact list
    private ListView listView;
    private SimpleAdapter simpleAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    // each person: questionnaire
    private View cover;
    private ConstraintLayout questionnaire;
    private ConstraintLayout contactStatusLayout;
    private TextView contactStatusText;
    private CircularProgressBar contactStatusProgressBar;
    private TextView contactName;
    private TextView contactStatusUpdateText;
    private Button contactStatusOkButton;
    private ScrollView senderQuestionnaireScrollview;

    // Q1: 哪個可以判斷是否是個好時機
    private RadioGroup senderQuestion1;
    // Q2: 哪個可以判斷是否會打擾到他
    private RadioGroup senderQuestion2;
    // Q3: 地點
    private RadioGroup senderLocation1;
    private RadioButton home;
    private RadioButton dorm;
    private RadioButton lab;

    private RadioGroup senderLocation2;
    private RadioButton classroom;
    private RadioButton library;
    private RadioButton outdoor;

    private RadioGroup senderLocation3;
    private RadioButton transportation;
    private RadioButton mall;

    private RadioGroup senderLocation4;
    private RadioButton locationOther;
    private EditText locationOtherText;
    // Q4: 看對方的狀態的目的
    private RadioGroup radioGroupUsingPurpose;
    private EditText usingPurposeOtherText;
    // Q5: 溝通目的
    private RadioGroup radioGroupCommunicatePurpose;

    private Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_list);

        mContext = getApplicationContext();
        sharedPrefs = getSharedPreferences(Constants.sharedPrefString_User, MODE_PRIVATE);
        mInputMethodManager = (InputMethodManager) getSystemService(mContext.INPUT_METHOD_SERVICE);

        // TODO: 只需要做一次，但是app被滑掉的話，又會再做一次(activity重新產生又會再做)
        // notification
//        NotificationHelper.scheduleDailyNotification(mContext);
        ///////////////////////////////////////////////////////////////
//        NotificationHelper.scheduleRepeatingRTCNotification(mContext);
        /////////////////////////////////////////////////////////////////
        //

        // contact list
        listView = findViewById(R.id.contact_list);
        mSwipeRefreshLayout = findViewById(R.id.refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onStart();
//                onCreate(null);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        // self status
        selfStatusText = findViewById(R.id.contactList_self_status);
        selfStatusUpdateText = findViewById(R.id.contactList_profile_updateTime);
        selfStatusProgressBar = findViewById(R.id.contactList_progressCircle);
        Button editProfile = findViewById(R.id.edit_profile_button);
        editProfile.setOnClickListener(editProfileListener);

        // contact person status
        cover = findViewById(R.id.contactList_cover);
        questionnaire = findViewById(R.id.sender_questionnaire);
        contactStatusLayout = findViewById(R.id.contact_status);
        contactName = findViewById(R.id.contact_status_name);
        contactStatusText = findViewById(R.id.contact_status_rate);
        contactStatusProgressBar = findViewById(R.id.contact_status_circle);
        contactStatusUpdateText = findViewById(R.id.contact_status_updateTime);
        contactStatusOkButton = findViewById(R.id.contact_status_ok_button);
        contactStatusOkButton.setOnClickListener(contactStatusOkListener);
        senderQuestionnaireScrollview = findViewById(R.id.sender_questionnaire_scrollview);
        // Q1: 哪種能幫助你判斷「現在是否是個好時機」傳訊息給對方
        senderQuestion1 = findViewById(R.id.sender_question1);
        senderQuestion1.setOnCheckedChangeListener(new Q1Listener());
        // Q2: 哪種能幫助你判斷「現在傳訊息給對方是否會打擾到他」
        senderQuestion2 = findViewById(R.id.sender_question2);
        senderQuestion2.setOnCheckedChangeListener(new Q2Listener());
        // Q3: 地點是在哪裡
        senderLocation1 = findViewById(R.id.sender_location1);
        senderLocation2 = findViewById(R.id.sender_location2);
        senderLocation3 = findViewById(R.id.sender_location3);
        senderLocation4 = findViewById(R.id.sender_location4);
        home = findViewById(R.id.sender_home);
        dorm = findViewById(R.id.sender_dorm);
        lab = findViewById(R.id.sender_lab);
        classroom = findViewById(R.id.sender_classroom);
        library = findViewById(R.id.sender_library);
        outdoor = findViewById(R.id.sender_outdoor);
        transportation = findViewById(R.id.sender_transportation);
        mall = findViewById(R.id.sender_mall);
        locationOther = findViewById(R.id.sender_location_other);
        locationOtherText = findViewById(R.id.sender_location_other_text);
        locationOtherText.setOnEditorActionListener(new locationEditTextListener());

        senderLocation1.setOnCheckedChangeListener(new locationListener1());
        senderLocation2.setOnCheckedChangeListener(new locationListener2());
        senderLocation3.setOnCheckedChangeListener(new locationListener3());
        senderLocation4.setOnCheckedChangeListener(new locationListener4());
        // Q4: 請問你查看對方的狀態的目的是
        radioGroupUsingPurpose = findViewById(R.id.using_purpose);
        usingPurposeOtherText = findViewById(R.id.using_purpose_other_text);

        radioGroupUsingPurpose.setOnCheckedChangeListener(new usingPurposeListener());
        usingPurposeOtherText.setOnEditorActionListener(new usingPurposeEditTextListener());
        // Q5: 請問你的溝通目的是
        radioGroupCommunicatePurpose = findViewById(R.id.communication_purpose);
        radioGroupCommunicatePurpose.setOnCheckedChangeListener(new communicatePurposeListener());

        // submit button
        submit = findViewById(R.id.sender_questionnaire_submit);
        submit.setOnClickListener(submitListener);

        // buttons
//        Button statusExample = findViewById(R.id.status_example);
//        statusExample.setOnClickListener(statusExampleListener);
        Button questionnaire = findViewById(R.id.questionnaire);
        questionnaire.setOnClickListener(questionnaireListener);

        id = sharedPrefs.getString("id", "");

    }

    @Override
    protected void onStart() {
        Log.d(TAG, "!!!on start !!!");
        super.onStart();

        if (!id.equals("")) {
            presentWay = sharedPrefs.getString("way", "digit");
            updateTime = sharedPrefs.getLong("updateTime", 0);
            selfStatus = sharedPrefs.getInt("status", 0);

            renderStatus(selfStatusUpdateText, selfStatusText, selfStatusProgressBar,
                         updateTime, selfStatus, presentWay,
                         sharedPrefs.getString("statusText", ""),
                         sharedPrefs.getInt("statusColor", -1));
            getData();
        }
    }

    private void renderStatus(TextView updateTimeText, TextView statusText, CircularProgressBar circle,
                              long time, int status, String way, String textStatusText, int color) {

        updateTimeText.setText(ScheduleAndSampleManager.getMinSecStringFromMillis(time));

        if (way.equals("text")) {
            statusText.setText(textStatusText);
            statusText.setVisibility(View.VISIBLE);
            circle.setVisibility(View.INVISIBLE);
        } else if (way.equals("digit")) {
            statusText.setText("回覆率" + status + "%");
            statusText.setVisibility(View.VISIBLE);
            circle.setVisibility(View.INVISIBLE);
        } else if (way.equals("graphic")) {
//            int color = Color.rgb(51, 102, 153);
//            Log.d(TAG, "color: " + color);
            circle.setProgressWithAnimation(Float.valueOf(status), 1500);
            circle.setColor(color);
//            selfStatusProgressBar.setBackgroundColor(circleProgressBackgroundColor);
            statusText.setVisibility(View.INVISIBLE);
            circle.setVisibility(View.VISIBLE);
        }
    }

    private Button.OnClickListener questionnaireListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setClass(ContactList.this, SelfQuestionnaire.class);
            startActivity(intent);
        }
    };

    private Button.OnClickListener contactStatusOkListener = new Button.OnClickListener() {

        @Override
        public void onClick(View view) {
            contactStatusLayout.setVisibility(View.INVISIBLE);
            cover.setVisibility(View.INVISIBLE);
        }
    };

//    private Button.OnClickListener statusExampleListener = new Button.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            Intent intent = new Intent();
//            intent.setClass(ContactList.this, StatusExample.class);
//            startActivity(intent);
//        }
//    };

    private Button.OnClickListener editProfileListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setClass(ContactList.this, EditProfilePage.class);
            startActivity(intent);
        }
    };

    private void getData() {
        Log.d(TAG, "get data");
        userList = new ArrayList<>();
        JSONObject data = new JSONObject();
        try {
            data.put("id", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mQueue = Volley.newRequestQueue(mContext);
        // TODO: handle if no network...
        mJsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fetchDataUrl, data,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        try {
                            if (response.getString("response").equals("success")) {
                                JSONArray list = response.getJSONArray("list");
                                for (int i=0;i<list.length();i++) {
                                    JSONObject user = list.getJSONObject(i);
                                    String userId = user.getString("id");
                                    userList.add(userId);
                                }
                                renderList();

                            } else {
                                Log.d(TAG, "getList error");
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

    private void renderList() {
        contactList = new ArrayList<>();
        for (int i=0;i<userList.size();i++) {
            Log.d(TAG, userList.get(i));
        }
        List<Map<String, Object>> items = new ArrayList<>();
        for (int i=0;i<userList.size();i++) {
            if (!userList.get(i).equals(id)) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", userList.get(i));
                items.add(item);
                contactList.add(userList.get(i));
            }
        }
        simpleAdapter = new SimpleAdapter(mContext,
                                          items,
                                          R.layout.contact_list_row,
                                          new String[]{"id"},
                                          new int[]{R.id.contact_list_row_text});
        listView.setAdapter(simpleAdapter);
        listView.setOnItemClickListener(onClickListView);
    }

    private AdapterView.OnItemClickListener onClickListView = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            locationOtherText.setText("");
            locationOtherText.clearFocus();
            usingPurposeOtherText.setText("");
            usingPurposeOtherText.clearFocus();
            selectedContactPerson = contactList.get(position);

            // 問卷先隱藏
//            questionnaire.setVisibility(View.VISIBLE);
            // 問卷先隱藏

            // 對方的狀態
            String contact = contactList.get(position);
            contactStatusLayout.setVisibility(View.VISIBLE);
            contactName.setText(contact);
            JSONObject data = new JSONObject();
            try {
                data.put("id", contact);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mQueue = Volley.newRequestQueue(mContext);
            mJsonObjectRequest = new JsonObjectRequest(Request.Method.POST, getContactStatusUrl, data,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(TAG, response.toString());
                            try {
                                renderStatus(contactStatusUpdateText, contactStatusText, contactStatusProgressBar,
                                        response.getLong("createdTime"), response.getInt("status"),
                                        response.getString("presentWay"), response.getString("statusText"),
                                        response.getInt("statusColor"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, "error: " + error.getMessage());
                }
            });
            mQueue.add(mJsonObjectRequest);
            // 對方的狀態

            cover.setVisibility(View.VISIBLE);
            Log.d(TAG, "user: " + contactList.get(position));
        }
    };

    private Button.OnClickListener submitListener = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO: send to database
            Log.d(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            Log.d(TAG, "selectedContactPerson: " + selectedContactPerson);
            Log.d(TAG, "selectedGoodTiming: " + selectedGoodTiming);
            Log.d(TAG, "selectedIsBother: " + selectedIsBother);
            Log.d(TAG, "selectedLocation: " + selectedLocation);
            Log.d(TAG, "usingPurpose: " + usingPurpose);
            Log.d(TAG, "communicatePurpose: " + communicatePurpose);

            senderQuestion1.clearCheck();
            senderQuestion2.clearCheck();
            senderLocation1.clearCheck();
            senderLocation2.clearCheck();
            senderLocation3.clearCheck();
            senderLocation4.clearCheck();
            radioGroupUsingPurpose.clearCheck();
            radioGroupCommunicatePurpose.clearCheck();
            questionnaire.setVisibility(View.INVISIBLE);
            cover.setVisibility(View.INVISIBLE);
            senderQuestionnaireScrollview.fullScroll(senderQuestionnaireScrollview.FOCUS_UP);
        }
    };

    private class communicatePurposeListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.inform:
                    communicatePurpose = "inform";
                    break;
                case R.id.short_answer:
                    communicatePurpose = "short_answer";
                    break;
                case R.id.long_time:
                    communicatePurpose = "long_time";
                    break;
            }
        }
    }

    private class usingPurposeListener implements RadioGroup.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId == R.id.using_purpose_other) {
                usingPurposeOtherText.requestFocus();
                // show keyboard
                mInputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            } else {
                usingPurposeOtherText.clearFocus();
                mIBinder = ContactList.this.getCurrentFocus().getWindowToken();
                mInputMethodManager.hideSoftInputFromWindow(mIBinder, InputMethodManager.HIDE_NOT_ALWAYS);
                if (checkedId == R.id.communicate) {
                    usingPurpose = "communicate";
                } else if (checkedId == R.id.curious) {
                    usingPurpose = "curious";
                } else if (checkedId == R.id.boring) {
                    usingPurpose = "boring";
                }
            }
        }
    }

    private class usingPurposeEditTextListener implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            mIBinder = ContactList.this.getCurrentFocus().getWindowToken();
            mInputMethodManager.hideSoftInputFromWindow(mIBinder, InputMethodManager.HIDE_NOT_ALWAYS);
            usingPurpose = usingPurposeOtherText.getText().toString();
            usingPurposeOtherText.clearFocus();
            return false;
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
                    senderLocation2.clearCheck();
                    senderLocation3.clearCheck();
                    senderLocation4.clearCheck();
                }
            }
        }
    }

    private class locationListener2 implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                if (classroom.isChecked()) {
                    selectedLocation = "classroom";
                } else if (library.isChecked()) {
                    selectedLocation = "library";
                } else if (outdoor.isChecked()) {
                    selectedLocation = "outdoor";
                }
                if (classroom.isChecked() || library.isChecked() || outdoor.isChecked()) {
                    senderLocation1.clearCheck();
                    senderLocation3.clearCheck();
                    senderLocation4.clearCheck();
                    locationOtherText.clearFocus();
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
                    senderLocation1.clearCheck();
                    senderLocation2.clearCheck();
                    senderLocation4.clearCheck();
                }
            }
        }
    }

    private class locationListener4 implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.sender_location_other:
                    if (locationOther.isChecked()) {
                        locationOtherText.requestFocus();
                        // show keyboard
                        mInputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                        senderLocation1.clearCheck();
                        senderLocation2.clearCheck();
                        senderLocation3.clearCheck();
                    }
                    break;
            }
        }
    }

    private class locationEditTextListener implements EditText.OnEditorActionListener {
        @Override
        public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
            mIBinder = ContactList.this.getCurrentFocus().getWindowToken();
            mInputMethodManager.hideSoftInputFromWindow(mIBinder, InputMethodManager.HIDE_NOT_ALWAYS);
            selectedLocation = locationOtherText.getText().toString();
            locationOtherText.clearFocus();
            return false;
        }
    }

    private class Q1Listener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.sender_question1a:
                    // TODO: get data from database
                    // TODO: 看是哪兩種方法
                    selectedGoodTiming = "a";
                    break;
                case R.id.sender_question1b:
                    // TODO: get data from database
                    // TODO: 看是哪兩種方法
                    selectedGoodTiming = "b";
                    break;
                case R.id.sender_question1both:
                    // TODO: get data from database
                    // TODO: 看是哪兩種方法
                    selectedGoodTiming = "both";
                    break;
                case R.id.sender_question1neither:
                    // TODO: get data from database
                    // TODO: 看是哪兩種方法
                    selectedGoodTiming = "neither";
                    break;
            }
        }
    }

    private class Q2Listener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.sender_question2a:
                    // TODO: get data from database
                    // TODO: 看是哪兩種方法
                    selectedIsBother = "a";
                    break;
                case R.id.sender_question2b:
                    // TODO: get data from database
                    // TODO: 看是哪兩種方法
                    selectedIsBother = "b";
                    break;
                case R.id.sender_question2both:
                    // TODO: get data from database
                    // TODO: 看是哪兩種方法
                    selectedIsBother = "both";
                    break;
                case R.id.sender_question2neither:
                    // TODO: get data from database
                    // TODO: 看是哪兩種方法
                    selectedIsBother = "neither";
                    break;
            }
        }
    }
}