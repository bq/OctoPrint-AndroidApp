package android.app.printerapp.util.ui;

import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

/**
 * Created by alberto-baeza on 11/20/14.
 */
public class AnimationHelper {

    // To animate view slide out from right to left
    public static void slideToLeft(View view){
        TranslateAnimation animate = new TranslateAnimation(40, 0,0,0);
        animate.setDuration(500);
        animate.setFillAfter(false);
        view.startAnimation(animate);
    }

    //Animate fragments
    public static void inFromRightAnimation(View v){
        Animation inFromRight = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, +1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        inFromRight.setDuration(240);
        inFromRight.setInterpolator(new AccelerateInterpolator());
        v.startAnimation(inFromRight);
    }

}
