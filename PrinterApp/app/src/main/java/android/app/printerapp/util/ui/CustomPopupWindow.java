package android.app.printerapp.util.ui;

import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.widget.PopupWindow;

/**
 * Util class that allow to create a pop up window with a custom content view
 */
public class CustomPopupWindow {

    private View mContentView;
    private int mWidth;
    private int mHeight;
    private int mAnimationId;
    private boolean mOutsideTouchable = false;

    /**
     * Initialize the params of the pop up window
     * @param contentView View to be included in the pop up window
     * @param width Width of the pop up window. This need not be the same size of the content view.
     * @param height Height of the pop up window. This need not be the same size of the content view.
     * @param animationStyleId int id of the animation to be included when the pop up window is displayed. If the id
     *                         is -1, an animation is not be included.
     */
    public CustomPopupWindow(View contentView, int width, int height, int animationStyleId) {
        mContentView = contentView;
        mWidth = width;
        mHeight = height;
        mAnimationId = animationStyleId;
    }

    /**
     * Initialize the params of the pop up window
     * @param contentView View to be included in the pop up window
     * @param width Width of the pop up window. This need not be the same size of the content view.
     * @param height Height of the pop up window. This need not be the same size of the content view.
     * @param animationStyleId int id of the animation to be included when the pop up window is displayed. If the id
     *                         is -1, an animation is not be included.
     * @param outsideTouchable Indicates if the popup window can be dismissed when clicked outside of it
     */
    public CustomPopupWindow(View contentView, int width, int height, int animationStyleId, boolean outsideTouchable) {
        mContentView = contentView;
        mWidth = width;
        mHeight = height;
        mAnimationId = animationStyleId;
        mOutsideTouchable = outsideTouchable;
    }

    public PopupWindow getPopupWindow() {

        PopupWindow popupWindow = new PopupWindow(
        		mContentView, mWidth, mHeight);

        //Needed for dismiss the popup window when clicked outside the popup window
        popupWindow.setOutsideTouchable(mOutsideTouchable);
        popupWindow.setFocusable(false);

        //Set the animation of the pop up window
        if(mAnimationId > 0) popupWindow.setAnimationStyle(mAnimationId);

        //Clear the default translucent background
        popupWindow.setBackgroundDrawable(new BitmapDrawable());

        return popupWindow;
    }

    public void hidePopup(){

       getPopupWindow().update(0,0);

    }

}