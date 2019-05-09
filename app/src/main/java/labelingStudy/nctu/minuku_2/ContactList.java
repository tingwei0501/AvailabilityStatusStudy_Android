package labelingStudy.nctu.minuku_2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.Toast;

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
import labelingStudy.nctu.minuku_2.Questionnaire.ContactStatusQuestionnaire;
import labelingStudy.nctu.minuku_2.Questionnaire.SelfQuestionnaire;

public class ContactList extends Activity {

    private static final String TAG = "ContactList";
    private static final String fetchDataUrl = "http://13.59.255.194:5000/getList";
    private static final String getContactStatusUrl = "http://13.59.255.194:5000/getContactStatus";
    private Context mContext;

    private RequestQueue mQueue;
    private JsonObjectRequest mJsonObjectRequest;
    private ArrayList<String> userList;
    private ArrayList<String> contactList;
    private String id;
    private SharedPreferences sharedPrefs;


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

    // each person: self_questionnaire
    private View cover;
//    private ConstraintLayout questionnaire;
    private ConstraintLayout contactStatusLayout;
    private TextView contactStatusText;
    private CircularProgressBar contactStatusProgressBar;
    private TextView contactName;
    private TextView contactStatusUpdateText;
    private Button contactStatusOkButton;
    private Button contactStatusCancelButton;

    public static FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_list);

        initFab();
        mContext = getApplicationContext();
        sharedPrefs = getSharedPreferences(Constants.sharedPrefString_User, MODE_PRIVATE);

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
        contactStatusLayout = findViewById(R.id.contact_status);
        contactName = findViewById(R.id.contact_status_name);
        contactStatusText = findViewById(R.id.contact_status_rate);
        contactStatusProgressBar = findViewById(R.id.contact_status_circle);
        contactStatusUpdateText = findViewById(R.id.contact_status_updateTime);
        contactStatusOkButton = findViewById(R.id.contact_status_ok_button);
        contactStatusOkButton.setOnClickListener(contactStatusOkListener);
        contactStatusCancelButton = findViewById(R.id.contact_status_cancel_button);
        contactStatusCancelButton.setOnClickListener(contactStatusCancleListener);

        // buttons
//        Button statusExample = findViewById(R.id.status_example);
//        statusExample.setOnClickListener(statusExampleListener);
        Button questionnaire = findViewById(R.id.questionnaire);
        questionnaire.setOnClickListener(questionnaireListener);

        id = sharedPrefs.getString("id", "");

    }

    private void initFab() {
        mFab = findViewById(R.id.floatingActionButton);
        mFab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ContactList.this, ContactStatusQuestionnaire.class);
                startActivity(intent);
//                try {
//                    Thread.sleep(1000);
//                    Toast.makeText(ContactList.this, "FAB Clicked", Toast.LENGTH_SHORT).show();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
        });
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
            Intent intent = new Intent(ContactList.this, ContactStatusQuestionnaire.class);
            startActivity(intent);
        }
    };

    private Button.OnClickListener contactStatusCancleListener = new Button.OnClickListener() {

        @Override
        public void onClick(View view) {
            contactStatusLayout.setVisibility(View.INVISIBLE);
            cover.setVisibility(View.INVISIBLE);
            // TODO: 跳出問卷按鈕
            try {
                Thread.sleep(1000);
                mFab.setVisibility(View.VISIBLE);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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

//            locationOtherText.setText("");
//            locationOtherText.clearFocus();
//            usingPurposeOtherText.setText("");
//            usingPurposeOtherText.clearFocus();
//            selectedContactPerson = contactList.get(position);

            // 問卷先隱藏
//            self_questionnaire.setVisibility(View.VISIBLE);
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

}
