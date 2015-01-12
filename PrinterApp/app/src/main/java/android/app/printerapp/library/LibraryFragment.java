package android.app.printerapp.library;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.printerapp.R;
import android.app.printerapp.octoprint.HttpUtils;
import android.app.printerapp.viewer.FileBrowser;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import java.io.File;
import java.util.Comparator;

/**
 * Fragment to show the library with files on the system/remote
 *
 * @author alberto-baeza
 */
public class LibraryFragment extends Fragment {

    private LibraryAdapter mGridAdapter;
    private LibraryAdapter mListAdapter;

    private ViewSwitcher mSwitcher;

    private String mCurrentFilter = null;
    private String mCurrentTab = null;

    private File mMoveFile = null;

    private View.OnClickListener mOnNavTextViewClick;

    private View mRootView;

    public View getmRootView() {
        return mRootView;
    }

    public void setmRootView(View mRootView) {
        this.mRootView = mRootView;
    }

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
        mRootView = null;

        //If is not new
        if (savedInstanceState == null) {

            //Show custom option menu
            setHasOptionsMenu(true);

            //Inflate the fragment
            mRootView = inflater.inflate(R.layout.library_layout,
                    container, false);

            mRootView.setFocusableInTouchMode(true);
            /*mRootView.setOnKeyListener(new OnKeyListener() {

                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {

                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        Log.i("OUT", "JHSOTOAIA PUTAO");
                        return true;
                    }
                    return false;
                }
            });*/

            /**
             * CUSTOM VIEW METHODS
             */

            //References to adapters
            //TODO maybe share a gridview

            mSwitcher = (ViewSwitcher) mRootView.findViewById(R.id.view_switcher_library);

            //Initial file list
            LibraryController.reloadFiles("all");

            mGridAdapter = new LibraryAdapter(getActivity(), this, R.layout.grid_item_library, LibraryController.getFileList());
            mListAdapter = new LibraryAdapter(getActivity(), this, R.layout.list_item_library, LibraryController.getFileList());

            LibraryOnClickListener clickListener = new LibraryOnClickListener(this);

            GridView g = (GridView) mRootView.findViewById(R.id.grid_storage);
            g.setSelector(getResources().getDrawable(R.drawable.list_selector));
            g.setOnItemClickListener(clickListener);
            g.setAdapter(mGridAdapter);

            ListView l = (ListView) mRootView.findViewById(R.id.list_storage);
            View lheader = View.inflate(getActivity(), R.layout.list_header_library, null);
            l.addHeaderView(lheader);
            ImageButton backButton = (ImageButton) lheader.findViewById(R.id.go_back_icon);
            if (backButton != null)
                backButton.setColorFilter(getActivity().getResources().getColor(R.color.body_text_2),
                        PorterDuff.Mode.MULTIPLY);
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //Logic to go back on the library navigation
                    if (!LibraryController.getCurrentPath().getAbsolutePath().equals(LibraryController.getParentFolder().getAbsolutePath())) {
                        Log.i("OUT", "This is " + LibraryController.getCurrentPath());
                        LibraryController.reloadFiles(LibraryController.getCurrentPath().getParent());
                        sortAdapter();
                    }
                }
            });
            l.setSelector(getResources().getDrawable(R.drawable.list_selector));
            l.setOnItemClickListener(clickListener);
            l.setDivider(null);
            l.setAdapter(mListAdapter);

            //Set left navigation menu behavior
            ((TextView) mRootView.findViewById(R.id.library_nav_all_models)).setOnClickListener(getOnNavTextViewClickListener());
            ((TextView) mRootView.findViewById(R.id.library_nav_local_models)).setOnClickListener(getOnNavTextViewClickListener());
            ((TextView) mRootView.findViewById(R.id.library_nav_printer_models)).setOnClickListener(getOnNavTextViewClickListener());
            ((TextView) mRootView.findViewById(R.id.library_nav_fav_models)).setOnClickListener(getOnNavTextViewClickListener());

            sortAdapter();

        }
        return mRootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.library_menu, menu);

        if (mMoveFile != null) {

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

            case R.id.library_add:
                optionAddLibrary();
                return true;
//            case R.id.library_list:
//                if (mSwitcher.getCurrentView().getId() == (R.id.list_storage)) {
//                    item.setTitle(R.string.library_menu_list);
//                    item.setIcon(android.R.drawable.list_selector_background);
//                } else item.setTitle(R.string.library_menu_grid);
//                optionSwitchList();
//                return true;
            case R.id.library_create:
                optionCreateLibrary();
                return true;
            case R.id.library_paste:
                optionPaste();
                return true;
            case R.id.library_reload:
                refreshFiles();
                return true;
            case R.id.library_models:
                optionThingiverse();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

                switch (v.getId()) {
                    case R.id.library_nav_all_models:
                        mCurrentTab = "all";
                        break;
                    case R.id.library_nav_local_models:
                        mCurrentTab = "current";
                        break;
                    case R.id.library_nav_printer_models:
                        mCurrentTab = "printer";
                        break;
                    case R.id.library_nav_fav_models:
                        mCurrentTab = "favorites";
                        break;
                    default:
                        break;
                }
                refreshFiles();
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
            final LinearLayout navMenu = (LinearLayout) mRootView.findViewById(R.id.library_nav_menu);

            //Set the behavior of the nav items
            for (int i = 0; i < navMenu.getChildCount(); i++) {
                View v = navMenu.getChildAt(i);
                if (v instanceof TextView) {
                    TextView tv = (TextView) v;
                    if (tv.getId() == selectedId)
                        tv.setTextAppearance(getActivity(), R.style.SelectedNavigationMenuItem);
                    else
                        tv.setTextAppearance(getActivity(), R.style.NavigationMenuItem);
                }
            }

        }
    }

    //Reload file list with the currently selected tab
    public void refreshFiles() {

        if (mCurrentTab != null) LibraryController.reloadFiles(mCurrentTab);
        sortAdapter();
    }


    //Filter elements in the current tab from the menu option
    /*public void optionFilterLibrary() {

        //Dialog to filter
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
        adb.setTitle(R.string.filter);

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.menu_filter_library_dialog, null, false);

        final RadioGroup rg = (RadioGroup) v.findViewById(R.id.radioGroup_library);

        adb.setView(v);

        adb.setPositiveButton(R.string.filter, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch (rg.getCheckedRadioButtonId()) {

                    case R.id.lb_radio3: //remove All filters and reload the whole list
                        mCurrentFilter = null;
                        refreshFiles();
                        break;
                    case R.id.lb_radio1: //Show gcodes only
                        mCurrentFilter = "gcode";
                        break;
                    case R.id.lb_radio2: //Show stl only
                        mCurrentFilter = "stl";
                        break;
                }

                //Apply current filter
                if (mCurrentFilter != null) {
                    mGridAdapter.getFilter().filter(mCurrentFilter);
                    mListAdapter.getFilter().filter(mCurrentFilter);
                } else {
                    mGridAdapter.removeFilter();
                    mListAdapter.removeFilter();
                }

                sortAdapter();

            }
        });
        adb.setNegativeButton(R.string.cancel, null);

        adb.show();
    }*/

    //Search an item within the library applying a filter to the adapter
    public void optionSearchLibrary() {

        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
        adb.setTitle(R.string.library_search_dialog_title);

        final EditText et = new EditText(getActivity());
        adb.setView(et);

        adb.setPositiveButton(R.string.search, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                mCurrentFilter = et.getText().toString();
                mGridAdapter.getFilter().filter(mCurrentFilter);
            }
        });

        adb.setNegativeButton(R.string.cancel, null);
        adb.show();

    }

    //Add a new project using the viewer file browser
    public void optionAddLibrary() {
        //TODO fix filebrowser parameters
        FileBrowser.openFileBrowser(getActivity(), FileBrowser.LIBRARY, getString(R.string.library_menu_add), ".stl", "");
    }

    //Create a single new folder via mkdir
    public void optionCreateLibrary() {

        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
        adb.setTitle(R.string.library_create_dialog_title);

        final EditText et = new EditText(getActivity());
        adb.setView(et);
        et.setText("New");
        et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        et.setSelectAllOnFocus(true);

        adb.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = et.getText().toString();
                if (name != null) LibraryController.createFolder(name);
                notifyAdapter();
            }
        });

        adb.setNegativeButton(R.string.cancel, null);
        adb.show();

    }

    public void optionSwitchList() {
        mSwitcher.showNext();
        notifyAdapter();
    }

    public void optionPaste() {

        //Copy file to new folder
        File fileTo = new File(LibraryController.getCurrentPath() + "/" + mMoveFile.getName());

        //Delete file if success
        if (!mMoveFile.renameTo(fileTo)) {
            mMoveFile.delete();
        }

        LibraryController.reloadFiles(LibraryController.getCurrentPath().getAbsolutePath());
        sortAdapter();

        setMoveFile(null);
    }

    /**
     * Open Thingiverse in the browser
     */
    public void optionThingiverse() {

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(HttpUtils.URL_THINGIVERSE));
        startActivity(browserIntent);

    }

    //Random adapter with lots of comparisons
    @SuppressLint("DefaultLocale")
    public void sortAdapter() {

        if (mCurrentFilter != null) mGridAdapter.removeFilter();

        //Sort by absolute file (puts folders before files)
        mGridAdapter.sort(new Comparator<File>() {

            public int compare(File arg0, File arg1) {

                if (arg0.getParent().equals("printer")) return -1;

                //Must check all cases, Folders > Projects > Files
                if (arg0.isDirectory()) {

                    if (LibraryController.isProject(arg0)) {

                        if (arg1.isDirectory()) {
                            if (LibraryController.isProject(arg1))
                                return arg0.getName().toLowerCase().compareTo(arg1.getName().toLowerCase());
                            else return 1;
                        } else return -1;

                    } else {
                        if (arg1.isDirectory()) {
                            if (LibraryController.isProject(arg1)) return -1;
                            else
                                return arg0.getName().toLowerCase().compareTo(arg1.getName().toLowerCase());

                        } else return -1;
                    }
                } else {
                    if (arg1.isDirectory()) return 1;
                    else
                        return arg0.getName().toLowerCase().compareTo(arg1.getName().toLowerCase());
                }

            }
        });

        //Apply the current filter to the folder
        if (mCurrentFilter != null) mGridAdapter.getFilter().filter(mCurrentFilter);
        notifyAdapter();
    }


    public void notifyAdapter() {
        mGridAdapter.notifyDataSetChanged();
        mListAdapter.notifyDataSetChanged();
    }

    public void setMoveFile(File file) {
        mMoveFile = file;
        getActivity().invalidateOptionsMenu();
    }

}
