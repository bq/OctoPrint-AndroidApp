package android.app.printerapp.library;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.printerapp.ItemListActivity;
import android.app.printerapp.R;
import android.app.printerapp.devices.DevicesListController;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.library.detail.DetailViewFragment;
import android.app.printerapp.model.ModelFile;
import android.app.printerapp.model.ModelPrinter;
import android.app.printerapp.octoprint.OctoprintFiles;
import android.content.DialogInterface;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.io.File;

/**
 * This class will handle the click events for the library elements
 *
 * @author alberto-baeza
 */
public class LibraryOnClickListener implements OnItemClickListener {

    LibraryFragment mContext;


    public LibraryOnClickListener(LibraryFragment context) {
        this.mContext = context;
    }

    //On long click we'll display the gcodes
    public void onOverflowButtonClick(View view, int position) {

        //Avoid to click in the header
//        position--;

        Log.d("LibraryOnClickListener", "onOverflowButtonClick");

        File f = LibraryController.getFileList().get(position);

        //If it's not IN the printer
        if ((!f.getParent().contains("printer")) &&
                (!f.getParent().contains("sd")) &&
                (!f.getParent().contains("local"))) {

            showOptionPopUp(view, position);
        }
    }

    @SuppressLint("SdCardPath")
    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

        //Avoid to click in the header
        arg2--;

        Log.d("LibraryOnClickListener", "onItemClick");

        //Logic for getting file type
        File f = LibraryController.getFileList().get(arg2);

        //If it's folder open it
        if (f.isDirectory()) {


            //If it's project folder, send stl
            if (LibraryController.isProject(f)) {
                //Show detail view as a fragment
                showRightPanel(arg2);

            } else {
                //Not a project, open folder
                LibraryController.reloadFiles(f.getAbsolutePath());
                mContext.sortAdapter();
            }

            //If it's not a folder, just send the file
        } else {

            //it's a printer file
            if (f.getParent().contains("printer")) {
                LibraryController.retrievePrinterFiles(Long.parseLong(f.getName()));
                mContext.notifyAdapter();

            } else {

                try {

                    ModelPrinter p = DevicesListController.getPrinter(Long.parseLong(LibraryController.getCurrentPath().getName()));

                    //it's a printer folder because there's a printer with the same name
                    if (p != null) {
                        //either sd or internal
                        if (f.getParent().equals("sd")) {
                            OctoprintFiles.fileCommand(mContext.getActivity(), p.getAddress(), f.getName(), "/sdcard/", false, true);
                            //OctoprintSlicing.sliceCommand(mContext.getActivity(), p.getAddress(), f, "/local/");
                        } else
                            OctoprintFiles.fileCommand(mContext.getActivity(), p.getAddress(), f.getName(), "/local/", false, true);
                        Toast.makeText(mContext.getActivity(), "Loading " + f.getName() + " in " + p.getDisplayName(), Toast.LENGTH_LONG).show();
                    } else {

                        //it's a raw file
                        if (f.getAbsoluteFile().length() > 0) {
                            //TODO select printer for raw files?
                            //DevicesListController.selectPrinter(mContext.getActivity(), f , 0);
                            ItemListActivity.requestOpenFile(f.getAbsolutePath());

                        } else {
                            Toast.makeText(mContext.getActivity(), R.string.storage_toast_corrupted, Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (NumberFormatException e) {

                    e.printStackTrace();

                }
            }
        }
    }

    private void showRightPanel(final int index) {

        FragmentTransaction fragmentTransaction = mContext.getFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fragment_slide_in_right, R.anim.fragment_slide_out_right);

        //New DetailView with the file as an index
        DetailViewFragment detail = new DetailViewFragment();
        Bundle args = new Bundle();
        args.putInt("index", index);
        detail.setArguments(args);

        fragmentTransaction.replace(R.id.right_panel_container, detail).commit();
    }

    //Show dialog for handling files
    private void showOptionPopUp(View view, final int index) {

        //Creating the instance of PopupMenu
        final PopupMenu popup = new PopupMenu(mContext.getActivity(), view);

        //Logic for getting file type
        final File f = LibraryController.getFileList().get(index);

        //Different pop ups for different type of files
        if (f.getParent().equals("sd") || f.getParent().equals("local")) {
            popup.getMenuInflater().inflate(R.menu.library_model_menu_local, popup.getMenu());
        } else {
            popup.getMenuInflater().inflate(R.menu.library_model_menu, popup.getMenu());
        }


        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.library_model_print: //Print / Multiprint
                        if (f.isDirectory()) {
                            if (LibraryController.isProject(f)) {
                                //Show detail view as a fragment
                                showRightPanel(index);
                            }
                        } else {
                            ItemListActivity.requestOpenFile(f.getAbsolutePath());

                        }
                        break;
                    case R.id.library_model_edit: //Edit
                        //TODO Doesn't work when empty gcodes comeon
                        popup.dismiss();
                        if (f.isDirectory()) {
                            if (LibraryController.isProject(f)) {

                                if (((ModelFile) f).getStl() == null) {
                                    ItemListActivity.requestOpenFile(((ModelFile) f).getGcodeList());
                                    //DevicesListController.selectPrinter(mContext.getActivity(), new File (((ModelFile)f).getGcodeList()) , 0);

                                } else {
                                    ItemListActivity.requestOpenFile(((ModelFile) f).getStl());

                                }
                            }
                        } else {
                            //Check if the gcode is empty, won't work if file is actually corrupted
                            if (f.getAbsoluteFile().length() > 0) {
                                ItemListActivity.requestOpenFile(f.getAbsolutePath());
                            } else {
                                Toast.makeText(mContext.getActivity(), R.string.storage_toast_corrupted, Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;
                    case R.id.library_model_move: //Move
                        mContext.setMoveFile(f);
                        Toast.makeText(mContext.getActivity(), R.string.library_paste_toast, Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.library_model_delete: //Delete
                        AlertDialog.Builder adb_delete = new AlertDialog.Builder(mContext.getActivity());
                        adb_delete.setTitle(R.string.library_delete_dialog_title);
                        adb_delete.setMessage(f.getName());
                        adb_delete.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                LibraryController.deleteFiles(f);
                                LibraryController.getFileList().remove(f);

                                if (DatabaseController.isPreference(DatabaseController.TAG_FAVORITES, f.getName())) {
                                    DatabaseController.handlePreference(DatabaseController.TAG_FAVORITES, f.getName(), null, false);
                                }
                                mContext.notifyAdapter();

                            }
                        });

                        adb_delete.setNegativeButton(R.string.cancel, null);
                        adb_delete.show();
                        break;
                }
                return true;
            }
        });

        popup.show();//showing popup menu

    }


}
