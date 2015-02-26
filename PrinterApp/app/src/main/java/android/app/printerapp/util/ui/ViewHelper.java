package android.app.printerapp.util.ui;

import android.view.View;
import android.view.ViewGroup;

import com.gc.materialdesign.views.Button;

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
                if (child instanceof Button) child.setClickable(false);
            }
        }

    }
}
