package android.app.printerapp.library;

import android.app.printerapp.R;
import android.app.printerapp.model.ModelFile;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class DetailViewFragment extends Fragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
			//Reference to View
			View rootView = null;
			
			Bundle args = getArguments();
					
			//If is not new
			if (savedInstanceState==null){
				
				//Inflate the fragment
				rootView = inflater.inflate(R.layout.detailview_layout,
						container, false);
				
				rootView.findViewById(R.id.detail_ib_print).setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						
						Log.i("OUT","Print this sheeeeeet");
						
					}
				});
				
				ModelFile f = (ModelFile) StorageController.getFileList().get(args.getInt("index"));
				
				ImageView iv = (ImageView) rootView.findViewById(R.id.detail_iv_preview);
				iv.setImageDrawable(f.getSnapshot());
				
				TextView tv = (TextView) rootView.findViewById(R.id.detail_tv_name);
				tv.setText(f.getName());
				
				
			}
			else{
				
			}

		return rootView;
	}


}
