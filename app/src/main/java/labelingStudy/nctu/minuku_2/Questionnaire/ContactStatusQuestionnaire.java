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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

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
    private InputMethodManager mInputMethodManager;

    // contact information
    private String contactId;
    private long checkContactStatusTime;
    private String contactStatusString;
    private String contactStatusWay;
    private String contactStatusForm;
    private int contactStatusRate;
    private int contactStatusColor;
    private TextView contactName;
    private TextView checkTime;
    private TextView contactRateText;
    private TextView contactStatusFormText;
    private CircularProgressBar contactCircle;

    // collected data
    private int selectedIsFreeA = 1;
    private int selectedIsFreeB = 1;
    private int selectedIsFreeC = 1;
    private int selectedPreferWayA = 1;
    private int selectedPreferWayB = 1;
    private int selectedPreferWayC = 1;

    private String selectedWhom = "";
    private String selectedWhomOther = "";
    private String selectedLocation = "";
    private String selectedLocationOther = "";
    private String selectedActivity = "";
    private String selectedActivityOther = "";
    private String usingPurpose = "";
    private String communicatePurpose = "";

    // 比較的方式
    private CircularProgressBar circularProgressBar1;
    private TextView textView_text;
    private TextView textView_digit;
    private TextView formText_graphic;
    private TextView formText_digit;

    private CircularProgressBar circularProgressBar2;
    private TextView textView_text2;
    private TextView textView_digit2;
    private TextView formText_graphic2;
    private TextView formText_digit2;
    // Q1: 哪個可以判斷是否有空
    private IndicatorSeekBar senderQuestion1a;
    private IndicatorSeekBar senderQuestion1b;
    private IndicatorSeekBar senderQuestion1c;
    // Q2: 想看到哪種方式呈現
    private IndicatorSeekBar senderQuestion2a;
    private IndicatorSeekBar senderQuestion2b;
    private IndicatorSeekBar senderQuestion2c;
    // Q1 context : whom, where, what
    private Spinner contextWhomSpinner;
    private Spinner contextWhereSpinner;
    private Spinner contextWhatSpinner;
    private EditText contextWhomEditText;
    private EditText contextWhereEditText;
    private EditText contextWhatEditText;
//    private RadioGroup senderLocation1;
//    private RadioButton home;
//
//    private RadioGroup senderLocation4;
//    private RadioButton locationOther;
//    private EditText locationOtherText;
    // Q4: 看對方的狀態的目的
    private RadioGroup radioGroupUsingPurpose;
    private RadioButton usingPurposeOther;
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
        contactStatusFormText = findViewById(R.id.contact_status_statusForm);

        // Q1: 哪種能幫助你判斷「對方現在是否有空」
        senderQuestion1a = findViewById(R.id.usefulSeekBarA);
        senderQuestion1a.setOnSeekChangeListener(new Q1aListener());
        senderQuestion1b = findViewById(R.id.usefulSeekBarB);
        senderQuestion1b.setOnSeekChangeListener(new Q1bListener());
        senderQuestion1c = findViewById(R.id.usefulSeekBarC);
        senderQuestion1c.setOnSeekChangeListener(new Q1cListener());
        // 三種方式
        circularProgressBar1 = findViewById(R.id.contactList_peopleStatus_progressCircle1);
        textView_text = findViewById(R.id.contactList_peopleStatus_text1);
        textView_digit = findViewById(R.id.contactList_peopleStatus_text2);
        formText_graphic = findViewById(R.id.contact_status_form_graphic);
        formText_digit = findViewById(R.id.contact_status_form_digit);

        circularProgressBar2 = findViewById(R.id.contactList_peopleStatus_progressCircle2);
        textView_text2 = findViewById(R.id.contactList_peopleStatus_text3);
        textView_digit2 = findViewById(R.id.contactList_peopleStatus_text4);
        formText_graphic2 = findViewById(R.id.contact_status_form_graphic2);
        formText_digit2 = findViewById(R.id.contact_status_form_digit2);
        // Q2: 請問你較想要看到哪種呈現方式
        senderQuestion2a = findViewById(R.id.preferSeekBarA);
        senderQuestion2a.setOnSeekChangeListener(new Q2aListener());
        senderQuestion2b = findViewById(R.id.preferSeekBarB);
        senderQuestion2b.setOnSeekChangeListener(new Q2bListener());
        senderQuestion2c = findViewById(R.id.preferSeekBarC);
        senderQuestion2c.setOnSeekChangeListener(new Q2cListener());
        // Q1 1. context: whom 誰
        contextWhomSpinner = findViewById(R.id.contact_context_whom);
        ArrayAdapter<CharSequence> contextWhomLunchList = ArrayAdapter.createFromResource(ContactStatusQuestionnaire.this,
                R.array.contextWhom,
                android.R.layout.simple_spinner_dropdown_item);
        contextWhomSpinner.setAdapter(contextWhomLunchList);
        contextWhomSpinner.setOnItemSelectedListener(new contextWhomSpinnerListener());
        contextWhomEditText = findViewById(R.id.contact_context_whom_other);
        contextWhomEditText.setOnEditorActionListener(new contextWhomOtherListener());
        // context: where 哪裡
        contextWhereSpinner = findViewById(R.id.contact_context_where);
        ArrayAdapter<CharSequence> contextWhereLunchList = ArrayAdapter.createFromResource(ContactStatusQuestionnaire.this,
                R.array.contextWhere,
                android.R.layout.simple_spinner_dropdown_item);
        contextWhereSpinner.setAdapter(contextWhereLunchList);
        contextWhereSpinner.setOnItemSelectedListener(new contextWhereSpinnerListener());
        contextWhereEditText = findViewById(R.id.contact_context_where_other);
        contextWhereEditText.setOnEditorActionListener(new contextWhereOtherListener());
        // context: what
        contextWhatSpinner = findViewById(R.id.contact_context_what);
        ArrayAdapter<CharSequence> contextWhatLunchList = ArrayAdapter.createFromResource(ContactStatusQuestionnaire.this,
                R.array.contextWhat,
                android.R.layout.simple_spinner_dropdown_item);
        contextWhatSpinner.setAdapter(contextWhatLunchList);
        contextWhatSpinner.setOnItemSelectedListener(new contextWhatSpinnerListener());
        contextWhatEditText = findViewById(R.id.contact_context_what_other);
        contextWhatEditText.setOnEditorActionListener(new contextWhatOtherListener());
