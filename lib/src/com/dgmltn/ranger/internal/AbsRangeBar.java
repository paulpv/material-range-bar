/*
 * Copyright 2013, Edmodo, Inc. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file, or at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" 
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License. 
 */

package com.dgmltn.ranger.internal;
/*
 * Copyright 2015, Appyvet, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file, or at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;

import com.dgmltn.ranger.PinView;
import com.dgmltn.ranger.R;
import com.pebblebee.common.logging.PbLog;

/**
 * The MaterialRangeBar is a single or double-sided version of a {@link android.widget.SeekBar}
 * with discrete values. Whereas the thumb for the SeekBar can be dragged to any
 * position in the bar, the RangeBar only allows its thumbs to be dragged to
 * discrete positions (denoted by tick marks) in the bar. When released, a
 * RangeBar thumb will snap to the nearest tick mark.
 * This version is forked from edmodo range bar
 * https://github.com/edmodo/range-bar.git
 * Clients of the RangeBar can attach a
 * {@link AbsRangeBar.OnRangeBarChangeListener} to be notified when the pins
 * have
 * been moved.
 */
public abstract class AbsRangeBar extends View {

    // Member Variables ////////////////////////////////////////////////////////

    private static final String TAG = "AbsRangeBar";

    // Default values for variables
    private static final int DEFAULT_TICK_COUNT = 6;
    private static final float DEFAULT_TICK_SIZE_DP = 1;
    private static final float DEFAULT_PIN_PADDING_DP = 16;
    public static final float DEFAULT_MIN_PIN_FONT_SP = 8;
    public static final float DEFAULT_MAX_PIN_FONT_SP = 24;
    private static final float DEFAULT_BAR_WEIGHT_DP = 1;
    private static final int DEFAULT_BAR_COLOR = Color.LTGRAY;
    private static final int DEFAULT_TEXT_COLOR = Color.WHITE;
    private static final int DEFAULT_TICK_COLOR = Color.BLACK;
    private static final int INDIGO_500 = 0xff3f51b5;
    private static final int DEFAULT_PIN_COLOR = INDIGO_500;
    private static final float DEFAULT_CONNECTING_LINE_WEIGHT_DP = 2;
    private static final int DEFAULT_CONNECTING_LINE_COLOR = INDIGO_500;
    private static final float DEFAULT_EXPANDED_PIN_RADIUS_DP = 12;
    private static final float DEFAULT_CIRCLE_SIZE_DP = 5;

    // "natural" dimensions of this View for WRAP_CONTENT
    private static final int DEFAULT_WIDTH = 500;
    private static final int DEFAULT_HEIGHT = 150;

    // Instance variables for all of the customizable attributes

    // Bar
    private float mBarWeight;
    private int mBarColor;

    // Ticks
    private float mTickSize;
    private int mTickColor;
    private int mTickCount;
    private boolean mDrawTicks = true;

    // Selectors
    private int mFirstSelectorColor;
    private int mSecondSelectorColor;
    private float mSelectorSize;

    // Pins
    private PinView mFirstPinView;
    private PinView mSecondPinView;
    private int mFirstPinColor;
    private int mSecondPinColor;
    private float mPinRadius;
    private float mExpandedPinRadius;
    private float mMinPinFont;
    private float mMaxPinFont;
    private float mPinPadding;
    private int mFirstPinTextColor;
    private int mSecondPinTextColor;
    private boolean mArePinsTemporary = true;

    // Connecting Line
    private float mConnectingLineWeight;
    protected int mFirstConnectingLineColor;
    protected int mSecondConnectingLineColor;

    // Listeners
    private OnRangeBarChangeListener mListener;

    private boolean mIsRangeBar = true;
    private int mActiveFirstConnectingLineColor;
    private int mActiveSecondConnectingLineColor;
    private int mActiveBarColor;
    private int mActiveTickColor;
    private int mActiveFirstCircleColor;
    private int mActiveSecondCircleColor;

    protected boolean mConnectingLineInverted;

    private ValueFormatter mValueFormatter = new ValueFormatter() {
        @Override
        public String getLabel(int index) {
            String value = Integer.toString(index);
            if (value.length() > 4) {
                return value.substring(0, 4);
            } else {
                return value;
            }
        }
    };

    // Constructors ////////////////////////////////////////////////////////////

    public AbsRangeBar(Context context) {
        super(context);
    }

    public AbsRangeBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AbsRangeBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    /**
     * Does all the functions of the constructor for RangeBar. Called by both
     * RangeBar constructors in lieu of copying the code for each constructor.
     *
     * @param context Context from the constructor.
     * @param attrs   AttributeSet from the constructor.
     */
    @SuppressWarnings("UnnecessaryLocalVariable")
    private void init(Context context, AttributeSet attrs) {

        mFirstPinView = new PinView(context, "mFirstPinView");
        mSecondPinView = new PinView(context, "mSecondPinView");

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.AbsRangeBar, 0, 0);

