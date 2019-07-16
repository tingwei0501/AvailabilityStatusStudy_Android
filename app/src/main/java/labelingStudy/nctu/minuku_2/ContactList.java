package labelingStudy.nctu.minuku_2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import labelingStudy.nctu.minuku.Utilities.ScheduleAndSampleManager;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku_2.Questionnaire.ContactStatusQuestionnaire;
import labelingStudy.nctu.minuku_2.Questionnaire.SelfQuestionnaire;

import static labelingStudy.nctu.minuku_2.manager.renderManager.renderStatus;

public class ContactList extends Activity {

    private static final String TAG = "ContactList";
    private Context mContext;

    private RequestQueue mQueue;
    private JsonObjectRequest mJsonObjectRequest;
    private ArrayList<String> userList;
    private ArrayList<String> contactList;
    private String id;
    private SharedPreferences sharedPrefs;
    private Button editProfile;
    // TODO for partial only: Main需註解
    private Switch openNotification;

    // present way
    private String presentWay;
    private String statusForm;
    private int selfStatus;
    private long updateTime;
    private String selfStatusString;
    private int statusColor;
    private TextView selfStatusFormTextView;
    private TextView selfStatusText;
    private TextView selfStatusUpdateText;
    private CircularProgressBar selfStatusProgressBar;

    // contact list
    private ListView listView;
    private SimpleAdapter simpleAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    // each person: self_questionnaire
    private View cover;
    private ConstraintLayout contactStatusLayout;
    private Button contactStatusClose;
    private TextView contactStatusText;
    private TextView contactStatusFormText;
    private CircularProgressBar contactStatusProgressBar;
    private TextView contactName;
    private TextView contactStatusUpdateText;
    // contact information
    private String contactId;
    private long checkContactStatusTime;
    private String contactStatusString;
    private String contactStatusWay;
    private String contactStatusForm;
    private int contactStatusRate;
    private int contactStatusColor;

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
        selfStatusFormTextView = findViewById(R.id.contactList_statusForm);

        editProfile = findViewById(R.id.edit_profile_button);
        editProfile.setOnClickListener(editProfileListener);

        // contact person status
        cover = findViewById(R.id.contactList_cover);
        contactStatusLayout = findViewById(R.id.contact_status);

        contactStatusClose = findViewById(R.id.contact_status_close);
        contactStatusClose.setOnClickListener(contactStatusCloseListener);

        contactName = findViewById(R.id.contact_status_name);
        contactStatusFormText = findViewById(R.id.contact_status_statusForm);
        contactStatusText = findViewById(R.id.contact_status_rate);
        contactStatusProgressBar = findViewById(R.id.contact_status_circle);
        contactStatusUpdateText = findViewById(R.id.contact_status_updateTime);

        contactStatusOkButton = findViewById(R.id.contact_status_ok_button);
        contactStatusOkButton.setOnClickListener(contactStatusOkListener);

        contactStatusCancelButton = findViewById(R.id.contact_status_cancel_button);
        contactStatusCancelButton.setOnClickListener(contactStatusCancelListener);

        // TODO: for partial subject only: Main需註解 //////////////
//        openNotification = findViewById(R.id.openNotificationSwitch);
//        openNotification.setChecked(sharedPrefs.getBoolean("openNotification", false));
//        openNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                if (b) {
//                    Log.d(TAG, "true");
//                    sharedPrefs.edit()
//                            .putBoolean("openNotification", true)
//                            .commit();
//                } else {
//                    Log.d(TAG, "false");
//                    sharedPrefs.edit()
//                            .putBoolean("openNotification", false)
//                            .commit();
//                }
//            }
//        });
        ////////////////////////////////////////////////////////

        id = sharedPrefs.getString("id", "");

    }

    private void initFab() {
        mFab = findViewById(R.id.floatingActionButton);
        mFab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                sendContactDataToQuestionnaire();
            }
        });
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "!!!on start !!!");
        super.onStart();

        if (!id.equals("")) {
            presentWay = sharedPrefs.getString("way", Constants.PRESENT_IN_GRAPHIC);
            statusForm = sharedPrefs.getString("statusForm", Constants.STATUS_FORM_REPLY);
            Log.d(TAG, "statusForm: " + statusForm);

            updateTime = sharedPrefs.getLong("updateTime", 0);
            selfStatus = sharedPrefs.getInt("status", 0);
            selfStatusString = sharedPrefs.getString("statusText", "");
            statusColor = sharedPrefs.getInt("statusColor", -1);

            selfStatusUpdateText.setText(ScheduleAndSampleManager.getMinSecStringFromMillis(updateTime));

            // to be checked
            // TODO: 630 改
            if (presentWay.equals(Constants.PRESENT_IN_TEXT)) {
                selfStatusFormTextView.setVisibility(View.GONE);
            } else {
                selfStatusFormTextView.setVisibility(View.VISIBLE);
            }

            renderStatus(selfStatusText, selfStatusProgressBar, selfStatusFormTextView,
                         statusForm, selfStatus, presentWay,
                         selfStatusString, statusColor);
            getData();
        }
    }

    private Button.OnClickListener contactStatusCloseListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            ContactStatusLayout(true, View.INVISIBLE);
        }
    };

