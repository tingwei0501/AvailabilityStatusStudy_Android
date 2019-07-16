package labelingStudy.nctu.minuku_2.manager;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.util.Random;

import labelingStudy.nctu.minuku.config.Constants;

public class renderManager {
    private static final String TAG = "renderManager";

    public static void renderStatus(TextView statusText, CircularProgressBar circle, TextView statusFormText,
                                    String statusForm, int status, String way,
                                    String textStatusText, int color) {

        if (way.equals(Constants.PRESENT_IN_TEXT)) {
            Log.d(TAG, "in text ");
            statusText.setText(textStatusText);
            statusText.setVisibility(View.VISIBLE);
            circle.setVisibility(View.INVISIBLE);
            statusFormText.setVisibility(View.GONE);
        } else if (way.equals(Constants.PRESENT_IN_DIGIT)) {
            if (statusForm.equals(Constants.STATUS_FORM_DISTURB)) status = 100 - status;
            Log.d(TAG, "in digit ");
            statusFormText.setVisibility(View.VISIBLE);
            statusFormText.setText(statusForm);
            statusText.setText(status + "%");
            statusText.setVisibility(View.VISIBLE);
            circle.setVisibility(View.INVISIBLE);
        } else if (way.equals(Constants.PRESENT_IN_GRAPHIC)) {
//            int color = Color.rgb(51, 102, 153);
            if (statusForm.equals(Constants.STATUS_FORM_DISTURB)) status = 100 - status;
            Log.d(TAG, "in graphic ");
            statusFormText.setVisibility(View.VISIBLE);
            statusFormText.setText(statusForm);
            circle.setProgressWithAnimation(Float.valueOf(status), Constants.PROGRESS_ANIMATION_RATE);
            circle.setColor(color);
            statusText.setVisibility(View.INVISIBLE);
            circle.setVisibility(View.VISIBLE);
        }
    }

    public static String randomPresentWay() {
        String[] array = {Constants.PRESENT_IN_TEXT, Constants.PRESENT_IN_DIGIT, Constants.PRESENT_IN_GRAPHIC};

        String presentWay = array[new Random().nextInt(array.length)];

        return presentWay;
    }

    public static String randomStatusForm() {
        String[] array = {Constants.STATUS_FORM_READ, Constants.STATUS_FORM_REPLY, Constants.STATUS_FORM_DISTURB};
        String statusForm = array[new Random().nextInt(array.length)];

        return statusForm;
    }

    public static int wayToIndex(String way) {
        if (way.equals(Constants.PRESENT_IN_TEXT)) return 0;
        else if (way.equals(Constants.PRESENT_IN_DIGIT)) return 1;
        else if (way.equals(Constants.PRESENT_IN_GRAPHIC)) return 2;
        else return -1;
    }

    public static String rateGetText(int rate) {
        if (rate >= 90) return "目前會回覆";
        else if (rate >= 70) return "回覆率高";
        else if (rate >= 60) return "有可能會回覆";
        else if (rate >= 40) return "回覆率中等";
        else if (rate >= 30) return "回覆率低";
        else if (rate >= 20) return "可能不會回覆";
        else if (rate >= 10) return "目前不會回覆";
        else return "目前不會看訊息";
    }

    public static int statusGetRate(String status) {

        if (status.equals("回覆率高")) return 82;//
        else if (status.equals("回覆率中等")) return 56;//
        else if (status.equals("回覆率低")) return 32;
        else if (status.equals("目前會回覆")) return 90;//
        else if (status.equals("可能不會回覆")) return 20;
        else if (status.equals("有可能會回覆")) return 65;//
        else if (status.equals("目前不會回覆")) return 11;
        else if (status.equals("目前不會看訊息")) return 9;
        else if (status.equals("忙碌中")) return 34;
        else if (status.equals("開會中")) return 52;
        else if (status.equals("上線中")) return 94;
        else if (status.equals("有空")) return 92;
        else if (status.equals("請勿打擾")) return 5;
        else if (status.equals("歡迎打擾")) return 95;
        else return 0;
    }

    public static int statusGetIndex(String status) {

        if (status.equals("回覆率高")) return 0;//
        else if (status.equals("回覆率中等")) return 1;//
        else if (status.equals("回覆率低")) return 2;
        else if (status.equals("目前會回覆")) return 3;//
        else if (status.equals("可能不會回覆")) return 5;
        else if (status.equals("有可能會回覆")) return 4;//
        else if (status.equals("目前不會回覆")) return 6;
        else if (status.equals("目前不會看訊息")) return 7;
        else if (status.equals("忙碌中")) return 8;
        else if (status.equals("開會中")) return 9;
        else if (status.equals("上線中")) return 10;
        else if (status.equals("請勿打擾")) return 11;
        else if (status.equals("有空")) return 12;
        else if (status.equals("歡迎打擾")) return 13;
        else return -1;
    }

    public static int formGetIndex(String form) {
        if (form.equals(Constants.STATUS_FORM_REPLY)) return 0;
        else if (form.equals(Constants.STATUS_FORM_READ)) return 1;
        else if (form.equals(Constants.STATUS_FORM_DISTURB)) return 2;
        else return -1;
    }
}
