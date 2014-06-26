package android.app.printerapp.library;

import java.util.ArrayList;

import android.app.printerapp.R;
import android.app.printerapp.devices.DevicesListController;
import android.app.printerapp.model.ModelFile;
import android.app.printerapp.model.ModelPrinter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

public class LibraryFragment extends Fragment {
	
	StorageAdapter mAdapter;
	
	ArrayList<ModelFile> mCurrentFileList = new ArrayList<ModelFile>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Retain instance to keep the Fragment from destroying itself
		setRetainInstance(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		//Reference to View
		View rootView = null;
				
		//If is not new
		if (savedInstanceState==null){
			
			//Show custom option menu
			setHasOptionsMenu(true);
			
			//Inflate the fragment
			rootView = inflater.inflate(R.layout.library_layout,
					container, false);
			
			/**
			 * CUSTOM VIEW METHODS
			 */
			retrieveAllFiles();

			
			mAdapter = new StorageAdapter(getActivity(), R.layout.storage_main, mCurrentFileList);
			
			
			GridView g = (GridView) rootView.findViewById(R.id.grid_storage);
			g.setAdapter(mAdapter);
			
			GridView gw = (GridView) rootView.findViewById(R.id.grid_storage_witbox);
			gw.setAdapter(mAdapter);
			
			GridView gu = (GridView) rootView.findViewById(R.id.grid_storage_usb);
			gu.setAdapter(mAdapter);
			
			
			//Set tab host for the view
			setTabHost(rootView);
			
		
		}
		return rootView;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.library_menu, menu);
	}
	
	//Option menu
   @Override
	public boolean onOptionsItemSelected(android.view.MenuItem item) {
	   
	   switch (item.getItemId()) {
	   
	   case R.id.library_search: //Add a new printer;
			return true;
			
       	case R.id.library_filter: //Filter grid / list

            return true;
              
          
       default:
           return super.onOptionsItemSelected(item);
	   }
	}
   
	
	/**
	 * Constructor for the tab host
	 * TODO: Should be moved to a View class since it only handles ui.
	 */
	public void setTabHost(View v){
				 
		final TabHost tabs=(TabHost) v.findViewById(android.R.id.tabhost);
		tabs.setup();
		 
		TabHost.TabSpec spec=tabs.newTabSpec("Models");
		spec.setIndicator("All the models");
		spec.setContent(R.id.tab1);
		tabs.addTab(spec);
		 
		spec=tabs.newTabSpec("Witbox");
		spec.setIndicator("Witbox memory");
		spec.setContent(R.id.tab2);
		tabs.addTab(spec);
		
		spec=tabs.newTabSpec("Usb");
		spec.setIndicator("USB");
		spec.setContent(R.id.tab3);
		tabs.addTab(spec);
		
		spec=tabs.newTabSpec("Favorites");
		spec.setIndicator("Favorites");
		spec.setContent(R.id.tab4);
		tabs.addTab(spec);
		
		 
		tabs.setCurrentTab(0);
		
		tabs.setOnTabChangedListener(new OnTabChangeListener() {
		    @Override
		    public void onTabChanged(String tabId) {
		        
		    	switch (tabs.getCurrentTab()) {
				case 0:
						mAdapter.getFilter().filter(null);
					break;
				case 1:
						mAdapter.getFilter().filter("Witbox");
					break;
				case 2:
						mAdapter.getFilter().filter("sd");
					break;

				default:
					break;
				}
		    	
		    	
		    }
		});
		
	}
	
	public void retrieveAllFiles(){
		
		mCurrentFileList = StorageController.getFileList();
		
		for (ModelPrinter p : DevicesListController.getList()){
			
			mCurrentFileList.addAll(p.getFiles());
			
		}
		
	}

}
