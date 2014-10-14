package android.app.printerapp.util.ui;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;

/**
 * Expand or collapse a specific view
 *
 * @author sara
 */
public class ExpandCollapseAnimation {

    /**
     * Expand a view to its maximum height.
     *
     * @param view View to be expanded
     */
    public static void expand(final View view) {
        view.measure(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        final int targetHeight = view.getMeasuredHeight();

        view.getLayoutParams().height = 0;

        if (view.getVisibility() == View.INVISIBLE) view.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
//                view.getLayoutParams().height = interpolatedTime == 1
//                        ? FrameLayout.LayoutParams.WRAP_CONTENT
//                        : (int) (targetHeight * interpolatedTime);
                view.getLayoutParams().height = FrameLayout.LayoutParams.WRAP_CONTENT;
                view.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        //Duration of the animation: 1dp/ms
        a.setDuration((int)(targetHeight / view.getContext().getResources().getDisplayMetrics().density));
        view.startAnimation(a);
    }

    /**
     * Collapse a view to its minimum height.
     *
     * @param view      View to be collapsed
     * @param newHeight Height to be applied to the view. If this value is 0, it is applied an
     *                  automatic height depending on the interpolatedTime and the initial
     *                  height
     */
    public static void collapse(final View view, final int newHeight) {
        final int initialHeight = view.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (newHeight != 0) {
                    view.getLayoutParams().height = newHeight;
                    view.requestLayout();
                } else {
                    if (interpolatedTime == 1) {
                        view.setVisibility(View.GONE);
                    } else {
                        view.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                        view.requestLayout();
                    }
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        //Duration of the animation: 1dp/ms
        a.setDuration((int) (initialHeight / view.getContext().getResources().getDisplayMetrics().density));
        view.startAnimation(a);
    }
}
