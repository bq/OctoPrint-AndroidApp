package android.app.printerapp.settings;

import android.app.printerapp.R;
import android.app.printerapp.devices.database.DatabaseController;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * This class will manage the hidden/blacklisted new elements that shouldn't be shown in the main grid
 * It's similar to the normal linked printers list without some functionality
 * Created by alberto-baeza on 11/24/14.
 */
public class SettingsHiddenAdapter extends ArrayAdapter<String> {

    private ArrayList<String>mCurrent;

    public SettingsHiddenAdapter(Context context, int resource,
                               List<String> objects) {

        super(context, resource, objects);

        mCurrent = (ArrayList<String>)objects;

    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {


        View v = convertView;
        final String s = getItem(position);


        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        if (v == null) {


            v = inflater.inflate(R.layout.settings_row, null, false);

        } else {

            //v = inflater.inflate(R.layout.settings_row, null, false);


        }

        TextView tv = (TextView) v.findViewById(R.id.settings_text);
        tv.setText(s);

        ImageView iv = (ImageView) v.findViewById(R.id.imageView_settings);
        iv.setImageResource(R.drawable.icon_detectedprinter);

        v.findViewById(R.id.settings_connection).setVisibility(View.GONE);
        v.findViewById(R.id.settings_edit).setVisibility(View.GONE);
        v.findViewById(R.id.settings_delete).setVisibility(View.GONE);

        v.findViewById(R.id.settings_hide).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatabaseController.handlePreference("Blacklist",s, null, false);
                mCurrent.remove(position);
                notifyDataSetChanged();


            }
        });

        return v;



    }
}
