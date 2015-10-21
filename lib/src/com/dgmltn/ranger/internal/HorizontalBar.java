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

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;

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
     * @param size         The measured size of this view
     * @param padding      The 4 padding values of this view
     */
    public HorizontalBar(Point size, Rect padding) {
        super();

        mLeftX = padding.left;
        mRightX = size.x - padding.right;
        mY = size.y - padding.bottom;
    }

    @Override
    public void configureTicks(int count, int color, float size) {
        super.configureTicks(count, color, size);
        mTickDistance = (mRightX - mLeftX) / (mTickCount - 1);
    }

    @Override
    public int comparePointsOnBar(PointF point1, PointF point2) {
        return Float.compare(point1.x, point2.x);
    }

    @Override
    public void getNearestPointOnBar(PointF out, PointF point) {
        out.set(Math.min(mRightX, Math.max(mLeftX, point.x)), mY);
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
    public void draw(Canvas canvas) {
        canvas.drawLine(mLeftX, mY, mRightX, mY, mBarPaint);
    }

    @Override
    public void drawConnectingLine(Canvas canvas, PointF left, PointF right) {
        canvas.drawLine(left.x, mY, right.x, mY, mConnectingLinePaint);
    }

}
