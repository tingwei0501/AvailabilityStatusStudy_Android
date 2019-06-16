package labelingStudy.nctu.minuku_2.Questionnaire;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.json.JSONException;
import org.json.JSONObject;

import labelingStudy.nctu.minuku.Utilities.ScheduleAndSampleManager;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku_2.ContactList;
import labelingStudy.nctu.minuku_2.R;

import static labelingStudy.nctu.minuku_2.manager.renderManager.randomPresentWay;
import static labelingStudy.nctu.minuku_2.manager.renderManager.renderStatus;

public class ContactStatusQuestionnaire extends Activity {
    private static final String TAG = "ContactStatusQuestionnaire";

    private Context mContext;
    private SharedPreferences sharedPreferences;
    private RequestQueue mQueue;
    private JsonObjectRequest mJsonObjectRequest;
//    private IBinder mIBinder;
    private InputMethodManager mInputMethodManager;

    // contact information
    private String contactId;
    private long checkContactStatusTime;
    private String contactStatusString;
    private String contactStatusWay;
    private int contactStatusRate;
    private int contactStatusColor;
    private TextView contactName;
    private TextView checkTime;
    private TextView contactRateText;
    private CircularProgressBar contactCircle;

    // collected data
    private String selectedIsFreeA = "";
    private String selectedIsFreeB = "";
    private String selectedIsFreeC = "";
    private String selectedPreferWayA = "";
    private String selectedPreferWayB = "";
    private String selectedPreferWayC = "";
    private String selectedLocation = "";
    private String usingPurpose = "";
    private String communicatePurpose = "";

    // 比較的方式
    private CircularProgressBar circularProgressBar1;
    private TextView textView_text;
    private TextView textView_digit;
    // Q1: 哪個可以判斷是否有空
    private RadioGroup senderQuestion1a;
    private RadioGroup senderQuestion1b;
    private RadioGroup senderQuestion1c;
    // Q2: 想看到哪種方式呈現
    private RadioGroup senderQuestion2a;
    private RadioGroup senderQuestion2b;
    private RadioGroup senderQuestion2c;
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
    private ConstraintLayout communicationPurposeLayout;
    private RadioGroup radioGroupCommunicatePurpose;

    private Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_status_questionnaire);

        mContext = getApplicationContext();
        sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        mInputMethodManager = (InputMethodManager) getSystemService(mContext.INPUT_METHOD_SERVICE);

        // 對方的狀態
        contactName = findViewById(R.id.contact_questionnaire_name);
        checkTime = findViewById(R.id.contact_questionnaire_time);
        contactRateText = findViewById(R.id.contact_questionnaire_rate);
        contactCircle = findViewById(R.id.contact_questionnaire_circle);

        // Q1: 哪種能幫助你判斷「對方現在是否有空」
        senderQuestion1a = findViewById(R.id.sender_question1_a);
        senderQuestion1a.setOnCheckedChangeListener(new Q1aListener());
        senderQuestion1b = findViewById(R.id.sender_question1_b);
        senderQuestion1b.setOnCheckedChangeListener(new Q1bListener());
        senderQuestion1c = findViewById(R.id.sender_question1_c);
        senderQuestion1c.setOnCheckedChangeListener(new Q1cListener());
        circularProgressBar1 = findViewById(R.id.contactList_peopleStatus_progressCircle1);
        textView_text = findViewById(R.id.contactList_peopleStatus_text1);
        textView_digit = findViewById(R.id.contactList_peopleStatus_text2);
        // Q2: 請問你較想要看到哪種呈現方式
        senderQuestion2a = findViewById(R.id.sender_question2_a);
        senderQuestion2a.setOnCheckedChangeListener(new Q2aListener());
        senderQuestion2b = findViewById(R.id.sender_question2_b);
        senderQuestion2b.setOnCheckedChangeListener(new Q2bListener());
        senderQuestion2c = findViewById(R.id.sender_question2_c);
        senderQuestion2c.setOnCheckedChangeListener(new Q2cListener());
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
        communicationPurposeLayout = findViewById(R.id.communicationPurposeLayout);
        radioGroupCommunicatePurpose = findViewById(R.id.communication_purpose);
        radioGroupCommunicatePurpose.setOnCheckedChangeListener(new communicatePurposeListener());

        // submit button
        submit = findViewById(R.id.sender_questionnaire_submit);
        submit.setOnClickListener(submitListener);

        getContactInfo();