        try {
            // Sets the values of the user-defined attributes based on the XML attributes.
            int tickCount = ta.getInt(R.styleable.AbsRangeBar_tickCount, DEFAULT_TICK_COUNT);
            validateTickCount(tickCount);
            mTickCount = tickCount;

            float density = context.getResources().getDisplayMetrics().density;
            mTickSize = ta.getDimension(R.styleable.AbsRangeBar_tickHeight,
                    DEFAULT_TICK_SIZE_DP * density);
            mBarWeight = ta.getDimension(R.styleable.AbsRangeBar_barWeight,
                    DEFAULT_BAR_WEIGHT_DP * density);
            mBarColor = ta.getColor(R.styleable.AbsRangeBar_rangeBarColor,
                    DEFAULT_BAR_COLOR);
            mActiveBarColor = mBarColor;

            int pinTextColor = ta.getColor(R.styleable.AbsRangeBar_textColor,
                    DEFAULT_TEXT_COLOR);
            mFirstPinTextColor = pinTextColor;
            mSecondPinTextColor = pinTextColor;

            int pinColor = ta.getColor(R.styleable.AbsRangeBar_pinColor,
                    DEFAULT_PIN_COLOR);
            mFirstPinColor = pinColor;
            mSecondPinColor = pinColor;

            mSelectorSize = ta.getDimension(R.styleable.AbsRangeBar_selectorSize,
                    DEFAULT_CIRCLE_SIZE_DP * density);
            int selectorColor = ta.getColor(R.styleable.AbsRangeBar_selectorColor,
                    DEFAULT_CONNECTING_LINE_COLOR);
            mFirstSelectorColor = selectorColor;
            mSecondSelectorColor = selectorColor;
            mActiveFirstCircleColor = selectorColor;
            mActiveSecondCircleColor = selectorColor;

            mTickColor = ta.getColor(R.styleable.AbsRangeBar_tickColor,
                    DEFAULT_TICK_COLOR);
            mActiveTickColor = mTickColor;
            mConnectingLineWeight = ta.getDimension(R.styleable.AbsRangeBar_connectingLineWeight,
                    DEFAULT_CONNECTING_LINE_WEIGHT_DP * density);

            int connectingLineColor = ta.getColor(R.styleable.AbsRangeBar_connectingLineColor,
                    DEFAULT_CONNECTING_LINE_COLOR);
            mFirstConnectingLineColor = connectingLineColor;
            mSecondConnectingLineColor = connectingLineColor;
            mActiveFirstConnectingLineColor = connectingLineColor;
            mActiveSecondConnectingLineColor = connectingLineColor;

            mExpandedPinRadius = ta.getDimension(R.styleable.AbsRangeBar_pinRadius,
                    DEFAULT_EXPANDED_PIN_RADIUS_DP * density);
            mPinPadding = ta.getDimension(R.styleable.AbsRangeBar_pinPadding,
                    DEFAULT_PIN_PADDING_DP * density);
            mIsRangeBar = ta.getBoolean(R.styleable.AbsRangeBar_rangeBar, true);
            mArePinsTemporary = ta.getBoolean(R.styleable.AbsRangeBar_temporaryPins, true);

            float scaledDensity = getResources().getDisplayMetrics().scaledDensity;
            mMinPinFont = ta.getDimension(R.styleable.AbsRangeBar_pinMinFont,
                    DEFAULT_MIN_PIN_FONT_SP * scaledDensity);
            mMaxPinFont = ta.getDimension(R.styleable.AbsRangeBar_pinMaxFont,
                    DEFAULT_MAX_PIN_FONT_SP * scaledDensity);
        } finally {
            ta.recycle();
        }

