package android.app.printerapp.devices;

import android.app.printerapp.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Custom drag shadow builder used when a quick print object is selected in the
 * slide up panel.
 */
public class QuickPrintObjectDragShadowBuilder extends View.DragShadowBuilder {
    private Drawable mShadow;

    private Context mContext;

    public QuickPrintObjectDragShadowBuilder(Context context, View v) {
        super(v);
        mContext = context;
    }

    @Override
    public void onDrawShadow(Canvas canvas) {

        int width = getView().getWidth();
        int height = getView().getHeight();
        Paint paint = new Paint();
        paint.setColor(mContext.getResources().getColor(R.color.theme_primary));
        canvas.drawRect(new Rect(0, 0, width, height), paint);
        super.onDrawShadow(canvas);

    }

    @Override
    public void onProvideShadowMetrics(Point shadowSize, Point touchPoint) {
        int width = getView().getWidth();
        int height = getView().getHeight();

        shadowSize.set(width, height);
        touchPoint.set(width / 2, height);
    }
}