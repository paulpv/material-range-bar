/*
 * Copyright 2014, Appyvet, Inc.
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

package com.dgmltn.ranger;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.dgmltn.ranger.internal.AbsRangeBar;

/**
 * Represents a thumb in the RangeBar slider. This is the handle for the slider
 * that is pressed and slid.
 */
public class PinView extends View {
    //private static final String TAG = PbLog.TAG("PinView");

    // Private Constants ///////////////////////////////////////////////////////

    // The radius (in dp) of the touchable area around the thumb. We are basing
    // this value off of the recommended 48dp Rhythm. See:
    // http://developer.android.com/design/style/metrics-grids.html#48dp-rhythm
    private static final float MINIMUM_TARGET_RADIUS_DP = 24;

    // Sets the default values for radius, normal, pressed if circle is to be
    // drawn but no value is given.
    private static final float DEFAULT_THUMB_RADIUS_DP = 14;

    // Member Variables ////////////////////////////////////////////////////////

    private final String mName;

    // Radius (in pixels) of the touch area of the thumb.
    private float mTargetRadiusPx;

    // Indicates whether this thumb is currently pressed and active.
    private boolean mIsPressed;

    // The current position of the thumb in the parent view.
    private PointF mPosition;

    // mPaint to draw the thumbs if attributes are selected

    private Paint mTextPaint;

    private Drawable mPin;

    private String mLabel;

    // Radius of the new thumb if selected
    private int mPinRadiusPx;

    private ColorFilter mPinFilter;

    private float mPinPadding;

    private float mTextYPadding;

    private Rect mBounds = new Rect();

    private float mDensity;

    private Paint mCirclePaint;

    private float mCircleRadiusPx;

    private float mMinPinFont = AbsRangeBar.DEFAULT_MIN_PIN_FONT_SP;

    private float mMaxPinFont = AbsRangeBar.DEFAULT_MAX_PIN_FONT_SP;

    private int mIndex;

    // Constructors ////////////////////////////////////////////////////////////

    public PinView(Context context, String name) {
        super(context);
        mName = name;
    }

    @Override
    public String toString() {
        return mName + //'@' + Integer.toHexString(hashCode()) +
                '{' +
                "mIndex=" + mIndex +
                ", mIsPressed=" + mIsPressed +
                '}';
    }

    // Initialization //////////////////////////////////////////////////////////

    /**
     * The view is created empty with a default constructor. Use init to set all the initial
     * variables for the pin
     *
     * @param position     The position of this point in its parent's view
     * @param pinRadiusDP  the initial size of the pin
     * @param pinColor     the color of the pin
     * @param textColor    the color of the value text in the pin
     * @param circleRadius the radius of the selector circle
     * @param circleColor  the color of the selector circle
     */
    public void init(PointF position, float pinRadiusDP, int pinColor, int textColor,
                     float circleRadius, int circleColor, float minFont, float maxFont) {

        mPin = ContextCompat.getDrawable(getContext(), R.drawable.rotate);

        mPosition = position;

        mDensity = getResources().getDisplayMetrics().density;
        mMinPinFont = minFont / mDensity;
        mMaxPinFont = maxFont / mDensity;

        mPinPadding = (int) (15f * mDensity);
        mCircleRadiusPx = circleRadius;
        mTextYPadding = (int) (3.5f * mDensity);
        mPinRadiusPx = (int) ((pinRadiusDP == -1 ? DEFAULT_THUMB_RADIUS_DP : pinRadiusDP) * mDensity);

        // Creates the paint and sets the Paint values
        mTextPaint = new Paint();
        mTextPaint.setColor(textColor);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(15f * mDensity);

        // Creates the paint and sets the Paint values
        mCirclePaint = new Paint();
        mCirclePaint.setColor(circleColor);
        mCirclePaint.setAntiAlias(true);

        // Color filter for the selection pin
        mPinFilter = new LightingColorFilter(pinColor, pinColor);

        // Sets the minimum touchable area, but allows it to expand based on image size
        mTargetRadiusPx = Math.max(MINIMUM_TARGET_RADIUS_DP * mDensity, mPinRadiusPx);
    }

