package com.timhaasdyk.german_verbs;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Switch;

/**
 * Created by timh on 13.09.15.
 */
public class CustomSwitch extends Switch {
    public CustomSwitch(Context context) {
        super(context);
    }

    public CustomSwitch(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public CustomSwitch(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void requestLayout () {
        try {
            java.lang.reflect.Field mOnLayout = Switch.class.getDeclaredField ( "mOnLayout");
            mOnLayout.setAccessible (true);
            mOnLayout.set (this, null);
            java.lang.reflect.Field mOffLayout = Switch.class.getDeclaredField ( "mOffLayout");
            mOffLayout.setAccessible (true) ;
            mOffLayout.set (this, null);
        } catch (Exception ex) {
            Log.e("CustomerSwitch - failed", ex.getMessage(), ex);
        }
        super.requestLayout();
    }
}
