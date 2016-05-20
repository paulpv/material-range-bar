package com.dgmltn.ranger;

import android.content.Context;
import android.util.AttributeSet;

public class HorizontalRangeBarPreference extends AbsRangeBarPreference {
    @SuppressWarnings("unused")
    public HorizontalRangeBarPreference(Context context) {
        super(context);
    }

    @SuppressWarnings("unused")
    public HorizontalRangeBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressWarnings("unused")
    public HorizontalRangeBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public int getLayoutResource() {
        return R.layout.horizontal_range_bar_preference;
    }
}
