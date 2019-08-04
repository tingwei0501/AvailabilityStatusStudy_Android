package labelingStudy.nctu.minuku_2.Questionnaire;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
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

import static labelingStudy.nctu.minuku_2.manager.renderManager.formGetIndex;
import static labelingStudy.nctu.minuku_2.manager.renderManager.randomStatusForm;
import static labelingStudy.nctu.minuku_2.manager.renderManager.rateGetText;
import static labelingStudy.nctu.minuku_2.manager.renderManager.renderStatus;
import static labelingStudy.nctu.minuku_2.manager.renderManager.statusGetIndex;
import static labelingStudy.nctu.minuku_2.manager.renderManager.statusGetRate;
import static labelingStudy.nctu.minuku_2.manager.renderManager.wayToIndex;


public class SelfQuestionnaire extends Activity {
    private static final String TAG = "SelfQuestionnaire";
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
    private String idealStatueWay;
    private String showStatusWay = "";
    private String idealStatusForm = "";
    private String showStatusForm = "";
    private String showStatusDifferentOtherReason = "";
    private int idealStatusRate;
    private int showStatusRate = -1; // digit or graphic
    private String showStatusString = ""; // text
    private String idealStatusString = "";
    private String showDifferentReason = "";
    private ArrayList<String> showToWhoArray = new ArrayList<>();
    private ArrayList<String> blockToWhoArray = new ArrayList<>();

    // show your status
    private TextView time;
    private CircularProgressBar selfStatusCircle;
    private TextView selfStatusText;
    private TextView selfStatusFormText;
    private String selfStatusWay = "";
    private String selfStatusForm = "";
    private String selfStatusString;
    private int selfStatusRate;
    private int selfStatusColor;
    private long showTime;

    // context: whom, where, what
    private Spinner contextWhomSpinner;
    private Spinner contextWhereSpinner;
    private Spinner contextWhatSpinner;
    private EditText contextWhomEditText;
    private EditText contextWhereEditText;
    private EditText contextWhatEditText;
    //
    // Q3: 實際狀態如何
    private Spinner waySpinnerIdeal;
    private Spinner formSpinnerIdeal;
    private Spinner textSpinnerIdeal;
    private IndicatorSeekBar digitSeekBarIdeal;
    private CircularProgressBar graphicCircleIdeal;
    private SeekBar graphicSeekBarIdeal;
    //
    //Q5: 想要怎麼呈現給聯絡人看
    private CheckBox asIdealStatusCheckBox;
    private Spinner waySpinnerShow;
    private Spinner formSpinnerShow;
    private Spinner textSpinnerShow;
    private IndicatorSeekBar digitSeekBarShow;
    private CircularProgressBar graphicCircleShow;
    private SeekBar graphicSeekBarShow;
    //
    // Q6: 想呈現的跟系統給的不同: 理由
    private boolean idealShowDifferent = true;
    private ConstraintLayout showStatusDifferentReasonLayout;
//    private EditText showStatusDifferentEditText;
    private CheckBox showStatusDifferentLessBother;
    private CheckBox showStatusDifferentDonotBother;
    private CheckBox showStatusDifferentBother;
    private CheckBox showStatusDifferentAccurate;
    private CheckBox showStatusDifferentBlurred;
    private CheckBox showStatusDifferentHide;
    private CheckBox showStatusDifferentOther;
    private EditText showStatusDifferentOtherText;
    //
    // Q7 特別想給誰看到嗎 & Q8 不想給誰看到
    private ArrayList<String> contactList;
    private ListView showToWho;
    private EditText showToWhoOtherEditText;
    private ListView blockToWho;
    private EditText blockToWhoOtherEditText;
    //
    // Q9: 一句話形容
//    private EditText mySituationEditText;