    public int getIndex() {
        return mIndex;
    }

    public boolean setIndex(int index) {
        //PbLog.e(TAG, this + " setIndex(" + index + ')');
        if (index != mIndex) {
            mIndex = index;
            return true;
        }
        return false;
    }

    /**
     * Set the x-y position of the pin
     */
    public void setPosition(PointF position) {
        //PbLog.e(TAG, this + " setPosition(" + position + ')');
        mPosition.set(position);
    }

    /**
     * Get the x-y location of the pin
     *
     * @return PointF location of the pin
     */
    public PointF getPosition() {
        return mPosition;
    }

    /**
     * Set the label of the pin
     *
     * @param label String label of the pin
     */
    public void setLabel(String label) {
        mLabel = label;
    }

    /**
     * Determine if the pin is pressed
     *
     * @return true if is in pressed state, otherwise false
     */
    @Override
    public boolean isPressed() {
        return mIsPressed;
    }

    /**
     * Sets the state of the pin to pressed
     */
    public void press() {
        mIsPressed = true;
    }

    /**
     * Release the pin, sets pressed state to false
     */
    public void release() {
        mIsPressed = false;
    }

    /**
     * Determines if the input coordinate is close enough to this thumb to
     * consider it a press.
     *
     * @param x the x-coordinate of the user touch
     * @param y the y-coordinate of the user touch
     * @return true if the coordinates are within this thumb's target area;
     * false otherwise
     */
    public boolean isInTargetZone(float x, float y) {
        //PbLog.e(TAG, this + " isInTargetZone(x=" + x + ", y=" + y + ')');
        //PbLog.e(TAG, this + " isInTargetZone: mPosition=" + mPosition);
        //PbLog.e(TAG, this + " isInTargetZone: mPinPadding=" + mPinPadding);
        //PbLog.e(TAG, this + " isInTargetZone: mTargetRadiusPx=" + mTargetRadiusPx);
        return (Math.abs(x - mPosition.x) <= mTargetRadiusPx &&
                Math.abs(y - mPosition.y + mPinPadding) <= mTargetRadiusPx);
    }

    /**
     * Set size of the pin and padding for use when animating pin enlargement on press
     *
     * @param size    the size of the pin radius
     * @param padding the size of the padding
     */
    public void setSize(float size, float padding) {
        mPinPadding = (int) padding;
        mPinRadiusPx = (int) size;
        invalidate();
    }

    //Draw the circle regardless of pressed state. If pin size is >0 then also draw the pin and text
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        canvas.drawCircle(mPosition.x, mPosition.y, mCircleRadiusPx, mCirclePaint);

        // Draw pin if pressed
        if (mPinRadiusPx > 0) {
            mBounds.set((int) mPosition.x - mPinRadiusPx,
                    (int) mPosition.y - (mPinRadiusPx * 2) - (int) mPinPadding,
                    (int) mPosition.x + mPinRadiusPx,
                    (int) mPosition.y - (int) mPinPadding);
            mPin.setBounds(mBounds);
            String text = mLabel;

            calibrateTextSize(mTextPaint, text, mBounds.width());
            mTextPaint.getTextBounds(text, 0, text.length(), mBounds);
            mTextPaint.setTextAlign(Paint.Align.CENTER);
            mPin.setColorFilter(mPinFilter);
            mPin.draw(canvas);
            canvas.drawText(text,
                    mPosition.x, mPosition.y - mPinRadiusPx - mPinPadding + mTextYPadding,
                    mTextPaint);
        }
    }

    // Private Methods /////////////////////////////////////////////////////////////////

    //Set text size based on available pin width.
    private void calibrateTextSize(Paint paint, String text, float boxWidth) {
        paint.setTextSize(10);

        float textSize = paint.measureText(text);
        float estimatedFontSize = boxWidth * 8 / textSize / mDensity;

        if (estimatedFontSize < mMinPinFont) {
            estimatedFontSize = mMinPinFont;
        } else if (estimatedFontSize > mMaxPinFont) {
            estimatedFontSize = mMaxPinFont;
        }
        paint.setTextSize(estimatedFontSize * mDensity);
    }
}