//
    }

    @SuppressLint("LongLogTag")
    private void renderCompareStatus() {

        Log.d(TAG, "way: " + contactStatusWay);
        Log.d(TAG, "contactStatusRate: " + contactStatusRate);
        Log.d(TAG, "contactStatusString: " + contactStatusString);
        Log.d(TAG, "contactStatusColor: " + contactStatusColor);
        // text //
        textView_text.setText(contactStatusString);

        // graphic //
        circularProgressBar1.setProgressWithAnimation(Float.valueOf(contactStatusRate), Constants.PROGRESS_ANIMATION_RATE);
        if (contactStatusColor != -1) {
            circularProgressBar1.setColor(contactStatusColor);
        } else {
            circularProgressBar1.setColor(Constants.DEFAULT_COLOR);
        }

        // digit //
        textView_digit.setText("回覆率" + contactStatusRate + "%");
    }

    @SuppressLint("LongLogTag")
    private void getContactInfo() {
        Bundle bundle = getIntent().getExtras();
        try {
            Log.d(TAG, "in bundle");
            contactId = bundle.getString("id", "NA");
            contactStatusRate = bundle.getInt("status", 0);
            checkContactStatusTime = bundle.getLong("checkTime", 0);
            contactStatusWay = bundle.getString("way", "digit");
            contactStatusColor = bundle.getInt("color", 0);
            contactStatusString = bundle.getString("statusString", "NA");
        } catch (Exception e) {
            e.printStackTrace();
        }

        contactName.setText(contactId);
        checkTime.setText(ScheduleAndSampleManager.getMinSecStringFromMillis(checkContactStatusTime));
        renderStatus(contactRateText, contactCircle,
                contactStatusRate, contactStatusWay,
                contactStatusString, contactStatusColor);

        renderCompareStatus();
    }

    private Button.OnClickListener submitListener = new Button.OnClickListener() {

        @SuppressLint("LongLogTag")
        @Override
        public void onClick(View v) {

            Log.d(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
//            Log.d(TAG, "selectedContactPerson: " + selectedContactPerson);
            Log.d(TAG, "selectedIsFreeA: " + selectedIsFreeA);
            Log.d(TAG, "selectedPreferWayA: " + selectedPreferWayA);
            Log.d(TAG, "selectedLocation: " + selectedLocation);
            Log.d(TAG, "usingPurpose: " + usingPurpose);
            Log.d(TAG, "communicatePurpose: " + communicatePurpose);

            boolean pass = false;
            JSONObject data = getDataToServer();

            if ((!selectedIsFreeA.equals("") && !selectedPreferWayA.equals("") &&
                !selectedIsFreeB.equals("") && !selectedPreferWayB.equals("") &&
                !selectedIsFreeC.equals("") && !selectedPreferWayC.equals("") &&
                !selectedLocation.equals("") && !usingPurpose.equals(""))) {
                // if chose communicate for being the using purpose,
                // must chose communication purpose
                if (usingPurpose.equals("communicate") && !communicatePurpose.equals("")) pass = true;
                else pass = true;
            }

            if (pass) {
                // send to database
                mQueue = Volley.newRequestQueue(mContext);
                mJsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Constants.storeContactQuestionnaire, data,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d(TAG, response.toString());
                                // start activity here
                                try {
                                    if (response.getString("response").equals("success insert")) {
                                        Toast.makeText(ContactStatusQuestionnaire.this,
                                                "感謝您填寫問卷!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(ContactStatusQuestionnaire.this, ContactList.class);
                                        startActivity(intent);

                                        ContactStatusQuestionnaire.this.finish();
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
                Toast.makeText(ContactStatusQuestionnaire.this,
                        "請每題都要填寫!", Toast.LENGTH_SHORT).show();
            }

        }
    };

    private JSONObject getDataToServer() {
        JSONObject data = new JSONObject();
        try {
            data.put("id", sharedPreferences.getString("id", ""));
            data.put("contactId", contactId);
            data.put("contactStatusRate", contactStatusRate);
            data.put("checkContactStatusTime", checkContactStatusTime);
            data.put("contactStatusWay", contactStatusWay);
            data.put("contactStatusColor", contactStatusColor);
            data.put("contactStatusString", contactStatusString);

            data.put("selectedLocation", selectedLocation);
            data.put("usingPurpose", usingPurpose);
            data.put("communicatePurpose", communicatePurpose);
            data.put("selectedIsFreeA", selectedIsFreeA);
            data.put("selectedIsFreeB", selectedIsFreeB);
            data.put("selectedIsFreeC", selectedIsFreeC);
            data.put("selectedPreferWayA", selectedPreferWayA);
            data.put("selectedPreferWayB", selectedPreferWayB);
            data.put("selectedPreferWayC", selectedPreferWayC);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return data;
    }

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
                mInputMethodManager.hideSoftInputFromWindow(ContactStatusQuestionnaire.this.getWindow().getDecorView().getRootView().getWindowToken(), 0);
//                mIBinder = ContactStatusQuestionnaire.this.getCurrentFocus().getWindowToken();
//                mInputMethodManager.hideSoftInputFromWindow(mIBinder, InputMethodManager.HIDE_NOT_ALWAYS);
                if (checkedId == R.id.communicate) {
                    usingPurpose = "communicate";
                    communicationPurposeLayout.setVisibility(View.VISIBLE);
                } else if (checkedId == R.id.curious) {
                    usingPurpose = "curious";
                    communicationPurposeLayout.setVisibility(View.GONE);
                } else if (checkedId == R.id.boring) {
                    usingPurpose = "boring";
                    communicationPurposeLayout.setVisibility(View.GONE);
                } else if (checkedId == R.id.using_purpose_other) {
                    communicationPurposeLayout.setVisibility(View.GONE);
                }
            }
        }
    }

    private class usingPurposeEditTextListener implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            mInputMethodManager.hideSoftInputFromWindow(ContactStatusQuestionnaire.this.getWindow().getDecorView().getRootView().getWindowToken(), 0);
//            mIBinder = ContactStatusQuestionnaire.this.getCurrentFocus().getWindowToken();
//            mInputMethodManager.hideSoftInputFromWindow(mIBinder, InputMethodManager.HIDE_NOT_ALWAYS);
            usingPurpose = usingPurposeOtherText.getText().toString();
            usingPurposeOtherText.clearFocus();
            return false;
        }
    }

    private class locationListener1 implements RadioGroup.OnCheckedChangeListener {
        @SuppressLint("LongLogTag")
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
//            Log.d(TAG, "checkedId: " + checkedId);

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
//            Log.d(TAG, "checkedId: " + checkedId);

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
            mInputMethodManager.hideSoftInputFromWindow(ContactStatusQuestionnaire.this.getWindow().getDecorView().getRootView().getWindowToken(), 0);
//            mIBinder = ContactStatusQuestionnaire.this.getCurrentFocus().getWindowToken();
//            mInputMethodManager.hideSoftInputFromWindow(mIBinder, InputMethodManager.HIDE_NOT_ALWAYS);
            selectedLocation = locationOtherText.getText().toString();
            locationOtherText.clearFocus();
            return false;
        }
    }

    // 請問哪種呈現方式較能幫助你判斷「對方現在是否有空」?
    private class Q1aListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.sender_question1a_3:
                    selectedIsFreeA = "3";
                    break;
                case R.id.sender_question1a_2:
                    selectedIsFreeA = "2";
                    break;
                case R.id.sender_question1a_1:
                    selectedIsFreeA = "1";
                    break;
            }
        }
    }

    private class Q1cListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.sender_question1c_3:
                    selectedIsFreeC = "3";
                    break;
                case R.id.sender_question1c_2:
                    selectedIsFreeC = "2";
                    break;
                case R.id.sender_question1c_1:
                    selectedIsFreeC = "1";
                    break;
            }
        }
    }

    private class Q1bListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.sender_question1b_3:
                    selectedIsFreeB = "3";
                    break;
                case R.id.sender_question1b_2:
                    selectedIsFreeB = "2";
                    break;
                case R.id.sender_question1b_1:
                    selectedIsFreeB = "1";
                    break;
            }
        }
    }

    // 請問你較想要看到哪種呈現方式?
    private class Q2aListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.sender_question2a_3:
                    selectedPreferWayA = "3";
                    break;
                case R.id.sender_question2a_2:
                    selectedPreferWayA = "2";
                    break;
                case R.id.sender_question2a_1:
                    selectedPreferWayA = "1";
                    break;
            }
        }
    }

    private class Q2bListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.sender_question2b_3:
                    selectedPreferWayB = "3";
                    break;
                case R.id.sender_question2b_2:
                    selectedPreferWayB = "2";
                    break;
                case R.id.sender_question2b_1:
                    selectedPreferWayB = "1";
                    break;
            }
        }
    }

    private class Q2cListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.sender_question2c_3:
                    selectedPreferWayC = "3";
                    break;
                case R.id.sender_question2c_2:
                    selectedPreferWayC = "2";
                    break;
                case R.id.sender_question2c_1:
                    selectedPreferWayC = "1";
                    break;
            }
        }
    }
}
