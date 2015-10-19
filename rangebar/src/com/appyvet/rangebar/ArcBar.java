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

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;

/**
 * This class represents the underlying gray bar in the RangeBar (without the
 * thumbs).
 */
public class ArcBar extends Bar {

	private static final float ARC_START = 120f;
	private static final float ARC_SWEEP = 300f;

	// Member Variables ////////////////////////////////////////////////////////

	private PointF mCenter;
	private float mRadius;

	// Constructor /////////////////////////////////////////////////////////////

	/**
	 * Bar constructor
	 *
	 * @param size    The measured size of this view
	 * @param padding The 4 padding values of this view
	 */
	public ArcBar(Point size, Rect padding) {
		super();
		float w = size.x - padding.left - padding.right;
		float h = size.y - padding.top - padding.bottom;
		mCenter = new PointF(w / 2f + padding.left, h / 2f + padding.top);
		mRadius = Math.min(w, h) / 2f - 16f;
	}

	@Override
	public void getNearestPointOnBar(PointF out, PointF point) {
		float normalized = getNormalizedAngle(point);

		if (normalized >= ARC_SWEEP / 2f + 180f) {
			normalized = 0f;
		}
		else if (normalized > ARC_SWEEP) {
			normalized = ARC_SWEEP;
		}

		out.set(ArcUtils.pointFromAngleDegrees(mCenter, mRadius, normalized + ARC_START));
	}

	@Override
	public int getNearestTickIndex(PointF point) {
		float normalized = getNormalizedAngle(point);

		float mTickDegrees = ARC_SWEEP / (mTickCount - 1f);

		return (int) (normalized / mTickDegrees + 0.5f);
	}

	@Override
	public void getPointOfTick(PointF out, int index) {
		float mTickDegrees = ARC_SWEEP / (mTickCount - 1f);
		out.set(ArcUtils.pointFromAngleDegrees(mCenter, mRadius, ARC_START + mTickDegrees * index));
	}

	@Override
	public void draw(Canvas canvas) {
		ArcUtils.drawArc(canvas, mCenter, mRadius, ARC_START, ARC_SWEEP, mBarPaint);
	}

	@Override
	public void drawConnectingLine(Canvas canvas, PointF point1, PointF point2) {
		float angle1 = getAngle(point1);
		float angle2 = getAngle(point2) + 720f;
		float sweep = (angle2 - angle1) % 360f;
		ArcUtils.drawArc(canvas, mCenter, mRadius, angle1, sweep, mConnectingLinePaint);
	}

	/**
	 * Returns the angle between 0 and the point.
	 * @param target
	 * @return
	 */
	private float getAngle(PointF target) {
		return (float) Math.toDegrees(Math.atan2(target.y - mCenter.y, target.x - mCenter.x));
	}

	/**
	 * Returns the angle between ARC_START and the point.
	 * @param target
	 * @return
	 */
	private float getNormalizedAngle(PointF target) {
		return (getAngle(target) - ARC_START + 720f) % 360f;
	}
}
