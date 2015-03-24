package android.app.printerapp;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.printerapp.devices.DevicesFragment;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.devices.discovery.InitialFragment;
import android.app.printerapp.devices.printview.GcodeCache;
import android.app.printerapp.devices.printview.PrintViewFragment;
import android.app.printerapp.history.HistoryDrawerAdapter;
import android.app.printerapp.history.SwipeDismissListViewTouchListener;
import android.app.printerapp.library.LibraryController;
import android.app.printerapp.library.LibraryFragment;
import android.app.printerapp.library.detail.DetailViewFragment;
import android.app.printerapp.settings.SettingsFragment;
import android.app.printerapp.util.ui.AnimationHelper;
import android.app.printerapp.viewer.ViewerMainFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by alberto-baeza on 1/21/15.
 */
public class MainActivity extends ActionBarActivity {

    //List of Fragments
    private DevicesFragment mDevicesFragment; //Devices fragment @static for refresh
    private LibraryFragment mLibraryFragment; //Storage fragment
    private ViewerMainFragment mViewerFragment; //Print panel fragment @static for model load

    //Class specific variables
    private static Fragment mCurrent; //The current shown fragment @static
    private static FragmentManager mManager; //Fragment manager to handle transitions @static
    private static DialogController mDialog; //Dialog controller @static

    private static TabHost mTabHost;

    //Drawer
    private static DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private HistoryDrawerAdapter mDrawerAdapter;
    private static ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        /**
         * Since API level 11, thread policy has changed and now does not allow network operation to
         * be executed on UI thread (NetworkOnMainThreadException), so we have to add these lines to
         * permit it.
         */
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        mTabHost = (TabHost) findViewById(R.id.tabHost);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //Initialize variables
        mManager = getFragmentManager();
        mDialog = new DialogController(this);

        //Initialize fragments
        mDevicesFragment = (DevicesFragment) getFragmentManager().findFragmentByTag(ListContent.ID_DEVICES);
        mLibraryFragment = (LibraryFragment) getFragmentManager().findFragmentByTag(ListContent.ID_LIBRARY);
        mViewerFragment = (ViewerMainFragment) getFragmentManager().findFragmentByTag(ListContent.ID_VIEWER);


        initDrawer();

