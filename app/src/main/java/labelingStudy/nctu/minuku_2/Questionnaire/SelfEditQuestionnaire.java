package labelingStudy.nctu.minuku_2.Questionnaire;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import labelingStudy.nctu.minuku.Utilities.ScheduleAndSampleManager;
import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku_2.ContactList;
import labelingStudy.nctu.minuku_2.R;
import labelingStudy.nctu.minuku_2.service.BackgroundService;

import static android.content.Context.MODE_PRIVATE;
import static labelingStudy.nctu.minuku_2.manager.renderManager.renderStatus;
import static labelingStudy.nctu.minuku_2.manager.renderManager.statusGetRate;

public class SelfEditQuestionnaire extends Activity {
    private static final String TAG = "SelfEditQuestionnaire";
    private Context mContext;
    private RequestQueue mQueue;
    private JsonObjectRequest mJsonObjectRequest;
    private SharedPreferences sharedPreferences;
    private SharedPreferences sharedPreferences_contact;
    private InputMethodManager mInputMethodManager;

    // collected data
    private String selectedWhom = "";
    private String selectedWhomOther = "";
    private String selectedLocation = "";
    private String selectedLocationOther = "";
    private String selectedActivity = "";
    private String selectedActivityOther = "";
    private String idealStatusWay = "";
    private String idealStatusForm = "";
    private int idealStatusRate = -1;
    private String idealStatusString = "";
    private String changeStatusOtherReason = "";
//    private String notFitReason = "";
    private ArrayList<String> showToWhoArray = new ArrayList<>();
    private ArrayList<String> blockToWhoArray = new ArrayList<>();
    private int timePeriod;

    // show your status (更改過)
    private TextView time;
    private TextView hour, minute;
    private CircularProgressBar selfStatusCircle;
    private TextView selfStatusText;
    private TextView selfStatusFormText;
    private String selfStatusWay;
    private String selfStatusForm;
    private String selfStatusString;
    private int selfStatusRate;
    private int selfStatusColor;
    private long showTime;

    // Q1 context : whom, where, what
    private Spinner contextWhomSpinner;
    private Spinner contextWhereSpinner;
    private Spinner contextWhatSpinner;
    private EditText contextWhomEditText;
    private EditText contextWhereEditText;
    private EditText contextWhatEditText;
    //
    // Q2 實際狀態
    private boolean idealShowDifferent = true;
    private CheckBox asChangedStatusCheckBox;
    private Spinner waySpinnerIdeal;
    private Spinner formSpinnerIdeal;
    private Spinner textSpinnerIdeal;
    private IndicatorSeekBar digitSeekBarIdeal;
    private CircularProgressBar graphicCircleIdeal;
    private SeekBar graphicSeekBarIdeal;
    //
    // Q3 修改原因
//    private EditText notFitReasonEditText;
    private CheckBox notFitReasonLessBother;
    private CheckBox notFitReasonDonotBother;
    private CheckBox notFitReasonBother;
    private CheckBox notFitReasonAccurate;
    private CheckBox notFitReasonBlurred;
    private CheckBox notFitReasonHide;
    private CheckBox notFitReasonOther;
    private EditText notFitReasonOtherText;

    // Q3想特別呈現給誰嗎 & Q4不想給誰
    private ArrayList<String> contactList;
    private ListView showToWho;
    private EditText showToWhoOtherEditText;
    private ListView blockToWho;
    private EditText blockToWhoOtherEditText;
    // Q5 一句話
//    private EditText mySituationEditText;

