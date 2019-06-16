package labelingStudy.nctu.minuku_2.manager;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.util.Random;

import labelingStudy.nctu.minuku.config.Constants;

public class renderManager {
    private static final String TAG = "renderManager";

    public static void renderStatus(TextView statusText, CircularProgressBar circle,
                                    int status, String way, String textStatusText, int color) {

//        updateTimeText.setText(ScheduleAndSampleManager.getMinSecStringFromMillis(time));

        if (way.equals(Constants.PRESENT_IN_TEXT)) {
            Log.d(TAG, "in text ");
            statusText.setText(textStatusText);
            statusText.setVisibility(View.VISIBLE);
            circle.setVisibility(View.INVISIBLE);
        } else if (way.equals(Constants.PRESENT_IN_DIGIT)) {
            Log.d(TAG, "in digit ");
            statusText.setText("回覆率" + status + "%");
            statusText.setVisibility(View.VISIBLE);
            circle.setVisibility(View.INVISIBLE);
        } else if (way.equals(Constants.PRESENT_IN_GRAPHIC)) {
//            int color = Color.rgb(51, 102, 153);
//            Log.d(TAG, "color: " + color);
            Log.d(TAG, "in graphic ");
            circle.setProgressWithAnimation(Float.valueOf(status), Constants.PROGRESS_ANIMATION_RATE);
            circle.setColor(color);
//            selfStatusProgressBar.setBackgroundColor(circleProgressBackgroundColor);
            statusText.setVisibility(View.INVISIBLE);
            circle.setVisibility(View.VISIBLE);
        }
    }

    public static String randomPresentWay() {
        String[] array = {Constants.PRESENT_IN_TEXT, Constants.PRESENT_IN_DIGIT, Constants.PRESENT_IN_GRAPHIC};

        String presentWay = array[new Random().nextInt(array.length)];

        return presentWay;
    }

    public static String statusGetText(int userStatus) {
        if (userStatus >= 90) {
            String[] arr = {"回覆率極高", "很有可能會回覆"};
            return arr[new Random().nextInt(arr.length)];
        } else if (userStatus >= 70) {
            String[] arr = {"回覆率高", "可能會回覆"};
            return arr[new Random().nextInt(arr.length)];
        } else if (userStatus >= 55) {
            String[] arr = {"回覆率中偏高", "有可能會回覆"};
            return arr[new Random().nextInt(arr.length)];
        } else if (userStatus >= 45) {
            String[] arr = {"回覆率中", "有可能不會回覆"};
            return arr[new Random().nextInt(arr.length)];
        } else if (userStatus >= 30) {
            String[] arr = {"回覆率中偏低", "可能不會回覆"};
            return arr[new Random().nextInt(arr.length)];
        } else if (userStatus >= 20) {
            String[] arr = {"回覆率低", "應該不會回覆"};
            return arr[new Random().nextInt(arr.length)];
        } else {
            String[] arr = {"回覆率極低", "不太可能會回覆"};
            return arr[new Random().nextInt(arr.length)];
        }
    }

    public static int statusGetRate(String status) {
        if (status.equals("回覆率高")) return 82;
        else if (status.equals("回覆率中等")) return 56;
        else if (status.equals("回覆率低")) return 32;
        else if (status.equals("可能不會回覆")) return 20;
        else if (status.equals("有可能會回覆")) return 65;
        else if (status.equals("忙碌中")) return 13;
        else if (status.equals("有空")) return 92;
        else if (status.equals("未知")) return 0;
        else return 0;
    }
}
