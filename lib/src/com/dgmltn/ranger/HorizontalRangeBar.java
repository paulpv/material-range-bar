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

	private float mTickDistance;

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
	protected void createBar() {
		super.createBar();

		mLeftX = getPaddingLeft();
		mRightX = getWidth() - getPaddingRight();
		mY = getHeight() - getPaddingBottom();
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
	public void drawBar(Canvas canvas) {
		canvas.drawLine(mLeftX, mY, mRightX, mY, mBarPaint);
	}

	@Override
	public void drawConnectingLine(Canvas canvas, PointF left, PointF right) {
		canvas.drawLine(left.x, mY, right.x, mY, mConnectingLinePaint);
	}
}