    private Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.self_questionnaire);
        mContext = getApplicationContext();
        sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        sharedPreferences_contact = getSharedPreferences(Constants.sharedPrefString_ContactList, MODE_PRIVATE);

        mInputMethodManager = (InputMethodManager) getSystemService(mContext.INPUT_METHOD_SERVICE);

        time = findViewById(R.id.self_questionnaire_time);
        selfStatusCircle = findViewById(R.id.self_questionnaire_circle);
        selfStatusText = findViewById(R.id.self_questionnaire_rate);
        selfStatusFormText = findViewById(R.id.self_questionnaire_statusForm);
        // context: whom
        contextWhomSpinner = findViewById(R.id.self_context_whom);
        ArrayAdapter<CharSequence> contextWhomLunchList = ArrayAdapter.createFromResource(SelfQuestionnaire.this,
                R.array.contextWhom,
                android.R.layout.simple_spinner_dropdown_item);
        contextWhomSpinner.setAdapter(contextWhomLunchList);
        contextWhomSpinner.setOnItemSelectedListener(new contextWhomSpinnerListener());
        contextWhomEditText = findViewById(R.id.self_context_whom_other);
        contextWhomEditText.setOnEditorActionListener(new contextWhomOtherListener());
        // context: where
        contextWhereSpinner = findViewById(R.id.self_context_where);
        ArrayAdapter<CharSequence> contextWhereLunchList = ArrayAdapter.createFromResource(SelfQuestionnaire.this,
                R.array.contextWhere,
                android.R.layout.simple_spinner_dropdown_item);
        contextWhereSpinner.setAdapter(contextWhereLunchList);
        contextWhereSpinner.setOnItemSelectedListener(new contextWhereSpinnerListener());
        contextWhereEditText = findViewById(R.id.self_context_where_other);
        contextWhereEditText.setOnEditorActionListener(new contextWhereOtherListener());
        // context: what
        contextWhatSpinner = findViewById(R.id.self_context_what);
        ArrayAdapter<CharSequence> contextWhatLunchList = ArrayAdapter.createFromResource(SelfQuestionnaire.this,
                R.array.contextWhat,
                android.R.layout.simple_spinner_dropdown_item);
        contextWhatSpinner.setAdapter(contextWhatLunchList);
        contextWhatSpinner.setOnItemSelectedListener(new contextWhatSpinnerListener());
        contextWhatEditText = findViewById(R.id.self_context_what_other);
        contextWhatEditText.setOnEditorActionListener(new contextWhatOtherListener());
        //
        // Q3 3. 實際狀態為如何?
        waySpinnerIdeal = findViewById(R.id.way_spinner_ideal);
        ArrayAdapter<CharSequence> wayLunchListIdeal = ArrayAdapter.createFromResource(SelfQuestionnaire.this,
                R.array.showContactWay,
                android.R.layout.simple_spinner_dropdown_item);
        waySpinnerIdeal.setAdapter(wayLunchListIdeal);
        waySpinnerIdeal.setOnItemSelectedListener(new presentWaySpinnerIdealListener());

        formSpinnerIdeal = findViewById(R.id.form_spinner_ideal);
        ArrayAdapter<CharSequence> formLunchListIdeal = ArrayAdapter.createFromResource(SelfQuestionnaire.this,
                R.array.showContactForm,
                android.R.layout.simple_spinner_dropdown_item);
        formSpinnerIdeal.setAdapter(formLunchListIdeal);
        formSpinnerIdeal.setOnItemSelectedListener(new formSpinnerIdealListener());

        textSpinnerIdeal = findViewById(R.id.textStatus_spinner_ideal);
        ArrayAdapter<CharSequence> textLunchListIdeal = ArrayAdapter.createFromResource(SelfQuestionnaire.this,
                R.array.textStatus,
                android.R.layout.simple_spinner_dropdown_item);
        textSpinnerIdeal.setAdapter(textLunchListIdeal);
        textSpinnerIdeal.setOnItemSelectedListener(new textStatusSpinnerIdealListener());

        digitSeekBarIdeal = findViewById(R.id.digitStatus_seekbar_ideal);
        digitSeekBarIdeal.setOnSeekChangeListener(new digitSeekBarIdealListener());

        graphicCircleIdeal = findViewById(R.id.graphStatus_circle_ideal);
        graphicCircleIdeal.setColor(Constants.DEFAULT_COLOR);
        graphicSeekBarIdeal = findViewById(R.id.graphStatus_circleSeekBar_ideal);
        graphicSeekBarIdeal.setOnSeekBarChangeListener(new graphicSeekBarIdealListener());
        //

        // Q5: 請問你想要怎麼呈現狀態給聯絡人看?
        asIdealStatusCheckBox = findViewById(R.id.asIdealStatusCheckbox);
        asIdealStatusCheckBox.setOnCheckedChangeListener(new asIdealStatusListener());
        waySpinnerShow = findViewById(R.id.way_spinner_show);
        ArrayAdapter<CharSequence> wayLunchListShow = ArrayAdapter.createFromResource(SelfQuestionnaire.this,
                R.array.showContactWay_withhint,
                android.R.layout.simple_spinner_dropdown_item);
        waySpinnerShow.setAdapter(wayLunchListShow);
        waySpinnerShow.setOnItemSelectedListener(new presentWaySpinnerShowListener());

        formSpinnerShow = findViewById(R.id.form_spinner_show);
        ArrayAdapter<CharSequence> formLunchListShow = ArrayAdapter.createFromResource(SelfQuestionnaire.this,
                R.array.showContactForm_withhint,
                android.R.layout.simple_spinner_dropdown_item);
        formSpinnerShow.setAdapter(formLunchListShow);
        formSpinnerShow.setOnItemSelectedListener(new formSpinnerShowListener());

        textSpinnerShow = findViewById(R.id.textStatus_spinner_show);
        ArrayAdapter<CharSequence> textLunchListShow = ArrayAdapter.createFromResource(SelfQuestionnaire.this,
                R.array.textStatus_withhint,
                android.R.layout.simple_spinner_dropdown_item);
        textSpinnerShow.setAdapter(textLunchListShow);
        textSpinnerShow.setOnItemSelectedListener(new textStatusSpinnerShowListener());

        digitSeekBarShow = findViewById(R.id.digitStatus_seekbar_show);
        digitSeekBarShow.setOnSeekChangeListener(new digitSeekBarShowListener());

        graphicCircleShow = findViewById(R.id.graphStatus_circle_show);
        graphicCircleShow.setColor(Constants.DEFAULT_COLOR);
        graphicSeekBarShow = findViewById(R.id.graphStatus_circleSeekBar_show);
        graphicSeekBarShow.setOnSeekBarChangeListener(new graphicSeekBarShowListener());
        //
        // Q6: 想呈現的跟提供的不同: 為甚麼?
        showStatusDifferentReasonLayout = findViewById(R.id.showStatusDifferentReasonLayout);
