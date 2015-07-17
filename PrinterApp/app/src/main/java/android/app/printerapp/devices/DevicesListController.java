package android.app.printerapp.devices;

import android.app.printerapp.Log;
import android.app.printerapp.MainActivity;
import android.app.printerapp.R;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.model.ModelPrinter;
import android.app.printerapp.octoprint.OctoprintFiles;
import android.app.printerapp.octoprint.StateUtils;
import android.app.printerapp.viewer.SlicingHandler;
import android.app.printerapp.viewer.ViewerMainFragment;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.util.ArrayList;


/**
 * This class will handle list events such as add, remove or update
 *
 * @author alberto-baeza
 */
public class DevicesListController {

    //max items in the grid
    public static final int GRID_MAX_ITEMS = 15;

    //List for the printers found
    private static ArrayList<ModelPrinter> mList = new ArrayList<ModelPrinter>();

    //Add element to the list
    public static void addToList(ModelPrinter m) {
        mList.add(m);

        Log.i("Extra", "Added " + m.getId());

    }

    //Return the list
    public static ArrayList<ModelPrinter> getList() {

        return mList;
    }

    //Return a specific printer
    /*public static ModelPrinter getPrinter(String name) {
		
		for (ModelPrinter p : mList){
			
			if (p.getName().equals(name)){
				return p;
			}
		}
		
		return null;
	}*/

    //Return a specific printer by id
    public static ModelPrinter getPrinter(long id) {

        //If it's a linked printer, retrieve id
        for (ModelPrinter p : mList) {

            if (DatabaseController.checkExisting(p)) {

                if (p.getId() == id) {
                    return p;
                }
            }

        }

        return null;
    }

    //Return a specific printer by position
    public static ModelPrinter getPrinterByPosition(int pos) {
        //Else just retrieve position since it's a new printer
        for (ModelPrinter p : mList) {

            if (p.getPosition() == pos) {
                return p;
            }
        }

        return null;
    }

    //Load device list from the Database
    public static void loadList(final Context context) {

        mList.clear();

        Cursor c = DatabaseController.retrieveDeviceList();

        c.moveToFirst();

        while (!c.isAfterLast()) {

            Log.i("OUT", "Entry: " + c.getString(1) + ";" + c.getString(2) + ";" + c.getString(3));

            final ModelPrinter m = new ModelPrinter(c.getString(1), c.getString(2), Integer.parseInt(c.getString(5)));

            if (Integer.parseInt(c.getString(5)) == StateUtils.TYPE_CUSTOM) {

                m.setType(StateUtils.TYPE_CUSTOM, c.getString(6));
            }

            m.setId(c.getInt(0));

            //Custom name
            m.setDisplayName(c.getString(4));

            m.setPosition(Integer.parseInt(c.getString(3)));
            m.setNetwork(c.getString(7));

            addToList(m);

            if (DatabaseController.isPreference(DatabaseController.TAG_REFERENCES, m.getName())) {

                m.setJobPath(DatabaseController.getPreference(DatabaseController.TAG_REFERENCES, m.getName()));

            }


            //Timeout for reconnection
            Handler handler = new Handler();
            handler.post(new Runnable() {

                @Override
                public void run() {

                    m.startUpdate(context);
                }
            });
            c.moveToNext();
        }

        DatabaseController.closeDb();




    }

    //Search first available position by listing the printers
    //TODO HARDCODED MAXIMUM CELLS
    public static int searchAvailablePosition() {

        boolean[] mFree = new boolean[GRID_MAX_ITEMS];

        for (int i = 0; i < GRID_MAX_ITEMS; i++) {
            mFree[i] = false;

        }

        for (ModelPrinter p : mList) {

            if (p.getPosition() != -1) mFree[p.getPosition()] = true;

        }

        for (int i = 0; i < GRID_MAX_ITEMS; i++) {

            if (!mFree[i]) return i;

        }


        return -1;

    }


    public static void removeElement(int position) {

        ModelPrinter target = null;

        //search printer by position
        for (ModelPrinter mp : mList) {
            if (mp.getPosition() == position) {

                target = mp;
            }
        }

        if (target != null) {
            Log.i("OUT", "Removing " + target.getName() + " with  index " + mList.indexOf(target));
            mList.remove(mList.indexOf(target));
        }

    }