        initBar();
        initPins(true);
        setTickCount(mTickCount);
    }

    // View Methods ////////////////////////////////////////////////////////////

    @Override
    public Parcelable onSaveInstanceState() {

        Bundle bundle = new Bundle();

        bundle.putParcelable("instanceState", super.onSaveInstanceState());

        bundle.putFloat("BAR_WEIGHT", mBarWeight);
        bundle.putInt("BAR_COLOR", mBarColor);

        bundle.putInt("TICK_COUNT", mTickCount);
        bundle.putInt("TICK_COLOR", mTickColor);
        bundle.putFloat("TICK_SIZE", mTickSize);

        bundle.putFloat("CONNECTING_LINE_WEIGHT", mConnectingLineWeight);
        bundle.putInt("FIRST_CONNECTING_LINE_COLOR", mFirstConnectingLineColor);
        bundle.putInt("SECOND_CONNECTING_LINE_COLOR", mSecondConnectingLineColor);

        bundle.putFloat("SELECTOR_SIZE", mSelectorSize);
        bundle.putInt("FIRST_SELECTOR_COLOR", mFirstSelectorColor);
        bundle.putInt("SECOND_SELECTOR_COLOR", mSecondSelectorColor);
        bundle.putFloat("PIN_RADIUS", mPinRadius);
        bundle.putFloat("EXPANDED_PIN_RADIUS", mExpandedPinRadius);
        bundle.putFloat("PIN_PADDING", mPinPadding);
        bundle.putBoolean("IS_RANGE_BAR", mIsRangeBar);
        bundle.putBoolean("ARE_PINS_TEMPORARY", mArePinsTemporary);
        bundle.putInt("FIRST_PIN_INDEX", getFirstIndex());
        bundle.putInt("SECOND_PIN_INDEX", getSecondIndex());

        bundle.putFloat("MIN_PIN_FONT", mMinPinFont);
        bundle.putFloat("MAX_PIN_FONT", mMaxPinFont);

        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {

        if (state instanceof Bundle) {

            Bundle bundle = (Bundle) state;

            mTickCount = bundle.getInt("TICK_COUNT");
            mTickColor = bundle.getInt("TICK_COLOR");
            mTickSize = bundle.getFloat("TICK_SIZE");
            mBarWeight = bundle.getFloat("BAR_WEIGHT");
            mBarColor = bundle.getInt("BAR_COLOR");
            mSelectorSize = bundle.getFloat("SELECTOR_SIZE");
            mFirstSelectorColor = bundle.getInt("FIRST_SELECTOR_COLOR");
            mSecondSelectorColor = bundle.getInt("SECOND_SELECTOR_COLOR");
            mConnectingLineWeight = bundle.getFloat("CONNECTING_LINE_WEIGHT");
            mFirstConnectingLineColor = bundle.getInt("FIRST_CONNECTING_LINE_COLOR");
            mSecondConnectingLineColor = bundle.getInt("SECOND_CONNECTING_LINE_COLOR");

            mPinRadius = bundle.getFloat("PIN_RADIUS");
            mExpandedPinRadius = bundle.getFloat("EXPANDED_PIN_RADIUS");
            mPinPadding = bundle.getFloat("PIN_PADDING");
            mIsRangeBar = bundle.getBoolean("IS_RANGE_BAR");
            mArePinsTemporary = bundle.getBoolean("ARE_PINS_TEMPORARY");

            setFirstPinIndex(bundle.getInt("FIRST_PIN_INDEX"));
            setSecondPinIndex(bundle.getInt("SECOND_PIN_INDEX"));

            mMinPinFont = bundle.getFloat("MIN_PIN_FONT");
            mMaxPinFont = bundle.getFloat("MAX_PIN_FONT");

            super.onRestoreInstanceState(bundle.getParcelable("instanceState"));
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width;
        int height;

        // Get measureSpec mode and size values.
        final int measureWidthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int measureHeightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int measureWidth = MeasureSpec.getSize(widthMeasureSpec);
        final int measureHeight = MeasureSpec.getSize(heightMeasureSpec);

        // The RangeBar width should be as large as possible.
        if (measureWidthMode == MeasureSpec.AT_MOST) {
            width = measureWidth;
        } else if (measureWidthMode == MeasureSpec.EXACTLY) {
            width = measureWidth;
        } else {
            width = DEFAULT_WIDTH;
        }

        // The RangeBar height should be as small as possible.
        if (measureHeightMode == MeasureSpec.AT_MOST) {
            height = Math.min(DEFAULT_HEIGHT, measureHeight);
        } else if (measureHeightMode == MeasureSpec.EXACTLY) {
            height = measureHeight;
        } else {
            height = DEFAULT_HEIGHT;
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // This is the initial point at which we know the size of the View.
        resizeBar(w, h);

        initPins(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawBar(canvas);
        if (mDrawTicks) {
            drawTicks(canvas);
        }
        drawConnectingLine(canvas, mFirstPinView, mSecondPinView);
        mFirstPinView.draw(canvas);
        if (mIsRangeBar) {
            mSecondPinView.draw(canvas);
        }
    }

    // Touch Methods ////////////////////////////////////////////////////////////

    private PinView mDraggingPin;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // Some logic ideas came from AbsSeekBar.java
        // https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/widget/AbsSeekBar.java

        // If this View is not enabled, don't allow for touch interactions.
        if (!isEnabled()) {
            return false;
        }

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN: {
                //PbLog.e(TAG, "onTouchEvent: ACTION_DOWN");
                if (mDraggingPin == null) {
                    mDraggingPin = getTargetPinView(event.getX(), event.getY());
                    //PbLog.e(TAG, "onTouchEvent: ACTION_DOWN mDraggingPin=" + mDraggingPin);
                    if (mDraggingPin != null) {
                        pressPin(mDraggingPin);
                        setPressed(true);
                        ViewParent parent = getParent();
                        if (parent != null) {
                            parent.requestDisallowInterceptTouchEvent(true);
                        }
                    }
                }
                break;
            }
            case MotionEvent.ACTION_MOVE:
                //PbLog.e(TAG, "onTouchEvent: ACTION_MOVE");
                if (mDraggingPin != null) {
                    int nearestTickIndex = getNearestIndex(mDraggingPin);
                    //PbLog.e(TAG, "onTouchEvent: ACTION_MOVE nearestTickIndex=" + nearestTickIndex);
                    PointF point = new PointF(event.getX(), event.getY());
                    setPinIndex(mDraggingPin, nearestTickIndex, point);
                }
                break;

            case MotionEvent.ACTION_UP:
                //PbLog.e(TAG, "onTouchEvent: ACTION_UP");
                if (mDraggingPin != null) {
                    releasePin(mDraggingPin);
                    mDraggingPin = null;
                    setPressed(false);
                }// else {
                //    // Touch up when we never crossed the touch slop threshold should
                //    // be interpreted as a tap-seek to that location. But let's not do that now.
                //}
                break;

            case MotionEvent.ACTION_CANCEL:
                //PbLog.e(TAG, "onTouchEvent: ACTION_CANCEL");
                if (mDraggingPin != null) {
                    mDraggingPin = null;
                    setPressed(false);
                }
                break;
        }

        return true;
    }

    // Public Methods //////////////////////////////////////////////////////////

    /**
     * Sets a listener to receive notifications of changes to the RangeBar. This
     * will overwrite any existing set listeners.
     *
     * @param listener the RangeBar notification listener; null to remove any
     *                 existing listener
     */
    public void setOnRangeBarChangeListener(OnRangeBarChangeListener listener) {
        mListener = listener;
    }

    /**
     * Sets an object to format pin values.
     *
     * @param formatter
     */
    public void setValueFormatter(ValueFormatter formatter) {
        mValueFormatter = formatter;
        invalidate();
    }

    /**
     * Enables or disables the drawing of tick marks.
     *
     * @param enable
     */
    public void enableDrawTicks(boolean enable) {
        mDrawTicks = enable;
        invalidate();
    }

    private void validateTickCount(int tickCount) {
        if (tickCount < 2) {
            throw new IllegalArgumentException("tickCount must be > 1");
        }
    }

    /**
     * Sets up the ticks in the RangeBar.
     *
     * @param tickCount int specifying the number of ticks to display; must be > 1
     */
    public void setTickCount(int tickCount) {
        PbLog.e(TAG, "setTickCount(" + tickCount + ')');

        validateTickCount(tickCount);

        mTickCount = tickCount;

        int maxSecondIndex = mTickCount - 1;
        //PbLog.e(TAG, "setTickCount: maxSecondIndex=" + maxSecondIndex);
        int secondIndex = mSecondPinView.getIndex();
        //PbLog.e(TAG, "setTickCount: secondIndex=" + secondIndex);
        if (secondIndex > maxSecondIndex) {
            setSecondPinIndex(maxSecondIndex);
        }

        int maxFirstIndex = mSecondPinView.getIndex() - 1;
        //PbLog.e(TAG, "setTickCount: maxFirstIndex=" + maxFirstIndex);
        int firstIndex = mFirstPinView.getIndex();
        //PbLog.e(TAG, "setTickCount: firstIndex=" + firstIndex);
        if (firstIndex > maxFirstIndex) {
            setFirstPinIndex(firstIndex);
        }

        invalidate();
    }

    /**
     * Sets the size (radius) of the ticks in the range bar.
     *
     * @param size Float size the height of each tick mark in px.
     */
    public void setTickHeight(float size) {
        mTickSize = size;
        invalidate();
    }

    /**
     * Set the weight of the bar line and the tick lines in the range bar.
     *
     * @param barWeight Float specifying the weight of the bar and tick lines in
     *                  px.
     */
    public void setBarWeight(float barWeight) {
        mBarWeight = barWeight;
        mBarPaint.setStrokeWidth(mBarWeight);
        invalidate();
    }

    /**
     * Set the color of the bar line and the tick lines in the range bar.
     *
     * @param barColor Integer specifying the color of the bar line.
     */
    public void setBarColor(int barColor) {
        mBarColor = barColor;
        mBarPaint.setColor(mBarColor);
        invalidate();
    }

    /**
     * Set the color of the pins.
     *
     * @param pinColor Integer specifying the color of the pin.
     */
    public void setPinColor(int pinColor) {
        setFirstPinColor(pinColor, false);
        setSecondPinColor(pinColor, true);
    }

    public void setFirstPinColor(int pinColor) {
        setFirstPinColor(pinColor, true);
    }

    protected void setFirstPinColor(int pinColor, boolean invalidate) {
        mFirstPinColor = pinColor;
        if (invalidate) {
            initPins(false);
        }
    }

    public void setSecondPinColor(int pinColor) {
        setSecondPinColor(pinColor, true);
    }

    protected void setSecondPinColor(int pinColor, boolean invalidate) {
        mSecondPinColor = pinColor;
        if (invalidate) {
            initPins(false);
        }
    }

    /**
     * Set the color of the text within the pin.
     *
     * @param textColor Integer specifying the color of the text in the pin.
     */
    public void setPinTextColor(int textColor) {
        setFirstPinTextColor(textColor, false);
        setSecondPinTextColor(textColor, true);
    }

    public void setFirstPinTextColor(int textColor) {
        setFirstPinTextColor(textColor, true);
    }

    protected void setFirstPinTextColor(int textColor, boolean invalidate) {
        mFirstPinTextColor = textColor;
        if (invalidate) {
            initPins(false);
        }
    }

    public void setSecondPinTextColor(int textColor) {
        setSecondPinTextColor(textColor, true);
    }

    protected void setSecondPinTextColor(int textColor, boolean invalidate) {
        mSecondPinTextColor = textColor;
        if (invalidate) {
            initPins(false);
        }
    }

    /**
     * Set if the view is a range bar or a seek bar.
     *
     * @param isRangeBar Boolean - true sets it to rangebar, false to seekbar.
     */
    public void setRangeBarEnabled(boolean isRangeBar) {
        mIsRangeBar = isRangeBar;
        invalidate();
    }

    /**
     * Set if the pins should dissapear after released
     *
     * @param arePinsTemporary Boolean - true if pins should disappear after released, false to stay
     *                         visible
     */
    public void setTemporaryPins(boolean arePinsTemporary) {
        mArePinsTemporary = arePinsTemporary;
        invalidate();
    }

    /**
     * Set the color of the ticks.
     *
     * @param tickColor Integer specifying the color of the ticks.
     */
    public void setTickColor(int tickColor) {
        mTickColor = tickColor;
        mTickPaint.setColor(mTickColor);
        invalidate();
    }

    /**
     * Set the color of the selector.
     *
     * @param selectorColor Integer specifying the color of the ticks.
     */
    public void setSelectorColor(int selectorColor) {
        setFirstSelectorColor(selectorColor, false);
        setSecondSelectorColor(selectorColor, true);
    }

    public void setFirstSelectorColor(int selectorColor) {
        setFirstSelectorColor(selectorColor, true);
    }

    protected void setFirstSelectorColor(int selectorColor, boolean invalidate) {
        mFirstSelectorColor = selectorColor;
        if (invalidate) {
            initPins(false);
        }
    }

    public void setSecondSelectorColor(int selectorColor) {
        setSecondSelectorColor(selectorColor, true);
    }

    protected void setSecondSelectorColor(int selectorColor, boolean invalidate) {
        mSecondSelectorColor = selectorColor;
        if (invalidate) {
            initPins(false);
        }
    }

    /**
     * Set the weight of the connecting line between the thumbs.
     *
     * @param connectingLineWeight Float specifying the weight of the connecting
     *                             line.
     */
    public void setConnectingLineWeight(float connectingLineWeight) {
        mConnectingLineWeight = connectingLineWeight;
        mFirstConnectingLinePaint.setStrokeWidth(mConnectingLineWeight);
        mSecondConnectingLinePaint.setStrokeWidth(mConnectingLineWeight);
        invalidate();
    }

    /**
     * Set the color of the connecting line between the thumbs.
     *
     * @param connectingLineColor Integer specifying the color of the connecting
     *                            line.
     */
    public void setConnectingLineColor(int connectingLineColor) {
        setFirstConnectingLineColor(connectingLineColor, true);
        setSecondConnectingLineColor(connectingLineColor, false);
    }

    public void setFirstConnectingLineColor(int connectingLineColor) {
        setFirstConnectingLineColor(connectingLineColor, true);
    }

    protected void setFirstConnectingLineColor(int connectingLineColor, boolean invalidate) {
        mFirstConnectingLineColor = connectingLineColor;
        mFirstConnectingLinePaint.setColor(mFirstConnectingLineColor);
        if (invalidate) {
            invalidate();
        }
    }

    public void setSecondConnectingLineColor(int connectingLineColor) {
        setSecondConnectingLineColor(connectingLineColor, true);
    }

    protected void setSecondConnectingLineColor(int connectingLineColor, boolean invalidate) {
        mSecondConnectingLineColor = connectingLineColor;
        mSecondConnectingLinePaint.setColor(mSecondConnectingLineColor);
        if (invalidate) {
            invalidate();
        }
    }

    /**
     * If this is set, the thumb images will be replaced with a circle of the
     * specified radius. Default width = 20dp.
     *
     * @param pinRadius Float specifying the radius of the thumbs to be drawn.
     */
    public void setPinRadius(float pinRadius) {
        mExpandedPinRadius = pinRadius;
        initPins(false);
    }

    /**
     * Gets the tick count.
     *
     * @return the tick count
     */
    public int getTickCount() {
        return mTickCount;
    }

    /**
     * Gets the type of the bar.
     *
     * @return true if rangebar, false if seekbar.
     */
    public boolean isRangeBar() {
        return mIsRangeBar;
    }

    /**
     * Gets the index of the first pin.
     *
     * @return the 0-based index of the first pin
     */
    public int getFirstIndex() {
        return mFirstPinView.getIndex();
    }

    /**
     * Gets the index of the second pin.
     *
     * @return the 0-based index of the second pin
     */
    public int getSecondIndex() {
        return mSecondPinView.getIndex();
    }

    public void setFirstPinIndex(int index) {
        setPinIndex(mFirstPinView, index, null);
    }


    public void setSecondPinIndex(int index) {
        setPinIndex(mSecondPinView, index, null);
    }

    private boolean setPinIndex(PinView pinView, int index, PointF point) {
        //PbLog.e(TAG, "setPinIndex(pinView=" + pinView + ", index=" + index + ", point=" + point + ')');
        if (pinView == null) {
            return false;
        }

        PointF pointMin = new PointF();
        PointF pointMax = new PointF();
        PointF pointIndex = new PointF();
        if (point == null) {
            getPointOfIndex(index, pointIndex);
        } else {
            getNearestPointOnBar(point, pointIndex);
        }
        //PbLog.e(TAG, "setPinIndex: pointIndex=" + pointIndex);

        int indexMin;
        int indexMax;
        if (mFirstPinView.equals(pinView)) {
            indexMin = 0;
            indexMax = mSecondPinView.getIndex() - 1;
        } else if (mSecondPinView.equals(pinView)) {
            indexMin = mFirstPinView.getIndex() + 1;
            indexMax = mTickCount - 1;
        } else {
            //PbLog.e(TAG, "setPinIndex: pinView != mFirstPinView || pinView != mSecondPinView; ignoring");
            return false;
        }

        //PbLog.e(TAG, "setPinIndex: indexMin=" + indexMin);
        //PbLog.e(TAG, "setPinIndex: indexMax=" + indexMax);

        getPointOfIndex(indexMin, pointMin);
        //PbLog.e(TAG, "setPinIndex: pointMin=" + pointMin);
        getPointOfIndex(indexMax, pointMax);
        //PbLog.e(TAG, "setPinIndex: pointMax=" + pointMax);

        boolean allowed = comparePointsOnBar(pointMin, pointIndex) <= 0 &&
                comparePointsOnBar(pointIndex, pointMax) <= 0;
        //PbLog.e(TAG, "setPinIndex: allowed=" + allowed);

        boolean changed = allowed && pinView.setIndex(index);
        //PbLog.e(TAG, "setPinIndex: changed=" + changed);

        if (allowed) {
            movePin(pinView, pointIndex);

            String label = getPinLabel(index);
            pinView.setLabel(label);
        }

        if (changed) {
            if (mListener != null) {
                mListener.onRangeChangeListener(this, mFirstPinView.getIndex(), mSecondPinView.getIndex());
            }
        }

        return true;
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (!enabled) {
            mBarColor = DEFAULT_BAR_COLOR;
            mFirstConnectingLineColor = DEFAULT_BAR_COLOR;
            mSecondConnectingLineColor = DEFAULT_BAR_COLOR;
            mFirstSelectorColor = DEFAULT_BAR_COLOR;
            mSecondSelectorColor = DEFAULT_BAR_COLOR;
            mTickColor = DEFAULT_BAR_COLOR;
        } else {
            mBarColor = mActiveBarColor;
            mFirstConnectingLineColor = mActiveFirstConnectingLineColor;
            mSecondConnectingLineColor = mActiveSecondConnectingLineColor;
            mFirstSelectorColor = mActiveFirstCircleColor;
            mSecondSelectorColor = mActiveSecondCircleColor;
            mTickColor = mActiveTickColor;
        }

        mBarPaint.setColor(mBarColor);
        mBarPaint.setStrokeWidth(mBarWeight);
        mTickPaint.setColor(mTickColor);
        mFirstConnectingLinePaint.setColor(mFirstConnectingLineColor);
        mFirstConnectingLinePaint.setStrokeWidth(mConnectingLineWeight);
        mSecondConnectingLinePaint.setColor(mSecondConnectingLineColor);
        mSecondConnectingLinePaint.setStrokeWidth(mConnectingLineWeight);

        initPins(false);

        super.setEnabled(enabled);
    }

    public void setConnectingLineInverted(boolean connectingLineInverted) {
        mConnectingLineInverted = connectingLineInverted;
    }

    // Private Methods /////////////////////////////////////////////////////////

    /**
     * Initializes (and creates if necessary) the one or two Pins.
     */
    private void initPins(boolean resetIndexes) {

        initPin(mFirstPinView, mFirstPinColor, mFirstPinTextColor, mFirstSelectorColor);
        initPin(mSecondPinView, mSecondPinColor, mSecondPinTextColor, mSecondSelectorColor);

        if (resetIndexes) {
            mFirstPinView.setIndex(0);
            mSecondPinView.setIndex(mTickCount - 1);
        }

        if (!mArePinsTemporary) {
            pressPin(mFirstPinView);
            if (mIsRangeBar) {
                pressPin(mSecondPinView);
            }
        }

        invalidate();
    }

    private void initPin(PinView pinView, int pinColor, int pinTextColor, int pinSelectorColor) {

        int pinIndex = pinView.getIndex();

        PointF pinPoint = new PointF();
        getPointOfIndex(pinIndex, pinPoint);

        pinView.init(pinPoint,
                0, pinColor, pinTextColor,
                mSelectorSize, pinSelectorColor,
                mMinPinFont, mMaxPinFont);

        pinView.setLabel(getPinLabel(pinIndex));
    }

    private PinView getTargetPinView(float x, float y) {
        if (mFirstPinView != null) {
            if (mFirstPinView.isInTargetZone(x, y)) {
                return mFirstPinView;
            }
        }
        if (mSecondPinView != null) {
            if (mSecondPinView.isInTargetZone(x, y)) {
                return mSecondPinView;
            }
        }
        return null;
    }

    /**
     * Set the thumb to be in the pressed state and calls invalidate() to redraw
     * the canvas to reflect the updated state.
     *
     * @param thumb the thumb to press
     */
    private void pressPin(final PinView thumb) {
        if (mArePinsTemporary) {
            ValueAnimator animator = ValueAnimator.ofFloat(0, mExpandedPinRadius);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mPinRadius = (Float) (animation.getAnimatedValue());
                    thumb.setSize(mPinRadius, mPinPadding * animation.getAnimatedFraction());
                    invalidate();
                }
            });
            animator.start();
        } else {
            thumb.setSize(mExpandedPinRadius, mPinPadding);
        }

        thumb.press();
    }

    /**
     * Set the thumb to be in the normal/un-pressed state and calls invalidate()
     * to redraw the canvas to reflect the updated state.
     *
     * @param pinView the thumb to release
     */
    private void releasePin(final PinView pinView) {
        PointF point = new PointF();
        getNearestIndexPosition(pinView.getPosition(), point);
        pinView.setPosition(point);
        int tickIndex = getNearestIndex(pinView);

        pinView.setLabel(getPinLabel(tickIndex));

        if (mArePinsTemporary) {
            ValueAnimator animator = ValueAnimator.ofFloat(mExpandedPinRadius, 0);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mPinRadius = (Float) (animation.getAnimatedValue());
                    pinView.setSize(mPinRadius,
                            mPinPadding - (mPinPadding * animation.getAnimatedFraction()));
                    invalidate();
                }
            });
            animator.start();
        } else {
            invalidate();
        }

        pinView.release();
    }

    /**
     * Calculates the value for the tickmark at index n.
     *
     * @param tickIndex the index to get the value for
     */
    public String getPinLabel(int tickIndex) {
        if (mValueFormatter == null) {
            return Integer.toString(tickIndex);
        }
        return mValueFormatter.getLabel(tickIndex);
    }

    /**
     * Moves the thumb to the given x-coordinate.
     *
     * @param pinView the PinView to move
     * @param point   the point to move the PinView to
     */
    private void movePin(PinView pinView, PointF point) {
        PbLog.e(TAG, "movePin(pinView=" + pinView + ", point=" + point + ')');
        if (pinView.getPosition().equals(point)) {
            return;
        }

        pinView.setPosition(point);
        invalidate();
    }

    // Bar Implementation ///////////////////////////////////////////////////

    protected Paint mBarPaint;
    protected Paint mTickPaint;
    protected Paint mFirstConnectingLinePaint;
    protected Paint mSecondConnectingLinePaint;


    protected void initBar() {
        // Initialize the paint.
        mBarPaint = new Paint();
        mBarPaint.setAntiAlias(true);
        mBarPaint.setStyle(Paint.Style.STROKE);
        mBarPaint.setColor(mBarColor);
        mBarPaint.setStrokeWidth(mBarWeight);

        mTickPaint = new Paint();
        mTickPaint.setAntiAlias(true);
        mTickPaint.setColor(mTickColor);

        // Initialize the paint, set values
        mFirstConnectingLinePaint = new Paint();
        mFirstConnectingLinePaint.setStrokeCap(Paint.Cap.ROUND);
        mFirstConnectingLinePaint.setStyle(Paint.Style.STROKE);
        mFirstConnectingLinePaint.setAntiAlias(true);
        mFirstConnectingLinePaint.setColor(mFirstConnectingLineColor);
        mFirstConnectingLinePaint.setStrokeWidth(mConnectingLineWeight);

        mSecondConnectingLinePaint = new Paint();
        mSecondConnectingLinePaint.setStrokeCap(Paint.Cap.ROUND);
        mSecondConnectingLinePaint.setStyle(Paint.Style.STROKE);
        mSecondConnectingLinePaint.setAntiAlias(true);
        mSecondConnectingLinePaint.setColor(mSecondConnectingLineColor);
        mSecondConnectingLinePaint.setStrokeWidth(mConnectingLineWeight);
    }

    protected void resizeBar(int w, int h) {
        // Nothing to do here; Reserved for sublcasses
    }

    /**
     * Draws the tick marks on the bar.
     *
     * @param canvas Canvas to draw on; should be the Canvas passed into {#link
     *               View#onDraw()}
     */
    protected void drawTicks(Canvas canvas) {
        PointF tempPoint = new PointF();
        for (int i = 0; i < mTickCount; i++) {
            getPointOfIndex(i, tempPoint);
            canvas.drawCircle(tempPoint.x, tempPoint.y, mTickSize, mTickPaint);
        }
    }

    /**
     * Gets the zero-based index of the nearest tick to the given thumb.
     *
     * @param pinView the PinView to find the nearest tick for
     * @return the zero-based index of the nearest tick
     */
    protected int getNearestIndex(PinView pinView) {
        return getNearestIndex(pinView.getPosition());
    }

    /**
     * Gets the x/y-coordinates of the nearest tick to the given point.
     *
     * @param pointIn  the point of the nearest tick
     * @param pointOut the nearest tick will be stored in this object
     */
    protected void getNearestIndexPosition(PointF pointIn, PointF pointOut) {
        final int nearestIndex = getNearestIndex(pointIn);
        getPointOfIndex(nearestIndex, pointOut);
    }

    /**
     * Draw the connecting line between the two thumbs in RangeBar.
     *
     * @param canvas    the Canvas to draw to
     * @param firstPin  the first pin
     * @param secondPin the second pin
     */
    protected void drawConnectingLine(Canvas canvas, PinView firstPin, PinView secondPin) {
        drawConnectingLine(canvas, firstPin.getPosition(), secondPin.getPosition());
    }

    /**
     * Compares the two points as they relate to the bar. If point1
     * is before point2, result will be <0. If they're at the same point,
     * 0, and if point1 is after point2, then result will be >0.
     *
     * @param point1
     * @param point2
     * @return
     */
    protected abstract int comparePointsOnBar(PointF point1, PointF point2);

    /**
     * Gets the point on the bar nearest to the passed point.
     *
     * @param pointIn  the point of the nearest point on the bar
     * @param pointOut the nearest point will be stored in this object
     */
    protected abstract void getNearestPointOnBar(PointF pointIn, PointF pointOut);

    /**
     * Gets the zero-based index of the nearest tick to the given point.
     *
     * @param point the point to find the nearest tick for
     * @return the zero-based index of the nearest tick
     */
    protected abstract int getNearestIndex(PointF point);

    /**
     * Gets the coordinates of the index-th tick.
     */
    protected abstract void getPointOfIndex(int index, PointF pointOut);

    /**
     * Draws the bar on the given Canvas.
     *
     * @param canvas Canvas to draw on; should be the Canvas passed into {#link
     *               View#onDraw()}
     */
    protected abstract void drawBar(Canvas canvas);

    /**
     * Draw a connecting line between two points that have been precalculated to be on the bar.
     *
     * @param canvas
     * @param firstPoint
     * @param secondPoint
     */
    protected abstract void drawConnectingLine(Canvas canvas, PointF firstPoint, PointF secondPoint);

    // Interfaces ///////////////////////////////////////////////////////////

    /**
     * A callback that notifies clients when the RangeBar has changed. The
     * listener will only be called when either thumb's index has changed - not
     * for every movement of the thumb.
     */
    public interface OnRangeBarChangeListener {
        void onRangeChangeListener(AbsRangeBar rangeBar, int firstIndex, int secondIndex);
    }

    public interface ValueFormatter {
        String getLabel(int index);
    }
}
