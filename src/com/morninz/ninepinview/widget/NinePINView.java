/**
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 Mornin.Z
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.morninz.ninepinview.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.morninz.ninepinview.R;

/**
 * A nine points graphic PIN view. User use finger to draw a graphic that
 * connect some points, and the string consist of the drawn points' indexes is
 * the PIN code. And each point can be drawn only once, eg: graphic 'Z'
 * represent PIN code "0124678".
 * <p>
 * The nine points' indexes are below:
 * <p>
 * <p>
 * 0 ---- 1 ---- 2
 * </p>
 * <p>
 * 3 ---- 4 ---- 5
 * </p>
 * <p>
 * 6 ---- 7 ---- 8
 * </p>
 * 
 * @author mornin.z
 * 
 */
public class NinePINView extends View {
	private final static String TAG = "NinePINView";

	protected final int POINT_COUNT = 9;
	protected final int DEFAULT_PADDING = 20;
	protected final int DEFAULT_POINT_COLOR = 0xFFFFFFFF;
	protected final int DEFAULT_CIRCLE_COLOR = 0xFF66CC99;
	protected final int DEFAULT_LINE_COLOR = 0x77FFFFFF;
	protected final int DEFAULT_WRONG_COLOR = 0xFFFF0000;
	private int mPointColor;
	private float mPointSize;
	private Paint mPointPaint;

	private int mCircleColor;
	private float mCircleWidth;
	private float mCircleRadius;
	private Paint mCirclePaint;

	private int mLineColor;
	private float mLineWidth;
	private Paint mLinePaint;

	private int mWrongColor;
	private Path[] mWrongPaths = new Path[POINT_COUNT];
	private Paint mWrongPaint;

	/**
	 * The correct PIN.
	 */
	private String mCorrectPIN = "00";
	/**
	 * PIN is consist of points in the drawn shape.
	 */
	private String mDrawnPIN;

	/**
	 * 9 center points
	 */
	private Point[] mCenterPoints = new Point[POINT_COUNT];

	/**
	 * The all point have been drawn since last MotionEvent.ACTION_DOWN event
	 * triggered.
	 */
	private List<Point> mDrawnPoints = new ArrayList<Point>();

	/**
	 * The last been drawn point.
	 */
	private Point mLastDrawnPoint = null;

	private class Point {
		int index;// index of 9 center points
		float x;
		float y;

		@Override
		public String toString() {
			return "Point [index=" + index + ", x=" + x + ", y=" + y + "]";
		}
	}

	/**
	 * The current X coordinate of finger
	 */
	private float mCurrX;
	/**
	 * The current Y coordinate of finger
	 */
	private float mCurrY;

	protected boolean mWillDrawWrongTriangle;

	protected OnDrawListener mOnDrawListener;

	protected Mode mCurrMode = Mode.MODE_WORK;

	public static enum Mode {
		/**
		 * Study correct PIN shape mode.
		 */
		MODE_STUDY,

		/**
		 * Inspect drawn PIN shape mode.
		 */
		MODE_WORK
	}

	public NinePINView(Context context) {
		this(context, null);
	}