//    private Button.OnClickListener questionnaireListener = new Button.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            Intent intent = new Intent();
//            intent.setClass(ContactList.this, SelfQuestionnaire.class);
//            startActivity(intent);
//        }
//    };

    // 去填寫問卷按鈕
    private Button.OnClickListener contactStatusOkListener = new Button.OnClickListener() {

        @Override
        public void onClick(View view) {
            ContactStatusLayout(true, View.INVISIBLE);
            sendContactDataToQuestionnaire();
        }
    };

    private Button.OnClickListener contactStatusCancelListener = new Button.OnClickListener() {

        @Override
        public void onClick(View view) {
            ContactStatusLayout(true, View.INVISIBLE);
            // 跳出問卷按鈕
            try {
                Thread.sleep(1000);
                mFab.setVisibility(View.VISIBLE);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    private void sendContactDataToQuestionnaire() {
        Intent intent = new Intent(ContactList.this, ContactStatusQuestionnaire.class);
        Bundle bundle = new Bundle();
        bundle.putString("id", contactId);
        bundle.putInt("status", contactStatusRate);
        bundle.putLong("checkTime", checkContactStatusTime);
        bundle.putString("way", contactStatusWay);
        bundle.putString("statusForm", contactStatusForm);
        bundle.putInt("color", contactStatusColor);
        bundle.putString("statusString", contactStatusString);

        intent.putExtras(bundle);
        startActivity(intent);
//        ContactList.this.finish();
    }

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
            // save current status
            Bundle bundle = new Bundle();
            bundle.putString("presentWay", presentWay);
            bundle.putString("statusForm", statusForm);
            bundle.putLong("updateTime", updateTime);
            bundle.putInt("status", selfStatus);
            bundle.putString("statusText", selfStatusString);
            bundle.putInt("statusColor", statusColor);

            intent.putExtras(bundle);
            startActivity(intent);
//            ContactList.this.finish();
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
        // handle if no network...
        mJsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Constants.fetchDataUrl, data,
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
        // 去除自己
        List<Map<String, Object>> items = new ArrayList<>();
        for (int i=0;i<userList.size();i++) {
            if (!userList.get(i).equals(id)) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", userList.get(i));
                items.add(item);
                contactList.add(userList.get(i));
            }
        }
        // 存進 sharedPreference
        SharedPreferences.Editor contact = getSharedPreferences(Constants.sharedPrefString_ContactList, MODE_PRIVATE).edit();
        contact.putInt("contactLength", contactList.size());
        for (int i=0;i<contactList.size();i++) {
            Log.d(TAG, "contact: " + contactList.get(i));
            contact.putString("contact_"+i, contactList.get(i));
        }
        contact.commit();

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

            // 對方的狀態
            ContactStatusLayout(false, View.VISIBLE);

            contactId = contactList.get(position);
            checkContactStatusTime = ScheduleAndSampleManager.getCurrentTimeInMillis();
            contactName.setText(contactId);
            JSONObject data = new JSONObject();
            try {
                data.put("id", contactId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mQueue = Volley.newRequestQueue(mContext);
            mJsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Constants.getContactStatusUrl, data,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(TAG, response.toString());
                            try {
                                contactStatusRate = response.getInt("status");
                                contactStatusForm = response.getString("statusForm");
                                contactStatusString = response.getString("statusText");
                                contactStatusColor = response.getInt("statusColor");
                                contactStatusWay = response.getString("presentWay");
                                contactStatusUpdateText.setText(ScheduleAndSampleManager.getMinSecStringFromMillis(response.getLong("createdTime")));
                                renderStatus(contactStatusText, contactStatusProgressBar, contactStatusFormText,
                                        contactStatusForm, contactStatusRate,
                                        contactStatusWay, contactStatusString, contactStatusColor);
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

            Log.d(TAG, "user: " + contactList.get(position));
        }
    };

    private void ContactStatusLayout(boolean click, int visibility) {
        cover.setVisibility(visibility);
        contactStatusLayout.setVisibility(visibility);
        //TODO for partial: 需註解
        contactStatusClose.setVisibility(visibility);

        listView.setEnabled(click);        // 後面聯絡人不能按
        //TODO for partial: 需註解
        editProfile.setEnabled(click);     // 編輯的按鈕
    }

}
