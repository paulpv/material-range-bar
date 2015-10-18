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

package com.appyvet.rangebar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.TypedValue;

/**
 * This class represents the underlying gray bar in the RangeBar (without the
 * thumbs).
 */
public abstract class Bar {

    // Member Variables ////////////////////////////////////////////////////////

    final Paint mBarPaint;
    final Paint mTickPaint;
    final Paint mConnectingLinePaint;

    // Left-coordinate of the horizontal bar.
    final float mLeftX;

    final float mRightX;

    final float mY;

    int mNumSegments;

    final float mTickHeight;

    // Constructor /////////////////////////////////////////////////////////////


    /**
     * Bar constructor
     *
     * @param ctx          the context
     * @param size         the start x co-ordinate
     * @param y            the y co-ordinate
     * @param padding      the length of the bar in px
     * @param tickCount    the number of ticks on the bar
     * @param tickHeightDP the height of each tick
     * @param tickColor    the color of each tick
     * @param barWeight    the weight of the bar
     * @param barColor     the color of the bar
     */
    public Bar(Context ctx,
            Point size,
            Rect padding,
            int tickCount,
            float tickHeightDP,
            int tickColor,
            float barWeight,
            int barColor,
            float connectingWeight,
            int connectingColor
        ) {

        mLeftX = padding.left;
        mRightX = size.x - padding.right;
        mY = size.y - padding.bottom;

        mNumSegments = tickCount - 1;
        mTickHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                tickHeightDP, ctx.getResources().getDisplayMetrics());

        // Initialize the paint.
        mBarPaint = new Paint();
        mBarPaint.setColor(barColor);
        mBarPaint.setStrokeWidth(barWeight);
        mBarPaint.setAntiAlias(true);
        mBarPaint.setStyle(Paint.Style.STROKE);

        mTickPaint = new Paint();
        mTickPaint.setColor(tickColor);
        mTickPaint.setStrokeWidth(barWeight);
        mTickPaint.setAntiAlias(true);

        // Initialize the paint, set values
        mConnectingLinePaint = new Paint();
        mConnectingLinePaint.setColor(connectingColor);
        mConnectingLinePaint.setStrokeWidth(connectingWeight);
        mConnectingLinePaint.setStrokeCap(Paint.Cap.ROUND);
        mConnectingLinePaint.setAntiAlias(true);

    }

    /**
     * Get the x-coordinate of the left edge of the bar.
     *
     * @return x-coordinate of the left edge of the bar
     */
    public float getLeftX() {
        return mLeftX;
    }

    /**
     * Get the x-coordinate of the right edge of the bar.
     *
     * @return x-coordinate of the right edge of the bar
     */
    public float getRightX() {
        return mRightX;
    }

    /**
     * Draw the connecting line between the two thumbs in rangebar.
     *
     * @param canvas     the Canvas to draw to
     * @param leftThumb  the left thumb
     * @param rightThumb the right thumb
     */
    public void drawConnectingLine(Canvas canvas, PinView leftThumb, PinView rightThumb) {
        canvas.drawLine(leftThumb.getX(), mY, rightThumb.getX(), mY, mConnectingLinePaint);
    }

    /**
     * Draw the connecting line between for single slider.
     *
     * @param canvas     the Canvas to draw to
     * @param rightThumb the right thumb
     * @param leftMargin the left margin
     */
    public void drawConnectingLine(Canvas canvas, float leftMargin, PinView rightThumb) {
        canvas.drawLine(leftMargin, mY, rightThumb.getX(), mY, mConnectingLinePaint);
    }

    // Abstract Methods /////////////////////////////////////////////////

    /**
     * Draws the bar on the given Canvas.
     *
     * @param canvas Canvas to draw on; should be the Canvas passed into {#link
     *               View#onDraw()}
     */
    public abstract void draw(Canvas canvas);

    /**
     * Gets the x-coordinate of the nearest tick to the given x-coordinate.
     *
     * @param thumb the thumb to find the nearest tick for
     * @return the x-coordinate of the nearest tick
     */
    public abstract float getNearestTickCoordinate(PinView thumb);

    /**
     * Gets the zero-based index of the nearest tick to the given thumb.
     *
     * @param thumb the Thumb to find the nearest tick for
     * @return the zero-based index of the nearest tick
     */
    public abstract int getNearestTickIndex(PinView thumb);

    /**
     * Draws the tick marks on the bar.
     *
     * @param canvas Canvas to draw on; should be the Canvas passed into {#link
     *               View#onDraw()}
     */
    public abstract void drawTicks(Canvas canvas);
}
