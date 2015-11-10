package com.dgmltn.ranger.demo;

import android.content.Context;
import android.util.AttributeSet;

public class HorizontalRangeBarPreference extends AbsRangeBarPreference {
    @SuppressWarnings("unused")
    public HorizontalRangeBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressWarnings("unused")
    public HorizontalRangeBarPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @SuppressWarnings("unused")
    public HorizontalRangeBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressWarnings("unused")
    public HorizontalRangeBarPreference(Context context) {
        super(context);
    }

    @Override
    public int getLayoutResource() {
        return R.layout.horizontal_range_bar_preference;
    }
}
