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
	public void getNearestPointOnBar(PointF out, PointF point) {
		out.set(Math.min(mRightX, Math.max(mLeftX, point.x)), mY);
	}

	@Override
	public void getPointOfTick(PointF out, int index) {
		if (index == getTickCount() - 1) {
			// Avoid any rounding discrepancies
			out.set(mRightX, mY);
		}
		else {
			out.set(index * getTickDistance() + mLeftX, mY);
		}
	}

	@Override
	public int getNearestTickIndex(PointF point) {
		float d = getTickDistance();
		return (int) ((point.x - mLeftX + d / 2f) / d);
	}

	@Override
	public void drawBar(Canvas canvas) {
		canvas.drawLine(mLeftX, mY, mRightX, mY, mBarPaint);
	}

	@Override
	public void drawConnectingLine(Canvas canvas, PointF left, PointF right) {
		if (mIsInverted) {
			canvas.drawLine(mLeftX, mY, left.x, mY, mConnectingLinePaint);
			canvas.drawLine(right.x, mY, mRightX, mY, mConnectingLinePaint);
		}
		else {
			canvas.drawLine(left.x, mY, right.x, mY, mConnectingLinePaint);
		}
	}

	private float getTickDistance() {
		return (mRightX - mLeftX) / (getTickCount() - 1);
	}
}
