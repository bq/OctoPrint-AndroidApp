package android.app.printerapp.devices;

import android.app.Activity;
import android.app.Fragment;
import android.app.printerapp.MainActivity;
import android.app.printerapp.R;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.devices.discovery.DiscoveryController;
import android.app.printerapp.model.ModelPrinter;
import android.app.printerapp.octoprint.OctoprintConnection;
import android.app.printerapp.octoprint.StateUtils;
import android.app.printerapp.util.ui.AnimationHelper;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
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
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
                new DiscoveryController(getActivity()).scanDelayDialog();
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
                                new FinishDialog(getActivity(),m);

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

                //createFloatingIcon();

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
                                    MainActivity.refreshDevicesCount();
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







}
