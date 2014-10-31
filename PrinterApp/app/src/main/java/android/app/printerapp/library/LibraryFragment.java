package android.app.printerapp.library;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.printerapp.R;
import android.app.printerapp.octoprint.HttpUtils;
import android.app.printerapp.viewer.FileBrowser;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
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
import android.widget.TabHost.OnTabChangeListener;
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
        if (savedInstanceState == null) {

            //Show custom option menu
            setHasOptionsMenu(true);

            //Inflate the fragment
            rootView = inflater.inflate(R.layout.library_layout,
                    container, false);

            rootView.setFocusableInTouchMode(true);
            rootView.setOnKeyListener(new OnKeyListener() {

                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {

                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        Log.i("OUT", "JHSOTOAIA PUTAO");
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
            LibraryController.reloadFiles("all");

            mGridAdapter = new LibraryAdapter(getActivity(), R.layout.grid_item_library, LibraryController.getFileList());
            mListAdapter = new LibraryAdapter(getActivity(), R.layout.list_item_library, LibraryController.getFileList());

            LibraryOnClickListener clickListener = new LibraryOnClickListener(this);

            GridView g = (GridView) rootView.findViewById(R.id.grid_storage);
            g.setSelector(getResources().getDrawable(R.drawable.list_selector));
            g.setOnItemClickListener(clickListener);
            g.setOnItemLongClickListener(clickListener);
            g.setAdapter(mGridAdapter);

            ListView l = (ListView) rootView.findViewById(R.id.list_storage);
            l.setSelector(getResources().getDrawable(R.drawable.list_selector));
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

            case R.id.library_filter:
                optionFilterLibrary();
                return true;

            case R.id.library_add:
                optionAddLibrary();
                return true;
            case R.id.library_list:
                if (mSwitcher.getCurrentView().getId() == (R.id.list_storage)) {
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
            case R.id.library_reload:
                refreshFiles();
                return true;
            case R.id.library_thingiverse:
                optionThingiverse();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * Constructor for the tab host
     * TODO: Should be moved to a View class since it only handles ui.
     */
    public void setTabHost(View v) {

        final TabHost tabs = (TabHost) v.findViewById(android.R.id.tabhost);
        tabs.setup();

        TabHost.TabSpec spec = tabs.newTabSpec("Models");
        spec.setIndicator(getString(R.string.library_tabhost_tab_all));
        spec.setContent(R.id.tab1);
        tabs.addTab(spec);

        spec = tabs.newTabSpec("Local");
        spec.setIndicator(getString(R.string.library_tabhost_tab_local));
        spec.setContent(R.id.tab2);
        tabs.addTab(spec);

        spec = tabs.newTabSpec("Printer");
        spec.setIndicator(getString(R.string.library_tabhost_tab_printer));
        spec.setContent(R.id.tab3);
        tabs.addTab(spec);

        spec = tabs.newTabSpec("Favorites");
        spec.setIndicator(getString(R.string.library_tabhost_tab_favorites));
        spec.setContent(R.id.tab4);
        tabs.addTab(spec);

        tabs.setCurrentTab(0);
        mCurrentTab = "all";

        //Set style for the tab widget
        for (int i = 0; i < tabs.getTabWidget().getChildCount(); i++) {
            final View tab = tabs.getTabWidget().getChildTabViewAt(i);
            tab.setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_indicator_ab_green));
            TextView tv = (TextView) tabs.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            tv.setTextColor(getResources().getColor(R.color.body_text_2));
        }

        tabs.setOnTabChangedListener(new OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {

                switch (tabs.getCurrentTab()) {
                    case 0:
                        mCurrentTab = "all";
                        break;
                    case 1:
                        mCurrentTab = "current";
                        break;
                    case 2:
                        mCurrentTab = "printer";
                        break;
                    case 3:
                        mCurrentTab = "favorites";
                        break;
                    default:
                        break;
                }

                refreshFiles();


            }
        });

    }

    //Reload file list with the currently selected tab
    public void refreshFiles() {

        if (mCurrentTab != null) LibraryController.reloadFiles(mCurrentTab);
        sortAdapter();
    }


    //Filter elements in the current tab from the menu option
    public void optionFilterLibrary() {

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
    }

    //Search an item within the library applying a filter to the adapter
    public void optionSearchLibrary() {

        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
        adb.setTitle(R.string.library_search_dialog_title);

        final EditText et = new EditText(getActivity());
        adb.setView(et);

        adb.setPositiveButton(R.string.search, new OnClickListener() {

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

        adb.setPositiveButton(R.string.create, new OnClickListener() {

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
    public void optionThingiverse(){

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

    //onBackPressed handler to the file browser
    public boolean goBack() {
        if (!LibraryController.getCurrentPath().getAbsolutePath().equals(LibraryController.getParentFolder().getAbsolutePath())) {
            Log.i("OUT", "This is " + LibraryController.getCurrentPath());
            LibraryController.reloadFiles(LibraryController.getCurrentPath().getParent());
            sortAdapter();
            return true;
        }
        return false;
    }
}
