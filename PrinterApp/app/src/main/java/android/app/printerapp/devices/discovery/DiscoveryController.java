package android.app.printerapp.devices.discovery;

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

    public DiscoveryController(Context context){

        mContext = context;
        mServiceList = new ArrayList<ModelPrinter>();
        mNetworkManager = new PrintNetworkManager(DiscoveryController.this);
        new JmdnsServiceListener(DiscoveryController.this);

        scanDelayDialog();


    }

    private void scanDelayDialog(){

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
                .customView(v, false);


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

                            mNetworkManager.setupNetwork(p, i);
                        }




                    }



                }

                dialog.dismiss();


            }
        });

        dialog.show();

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

    public void addElement(ModelPrinter name){

        if (!DevicesListController.checkExisting(name.getAddress()))
            if (!checkExisting(name)){

                mServiceList.add(name);
                if (mAdapter!=null)
                    mAdapter.notifyDataSetChanged();
            }

    }
    public Context getActivity(){

        return mContext;
    }
}