//        showStatusDifferentEditText = findViewById(R.id.showStatusDifferent_editText);
//        showStatusDifferentEditText.setOnEditorActionListener(new showStatusDifferentListener());
        showStatusDifferentLessBother = findViewById(R.id.showDifferent_less_bother);
        showStatusDifferentDonotBother = findViewById(R.id.showDifferent_donot_bother);
        showStatusDifferentBother = findViewById(R.id.showDifferent_bother);
        showStatusDifferentAccurate = findViewById(R.id.showDifferent_accurate);
        showStatusDifferentBlurred = findViewById(R.id.showDifferent_blurred);
        showStatusDifferentHide = findViewById(R.id.showDifferent_hide);
        showStatusDifferentOther = findViewById(R.id.showDifferent_other);
        showStatusDifferentOtherText = findViewById(R.id.showDifferent_otherText);
        showStatusDifferentOtherText.setOnEditorActionListener(new showStatusDifferentEditListener());
        // Q7 Q8
        initContact();
        showToWhoOtherEditText = findViewById(R.id.self_showToWho_other);
        showToWhoOtherEditText.setOnEditorActionListener(new showToWhoOtherListener());
        showToWho = findViewById(R.id.self_showToWho_list);
        showToWho.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        ArrayAdapter contact1 = new ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice,
                contactList);
        showToWho.setOnItemClickListener(new showToWhoListener());
        showToWho.setAdapter(contact1);

        blockToWhoOtherEditText = findViewById(R.id.self_blockToWho_other);
        blockToWhoOtherEditText.setOnEditorActionListener(new blockToWhoOtherListener());
        blockToWho = findViewById(R.id.self_blockToWho_list);
        blockToWho.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        blockToWho.setOnItemClickListener(new blockToWhoListener());
        blockToWho.setAdapter(contact1);
        // Q9 一句話