//        senderLocation1 = findViewById(R.id.sender_location1);
//        senderLocation2 = findViewById(R.id.sender_location2);
//        senderLocation3 = findViewById(R.id.sender_location3);
//        senderLocation4 = findViewById(R.id.sender_location4);
//        home = findViewById(R.id.sender_home);
//        dorm = findViewById(R.id.sender_dorm);
//        lab = findViewById(R.id.sender_lab);
//        classroom = findViewById(R.id.sender_classroom);
//        library = findViewById(R.id.sender_library);
//        outdoor = findViewById(R.id.sender_outdoor);
//        transportation = findViewById(R.id.sender_transportation);
//        mall = findViewById(R.id.sender_mall);
//        locationOther = findViewById(R.id.sender_location_other);
//        locationOtherText = findViewById(R.id.sender_location_other_text);
//        locationOtherText.setOnEditorActionListener(new locationEditTextListener());
//
//        senderLocation1.setOnCheckedChangeListener(new locationListener1());
//        senderLocation2.setOnCheckedChangeListener(new locationListener2());
//        senderLocation3.setOnCheckedChangeListener(new locationListener3());
//        senderLocation4.setOnCheckedChangeListener(new locationListener4());
        // Q4: 請問你查看對方的狀態的目的是
        usingPurposeOther = findViewById(R.id.using_purpose_other);
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
        int renderRate = contactStatusRate;
        Log.d(TAG, "way: " + contactStatusWay);
        Log.d(TAG, "contactStatusRate: " + contactStatusRate);
        Log.d(TAG, "contactStatusString: " + contactStatusString);
        Log.d(TAG, "contactStatusColor: " + contactStatusColor);
        // text //
        textView_text.setText(contactStatusString);
        textView_text2.setText(contactStatusString);

        if (contactStatusForm.equals(Constants.STATUS_FORM_DISTURB)) renderRate = 100 - contactStatusRate;

        // graphic //
        circularProgressBar1.setProgressWithAnimation(Float.valueOf(renderRate), Constants.PROGRESS_ANIMATION_RATE);
        formText_graphic.setText(contactStatusForm);

        circularProgressBar2.setProgressWithAnimation(Float.valueOf(renderRate), Constants.PROGRESS_ANIMATION_RATE);
        formText_graphic2.setText(contactStatusForm);

        if (contactStatusColor != -1) {
            circularProgressBar1.setColor(contactStatusColor);
            circularProgressBar2.setColor(contactStatusColor);
        } else {
            circularProgressBar1.setColor(Constants.DEFAULT_COLOR);
            circularProgressBar2.setColor(Constants.DEFAULT_COLOR);
        }

        // digit // TODO:
        formText_digit.setText(contactStatusForm);
        textView_digit.setText(renderRate + "%");

        formText_digit2.setText(contactStatusForm);
        textView_digit2.setText(renderRate + "%");
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
            contactStatusForm = bundle.getString("statusForm", "NA");
            contactStatusColor = bundle.getInt("color", 0);
            contactStatusString = bundle.getString("statusString", "NA");
        } catch (Exception e) {
            e.printStackTrace();
        }

        contactName.setText(contactId);
        checkTime.setText(ScheduleAndSampleManager.getMinSecStringFromMillis(checkContactStatusTime));
        renderStatus(contactRateText, contactCircle, contactStatusFormText,
                contactStatusForm, contactStatusRate, contactStatusWay,
                contactStatusString, contactStatusColor);

        renderCompareStatus();
    }

    private Button.OnClickListener submitListener = new Button.OnClickListener() {

        @SuppressLint("LongLogTag")
        @Override
        public void onClick(View v) {

            Log.d(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            Log.d(TAG, "selectedIsFreeA: " + selectedIsFreeA);
            Log.d(TAG, "selectedPreferWayA: " + selectedPreferWayA);
            Log.d(TAG, "selectedLocation: " + selectedLocation);
            Log.d(TAG, "usingPurpose: " + usingPurpose);
            Log.d(TAG, "communicatePurpose: " + communicatePurpose);

            boolean pass = false;
            selectedWhomOther = contextWhomEditText.getText().toString();
            selectedLocationOther = contextWhereEditText.getText().toString();
            selectedActivityOther = contextWhatEditText.getText().toString();

            JSONObject data = getDataToServer();

            if (usingPurpose.equals("") || usingPurposeOther.isChecked()) {
                usingPurpose = usingPurposeOtherText.getText().toString();
            }

            if (
//                selectedIsFreeA != 0 && selectedPreferWayA != 0 &&
//                selectedIsFreeB != 0 && selectedPreferWayB != 0 &&
//                selectedIsFreeC != 0 && selectedPreferWayC != 0 &&
                !usingPurpose.equals("") &&
                (!selectedLocation.equals("") || !selectedLocationOther.equals("")) &&
                (!selectedWhom.equals("") || !selectedWhomOther.equals("")) &&
                (!selectedActivity.equals("") || !selectedActivityOther.equals(""))) {
                // if chose communicate for being the using purpose,
                // must chose communication purpose
                if (usingPurpose.equals("communicate") && communicatePurpose.equals("")) pass = false;
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
            data.put("checkContactStatusTimeString", ScheduleAndSampleManager.getTimeString(checkContactStatusTime));
            data.put("contactStatusWay", contactStatusWay);
            data.put("contactStatusForm", contactStatusForm);
            data.put("contactStatusColor", contactStatusColor);
            data.put("contactStatusString", contactStatusString);

            data.put("selectedLocation", selectedLocation);
            data.put("selectedLocationOther", selectedLocationOther);
            data.put("selectedActivity", selectedActivity);
            data.put("selectedActivityOther", selectedActivityOther);
            data.put("selectedWhom", selectedWhom);
            data.put("selectedWhomOther", selectedWhomOther);

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
            usingPurposeOtherText.clearFocus();
            return false;
        }
    }

    private class contextWhomSpinnerListener implements AdapterView.OnItemSelectedListener {
        @SuppressLint("LongLogTag")
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if (adapterView.getItemAtPosition(i).equals("與誰")) {
                // 如果是選第一個: 說明文字
                selectedWhom = "";
            } else {
                selectedWhom = adapterView.getItemAtPosition(i).toString();
                Log.d(TAG, "selectedWhom: " + selectedWhom);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    private class contextWhereSpinnerListener implements AdapterView.OnItemSelectedListener {
        @SuppressLint("LongLogTag")
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if (adapterView.getItemAtPosition(i).equals("在哪裡")) {
                // 如果是選第一個: 說明文字
                selectedLocation = "";
            } else {
                selectedLocation = adapterView.getItemAtPosition(i).toString();
                Log.d(TAG, "selectedLocation: " + selectedLocation);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    private class contextWhatSpinnerListener implements AdapterView.OnItemSelectedListener {
        @SuppressLint("LongLogTag")
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if (adapterView.getItemAtPosition(i).equals("做什麼")) {
                // 如果是選第一個: 說明文字
                selectedActivity = "";
            } else {
                selectedActivity = adapterView.getItemAtPosition(i).toString();
                Log.d(TAG, "selectedActivity: " + selectedActivity);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    private class contextWhomOtherListener implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            contextWhomEditText.clearFocus();
            mInputMethodManager.hideSoftInputFromWindow(ContactStatusQuestionnaire.this.getWindow().getDecorView().getRootView().getWindowToken(), 0);
            return false;
        }
    }

    private class contextWhereOtherListener implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            contextWhereEditText.clearFocus();
            mInputMethodManager.hideSoftInputFromWindow(ContactStatusQuestionnaire.this.getWindow().getDecorView().getRootView().getWindowToken(), 0);
            return false;
        }
    }

    private class contextWhatOtherListener implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            contextWhatEditText.clearFocus();
            mInputMethodManager.hideSoftInputFromWindow(ContactStatusQuestionnaire.this.getWindow().getDecorView().getRootView().getWindowToken(), 0);
            return false;
        }
    }

//    private class locationListener1 implements RadioGroup.OnCheckedChangeListener {
//        @SuppressLint("LongLogTag")
//        @Override
//        public void onCheckedChanged(RadioGroup group, int checkedId) {
////            Log.d(TAG, "checkedId: " + checkedId);
//
//            if (checkedId != -1) {
//                if (home.isChecked()) {
//                    selectedLocation = "home";
//                    Log.d(TAG, (String) home.getText()); // 家裡
//                } else if (dorm.isChecked()) {
//                    selectedLocation = "dorm";
//                } else if (lab.isChecked()) {
//                    selectedLocation = "lab";
//                }
//                if (home.isChecked() || dorm.isChecked() || lab.isChecked()) {
//                    locationOtherText.clearFocus();
//                    senderLocation2.clearCheck();
//                    senderLocation3.clearCheck();
//                    senderLocation4.clearCheck();
//                }
//            }
//        }
//    }

//    private class locationListener4 implements RadioGroup.OnCheckedChangeListener {
//        @Override
//        public void onCheckedChanged(RadioGroup group, int checkedId) {
//            switch (checkedId) {
//                case R.id.sender_location_other:
//                    if (locationOther.isChecked()) {
//                        locationOtherText.requestFocus();
//                        // show keyboard
//                        mInputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
//                        senderLocation1.clearCheck();
//                        senderLocation2.clearCheck();
//                        senderLocation3.clearCheck();
//                    }
//                    break;
//            }
//        }
//    }
//
//    private class locationEditTextListener implements EditText.OnEditorActionListener {
//        @Override
//        public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
//            mInputMethodManager.hideSoftInputFromWindow(ContactStatusQuestionnaire.this.getWindow().getDecorView().getRootView().getWindowToken(), 0);
//            locationOtherText.clearFocus();
//            return false;
//        }
//    }

    // 請問哪種呈現方式較能幫助你判斷「對方現在是否有空」?
    private class Q1aListener implements OnSeekChangeListener {
        @Override
        public void onSeeking(SeekParams seekParams) {

        }

        @Override
        public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

        }

        @SuppressLint("LongLogTag")
        @Override
        public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
            Log.d(TAG, "A: " + seekBar.getProgress());
            selectedIsFreeA = seekBar.getProgress();
        }
    }

    private class Q1cListener implements OnSeekChangeListener {
        @Override
        public void onSeeking(SeekParams seekParams) {

        }

        @Override
        public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

        }

        @SuppressLint("LongLogTag")
        @Override
        public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
            Log.d(TAG, "C: " + seekBar.getProgress());
            selectedIsFreeC = seekBar.getProgress();
        }
    }

    private class Q1bListener implements OnSeekChangeListener {
        @Override
        public void onSeeking(SeekParams seekParams) {

        }

        @Override
        public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

        }

        @SuppressLint("LongLogTag")
        @Override
        public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
            Log.d(TAG, "B: " + seekBar.getProgress());
            selectedIsFreeB = seekBar.getProgress();
        }
    }

    // 請問你較想要看到哪種呈現方式?
    private class Q2aListener implements OnSeekChangeListener {
        @Override
        public void onSeeking(SeekParams seekParams) {

        }

        @Override
        public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

        }

        @SuppressLint("LongLogTag")
        @Override
        public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
            Log.d(TAG, "pA: " + seekBar.getProgress());
            selectedPreferWayA = seekBar.getProgress();
        }
    }

    private class Q2bListener implements OnSeekChangeListener {
        @Override
        public void onSeeking(SeekParams seekParams) {

        }

        @Override
        public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

        }

        @SuppressLint("LongLogTag")
        @Override
        public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
            Log.d(TAG, "pB: " + seekBar.getProgress());
            selectedPreferWayB = seekBar.getProgress();
        }
    }

    private class Q2cListener implements OnSeekChangeListener {
        @Override
        public void onSeeking(SeekParams seekParams) {

        }

        @Override
        public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

        }

        @SuppressLint("LongLogTag")
        @Override
        public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
            Log.d(TAG, "pC: " + seekBar.getProgress());
            selectedPreferWayC = seekBar.getProgress();
        }
    }
}
