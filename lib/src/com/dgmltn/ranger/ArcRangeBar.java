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

package com.dgmltn.ranger;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.dgmltn.ranger.internal.AbsRangeBar;
import com.dgmltn.ranger.internal.ArcUtils;

/**
 * This class represents the underlying gray bar in the RangeBar (without the
 * thumbs).
 */
public class ArcRangeBar extends AbsRangeBar {

	private static final float ARC_START = 150f;
	private static final float ARC_SWEEP = 240f;

	private PointF mCenter = new PointF();
	private float mRadius = 1f;
	private RectF mBounds = new RectF();

	// Constructor /////////////////////////////////////////////////////////////

	public ArcRangeBar(Context context) {
		super(context);
	}

	public ArcRangeBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ArcRangeBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	// Implementation /////////////////////////////////////////////////////////////

	@Override
	protected void resizeBar(int w, int h) {
		super.resizeBar(w, h);

		float pw = w - getPaddingLeft() - getPaddingRight();
		float ph = h - getPaddingTop() - getPaddingBottom();

		calculateBounds();

		float scale = Math.min(pw / mBounds.width(), ph / mBounds.height());
		mRadius *= scale;

		calculateBounds();

		float dx = -mBounds.left + pw / 2 - mBounds.width() / 2 + getPaddingLeft();
		float dy = -mBounds.top + ph / 2 - mBounds.height() / 2 + getPaddingTop();
		mCenter.offset(dx, dy);
		mBounds.offset(dx, dy);
	}

	private void calculateBounds() {
		PointF test = new PointF(mCenter.x - mRadius, mCenter.y);
		getNearestPointOnBar(test, test);
		mBounds.set(test.x, test.y, test.x, test.y);
		test.set(mCenter.x + mRadius, mCenter.y);
		getNearestPointOnBar(test, test);
		mBounds.union(test.x, test.y);
		test.set(mCenter.x, mCenter.y - mRadius);
		getNearestPointOnBar(test, test);
		mBounds.union(test.x, test.y);
		test.set(mCenter.x, mCenter.y + mRadius);
		getNearestPointOnBar(test, test);
		mBounds.union(test.x, test.y);
	}

	@Override
	public int comparePointsOnBar(PointF point1, PointF point2) {
		float angle1 = getNormalizedAngle(point1);
		float angle2 = getNormalizedAngle(point2);
		return Float.compare(angle1, angle2);
	}

	@Override
	public void getNearestPointOnBar(PointF out, PointF point) {
		float normalized = getNormalizedAngle(point);

		out.set(ArcUtils.pointFromAngleDegrees(mCenter, mRadius, normalized + ARC_START));
	}

	@Override
	public int getNearestTickIndex(PointF point) {
		float normalized = getNormalizedAngle(point);

		float mTickDegrees = ARC_SWEEP / (getTickCount() - 1f);

		return (int) (normalized / mTickDegrees + 0.5f);
	}

	@Override
	public void getPointOfTick(PointF out, int index) {
		float mTickDegrees = ARC_SWEEP / (getTickCount() - 1f);
		out.set(ArcUtils.pointFromAngleDegrees(mCenter, mRadius, ARC_START + mTickDegrees * index));
	}

	@Override
	public void drawBar(Canvas canvas) {
		ArcUtils.drawArc(canvas, mCenter, mRadius, ARC_START, ARC_SWEEP, mBarPaint);
	}

	@Override
	public void drawConnectingLine(Canvas canvas, PointF point1, PointF point2) {
		float angle1 = getAngle(point1);
		float angle2 = getAngle(point2) + 720f;
		float sweep = (angle2 - angle1) % 360f;
		ArcUtils.drawArc(canvas, mCenter, mRadius, angle1, sweep, mConnectingLinePaint);
	}

	// Private members /////////////////////////////////////////////////////////////

	/**
	 * Returns the angle between 0 and the point.
	 *
	 * @param target
	 * @return
	 */
	private float getAngle(PointF target) {
		return (float) Math.toDegrees(Math.atan2(target.y - mCenter.y, target.x - mCenter.x));
	}

	/**
	 * Returns the angle between ARC_START and the point.
	 *
	 * @param target
	 * @return
	 */
	private float getNormalizedAngle(PointF target) {
		float normalized = (getAngle(target) - ARC_START + 720f) % 360f;

		if (normalized >= ARC_SWEEP / 2f + 180f) {
			normalized = 0f;
		}
		else if (normalized > ARC_SWEEP) {
			normalized = ARC_SWEEP;
		}

		return normalized;
	}
}