//        mySituationEditText = findViewById(R.id.self_editText);
//        mySituationEditText.setOnEditorActionListener(new mySituationListener());
        //
        // submit button
        submit = findViewById(R.id.self_questionnaire_submit);
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
        try {
            Bundle bundle = getIntent().getExtras();
            showTime = bundle.getLong("showTime", 0);
            selfStatusRate = bundle.getInt("status", 0);
            selfStatusWay = bundle.getString("way", "NA");
            selfStatusColor = bundle.getInt("color", 0);
            selfStatusString = bundle.getString("statusString", "NA");
            selfStatusForm = bundle.getString("statusForm", "NA");
//            Log.d(TAG, "in bundle");
            Log.d(TAG, "in bundle selfStatusWay: " + selfStatusWay);
            Log.d(TAG, "in bundle selfStatusRate: " + selfStatusRate);

        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.d(TAG, "not from notification.");
            showTime = sharedPreferences.getLong("updateTime", 0);
            selfStatusRate = sharedPreferences.getInt("status", 0);
            selfStatusWay = sharedPreferences.getString("way", "NA");
            selfStatusForm = sharedPreferences.getString("statusForm", "NA");
            selfStatusColor = sharedPreferences.getInt("statusColor", -1);
            selfStatusString = sharedPreferences.getString("statusText", "NA");
        }
        if (showTime != 0) {
            // has data (from notification)
            //顯示在畫面上
            time.setText(ScheduleAndSampleManager.getMinSecStringFromMillis(showTime));
            renderStatus(selfStatusText, selfStatusCircle, selfStatusFormText, selfStatusForm,
                    selfStatusRate, selfStatusWay, selfStatusString, selfStatusColor);
            // 會不會直接根據這個預設值 顯示文字或是數字seekBar OK
            //
            waySpinnerIdeal.setSelection(wayToIndex(selfStatusWay));
            if (selfStatusWay.equals(Constants.PRESENT_IN_TEXT)) {
                textSpinnerIdeal.setSelection(statusGetIndex(selfStatusString));
            } else {
                int showRate = selfStatusRate;
                if (selfStatusForm.equals(Constants.STATUS_FORM_DISTURB)) {
                    showRate = 100 - showRate;
                }
                formSpinnerIdeal.setSelection(formGetIndex(selfStatusForm));
                digitSeekBarIdeal.setProgress(showRate);
                graphicCircleIdeal.setProgress(showRate);
                graphicSeekBarIdeal.setProgress(showRate);
                idealStatusForm = selfStatusForm;
            }
            idealStatueWay = selfStatusWay;
            idealStatusString = selfStatusString;
            idealStatusRate = selfStatusRate;
            Log.d(TAG, "onStart idealStatueWay: " + idealStatueWay);
            Log.d(TAG, "onStart idealStatusRate: " + idealStatusRate);
        }
    }

    private Button.OnClickListener submitListener = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {
            Log.d(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            Log.d(TAG, "showStatusForm " + showStatusForm);
            Log.d(TAG, "idealStatusForm "+ idealStatusForm);

            long currentTime = ScheduleAndSampleManager.getCurrentTimeInMillis();
            boolean pass = false;
            JSONObject data = new JSONObject();
            JSONArray showArr = new JSONArray();
            JSONArray blockArr = new JSONArray();

            selectedWhomOther = contextWhomEditText.getText().toString();
            selectedLocationOther = contextWhereEditText.getText().toString();
            selectedActivityOther = contextWhatEditText.getText().toString();

            if ((!selectedWhom.equals("") || !selectedWhomOther.equals("")) &&
                (!selectedLocation.equals("") || !selectedLocationOther.equals("")) &&
                (!selectedActivity.equals("") || !selectedActivityOther.equals(""))) {
                try {
                    data.put("user_id", sharedPreferences.getString("id", ""));
                    data.put("createdTime", showTime);
                    data.put("createdTimeString", ScheduleAndSampleManager.getTimeString(showTime));
                    data.put("completeTimeString", ScheduleAndSampleManager.getTimeString(currentTime));
                    data.put("completeTime", currentTime);
                    data.put("selectedWhomOther", selectedWhomOther);
                    data.put("selectedWhom", selectedWhom);
                    data.put("selectedLocation", selectedLocation);
                    data.put("selectedLocationOther", selectedLocationOther);
                    data.put("selectedActivity", selectedActivity);
                    data.put("selectedActivityOther", selectedActivityOther);

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
                    pass = true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            try {
                idealStatueWay = waySpinnerIdeal.getSelectedItem().toString();
                data.put("idealStatueWay", idealStatueWay);
                // ideal
                if (idealStatueWay.equals("文字顯示")) {
                    idealStatusString = textSpinnerIdeal.getSelectedItem().toString();
                    data.put("idealStatusForm", "NA");
                    data.put("idealStatusString", idealStatusString);
                    data.put("idealStatusRate", statusGetRate(idealStatusString));
                } else {
                    Log.d(TAG, ">> 00 >> idealStatusRate " + idealStatusRate);
                    idealStatusForm = formSpinnerIdeal.getSelectedItem().toString();
                    data.put("idealStatusForm", idealStatusForm);
                    data.put("idealStatusString", rateGetText(idealStatusRate));
                    data.put("idealStatusRate", idealStatusRate);
                }
                Log.d(TAG, ">>1>> idealStatueWay: " + idealStatueWay);
                Log.d(TAG, ">>1>> idealStatusForm: " + idealStatusForm);
                Log.d(TAG, ">>1>> idealStatusRate: " + idealStatusRate);
                Log.d(TAG, ">>1>> idealStatusString: " + idealStatusString);

                data.put("idealShowDifferent", idealShowDifferent);

                if (idealShowDifferent) {  // 沒有勾: 不同於實際狀態，需要抓取spinner的值 去顯示
                    showStatusWay = waySpinnerShow.getSelectedItem().toString();
                    data.put("showStatusWay", showStatusWay);

                    if (showStatusWay.equals("文字顯示")) {
                        if (!showStatusString.equals("")) {
                            showStatusString = textSpinnerShow.getSelectedItem().toString();
                            data.put("showStatusForm", "NA");
                            data.put("showStatusString", showStatusString);
                            data.put("showStatusRate", statusGetRate(showStatusString));

                            sharedPreferences.edit()
                                    .putString("way", "text")
                                    .putString("statusForm", randomStatusForm())  //TODO: 7/20 改 測試Contact Questionnaire會不會呈現"NA"
                                    .putInt("status", statusGetRate(showStatusString))
                                    .putString("statusText", showStatusString)
                                    .putLong("updateTime", currentTime)
                                    .putInt("statusColor", Constants.DEFAULT_COLOR)
                                    .commit();
                            // 確保前面有通過，才能給true //
                            if (pass) pass = true;
                        } else pass = false;
                    } else {
                        if (showStatusRate != -1 && !showStatusForm.equals("")) {
                            Log.d(TAG, "showStatusRate~~~ 0: " + showStatusRate);
                            showStatusForm = formSpinnerShow.getSelectedItem().toString();
                            if (showStatusForm.equals(Constants.STATUS_FORM_DISTURB)) {
                                showStatusRate = 100 - showStatusRate;
                            }
                            Log.d(TAG, "showStatusRate~~~ 1: " + showStatusRate);
                            if (showStatusWay.equals("數字顯示")) {
//                                showStatusRate = digitSeekBarShow.getProgress();
                                sharedPreferences.edit()
                                        .putString("way", "digit")
                                        .commit();
                            } else {
//                                showStatusRate = graphicSeekBarShow.getProgress();
                                sharedPreferences.edit()
                                        .putString("way", "graphic")
                                        .commit();
                            }
                            sharedPreferences.edit()
                                    .putString("statusForm", showStatusForm)
                                    .putInt("status", showStatusRate)
                                    .putString("statusText", rateGetText(showStatusRate))
                                    .putLong("updateTime", currentTime)
                                    .putInt("statusColor", Constants.DEFAULT_COLOR)
                                    .commit();
                            if (pass) pass = true;
                            data.put("showStatusForm", showStatusForm);
                            data.put("showStatusString", rateGetText(showStatusRate));
                            data.put("showStatusRate", showStatusRate);
                        } else pass = false;
                    }
                    Log.d(TAG, "showStatusForm~ " + showStatusForm);
                    Log.d(TAG, "showStatusWay~ " + showStatusWay);
                    Log.d(TAG, "showStatusRate~ " + showStatusRate);
                    Log.d(TAG, "showStatusString~ " + showStatusString);

                    // 存文字欄
//                    showDifferentReason = showStatusDifferentEditText.getText().toString();
//                    if (!showDifferentReason.equals("")) {
//                        data.put("showDifferentReason", showDifferentReason);
//                        if (pass) pass = true;
//                    } else pass = false;
                    // save show different reasons: CheckBox
                    JSONArray showStatusDifferentReasonArr = new JSONArray();
                    CheckBox[] id = {showStatusDifferentLessBother, showStatusDifferentDonotBother,
                            showStatusDifferentBother, showStatusDifferentAccurate,
                             showStatusDifferentBlurred, showStatusDifferentHide, showStatusDifferentOther};
                    boolean atLeastOneReason = false;
                    for (CheckBox i:id) {
                        if (i.isChecked()) {
                            showStatusDifferentReasonArr.put(true);
                            atLeastOneReason = true;
                        }
                        else showStatusDifferentReasonArr.put(false);
                    }
                    showStatusDifferentOtherReason = showStatusDifferentOtherText.getText().toString();
                    if (!atLeastOneReason && showStatusDifferentOtherReason.equals("")) pass = false;

                    data.put("showStatusDifferentOtherReason", showStatusDifferentOtherReason);
                    Log.d(TAG, "other reason: " + showStatusDifferentOtherReason);

                    data.put("showStatusDifferentReasons", showStatusDifferentReasonArr);
                } else {
                    // 直接顯示ideal的狀態
                    if (idealStatueWay.equals("文字顯示")) {
                        sharedPreferences.edit()
                                .putString("way", "text")
                                .putString("statusForm", randomStatusForm())  //TODO: 7/20 改
                                .putInt("status", statusGetRate(idealStatusString))
                                .putString("statusText", idealStatusString)
                                .putLong("updateTime", currentTime)
                                .putInt("statusColor", -1)
                                .commit();
                    } else {
                        if (idealStatueWay.equals("數字顯示")) {
                            sharedPreferences.edit()
                                    .putString("way", "digit")
                                    .commit();
                        } else {
                            sharedPreferences.edit()
                                    .putString("way", "graphic")
                                    .commit();
                        }
                        sharedPreferences.edit()
                                .putString("statusForm", idealStatusForm)
                                .putInt("status", idealStatusRate)
                                .putString("statusText", rateGetText(idealStatusRate))
                                .putLong("updateTime", currentTime)
                                .putInt("statusColor", Constants.DEFAULT_COLOR)
                                .commit();
                    }
                    Log.d(TAG, ">>2>> idealStatueWay: " + idealStatueWay);
                    Log.d(TAG, ">>2>> idealStatusForm: " + idealStatusForm);
                    Log.d(TAG, ">>2>> idealStatusRate: " + idealStatusRate);
                    Log.d(TAG, ">>2>> idealStatusString: " + idealStatusString);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (pass) {
                // send to database
                // collect user 當前狀態(通知送出時的狀態)
                try {
                    data.put("status", selfStatusRate);
                    data.put("presentWay", selfStatusWay);
                    data.put("statusForm", selfStatusForm);
                    data.put("statusText", selfStatusString);
                    data.put("statusColor", selfStatusColor);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                mQueue = Volley.newRequestQueue(mContext);
                mJsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Constants.storeSelfQuestionnaire, data,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d(TAG, response.toString());
                                // start activity here
                                try {
                                    if (response.getString("response").equals("success insert")) {
                                        // 填問卷後，暫時不改狀態(5 min)
                                        BackgroundService.isHandleAfterEdit = true;
                                        BackgroundService.afterEditTimePeriod = 5;
                                        sharedPreferences.edit().putBoolean("afterEdit", true).commit();
                                        Intent intent = new Intent();
                                        intent.setClass(SelfQuestionnaire.this, ContactList.class);
                                        Toast.makeText(SelfQuestionnaire.this,
                                                "感謝您填寫問卷!", Toast.LENGTH_SHORT).show();
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                        startActivity(intent);
                                        SelfQuestionnaire.this.finish();
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
                Toast.makeText(SelfQuestionnaire.this,
                        "請每題都要填寫!", Toast.LENGTH_SHORT).show();
            }
        }
    };

    // show
    private class asIdealStatusListener implements CheckBox.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (asIdealStatusCheckBox.isChecked()) {
                idealShowDifferent = false;
                showStatusDifferentReasonLayout.setVisibility(View.GONE);
                reviseStatusOrNotShow(false);
            } else {
                // 與實際不同
                idealShowDifferent = true;
                reviseStatusOrNotShow(true);
                showStatusDifferentReasonLayout.setVisibility(View.VISIBLE);
            }
        }
    }
    // show
    private void reviseStatusOrNotShow(boolean revise) {
        waySpinnerShow.setEnabled(revise);
        textSpinnerShow.setEnabled(revise);
        digitSeekBarShow.setEnabled(revise);
        graphicSeekBarShow.setEnabled(revise);
        formSpinnerShow.setEnabled(revise);
    }

    // Ideal
    private class presentWaySpinnerIdealListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            Log.d(TAG, "presentWaySpinnerIdeal position: " + position);
            switch (position) {
                case 0: // 文字
                    idealStatueWay = "文字顯示";
                    textSpinnerIdeal.setVisibility(View.VISIBLE);
                    formSpinnerIdeal.setVisibility(View.GONE);
                    digitSeekBarIdeal.setVisibility(View.GONE);
                    graphicCircleIdeal.setVisibility(View.GONE);
                    graphicSeekBarIdeal.setVisibility(View.GONE);
                    break;
                case 1: // 數字
                    idealStatueWay = "數字顯示";
                    textSpinnerIdeal.setVisibility(View.GONE);
                    digitSeekBarIdeal.setVisibility(View.VISIBLE);
                    formSpinnerIdeal.setVisibility(View.VISIBLE);
                    graphicCircleIdeal.setVisibility(View.GONE);
                    graphicSeekBarIdeal.setVisibility(View.GONE);
                    break;
                case 2:
                    idealStatueWay = "圖像顯示";
                    textSpinnerIdeal.setVisibility(View.GONE);
                    digitSeekBarIdeal.setVisibility(View.GONE);
                    formSpinnerIdeal.setVisibility(View.VISIBLE);
                    graphicCircleIdeal.setVisibility(View.VISIBLE);
                    graphicSeekBarIdeal.setVisibility(View.VISIBLE);
                    break;
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
            idealStatusString = textSpinnerIdeal.getSelectedItem().toString();
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
            if (idealStatusForm.equals(Constants.STATUS_FORM_DISTURB)) {
                idealStatusRate = 100 - indicatorSeekBar.getProgress();
            } else {
                idealStatusRate = indicatorSeekBar.getProgress();
            }
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
            if (idealStatusForm.equals(Constants.STATUS_FORM_DISTURB)) {
                idealStatusRate = 100 - seekBar.getProgress();
            } else {
                idealStatusRate = seekBar.getProgress();
            }
        }
    }

    // show
    private class presentWaySpinnerShowListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            Log.d(TAG, "presentWaySpinnerShow position: " + position);
            switch (position) {
                case 0:
                    showStatusWay = "";
                    break;
                case 1: // 文字
                    showStatusWay = Constants.PRESENT_IN_TEXT;
                    textSpinnerShow.setVisibility(View.VISIBLE);
                    formSpinnerShow.setVisibility(View.GONE);
                    digitSeekBarShow.setVisibility(View.GONE);
                    graphicCircleShow.setVisibility(View.GONE);
                    graphicSeekBarShow.setVisibility(View.GONE);
                    break;
                case 2: // 數字
                    showStatusWay = Constants.PRESENT_IN_DIGIT;
                    textSpinnerShow.setVisibility(View.GONE);
                    digitSeekBarShow.setVisibility(View.VISIBLE);
                    formSpinnerShow.setVisibility(View.VISIBLE);
                    graphicCircleShow.setVisibility(View.GONE);
                    graphicSeekBarShow.setVisibility(View.GONE);
                    break;
                case 3:
                    showStatusWay = Constants.PRESENT_IN_GRAPHIC;
                    textSpinnerShow.setVisibility(View.GONE);
                    digitSeekBarShow.setVisibility(View.GONE);
                    formSpinnerShow.setVisibility(View.VISIBLE);
                    graphicCircleShow.setVisibility(View.VISIBLE);
                    graphicSeekBarShow.setVisibility(View.VISIBLE);
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    private class textStatusSpinnerShowListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
            Log.d(TAG, "textStatusSpinnerShow position: " + position);
            if (position == 0) showStatusString = "";
            else {
                showStatusString = adapterView.getItemAtPosition(position).toString();
                showStatusWay = Constants.PRESENT_IN_TEXT;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    private class digitSeekBarShowListener implements OnSeekChangeListener {
        @Override
        public void onSeeking(SeekParams seekParams) {

        }

        @Override
        public void onStartTrackingTouch(IndicatorSeekBar indicatorSeekBar) {

        }

        @Override
        public void onStopTrackingTouch(IndicatorSeekBar indicatorSeekBar) {
            // 存值
            showStatusRate = indicatorSeekBar.getProgress();
            showStatusWay = Constants.PRESENT_IN_DIGIT;
        }
    }

    private class graphicSeekBarShowListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            graphicCircleShow.setProgress(i);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // 存值
            showStatusRate = seekBar.getProgress();
            showStatusWay = Constants.PRESENT_IN_GRAPHIC;
        }
    }

    private class showStatusDifferentEditListener implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            mInputMethodManager.hideSoftInputFromWindow(SelfQuestionnaire.this.getWindow().getDecorView().getRootView().getWindowToken(), 0);
//            showStatusDifferentOtherReason = showStatusDifferentOtherText.getText().toString();
            showStatusDifferentOtherText.clearFocus();
            return false;
        }
    }

//    private class mySituationListener implements TextView.OnEditorActionListener {
//        @Override
//        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
//            mySituationEditText.clearFocus();
//            mInputMethodManager.hideSoftInputFromWindow(SelfQuestionnaire.this.getWindow().getDecorView().getRootView().getWindowToken(), 0);
//            return false;
//        }
//    }

    private class showToWhoListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            ArrayList<String> selectedContactToShow = new ArrayList<>();
            SparseBooleanArray checkedItems = showToWho.getCheckedItemPositions();
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
            mInputMethodManager.hideSoftInputFromWindow(SelfQuestionnaire.this.getWindow().getDecorView().getRootView().getWindowToken(), 0);

            return false;
        }
    }

    private class contextWhereOtherListener implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            contextWhereEditText.clearFocus();
            mInputMethodManager.hideSoftInputFromWindow(SelfQuestionnaire.this.getWindow().getDecorView().getRootView().getWindowToken(), 0);
            return false;
        }
    }

    private class contextWhatOtherListener implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            contextWhatEditText.clearFocus();
            mInputMethodManager.hideSoftInputFromWindow(SelfQuestionnaire.this.getWindow().getDecorView().getRootView().getWindowToken(), 0);
            return false;
        }
    }

    private class showToWhoOtherListener implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            showToWhoOtherEditText.clearFocus();
            mInputMethodManager.hideSoftInputFromWindow(SelfQuestionnaire.this.getWindow().getDecorView().getRootView().getWindowToken(), 0);
            return false;
        }
    }

    private class blockToWhoOtherListener implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            blockToWhoOtherEditText.clearFocus();
            mInputMethodManager.hideSoftInputFromWindow(SelfQuestionnaire.this.getWindow().getDecorView().getRootView().getWindowToken(), 0);
            return false;
        }
    }

    private class formSpinnerIdealListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            idealStatusForm = adapterView.getItemAtPosition(i).toString();
            if (idealStatusForm.equals(Constants.STATUS_FORM_DISTURB)) {
                if (idealStatueWay.equals("數字顯示")) {
                    idealStatusRate = 100 - digitSeekBarIdeal.getProgress();
                } else {
                    idealStatusRate = 100 - graphicSeekBarIdeal.getProgress();
                }

            }
            Log.d(TAG, "idealStatusForm: " + idealStatusForm);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    private class formSpinnerShowListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if (i == 0) {
                showStatusForm = "";
            } else {
                showStatusForm = adapterView.getItemAtPosition(i).toString();
                Log.d(TAG, "showStatusForm: " + showStatusForm);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    private class showStatusDifferentListener implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            textView.clearFocus();
            mInputMethodManager.hideSoftInputFromWindow(SelfQuestionnaire.this.getWindow().getDecorView().getRootView().getWindowToken(), 0);
            return false;
        }
    }
}
