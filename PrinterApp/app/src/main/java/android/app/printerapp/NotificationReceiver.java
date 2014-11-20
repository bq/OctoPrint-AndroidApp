package android.app.printerapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.printerapp.devices.DevicesListController;
import android.app.printerapp.model.ModelPrinter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

/**
 * This class will handle asynchronous notifications from the server when a printing is finished
 * and the app is listening. Declared globally in the manifest to avoid leaking intents
 *
 * Created by alberto-baeza on 11/20/14.
 */
public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        long id = intent.getLongExtra("printer", 0);

        //Target printer
        ModelPrinter p = DevicesListController.getPrinter(id);


        // Sets an ID for the notification
        int mNotificationId = (int)id;


        Intent resultIntent = new Intent(context, SplashScreenActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT|
                Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        //TODO random crash
        try{
            //Creates notification
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.notification_logo)
                            .setContentTitle(context.getString(R.string.finish_dialog_title) + " " + p.getJob().getFilename())
                            .setContentText(p.getDisplayName())
                            .setAutoCancel(true);

            mBuilder.setContentIntent(resultPendingIntent);

            // Gets an instance of the NotificationManager service
            NotificationManager mNotifyMgr =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            // Builds the notification and issues it.
            mNotifyMgr.notify(mNotificationId, mBuilder.build());

        } catch (NullPointerException e){

            e.printStackTrace();
        }



    }
}
