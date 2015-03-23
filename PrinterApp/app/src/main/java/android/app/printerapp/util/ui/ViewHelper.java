package android.app.printerapp.util.ui;

import android.app.printerapp.R;
import android.media.Image;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;

import com.material.widget.PaperButton;

import java.lang.reflect.ParameterizedType;

/**
 * Created by alberto-baeza on 2/26/15.
 */
public class ViewHelper {

    public static void disableEnableAllViews(boolean enable, ViewGroup vg){


        for (int i = 0; i < vg.getChildCount(); i++){
            View child = vg.getChildAt(i);
            child.setEnabled(enable);
            if (child instanceof ViewGroup){
                disableEnableAllViews(enable, (ViewGroup)child);
            }

            if (child instanceof PaperButton) {
                PaperButton paperButton = (PaperButton) child;
                paperButton.setClickable(enable);
                paperButton.refreshTextColor(enable);
            }

            if (child instanceof LockableScrollView) {
                LockableScrollView scrollView = (LockableScrollView) child;
                scrollView.setScrollingEnabled(enable);
                scrollView.setVerticalScrollBarEnabled(enable);
                scrollView.setHorizontalScrollBarEnabled(enable);
            }
        }
    }
}
