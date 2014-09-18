package android.app.printerapp;

import android.app.printerapp.devices.DevicesFragment;
import android.app.printerapp.devices.DevicesListController;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.devices.printview.PrintViewFragment;
import android.app.printerapp.library.LibraryFragment;
import android.app.printerapp.library.StorageController;
import android.app.printerapp.library.detail.DetailViewFragment;
import android.app.printerapp.settings.SettingsFragment;
import android.app.printerapp.viewer.ViewerMain;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;

/**
 * Main class what will hold the Fragments and make the transition between them
 * TODO: Since a status hold was required, fragments aren't replaced but hidden
 * and shown when requested.
 * 
 * TODO: Fragments will be made static to access to some methods such as refreshing
 * or loading views.
 */
public class ItemListActivity extends FragmentActivity implements
		ItemListFragment.Callbacks {

	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	private boolean mTwoPane;
	
	//List of Fragments
	private static DevicesFragment mDevicesFragment; //Devices fragment @static for refresh
	private LibraryFragment mLibraryFragment; //Storage fragment
	private static ViewerMain mViewerFragment; //Print panel fragment @static for model load
	private SettingsFragment mSettingsFragment; //Settings fragment
	
	//Class specific variables
	private static Fragment mCurrent; //The current shown fragment @static
	private static FragmentManager mManager; //Fragment manager to handle transitions @static
	private static DialogController mDialog; //Dialog controller @static
	
	//Drawer handling
	private DrawerLayout mDrawer;
	private ActionBarDrawerToggle mDrawerToggle;

	
	
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
			
			
			/***************************************************************
			 *  Drawer declaration
			 ***************************************************************/
			
			mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
			mDrawer.setScrimColor(Color.TRANSPARENT);
			
			mDrawerToggle = new ActionBarDrawerToggle(
		                this,                  /* host Activity */
		                mDrawer,         /* DrawerLayout object */
		                android.R.drawable.ic_menu_more,  /* nav drawer icon to replace 'Up' caret */
		                R.string.add,  /* "open drawer" description */
		                R.string.cancel  /* "close drawer" description */
		                ) {

		            /** Called when a drawer has settled in a completely closed state. */
		            public void onDrawerClosed(View view) {
		                super.onDrawerClosed(view);
		                
		                //NYI
		            }

		            /** Called when a drawer has settled in a completely open state. */
		            public void onDrawerOpened(View drawerView) {
		                super.onDrawerOpened(drawerView);
		                
		                //NYI
		                
		            }
		        };
			
			
			// Set the drawer toggle as the DrawerListener
	        mDrawer.setDrawerListener(mDrawerToggle);
	        mDrawer.openDrawer(Gravity.START);
	        
	        getActionBar().setDisplayHomeAsUpEnabled(true);
	        getActionBar().setHomeButtonEnabled(true);
		}

		
		//Initialize db and lists
		new DatabaseController(this);
		DevicesListController.loadList(this);
		new StorageController();
		
		//Initialize variables
		mManager = getSupportFragmentManager();
		mDialog = new DialogController(this);
		
		//Initialize fragments
		mDevicesFragment = (DevicesFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.fragment_devices));
		mLibraryFragment = (LibraryFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.fragment_models));
		mViewerFragment = (ViewerMain) getSupportFragmentManager().findFragmentByTag(getString(R.string.fragment_print));
		mSettingsFragment = (SettingsFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.fragment_settings));
	}
	
	
	//handle action bar menu open
	 @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	    // Pass the event to ActionBarDrawerToggle, if it returns
	    // true, then it has handled the app icon touch event
	    if (mDrawerToggle.onOptionsItemSelected(item)) {
	      return true;
	    }
	    // Handle your other action bar items...
	
	    return super.onOptionsItemSelected(item);
    }

	/**
	 * Callback method from {@link ItemListFragment.Callbacks} indicating that
	 * the item with the given ID was selected.
	 */
	@Override
	public void onItemSelected(String id) {
		/**
		 * Marina
		 * This is necessary to close the ActionMode edition bar when changing between fragments
		 */
		if (Integer.valueOf(id)!=2) ViewerMain.hideActionModeBar();
		
			
		if (mTwoPane) {
			
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			
			
			//start transaction
			FragmentTransaction mTransaction = mManager.beginTransaction();
			
			//Pop backstack to avoid having bad references when coming from a Detail view				
			mManager.popBackStack();
			
			//If there is a fragment being shown, hide it to show the new one
			if (mCurrent!=null) {
				
				try { 
					
					mTransaction.hide(mCurrent);
						
				} catch (NullPointerException e){
					
					e.printStackTrace();
				}
				
			}
			
	
			//Select fragment			
			switch (Integer.valueOf(id)){
			
				case 1:{
					
					//Check if we already created the Fragment to avoid having multiple instances
					 if (getSupportFragmentManager().findFragmentByTag(getString(R.string.fragment_devices))==null){
						 mDevicesFragment = new DevicesFragment();
						 mTransaction.add(R.id.item_detail_container,mDevicesFragment, getString(R.string.fragment_devices));	

					 }  
					  
					 mCurrent = mDevicesFragment;
					} break;
					
				case 2:{
					
					//Check if we already created the Fragment to avoid having multiple instances
					 if (getSupportFragmentManager().findFragmentByTag(getString(R.string.fragment_print))==null){
						 mViewerFragment = new ViewerMain();
						 mTransaction.add(R.id.item_detail_container,mViewerFragment, getString(R.string.fragment_print));
							
					 } 

					 mCurrent = mViewerFragment;
						 
					
				} break;
				
				case 3:{
					//Check if we already created the Fragment to avoid having multiple instances
					 if (getSupportFragmentManager().findFragmentByTag(getString(R.string.fragment_models))==null){
						 mLibraryFragment = new LibraryFragment();
						 mTransaction.add(R.id.item_detail_container,mLibraryFragment, getString(R.string.fragment_models));
						 
					 } 
						 
					mCurrent = mLibraryFragment;

				} break;
				
				case 4:{
					
					
				} break;
				
				case 5:{
					//Check if we already created the Fragment to avoid having multiple instances
					 if (getSupportFragmentManager().findFragmentByTag(getString(R.string.fragment_settings))==null){
						 mSettingsFragment = new SettingsFragment();
						 mTransaction.add(R.id.item_detail_container,mSettingsFragment, getString(R.string.fragment_settings));

					 } 
					 
					mCurrent = mSettingsFragment;

				}break;	
				
			}
			
			//TODO: Set the visibility for the viewer if we're not on the Viewer
			if (mViewerFragment!=null) {
				
				if (mCurrent!=mViewerFragment){
					
					//Make the surface invisible to avoid frame overlapping
					mViewerFragment.setSurfaceVisibility(0);
				}
				else {
					 //Make the surface visible when we press
					 mViewerFragment.setSurfaceVisibility(1);
				}
				
			}
			
			//Show current fragment
			if (mCurrent!=null ){
				mTransaction.show(mCurrent).commit();
				getActionBar().setTitle(mCurrent.getTag());
			}
			
		} else {
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			//TODO: NYI
			Intent detailIntent = new Intent(this, ItemDetailActivity.class);
			startActivity(detailIntent);
			

		}
		
		//Run on a new thread because Model loading takes too much time
		Handler handler = new Handler();
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				
				//Close drawer AFTER Fragment was loaded
				mDrawer.closeDrawers();
				
			}
		});
		
		
	}	
	
	/**
	 * Method to create a new type of fragment to show special detailed views.
	 * 
	 * @param type Type of detailed view 0: DetailView 1: PrintView
	 * @param extra Extra argument to the fragment DetailView: File index, PrintView: Printer name
	 */
	public static void showExtraFragment(int type, String extra){
		
		//New transaction
		FragmentTransaction mTransaction = mManager.beginTransaction();
		
		//Add current fragment to the backstack and hide it (will show again later)
		mTransaction.addToBackStack(mCurrent.getTag());
		mTransaction.hide(mCurrent);
		
		switch (type){
		
			case 0:
				
				//New DetailView with the file as an index
				DetailViewFragment detail = new DetailViewFragment();
				Bundle args = new Bundle();
			    args.putInt("index", Integer.parseInt(extra));
			    detail.setArguments(args);
			    
			    //Transition is made by replacing instead of hiding to allow backstack navigation
				
			    //TODO: Use resource for id tag
			    
			    mTransaction.replace(R.id.item_detail_container, detail, "Detail").commit();
				break;
				
			case 1:
				
				//New detailview with the printer name as extra
				PrintViewFragment detailp = new PrintViewFragment();
				Bundle argsp = new Bundle();
			    argsp.putString("printer", extra);
			    detailp.setArguments(argsp);
			    //Transition is made by replacing instead of hiding to allow backstack navigation
				
			    //TODO: Use resource for id tag
				mTransaction.replace(R.id.item_detail_container, detailp, "Printer").commit();
				break;
			
		}	
	}
	
	/**
	 * Override to allow back navigation on the Storage fragment.
	 */
	@Override
	public void onBackPressed() {
		
		if (mCurrent == mLibraryFragment){
			
			if (!mLibraryFragment.goBack()) super.onBackPressed();
			
		} else super.onBackPressed();
		
	}
	
	
	//Show dialog
	public static void showDialog(String msg){
		mDialog.displayDialog(msg);
	}
	
	
	
	
	/*****************************
	 *  Devices Fragment handlers
	 *****************************/
	
	//TODO find a better way maybe?
	/**
	 * Static method to refresh the adapters for the fragments with 
	 * the info from the server socket.
	 */
	public static void notifyAdapters(){
		
		try{
			
			//Refresh the devices fragment with status
			if (mDevicesFragment!=null) mDevicesFragment.notifyAdapter();
			
			//Refresh printview fragment if exists
			Fragment fragment = mManager.findFragmentByTag("Printer");
			if (fragment!=null)((PrintViewFragment) fragment).refreshData();
		
		}catch (NullPointerException e){
			
			e.printStackTrace();
		}
	}
	
	
	
	/*****************************
	 *  Viewer Fragment handlers
	 *****************************/
			
	/**
	 * Send a file to the Viewer to display
	 * @param path File path
	 */
	public static void requestOpenFile(final String path){
		
		//This method will simulate a click and all its effects
		ItemListFragment.performClick(1);
		
		//Handler will avoid crash
		Handler handler = new Handler();
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				
				ViewerMain.openFile(path);
					
				}	
			});

	}

	

	
	
}
