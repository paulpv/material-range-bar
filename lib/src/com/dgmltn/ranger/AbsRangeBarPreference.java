package com.dgmltn.ranger;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.dgmltn.ranger.internal.AbsRangeBar;

public abstract class AbsRangeBarPreference
        extends CheckBoxPreference {

    //private static final String TAG = PbLog.TAG("AbsRangeBarPreference");

    public interface OnRangeBarPreferenceListener {
        /**
         * Notification that the progress level has changed. Clients can use the fromUser parameter
         * to distinguish user-initiated changes from those that occurred programmatically.
         *
         * @param preference  The AbsRangeBarPreference whose progress has changed
         * @param firstValue
         * @param secondValue
         * @return true to allow the change, false to prevent the change
         */
        boolean onRangeChanged(AbsRangeBarPreference preference, int firstValue, int secondValue);

        void onRangeBarViewBound(AbsRangeBar rangeBar);
    }

    public interface ValueFormatter {
        String getLabel(int value);
    }

    // Reasonable default min and max values
    private int DEFAULT_VALUE_MIN = -42;
    private int DEFAULT_VALUE_MAX = 42;

    private OnRangeBarPreferenceListener mRangeBarPreferenceListener;
    private ValueFormatter mRangeBarValueFormatter;
    private boolean mEnableDrawTicks;

    private int mValueMin = DEFAULT_VALUE_MIN;
    private int mValueMax = DEFAULT_VALUE_MAX;

    private int mFirstPinValue = mValueMin;
    private int mSecondPinValue = mValueMax;

    private boolean mTrackingTouch;

    AttributeSet mAttributes;

    public AbsRangeBarPreference(Context context) {
        this(context, null);
    }

    public AbsRangeBarPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public AbsRangeBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AbsRangeBarPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mAttributes = attrs;
        setLayoutResource(getLayoutResource());
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        AbsRangeBar rangeBar = (HorizontalRangeBar) view.findViewById(R.id.horizontal_range_bar);
        TextView textValueMin = (TextView) view.findViewById(R.id.text_value_min);
        TextView textValueMid = (TextView) view.findViewById(R.id.text_value_mid);
        TextView textValueMax = (TextView) view.findViewById(R.id.text_value_max);

        //Context context = getContext();
        //TypedArray ta = context.obtainStyledAttributes(mAttributes, R.styleable.AbsRangeBar, 0, 0);
        //rangeBar.initialize(ta, true);

        rangeBar.setOnRangeBarChangeListener(new AbsRangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChanged(AbsRangeBar rangeBar, int firstIndex, int secondIndex, boolean fromUser) {
                //PbLog.e(TAG, "onRangeChanged(rangeBar, firstIndex=" + firstIndex + ", secondIndex=" + secondIndex + ", fromUser=" + fromUser + ')');
                if (fromUser && !mTrackingTouch) {
                    syncRangeBar(rangeBar);
                }
            }

            @Override
            public void onStartTrackingTouch(AbsRangeBar rangeBar) {
                //PbLog.e(TAG, "onStartTrackingTouch(rangeBar)");
                mTrackingTouch = true;
            }

            @Override
            public void onStopTrackingTouch(AbsRangeBar rangeBar) {
                //PbLog.e(TAG, "onStopTrackingTouch(rangeBar)");
                mTrackingTouch = false;
                syncRangeBar(rangeBar);
            }
        });

        //setValueFormatter(mRangeBarValueFormatter);
        rangeBar.setIndexFormatter(new AbsRangeBar.IndexFormatter() {
            @Override
            public String getLabel(int index) {
                return mRangeBarValueFormatter.getLabel(indexToValue(index));
            }
        });

        //enableDrawTicks(mEnableDrawTicks);
        rangeBar.enableDrawTicks(mEnableDrawTicks);

        //initialize(mValueMin, mValueMax);
        rangeBar.setTickCount(valueToIndex(mValueMax));
        String text;
        text = rangeBar.getPinLabel(valueToIndex(mValueMin));
        textValueMin.setText(text);

        int valueMid = (int) (Math.round(mValueMin + mValueMax) / 2.0);
        text = rangeBar.getPinLabel(valueToIndex(valueMid));
        textValueMid.setText(text);

        text = rangeBar.getPinLabel(valueToIndex(mValueMax));
        textValueMax.setText(text);

        //setPinValues(mFirstPinValue, mSecondPinValue);
        rangeBar.setPinIndices(valueToIndex(mFirstPinValue), valueToIndex(mSecondPinValue));

        if (mRangeBarPreferenceListener != null) {
            mRangeBarPreferenceListener.onRangeBarViewBound(rangeBar);
        }
    }

    private int indexToValue(int index) {
        return index + mValueMin;
    }

    private int valueToIndex(int value) {
        return value - mValueMin;
    }

    public void setOnRangeBarPreferenceListener(OnRangeBarPreferenceListener listener) {
        mRangeBarPreferenceListener = listener;
    }

    public void setValueFormatter(ValueFormatter formatter) {
        if (formatter == null) {
            formatter = new ValueFormatter() {
                @Override
                public String getLabel(int value) {
                    return Integer.toString(value);
                }
            };
        }
        mRangeBarValueFormatter = formatter;
        notifyChanged();
    }

    public void enableDrawTicks(boolean enable) {
        mEnableDrawTicks = enable;
        notifyChanged();
    }

    public void setMinMaxValues(int valueMin, int valueMax) {
        if (valueMin >= valueMax) {
            throw new IllegalArgumentException("valueMin(" + valueMin + ") must be < valueMax(" + valueMax + ')');
        }

        if (valueMin == Integer.MIN_VALUE || valueMin == Integer.MAX_VALUE) {
            valueMin = DEFAULT_VALUE_MIN;
        }
        if (valueMax == Integer.MIN_VALUE || valueMax == Integer.MAX_VALUE) {
            valueMax = DEFAULT_VALUE_MAX;
        }

        if (valueMin != mValueMin || valueMax != mValueMax) {
            mFirstPinValue = valueMin;
            mSecondPinValue = valueMax;
            mValueMin = valueMin;
            mValueMax = valueMax;
            notifyChanged();
        }
    }

    public int getFirstPinValue() {
        return mFirstPinValue;
    }

    public int getSecondPinValue() {
        return mSecondPinValue;
    }

    public void setPinValues(int firstPinValue, int secondPinValue) {
        setPinValues(firstPinValue, secondPinValue, true);
    }

    private void setPinValues(int firstPinValue, int secondPinValue, boolean notifyChanged) {
        if (firstPinValue >= secondPinValue) {
            throw new IllegalArgumentException("firstPinValue(" + firstPinValue + ") must be < secondPinValue(" + secondPinValue + ')');
        }
        if (firstPinValue < mValueMin) {
            firstPinValue = mValueMin;
        }
        if (secondPinValue > mValueMax) {
            secondPinValue = mValueMax;
        }
        if (firstPinValue != mFirstPinValue || secondPinValue != mSecondPinValue) {
            mFirstPinValue = firstPinValue;
            mSecondPinValue = secondPinValue;

            // TODO:(pv) Persist value to mPreferenceManager.getEditor()

            if (notifyChanged) {
                notifyChanged();
            }
        }
    }

    private boolean callRangeChangeListener(int firstPinValue, int secondPinValue) {
        return mRangeBarPreferenceListener == null || mRangeBarPreferenceListener.onRangeChanged(this, firstPinValue, secondPinValue);
    }

    private void syncRangeBar(AbsRangeBar rangeBar) {
        int firstPinValue = indexToValue(rangeBar.getFirstPinIndex());
        int secondPinValue = indexToValue(rangeBar.getSecondPinIndex());
        if (firstPinValue != mFirstPinValue || secondPinValue != mSecondPinValue) {
            if (callRangeChangeListener(firstPinValue, secondPinValue)) {
                setPinValues(firstPinValue, secondPinValue, false);
            } else {
                rangeBar.setPinIndices(valueToIndex(mFirstPinValue), valueToIndex(mSecondPinValue));
            }
        }
    }
}
