package android.app.printerapp.library;

import java.util.ArrayList;
import java.util.Comparator;

import android.app.AlertDialog;
import android.app.printerapp.R;
import android.app.printerapp.model.ModelFile;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RadioGroup;
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
			
			mAdapter.sort(new Comparator<ModelFile>() {
			    public int compare(ModelFile arg0, ModelFile arg1) {
			        return arg0.getStorage().compareTo(arg1.getStorage());
			    }
			});
			
		
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
	   
	   case R.id.library_search: 
		   optionSearchLibrary();
			return true;
			
       	case R.id.library_filter: 
       		optionFilterLibrary();
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
		
	
	//Retrieve all files from the system
	//TODO How to handle printer files?
	public void retrieveAllFiles(){
		
		mCurrentFileList = StorageController.getFileList();
		
				
	}
	
	//Filter elements in the current tab from the menu option
	//TODO WIP still not functional
	public void optionFilterLibrary(){
		
		AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
		adb.setTitle(R.string.filter);
		
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.menu_filter_library_dialog, null, false);
		
		final RadioGroup rg = (RadioGroup) v.findViewById(R.id.radioGroup_library);
		
		adb.setView(v);
		
		adb.setPositiveButton(R.string.filter,  new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
								
				switch (rg.getCheckedRadioButtonId()){
				
				case R.id.lb_radio0:
					
					mAdapter.getFilter().filter(null);
					
					break;
					
				case R.id.lb_radio1:
					
					mAdapter.getFilter().filter("gcode");
					
					break;
					
				case R.id.lb_radio2:
					
					mAdapter.getFilter().filter("stl");
					
					break;
				
				}
				
			}
		});
		adb.setNegativeButton(R.string.cancel, null);
		
		adb.show();
	}
	
	public void optionSearchLibrary(){
		
		AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
		adb.setTitle(R.string.library_search_dialog_title);
		
		EditText et = new EditText(getActivity());
		adb.setView(et);
		
		adb.setPositiveButton(R.string.search, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
								
			}
		});
		
		adb.setNegativeButton(R.string.cancel, null);
		adb.show();
		
	}

}
