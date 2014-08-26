package android.app.printerapp.library.detail;

import java.util.List;

import android.app.printerapp.R;
import android.app.printerapp.model.ModelComment;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DetailViewCommAdapter extends ArrayAdapter<ModelComment>{

	public DetailViewCommAdapter(Context context, int resource,
			List<ModelComment> objects) {
		super(context, resource, objects);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View v = convertView;	
		
		final ModelComment c = getItem(position);
		
		//View not yet created
		if (v==null){
			
			
			//Inflate the view
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.detailview_list_comment, null, false);
			
			
		} else {
			//v = convertView;
		}
		
		TextView tv1 = (TextView) v.findViewById(R.id.detailview_comment_tv1);
		TextView tv2 = (TextView) v.findViewById(R.id.detailview_comment_tv2);
		TextView tv3 = (TextView) v.findViewById(R.id.detailview_comment_tv3);
		
		tv1.setText(c.getAuthor());
		tv2.setText(c.getDate());
		tv3.setText(c.getComment());
		
		return v;
	}

}
