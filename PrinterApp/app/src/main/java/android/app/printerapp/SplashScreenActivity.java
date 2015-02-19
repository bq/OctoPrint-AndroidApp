package android.app.printerapp;

import android.app.Activity;
import android.app.printerapp.devices.DevicesListController;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.library.LibraryController;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Splash screen activity that shows the logo of the app during a time interval
 * of 3 seconds. Then, the main activity is charged and showed.
 *
 * @author sara-perez
 */
public class SplashScreenActivity extends Activity {

    private static final String TAG = "SplashScreenActivity";

    //Set the duration of the splash screen
    private static final long SPLASH_SCREEN_DELAY = 3000;

    Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash_screen);

        mContext = this;

        //Initialize db and lists
        new DatabaseController(this);
        DevicesListController.loadList(this);
        LibraryController.initializeHistoryList();

        //Initialize default settings
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);


        if (isTaskRoot()){

            //Simulate a long loading process on application startup
            Timer timer = new Timer();
            timer.schedule(splashDelay, SPLASH_SCREEN_DELAY);

        }else finish();

    }

    TimerTask splashDelay = new TimerTask() {
        @Override
        public void run() {

            Log.d(TAG, "[START PRINTERAPP]");

            Intent mainIntent = new Intent().setClass(
                    SplashScreenActivity.this, MainActivity.class);
            startActivity(mainIntent);

            //Close the activity so the user won't able to go back this
            //activity pressing Back button
            finish();
        }
    };

}
