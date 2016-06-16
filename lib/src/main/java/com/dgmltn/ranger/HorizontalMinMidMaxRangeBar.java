package com.dgmltn.ranger;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dgmltn.ranger.internal.AbsRangeBar;

// TODO:(pv) Give appropriate generic name and move in to material-range-bar
public class HorizontalMinMidMaxRangeBar
        extends RelativeLayout
{
    //private static final String TAG = PbLog.TAG(HorizontalMinMidMaxRangeBar.class);

    public interface HorizontalMinMaxSummaryRangeBarListener
    {
        /**
         * Notification that the progress level has changed. Clients can use the fromUser parameter
         * to distinguish user-initiated changes from those that occurred programmatically.
         *
         * @param rangeBar    The HorizontalMinMaxSummaryRangeBar whose progress has changed
         * @param firstValue
         * @param secondValue
         * @return true to allow the change, false to prevent the change
         */
        boolean onHorizontalRangeBarChanged(HorizontalMinMidMaxRangeBar rangeBar, int firstValue, int secondValue);
    }

    public interface ValueFormatter
    {
        String getLabel(int value);
    }

    // Reasonable default min and max values
    private int DEFAULT_VALUE_MIN = -100;
    private int DEFAULT_VALUE_MAX = 100;

    private HorizontalMinMaxSummaryRangeBarListener mListener;

    private ValueFormatter mValueFormatter;

    private int mValueMin = -1;
    private int mValueMax = -1;

    private int mFirstPinValue  = -1;
    private int mSecondPinValue = -1;

    private boolean mTrackingTouch;

    private HorizontalRangeBar mRangeBar;
    private TextView           mTextValueMin;
    private TextView           mTextValueMid;
    private TextView           mTextValueMax;

    public HorizontalMinMidMaxRangeBar(Context context)
    {
        super(context);
        applyAttributes(context, null);
        init();
    }

    public HorizontalMinMidMaxRangeBar(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        applyAttributes(context, attrs);
        init();
    }

    public HorizontalMinMidMaxRangeBar(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        applyAttributes(context, attrs);
        init();
    }

    private void applyAttributes(Context context, AttributeSet attrs)
    {
        if (attrs == null)
        {
            return;
        }
        /*
        final TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.Pb3dTextureView);
        final int count = array.getIndexCount();
        for (int i = 0; i < count; ++i)
        {
            int attr = array.getIndex(i);
            // ...
        }
        array.recycle();
        */
    }

    private void init()
    {
        Context context = getContext();
        inflate(context, R.layout.horizontal_min_max_summary_range_bar, this);

        mRangeBar = (HorizontalRangeBar) findViewById(com.dgmltn.ranger.R.id.horizontal_range_bar);
        mTextValueMin = (TextView) findViewById(com.dgmltn.ranger.R.id.text_value_min);
        mTextValueMid = (TextView) findViewById(com.dgmltn.ranger.R.id.text_value_mid);
        mTextValueMax = (TextView) findViewById(com.dgmltn.ranger.R.id.text_value_max);

        mRangeBar.setOnRangeBarChangeListener(new AbsRangeBar.OnRangeBarChangeListener()
        {
            @Override
            public void onRangeChanged(AbsRangeBar rangeBar, int firstIndex, int secondIndex, boolean fromUser)
            {
                //PbLog.e(TAG, "onRangeChanged(rangeBar, firstIndex=" + firstIndex +
                //             ", secondIndex=" + secondIndex +
                //             ", fromUser=" + fromUser + ')');
                //PbLog.e(TAG, "onRangeChanged: mTrackingTouch=" + mTrackingTouch);
                if (fromUser && !mTrackingTouch)
                {
                    syncRangeBar(rangeBar);
                }
            }

            @Override
            public void onStartTrackingTouch(AbsRangeBar rangeBar)
            {
                //PbLog.e(TAG, "onStartTrackingTouch: rangeBar.getFirstPinIndex()=" + rangeBar.getFirstPinIndex());
                //PbLog.e(TAG, "onStartTrackingTouch: rangeBar.getSecondPinIndex()=" + rangeBar.getSecondPinIndex());
                mTrackingTouch = true;
            }

            @Override
            public void onStopTrackingTouch(AbsRangeBar rangeBar)
            {
                //PbLog.e(TAG, "onStopTrackingTouch: rangeBar.getFirstPinIndex()=" + rangeBar.getFirstPinIndex());
                //PbLog.e(TAG, "onStopTrackingTouch: rangeBar.getSecondPinIndex()=" + rangeBar.getSecondPinIndex());
                mTrackingTouch = false;
                syncRangeBar(rangeBar);
            }
        });

        setValueFormatter(mValueFormatter);

        setConnectingLineInverted(true);

        int colorCold = 0xFF2196F3;
        int colorHot = 0xFFFF5722;
        int colorText = 0xFF58595B;

        setFirstConnectingLineColor(colorCold);
        setFirstPinColor(colorCold);
        setFirstSelectorColor(colorCold);
        setFirstPinTextColor(colorText);

        setSecondConnectingLineColor(colorHot);
        setSecondPinColor(colorHot);
        setSecondSelectorColor(colorHot);
        setSecondPinTextColor(colorText);

        enableDrawTicks(false);
        setTemporaryPins(false);
        //setConnectingLineWeight(4.0f);
        setPinRadius(36.0f); // TODO:(pv) oval radiusMajor/radiusMinor?

        setMinMaxValues(DEFAULT_VALUE_MIN, DEFAULT_VALUE_MAX);

        callRangeChangeListener(mFirstPinValue, mSecondPinValue);
    }

    private int indexToValue(int index)
    {
        //PbLog.e(TAG, "indexToValue(index=" + index + ')');
        int value = index + mValueMin;
        //PbLog.e(TAG, "indexToValue: value=" + value);
        return value;
    }

    private int valueToIndex(int value)
    {
        //PbLog.e(TAG, "valueToIndex(value=" + value + ')');
        int index = value - mValueMin;
        //PbLog.e(TAG, "valueToIndex: index=" + index);
        return index;
    }

    public void setListener(HorizontalMinMaxSummaryRangeBarListener listener)
    {
        mListener = listener;
    }

    public void setValueFormatter(ValueFormatter formatter)
    {
        if (formatter == null)
        {
            formatter = new ValueFormatter()
            {
                @Override
                public String getLabel(int value)
                {
                    String label = Integer.toString(value);
                    if (value > 0)
                    {
                        label = "+" + label;
                    }
                    return label;
                }
            };
        }

        mValueFormatter = formatter;

        mRangeBar.setIndexFormatter(new AbsRangeBar.IndexFormatter()
        {
            @Override
            public String getLabel(int index)
            {
                return mValueFormatter.getLabel(indexToValue(index));
            }
        });
    }

    public void enableDrawTicks(boolean enable)
    {
        mRangeBar.enableDrawTicks(enable);
    }

    public void setMinMaxValues(int valueMin, int valueMax)
    {
        if (valueMin >= valueMax)
        {
            throw new IllegalArgumentException("valueMin(" + valueMin + ") must be < valueMax(" + valueMax + ')');
        }

        if (valueMin == Integer.MIN_VALUE || valueMin == Integer.MAX_VALUE)
        {
            valueMin = DEFAULT_VALUE_MIN;
        }
        if (valueMax == Integer.MIN_VALUE || valueMax == Integer.MAX_VALUE)
        {
            valueMax = DEFAULT_VALUE_MAX;
        }

        if (valueMin != mValueMin || valueMax != mValueMax)
        {
            mValueMin = valueMin;
            mValueMax = valueMax;

            mRangeBar.setTickCount(valueToIndex(mValueMax) + 1);
            String text;
            text = mRangeBar.getPinLabel(valueToIndex(mValueMin));
            mTextValueMin.setText(text);

            int valueMid = (int) (Math.round(mValueMin + mValueMax) / 2.0);
            text = mRangeBar.getPinLabel(valueToIndex(valueMid));
            mTextValueMid.setText(text);

            text = mRangeBar.getPinLabel(valueToIndex(mValueMax));
            mTextValueMax.setText(text);

            mFirstPinValue = valueMin;
            mSecondPinValue = valueMax;

            mRangeBar.setPinIndices(valueToIndex(mFirstPinValue), valueToIndex(mSecondPinValue));
        }
    }

    public int getFirstPinValue()
    {
        return mFirstPinValue;
    }

    public int getSecondPinValue()
    {
        return mSecondPinValue;
    }

    public void setPinValues(int firstPinValue, int secondPinValue)
    {
        if (firstPinValue < mValueMin)
        {
            firstPinValue = mValueMin;
        }
        if (secondPinValue > mValueMax || secondPinValue <= firstPinValue)
        {
            secondPinValue = mValueMax;
        }
        if (firstPinValue != mFirstPinValue || secondPinValue != mSecondPinValue)
        {
            mFirstPinValue = firstPinValue;
            mSecondPinValue = secondPinValue;

            mRangeBar.setPinIndices(valueToIndex(mFirstPinValue), valueToIndex(mSecondPinValue));
        }
    }

    public void setConnectingLineInverted(boolean value)
    {
        mRangeBar.setConnectingLineInverted(value);
    }

    public void setFirstConnectingLineColor(int color)
    {
        mRangeBar.setFirstConnectingLineColor(color);
    }

    public void setFirstPinColor(int color)
    {
        mRangeBar.setFirstPinColor(color);
    }

    public void setFirstSelectorColor(int color)
    {
        mRangeBar.setFirstSelectorColor(color);
    }

    public void setFirstPinTextColor(int color)
    {
        mRangeBar.setFirstPinTextColor(color);
    }

    public void setSecondConnectingLineColor(int color)
    {
        mRangeBar.setSecondConnectingLineColor(color);
    }

    public void setSecondPinColor(int color)
    {
        mRangeBar.setSecondPinColor(color);
    }

    public void setSecondSelectorColor(int color)
    {
        mRangeBar.setSecondSelectorColor(color);
    }

    public void setSecondPinTextColor(int color)
    {
        mRangeBar.setSecondPinTextColor(color);
    }

    public void setTemporaryPins(boolean value)
    {
        mRangeBar.setTemporaryPins(value);
    }

    public void setPinRadius(float radius)
    {
        mRangeBar.setPinRadius(radius);
    }

    private boolean callRangeChangeListener(int firstPinValue, int secondPinValue)
    {
        return mListener == null ||
               mListener.onHorizontalRangeBarChanged(this, firstPinValue, secondPinValue);
    }

    private void syncRangeBar(AbsRangeBar rangeBar)
    {
        //PbLog.e(TAG, "syncRangeBar(rangeBar)");

        int firstPinValue = indexToValue(rangeBar.getFirstPinIndex());
        int secondPinValue = indexToValue(rangeBar.getSecondPinIndex());
        if (firstPinValue != mFirstPinValue || secondPinValue != mSecondPinValue)
        {
            if (callRangeChangeListener(firstPinValue, secondPinValue))
            {
                //PbLog.e(TAG, "syncRangeBar: setPinValues(...)");
                setPinValues(firstPinValue, secondPinValue);
            }
            else
            {
                //PbLog.e(TAG, "syncRangeBar: ignoring and restoring previous pin values");
                rangeBar.setPinIndices(valueToIndex(mFirstPinValue), valueToIndex(mSecondPinValue));
            }
        }
    }
}
