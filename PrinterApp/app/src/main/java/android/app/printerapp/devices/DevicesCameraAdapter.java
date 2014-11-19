package android.app.printerapp.devices;

import android.app.printerapp.R;
import android.app.printerapp.devices.camera.CameraHandler;
import android.app.printerapp.model.ModelPrinter;
import android.app.printerapp.octoprint.StateUtils;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class DevicesCameraAdapter extends ArrayAdapter<ModelPrinter> {

    ArrayList<CameraHandler> mVideoList;

    public DevicesCameraAdapter(Context context, int resource,
                                List<ModelPrinter> objects) {
        super(context, resource, objects);

        mVideoList = new ArrayList<CameraHandler>();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        ViewHolder holder;

        ModelPrinter m = getItem(position);

        //View not yet created
        if (convertView == null) {

            //Inflate the view
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.video_view, null, false);

            holder = new ViewHolder();
            holder.videoItem = (LinearLayout) convertView.findViewById(R.id.video_layout);
            holder.textView = (TextView) convertView.findViewById(R.id.textView_video);


            if ((m.getStatus() != StateUtils.STATE_NEW) && (m.getStatus() != StateUtils.STATE_ADHOC)
                    && (m.getStatus() != StateUtils.STATE_NONE))
            {


                //Remove all previous views (refresh icon)
                holder.videoItem.removeAllViews();
                holder.cameraHandler = new CameraHandler(getContext(),m.getAddress());
                holder.videoItem.addView(holder.cameraHandler.getView());


                //TODO fix video list and create an external one to be able to notify the list
                //mVideoList.clear();
                mVideoList.add(holder.cameraHandler);

                Log.i("OUT", "Size : " + mVideoList.size());

                holder.cameraHandler.startVideo();
            } else holder.videoItem.setVisibility(View.GONE);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.textView.setText(m.getDisplayName());
        holder.textView.bringToFront();

        /*if (holder.cameraHandler!=null){

            if (!holder.cameraHandler.isRunning) {

                holder.progressBar.bringToFront();

            } else holder.videoItem.bringToFront();

            Log.i("OUT","i have a camera");

        } else {

                Log.i("OUT","i don have a camera yett");

            }*/


        return convertView;
    }

    public void hideSurfaces() {

        for (CameraHandler camera : mVideoList){

            Log.i("OUT","CHAPANDO");

                camera.getView().stopPlayback();
                camera.getView().setVisibility(View.GONE);
        }


    }

    static class ViewHolder {

        LinearLayout videoItem;
        CameraHandler cameraHandler;
        TextView textView;

    }

}
