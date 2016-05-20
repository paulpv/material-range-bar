package com.dgmltn.ranger;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.util.AttributeSet;

import com.dgmltn.ranger.internal.AbsRangeBar;

/**
 * Created by doug on 10/21/15.
 */
public class HorizontalRangeBar extends AbsRangeBar {

    //private static final String TAG = PbLog.TAG(HorizontalRangeBar.class);

    // Endpoints of the horizontal bar
    private float mLeftX;
    private float mRightX;

    // Y position of the horizontal bar
    private float mY;

    // Constructor /////////////////////////////////////////////////////////////

    public HorizontalRangeBar(Context context) {
        super(context);
    }

    public HorizontalRangeBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HorizontalRangeBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    // Implementation /////////////////////////////////////////////////////////////

    @Override
    protected void resizeBar(int w, int h) {
        super.resizeBar(w, h);

        mLeftX = getPaddingLeft();
        mRightX = getWidth() - getPaddingRight();
        mY = getHeight() - getPaddingBottom();
    }

    @Override
    public int comparePointsOnBar(PointF point1, PointF point2) {
        return Float.compare(point1.x, point2.x);
    }

    @Override
    public void getNearestPointOnBar(PointF pointIn, PointF pointOut) {
        //PbLog.e(TAG, "getNearestPointOnBar");
        pointOut.set(Math.min(mRightX, Math.max(mLeftX, pointIn.x)), mY);
    }

    @Override
    public int getNearestIndex(PointF point) {
        //PbLog.w(TAG, "getNearestIndex(point=" + point + ')');
        float tickDistance = getTickDistance();
        return (int) ((point.x - mLeftX + tickDistance / 2f) / tickDistance);
    }

    @Override
    public void getPointOfIndex(int index, PointF pointOut) {
        if (index == getTickCount() - 1) {
            // Avoid any rounding discrepancies
            pointOut.set(mRightX, mY);
        } else {
            pointOut.set(mLeftX + (getTickDistance() * index), mY);
        }
    }

    @Override
    public void drawBar(Canvas canvas) {
        canvas.drawLine(mLeftX, mY, mRightX, mY, mBarPaint);
    }

    @Override
    public void drawConnectingLine(Canvas canvas, PointF left, PointF right) {
        if (mConnectingLineInverted) {
            canvas.drawLine(mLeftX, mY, left.x, mY, mFirstConnectingLinePaint);
            canvas.drawLine(right.x, mY, mRightX, mY, mSecondConnectingLinePaint);
        } else {
            canvas.drawLine(left.x, mY, right.x, mY, mFirstConnectingLinePaint);
        }
    }

    // Private members /////////////////////////////////////////////////////////////

    private float getTickDistance() {
        return (mRightX - mLeftX) / (getTickCount() - 1f);
    }
}
