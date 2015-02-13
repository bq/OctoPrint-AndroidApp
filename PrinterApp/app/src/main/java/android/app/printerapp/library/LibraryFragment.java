package android.app.printerapp.library;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.printerapp.MainActivity;
import android.app.printerapp.R;
import android.app.printerapp.octoprint.HttpUtils;
import android.app.printerapp.viewer.FileBrowser;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.MaterialDialogCompat;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.File;
import java.util.Comparator;

/**
 * Fragment to show the library with files on the system/remote
 *
 * @author alberto-baeza
 */
public class LibraryFragment extends Fragment {



    private LibraryAdapter mListAdapter;

    private ListView mListView;
    private View mListHeader;

    private String mCurrentFilter = null;
    private String mCurrentTab = LibraryController.TAB_ALL;

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

            //Initial file list
            LibraryController.reloadFiles(LibraryController.TAB_ALL);

            mListAdapter = new LibraryAdapter(getActivity(), this, R.layout.list_item_library, LibraryController.getFileList());

            mListHeader = (View) mRootView.findViewById(R.id.list_storage_header);
            hideListHeader();

            mListView = (ListView) mRootView.findViewById(R.id.list_storage);

            if (LibraryController.getFileList().size() == 0){

                LinearLayout emptyView = (LinearLayout) mRootView.findViewById(R.id.library_empty_view);

                mListView.setEmptyView(emptyView);
                emptyView.findViewById(R.id.obtain_models_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        optionGetModelsDialog();
                    }
                });

                emptyView.findViewById(R.id.scan_device_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        optionSearchSystem();
                    }
                });
            }




            LibraryOnClickListener clickListener = new LibraryOnClickListener(this, mListView);
            mListView.setSelector(getResources().getDrawable(R.drawable.list_selector));
            mListView.setOnItemClickListener(clickListener);
            mListView.setOnItemLongClickListener(clickListener);
            mListView.setDivider(null);
            mListView.setAdapter(mListAdapter);

            ImageButton backButton = (ImageButton) mRootView.findViewById(R.id.go_back_icon);
            if (backButton != null)
                backButton.setColorFilter(getActivity().getResources().getColor(R.color.body_text_2),
                        PorterDuff.Mode.MULTIPLY);
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //Logic to go back on the library navigation
//                    if(!LibraryController.getCurrentPath().getParent().equals(LibraryController.getParentFolder().getAbsolutePath())) {
//                        hideListHeader();
//                    }
                    if (!LibraryController.getCurrentPath().getAbsolutePath().equals(LibraryController.getParentFolder().getAbsolutePath())) {
                        Log.i("OUT", "This is " + LibraryController.getCurrentPath());
                        LibraryController.reloadFiles(LibraryController.getCurrentPath().getParent());
                        sortAdapter();

                        if (LibraryController.getCurrentPath().getAbsolutePath().equals(LibraryController.getParentFolder().getAbsolutePath() + "/Files")) {
                            hideListHeader();
                        }
                    }
                }

            });

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

        if ((mMoveFile != null) && ((mCurrentTab.equals(LibraryController.TAB_ALL))
            || (mCurrentTab.equals(LibraryController.TAB_CURRENT)))){

            menu.findItem(R.id.library_paste).setVisible(true);

        } else menu.findItem(R.id.library_paste).setVisible(false);

        if ((mCurrentTab.equals(LibraryController.TAB_FAVORITES)) || mCurrentTab.equals(LibraryController.TAB_PRINTER)){
            menu.findItem(R.id.library_create).setVisible(false);
        } else menu.findItem(R.id.library_create).setVisible(true);
    }

    //Option menu
    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {

        switch (item.getItemId()) {
            case R.id.library_search:
                optionSearchLibrary();
                return true;
            case R.id.library_add:
                //optionAddLibrary();
                optionSearchSystem();
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
            case R.id.library_models:
                optionGetModelsDialog();
                return true;
            case R.id.library_settings:
                MainActivity.showExtraFragment(0, 0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e("Tag", "Se para el fragmento");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e("Tag", "Se pausa el fragmento");
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

                LibraryController.setCurrentPath(LibraryController.getParentFolder() + "/Files");

                switch (v.getId()) {
                    case R.id.library_nav_all_models:
                        mCurrentTab = LibraryController.TAB_ALL;
                        break;
                    case R.id.library_nav_local_models:
                        mCurrentTab = LibraryController.TAB_CURRENT;
                        break;
                    case R.id.library_nav_printer_models:
                        mCurrentTab = LibraryController.TAB_PRINTER;
                        break;
                    case R.id.library_nav_fav_models:
                        mCurrentTab = LibraryController.TAB_FAVORITES;
                        break;
                    default:
                        break;
                }
                refreshFiles();
                hideListHeader();
                getActivity().invalidateOptionsMenu();
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

        Log.i("Files","Refresh for " + mCurrentTab);

        if (mCurrentTab != null) LibraryController.reloadFiles(mCurrentTab);
        sortAdapter();
    }

    //Search an item within the library applying a filter to the adapter
    public void optionSearchLibrary() {

        final EditText et = new EditText(getActivity());

        MaterialDialogCompat.Builder adb = new MaterialDialogCompat.Builder(getActivity());
        adb.setTitle(R.string.library_search_dialog_title);
        adb.setView(et);

        adb.setPositiveButton(R.string.search, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                mCurrentFilter = et.getText().toString();
                refreshFiles();
            }
        });

        adb.setNegativeButton(R.string.cancel, null);
        adb.show();

//        boolean wrapInScrollView = true;
//        new MaterialDialog.Builder(getActivity())
//                .title(R.string.library_search_dialog_title)
//                .customView(et, wrapInScrollView)
//                .positiveText(R.string.search)
//                .callback(new MaterialDialog.ButtonCallback() {
//                    @Override
//                    public void onPositive(MaterialDialog dialog) {
//                        mCurrentFilter = et.getText().toString();
//                    }
//                })
//                .negativeText(R.string.cancel)
//                .build()
//                .show();

    }

    //Search for models in filesystem
    public void optionSearchSystem() {

        new FileScanner(Environment.getExternalStorageDirectory().getAbsolutePath(), getActivity());

    }

    //Add a new project using the viewer file browser
    public void optionAddLibrary() {
        //TODO fix filebrowser parameters
        FileBrowser.openFileBrowser(getActivity(), FileBrowser.LIBRARY, getString(R.string.library_menu_add), ".stl", "");
    }

    //Create a single new folder via mkdir
    public void optionCreateLibrary() {

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View getModelsDialogView = inflater.inflate(R.layout.dialog_create_folder, null);
        final MaterialEditText nameEditText = (MaterialEditText) getModelsDialogView.findViewById(R.id.new_folder_name_edittext);

        final MaterialDialog.Builder createFolderDialog = new MaterialDialog.Builder(getActivity());
        createFolderDialog.title(R.string.library_create_dialog_title)
                .customView(getModelsDialogView, true)
                .positiveColorRes(R.color.theme_accent_1)
                .positiveText(R.string.create)
                .negativeColorRes(R.color.body_text_2)
                .negativeText(R.string.cancel)
                .autoDismiss(false)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        String name = nameEditText.getText().toString().trim();
                        if (name == null || name.equals("")) {
                            nameEditText.setError(getString(R.string.library_create_folder_name_error));
                        }
                        else {
                            LibraryController.createFolder(name);
                            refreshFiles();
                            dialog.dismiss();
                        }
                    }
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dialog.dismiss();
                    }

                })
                .show();
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

        refreshFiles();
    }

    /**
     * Show a dialog to select between Thingiverse or Yoymagine and open the selected url in the browser
     */
    public void optionGetModelsDialog() {

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View getModelsDialogView = inflater.inflate(R.layout.dialog_get_models, null);

        final MaterialDialog getModelsDialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.library_get_models_title)
                .customView(getModelsDialogView, true)
                .positiveColorRes(R.color.body_text_1)
                .positiveText(R.string.close)
                .show();

        LinearLayout thingiverseButton = (LinearLayout) getModelsDialogView.findViewById(R.id.thingiverse_button);
        thingiverseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(HttpUtils.URL_THINGIVERSE));
                startActivity(browserIntent);
                getModelsDialog.dismiss();
            }
        });

        LinearLayout youmagineButton = (LinearLayout) getModelsDialogView.findViewById(R.id.youmagine_button);
        youmagineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(HttpUtils.URL_YOUMAGINE));
                startActivity(browserIntent);
                getModelsDialog.dismiss();
            }
        });
    }

    public void showListHeader(String folderName) {
        TextView goBackTextView = (TextView) mListHeader.findViewById(R.id.model_name_column_textview);
        goBackTextView.setText(getString(R.string.library_list_go_back_tag) + " (" + folderName + ")");
        mListHeader.setVisibility(View.VISIBLE);
    }

    public void hideListHeader() {
        mListHeader.setVisibility(View.GONE);
    }

    //Random adapter with lots of comparisons
    @SuppressLint("DefaultLocale")
    public void sortAdapter() {

        if (mCurrentFilter != null) mListAdapter.removeFilter();

        //Sort by absolute file (puts folders before files)
        mListAdapter.sort(new Comparator<File>() {

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
        if (mCurrentFilter != null) mListAdapter.getFilter().filter(mCurrentFilter);
        notifyAdapter();
    }


    public void notifyAdapter() {
        mListAdapter.notifyDataSetChanged();
    }

    public void setMoveFile(File file) {
        mMoveFile = file;
        getActivity().invalidateOptionsMenu();
    }

    public String getCurrentTab(){
        return mCurrentTab;
    }

}
