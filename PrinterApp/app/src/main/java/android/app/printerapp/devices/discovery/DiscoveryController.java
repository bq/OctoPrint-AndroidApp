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
import android.widget.GridView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

/**
 * Created by alberto-baeza on 2/5/15.
 */
public class DiscoveryController {


    private ArrayList<ModelPrinter> mServiceList;
    private DiscoveryDevicesGridAdapter mAdapter;
    private GridView mPrintersGridView;

    private Context mContext;
    private PrintNetworkManager mNetworkManager;
    private JmdnsServiceListener mServiceListener;

    private Dialog mWaitProgressDialog;

    private ModelPrinter mFinalPrinter;

    public DiscoveryController(Context context) {

        mContext = context;
        mServiceList = new ArrayList<ModelPrinter>();

        scanDelayDialog();

    }

    private void scanDelayDialog() {

        mServiceList.clear();

        if (mNetworkManager==null) mNetworkManager = new PrintNetworkManager(DiscoveryController.this);
        else mNetworkManager.reloadNetworks();
        if (mServiceListener==null) mServiceListener = new JmdnsServiceListener(DiscoveryController.this);
        else mServiceListener.reloadListening();

        //Get progress dialog UI
        View scanDelayDialogView = LayoutInflater.from(mContext).inflate(R.layout.dialog_progress_content_vertical, null);
        ((TextView) scanDelayDialogView.findViewById(R.id.progress_dialog_text)).setText(R.string.printview_searching_networks_dialog_content);

        //Build progress dialog
        final MaterialDialog.Builder scanDelayDialogBuilder = new MaterialDialog.Builder(mContext);
        scanDelayDialogBuilder.title(R.string.printview_searching_networks_dialog_title)
                .customView(scanDelayDialogView, true)
                .cancelable(false)
                .autoDismiss(false);

        scanDelayDialogBuilder.dismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                scanNetwork();
            }
        });

        //Show dialog
        final Dialog scanDelayDialog = scanDelayDialogBuilder.build();
        scanDelayDialog.show();

        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                scanDelayDialog.dismiss();

            }
        }, 3000);

    }


    private void scanNetwork() {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View discoveryPrintersDialogView = inflater.inflate(R.layout.dialog_discovery_printers, null);

        final String[] mServiceNames = new String[mServiceList.size()];
        for (int i = 0; i < mServiceList.size(); i++) {

            ModelPrinter p = mServiceList.get(i);

            mServiceNames[i] = p.getName() + " " + p.getAddress();

        }

        MaterialDialog.Builder adb;
        final Dialog dialog;

        adb = new MaterialDialog.Builder(mContext)
                .title(R.string.printview_searching_networks_dialog_title)
                .customView(discoveryPrintersDialogView, false)
                .positiveText(R.string.retry)
                .positiveColorRes(R.color.theme_accent_1)
                .negativeText(R.string.cancel)
                .negativeColorRes(R.color.body_text_2)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        scanDelayDialog();
                    }
                });

        dialog = adb.build();

        mAdapter = new DiscoveryDevicesGridAdapter(mContext, mServiceList);

        mPrintersGridView = (GridView) discoveryPrintersDialogView.findViewById(R.id.discovery_gridview);
        mPrintersGridView.setAdapter(mAdapter);
        mPrintersGridView.setEmptyView(discoveryPrintersDialogView.findViewById(R.id.wifi_networks_noresults_container));
        mPrintersGridView.setSelector(R.drawable.selectable_rect_background_green);
        mPrintersGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                for (ModelPrinter p : mServiceList) {

                    Log.i("Discovery", "Searching " + p.getName());


                    if (mServiceNames[i].equals(p.getName() + " " + p.getAddress())) {

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


    public void waitServiceDialog() {

        //Get progress dialog UI
        View waitingForServiceDialogView = LayoutInflater.from(mContext).inflate(R.layout.dialog_progress_content_horizontal, null);
        ((TextView) waitingForServiceDialogView.findViewById(R.id.progress_dialog_text)).setText(R.string.devices_configure_waiting);

        //Show progress dialog
        final MaterialDialog.Builder configurePrinterDialogBuilder = new MaterialDialog.Builder(mContext);
        configurePrinterDialogBuilder.title(R.string.devices_configure_wifi_title)
                .customView(waitingForServiceDialogView, true)
                .cancelable(false)
                .autoDismiss(false);

        //Progress dialog to notify command events
        mWaitProgressDialog = configurePrinterDialogBuilder.build();
        mWaitProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (mFinalPrinter != null) {

                    OctoprintConnection.getNewConnection(mContext, mFinalPrinter);
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

                if (mWaitProgressDialog != null) {
                    mWaitProgressDialog.dismiss();
                    errorDialog();
                }

            }
        }, 30000);

    }

    private void errorDialog() {

        new MaterialDialog.Builder(mContext)
                .title(R.string.error)
                .content(R.string.devices_configure_wifi_error)
                .positiveText(R.string.retry)
                .positiveColorRes(R.color.theme_accent_1)
                .negativeText(R.string.cancel)
                .negativeColorRes(R.color.body_text_2)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        scanDelayDialog();
                    }
                })
                .show();

    }

    public boolean checkExisting(ModelPrinter m) {

        boolean exists = false;

        for (ModelPrinter p : mServiceList) {

            if ((m.getName().equals(p.getName())) || (m.getName().contains(PrintNetworkManager.getNetworkId(p.getName())))) {

                exists = true;

            }

        }

        return exists;

    }

    public void addElement(ModelPrinter printer) {

        if (mWaitProgressDialog != null) {


            if (printer.getStatus() == StateUtils.STATE_NEW)
                if (mNetworkManager.checkNetworkId(printer.getName(), true)) {

                    mServiceList.add(printer);

                    DevicesListController.addToList(printer);

                    mServiceListener.unregister();

                    mFinalPrinter = printer;

                    mWaitProgressDialog.dismiss();

                }


        } else {


            if (!DevicesListController.checkExisting(printer.getAddress()))
                if (!checkExisting(printer)) {

                    mServiceList.add(printer);
                    if (mAdapter != null)
                        mAdapter.notifyDataSetChanged();
                }
        }

    }


    public Context getActivity() {

        return mContext;
    }
}
