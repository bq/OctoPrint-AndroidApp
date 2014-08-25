package android.app.printerapp.library.detail;

import java.io.File;
import java.util.ArrayList;
import android.app.printerapp.R;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.library.StorageController;
import android.app.printerapp.model.ModelFile;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**This class will create the detail view for every Project.
 * it will contain a list of the files inside the project and a set of options.
 * 
 * @author alberto-baeza
 *
 */
public class DetailViewFragment extends Fragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
			//Reference to View
			View rootView = null;
	
			//If is not new
			if (savedInstanceState==null){
				
				Bundle args = getArguments();
				
				//Inflate the fragment
				rootView = inflater.inflate(R.layout.detailview_layout,
						container, false);
				
				rootView.findViewById(R.id.detail_ib_print).setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						
						Log.i("OUT","Print this sheeeeeet");
						
					}
				});
				
				final ModelFile f = (ModelFile) StorageController.getFileList().get(args.getInt("index"));
				
				ImageView iv = (ImageView) rootView.findViewById(R.id.detail_iv_preview);
				iv.setImageDrawable(f.getSnapshot());
				
				rootView.findViewById(R.id.detail_ib_favorite).setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						
						if (DatabaseController.isFavorite(f.getName())){
							DatabaseController.handleFavorite(f, false);
						} else DatabaseController.handleFavorite(f, true);
						
					}
				});
				
				TextView tv = (TextView) rootView.findViewById(R.id.detail_tv_name);
				tv.setText(f.getName());
				
				
				//Create a file adapter with every gcode
				File[] listFiles = new File(f.getGcodeList()).getParentFile().listFiles();
				ArrayList<File> arrayFiles = new ArrayList<File>();
				
				
				for (int i = 0; i<listFiles.length; i++){
					
					arrayFiles.add(listFiles[i]);
					
				}
				
				//add also de the stl
				arrayFiles.add(new File(f.getStl()));
				
				DetailViewAdapter adapter = new DetailViewAdapter(getActivity(), R.layout.detailview_list_element, arrayFiles, f.getSnapshot());
				ListView lv = (ListView) rootView.findViewById(R.id.detail_lv);
				lv.setAdapter(adapter);
					
				
				
			}

		return rootView;
	}


}