    private Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.self_edit_questionnaire);
        mContext = getApplicationContext();
        sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        sharedPreferences_contact = getSharedPreferences(Constants.sharedPrefString_ContactList, MODE_PRIVATE);

        mInputMethodManager = (InputMethodManager) getSystemService(mContext.INPUT_METHOD_SERVICE);

        time = findViewById(R.id.self_edit_questionnaire_time);
        hour = findViewById(R.id.self_edit_questionnaire_time_hour);
        minute = findViewById(R.id.self_edit_questionnaire_time_min);
        selfStatusCircle = findViewById(R.id.self_edit_questionnaire_circle);
        selfStatusText = findViewById(R.id.self_edit_questionnaire_rate);
        selfStatusFormText = findViewById(R.id.self_edit_questionnaire_statusForm);

        // Q1 1. context: whom 誰
        contextWhomSpinner = findViewById(R.id.self_edit_context_whom);
        ArrayAdapter<CharSequence> contextWhomLunchList = ArrayAdapter.createFromResource(SelfEditQuestionnaire.this,
                R.array.contextWhom,
                android.R.layout.simple_spinner_dropdown_item);
        contextWhomSpinner.setAdapter(contextWhomLunchList);
        contextWhomSpinner.setOnItemSelectedListener(new contextWhomSpinnerListener());
        contextWhomEditText = findViewById(R.id.self_edit_context_whom_other);
        contextWhomEditText.setOnEditorActionListener(new contextWhomOtherListener());
        // context: where 哪裡
        contextWhereSpinner = findViewById(R.id.self_edit_context_where);
        ArrayAdapter<CharSequence> contextWhereLunchList = ArrayAdapter.createFromResource(SelfEditQuestionnaire.this,
                R.array.contextWhere,
                android.R.layout.simple_spinner_dropdown_item);
        contextWhereSpinner.setAdapter(contextWhereLunchList);
        contextWhereSpinner.setOnItemSelectedListener(new contextWhereSpinnerListener());
        contextWhereEditText = findViewById(R.id.self_edit_context_where_other);
        contextWhereEditText.setOnEditorActionListener(new contextWhereOtherListener());
        // context: what
        contextWhatSpinner = findViewById(R.id.self_edit_context_what);
        ArrayAdapter<CharSequence> contextWhatLunchList = ArrayAdapter.createFromResource(SelfEditQuestionnaire.this,
                R.array.contextWhat,
                android.R.layout.simple_spinner_dropdown_item);
        contextWhatSpinner.setAdapter(contextWhatLunchList);
        contextWhatSpinner.setOnItemSelectedListener(new contextWhatSpinnerListener());
        contextWhatEditText = findViewById(R.id.self_edit_context_what_other);
        contextWhatEditText.setOnEditorActionListener(new contextWhatOtherListener());
        // Q2 實際狀態
        asChangedStatusCheckBox = findViewById(R.id.self_edit_asIdealStatusCheckbox);
        asChangedStatusCheckBox.setOnCheckedChangeListener(new asChangedStatusListener());
        waySpinnerIdeal = findViewById(R.id.self_edit_way_spinner_ideal);
        ArrayAdapter<CharSequence> wayLunchListShow = ArrayAdapter.createFromResource(SelfEditQuestionnaire.this,
                R.array.showContactWay_withhint,
                android.R.layout.simple_spinner_dropdown_item);
        waySpinnerIdeal.setAdapter(wayLunchListShow);
        waySpinnerIdeal.setOnItemSelectedListener(new presentWaySpinnerIdealListener());

        formSpinnerIdeal = findViewById(R.id.self_edit_form_spinner_ideal);
        ArrayAdapter<CharSequence> formLunchListShow = ArrayAdapter.createFromResource(SelfEditQuestionnaire.this,
                R.array.showContactForm_withhint,
                android.R.layout.simple_spinner_dropdown_item);
        formSpinnerIdeal.setAdapter(formLunchListShow);
        formSpinnerIdeal.setOnItemSelectedListener(new formSpinnerIdealListener());

        textSpinnerIdeal = findViewById(R.id.self_edit_textStatus_spinner_ideal);
        ArrayAdapter<CharSequence> textLunchListShow = ArrayAdapter.createFromResource(SelfEditQuestionnaire.this,
                R.array.textStatus_withhint,
                android.R.layout.simple_spinner_dropdown_item);
        textSpinnerIdeal.setAdapter(textLunchListShow);
        textSpinnerIdeal.setOnItemSelectedListener(new textStatusSpinnerIdealListener());

        digitSeekBarIdeal = findViewById(R.id.self_edit_digitStatus_seekbar_ideal);
        digitSeekBarIdeal.setOnSeekChangeListener(new digitSeekBarIdealListener());

        graphicCircleIdeal = findViewById(R.id.self_edit_graphStatus_circle_ideal);
        graphicCircleIdeal.setColor(Constants.DEFAULT_COLOR);
        graphicSeekBarIdeal = findViewById(R.id.self_edit_graphStatus_circleSeekBar_ideal);
        graphicSeekBarIdeal.setOnSeekBarChangeListener(new graphicSeekBarIdealListener());
        // Q3
