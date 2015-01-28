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

   private static boolean isForeground = true;

    @Override
    public void onReceive(Context context, Intent intent) {

        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);



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




            long id = intent.getLongExtra("printer", 0);
            int progress = intent.getIntExtra("progress", 0);

            //Target printer
            ModelPrinter p = DevicesListController.getPrinter(id);


            // Sets an ID for the notification
            int mNotificationId = (int)id;

        if (!isForeground){

            try{

                String text = null;

                String type = intent.getStringExtra("type");

                if (type.equals("finish")) text = context.getString(R.string.finish_dialog_title) + " " + p.getJob().getFilename();
                if (type.equals("print")) text = context.getString(R.string.notification_printing_progress);
                if (type.equals("slice")) text = context.getString(R.string.notification_slicing_progress);


                //Creates notification
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(context)
                                .setSmallIcon(R.drawable.notification_logo)
                                .setContentTitle(p.getDisplayName())
                                .setContentText(text + " (" + progress + "%)")
                                .setAutoCancel(true);

                mBuilder.setContentIntent(resultPendingIntent);



                // Builds the notification and issues it.
                mNotifyMgr.notify(mNotificationId, mBuilder.build());

            } catch (NullPointerException e){

                e.printStackTrace();
            }

        } else {

            mNotifyMgr.cancel(mNotificationId);


        }




    }

    //Change if the application goes background
    public static void setForeground (boolean foreground){
        isForeground = foreground;

    }
}
