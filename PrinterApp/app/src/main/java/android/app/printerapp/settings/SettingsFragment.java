package android.app.printerapp.settings;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.printerapp.ListContent;
import android.app.printerapp.MainActivity;
import android.app.printerapp.R;
import android.app.printerapp.devices.DevicesListController;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.model.ModelPrinter;
import android.app.printerapp.octoprint.StateUtils;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Class to manage the application and printer settings
 */
public class SettingsFragment extends Fragment {

    private View.OnClickListener mOnNavTextViewClick;

    private View mRootView;

    private static FragmentManager mManager; //Fragment manager to handle transitions @static

    public SettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Retain instance to keep the Fragment from destroying itself
        setRetainInstance(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Reference to View
        mRootView = null;

        mManager = getFragmentManager();

        //If is not new
        if (savedInstanceState == null) {

            //Show custom option menu
            setHasOptionsMenu(true);

            //Update the actionbar to show the up carat/affordance
            ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            //Inflate the fragment
            mRootView = inflater.inflate(R.layout.settings_layout, container, false);

            /*********************************************************/

            //Set left navigation menu behavior
            (mRootView.findViewById(R.id.settings_nav_general_textview)).setOnClickListener(getOnNavTextViewClickListener());
            ((ImageView) (mRootView.findViewById(R.id.settings_nav_general_icon))).setColorFilter(R.color.dark_gray, PorterDuff.Mode.MULTIPLY);
            (mRootView.findViewById(R.id.settings_nav_devices_textview)).setOnClickListener(getOnNavTextViewClickListener());
            ((ImageView) (mRootView.findViewById(R.id.settings_nav_devices_icon))).setColorFilter(R.color.dark_gray, PorterDuff.Mode.MULTIPLY);
            (mRootView.findViewById(R.id.settings_nav_about_textview)).setOnClickListener(getOnNavTextViewClickListener());
            ((ImageView) (mRootView.findViewById(R.id.settings_nav_about_icon))).setColorFilter(R.color.dark_gray, PorterDuff.Mode.MULTIPLY);

            //Set the general settings fragment as the main view
            FragmentTransaction fragmentTransaction = mManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.fragment_slide_in_top, R.anim.fragment_slide_out_down);
            SettingsGeneralFragment generalSettings = new SettingsGeneralFragment();
            fragmentTransaction.replace(R.id.settings_fragment_container, generalSettings).commit();



        }
        return mRootView;
    }

    /**
     * Listener for the navigation text views
     *
     * @return
     */
    private View.OnClickListener getOnNavTextViewClickListener() {
        if (mOnNavTextViewClick != null) return mOnNavTextViewClick;

        mOnNavTextViewClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                selectNavItem(v.getId());

                //New transaction
                FragmentTransaction fragmentTransaction = mManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.fragment_slide_in_top, R.anim.fragment_slide_out_down);

                switch (v.getId()) {
                    case R.id.settings_nav_general_textview:
                        SettingsGeneralFragment generalSettings = new SettingsGeneralFragment();
                        fragmentTransaction.replace(R.id.settings_fragment_container, generalSettings).commit();
                        break;
                    case R.id.settings_nav_devices_textview:
                        SettingsDevicesFragment devicesSettings = new SettingsDevicesFragment();
                        fragmentTransaction.replace(R.id.settings_fragment_container, devicesSettings, ListContent.ID_DEVICES_SETTINGS).commit();
                        break;
                    case R.id.settings_nav_about_textview:
                        SettingsAboutFragment aboutSettings = new SettingsAboutFragment();
                        fragmentTransaction.replace(R.id.settings_fragment_container, aboutSettings, ListContent.ID_DEVICES_SETTINGS).commit();
                        break;
                    default:
                        break;
                }
            }
        };

        return mOnNavTextViewClick;
    }

    /**
     * Set the state of the selected nav item
     *
     * @param selectedId Id of the nav item that has been pressed
     */
    public void selectNavItem(int selectedId) {

        if (mRootView != null) {
            //Get the left nav menu
            final LinearLayout navMenu = (LinearLayout) mRootView.findViewById(R.id.settings_nav_menu);

            //Set the behavior of the nav items
            for (int i = 0; i < navMenu.getChildCount(); i++) {
                View v = navMenu.getChildAt(i);
                if (v instanceof LinearLayout) {
                    for (int j = 0; j < navMenu.getChildCount(); j++) {
                        View l = ((LinearLayout) v).getChildAt(j);
                        if (l instanceof TextView) {
                            TextView tv = (TextView) l;
                            if (tv.getId() == selectedId)
                                tv.setTextAppearance(getActivity(), R.style.SelectedNavigationMenuItem);
                            else
                                tv.setTextAppearance(getActivity(), R.style.NavigationMenuItem);
                        }
                    }
                }
            }

        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.settings_menu, menu);
    }

    //Option menu
    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                getActivity().onBackPressed();
                return true;

            case R.id.settings_menu_add: //Add a new printer
                //optionAddPrinter();
                //new DiscoveryController(getActivity());
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }



    public void notifyAdapter() {
        Fragment fragment = mManager.findFragmentByTag(ListContent.ID_DEVICES_SETTINGS);
        if (fragment != null) ((SettingsDevicesFragment) fragment).notifyAdapter();
    }

}