//        notFitReasonEditText = findViewById(R.id.self_edit_change_reason_editText);
//        notFitReasonEditText.setOnEditorActionListener(new notFitReasonListener());
        notFitReasonLessBother = findViewById(R.id.self_edit_notFitReason_less_bother);
        notFitReasonDonotBother = findViewById(R.id.self_edit_notFitReason_donot_bother);
        notFitReasonBother = findViewById(R.id.self_edit_notFitReason_bother);
        notFitReasonAccurate = findViewById(R.id.self_edit_notFitReason_accurate);
        notFitReasonBlurred = findViewById(R.id.self_edit_notFitReason_blurred);
        notFitReasonHide = findViewById(R.id.self_edit_notFitReason_hide);
        notFitReasonOther = findViewById(R.id.self_edit_notFitReason_other_text);
        notFitReasonOther.setOnCheckedChangeListener(new notFitReasonOtherListener());
        notFitReasonOtherText = findViewById(R.id.self_edit_notFitReason_otherText_text);
        notFitReasonOtherText.setOnEditorActionListener(new notFitReasonOtherEditTextListener());
        //
        // Q3
        initContact();
        showToWhoOtherEditText = findViewById(R.id.self_edit_showToWho_other);
        showToWhoOtherEditText.setOnEditorActionListener(new showToWhoOtherListener());
        showToWho = findViewById(R.id.self_edit_showToWho_list);
        showToWho.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        ArrayAdapter contact1 = new ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice,
                contactList);
        showToWho.setOnItemClickListener(new showToWhoListener());
        showToWho.setAdapter(contact1);

        blockToWhoOtherEditText = findViewById(R.id.self_edit_blockToWho_other);
        blockToWhoOtherEditText.setOnEditorActionListener(new blockToWhoOtherListener());
        blockToWho = findViewById(R.id.self_edit_blockToWho_list);
        blockToWho.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        blockToWho.setOnItemClickListener(new blockToWhoListener());
        blockToWho.setAdapter(contact1);
        //
        // Q5
