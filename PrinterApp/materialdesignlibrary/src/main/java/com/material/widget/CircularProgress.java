package com.material.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by IntelliJ IDEA.
 * User: keith.
 * Date: 14-10-10.
 * Time: 14:46.
 */
public class CircularProgress extends View {

    private static final int PROGRESS_DURATION = 300;
    private static final int SMALL_SIZE = 0;
    private static final int NORMAL_SIZE = 1;
    private static final int LARGE_SIZE = 2;

    private int mColor;
    private int mSize;
    private boolean mIndeterminate;
    private int mBorderWidth;
    private RectF arcRectF;
    private int mDuration;
    private int mMax;
    private int mProgress;

    private Paint arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public CircularProgress(Context context) {
        this(context, null);
    }

    public CircularProgress(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircularProgress(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.CircularProgress);
        mColor = attributes.getColor(R.styleable.CircularProgress_circular_progress_color,
                getResources().getColor(R.color.circular_progress_color));
        mSize = attributes.getInt(R.styleable.CircularProgress_circular_progress_size, NORMAL_SIZE);
        mIndeterminate = attributes.getBoolean(R.styleable.CircularProgress_circular_progress_indeterminate,
                getResources().getBoolean(R.bool.circular_progress_indeterminate));
        mBorderWidth = attributes.getDimensionPixelSize(R.styleable.CircularProgress_circular_progress_border_width,
                getResources().getDimensionPixelSize(R.dimen.circular_progress_border_width));
        mDuration = attributes.getInteger(R.styleable.CircularProgress_circular_progress_duration, PROGRESS_DURATION);
        mMax = attributes.getInteger(R.styleable.CircularProgress_circular_progress_max,
                getResources().getInteger(R.integer.circular_progress_max));
        attributes.recycle();
        arcPaint.setColor(mColor);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(mBorderWidth);
    }

    public synchronized int getProgress() {
        return mIndeterminate ? 0 : mProgress;
    }

    public synchronized int getMax() {
        return mMax;
    }

    public synchronized void setMax(int max) {
        if (max < 0) {
            max = 0;
        }
        if (max != mMax) {
            mMax = max;
            postInvalidate();

            if (mProgress > max) {
                mProgress = max;
            }
        }
    }
    
    public synchronized final void incrementProgressBy(int diff) {
        setProgress(mProgress + diff);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
    }

    private RectF getArcRectF() {
        if (arcRectF == null) {
            int size = Math.min(getWidth() - mBorderWidth * 2, getHeight() - mBorderWidth * 2);
            arcRectF = new RectF();
            arcRectF.left = (getWidth() - size) / 2;
            arcRectF.top = (getHeight() - size) / 2;
            arcRectF.right = getWidth() - (getWidth() - size) / 2;
            arcRectF.bottom = getHeight() - (getHeight() - size) / 2;
        }
        return arcRectF;
    }

    public void setColor(int color) {
        mColor = color;
        arcPaint.setColor(mColor);
        invalidate();
    }

    public void setProgress(int progress) {
        if (progress > mMax || progress < 0) {
            return;
        }
        mProgress = progress;
        invalidate();
    }

    public void setIndeterminate(boolean indeterminate) {
        mIndeterminate = indeterminate;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            int size = 0;
            switch (mSize) {
                case SMALL_SIZE:
                    size = getResources().getDimensionPixelSize(R.dimen.circular_progress_small_size);
                    break;
                case NORMAL_SIZE:
                    size = getResources().getDimensionPixelSize(R.dimen.circular_progress_normal_size);
                    break;
                case LARGE_SIZE:
                    size = getResources().getDimensionPixelSize(R.dimen.circular_progress_large_size);
                    break;
            }
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawArc(getArcRectF(), 0, 270, false, arcPaint);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        return super.onSaveInstanceState();
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }
}
