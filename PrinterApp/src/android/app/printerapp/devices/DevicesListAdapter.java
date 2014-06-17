package android.app.printerapp.devices;

import java.util.List;
import java.util.Scanner;

import android.app.printerapp.R;
import android.app.printerapp.model.ModelJob;
import android.app.printerapp.model.ModelPrinter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DevicesListAdapter extends ArrayAdapter<ModelPrinter>{

	public DevicesListAdapter(Context context, int resource,
			List<ModelPrinter> objects) {
		super(context, resource, objects);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View v;
		ModelPrinter m = getItem(position);
		
		
		//View not yet created
		if (convertView==null){
			
			
			
			//Inflate the view
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.list_element, null, false);
			
		} else {
			v = convertView;
		}
		
		//Printer tag reference
		TextView tag = (TextView) v.findViewById(R.id.list_column_1_text);
		tag.setText(m.getName());
		
		ModelJob job = m.getJob();
		
		
		//Job references, if there's no job, don't update anything
		//TODO: Remove null option
		if (job!=null){

			TextView file = (TextView) v.findViewById(R.id.list_column_2);
			file.setText(job.getFilename());
			
			TextView status = (TextView) v.findViewById(R.id.list_column_3);	
			status.setText(String.valueOf(calculatePercentage(job.getPrinted(), job.getSize())
					+ "% (" + job.getPrintTimeLeft() + " left)"));
			
		}
		
		
		return v;
	}
	
	
	//Transform item size / current to a percentage to show	on the list view
	public static int calculatePercentage(String c, String t){
		
		int result = 0;
		
		
		
		if ((c!="null") && (t!="null")){
			
			if (c.substring(c.length()-1).equals("B")){
				Scanner sc = new Scanner(c);
				sc.useDelimiter("[^0-9]+");			
				int current = sc.nextInt();
				sc.close();
				
				Scanner st = new Scanner(t);
				st.useDelimiter("[^0-9]+");			
				int total = st.nextInt();
				st.close();
				
				result = ( current * 100 ) / total;
			}
			
		}
				
		return result;
	}

}
