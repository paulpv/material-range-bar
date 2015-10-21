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
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
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
	private static final float DEFAULT_TICK_START = 0;

	private static final float DEFAULT_TICK_END = 5;

	private static final float DEFAULT_TICK_INTERVAL = 1;

	private static final float DEFAULT_TICK_HEIGHT_DP = 1;

	private static final float DEFAULT_PIN_PADDING_DP = 16;

	public static final float DEFAULT_MIN_PIN_FONT_SP = 8;

	public static final float DEFAULT_MAX_PIN_FONT_SP = 24;

	private static final float DEFAULT_BAR_WEIGHT_PX = 2;

	private static final int DEFAULT_BAR_COLOR = Color.LTGRAY;

	private static final int DEFAULT_TEXT_COLOR = Color.WHITE;

	private static final int DEFAULT_TICK_COLOR = Color.BLACK;

	// Corresponds to material indigo 500.
	private static final int DEFAULT_PIN_COLOR = 0xff3f51b5;

	private static final float DEFAULT_CONNECTING_LINE_WEIGHT_PX = 4;

	// Corresponds to material indigo 500.
	private static final int DEFAULT_CONNECTING_LINE_COLOR = 0xff3f51b5;

	private static final float DEFAULT_EXPANDED_PIN_RADIUS_DP = 12;

	private static final float DEFAULT_CIRCLE_SIZE_DP = 5;

	private static final float DEFAULT_BAR_PADDING_BOTTOM_DP = 24;

	// Instance variables for all of the customizable attributes

	private float mTickSize = DEFAULT_TICK_HEIGHT_DP;

	private float mTickStart = DEFAULT_TICK_START;

	private float mTickEnd = DEFAULT_TICK_END;

	private float mTickInterval = DEFAULT_TICK_INTERVAL;

	private float mBarWeight = DEFAULT_BAR_WEIGHT_PX;

	private int mBarColor = DEFAULT_BAR_COLOR;

	private int mPinColor = DEFAULT_PIN_COLOR;

	private int mTextColor = DEFAULT_TEXT_COLOR;

	private float mConnectingLineWeight = DEFAULT_CONNECTING_LINE_WEIGHT_PX;

	private int mConnectingLineColor = DEFAULT_CONNECTING_LINE_COLOR;

	private float mThumbRadiusDP = DEFAULT_EXPANDED_PIN_RADIUS_DP;

	private int mTickColor = DEFAULT_TICK_COLOR;

	private float mExpandedPinRadius = DEFAULT_EXPANDED_PIN_RADIUS_DP;

	private int mCircleColor = DEFAULT_CONNECTING_LINE_COLOR;

	private float mCircleSize = DEFAULT_CIRCLE_SIZE_DP;

	private float mMinPinFont = DEFAULT_MIN_PIN_FONT_SP;

	private float mMaxPinFont = DEFAULT_MAX_PIN_FONT_SP;

	// setTickCount only resets indices before a thumb has been pressed or a
	// setThumbIndices() is called, to correspond with intended usage
	private boolean mFirstSetTickCount = true;

	private int mDefaultWidth = 500;

	private int mDefaultHeight = 150;

	protected int mTickCount = (int) ((mTickEnd - mTickStart) / mTickInterval) + 1;

	private PinView mLeftThumb;

	private PinView mRightThumb;

	private OnRangeBarChangeListener mListener;

	private OnRangeBarTextListener mPinTextListener;

	private int mLeftIndex;

	private int mRightIndex;

	private boolean mIsRangeBar = true;

	private float mPinPadding = DEFAULT_PIN_PADDING_DP;

	private int mActiveConnectingLineColor;

	private int mActiveBarColor;

	private int mActiveTickColor;

	private int mActiveCircleColor;

	private boolean mDrawTicks = true;

	private boolean mArePinsTemporary = true;

	private PinTextFormatter mPinTextFormatter = new PinTextFormatter() {
		@Override
		public String getText(String value) {
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
			final float tickStart = ta.getFloat(R.styleable.AbsRangeBar_tickStart, DEFAULT_TICK_START);
			final float tickEnd = ta.getFloat(R.styleable.AbsRangeBar_tickEnd, DEFAULT_TICK_END);
			final float tickInterval = ta.getFloat(R.styleable.AbsRangeBar_tickInterval, DEFAULT_TICK_INTERVAL);
			int tickCount = (int) ((tickEnd - tickStart) / tickInterval) + 1;

			if (isValidTickCount(tickCount)) {
				// Similar functions performed above in setTickCount; make sure
				// you know how they interact
				mTickCount = tickCount;
				mTickStart = tickStart;
				mTickEnd = tickEnd;
				mTickInterval = tickInterval;
				mLeftIndex = 0;
				mRightIndex = mTickCount - 1;

				if (mListener != null) {
					mListener.onRangeChangeListener(this, mLeftIndex, mRightIndex,
						getPinValue(mLeftIndex),
						getPinValue(mRightIndex));
				}
			}
			else {
				Log.e(TAG, "tickCount less than 2; invalid tickCount. XML input ignored.");
			}

			float density = context.getResources().getDisplayMetrics().density;
			mTickSize = ta.getDimension(R.styleable.AbsRangeBar_tickHeight,
				DEFAULT_TICK_HEIGHT_DP * density);
			mBarWeight = ta.getDimension(R.styleable.AbsRangeBar_barWeight,
				DEFAULT_BAR_WEIGHT_PX);
			mBarColor = ta.getColor(R.styleable.AbsRangeBar_rangeBarColor,
				DEFAULT_BAR_COLOR);
			mTextColor = ta.getColor(R.styleable.AbsRangeBar_textColor,
				DEFAULT_TEXT_COLOR);
			mPinColor = ta.getColor(R.styleable.AbsRangeBar_pinColor,
				DEFAULT_PIN_COLOR);
			mActiveBarColor = mBarColor;
			mCircleSize = ta.getDimension(R.styleable.AbsRangeBar_selectorSize,
				DEFAULT_CIRCLE_SIZE_DP * density);
			mCircleColor = ta.getColor(R.styleable.AbsRangeBar_selectorColor,
				DEFAULT_CONNECTING_LINE_COLOR);
			mActiveCircleColor = mCircleColor;
			mTickColor = ta.getColor(R.styleable.AbsRangeBar_tickColor,
				DEFAULT_TICK_COLOR);
			mActiveTickColor = mTickColor;
			mConnectingLineWeight = ta.getDimension(R.styleable.AbsRangeBar_connectingLineWeight,
				DEFAULT_CONNECTING_LINE_WEIGHT_PX);
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

		mScaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

		initBar();
	}

	// View Methods ////////////////////////////////////////////////////////////

	@Override
	public Parcelable onSaveInstanceState() {

		Bundle bundle = new Bundle();

		bundle.putParcelable("instanceState", super.onSaveInstanceState());

		bundle.putInt("TICK_COUNT", mTickCount);
		bundle.putFloat("TICK_START", mTickStart);
		bundle.putFloat("TICK_END", mTickEnd);
		bundle.putFloat("TICK_INTERVAL", mTickInterval);
		bundle.putInt("TICK_COLOR", mTickColor);

		bundle.putFloat("TICK_HEIGHT_DP", mTickSize);
		bundle.putFloat("BAR_WEIGHT", mBarWeight);
		bundle.putInt("BAR_COLOR", mBarColor);
		bundle.putFloat("CONNECTING_LINE_WEIGHT", mConnectingLineWeight);
		bundle.putInt("CONNECTING_LINE_COLOR", mConnectingLineColor);

		bundle.putFloat("CIRCLE_SIZE", mCircleSize);
		bundle.putInt("CIRCLE_COLOR", mCircleColor);
		bundle.putFloat("THUMB_RADIUS_DP", mThumbRadiusDP);
		bundle.putFloat("EXPANDED_PIN_RADIUS_DP", mExpandedPinRadius);
		bundle.putFloat("PIN_PADDING", mPinPadding);
		bundle.putBoolean("IS_RANGE_BAR", mIsRangeBar);
		bundle.putBoolean("ARE_PINS_TEMPORARY", mArePinsTemporary);
		bundle.putInt("LEFT_INDEX", mLeftIndex);
		bundle.putInt("RIGHT_INDEX", mRightIndex);

		bundle.putBoolean("FIRST_SET_TICK_COUNT", mFirstSetTickCount);

		bundle.putFloat("MIN_PIN_FONT", mMinPinFont);
		bundle.putFloat("MAX_PIN_FONT", mMaxPinFont);

		return bundle;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {

		if (state instanceof Bundle) {

			Bundle bundle = (Bundle) state;

			mTickCount = bundle.getInt("TICK_COUNT");
			mTickStart = bundle.getFloat("TICK_START");
			mTickEnd = bundle.getFloat("TICK_END");
			mTickInterval = bundle.getFloat("TICK_INTERVAL");
			mTickColor = bundle.getInt("TICK_COLOR");
			mTickSize = bundle.getFloat("TICK_HEIGHT_DP");
			mBarWeight = bundle.getFloat("BAR_WEIGHT");
			mBarColor = bundle.getInt("BAR_COLOR");
			mCircleSize = bundle.getFloat("CIRCLE_SIZE");
			mCircleColor = bundle.getInt("CIRCLE_COLOR");
			mConnectingLineWeight = bundle.getFloat("CONNECTING_LINE_WEIGHT");
			mConnectingLineColor = bundle.getInt("CONNECTING_LINE_COLOR");

			mThumbRadiusDP = bundle.getFloat("THUMB_RADIUS_DP");
			mExpandedPinRadius = bundle.getFloat("EXPANDED_PIN_RADIUS_DP");
			mPinPadding = bundle.getFloat("PIN_PADDING");
			mIsRangeBar = bundle.getBoolean("IS_RANGE_BAR");
			mArePinsTemporary = bundle.getBoolean("ARE_PINS_TEMPORARY");

			mLeftIndex = bundle.getInt("LEFT_INDEX");
			mRightIndex = bundle.getInt("RIGHT_INDEX");
			mFirstSetTickCount = bundle.getBoolean("FIRST_SET_TICK_COUNT");

			mMinPinFont = bundle.getFloat("MIN_PIN_FONT");
			mMaxPinFont = bundle.getFloat("MAX_PIN_FONT");

			setRangePinsByIndices(mLeftIndex, mRightIndex);
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
			width = mDefaultWidth;
		}

		// The RangeBar height should be as small as possible.
		if (measureHeightMode == MeasureSpec.AT_MOST) {
			height = Math.min(mDefaultHeight, measureHeight);
		}
		else if (measureHeightMode == MeasureSpec.EXACTLY) {
			height = measureHeight;
		}
		else {
			height = mDefaultHeight;
		}

		setMeasuredDimension(width, height);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		// This is the initial point at which we know the size of the View.
		resizeBar(w, h);

		createPins();

		// Set the thumb indices.
		final int newLeftIndex = mIsRangeBar ? getNearestTickIndex(mLeftThumb) : 0;
		final int newRightIndex = getNearestTickIndex(mRightThumb);

		// Call the listener.
		if (newLeftIndex != mLeftIndex || newRightIndex != mRightIndex) {
			if (mListener != null) {
				mListener.onRangeChangeListener(this, mLeftIndex, mRightIndex,
					getPinValue(mLeftIndex),
					getPinValue(mRightIndex));
			}
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
			drawConnectingLine(canvas, mLeftThumb, mRightThumb);
			mLeftThumb.draw(canvas);
		}
		else {
			drawConnectingLine(canvas, mRightThumb);
		}
		mRightThumb.draw(canvas);

	}

	// Touch Methods ////////////////////////////////////////////////////////////

	private int mScaledTouchSlop;
	private float mTouchDownX;
	private float mTouchDownY;
	private boolean mIsDragging;

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		// If this View is not enabled, don't allow for touch interactions.
		if (!isEnabled()) {
			return false;
		}

		switch (event.getAction()) {

		case MotionEvent.ACTION_DOWN:
			if (isInScrollingContainer()) {
				mTouchDownX = event.getX();
				mTouchDownY = event.getY();
			}
			else {
				if (isInTargetZone(event.getX(), event.getY())) {
					onActionDown(event.getX(), event.getY());
					setPressed(true);
					onStartTrackingTouch();
					attemptClaimDrag();
				}
			}
			break;

		case MotionEvent.ACTION_MOVE:
			if (mIsDragging) {
				onActionMove(event.getX(), event.getY());
			}
			else {
				final float x = event.getX();
				final float y = event.getY();
				final int distance = (int) Math.sqrt(
					(x - mTouchDownX) * (x - mTouchDownX)
					+ (y - mTouchDownY) * (y - mTouchDownY));
				if (distance > mScaledTouchSlop) {
					if (isInTargetZone(mTouchDownX, mTouchDownY)) {
						onActionDown(mTouchDownX, mTouchDownY);
						setPressed(true);
						onStartTrackingTouch();
						attemptClaimDrag();
						onActionMove(event.getX(), event.getY());
					}
				}
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

	// From View.java
	// https://github.com/android/platform_frameworks_base/blob/master/core/java/android/view/View.java#L10407
	public boolean isInScrollingContainer() {
		ViewParent p = getParent();
		while (p != null && p instanceof ViewGroup) {
			if (((ViewGroup) p).shouldDelayChildPressedState()) {
				return true;
			}
			p = p.getParent();
		}
		return false;
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
	 * Sets a listener to modify the text
	 *
	 * @param mPinTextListener the RangeBar pin text notification listener; null to remove any
	 *                         existing listener
	 */
	public void setPinTextListener(OnRangeBarTextListener mPinTextListener) {
		this.mPinTextListener = mPinTextListener;
	}

	/**
	 * Enables or disables the drawing of tick marks.
	 * @param enable
	 */
	public void enableDrawTicks(boolean enable) {
		this.mDrawTicks = enable;
	}

	/**
	 * Sets the start tick in the RangeBar.
	 *
	 * @param tickStart Integer specifying the number of ticks.
	 */
	public void setTickStart(float tickStart) {
		int tickCount = (int) ((mTickEnd - tickStart) / mTickInterval) + 1;
		if (isValidTickCount(tickCount)) {
			mTickCount = tickCount;
			mTickStart = tickStart;

			// Prevents resetting the indices when creating new activity, but
			// allows it on the first setting.
			if (mFirstSetTickCount) {
				mLeftIndex = 0;
				mRightIndex = mTickCount - 1;

				if (mListener != null) {
					mListener.onRangeChangeListener(this, mLeftIndex, mRightIndex,
						getPinValue(mLeftIndex),
						getPinValue(mRightIndex));
				}
			}

			if (indexOutOfRange(mLeftIndex, mRightIndex)) {
				mLeftIndex = 0;
				mRightIndex = mTickCount - 1;

				if (mListener != null) {
					mListener.onRangeChangeListener(this, mLeftIndex, mRightIndex,
						getPinValue(mLeftIndex),
						getPinValue(mRightIndex));
				}
			}

			mTickPaint.setColor(mTickColor);
			createPins();
		}
		else {
			Log.e(TAG, "tickCount less than 2; invalid tickCount.");
			throw new IllegalArgumentException("tickCount less than 2; invalid tickCount.");
		}
	}

	/**
	 * Sets the start tick in the RangeBar.
	 *
	 * @param tickInterval Integer specifying the number of ticks.
	 */
	public void setTickInterval(float tickInterval) {
		int tickCount = (int) ((mTickEnd - mTickStart) / tickInterval) + 1;
		if (isValidTickCount(tickCount)) {
			mTickCount = tickCount;
			mTickInterval = tickInterval;

			// Prevents resetting the indices when creating new activity, but
			// allows it on the first setting.
			if (mFirstSetTickCount) {
				mLeftIndex = 0;
				mRightIndex = mTickCount - 1;

				if (mListener != null) {
					mListener.onRangeChangeListener(this, mLeftIndex, mRightIndex,
						getPinValue(mLeftIndex), getPinValue(mRightIndex));
				}
			}
			if (indexOutOfRange(mLeftIndex, mRightIndex)) {
				mLeftIndex = 0;
				mRightIndex = mTickCount - 1;

				if (mListener != null) {
					mListener.onRangeChangeListener(this, mLeftIndex, mRightIndex,
						getPinValue(mLeftIndex), getPinValue(mRightIndex));
				}
			}

			mTickPaint.setColor(mTickColor);
			createPins();
		}
		else {
			Log.e(TAG, "tickCount less than 2; invalid tickCount.");
			throw new IllegalArgumentException("tickCount less than 2; invalid tickCount.");
		}
	}

	/**
	 * Sets the end tick in the RangeBar.
	 *
	 * @param tickEnd Integer specifying the number of ticks.
	 */
	public void setTickEnd(float tickEnd) {
		int tickCount = (int) ((tickEnd - mTickStart) / mTickInterval) + 1;
		if (isValidTickCount(tickCount)) {
			mTickCount = tickCount;
			mTickEnd = tickEnd;

			// Prevents resetting the indices when creating new activity, but
			// allows it on the first setting.
			if (mFirstSetTickCount) {
				mLeftIndex = 0;
				mRightIndex = mTickCount - 1;

				if (mListener != null) {
					mListener.onRangeChangeListener(this, mLeftIndex, mRightIndex,
						getPinValue(mLeftIndex), getPinValue(mRightIndex));
				}
			}
			if (indexOutOfRange(mLeftIndex, mRightIndex)) {
				mLeftIndex = 0;
				mRightIndex = mTickCount - 1;

				if (mListener != null) {
					mListener.onRangeChangeListener(this, mLeftIndex, mRightIndex,
						getPinValue(mLeftIndex), getPinValue(mRightIndex));
				}
			}

			mTickPaint.setColor(mTickColor);
			createPins();
		}
		else {
			Log.e(TAG, "tickCount less than 2; invalid tickCount.");
			throw new IllegalArgumentException("tickCount less than 2; invalid tickCount.");
		}
	}

	/**
	 * Sets the height of the ticks in the range bar.
	 *
	 * @param tickHeight Float specifying the height of each tick mark in px.
	 */
	public void setTickHeight(float tickHeight) {
		mTickSize = tickHeight;
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
		createPins();
	}

	/**
	 * Set the color of the text within the pin.
	 *
	 * @param textColor Integer specifying the color of the text in the pin.
	 */
	public void setPinTextColor(int textColor) {
		mTextColor = textColor;
		createPins();
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
		mCircleColor = selectorColor;
		createPins();
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
		createPins();
	}

	/**
	 * Gets the start tick.
	 *
	 * @return the start tick.
	 */
	public float getTickStart() {
		return mTickStart;
	}

	/**
	 * Gets the end tick.
	 *
	 * @return the end tick.
	 */
	public float getTickEnd() {
		return mTickEnd;
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
	 * Sets the location of the pins according by the supplied index.
	 * Numbered from 0 to mTickCount - 1 from the left.
	 *
	 * @param leftPinIndex  Integer specifying the index of the left pin
	 * @param rightPinIndex Integer specifying the index of the right pin
	 */
	public void setRangePinsByIndices(int leftPinIndex, int rightPinIndex) {
		if (indexOutOfRange(leftPinIndex, rightPinIndex)) {
			Log.e(TAG,
				"Pin index left " + leftPinIndex + ", or right " + rightPinIndex
					+ " is out of bounds. Check that it is greater than the minimum ("
					+ mTickStart + ") and less than the maximum value ("
					+ mTickEnd + ")");
			throw new IllegalArgumentException(
				"Pin index left " + leftPinIndex + ", or right " + rightPinIndex
					+ " is out of bounds. Check that it is greater than the minimum ("
					+ mTickStart + ") and less than the maximum value ("
					+ mTickEnd + ")");
		}
		else {

			if (mFirstSetTickCount) {
				mFirstSetTickCount = false;
			}
			mLeftIndex = leftPinIndex;
			mRightIndex = rightPinIndex;
			createPins();

			if (mListener != null) {
				mListener.onRangeChangeListener(this, mLeftIndex, mRightIndex,
					getPinValue(mLeftIndex), getPinValue(mRightIndex));
			}
		}

		invalidate();
		requestLayout();
	}

	/**
	 * Sets the location of pin according by the supplied index.
	 * Numbered from 0 to mTickCount - 1 from the left.
	 *
	 * @param pinIndex Integer specifying the index of the seek pin
	 */
	public void setSeekPinByIndex(int pinIndex) {
		if (pinIndex < 0 || pinIndex > mTickCount) {
			Log.e(TAG,
				"Pin index " + pinIndex
					+ " is out of bounds. Check that it is greater than the minimum ("
					+ 0 + ") and less than the maximum value ("
					+ mTickCount + ")");
			throw new IllegalArgumentException(
				"Pin index " + pinIndex
					+ " is out of bounds. Check that it is greater than the minimum ("
					+ 0 + ") and less than the maximum value ("
					+ mTickCount + ")");

		}
		else {

			if (mFirstSetTickCount) {
				mFirstSetTickCount = false;
			}
			mRightIndex = pinIndex;
			createPins();

			if (mListener != null) {
				mListener.onRangeChangeListener(this, mLeftIndex, mRightIndex,
					getPinValue(mLeftIndex), getPinValue(mRightIndex));
			}
		}
		invalidate();
		requestLayout();
	}

	/**
	 * Sets the location of pins according by the supplied values.
	 *
	 * @param leftPinValue  Float specifying the index of the left pin
	 * @param rightPinValue Float specifying the index of the right pin
	 */
	public void setRangePinsByValue(float leftPinValue, float rightPinValue) {
		if (valueOutOfRange(leftPinValue, rightPinValue)) {
			Log.e(TAG,
				"Pin value left " + leftPinValue + ", or right " + rightPinValue
					+ " is out of bounds. Check that it is greater than the minimum ("
					+ mTickStart + ") and less than the maximum value ("
					+ mTickEnd + ")");
			throw new IllegalArgumentException(
				"Pin value left " + leftPinValue + ", or right " + rightPinValue
					+ " is out of bounds. Check that it is greater than the minimum ("
					+ mTickStart + ") and less than the maximum value ("
					+ mTickEnd + ")");
		}
		else {
			if (mFirstSetTickCount) {
				mFirstSetTickCount = false;
			}

			mLeftIndex = (int) ((leftPinValue - mTickStart) / mTickInterval);
			mRightIndex = (int) ((rightPinValue - mTickStart) / mTickInterval);
			createPins();

			if (mListener != null) {
				mListener.onRangeChangeListener(this, mLeftIndex, mRightIndex,
					getPinValue(mLeftIndex), getPinValue(mRightIndex));
			}
		}
		invalidate();
		requestLayout();
	}

	/**
	 * Sets the location of pin according by the supplied value.
	 *
	 * @param pinValue Float specifying the value of the pin
	 */
	public void setSeekPinByValue(float pinValue) {
		if (pinValue > mTickEnd || pinValue < mTickStart) {
			Log.e(TAG,
				"Pin value " + pinValue
					+ " is out of bounds. Check that it is greater than the minimum ("
					+ mTickStart + ") and less than the maximum value ("
					+ mTickEnd + ")");
			throw new IllegalArgumentException(
				"Pin value " + pinValue
					+ " is out of bounds. Check that it is greater than the minimum ("
					+ mTickStart + ") and less than the maximum value ("
					+ mTickEnd + ")");

		}
		else {
			if (mFirstSetTickCount) {
				mFirstSetTickCount = false;
			}
			mRightIndex = (int) ((pinValue - mTickStart) / mTickInterval);
			createPins();

			if (mListener != null) {
				mListener.onRangeChangeListener(this, mLeftIndex, mRightIndex,
					getPinValue(mLeftIndex), getPinValue(mRightIndex));
			}
		}
		invalidate();
		requestLayout();
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
		return mLeftIndex;
	}

	/**
	 * Gets the index of the right-most pin.
	 *
	 * @return the 0-based index of the right pin
	 */
	public int getRightIndex() {
		return mRightIndex;
	}

	/**
	 * Gets the tick interval.
	 *
	 * @return the tick interval
	 */
	public double getTickInterval() {
		return mTickInterval;
	}

	@Override
	public void setEnabled(boolean enabled) {
		if (!enabled) {
			mBarColor = DEFAULT_BAR_COLOR;
			mConnectingLineColor = DEFAULT_BAR_COLOR;
			mCircleColor = DEFAULT_BAR_COLOR;
			mTickColor = DEFAULT_BAR_COLOR;
		}
		else {
			mBarColor = mActiveBarColor;
			mConnectingLineColor = mActiveConnectingLineColor;
			mCircleColor = mActiveCircleColor;
			mTickColor = mActiveTickColor;
		}

		mBarPaint.setColor(mBarColor);
		mBarPaint.setStrokeWidth(mBarWeight);
		mTickPaint.setColor(mTickColor);
		mConnectingLinePaint.setColor(mConnectingLineColor);
		mConnectingLinePaint.setStrokeWidth(mConnectingLineWeight);

		createPins();
		super.setEnabled(enabled);
	}

	public void setPinTextFormatter(PinTextFormatter pinTextFormatter) {
		this.mPinTextFormatter = pinTextFormatter;
	}

	// Private Methods /////////////////////////////////////////////////////////

	/**
	 * Creates two new Pins.
	 */
	private void createPins() {
		Context ctx = getContext();

		if (mIsRangeBar) {
			PointF leftPoint = new PointF();
			getPointOfTick(leftPoint, 0);
			mLeftThumb = new PinView(ctx);
			mLeftThumb.init(ctx, leftPoint,
				0, mPinColor, mTextColor,
				mCircleSize, mCircleColor,
				mMinPinFont, mMaxPinFont);
			mLeftThumb.setLabel(getPinValue(mLeftIndex));
		}
		PointF rightPoint = new PointF();
		getPointOfTick(rightPoint, mTickCount - 1);
		mRightThumb = new PinView(ctx);
		mRightThumb.init(ctx, rightPoint,
			0, mPinColor, mTextColor,
			mCircleSize, mCircleColor,
			mMinPinFont, mMaxPinFont);
		mRightThumb.setLabel(getPinValue(mRightIndex));

		invalidate();
	}

	/**
	 * Returns if either index is outside the range of the tickCount.
	 *
	 * @param leftThumbIndex  Integer specifying the left thumb index.
	 * @param rightThumbIndex Integer specifying the right thumb index.
	 * @return boolean If the index is out of range.
	 */
	private boolean indexOutOfRange(int leftThumbIndex, int rightThumbIndex) {
		return (leftThumbIndex < 0 || leftThumbIndex >= mTickCount
			|| rightThumbIndex < 0
			|| rightThumbIndex >= mTickCount);
	}

	/**
	 * Returns if either value is outside the range of the tickCount.
	 *
	 * @param leftThumbValue  Float specifying the left thumb value.
	 * @param rightThumbValue Float specifying the right thumb value.
	 * @return boolean If the index is out of range.
	 */
	private boolean valueOutOfRange(float leftThumbValue, float rightThumbValue) {
		return (leftThumbValue < mTickStart || leftThumbValue > mTickEnd
			|| rightThumbValue < mTickStart || rightThumbValue > mTickEnd);
	}

	/**
	 * If is invalid tickCount, rejects. TickCount must be greater than 1
	 *
	 * @param tickCount Integer
	 * @return boolean: whether tickCount > 1
	 */
	private boolean isValidTickCount(int tickCount) {
		return (tickCount > 1);
	}

	private boolean isInTargetZone(float x, float y) {
		return mLeftThumb != null && mLeftThumb.isInTargetZone(x, y)
			|| mRightThumb.isInTargetZone(x, y);
	}

	/**
	 * Handles a {@link android.view.MotionEvent#ACTION_DOWN} event.
	 *
	 * @param x the x-coordinate of the down action
	 * @param y the y-coordinate of the down action
	 */
	private void onActionDown(float x, float y) {
		if (mIsRangeBar) {
			if (!mRightThumb.isPressed() && mLeftThumb.isInTargetZone(x, y)) {
				pressPin(mLeftThumb);
			}
			else if (!mLeftThumb.isPressed() && mRightThumb.isInTargetZone(x, y)) {
				pressPin(mRightThumb);
			}
		}
		else {
			if (mRightThumb.isInTargetZone(x, y)) {
				pressPin(mRightThumb);
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
		if (mIsRangeBar && mLeftThumb.isPressed()) {
			movePin(mLeftThumb, point);
		}
		else if (mRightThumb.isPressed()) {
			movePin(mRightThumb, point);
		}

		// If the thumbs have switched order, fix the references.
		if (mIsRangeBar && comparePointsOnBar(mLeftThumb.getPosition(), mRightThumb.getPosition()) > 0) {
            final PinView temp = mLeftThumb;
            mLeftThumb = mRightThumb;
            mRightThumb = temp;
        }

		// Get the updated nearest tick marks for each thumb.
		int newLeftIndex = mIsRangeBar ? getNearestTickIndex(mLeftThumb) : 0;
		int newRightIndex = getNearestTickIndex(mRightThumb);

		// If either of the indices have changed, update and call the listener.
		if (newLeftIndex != mLeftIndex || newRightIndex != mRightIndex) {

			mLeftIndex = newLeftIndex;
			mRightIndex = newRightIndex;
			if (mIsRangeBar) {
				mLeftThumb.setLabel(getPinValue(mLeftIndex));
			}
			mRightThumb.setLabel(getPinValue(mRightIndex));

			if (mListener != null) {
				mListener.onRangeChangeListener(this, mLeftIndex, mRightIndex,
					getPinValue(mLeftIndex),
					getPinValue(mRightIndex));
			}
		}
	}

	/**
	 * Handles a {@link android.view.MotionEvent#ACTION_UP} event.
	 *
	 * @param x the x-coordinate of the up action
	 * @param y the y-coordinate of the up action
	 */
	private void onActionUp(float x, float y) {
		if (mIsRangeBar && mLeftThumb.isPressed()) {
			releasePin(mLeftThumb);
		}
		else if (mRightThumb.isPressed()) {
			releasePin(mRightThumb);
		}
		//TODO: is this ever called?
//		else {
//
//			float leftThumbXDistance = mIsRangeBar ? Math.abs(mLeftThumb.getX() - x) : 0;
//			float rightThumbXDistance = Math.abs(mRightThumb.getX() - x);
//
//			if (leftThumbXDistance < rightThumbXDistance) {
//				if (mIsRangeBar) {
//
//					mLeftThumb.setX(x); //TODO: this is wrong
//					//TODO this instead mLeftThumb.setPosition(something);
//					releasePin(mLeftThumb);
//				}
//			}
//			else {
//				mRightThumb.setX(x); //TODO: this is wrong
//				//TODO this instead mRightThumb.setPosition(something);
//				releasePin(mRightThumb);
//			}
//
//			// Get the updated nearest tick marks for each thumb.
//			final int newLeftIndex = mIsRangeBar ? getNearestTickIndex(mLeftThumb) : 0;
//			final int newRightIndex = getNearestTickIndex(mRightThumb);
//			// If either of the indices have changed, update and call the listener.
//			if (newLeftIndex != mLeftIndex || newRightIndex != mRightIndex) {
//
//				mLeftIndex = newLeftIndex;
//				mRightIndex = newRightIndex;
//
//				if (mListener != null) {
//					mListener.onRangeChangeListener(this, mLeftIndex, mRightIndex,
//						getPinValue(mLeftIndex),
//						getPinValue(mRightIndex));
//				}
//			}
//		}
	}

	/**
	 * Set the thumb to be in the pressed state and calls invalidate() to redraw
	 * the canvas to reflect the updated state.
	 *
	 * @param thumb the thumb to press
	 */
	private void pressPin(final PinView thumb) {
		if (mFirstSetTickCount) {
			mFirstSetTickCount = false;
		}
		if (mArePinsTemporary) {
			ValueAnimator animator = ValueAnimator.ofFloat(0, mExpandedPinRadius);
			animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					mThumbRadiusDP = (Float) (animation.getAnimatedValue());
					thumb.setSize(mThumbRadiusDP, mPinPadding * animation.getAnimatedFraction());
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

		thumb.setLabel(getPinValue(tickIndex));

		if (mArePinsTemporary) {
			ValueAnimator animator = ValueAnimator.ofFloat(mExpandedPinRadius, 0);
			animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					mThumbRadiusDP = (Float) (animation.getAnimatedValue());
					thumb.setSize(mThumbRadiusDP,
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
	 * Set the value on the thumb pin, either from map or calculated from the tick intervals
	 * Integer check to format decimals as whole numbers
	 *
	 * @param tickIndex the index to set the value for
	 */
	private String getPinValue(int tickIndex) {
		if (mPinTextListener != null) {
			return mPinTextListener.getPinValue(this, tickIndex);
		}
		float tickValue = (tickIndex == (mTickCount - 1))
			? mTickEnd
			: (tickIndex * mTickInterval) + mTickStart;
		String xValue;
		if (tickValue == Math.ceil(tickValue)) {
			xValue = String.valueOf((int) tickValue);
		}
		else {
			xValue = String.valueOf(tickValue);
		}
		return mPinTextFormatter.getText(xValue);
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
		void onRangeChangeListener(AbsRangeBar rangeBar, int leftPinIndex,
			int rightPinIndex, String leftPinValue, String rightPinValue);
	}

	public interface PinTextFormatter {
		String getText(String value);
	}

	/**
	 * @author robmunro
	 *         A callback that allows getting pin text exernally
	 */
	public interface OnRangeBarTextListener {
		String getPinValue(AbsRangeBar rangeBar, int tickIndex);
	}


}