	public NinePINView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public NinePINView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.NinePINView);

		mPointColor = a.getColor(R.styleable.NinePINView_pointColor,
				DEFAULT_POINT_COLOR);
		mCircleColor = a.getColor(R.styleable.NinePINView_circleColor,
				DEFAULT_CIRCLE_COLOR);
		mLineColor = a.getColor(R.styleable.NinePINView_lineColor,
				DEFAULT_LINE_COLOR);
		mWrongColor = a.getColor(R.styleable.NinePINView_wrongColor,
				DEFAULT_WRONG_COLOR);

		mPointSize = a.getDimension(R.styleable.NinePINView_pointSize, 8.0f);
		mCircleWidth = a
				.getDimension(R.styleable.NinePINView_circleWidth, 5.0f);
		mLineWidth = a.getDimension(R.styleable.NinePINView_lineWidth, 5.0f);
		mCircleRadius = a.getDimension(R.styleable.NinePINView_circleRadius,
				40.0f);

		a.recycle();

		// center point
		mPointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPointPaint.setColor(mPointColor);
		mPointPaint.setStyle(Style.FILL);

		// circle
		mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mCirclePaint.setColor(mCircleColor);
		mCirclePaint.setStrokeWidth(mCircleWidth);
		mCirclePaint.setStyle(Style.STROKE);

		// connection line
		mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mLinePaint.setColor(mLineColor);
		mLinePaint.setStrokeWidth(mLineWidth);
		mLinePaint.setStyle(Style.FILL);
		mLinePaint.setStrokeCap(Cap.ROUND);
		mLinePaint.setStrokeJoin(Join.ROUND);

		// wrong triangle path
		mWrongPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mWrongPaint.setColor(mWrongColor);
		mWrongPaint.setStyle(Style.FILL);

		setPadding(Math.max(getPaddingLeft(), DEFAULT_PADDING),
				Math.max(getPaddingTop(), DEFAULT_PADDING),
				Math.max(getPaddingRight(), DEFAULT_PADDING),
				Math.max(getPaddingBottom(), DEFAULT_PADDING));

		mWillDrawWrongTriangle = false;
	}

	public int getPointColor() {
		return mPointColor;
	}

	public void setPointColor(int color) {
		mPointColor = color;
		mPointPaint.setColor(color);
	}

	public void setCircleColor(int color) {
		mCircleColor = color;
		mCirclePaint.setColor(color);
	}

	public void setLineColor(int color) {
		mLineColor = color;
		mLinePaint.setColor(color);
	}

	public void setPointSize(int size) {
		mPointSize = size;
		mPointPaint.setStrokeWidth(size);
	}

	public void setCircleWidth(int width) {
		mCircleWidth = width;
		mCirclePaint.setStrokeWidth(width);
	}

	public void setCircleRadius(int raduis) {
		mCircleRadius = raduis;
		mCirclePaint.setStrokeWidth(raduis);
	}

	public void setLineWidth(int width) {
		mLineWidth = width;
		mLinePaint.setStrokeWidth(width);
	}

	/**
	 * Set current work mode of NinePINView.
	 * 
	 * @param mode
	 *            {@link Mode#MODE_STUDY} or {@link Mode#MODE_WORK}
	 */
	public void setMode(Mode mode) {
		mCurrMode = mode;
	}

	/**
	 * Get the current work mode of NinePINView.
	 * 
	 * @return
	 */
	public Mode getMode() {
		return mCurrMode;
	}

	/**
	 * 
	 * @return PIN String has been drawn.
	 */
	public String getDrawnPIN() {
		return mDrawnPIN;
	}

	/**
	 * Set correct PIN String used to check whether the drawn shape is correct
	 * or not.
	 * <p>
	 * You must call this method before begin draw.
	 * </p>
	 * 
	 * @param pin
	 *            the correct PIN String. 0 is index of the first point.
	 *            <b>eg:</b> "012" PIN String represent that the shape connect
	 *            one, two and three points in first row.
	 * @throws IllegalArgumentException
	 *             if parameter <b>pin</b> contains characters besides '0'-'8'
	 *             or same charaters.
	 */
	public void setCorrectPIN(String pin) {
		boolean repeat = false;
		for (int i = 0; i < pin.length() - 1; i++) {
			for (int j = i + 1; j < pin.length(); j++) {
				if (pin.charAt(i) == pin.charAt(j)) {
					repeat = true;
					break;
				}
			}
		}
		boolean match = Pattern.matches("[0-8]{1,9}", pin);
		if (repeat || !match) {
			throw new IllegalArgumentException(
					"The pin must only contains characters '0'-'8' and not be repeat.");
		}
		mCorrectPIN = pin;
	}

	public void setOnDrawListener(OnDrawListener listener) {
		mOnDrawListener = listener;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		computePointsAndWrongTriangleCoordinate();
	}

	/**
	 * Compute the coordinates of 9 center points and wrong triangles.
	 */
	protected void computePointsAndWrongTriangleCoordinate() {
		int drawWidth = getWidth() - getPaddingLeft() - getPaddingRight();
		int drawHeight = getHeight() - getPaddingTop() - getPaddingBottom();

		float baseX = getPaddingLeft() + mCircleRadius;
		float baseY = getPaddingTop() + mCircleRadius;
		float gapX = drawWidth / 2.0f - mCircleRadius;
		float gapY = drawHeight / 2.0f - mCircleRadius;

		float r = mCircleRadius;

		for (int i = 0; i < POINT_COUNT; i++) {
			// compute center point's coordinate
			Point point = new Point();
			point.x = baseX + gapX * (i % 3);
			point.y = baseY + gapY * (i / 3);
			point.index = i;
			mCenterPoints[i] = point;
			// compute wrong triangle path of this point.
			Path path = new Path();
			float x1, y1, x2, y2, x3, y3;
			x1 = point.x + r;
			y1 = point.y;
			x2 = point.x + r * (2.0f / 3);
			y2 = point.y - r * (1.0f / 3);
			x3 = x2;
			y3 = point.y + r * (1.0f / 3);
			path.moveTo(x1, y1);
			path.lineTo(x2, y2);
			path.lineTo(x3, y3);
			path.lineTo(x1, y1);
			path.close();
			mWrongPaths[i] = path;
			Log.d(TAG, "[ " + x1 + ", " + y1 + " ], " + "[ " + x2 + ", " + y2
					+ " ], " + "[ " + x3 + ", " + y3 + " ]");
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		switch (MotionEventCompat.getActionMasked(e)) {
		case MotionEvent.ACTION_DOWN:
			onDrawStart();
			clearDrawn();
			kissSomePoint(e);
			invalidate();
			break;
		case MotionEvent.ACTION_MOVE:
			kissSomePoint(e);
			invalidate();
			break;
		case MotionEvent.ACTION_UP:
			if (mLastDrawnPoint != null) {
				mCurrX = mLastDrawnPoint.x;
				mCurrY = mLastDrawnPoint.y;
			}

			StringBuilder sb = new StringBuilder();
			for (Point p : mDrawnPoints) {
				sb.append(p.index);
			}
			mDrawnPIN = sb.toString();
			Log.d(TAG, "drawn pin : " + mDrawnPIN + ", correct pin : "
					+ mCorrectPIN);

			if (mCurrMode == Mode.MODE_STUDY) {
				onDrawComplete(true);
				clearDrawn();
			} else if (mCurrMode == Mode.MODE_WORK) {
				if (!mDrawnPIN.equals(mCorrectPIN)) {
					mCirclePaint.setColor(mWrongColor);
					mWillDrawWrongTriangle = true;
					onDrawComplete(false);
				} else {
					onDrawComplete(true);
				}
			}

			invalidate();
			break;
		}

		return true;
	}

	/**
	 * Clear to status before drawn, there are only 9 points in canvas.
	 */
	protected void clearDrawn() {
		mDrawnPoints.clear();
		mLastDrawnPoint = null;
		mCirclePaint.setColor(mCircleColor);
		mWillDrawWrongTriangle = false;
	}

	/**
	 * Check whether finger has entered some point's area or not.
	 * 
	 * @param e
	 *            motion envent.
	 */
	protected boolean kissSomePoint(MotionEvent e) {
		// We just check the one point.
		mCurrX = e.getX(0);
		mCurrY = e.getY(0);

		for (int i = 0; i < POINT_COUNT; i++) {
			Point p = mCenterPoints[i];
			if (Math.sqrt(Math.pow(mCurrX - p.x, 2) + Math.pow(mCurrY - p.y, 2)) <= mCircleRadius) {
				if (!mDrawnPoints.contains(p)) {
					Log.d(TAG, "kiss " + p);
					// Check the point between last drawn point and kissed
					// point, if not drawn, draw it.
					// There are two appropriate situations:
					// 1. The two points are in corner.
					// 2. The connection line of the two points through the
					// point 4.
					if (mLastDrawnPoint != null) {
						if ((isCornerPoint(mLastDrawnPoint) && isCornerPoint(p))
								|| (mLastDrawnPoint.index + p.index == 8)) {
							int middlePointIndex = (mLastDrawnPoint.index + p.index) / 2;
							Point middlePoint = mCenterPoints[middlePointIndex];
							if (!mDrawnPoints.contains(middlePoint)) {
								mDrawnPoints.add(middlePoint);
							}
						}
					}
					mLastDrawnPoint = p;
					mDrawnPoints.add(p);
					return true;
				} else {
					// This point has been kissed, don't be greedy!
					break;
				}
			}
		}

		return false;
	}

	private boolean isCornerPoint(Point p) {
		if (p.index == 0 || p.index == 2 || p.index == 6 || p.index == 8) {
			return true;
		} else {
			return false;
		}
	}

	protected void onDrawStart() {
		if (mOnDrawListener != null) {
			mOnDrawListener.onDrawStart(this);
		} else {
			Log.w(TAG,
					"You should call NinePINView.setOnDrawCompleteListener() method to set a draw listener.");
		}
	}

	protected void onDrawComplete(boolean correct) {
		// XXX: this is not required. The correct PIN should be set by user.
		if (mCurrMode == Mode.MODE_STUDY) {
			mCorrectPIN = mDrawnPIN;
		}

		if (mOnDrawListener != null) {
			mOnDrawListener.onDrawComplete(this, correct);
		} else {
			Log.w(TAG,
					"You should call NinePINView.setOnDrawCompleteListener() method to set a draw listener.");
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// draw circles around center points and connection line
		int drawnCount = mDrawnPoints.size();
		for (int j = 0; j < drawnCount; j++) {
			Point p1 = mDrawnPoints.get(j);
			canvas.drawCircle(p1.x, p1.y, mCircleRadius, mCirclePaint);
			if (j + 1 < drawnCount) {
				Point p2 = mDrawnPoints.get(j + 1);
				canvas.drawCircle(p2.x, p2.y, mCircleRadius, mCirclePaint);
				canvas.drawLine(p1.x, p1.y, p2.x, p2.y, mLinePaint);
				if (mWillDrawWrongTriangle) {
					// compute the wrong triangle's direction of this point.
					float angle = 0.f;
					if (p2.y == p1.y) {// x-axis
						angle = p2.x > p1.x ? 0.f : 180.f;
					} else if (p2.x == p1.x) { // y-axis
						angle = p2.y > p1.y ? 90.f : -90.f;
					} else {// in quadrants
						double tanA = ((double) p2.y - (double) p1.y)
								/ ((double) p2.x - (double) p1.x);
						// in 1 or 4 quadrant
						angle = (float) (Math.atan(tanA) * 180 / Math.PI);
						// in 2 or 3 quadrant
						if (p2.x < p1.x) {
							angle += 180.f;
						}
					}
					Log.d(TAG, "angle " + angle);
					canvas.save();
					canvas.rotate(angle, p1.x, p1.y);
					canvas.drawPath(mWrongPaths[p1.index], mWrongPaint);
					canvas.restore();
				}
			}
		}

		// draw extra connection line
		if (mLastDrawnPoint != null) {
			canvas.drawLine(mLastDrawnPoint.x, mLastDrawnPoint.y, mCurrX,
					mCurrY, mLinePaint);
		}

		// draw 9 center points
		for (int i = 0; i < POINT_COUNT; i++) {
			Point p = mCenterPoints[i];
			canvas.drawCircle(p.x, p.y, mPointSize, mPointPaint);
		}
	}

	/**
	 * Interface definition for a callback to be invoked when finger drawn is
	 * complete.
	 */
	public interface OnDrawListener {

		/**
		 * Called when finger begin to draw.
		 * 
		 * @param ninePINView
		 *            The ninePINView which is being drawing.
		 */
		public void onDrawStart(NinePINView ninePINView);

		/**
		 * Called when finger draw has been complete.
		 * 
		 * @param ninePINView
		 *            The ninePINView has been drawn.
		 * @param correct
		 *            true if shape that has been drawn is correct, false if
		 *            wrong.
		 */
		public void onDrawComplete(NinePINView ninePINView, boolean correct);

	}
}