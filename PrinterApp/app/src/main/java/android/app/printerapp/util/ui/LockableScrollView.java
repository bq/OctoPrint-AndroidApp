package android.app.printerapp.util.ui;

import android.app.printerapp.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Custom scroll view that allows you to enable or disable the scroll
 */
public class LockableScrollView extends uk.co.androidalliance.edgeeffectoverride.ScrollView {

    private int mScrollColor;

    public LockableScrollView(Context context) {
        super(context);
    }

    public LockableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAttrs(attrs);
    }

    public LockableScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setAttrs(attrs);
    }

    private void setAttrs(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.LockableScrollView, 0, 0);
            setScrollColor (a.getColor(R.styleable.LockableScrollView_scroll_effect_color, Color.WHITE));
            a.recycle();
        }
    }

    private void setScrollColor (int scrollColor) {
        mScrollColor = scrollColor;
        super.setEdgeEffectColor(mScrollColor);
    }

    //True if we can scroll (not locked)
    //False if we cannot scroll (locked)
    private boolean mScrollable = true;

    public void setScrollingEnabled(boolean enabled) {
        mScrollable = enabled;
    }

    public boolean isScrollable() {
        return mScrollable;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //If we can scroll pass the event to the superclass
                if (mScrollable) return super.onTouchEvent(ev);
                //Only continue to handle the touch event if scrolling enabled
                return mScrollable; // mScrollable is always false at this point
            default:
                return super.onTouchEvent(ev);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Don't do anything with intercepted touch events if
        // we are not scrollable
        if (!mScrollable) return false;
        else return super.onInterceptTouchEvent(ev);
    }
}