        //ItemListFragment.performClick(0);

        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(this).registerReceiver(mAdapterNotification,
                new IntentFilter("notify"));

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);

        this.registerReceiver(mLocaleChange,filter);

        mManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {

            }
        });

        //Init gcode cache
        new GcodeCache();

        //Set tab host for the view
        setTabHost();

    }

    public static void performClick(int i){

        mTabHost.setCurrentTab(i);

    }

    //Initialize history drawer
    public void initDrawer(){

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                   /* host Activity */
                mDrawerLayout,                /* DrawerLayout object */
                R.string.add,            /* "open drawer" description */
                R.string.cancel         /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {


                if (slideOffset == 1.0){
                    mDrawerAdapter.notifyDataSetChanged();
                }
                super.onDrawerSlide(drawerView, slideOffset);
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);



        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setSelector(getResources().getDrawable(R.drawable.selectable_rect_background_green));

        View drawerListEmptyView = findViewById(R.id.history_empty_view);
        mDrawerList.setEmptyView(drawerListEmptyView);

        LayoutInflater inflater = getLayoutInflater();
        mDrawerList.addHeaderView(inflater.inflate(R.layout.history_drawer_header, null));
        mDrawerAdapter = new HistoryDrawerAdapter(this, LibraryController.getHistoryList());

        mDrawerList.setAdapter(mDrawerAdapter);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                mDrawerLayout.closeDrawers();
                requestOpenFile(LibraryController.getHistoryList().get(i - 1).path);

            }
        });

        mDrawerList.setOnTouchListener(new SwipeDismissListViewTouchListener(mDrawerList, new SwipeDismissListViewTouchListener.DismissCallbacks() {

            @Override
            public boolean canDismiss(int position) {
                return true;
            }

            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                         for (int position : reverseSortedPositions) {

                                             Toast.makeText(MainActivity.this,getString(R.string.delete) + " " + LibraryController.getHistoryList().get(position - 1).model,Toast.LENGTH_SHORT).show();
                                             DatabaseController.removeFromHistory(LibraryController.getHistoryList().get(position - 1).path);
                                             mDrawerAdapter.removeItem(position - 1);


                                             }
                                     }
                             }));

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }


    public void setTabHost() {

        mTabHost.setup();

        //Models tab
        TabHost.TabSpec spec = mTabHost.newTabSpec("Library");
        spec.setIndicator(getTabIndicator(getResources().getString(R.string.fragment_models)));
        spec.setContent(R.id.maintab1);
        mTabHost.addTab(spec);

        //Print panel tab
        spec = mTabHost.newTabSpec("Panel");
        spec.setIndicator(getTabIndicator(getResources().getString(R.string.fragment_print)));
        spec.setContent(R.id.maintab2);
        mTabHost.addTab(spec);

        //Print view tab
        spec = mTabHost.newTabSpec("Printer");
        spec.setIndicator(getTabIndicator(getResources().getString(R.string.fragment_devices)));
        spec.setContent(R.id.maintab3);
        mTabHost.addTab(spec);

        if (DatabaseController.count() > 0){
            mTabHost.setCurrentTab(0);
            onItemSelected(0);
        } else {
            mTabHost.setCurrentTab(2);
            onItemSelected(2);

        }


        mTabHost.getTabWidget().setDividerDrawable(new ColorDrawable(getResources().getColor(R.color.transparent)));

        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {

                View currentView = mTabHost.getCurrentView();
                AnimationHelper.inFromRightAnimation(currentView);

                onItemSelected(mTabHost.getCurrentTab());

                //getSupportActionBar().setDisplayHomeAsUpEnabled(false);

            }
        });

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
     * Return the custom view of the tab
     *
     * @param title Title of the tab
     * @return Custom view of a tab layout
     */
    private View getTabIndicator(String title) {
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.main_activity_tab_layout, null);
        TextView tv = (TextView) view.findViewById(R.id.tab_title_textview);
        tv.setText(title);
        return view;
    }

    public void onItemSelected(int id) {

        if (id!= 1) {

            ViewerMainFragment.hideActionModePopUpWindow();
            ViewerMainFragment.hideCurrentActionPopUpWindow();
        }

        Log.i("OUT", "Pressed " + id);
        //start transaction
        FragmentTransaction fragmentTransaction = mManager.beginTransaction();


        //Pop backstack to avoid having bad references when coming from a Detail view
        mManager.popBackStack();

        //If there is a fragment being shown, hide it to show the new one
        if (mCurrent != null) {
            try {
                fragmentTransaction.hide(mCurrent);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        //Select fragment
        switch (id) {

            case 0: {
                closePrintView();
                //Check if we already created the Fragment to avoid having multiple instances
                if (getFragmentManager().findFragmentByTag(ListContent.ID_LIBRARY) == null) {
                    mLibraryFragment = new LibraryFragment();
                    fragmentTransaction.add(R.id.maintab1, mLibraryFragment, ListContent.ID_LIBRARY);
                }
                mCurrent = mLibraryFragment;
            }

            break;
            case 1: {
                closePrintView();
                closeDetailView();
                //Check if we already created the Fragment to avoid having multiple instances
                if (getFragmentManager().findFragmentByTag(ListContent.ID_VIEWER) == null) {
                    mViewerFragment = new ViewerMainFragment();
                    fragmentTransaction.add(R.id.maintab2, mViewerFragment, ListContent.ID_VIEWER);
                }
                mCurrent = mViewerFragment;
            }
            break;
            case 2: {
                closeDetailView();
                //Check if we already created the Fragment to avoid having multiple instances


                if (getFragmentManager().findFragmentByTag(ListContent.ID_DEVICES) == null) {
                    mDevicesFragment = new DevicesFragment();


                    fragmentTransaction.add(R.id.maintab3, mDevicesFragment, ListContent.ID_DEVICES);
                }

                mCurrent = mDevicesFragment;

                refreshDevicesCount();
            }
            break;
        }

        if (mViewerFragment != null) {
            if (mCurrent != mViewerFragment) {
                //Make the surface invisible to avoid frame overlapping
                mViewerFragment.setSurfaceVisibility(0);
            } else {
                //Make the surface visible when we press
                mViewerFragment.setSurfaceVisibility(1);
            }
        }

        //Show current fragment
        if (mCurrent != null) {
            Log.i("OUT", "Changing " + mCurrent.getTag());
            fragmentTransaction.show(mCurrent).commit();
            mDrawerToggle.setDrawerIndicatorEnabled(true);
        }


    }

    public static void refreshDevicesCount(){

        mCurrent.setMenuVisibility(false);

        Cursor c = DatabaseController.retrieveDeviceList();
        if (c.getCount() == 0) {

            mManager.popBackStack();
            showExtraFragment(2, 0);

        } else {

             if (c.getCount() == 1) {

                c.moveToFirst();

                Log.i("Extra", "Opening " + c.getInt(0));

                showExtraFragment(1, c.getInt(0));

                 mDrawerToggle.setDrawerIndicatorEnabled(true);
                 mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);


                } else {

                 mCurrent.setMenuVisibility(true);
                 mManager.popBackStack();
                 closePrintView();
                 closeInitialFragment();


             }
        }

        DatabaseController.closeDb();

    }

    /**
     * Method to create a new type of fragment to show special detailed views.
     *
     * @param type  Type of detailed view 0: DetailView 1: PrintView
     * @param id Extra argument to the fragment DetailView: File index, PrintView: Printer id
     */
    public static void showExtraFragment(int type, long id) {

        //New transaction
        FragmentTransaction mTransaction = mManager.beginTransaction();
        mTransaction.setCustomAnimations(0, 0 , 0, R.anim.fragment_slide_out_left);

        //Add current fragment to the backstack and hide it (will show again later)
        mTransaction.addToBackStack(mCurrent.getTag());
        mTransaction.hide(mCurrent);

        switch (type) {

            case 0:

                closePrintView();
                mManager.popBackStack();
                SettingsFragment settings = new SettingsFragment();
                mTransaction.replace(R.id.container_layout, settings, ListContent.ID_SETTINGS).commit();

                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                mDrawerToggle.setDrawerIndicatorEnabled(false);

                break;

            case 1:

                mCurrent.setMenuVisibility(false);
                //New detailview with the printer name as extra
                PrintViewFragment detailp = new PrintViewFragment();
                Bundle argsp = new Bundle();
                argsp.putLong("id", id);
                detailp.setArguments(argsp);
                mTransaction.replace(R.id.maintab3, detailp, ListContent.ID_PRINTVIEW).commit();

                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                mDrawerToggle.setDrawerIndicatorEnabled(false);

                break;

            case 2:

                InitialFragment initial = new InitialFragment();
                mTransaction.replace(R.id.maintab3, initial, ListContent.ID_INITIAL).commit();

                break;
        }


    }

    private static void closeInitialFragment(){

        Fragment fragment = mManager.findFragmentByTag(ListContent.ID_INITIAL);
        if (fragment != null) mManager.popBackStack();

    }

    private void closeSettings(){
        //Refresh printview fragment if exists
        Fragment fragment = mManager.findFragmentByTag(ListContent.ID_SETTINGS);
        if (fragment != null) refreshDevicesCount();
    }

    private static void closePrintView(){
        //Refresh printview fragment if exists
        Fragment fragment = mManager.findFragmentByTag(ListContent.ID_PRINTVIEW);
        if (fragment != null) ((PrintViewFragment) fragment).stopCameraPlayback();

        if (mCurrent!=null) mCurrent.setMenuVisibility(true);
    }

    public static void closeDetailView(){
        //Refresh printview fragment if exists
        Fragment fragment = mManager.findFragmentByTag(ListContent.ID_DETAIL);
        if (fragment != null) ((DetailViewFragment) fragment).removeRightPanel();
    }

    /**
     * Override to allow back navigation on the Storage fragment.
     */
    @Override
    public void onBackPressed() {

        //Update the actionbar to show the up carat/affordance
        //getSupportActionBar().setDisplayHomeAsUpEnabled(false);


        if (mCurrent != null) {
            Fragment fragment = mManager.findFragmentByTag(ListContent.ID_SETTINGS);
            Cursor c = DatabaseController.retrieveDeviceList();

            if ((fragment != null) || (c.getCount() > 1)){

                closePrintView();

                if (mManager.popBackStackImmediate()){

                    mDrawerToggle.setDrawerIndicatorEnabled(true);
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

                    //Basically refresh printer count if all were deleted in Settings mode
                    if (mCurrent == mDevicesFragment)
                        refreshDevicesCount();

                }else super.onBackPressed();
            } else super.onBackPressed();


        } else {
            super.onBackPressed();
        }
    }

    //Show dialog
    public static void showDialog(String msg) {
        mDialog.displayDialog(msg);
    }

    /**
     * Send a file to the Viewer to display
     *
     * @param path File path
     */
    public static void requestOpenFile(final String path) {

        //This method will simulate a click and all its effects
        performClick(1);

        //Handler will avoid crash
        Handler handler = new Handler();
        handler.post(new Runnable() {

            @Override
            public void run() {

                if (path!=null) ViewerMainFragment.openFileDialog(path);
            }
        });

    }

    public static String getCurrentNetwork(Context context){

        WifiManager mWifiManager =  (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return  mWifiManager.getConnectionInfo().getSSID();

    }

    //notify ALL adapters every time a notification is received
    private BroadcastReceiver mAdapterNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // Extract data included in the Intent
            String message = intent.getStringExtra("message");

            if (message!=null)
            if (message.equals("Devices")){

                if (mDevicesFragment!=null) mDevicesFragment.notifyAdapter();

                //Refresh printview fragment if exists
                Fragment fragment = mManager.findFragmentByTag(ListContent.ID_PRINTVIEW);
                if (fragment != null) ((PrintViewFragment) fragment).refreshData();

            } else if (message.equals("Profile")){

                if (mViewerFragment!=null) {
                    mViewerFragment.notifyAdapter();
                }

            } else if (message.equals("Files")){

                if (mLibraryFragment!=null) mLibraryFragment.refreshFiles();

            }

            /*else if (message.equals("Notification")){

                long id = intent.getLongExtra("printer", 0);

               notificationManager(id);

            }*/

        }
    };

    /*
Close app on locale change
 */
    private BroadcastReceiver mLocaleChange = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("OUT", "Exiting app");
            finish();
            System.exit(0);

        }
    };

    @Override
    protected void onDestroy() {

        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mAdapterNotification);

        super.onDestroy();
    }

    @Override
    protected void onResume() {

        NotificationReceiver.setForeground(true);

        super.onResume();

    }

    @Override
    protected void onPause() {


        NotificationReceiver.setForeground(false);

        super.onPause();

    }
}
