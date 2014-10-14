package com.material.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by IntelliJ IDEA.
 * User: keith.
 * Date: 14-10-10.
 * Time: 14:46.
 */
public class LinearProgress extends View {

    public LinearProgress(Context context) {
        this(context, null);
    }

    public LinearProgress(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LinearProgress(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
