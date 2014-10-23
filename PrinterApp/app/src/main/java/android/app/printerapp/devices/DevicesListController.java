package android.app.printerapp.devices;

import android.app.AlertDialog;
import android.app.printerapp.ItemListActivity;
import android.app.printerapp.ItemListFragment;
import android.app.printerapp.R;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.model.ModelPrinter;
import android.app.printerapp.octoprint.OctoprintFiles;
import android.app.printerapp.octoprint.StateUtils;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;


/**
 * This class will handle list events such as add, remove or update
 * @author alberto-baeza
 *
 */
public class DevicesListController {
	
	//List for the printers found
	private static ArrayList<ModelPrinter> mList = new ArrayList<ModelPrinter>();
			
	//Add element to the list
	public static void addToList(ModelPrinter m){
		mList.add(m);
		
	}
	
	//Return the list
	public static ArrayList<ModelPrinter> getList(){
		
		return mList;
	}
	
	//Return a specific printer
	public static ModelPrinter getPrinter(String name) {
		
		for (ModelPrinter p : mList){
			
			if (p.getName().equals(name)){
				return p;
			}
		}
		
		return null;
	}
	
	//Load device list from the Database
	public static void loadList(final Context context){
		
		mList.clear();
		
		Cursor c = DatabaseController.retrieveDeviceList();
		
		c.moveToFirst();
		
		while (!c.isAfterLast()){
			
			Log.i("OUT","Entry: " + c.getString(1) + ";" + c.getString(2) + ";" + c.getString(3));
			
			final ModelPrinter m = new ModelPrinter(c.getString(1),c.getString(2) , Integer.parseInt(c.getString(3)));

            m.setId(c.getInt(0));

			//Custom name
			m.setDisplayName(c.getString(4));
			
			addToList(m);

            //Timeout for reconnection
            Handler handler = new Handler();
            handler.post(new Runnable() {

                @Override
                public void run() {

                    m.setLinked(context);
                    Log.i("OUT","Running shit");

                }
            });

			
			c.moveToNext();
		}
	   
	   DatabaseController.closeDb();
	   ItemListActivity.notifyAdapters();

	}
	
	//Search first available position by listing the printers
	//TODO HARDCODED MAXIMUM CELLS
	public static int searchAvailablePosition(){
		
		int max = 12;
		boolean[] mFree = new boolean[max];
		
		for (int i = 0; i<max; i++){
			mFree[i] = false;
		
		}
		
		for (ModelPrinter p : mList){
			
			mFree[p.getPosition()] = true;
			
		}
		
		for (int i = 0; i<max; i++){
			
			if (!mFree[i]) return i;
			
		}
		
		
		return -1;
		
	}
	
	public static boolean checkExisting(ModelPrinter m){
		
		boolean exists = false;
		
		for (ModelPrinter p : mList){
			
			if ((m.getName().equals(p.getName()))||(m.getName().contains(getNetworkId(p.getName())))){
				
				exists = true;
				
			}
			
		}
		
		return exists;
		
	}

    /**
     * Create a select printer dialog to open the print panel or to upload a file with the selected
     * printer. 0 is for print panel, 1 is for upload
     * @param c App context
     * @param f File to upload/open
     */
    public static void selectPrinter(Context c, File f){

        final ArrayList<ModelPrinter> tempList = new ArrayList<ModelPrinter>();
        final File file = f;
        final Context context = c;

        //Fill the list with operational printers
        for (ModelPrinter p : mList){

            if (p.getStatus() == StateUtils.STATE_OPERATIONAL){

                tempList.add(p);

            }

        }

        String[] nameList = new String[tempList.size()];
        int i = 0;

        //New array with names only for the adapter
        for (ModelPrinter p : tempList){
            nameList[i] = p.getDisplayName();
            i++;
        }

        AlertDialog.Builder adb = new AlertDialog.Builder(context);
        adb.setTitle(R.string.library_select_printer_title);

        adb.setSingleChoiceItems(nameList,0,new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                ModelPrinter m = tempList.get(i);

                OctoprintFiles.uploadFile(context, file, m);
                ItemListFragment.performClick(0);
                ItemListActivity.showExtraFragment(1, m.getName());


                dialogInterface.dismiss();


            }
        });

        adb.show();


    }
	
	//Select a printer from all the linked available  and send to print
