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

import java.util.Arrays;
import java.util.Comparator;

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

	private static final String TAG = "RangeBar";

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
	private int mSelectorColor;
	private float mSelectorSize;

	// Pins
	private PinView[] mPinView;
	private int[] mPinIndex;
	private int mPinColor;
	private float mPinRadius;
	private float mExpandedPinRadius;
	private float mMinPinFont;
	private float mMaxPinFont;
	private float mPinPadding;
	private int mPinTextColor;
	private boolean mArePinsTemporary = true;

	// Connecting Line
	private float mConnectingLineWeight;
	private int mConnectingLineColor;

	// Listeners
	private OnRangeBarChangeListener mListener;

	private boolean mIsRangeBar = true;
	private int mActiveConnectingLineColor;
	private int mActiveBarColor;
	private int mActiveTickColor;
	private int mActiveCircleColor;

	private ValueFormatter mValueFormatter = new ValueFormatter() {
		@Override
		public String getLabel(int index) {
			String value = Integer.toString(index);
			if (value.length() > 4) {
				return value.substring(0, 4);
			}
			else {
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
	private void init(Context context, AttributeSet attrs) {
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.AbsRangeBar, 0, 0);

		try {
			// Sets the values of the user-defined attributes based on the XML attributes.
			mTickCount = ta.getInt(R.styleable.AbsRangeBar_tickCount, DEFAULT_TICK_COUNT);
			validateTickCount();
			mPinIndex = new int[] { 0, mTickCount - 1 };

			if (mListener != null) {
				mListener.onRangeChangeListener(this, mPinIndex);
			}

			float density = context.getResources().getDisplayMetrics().density;
			mTickSize = ta.getDimension(R.styleable.AbsRangeBar_tickHeight,
				DEFAULT_TICK_SIZE_DP * density);
			mBarWeight = ta.getDimension(R.styleable.AbsRangeBar_barWeight,
				DEFAULT_BAR_WEIGHT_DP * density);
			mBarColor = ta.getColor(R.styleable.AbsRangeBar_rangeBarColor,
				DEFAULT_BAR_COLOR);
			mPinTextColor = ta.getColor(R.styleable.AbsRangeBar_textColor,
				DEFAULT_TEXT_COLOR);
			mPinColor = ta.getColor(R.styleable.AbsRangeBar_pinColor,
				DEFAULT_PIN_COLOR);
			mActiveBarColor = mBarColor;
			mSelectorSize = ta.getDimension(R.styleable.AbsRangeBar_selectorSize,
				DEFAULT_CIRCLE_SIZE_DP * density);
			mSelectorColor = ta.getColor(R.styleable.AbsRangeBar_selectorColor,
				DEFAULT_CONNECTING_LINE_COLOR);
			mActiveCircleColor = mSelectorColor;
			mTickColor = ta.getColor(R.styleable.AbsRangeBar_tickColor,
				DEFAULT_TICK_COLOR);
			mActiveTickColor = mTickColor;
			mConnectingLineWeight = ta.getDimension(R.styleable.AbsRangeBar_connectingLineWeight,
				DEFAULT_CONNECTING_LINE_WEIGHT_DP * density);
			mConnectingLineColor = ta.getColor(R.styleable.AbsRangeBar_connectingLineColor,
				DEFAULT_CONNECTING_LINE_COLOR);
			mActiveConnectingLineColor = mConnectingLineColor;
			mExpandedPinRadius = ta.getDimension(R.styleable.AbsRangeBar_pinRadius,
				DEFAULT_EXPANDED_PIN_RADIUS_DP * density);
			mPinPadding = ta.getDimension(R.styleable.AbsRangeBar_pinPadding,
				DEFAULT_PIN_PADDING_DP * density);
			mIsRangeBar = ta.getBoolean(R.styleable.AbsRangeBar_rangeBar, true);
			mArePinsTemporary = ta.getBoolean(R.styleable.AbsRangeBar_temporaryPins, true);

			float sensity = getResources().getDisplayMetrics().scaledDensity;
			mMinPinFont = ta.getDimension(R.styleable.AbsRangeBar_pinMinFont,
				DEFAULT_MIN_PIN_FONT_SP * sensity);
			mMaxPinFont = ta.getDimension(R.styleable.AbsRangeBar_pinMaxFont,
				DEFAULT_MAX_PIN_FONT_SP * sensity);
		}
		finally {
			ta.recycle();
		}

		initBar();
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
		bundle.putInt("CONNECTING_LINE_COLOR", mConnectingLineColor);

		bundle.putFloat("SELECTOR_SIZE", mSelectorSize);
		bundle.putInt("SELECTOR_COLOR", mSelectorColor);
		bundle.putFloat("PIN_RADIUS", mPinRadius);
		bundle.putFloat("EXPANDED_PIN_RADIUS", mExpandedPinRadius);
		bundle.putFloat("PIN_PADDING", mPinPadding);
		bundle.putBoolean("IS_RANGE_BAR", mIsRangeBar);
		bundle.putBoolean("ARE_PINS_TEMPORARY", mArePinsTemporary);
		bundle.putIntArray("PIN_INDEXES", mPinIndex);

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
			mSelectorColor = bundle.getInt("SELECTOR_COLOR");
			mConnectingLineWeight = bundle.getFloat("CONNECTING_LINE_WEIGHT");
			mConnectingLineColor = bundle.getInt("CONNECTING_LINE_COLOR");

			mPinRadius = bundle.getFloat("PIN_RADIUS");
			mExpandedPinRadius = bundle.getFloat("EXPANDED_PIN_RADIUS");
			mPinPadding = bundle.getFloat("PIN_PADDING");
			mIsRangeBar = bundle.getBoolean("IS_RANGE_BAR");
			mArePinsTemporary = bundle.getBoolean("ARE_PINS_TEMPORARY");

			mPinIndex = bundle.getIntArray("PIN_INDEXES");

			mMinPinFont = bundle.getFloat("MIN_PIN_FONT");
			mMaxPinFont = bundle.getFloat("MAX_PIN_FONT");

			super.onRestoreInstanceState(bundle.getParcelable("instanceState"));

		}
		else {
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
		}
		else if (measureWidthMode == MeasureSpec.EXACTLY) {
			width = measureWidth;
		}
		else {
			width = DEFAULT_WIDTH;
		}

		// The RangeBar height should be as small as possible.
		if (measureHeightMode == MeasureSpec.AT_MOST) {
			height = Math.min(DEFAULT_HEIGHT, measureHeight);
		}
		else if (measureHeightMode == MeasureSpec.EXACTLY) {
			height = measureHeight;
		}
		else {
			height = DEFAULT_HEIGHT;
		}

		setMeasuredDimension(width, height);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		// This is the initial point at which we know the size of the View.
		resizeBar(w, h);

		initPins();

		updatePinIndicesAndLabelsBasedOnPosition();
	}

	private void updatePinIndicesAndLabelsBasedOnPosition() {
		// Set the thumb indices.
		boolean changed = false;
		for (int i = 0; i < mPinIndex.length; i++) {
			int newIndex = getNearestTickIndex(mPinView[i]);
			if (newIndex != mPinIndex[i]) {
				changed = true;
				mPinIndex[i] = newIndex;
				mPinView[i].setLabel(getPinLabel(newIndex));
			}
		}

		// Call the listener.
		if (changed && mListener != null) {
			mListener.onRangeChangeListener(this, mPinIndex);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		drawBar(canvas);
		if (mDrawTicks) {
			drawTicks(canvas);
		}
		if (mIsRangeBar) {
			drawConnectingLine(canvas, mPinView[0], mPinView[1]);
			mPinView[0].draw(canvas);
		}
		else {
			drawConnectingLine(canvas, mPinView[1]);
		}
		mPinView[1].draw(canvas);

	}

	// Touch Methods ////////////////////////////////////////////////////////////

	private boolean mIsDragging;

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		// If this View is not enabled, don't allow for touch interactions.
		if (!isEnabled()) {
			return false;
		}

		switch (event.getAction()) {

		case MotionEvent.ACTION_DOWN:
			if (isInTargetZone(event.getX(), event.getY())) {
				onActionDown(event.getX(), event.getY());
				setPressed(true);
				onStartTrackingTouch();
				attemptClaimDrag();
			}
			break;

		case MotionEvent.ACTION_MOVE:
			if (mIsDragging) {
				onActionMove(event.getX(), event.getY());
			}
			break;

		case MotionEvent.ACTION_UP:
			if (mIsDragging) {
				onActionUp(event.getX(), event.getY());
				onStopTrackingTouch();
				setPressed(false);
			}
			else {
				// Touch up when we never crossed the touch slop threshold should
				// be interpreted as a tap-seek to that location. But let's not do that now.
			}
			break;

		case MotionEvent.ACTION_CANCEL:
			if (mIsDragging) {
				onStopTrackingTouch();
				setPressed(false);
			}
			break;

		}

		return true;
	}

	// From AbsSeekBar.java //////////////////////////////////////////////////////////
	// https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/widget/AbsSeekBar.java

	/**
	 * Tries to claim the user's drag motion, and requests disallowing any
	 * ancestors from stealing events in the drag.
	 */
	private void attemptClaimDrag() {
		ViewParent parent = getParent();
		if (parent != null) {
			parent.requestDisallowInterceptTouchEvent(true);
		}
	}

	/**
	 * This is called when the user has started touching this widget.
	 */
	void onStartTrackingTouch() {
		mIsDragging = true;
	}

	/**
	 * This is called when the user either releases his touch or the touch is
	 * canceled.
	 */
	void onStopTrackingTouch() {
		mIsDragging = false;
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
		this.mValueFormatter = formatter;
	}

	/**
	 * Enables or disables the drawing of tick marks.
	 *
	 * @param enable
	 */
	public void enableDrawTicks(boolean enable) {
		this.mDrawTicks = enable;
	}

	/**
	 * Sets up the ticks in the RangeBar.
	 *
	 * @param count int specifying the number of ticks to display
	 */
	public void setTicks(int count) {
		mTickCount = count;
		validateTickCount();
		clampPins();
	}

	private void clampPins() {
		for (int i = 0; i < mPinIndex.length; i++) {
			int tmp = clampIndex(mPinIndex[i], 0, mTickCount - 1);
			setPinIndex(i, tmp);
			getPointOfTick(mTmpPoint, tmp);
			mPinView[i].setPosition(mTmpPoint);
		}
		invalidate();
	}

	private void setPinIndex(int index, int value) {
		if (mPinIndex[index] == value) {
			return;
		}

		mPinIndex[index] = value;
		if (mListener != null) {
			mListener.onRangeChangeListener(this, mPinIndex);
		}

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
		mPinColor = pinColor;
		initPins();
	}

	/**
	 * Set the color of the text within the pin.
	 *
	 * @param textColor Integer specifying the color of the text in the pin.
	 */
	public void setPinTextColor(int textColor) {
		mPinTextColor = textColor;
		initPins();
	}

	/**
	 * Set if the view is a range bar or a seek bar.
	 *
	 * @param isRangeBar Boolean - true sets it to rangebar, false to seekbar.
	 */
	public void setRangeBarEnabled(boolean isRangeBar) {
		mIsRangeBar = isRangeBar;
		if (!mIsRangeBar) {
			setPinIndex(0, 0);
		}
		invalidate();
	}

	/**
	 * Set if the pins should dissapear after released
	 *
	 * @param arePinsTemporary Boolean - true if pins shoudl dissapear after released, false to stay
	 *                         drawn
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
		mSelectorColor = selectorColor;
		initPins();
	}

	/**
	 * Set the weight of the connecting line between the thumbs.
	 *
	 * @param connectingLineWeight Float specifying the weight of the connecting
	 *                             line.
	 */
	public void setConnectingLineWeight(float connectingLineWeight) {
		mConnectingLineWeight = connectingLineWeight;
		mConnectingLinePaint.setStrokeWidth(mConnectingLineWeight);
		invalidate();
	}

	/**
	 * Set the color of the connecting line between the thumbs.
	 *
	 * @param connectingLineColor Integer specifying the color of the connecting
	 *                            line.
	 */
	public void setConnectingLineColor(int connectingLineColor) {
		mConnectingLineColor = connectingLineColor;
		mConnectingLinePaint.setColor(mConnectingLineColor);
		invalidate();
	}

	/**
	 * If this is set, the thumb images will be replaced with a circle of the
	 * specified radius. Default width = 20dp.
	 *
	 * @param pinRadius Float specifying the radius of the thumbs to be drawn.
	 */
	public void setPinRadius(float pinRadius) {
		mExpandedPinRadius = pinRadius;
		initPins();
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
	 * Sets the indices of the pins of this range bar. Silently adjust to the bar's ticks.
	 * Silently sorts the array of indices.
	 *
	 * @param indices
	 */
	public void setPinIndices(int[] indices) {
		if (!isRangeBar()) {
			throw new RuntimeException("Cannot set multiple values on a non Range Bar");
		}
		Arrays.sort(indices);
		clampPins();
	}

	/**
	 * Sets the index of the right pin. Silently adjust to the bar's ticks.
	 * Silently swaps left and right pins, if left is now greater than right.
	 *
	 * @param index
	 */
	public void setPinIndex(int index) {
		if (isRangeBar()) {
			throw new RuntimeException("Cannot set single value on a Range Bar");
		}
		setPinIndex(0, 0);
		setPinIndex(1, index);
		Arrays.sort(mPinIndex);
		clampPins();
	}

	private int clampIndex(int index, int min, int max) {
		return index < min ? min
			: index > max ? max
				: index;
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
	 * Gets the index of the left-most pin.
	 *
	 * @return the 0-based index of the left pin
	 */
	public int getLeftIndex() {
		return mPinIndex[0];
	}

	/**
	 * Gets the index of the right-most pin.
	 *
	 * @return the 0-based index of the right pin
	 */
	public int getRightIndex() {
		return mPinIndex[1];
	}

	@Override
	public void setEnabled(boolean enabled) {
		if (!enabled) {
			mBarColor = DEFAULT_BAR_COLOR;
			mConnectingLineColor = DEFAULT_BAR_COLOR;
			mSelectorColor = DEFAULT_BAR_COLOR;
			mTickColor = DEFAULT_BAR_COLOR;
		}
		else {
			mBarColor = mActiveBarColor;
			mConnectingLineColor = mActiveConnectingLineColor;
			mSelectorColor = mActiveCircleColor;
			mTickColor = mActiveTickColor;
		}

		mBarPaint.setColor(mBarColor);
		mBarPaint.setStrokeWidth(mBarWeight);
		mTickPaint.setColor(mTickColor);
		mConnectingLinePaint.setColor(mConnectingLineColor);
		mConnectingLinePaint.setStrokeWidth(mConnectingLineWeight);

		initPins();
		super.setEnabled(enabled);
	}

	// Private Methods /////////////////////////////////////////////////////////

	/**
	 * Initializes (and creates if necessary) the one or two Pins.
	 */
	private void initPins() {
		Context ctx = getContext();

		if (mPinView == null) {
			mPinView = new PinView[2];
		}

		if (mIsRangeBar) {
			PointF leftPoint = new PointF();
			getPointOfTick(leftPoint, mPinIndex[0]);
			if (mPinView[0] == null) {
				mPinView[0] = new PinView(ctx);
			}
			Arrays.sort(mPinIndex);
			mPinView[0].init(leftPoint,
				0, mPinColor, mPinTextColor,
				mSelectorSize, mSelectorColor,
				mMinPinFont, mMaxPinFont);
			mPinView[0].setLabel(getPinLabel(mPinIndex[0]));
		}

		PointF rightPoint = new PointF();
		getPointOfTick(rightPoint, mPinIndex[1]);
		if (mPinView[1] == null) {
			mPinView[1] = new PinView(ctx);
		}
		mPinView[1].init(rightPoint,
			0, mPinColor, mPinTextColor,
			mSelectorSize, mSelectorColor,
			mMinPinFont, mMaxPinFont);
		mPinView[1].setLabel(getPinLabel(mPinIndex[1]));

		invalidate();
	}

	/**
	 * If is invalid tickCount, rejects. TickCount must be greater than 1
	 */
	private void validateTickCount() {
		if (mTickCount < 2) {
			throw new RuntimeException("TickCount less than 2; invalid tickCount.");
		}
	}

	private boolean isInTargetZone(float x, float y) {
		for (int i = 0; i < mPinView.length; i++) {
			if (mPinView[i] != null && mPinView[i].isInTargetZone(x, y)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Handles a {@link android.view.MotionEvent#ACTION_DOWN} event.
	 *
	 * @param x the x-coordinate of the down action
	 * @param y the y-coordinate of the down action
	 */
	private void onActionDown(float x, float y) {
		for (int i = 0; i < mPinView.length; i++) {
			if (mPinView[i] != null && mPinView[i].isInTargetZone(x, y)) {
				pressPin(mPinView[i]);
				return;
			}
		}
	}

	/**
	 * Handles a {@link android.view.MotionEvent#ACTION_MOVE} event.
	 *
	 * @param x the x-coordinate of the move event
	 * @param y the y-coordinate of the move event
	 */
	private void onActionMove(float x, float y) {
		PointF point = new PointF(x, y);
		getNearestPointOnBar(point, point);

		// Move the pressed thumb to the new position.
		for (int i = 0; i < mPinView.length; i++) {
			if (mPinView[i] != null && mPinView[i].isPressed()) {
				movePin(mPinView[i], point);
			}
		}

		// If the thumbs have switched order, fix the references.
		Arrays.sort(mPinView, mPinViewComparator);

		updatePinIndicesAndLabelsBasedOnPosition();
	}

	private Comparator<PinView> mPinViewComparator = new Comparator<PinView>() {
		@Override
		public int compare(PinView lhs, PinView rhs) {
			return comparePointsOnBar(lhs.getPosition(), rhs.getPosition());
		}
	};

	/**
	 * Handles a {@link android.view.MotionEvent#ACTION_UP} event.
	 *
	 * @param x the x-coordinate of the up action
	 * @param y the y-coordinate of the up action
	 */
	private void onActionUp(float x, float y) {
		for (int i = 0; i < mPinView.length; i++) {
			if (mPinView[i] != null && mPinView[i].isPressed()) {
				releasePin(mPinView[i]);
			}
		}
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
		}

		thumb.press();

	}

	/**
	 * Set the thumb to be in the normal/un-pressed state and calls invalidate()
	 * to redraw the canvas to reflect the updated state.
	 *
	 * @param thumb the thumb to release
	 */
	private void releasePin(final PinView thumb) {
		PointF point = new PointF();
		getNearestTickPosition(point, thumb.getPosition());
		thumb.setPosition(point);
		int tickIndex = getNearestTickIndex(thumb);

		thumb.setLabel(getPinLabel(tickIndex));

		if (mArePinsTemporary) {
			ValueAnimator animator = ValueAnimator.ofFloat(mExpandedPinRadius, 0);
			animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					mPinRadius = (Float) (animation.getAnimatedValue());
					thumb.setSize(mPinRadius,
						mPinPadding - (mPinPadding * animation.getAnimatedFraction()));
					invalidate();
				}
			});
			animator.start();
		}
		else {
			invalidate();
		}

		thumb.release();
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
	 * @param thumb the thumb to move
	 * @param point the x-coordinate to move the thumb to
	 */
	private void movePin(PinView thumb, PointF point) {
		if (thumb.getPosition().equals(point)) {
			return;
		}

		thumb.setPosition(point);
		invalidate();
	}

	// Bar Implementation ///////////////////////////////////////////////////

	protected Paint mBarPaint;
	protected Paint mTickPaint;
	protected Paint mConnectingLinePaint;

	PointF mTmpPoint;

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
		mConnectingLinePaint = new Paint();
		mConnectingLinePaint.setStrokeCap(Paint.Cap.ROUND);
		mConnectingLinePaint.setStyle(Paint.Style.STROKE);
		mConnectingLinePaint.setAntiAlias(true);
		mConnectingLinePaint.setColor(mConnectingLineColor);
		mConnectingLinePaint.setStrokeWidth(mConnectingLineWeight);

		mTmpPoint = new PointF();
	}

	protected void resizeBar(int w, int h) {
		// Nothing to do here
	}

	/**
	 * Draws the tick marks on the bar.
	 *
	 * @param canvas Canvas to draw on; should be the Canvas passed into {#link
	 *               View#onDraw()}
	 */
	protected void drawTicks(Canvas canvas) {
		for (int i = 0; i < mTickCount; i++) {
			getPointOfTick(mTmpPoint, i);
			canvas.drawCircle(mTmpPoint.x, mTmpPoint.y, mTickSize, mTickPaint);
		}
	}

	/**
	 * Gets the zero-based index of the nearest tick to the given thumb.
	 *
	 * @param thumb the Thumb to find the nearest tick for
	 * @return the zero-based index of the nearest tick
	 */
	protected int getNearestTickIndex(PinView thumb) {
		return getNearestTickIndex(thumb.getPosition());
	}

	/**
	 * Gets the x/y-coordinates of the nearest tick to the given point.
	 *
	 * @param out   the nearest tick will be stored in this object
	 * @param point the point of the nearest tick
	 */
	protected void getNearestTickPosition(PointF out, PointF point) {
		final int nearestTickIndex = getNearestTickIndex(point);
		getPointOfTick(out, nearestTickIndex);
	}

	/**
	 * Draw the connecting line between the two thumbs in RangeBar.
	 *
	 * @param canvas     the Canvas to draw to
	 * @param leftThumb  the left thumb
	 * @param rightThumb the right thumb
	 */
	protected void drawConnectingLine(Canvas canvas, PinView leftThumb, PinView rightThumb) {
		drawConnectingLine(canvas, leftThumb.getPosition(), rightThumb.getPosition());
	}

	/**
	 * Draw the connecting line between for single slider.
	 *
	 * @param canvas     the Canvas to draw to
	 * @param rightThumb the right thumb
	 */
	protected void drawConnectingLine(Canvas canvas, PinView rightThumb) {
		getPointOfTick(mTmpPoint, 0);
		drawConnectingLine(canvas, mTmpPoint, rightThumb.getPosition());
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
	 * @param out   the nearest point will be stored in this object
	 * @param point the point of the nearest point on the bar
	 */
	protected abstract void getNearestPointOnBar(PointF out, PointF point);

	/**
	 * Gets the zero-based index of the nearest tick to the given point.
	 *
	 * @param point the point to find the nearest tick for
	 * @return the zero-based index of the nearest tick
	 */
	protected abstract int getNearestTickIndex(PointF point);

	/**
	 * Gets the coordinates of the index-th tick.
	 */
	protected abstract void getPointOfTick(PointF out, int index);

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
	 * @param point1
	 * @param point2
	 */
	protected abstract void drawConnectingLine(Canvas canvas, PointF point1, PointF point2);

	// Interfaces ///////////////////////////////////////////////////////////

	/**
	 * A callback that notifies clients when the RangeBar has changed. The
	 * listener will only be called when either thumb's index has changed - not
	 * for every movement of the thumb.
	 */
	public interface OnRangeBarChangeListener {
		void onRangeChangeListener(AbsRangeBar rangeBar, int[] indices);
	}

	public interface ValueFormatter {
		String getLabel(int index);
	}

}
