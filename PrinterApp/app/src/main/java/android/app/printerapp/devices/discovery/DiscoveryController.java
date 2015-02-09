package android.app.printerapp.devices.discovery;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.printerapp.R;
import android.app.printerapp.devices.DevicesListController;
import android.app.printerapp.model.ModelPrinter;
import android.app.printerapp.octoprint.OctoprintConnection;
import android.app.printerapp.octoprint.StateUtils;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

/**
 * Created by alberto-baeza on 2/5/15.
 */
public class DiscoveryController {


    private ArrayList<ModelPrinter> mServiceList;
    private ArrayAdapter<String> mAdapter;
    private ListView mListView;

    private Context mContext;
    private PrintNetworkManager mNetworkManager;
    private JmdnsServiceListener mServiceListener;

    private ProgressDialog mWaitProgressDialog;

    private ModelPrinter mFinalPrinter;

    public DiscoveryController(Context context){

        mContext = context;
        mServiceList = new ArrayList<ModelPrinter>();


        scanDelayDialog();


    }

    private void scanDelayDialog(){

        mServiceList.clear();
        if (mNetworkManager==null) mNetworkManager = new PrintNetworkManager(DiscoveryController.this);
        else mNetworkManager.reloadNetworks();
        if (mServiceListener==null) mServiceListener = new JmdnsServiceListener(DiscoveryController.this);
        else mServiceListener.reloadListening();

        final ProgressDialog pd = new ProgressDialog(mContext);
        pd.setIndeterminate(true);
        pd.setTitle("Add printer");
        pd.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                scanNetwork();
            }
        });

        pd.setCancelable(false);
        pd.show();

        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                pd.dismiss();

            }
        }, 3000);

    }



    private void scanNetwork(){

        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.discovery_list_view, null);

        final String[] mServiceNames = new String[mServiceList.size()];
        for (int i = 0; i < mServiceList.size(); i ++){

            ModelPrinter p = mServiceList.get(i);

            mServiceNames[i] = p.getName() + " " + p.getAddress();

        }

        MaterialDialog.Builder adb;
        final Dialog dialog;



        adb = new MaterialDialog.Builder(mContext)
                .title("Stuff")
                .customView(v, false)
                .positiveText("Retry")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {

                        scanDelayDialog();



                    }
                });



        dialog  = adb.build();


        mAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, android.R.id.text1,mServiceNames);
        mListView = (ListView) v.findViewById(R.id.discovery_listview);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                for (ModelPrinter p : mServiceList){

                    Log.i("Discovery", "Searching " + p.getName());

                    if (mServiceNames[i].equals(p.getName() + " " + p.getAddress() )){

                        DevicesListController.addToList(p);

                        if (p.getStatus() == StateUtils.STATE_NEW) {


                            OctoprintConnection.getNewConnection(mContext, p);


                        } else if (p.getStatus() == StateUtils.STATE_ADHOC) {

                            DevicesListController.addToList(p);
                            mNetworkManager.setupNetwork(p, p.getPosition());
                        }




                    }



                }

                dialog.dismiss();
                //mServiceListener.unregister();



            }
        });

        dialog.show();

    }


    public void waitServiceDialog(){

        mWaitProgressDialog = new ProgressDialog(mContext);
        mWaitProgressDialog.setIndeterminate(true);
        mWaitProgressDialog.setTitle("Waiting for service");
        mWaitProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
               if (mFinalPrinter!=null) {

                   OctoprintConnection.getNewConnection(mContext,mFinalPrinter);
                   mFinalPrinter = null;
                   mWaitProgressDialog = null;


               }

            }
        });
        mWaitProgressDialog.show();

        mServiceListener.reloadListening();


        Handler timeOut = new Handler();

        timeOut.postDelayed(new Runnable() {
            @Override
            public void run() {



                if (mWaitProgressDialog!=null) {

                    mWaitProgressDialog.dismiss();
                    errorDialog();

                }

            }
        }, 30000);



    }

    private void errorDialog(){

        new AlertDialog.Builder(mContext)
                .setTitle("Error")
                .setMessage("Error configuring the printer try again.")
                .setPositiveButton(R.string.confirm,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        scanDelayDialog();

                    }
                })
                .show();

    }

    public boolean checkExisting(ModelPrinter m){

        boolean exists = false;

        for (ModelPrinter p : mServiceList){

            if ((m.getName().equals(p.getName()))||(m.getName().contains(PrintNetworkManager.getNetworkId(p.getName())))){

                exists = true;

            }

        }

        return exists;

    }

    public void addElement(ModelPrinter printer){

        if (mWaitProgressDialog!=null){


            if (printer.getStatus() == StateUtils.STATE_NEW)
            if (mNetworkManager.checkNetworkId(printer.getName(), true)){

                mServiceList.add(printer);

                DevicesListController.addToList(printer);

                mServiceListener.unregister();

                mFinalPrinter = printer;


                mWaitProgressDialog.dismiss();

            }



        } else {


            if (!DevicesListController.checkExisting(printer.getAddress()))
            if (!checkExisting(printer)){

                mServiceList.add(printer);
                if (mAdapter!=null)
                    mAdapter.notifyDataSetChanged();
            }
        }

    }


    public Context getActivity(){

        return mContext;
    }
}
