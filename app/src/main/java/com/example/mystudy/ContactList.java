package com.example.mystudy;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class ContactList extends Activity {

    private static final String TAG = "ContactList";
    private static final String fetchDataUrl = "http://13.59.255.194:5000/getList";
    private Context mContext;
    private RequestQueue mQueue;
    private JsonObjectRequest mJsonObjectRequest;
    private ArrayList<String> userList;
    private String id;

    // present way
    private String presentWay;
    private String selfStatus;
    private int circleProgressColor;
//    private int circleProgressBackgroundColor;
    private TextView selfStatusText;
    private CircularProgressBar selfStatusProgressBar;
    //
    // contact list
    private ListView listView;
    private SimpleAdapter simpleAdapter;
    //
    // each person
    private View cover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_list);

        mContext = getApplicationContext();

        // TODO: 只需要做一次，但是app被滑掉的話，又會再做一次(activity重新產生又會再做)
        // notification
//        NotificationHelper.scheduleDailyNotification(mContext);
        NotificationHelper.scheduleRepeatingRTCNotification(mContext);
        //
        // contact list
        cover = findViewById(R.id.contactList_cover);
        listView = findViewById(R.id.contact_list);

        // self status
        selfStatusText = findViewById(R.id.contactList_self_status);
        selfStatusProgressBar = findViewById(R.id.contactList_progressCircle);
        // buttons
        Button editProfile = findViewById(R.id.edit_profile_button);
        editProfile.setOnClickListener(editProfileListener);
        Button statusExample = findViewById(R.id.status_example);
        statusExample.setOnClickListener(statusExampleListener);
        Button questionnaire = findViewById(R.id.questionnaire);
        questionnaire.setOnClickListener(questionnaireListener);

        SharedPreferences preferences = getSharedPreferences("user", MODE_PRIVATE);
        id = preferences.getString("id", "");
        // already login
        if (!id.equals("")) {
            presentWay = preferences.getString("way", "text");
            switch (presentWay) {
                case "text":
                    selfStatus = preferences.getString("status", "0");
                    Log.d(TAG, "selfStatus: " + selfStatus);
                    break;
                case "digit":
                    selfStatus = preferences.getString("status", "0");
                    Log.d(TAG, "selfStatus: " + selfStatus);
                    break;
                case "graphic":
                    selfStatus = preferences.getString("status", "0");
                    Log.d(TAG, "selfStatus: " + selfStatus);
                    circleProgressColor = preferences.getInt("color", -7617718);
                    Log.d(TAG, "circleProgressColor: " + circleProgressColor);
//                    circleProgressBackgroundColor = preferences.getInt("backgroundColor", 0);
//                    Log.d(TAG, "circleProgressBackgroundColor: " + circleProgressBackgroundColor);
                    break;
            }
                renderSelfStatus();
        }

        getData();
    }

    private void renderSelfStatus() {
        if (presentWay.equals("text")) {
            selfStatusText.setText(selfStatus);
            selfStatusText.setVisibility(View.VISIBLE);
            selfStatusProgressBar.setVisibility(View.INVISIBLE);
        } else if (presentWay.equals("digit")) {
            selfStatusText.setText("回覆率" + selfStatus + "%");
            selfStatusText.setVisibility(View.VISIBLE);
            selfStatusProgressBar.setVisibility(View.INVISIBLE);
        } else if (presentWay.equals("graphic")) {
            selfStatusProgressBar.setProgressWithAnimation(Float.valueOf(selfStatus), 1500);
            selfStatusProgressBar.setColor(circleProgressColor);
//            selfStatusProgressBar.setBackgroundColor(circleProgressBackgroundColor);
            selfStatusText.setVisibility(View.INVISIBLE);
            selfStatusProgressBar.setVisibility(View.VISIBLE);
        }
    }

    private Button.OnClickListener questionnaireListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setClass(ContactList.this, Questionnaire.class);
            startActivity(intent);
        }
    };

    private Button.OnClickListener statusExampleListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setClass(ContactList.this, StatusExample.class);
            startActivity(intent);
        }
    };

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
        for (int i=0;i<userList.size();i++) {
            Log.d(TAG, userList.get(i));
        }
        List<Map<String, Object>> items = new ArrayList<>();
        for (int i=0;i<userList.size();i++) {
            if (!userList.get(i).equals(id)) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", userList.get(i));
                items.add(item);
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
            Log.d(TAG, "press"+position);

            cover.setVisibility(View.VISIBLE);
            Log.d(TAG, "user: " + userList.get(position));
        }
    };
}
