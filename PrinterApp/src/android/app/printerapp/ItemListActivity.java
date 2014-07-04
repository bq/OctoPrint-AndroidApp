package android.app.printerapp;

import android.app.printerapp.devices.DevicesFragment;
import android.app.printerapp.devices.DevicesListController;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.library.LibraryFragment;
import android.app.printerapp.library.StorageController;
import android.app.printerapp.settings.SettingsFragment;
import android.app.printerapp.viewer.ViewerMain;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

/**
 * An activity representing a list of Items. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link ItemDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ItemListFragment} and the item details (if present) is a
 * {@link ItemDetailFragment}.
 * <p>
 * This activity also implements the required {@link ItemListFragment.Callbacks}
 * interface to listen for item selections.
 */
public class ItemListActivity extends FragmentActivity implements
		ItemListFragment.Callbacks {

	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	private boolean mTwoPane;
	
	private static DevicesFragment mDevicesFragment;
	private LibraryFragment mLibraryFragment;
	private static ViewerMain mViewerFragment;
	private SettingsFragment mSettingsFragment;
	
	private Fragment mCurrent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_item_list);

		if (findViewById(R.id.item_detail_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;

			// In two-pane mode, list items should be given the
			// 'activated' state when touched.
			((ItemListFragment) getSupportFragmentManager().findFragmentById(
					R.id.item_list)).setActivateOnItemClick(true);
		}

		// TODO: If exposing deep links into your app, handle intents here.
		
		//TODO List controllers
		new StorageController();
		//new ViewerMain();
		new DatabaseController(this);
		new DevicesListController();
		
		
		mDevicesFragment = (DevicesFragment) getSupportFragmentManager().findFragmentByTag("Devices");
		mLibraryFragment = (LibraryFragment) getSupportFragmentManager().findFragmentByTag("Library");
		mViewerFragment = (ViewerMain) getSupportFragmentManager().findFragmentByTag("Viewer");
		mSettingsFragment = (SettingsFragment) getSupportFragmentManager().findFragmentByTag("Settings");
	}

	/**
	 * Callback method from {@link ItemListFragment.Callbacks} indicating that
	 * the item with the given ID was selected.
	 */
	@Override
	public void onItemSelected(String id) {
		
		if (mTwoPane) {
			
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			
			//Switch between list ids
			
			if (mCurrent!=null)getSupportFragmentManager().beginTransaction().hide(mCurrent).commit();
			ActionModeHandler.modeFinish();			
			switch (Integer.valueOf(id)){
			
				case 1:{
					
					//Check if we already created the Fragment to avoid having multiple instances
					 if (getSupportFragmentManager().findFragmentByTag("Devices")==null){
						 mDevicesFragment = new DevicesFragment();
						 getSupportFragmentManager().beginTransaction().add(R.id.item_detail_container,mDevicesFragment, "Devices").commit();
							

					 } 
					 
					 mCurrent = mDevicesFragment;
					} break;
					
				case 2:{
					
					//Check if we already created the Fragment to avoid having multiple instances
					 if (getSupportFragmentManager().findFragmentByTag("Viewer")==null){
						 mViewerFragment = new ViewerMain();
						 getSupportFragmentManager().beginTransaction().add(R.id.item_detail_container,mViewerFragment, "Viewer").commit();
							
					 } 

					 mCurrent = mViewerFragment;
						 
					
				} break;
				
				case 3:{
					//Check if we already created the Fragment to avoid having multiple instances
					 if (getSupportFragmentManager().findFragmentByTag("Library")==null){
						 mLibraryFragment = new LibraryFragment();
						 getSupportFragmentManager().beginTransaction().add(R.id.item_detail_container,mLibraryFragment, "Library").commit();
						 
					 } 
						 
					mCurrent = mLibraryFragment;

				} break;
				
				case 5:{
					//Check if we already created the Fragment to avoid having multiple instances
					 if (getSupportFragmentManager().findFragmentByTag("Settings")==null){
						 mSettingsFragment = new SettingsFragment();
						 getSupportFragmentManager().beginTransaction().add(R.id.item_detail_container,mSettingsFragment, "Settings").commit();

					 } 
					 
					mCurrent = mSettingsFragment;

				}break;	
				
			}
			
			if (mCurrent!=null ) getSupportFragmentManager().beginTransaction().show(mCurrent).commit();
			
		} else {
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			Intent detailIntent = new Intent(this, ItemDetailActivity.class);
			startActivity(detailIntent);
			

		}
	}	
	
	
	public static void notifyAdapters(){
		if (mDevicesFragment!=null) mDevicesFragment.notifyAdapter();
	}
	
	//Send a fragment change request to the parent
	public static void requestOpenFile(final String path){
		ItemListFragment.performClick(1);
		
		//Handler will avoid crash
		Handler handler = new Handler();
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				
				mViewerFragment.openFile(path);
					
				}	
			});

	}
}
