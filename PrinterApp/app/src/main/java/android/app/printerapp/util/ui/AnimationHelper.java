package android.app.printerapp.util.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;

/**
 * Created by alberto-baeza on 11/20/14.
 */
public class AnimationHelper {

    private AnimatorSet mCaptureAnimator;
    public static final int TRANSLATE_ANIMATION_DURATION = 400;
    public static final int ALPHA_DURATION = 700;


    /**
     * To animate view slide out from right to left
     *
     * @param view
     */
    public static void slideToLeft(View view) {
        TranslateAnimation animate = new TranslateAnimation(40, 0, 0, 0);
        animate.setDuration(500);
        animate.setFillAfter(false);
        view.startAnimation(animate);
    }

    /**
     * Animate fragments
     *
     * @param view
     */
    public static void inFromRightAnimation(View view) {
        Animation inFromRight = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, +1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        inFromRight.setDuration(240);
        inFromRight.setInterpolator(new AccelerateInterpolator());
        view.startAnimation(inFromRight);
    }

    /**
     * Translate a view in the x axis
     * @param view View to be translated
     * @param from Initial position
     * @param to   FInal position
     */
    public void translateXAnimation(final View view, final float from, final float to) {

        if (mCaptureAnimator != null && mCaptureAnimator.isStarted()) {
            mCaptureAnimator.cancel();
        }

        mCaptureAnimator = new AnimatorSet();
        mCaptureAnimator.playTogether(
                ObjectAnimator.ofFloat(view, "x", from, to)
                        .setDuration(TRANSLATE_ANIMATION_DURATION));
        mCaptureAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                view.setClickable(false);
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                view.setClickable(true);
                if (mCaptureAnimator != null) {
                    mCaptureAnimator.removeAllListeners();
                }
                mCaptureAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                view.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
                // Do nothing.
            }
        });
        mCaptureAnimator.start();
    }


}
