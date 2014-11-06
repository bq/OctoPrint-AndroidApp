package android.app.printerapp.viewer.sidepanel;

import android.app.printerapp.R;
import android.app.printerapp.model.ModelPrinter;
import android.app.printerapp.octoprint.StateUtils;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by alberto-baeza on 10/30/14.
 */
public class SidePanelPrinterAdapter extends ArrayAdapter<ModelPrinter> {



    public SidePanelPrinterAdapter(Context context, int resource, List<ModelPrinter> objects) {
        super(context, resource, objects);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        ModelPrinter m = getItem(position);

        //View not yet created
        if (v==null){

            //Inflate the view
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.print_panel_spinner_item, null, false);



        } else {
            //v = convertView;
        }

        TextView tv = (TextView) v.findViewById(R.id.print_panel_spinner_text);


        if (m.getStatus() != StateUtils.STATE_OPERATIONAL){

            tv.setTextColor(Color.GRAY);

        } else tv.setTextColor(Color.BLACK);

        tv.setText(m.getDisplayName());

        return v;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        ModelPrinter m = getItem(position);

        //View not yet created
        if (v==null){

            //Inflate the view
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.print_panel_spinner_item, null, false);



        } else {
            //v = convertView;
        }

        TextView tv = (TextView) v.findViewById(R.id.print_panel_spinner_text);


        if (m.getStatus() != StateUtils.STATE_OPERATIONAL){

            tv.setTextColor(Color.GRAY);

        } else tv.setTextColor(Color.BLACK);

        tv.setText(m.getDisplayName());

        return v;
    }
}
