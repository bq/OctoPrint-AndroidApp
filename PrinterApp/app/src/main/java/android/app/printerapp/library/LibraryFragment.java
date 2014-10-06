package android.app.printerapp.library;

import java.io.File;
import java.util.Comparator;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.printerapp.R;
import android.app.printerapp.viewer.FileBrowser;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.ViewSwitcher;

/**
 * Fragment to show the library with files on the system/remote
 * @author alberto-baeza
 *
 */
public class LibraryFragment extends Fragment {
	
	private StorageAdapter mAdapter;
	private StorageAdapter mListAdapter;
	
	private ViewSwitcher mSwitcher;
	
	private String mCurrentFilter = null;
	
	private File mMoveFile = null;
	
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
			
			rootView.setFocusableInTouchMode(true);
			rootView.setOnKeyListener(new OnKeyListener() {
				
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					
					if( keyCode == KeyEvent.KEYCODE_BACK )
			        {
						
						Log.i("OUT","JHSOTOAIA PUTAO");
			            return true;
			        }
					
					return false;
				}
			});
			
			/**
			 * CUSTOM VIEW METHODS
			 */
			
			//References to adapters
			//TODO maybe share a gridview
			
			mSwitcher = (ViewSwitcher) rootView.findViewById(R.id.view_switcher_library);

			
			//Initial file list
			StorageController.reloadFiles("all");
			
			mAdapter = new StorageAdapter(getActivity(), R.layout.library_grid_element, StorageController.getFileList());
			mListAdapter = new StorageAdapter(getActivity(),R.layout.library_list_element, StorageController.getFileList());
			
			GridView g = (GridView) rootView.findViewById(R.id.grid_storage);
			
			StorageOnClickListener clickListener = new StorageOnClickListener(this);
			
			g.setOnItemClickListener(clickListener);
			g.setOnItemLongClickListener(clickListener);
			g.setAdapter(mAdapter);
			
			ListView l = (ListView) rootView.findViewById(R.id.list_storage);
			l.setOnItemClickListener(clickListener);
			l.setOnItemLongClickListener(clickListener);
			l.setAdapter(mListAdapter);

			//Set tab host for the view
			setTabHost(rootView);
			
			sortAdapter();

		}
		return rootView;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.library_menu, menu);

		if (mMoveFile!=null) {

			menu.findItem(R.id.library_paste).setVisible(true);
		} else menu.findItem(R.id.library_paste).setVisible(false);
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
            
       	case R.id.library_add:
       			
       		optionAddLibrary();
       		
       		return true;
       		
       	case R.id.library_list:
       		if (mSwitcher.getCurrentView().getId() == (R.id.list_storage)){
    			item.setTitle(R.string.library_menu_list);
    			item.setIcon(android.R.drawable.list_selector_background);
    		} else item.setTitle(R.string.library_menu_grid);
       		optionSwitchList();
       		
       		return true;
            
       	case R.id.library_create:
       		
       		optionCreateLibrary();
       		
       		return true;
       		
       	case R.id.library_paste:
       		
       		optionPaste();
       		
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
		spec.setIndicator(getString(R.string.library_tabhost_tab_all));
		spec.setContent(R.id.tab1);
		tabs.addTab(spec);
		 
		spec=tabs.newTabSpec("Local");
		spec.setIndicator(getString(R.string.library_tabhost_tab_local));
		spec.setContent(R.id.tab2);
		tabs.addTab(spec);
		
		spec=tabs.newTabSpec("Printer");
		spec.setIndicator(getString(R.string.library_tabhost_tab_printer));
		spec.setContent(R.id.tab3);
		tabs.addTab(spec);
		
		spec=tabs.newTabSpec("Favorites");
		spec.setIndicator(getString(R.string.library_tabhost_tab_favorites));
		spec.setContent(R.id.tab4);
		tabs.addTab(spec);
		
		
		tabs.setCurrentTab(0);
		
		tabs.getTabWidget().setBackgroundColor(Color.parseColor("#333333"));
		for(int i=0;i<tabs.getTabWidget().getChildCount();i++) 
	    {
	        TextView tv = (TextView) tabs.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
	        tv.setTextColor(Color.parseColor("#ffffff"));
	    } 
		
		tabs.setOnTabChangedListener(new OnTabChangeListener() {
		    @Override
		    public void onTabChanged(String tabId) {

		    	switch (tabs.getCurrentTab()) {				
				case 0:
						StorageController.reloadFiles("all");
						//StorageController.reloadFiles(StorageController.getParentFolder().getAbsolutePath());
					break;
				case 1:
						StorageController.reloadFiles(StorageController.getParentFolder().getAbsolutePath());					
					break; 
				case 2:
						StorageController.reloadFiles("printer");
					break;
					
				case 3:		
						//StorageController.reloadFiles(StorageController.getParentFolder().getAbsolutePath() + "/Files");
						StorageController.retrieveFavorites();
					break;

				default:
					break;
				}
		    	
		    	sortAdapter();
		    	
		    }
		});
		
	}
		
		
	//Filter elements in the current tab from the menu option
	public void optionFilterLibrary(){
		
		//Dialog to filter
		AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
		adb.setTitle(R.string.filter);
		
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.libray_menu_filter_dialog, null, false);
		
		final RadioGroup rg = (RadioGroup) v.findViewById(R.id.radioGroup_library);
		
		adb.setView(v);
		
		adb.setPositiveButton(R.string.filter,  new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
								
				switch (rg.getCheckedRadioButtonId()){
				
				case R.id.lb_radio3: //remove all filters and reload the whole list
					
					mCurrentFilter = null;
					StorageController.reloadFiles(StorageController.getParentFolder().getAbsolutePath());

					break;
									
				case R.id.lb_radio1: //Show gcodes only
					mCurrentFilter = "gcode";
					
					break;
					
				case R.id.lb_radio2: //Show stl only
					mCurrentFilter = "stl";
					
					break;
				
				}
				
				//Apply current filter
				if (mCurrentFilter!=null){
					mAdapter.getFilter().filter(mCurrentFilter);
					mListAdapter.getFilter().filter(mCurrentFilter);
					
				} else {
					mAdapter.removeFilter();
					mListAdapter.removeFilter();
				}
				
				sortAdapter();
				
			}
		});
		adb.setNegativeButton(R.string.cancel, null);
		
		adb.show();
	}
	
	//Search an item within the library applying a filter to the adapter
	public void optionSearchLibrary(){
		
		AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
		adb.setTitle(R.string.library_search_dialog_title);
		
		final EditText et = new EditText(getActivity());
		adb.setView(et);
		
		adb.setPositiveButton(R.string.search, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mCurrentFilter = et.getText().toString();
				mAdapter.getFilter().filter(mCurrentFilter);
				
			}
		});
		
		adb.setNegativeButton(R.string.cancel, null);
		adb.show();
		
	}
	
	//Add a new project using the viewer file browser
	public void optionAddLibrary(){		
		
		//TODO fix filebrowser parameters
		FileBrowser.openFileBrowser(getActivity(),FileBrowser.LIBRARY, getString(R.string.library_menu_add), ".stl", "");

	}
	
	//Create a single new folder via mkdir
	public void optionCreateLibrary(){
		
		AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
		adb.setTitle(R.string.library_create_dialog_title);
		
		final EditText et = new EditText(getActivity());
		adb.setView(et);
		et.setText("New");
		et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
		et.setSelectAllOnFocus(true);
		
		adb.setPositiveButton(R.string.create, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
						
				
				String name = et.getText().toString();
				
				if (name!=null) StorageController.createFolder(name);
				notifyAdapter();
				
			}
		});
		
		adb.setNegativeButton(R.string.cancel, null);
		adb.show();
		
	}
	
	public void optionSwitchList(){

		mSwitcher.showNext();
		notifyAdapter();
		
		
	}
	
	public void optionPaste(){
		
		//Copy file to new folder
		File fileTo = new File(StorageController.getCurrentPath() + "/" + mMoveFile.getName());
		
		//Delete file if success
		if (!mMoveFile.renameTo(fileTo)) {
		    mMoveFile.delete();
		}
		
		StorageController.reloadFiles(StorageController.getCurrentPath().getAbsolutePath());				
		sortAdapter();
		
		setMoveFile(null);
	}
	
	//Random adapter with lots of comparisons
	@SuppressLint("DefaultLocale")
	public void sortAdapter(){
		
		
		if (mCurrentFilter!=null) mAdapter.removeFilter();
		
		//Sort by absolute file (puts folders before files)
		mAdapter.sort(new Comparator<File>() {
						
			public int compare(File arg0, File arg1) {
				
				if (arg0.getParent().equals("printer")) return -1;
				
				//Must check all cases, Folders > Projects > Files
				if (arg0.isDirectory()){
					
					if (StorageController.isProject(arg0)){
						
						if (arg1.isDirectory()){
							if (StorageController.isProject(arg1)) return arg0.getName().toLowerCase().compareTo(arg1.getName().toLowerCase());
							else return 1;
						}				
						else return -1;
						
					} else {
						
						if (arg1.isDirectory()) {
							
							if (StorageController.isProject(arg1)) return -1;
							else return arg0.getName().toLowerCase().compareTo(arg1.getName().toLowerCase());
						
						} else return -1;
						
					}
					
				} else {
					
					if (arg1.isDirectory()) return 1;
					else return arg0.getName().toLowerCase().compareTo(arg1.getName().toLowerCase());
					
				}
				
		    }
		});
		
		//Apply the current filter to the folder
		if (mCurrentFilter!=null) mAdapter.getFilter().filter(mCurrentFilter);
		notifyAdapter();
	}
	
	
	
	public void notifyAdapter(){
		
		mAdapter.notifyDataSetChanged();
		mListAdapter.notifyDataSetChanged();
	}
	
	public void setMoveFile(File file){
		
		mMoveFile = file;
		getActivity().invalidateOptionsMenu();
	}
	
	//onBackPressed handler to the file browser
	public boolean goBack(){
		
		if (!StorageController.getCurrentPath().getAbsolutePath().equals(StorageController.getParentFolder().getAbsolutePath())){
			Log.i("OUT","This is " + StorageController.getCurrentPath());
			StorageController.reloadFiles(StorageController.getCurrentPath().getParent());				
			sortAdapter();
			
			return true;
			
		}
		
		return false;
		
	}
	
	
	

}