/*@SuppressLint("SdCardPath")
    public static void selectPrinter(final Context context, final File f){
		
		final ArrayList<ModelPrinter> tempList = new ArrayList<ModelPrinter>();
		
		//Fill the list with operational printers
		for (ModelPrinter p : mList){
			
			if (p.getStatus() == StateUtils.STATE_OPERATIONAL){
				
				tempList.add(p);
				
			}
			
		}
		String[] nameList = new String[tempList.size()];
		
		//We'll check for checked items (heh) with a boolean array
		//TODO use this same method with printer discovery
		final boolean[] checkedItems = new boolean[nameList.length];
		
		int i = 0;
		
		//New array with names only for the adapter
		for (ModelPrinter p : tempList){		
			nameList[i] = p.getDisplayName();
			i++;
		}
		
		AlertDialog.Builder adb2 = new AlertDialog.Builder(context);
		adb2.setTitle(R.string.library_select_printer_title);

		//Show list of available printers
		adb2.setMultiChoiceItems(nameList, null, new OnMultiChoiceClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				
				checkedItems[which]	= isChecked;

			}
		});
		
		adb2.setPositiveButton(R.string.library_option_print, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
	
				//SparseBooleanArray checked = ad2.getListView().getCheckedItemPositions();;
				
				//TODO Multiprint interaction
				for (int i = 0; i<checkedItems.length ; i++){
					
					if (checkedItems[i]){
						
						ModelPrinter m = tempList.get(i);

						
						//OctoprintSlicing.retrieveProfiles(m.getAddress());
						//OctoprintSlicing.sliceCommand(context, m.getAddress(), f, "/local/");
						
						//TODO Yolo Temp
						
						if (f.getParent().equals("sd")){
							OctoprintFiles.fileCommand(context, m.getAddress(), f.getName(), "/sdcard/", false);	
							
			    			
						} else if (f.getParent().equals("witbox")){
							OctoprintFiles.fileCommand(context, m.getAddress(), f.getName(), "/local/", false);	
							
				    		
						} else {
							
							if (LibraryController.hasExtension(1, f.getName())) OctoprintFiles.uploadFile(context, f, m, false, false);
							else if (LibraryController.hasExtension(0, f.getName())) OctoprintFiles.uploadFile(context, f, m, true, false);
							
						}
						if (LibraryController.hasExtension(1, f.getName())){
							ItemListFragment.performClick(0);
							if (checkedItems.length==1) ItemListActivity.showExtraFragment(1, m.getName());
						}







					}
													
				}

			}
		});
		
		adb2.setNegativeButton(R.string.cancel, null);
		
		adb2.show();
	}*/
	
	//TODO Move elsewhere maybe
	//Get the Network id key to associate with the service name
	public static String getNetworkId(String name){
		
			
			String[] parsedString = name.split("\\(");
			
			if (parsedString.length>1){
				
			
				String parsedName = parsedString[1];
				
				String finale =  parsedName.replaceAll("[^A-Za-z0-9]", "");

			return finale;
			
		} else return "00000";
	}
	
	
	/**
	 * check if there's already the printer listed filtered by ip
	 * @param ip
	 * @return
	 */
	public static boolean checkExisting(String ip){
		
		for (ModelPrinter p : mList){
			
			if (p.getAddress().equals(ip)){
				
				Log.i("OUT","Printer " + ip + " already added.");
				
				return true;
			}
			
		}
		
		return false;
		
	}

    /**
     * Return the first Operational printer on the list
     * @return
     */
    public static ModelPrinter selectAvailablePrinter(){

        //search for operational printers

        for (ModelPrinter p : DevicesListController.getList()){

            if (p.getStatus() == StateUtils.STATE_OPERATIONAL)
                return p;

        }
        return null;

    }
		
}
