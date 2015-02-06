package android.app.printerapp.devices;

import android.app.Activity;
import android.app.Fragment;
import android.app.printerapp.MainActivity;
import android.app.printerapp.R;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.devices.discovery.DiscoveryController;
import android.app.printerapp.devices.printview.GcodeCache;
import android.app.printerapp.library.LibraryController;
import android.app.printerapp.model.ModelPrinter;
import android.app.printerapp.octoprint.OctoprintConnection;
import android.app.printerapp.octoprint.OctoprintFiles;
import android.app.printerapp.octoprint.StateUtils;
import android.app.printerapp.util.ui.AnimationHelper;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialogCompat;

import java.io.File;

/**
 * This is the fragment that will contain the Device Grid and functionality
 *
 * @author alberto-baeza
 */
    public class DevicesFragment extends Fragment {


    //Controllers and adapters
    private DevicesGridAdapter mGridAdapter;
    private ImageView mHideOption;

    //Empty constructor
    public DevicesFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {

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

        Log.i("Devices","CREATE TOTAL");

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

            Log.i("Devices","CREATE VIEW");

            //Show custom option menu
            setHasOptionsMenu(true);

            //Inflate the fragment
            rootView = inflater.inflate(R.layout.devices_layout,container, false);



            //------------------------------- View references -----------------//

            //Grid

            mGridAdapter = new DevicesGridAdapter(getActivity(),
                    R.layout.grid_item_printer, DevicesListController.getList());
            GridView gridView = (GridView) rootView.findViewById(R.id.devices_grid);
            gridView.setSelector(new ColorDrawable(getResources().getColor(R.color.transparent)));
            gridView.setOnItemClickListener(gridClickListener());
            gridView.setOnItemLongClickListener(gridLongClickListener());

            gridView.setAdapter(mGridAdapter);

            /***************************************************************/

            mHideOption = (ImageView) rootView.findViewById(R.id.hide_icon);
            hideOptionHandler();

                    //Custom service listener
            //mServiceListener = new JmdnsServiceListener(this);
            //mNetworkManager = new PrintNetworkManager(this);

        }
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.devices_menu, menu);
    }

    //Option menu
    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {

        switch (item.getItemId()) {

            case R.id.devices_add:

                new DiscoveryController(getActivity());

                return true;

            case R.id.settings:
                MainActivity.showExtraFragment(0, 0);
                return true;

            case R.id.devices_menu_reload: //Reload service discovery

                //optionReload();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }







    /****************************************** UI HANDLING *********************************/




    //TODO get rid of this
    //Notify all adapters
    public void notifyAdapter() {

        try {
            mGridAdapter.notifyDataSetChanged();

            //TODO removed for list video bugs
            //mCameraAdapter.notifyDataSetChanged();
        } catch (NullPointerException e) {
            //Random adapter crash
            e.printStackTrace();
        }

    }



    /**
     * ***************************** click listeners ********************************
     */


    //onclick listener will open the action mode
    public OnItemClickListener gridClickListener() {

        return new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {

                ModelPrinter m = null;

                //search printer by position
                for (ModelPrinter mp : DevicesListController.getList()) {
                    if (mp.getPosition() == arg2) {

                        Log.i("OUT","Lets " + mp.getName());
                        m = mp;
                    }
                }

                if (m != null) {

                    if (m.getStatus() == StateUtils.STATE_NEW) {
                        //codeDialog(m);
                    } else if (m.getStatus() == StateUtils.STATE_ADHOC) {
                        //mNetworkManager.setupNetwork(m, arg2);
                    } else {
                        //show custom dialog
                        if (m.getStatus() == StateUtils.STATE_ERROR) {
                            Toast toast = new Toast(getActivity());
                            LayoutInflater inflater = getActivity().getLayoutInflater();
                            View toastView = inflater.inflate(R.layout.toast_layout, null);
                            TextView tv = (TextView) toastView.findViewById(R.id.toast_text);
                            tv.setText(m.getMessage());
                            toast.setGravity(Gravity.FILL_HORIZONTAL | Gravity.TOP, 0, 50);
                            toast.setDuration(Toast.LENGTH_SHORT);
                            toast.setView(toastView);
                            toast.show();
                        }

                        //Check if the Job has finished, and create a dialog to remove the file / send a new one
                        if ((m.getStatus() > 0) && (m.getStatus()<=7)) {

                            //if job finished, create dialog
                            if (m.getJob().getFinished()) {
                                createFinishDialog(m);

                                //if not finished, normal behavior
                            } else {
                                MainActivity.showExtraFragment(1, m.getId());
                            }
                        } else {

                            OctoprintConnection.getNewConnection(getActivity(), m);


                        }
                    }
                }
            }
        };

    }

    //onlongclick will start the draggable printer handler
    public OnItemLongClickListener gridLongClickListener() {
        return new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int arg2, long arg3) {

                ModelPrinter m = null;

                createFloatingIcon();

                for (ModelPrinter mp : DevicesListController.getList()) {
                    if (mp.getPosition() == arg2) m = mp;
                }

                if (m != null) {

                    ClipData data = null;

                    if ((m.getStatus() == StateUtils.STATE_ADHOC) || (m.getStatus() == StateUtils.STATE_NEW)){

                        //Calculate a negative number to differentiate between position search and id search
                        //Must be always < 0 since it's a valid position
                        data = ClipData.newPlainText("printer", "" + (((-1) * m.getPosition()) - 1));

                    }else {
                        data = ClipData.newPlainText("printer", "" + m.getId());

                    }



                    DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(arg1);
                    arg1.startDrag(data, shadowBuilder, arg1, 0);



                }


                return false;
            }
        };
    }

    /********************************************************************
     *          HIDE PRINTER OPTION
     ********************************************************************/

    public void createFloatingIcon(){
            mHideOption.setVisibility(View.VISIBLE);
           AnimationHelper.slideToLeft(mHideOption);
    }

    //Method to create and handle the hide option icon
    public void hideOptionHandler(){

        mHideOption.setVisibility(View.GONE);
        mHideOption.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent event) {

                //Get the drop event
                int action = event.getAction();
                switch (action) {

                    case DragEvent.ACTION_DRAG_ENTERED:

                        //Highlight on hover
                        view.setBackgroundColor(getActivity().getResources().getColor(android.R.color.holo_orange_light));

                        break;

                    //If it's a drop
                    case DragEvent.ACTION_DROP:

                        CharSequence tag = event.getClipDescription().getLabel();


                        //If it's a file (avoid draggable printers)
                        if (tag.equals("printer")) {

                            ClipData.Item item = event.getClipData().getItemAt(0);

                            int id = Integer.parseInt(item.getText().toString());
                            //Find a printer from it's name
                            ModelPrinter p = null;

                            if (id>=0){

                                p = DevicesListController.getPrinter(id);

                            } else {

                                p = DevicesListController.getPrinterByPosition(-(id + 1));

                            }
                            if (p!=null){



                                if ((p.getStatus() == StateUtils.STATE_ADHOC) || (p.getStatus() == StateUtils.STATE_NEW)){

                                    //DatabaseController.handlePreference(DatabaseController.TAG_BLACKLIST,p.getName() + " " + p.getAddress(),null,true);
                                    DevicesListController.removeElement(p.getPosition());


                                } else {

                                    DatabaseController.deleteFromDb(p.getId());
                                    DevicesListController.getList().remove(p); p.setPosition(-1);

                                }
                                notifyAdapter();

                                //SEND NOTIFICATION
                                Intent intent = new Intent("notify");
                                intent.putExtra("message", "Settings");
                                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                            }

                        }

                        //Highlight on hover
                        view.setBackgroundColor(getActivity().getResources().getColor(android.R.color.transparent));

                        break;

                    case DragEvent.ACTION_DRAG_EXITED:

                        //Highlight on hover
                        view.setBackgroundColor(getActivity().getResources().getColor(android.R.color.transparent));

                        break;

                    case DragEvent.ACTION_DRAG_ENDED:
                        mHideOption.setVisibility(View.GONE);
                        break;
                }

                return true;
            }
        });

    }

    @Override
    public void onDestroyView() {

        //TODO random crash
        //mNetworkManager.destroy();
        super.onDestroyView();
    }

    /**
     * *****************************************
     * FINISH DIALOG
     * ******************************************
     */

    private static final String STRING_TEMP = "/_tmp";

    public void createFinishDialog(final ModelPrinter m){

        //Constructor
        MaterialDialogCompat.Builder adb = new MaterialDialogCompat.Builder(getActivity());
        adb.setTitle(getActivity().getString(R.string.finish_dialog_title) + " " + m.getJob().getFilename());

        //Inflate the view
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.dialog_finish_printing, null, false);

        adb.setView(v);
        adb.setPositiveButton(R.string.confirm, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if (m.getJobPath()!=null){
                    File file = new File(m.getJobPath());

                    if (file.getParentFile().getAbsolutePath().contains(STRING_TEMP)) {

                        Log.i("FinishDialog","File: " + file.getAbsolutePath() + " needs to be saved. Hello: " + DatabaseController.getPreference(DatabaseController.TAG_REFERENCES, m.getName()));
                        createFinishDialogSave(m,file);



                    } else {
                        Log.i("FinishDialog","File: " + file.getAbsolutePath() + " needs NO SAVING CUZ ITS MINE.");
                        OctoprintFiles.fileCommand(getActivity(), m.getAddress(), m.getJob().getFilename(), "/local/", true, false);
                    }

                    GcodeCache.removeGcodeFromCache(m.getJobPath());
                    m.setJobPath(null);
                    DatabaseController.handlePreference(DatabaseController.TAG_REFERENCES,m.getName(),null,false);
                }
                else {

                    Log.i("FinishDialog","No jobpath");



                }

            }
        });

        adb.show();

    }

    public void createFinishDialogSave(final ModelPrinter m, final File file) {

        //Constructor
        MaterialDialogCompat.Builder adb = new MaterialDialogCompat.Builder(getActivity());
        adb.setTitle(m.getDisplayName() + " (100%) - " +file.getName());

        //Inflate the view
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.print_finished_dialog, null, false);

        final CheckBox cb_server = (CheckBox) v.findViewById(R.id.checkbox_keep_server);
        final CheckBox cb_local = (CheckBox) v.findViewById(R.id.checkbox_keep_local);
        final EditText et_name = (EditText) v.findViewById(R.id.et_name_model);

        et_name.setText(file.getName());

        adb.setView(v);

        adb.setPositiveButton(R.string.ok, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (cb_server.isChecked()){

                    //Select the same file again to reset progress
                    OctoprintFiles.fileCommand(getActivity(), m.getAddress(), m.getJob().getFilename(), "/local/", false, false);

                } else {

                    //Remove file from server
                    OctoprintFiles.fileCommand(getActivity(), m.getAddress(), m.getJob().getFilename(), "/local/", true, false);


                }

                if (cb_local.isChecked()){

                    File to = new File(file.getParentFile().getParentFile().getAbsolutePath() + "/_gcode/" + et_name.getText().toString());
                    file.renameTo(to);


                } else {

                    try{
                        //Delete file locally
                        if (file.delete()){

                            Log.i("OUT","File deleted!");

                        }

                    } catch (NullPointerException e){

                        Log.i("OUT","Error deleting the file");

                    }



                }

                LibraryController.deleteFiles(file.getParentFile());


            }

        });

        adb.setNegativeButton(R.string.cancel, null);

        adb.show();
    }



}
