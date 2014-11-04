package android.app.printerapp.viewer.sidepanel;

import android.app.printerapp.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by alberto-baeza on 10/30/14.
 */
public class SidePanelProfileAdapter  extends ArrayAdapter<JSONObject>{

    public SidePanelProfileAdapter(Context context, int resource, List<JSONObject> objects) {
        super(context, resource, objects);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        JSONObject object = getItem(position);

        //View not yet created
        if (convertView==null){

            //Inflate the view
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.print_panel_spinner_item, null, false);

            holder = new ViewHolder();
            holder.mProfileName = (TextView) convertView.findViewById(R.id.print_panel_spinner_text);
            convertView.setTag(holder);


        } else {

            holder = (ViewHolder) convertView.getTag();
        }

        try {
            holder.mProfileName.setText(object.getString("displayName"));
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        JSONObject object = getItem(position);

        //View not yet created
        if (convertView==null){

            //Inflate the view
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.print_panel_spinner_item, null, false);

            holder = new ViewHolder();
            holder.mProfileName = (TextView) convertView.findViewById(R.id.print_panel_spinner_text);
            convertView.setTag(holder);


        } else {

            holder = (ViewHolder) convertView.getTag();
        }

        try {
            holder.mProfileName.setText(object.getString("displayName"));
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return convertView;
    }

    static class ViewHolder{

        TextView mProfileName;

    }
}
