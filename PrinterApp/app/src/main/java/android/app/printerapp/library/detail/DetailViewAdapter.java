package android.app.printerapp.library.detail;

import android.app.printerapp.MainActivity;
import android.app.printerapp.R;
import android.app.printerapp.devices.DevicesListController;
import android.app.printerapp.library.LibraryController;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.material.widget.PaperButton;

import java.io.File;
import java.util.List;


/**
 * This is the adapter for the detail view
 *
 * @author alberto-baeza
 */
public class DetailViewAdapter extends ArrayAdapter<File> {

    private Drawable mDrawable;

    public DetailViewAdapter(Context context, int resource, List<File> objects, Drawable d) {
        super(context, resource, objects);
        mDrawable = d;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        final File f = getItem(position);

        //View not yet created
        if (v == null) {

            //Inflate the view
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.detailview_list_element, null, false);

        } else {
            //v = convertView;
        }

        //UI references
        TextView tv1 = (TextView) v.findViewById(R.id.detailview_list_tv1);
        tv1.setText(f.getName());

        PaperButton ib = (PaperButton) v.findViewById(R.id.detailview_list_iv1);
        PaperButton ibe = (PaperButton) v.findViewById(R.id.detailview_list_iv2);

        if ((LibraryController.hasExtension(1, f.getName()))){
            ibe.setVisibility(View.GONE);
            ib.setVisibility(View.VISIBLE);
            //it's an stl
        }else {
            ibe.setVisibility(View.VISIBLE);
            ib.setVisibility(View.GONE);
        }

        //Print button
        ib.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
            if ((LibraryController.hasExtension(0, f.getName())))
                MainActivity.requestOpenFile(f.getAbsolutePath());
            else DevicesListController.selectPrinter(v.getContext(), f, null);

            }
        });

        //Edit button
        ibe.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                MainActivity.requestOpenFile(f.getAbsolutePath());
            }
        });

        return v;
    }


}
