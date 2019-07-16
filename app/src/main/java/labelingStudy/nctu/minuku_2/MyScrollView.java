package labelingStudy.nctu.minuku_2;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class MyScrollView extends ScrollView {
    public MyScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // 兩種方法皆可
//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        requestDisallowInterceptTouchEvent(true);
//        return super.dispatchTouchEvent(ev);
//    }
    @Override //比較滑順
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }
}