//        mySituationEditText = findViewById(R.id.self_edit_editText);
//        mySituationEditText.setOnEditorActionListener(new mySituationListener());
        //

        // submit button
        submit = findViewById(R.id.self_edit_questionnaire_submit);
        submit.setOnClickListener(submitListener);
    }

    private void initContact() {
        contactList = new ArrayList<>();
        int length = sharedPreferences_contact.getInt("contactLength", 0);
        for (int i=0;i<length;i++) {
            String c = sharedPreferences_contact.getString("contact_"+i, null);
            Log.d(TAG, "#: " + c);
            contactList.add(c);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Bundle bundle = getIntent().getExtras();

        showTime = bundle.getLong("createdTime");
        selfStatusRate = bundle.getInt("status");
        selfStatusWay = bundle.getString("presentWay");
        selfStatusForm = bundle.getString("statusForm");
        selfStatusColor = bundle.getInt("statusColor");
        selfStatusString = bundle.getString("statusText");
        timePeriod = bundle.getInt("timePeriod");
        Log.d(TAG, "time period: " + timePeriod/60);
        Log.d(TAG, "time in min: " + timePeriod%60);

        if (showTime != 0) {
            time.setText(ScheduleAndSampleManager.getMinSecStringFromMillis(showTime));
            renderStatus(selfStatusText, selfStatusCircle, selfStatusFormText,
                    selfStatusForm, //應該是 selfStatusForm (從 Edit來)
                    selfStatusRate, selfStatusWay, selfStatusString, selfStatusColor);
            hour.setText(String.valueOf(timePeriod/60));
            minute.setText(String.valueOf(timePeriod%60));
        }
    }

    private Button.OnClickListener submitListener = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {

            boolean pass = false;
            long currentTime = ScheduleAndSampleManager.getCurrentTimeInMillis();

            JSONObject data = new JSONObject();
            JSONArray arr = new JSONArray();
            JSONArray showArr = new JSONArray();
            JSONArray blockArr = new JSONArray();
            selectedWhomOther = contextWhomEditText.getText().toString();
            selectedLocationOther = contextWhereEditText.getText().toString();
            selectedActivityOther = contextWhatEditText.getText().toString();
//            notFitReason = notFitReasonEditText.getText().toString();

            if ((!selectedLocation.equals("") || !selectedLocationOther.equals("")) &&
                (!selectedWhom.equals("") || !selectedWhomOther.equals("")) &&
                (!selectedActivity.equals("") || !selectedActivityOther.equals(""))) {
                pass = true;
                Log.d(TAG, "selectedLocation:  "+ selectedLocation);
                Log.d(TAG, "selectedWhom: " + selectedWhom);
                Log.d(TAG, "selectedActivity: " + selectedActivity);
                Log.d(TAG, "selectedLocationOther:  "+ selectedLocationOther);
                Log.d(TAG, "selectedWhomOther: " + selectedWhomOther);
                Log.d(TAG, "selectedActivityOther: " + selectedActivityOther);
//                Log.d(TAG, "notFitReason: " + notFitReason);

                try {
                    data.put("completeTime", currentTime);
                    data.put("completeTimeString", ScheduleAndSampleManager.getTimeString(currentTime));
                    data.put("user_id", sharedPreferences.getString("id", ""));

                    data.put("createdTime", showTime);
                    data.put("changeEventId", showTime); // use timestamp as primary key
                    data.put("createdTimeString", ScheduleAndSampleManager.getTimeString(showTime));

                    // context information
                    data.put("selectedLocation", selectedLocation);
                    data.put("selectedLocationOther", selectedLocationOther);
                    data.put("selectedActivity", selectedActivity);
                    data.put("selectedActivityOther", selectedActivityOther);
                    data.put("selectedWhom", selectedWhom);
                    data.put("selectedWhomOther", selectedWhomOther);
//                    data.put("mySituation", mySituationEditText.getText().toString());

                    // 想要給誰看
                    if (showToWhoArray.size() != 0) {
                        for (String c: showToWhoArray) {
                            showArr.put(c);
                        }
                        data.put("showToWho", showArr);
                    } else {
                        data.put("showToWho", "NA");
                    }
                    Log.d(TAG, "showToWhoOther: "+ showToWhoOtherEditText.getText().toString());
                    data.put("showToWhoOther", showToWhoOtherEditText.getText().toString());

                    // 不想給誰看
                    if (blockToWhoArray.size() != 0) {
                        for (String c: blockToWhoArray) {
                            blockArr.put(c);
                        }
                        data.put("blockToWho", blockArr);
                    } else {
                        data.put("blockToWho", "NA");
                    }
                    data.put("blockToWhoOther", blockToWhoOtherEditText.getText().toString());
//                    Log.d(TAG, "mySituation: " + mySituationEditText.getText().toString());

//                    data.put("notFitReason", notFitReason);
                    ////////// for CheckBox //////////////////////////////////////
                    CheckBox[] id = {notFitReasonLessBother, notFitReasonDonotBother, notFitReasonBother,
                            notFitReasonAccurate, notFitReasonBlurred, notFitReasonHide, notFitReasonOther};

                    boolean atLeastOneReason = false;
                    for (CheckBox i:id) {
                        if (i.isChecked()) {
                            arr.put(true);
                            atLeastOneReason = true;
                        } else {
                            arr.put(false);
                        }
                    }
                    changeStatusOtherReason = notFitReasonOtherText.getText().toString();
                    if (!atLeastOneReason && changeStatusOtherReason.equals("")) pass = false;

                    data.put("changeReasons", arr);
                    data.put("changeOtherReason", changeStatusOtherReason);
                    Log.d(TAG, "other reasons: " + changeStatusOtherReason);
                    //////////////////////////////////////////////////////////////
                    // 存實際狀態
                    if (idealShowDifferent) { // 沒有勾: 更改的不同於實際狀態，需要抓取spinner的值
                        idealStatusWay = waySpinnerIdeal.getSelectedItem().toString();
                        data.put("idealStatueWay", idealStatusWay);

                        if (idealStatusWay.equals("文字顯示")) {
                            if (!idealStatusString.equals("")) {
                                idealStatusString = textSpinnerIdeal.getSelectedItem().toString();
                                data.put("idealStatusForm", "NA");
                                data.put("idealStatusString", idealStatusString);
                                data.put("idealStatusRate", statusGetRate(idealStatusString));
                                if (pass) pass = true;
                            } else pass = false;
                        } else {
                            if (idealStatusRate != -1 && !idealStatusForm.equals("")) {
                                Log.d(TAG, "idealStatusRate~~~ 0: " + idealStatusRate);
                                idealStatusForm = formSpinnerIdeal.getSelectedItem().toString();
                                if (idealStatusForm.equals(Constants.STATUS_FORM_DISTURB)) {
                                    idealStatusRate = 100 - idealStatusRate;
                                }
                                Log.d(TAG, "idealStatusRate~~~ 1: " + idealStatusRate);
                                data.put("idealStatusRate", idealStatusRate);
                                data.put("idealStatusForm", idealStatusForm);
                                data.put("idealStatusString", "NA");
                                if (pass) pass = true;
                            } else pass = false;
                        }
                        Log.d(TAG, "idealStatusForm~ " + idealStatusForm);
                        Log.d(TAG, "idealStatusWay~ " + idealStatusWay);
                        Log.d(TAG, "idealStatusRate~ " + idealStatusRate);
                        Log.d(TAG, "idealStatusString~ " + idealStatusString);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (pass) {
                // send to database
                mQueue = Volley.newRequestQueue(mContext);
                mJsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Constants.storeSelfQuestionnaire, data,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d(TAG, response.toString());
                                // start activity here
                                try {
                                    if (response.getString("response").equals("success insert")) {
                                        Intent intent = new Intent(SelfEditQuestionnaire.this, ContactList.class);
                                        Toast.makeText(SelfEditQuestionnaire.this,
                                                "填寫成功!", Toast.LENGTH_SHORT).show();
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP); //加這行
                                        startActivity(intent);
                                        SelfEditQuestionnaire.this.finish();
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
                Toast.makeText(SelfEditQuestionnaire.this,
                        "請每題都要填寫!", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private class notFitReasonOtherListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            Log.d(TAG, "notFitReason " + b);
            if (b) {
                notFitReasonOther.requestFocus();
                notFitReasonOther.setCursorVisible(true);
                mInputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            } else {
                notFitReasonOther.clearFocus();
                notFitReasonOther.setCursorVisible(false);
                mInputMethodManager.hideSoftInputFromWindow(SelfEditQuestionnaire.this.getWindow().getDecorView().getRootView().getWindowToken(), 0);
            }
        }
    }

    private class notFitReasonOtherEditTextListener implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            notFitReasonOther.clearFocus();
            notFitReasonOther.setCursorVisible(false);
            mInputMethodManager.hideSoftInputFromWindow(SelfEditQuestionnaire.this.getWindow().getDecorView().getRootView().getWindowToken(), 0);
            return false;
        }
    }

    private class showToWhoListener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            ArrayList<String> selectedContactToShow = new ArrayList<>();
            SparseBooleanArray checkedItems = showToWho.getCheckedItemPositions();
//            Log.d(TAG, "checkedItems: " + checkedItems);
            for (int j=0;j<contactList.size();j++) {
                if (checkedItems.get(j)) { // 有被選中
                    selectedContactToShow.add(contactList.get(j));
                }
            }
            showToWhoArray = new ArrayList<>(selectedContactToShow);
        }
    }

    private class blockToWhoListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            ArrayList<String> selectedContactToBlock = new ArrayList<>();
            SparseBooleanArray checkedItems = blockToWho.getCheckedItemPositions();
//            Log.d(TAG, "checkedItems: " + checkedItems);
            for (int j=0;j<contactList.size();j++) {
                if (checkedItems.get(j)) { // 有被選中
                    selectedContactToBlock.add(contactList.get(j));
                }
            }
            blockToWhoArray = new ArrayList<>(selectedContactToBlock);
        }
    }

    private class contextWhomSpinnerListener implements AdapterView.OnItemSelectedListener {
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
            mInputMethodManager.hideSoftInputFromWindow(SelfEditQuestionnaire.this.getWindow().getDecorView().getRootView().getWindowToken(), 0);
            return false;
        }
    }

    private class contextWhereOtherListener implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            contextWhereEditText.clearFocus();
            mInputMethodManager.hideSoftInputFromWindow(SelfEditQuestionnaire.this.getWindow().getDecorView().getRootView().getWindowToken(), 0);
            return false;
        }
    }

    private class contextWhatOtherListener implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            contextWhatEditText.clearFocus();
            mInputMethodManager.hideSoftInputFromWindow(SelfEditQuestionnaire.this.getWindow().getDecorView().getRootView().getWindowToken(), 0);
            return false;
        }
    }

    private class showToWhoOtherListener implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            showToWhoOtherEditText.clearFocus();
            mInputMethodManager.hideSoftInputFromWindow(SelfEditQuestionnaire.this.getWindow().getDecorView().getRootView().getWindowToken(), 0);
            return false;
        }
    }

    private class blockToWhoOtherListener implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            blockToWhoOtherEditText.clearFocus();
            mInputMethodManager.hideSoftInputFromWindow(SelfEditQuestionnaire.this.getWindow().getDecorView().getRootView().getWindowToken(), 0);
            return false;
        }
    }

    private class notFitReasonListener implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            textView.clearFocus();
            mInputMethodManager.hideSoftInputFromWindow(SelfEditQuestionnaire.this.getWindow().getDecorView().getRootView().getWindowToken(), 0);
            return false;
        }
    }

    private class asChangedStatusListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if (b) {
                idealShowDifferent = false;
                reviseStatusOrNotShow(false);
            } else {
                // 與實際不同
                idealShowDifferent = true;
                reviseStatusOrNotShow(true);
            }
        }
    }

    private void reviseStatusOrNotShow(boolean revise) {
        waySpinnerIdeal.setEnabled(revise);
        textSpinnerIdeal.setEnabled(revise);
        digitSeekBarIdeal.setEnabled(revise);
        graphicSeekBarIdeal.setEnabled(revise);
        formSpinnerIdeal.setEnabled(revise);
    }

    private class presentWaySpinnerIdealListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            Log.d(TAG, "presentWaySpinnerIdeal position: " + i);
            switch (i) {
                case 0:
                    idealStatusWay = "";
                    break;
                case 1:
                    idealStatusWay = "文字顯示";
                    textSpinnerIdeal.setVisibility(View.VISIBLE);
                    formSpinnerIdeal.setVisibility(View.GONE);
                    digitSeekBarIdeal.setVisibility(View.GONE);
                    graphicCircleIdeal.setVisibility(View.GONE);
                    graphicSeekBarIdeal.setVisibility(View.GONE);
                    break;
                case 2:
                    idealStatusWay = "數字顯示";
                    textSpinnerIdeal.setVisibility(View.GONE);
                    formSpinnerIdeal.setVisibility(View.VISIBLE);
                    digitSeekBarIdeal.setVisibility(View.VISIBLE);
                    graphicCircleIdeal.setVisibility(View.GONE);
                    graphicSeekBarIdeal.setVisibility(View.GONE);
                    break;
                case 3:
                    idealStatusWay = "圖像顯示";
                    textSpinnerIdeal.setVisibility(View.GONE);
                    formSpinnerIdeal.setVisibility(View.VISIBLE);
                    digitSeekBarIdeal.setVisibility(View.GONE);
                    graphicCircleIdeal.setVisibility(View.VISIBLE);
                    graphicSeekBarIdeal.setVisibility(View.VISIBLE);
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    private class formSpinnerIdealListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if (i == 0) {
                idealStatusForm = "";
            } else {
                idealStatusForm = adapterView.getItemAtPosition(i).toString();
                Log.d(TAG, "idealStatusForm: " + idealStatusForm);
            }

        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    private class textStatusSpinnerIdealListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
            Log.d(TAG, "textStatusSpinnerIdeal position: " + position);
            if (position == 0) idealStatusString = "";
            else {
                idealStatusString = adapterView.getItemAtPosition(position).toString();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    private class digitSeekBarIdealListener implements OnSeekChangeListener {
        @Override
        public void onSeeking(SeekParams seekParams) {

        }

        @Override
        public void onStartTrackingTouch(IndicatorSeekBar indicatorSeekBar) {

        }

        @Override
        public void onStopTrackingTouch(IndicatorSeekBar indicatorSeekBar) {
            // 存值
            idealStatusRate = indicatorSeekBar.getProgress();
//            showStatusWay = Constants.PRESENT_IN_DIGIT;
        }
    }

    private class graphicSeekBarIdealListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            graphicCircleIdeal.setProgress(i);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // 存值
            idealStatusRate = seekBar.getProgress();
//            showStatusWay = Constants.PRESENT_IN_GRAPHIC;
        }
    }
}
