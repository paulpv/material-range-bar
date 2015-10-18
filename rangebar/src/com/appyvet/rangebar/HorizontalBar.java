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
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.TypedValue;

/**
 * This class represents the underlying gray bar in the RangeBar (without the
 * thumbs).
 */
public class HorizontalBar extends Bar {

    // Member Variables ////////////////////////////////////////////////////////

    float mTickDistance;

    // Endpoints of the horizontal bar
    final float mLeftX;
    final float mRightX;

    // Y position of the horizontal bar
    final float mY;

    // Constructor /////////////////////////////////////////////////////////////


    /**
     * Bar constructor
     *
     * @param ctx          the context
     * @param size         The measured size of this view
     * @param padding      The 4 padding values of this view
     * @param tickCount    the number of ticks on the bar
     * @param tickHeightDP the height of each tick
     * @param tickColor    the color of each tick
     * @param barWeight    the weight of the bar
     * @param barColor     the color of the bar
     */
    public HorizontalBar(Context ctx, Point size, Rect padding,
        int tickCount, float tickHeightDP, int tickColor,
        float barWeight, int barColor,
        float connectingWeight, int connectingColor) {

        super(ctx, size, padding,
            tickCount, tickHeightDP, tickColor,
            barWeight, barColor,
            connectingWeight, connectingColor);

        mTickDistance = (size.x - padding.left - padding.right) / (tickCount - 1);

        mLeftX = padding.left;
        mRightX = size.x - padding.right;
        mY = size.y - padding.bottom;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawLine(mLeftX, mY, mRightX, mY, mBarPaint);
    }

    @Override
    public void getNearestPointOnBar(PointF out, PointF point) {
        out.set(Math.min(mRightX, Math.max(mLeftX, point.x)), mY);
    }

    @Override
    public void getNearestTickPosition(PointF out, PointF point) {
        final int nearestTickIndex = getNearestTickIndex(point);
        out.set(mLeftX + (nearestTickIndex * mTickDistance), mY);
    }

    @Override
    public void getPointOfTick(PointF out, int index) {
        if (index == mTickCount - 1) {
            // Avoid any rounding discrepancies
            out.set(mRightX, mY);
        }
        else {
            out.set(index * mTickDistance + mLeftX, mY);
        }
    }

    @Override
    public int getNearestTickIndex(PointF point) {
        return (int) ((point.x - mLeftX + mTickDistance / 2f) / mTickDistance);
    }

    @Override
    public void drawConnectingLine(Canvas canvas, PinView leftThumb, PinView rightThumb) {
        PointF left = leftThumb.getPosition();
        PointF right = rightThumb.getPosition();
        canvas.drawLine(left.x, left.y, right.x, right.y, mConnectingLinePaint);
    }

    @Override
    public void drawConnectingLine(Canvas canvas, float leftMargin, PinView rightThumb) {
        PointF right = rightThumb.getPosition();
        canvas.drawLine(leftMargin, right.y, right.x, right.y, mConnectingLinePaint);
    }

}