    /**
     * Create a select printer dialog to open the print panel or to upload a file with the selected
     * printer. 0 is for print panel, 1 is for upload
     *
     * @param c App context
     * @param f File to upload/open
     */
    public static void selectPrinter(Context c, File f, final SlicingHandler slicer) {

        final ArrayList<ModelPrinter> tempList = new ArrayList<ModelPrinter>();
        final File file = f;
        final Context context = c;
        String dialogTitle = context.getString(R.string.library_select_printer_title) + " " + f.getName();

        //Fill the list with operational printers
        for (ModelPrinter p : mList) {
            if (p.getStatus() == StateUtils.STATE_OPERATIONAL) {
                tempList.add(p);
            }
        }

        String[] printersList = new String[tempList.size()];
        int i = 0;

        //New array with names only for the adapter
        for (ModelPrinter p : tempList) {
            printersList[i] = p.getDisplayName();
            i++;
        }

        if (printersList.length > 1){
            //Get the dialog view
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View selectPrinterView = inflater.inflate(R.layout.dialog_select_printer, null);
            final Spinner selectPrinterSpinner = (Spinner) selectPrinterView.findViewById(R.id.select_printer_spinner);

            //Set the list of printers into the spinner
            ArrayAdapter<String> printerAdapter = new ArrayAdapter<String>(context,
                    R.layout.print_panel_spinner_item, printersList);
            printerAdapter.setDropDownViewResource(R.layout.print_panel_spinner_dropdown_item);
            selectPrinterSpinner.setAdapter(printerAdapter);

            //Show the dialog
            final MaterialDialog.Builder selectPrinterDialog = new MaterialDialog.Builder(context);
            selectPrinterDialog.title(dialogTitle)
                    .customView(selectPrinterView, true)
                    .positiveColorRes(R.color.theme_accent_1)
                    .positiveText(R.string.library_print_model)
                    .negativeColorRes(R.color.body_text_2)
                    .negativeText(R.string.cancel)
                    .autoDismiss(false)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {

                            ModelPrinter m = tempList.get(selectPrinterSpinner.getSelectedItemPosition());

                            if (slicer != null) {

                                slicer.setPrinter(m);
                                slicer.setExtras("print", true);

                                m.setLoaded(false);

                            } else {
                                OctoprintFiles.getFiles(context, m, file);
                                //OctoprintFiles.uploadFile(context, file, m);
                            }
                            MainActivity.performClick(2);
                            MainActivity.showExtraFragment(1, m.getId());

                            dialog.dismiss();

                            ViewerMainFragment.optionClean();



                        }

                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            dialog.dismiss();
                        }

                    })
                    .show();
        } else if (printersList.length == 1 ){

            ModelPrinter m = tempList.get(0);

            if (slicer != null) {

                slicer.setPrinter(m);
                slicer.setExtras("print", true);

                m.setLoaded(false);

            } else {
                OctoprintFiles.getFiles(context, m, file);
                //OctoprintFiles.uploadFile(context, file, m);
            }
            MainActivity.performClick(2);
            MainActivity.showExtraFragment(1, m.getId());

            ViewerMainFragment.optionClean();

        } else Toast.makeText(context,R.string.viewer_printer_selected,Toast.LENGTH_SHORT).show();



    }

    /**
     * check if there's already the printer listed filtered by ip
     *
     * @param ip
     * @return
     */
    public static boolean checkExisting(String ip) {

        for (ModelPrinter p : mList) {

            if (p.getAddress().equals(ip)) {

                if (p.getStatus() != StateUtils.STATE_ADHOC) {

                    Log.i("OUT", "Printer " + ip + " already added.");

                    return true;
                }


            }

        }

        return false;

    }

    /**
     * Return the first Operational printer on the list
     *
     * @return
     */
    public static ModelPrinter selectAvailablePrinter(int type, String typeName) {

        //search for operational printers

        for (ModelPrinter p : DevicesListController.getList()) {

           if (type<3) {
               if (p.getType() == type)
                   if (p.getStatus() == StateUtils.STATE_OPERATIONAL)
                       return p;
           } else {

               if (p.getProfile().equals(typeName))
                   if (p.getStatus() ==  StateUtils.STATE_OPERATIONAL)
                       return p;

           }


        }
        return null;

    }

}
