package android.app.printerapp.library;

import java.io.File;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.printerapp.ItemListActivity;
import android.app.printerapp.R;
import android.app.printerapp.devices.DevicesListController;
import android.app.printerapp.model.ModelFile;
import android.app.printerapp.model.ModelPrinter;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;

/**
 * This class will handle the click events for the library elements
 * @author alberto-baeza
 *
 */
public class StorageOnClickListener implements OnItemClickListener, OnItemLongClickListener{

	LibraryFragment mContext;
	String[] mDialogOptions;
	
	public StorageOnClickListener(LibraryFragment context){
		
		this.mContext = context;
		this.mDialogOptions = new String[]{"Print","Edit","Move", "Delete"};

	}

	//On long click we'll display the gcodes
	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		
		//Logic for getting file type
		final File f = StorageController.getFileList().get(arg2);
		
		
		//Only when it's a project
		if (f.isDirectory()){
			if (StorageController.isProject(f)){
				
				AlertDialog.Builder adb = new AlertDialog.Builder(mContext.getActivity());
				adb.setTitle("Files...");
				
				//We need the alertdialog instance to dismiss it
				final AlertDialog ad = adb.create();
				
				String path = ((ModelFile)f).getGcodeList();
					
				//TODO add popup
				if (path!=null) {
					
					
					final File[] files = (new File(path)).getParentFile().listFiles();
					
					//Create a string-only array for the adapter
					if (files!=null){
						String[] names = new String[files.length];
						
						for (int i = 0 ; i< files.length ; i++){
							
							names[i] = files[i].getName();
							Log.i("OUT","Found " + files[i].getName());
							
						}
							
						adb.setAdapter(new ArrayAdapter<String>(mContext.getActivity(),
								android.R.layout.simple_list_item_1,names), 
								new OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
		
								    File m = files[which];
		
								    //Open desired file
								    ItemListActivity.requestOpenFile(m.getAbsolutePath());
								    
								    ad.dismiss();
								}
							});
						
							
					} else Log.i("OUT","Pero si soy null primo");
				}
							
				adb.show();
				
			}
		} else {
			
			showOptionDialog(f);
			
		}
		
		
		
			return false;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		
		
		
		

		//Logic for getting file type
		File f = StorageController.getFileList().get(arg2);
		
		//If it's folder open it
		if (f.isDirectory()){
			
			//If it's project folder, send stl
			if (StorageController.isProject(f)){
				
				String path = ((ModelFile)f).getStl();
				
				//TODO add popup
				if (path!=null)	ItemListActivity.requestOpenFile(path);
				
			} else  {							

				StorageController.reloadFiles(f.getAbsolutePath());

				//if it's not the parent folder, make a back folder
				if (!f.getAbsolutePath().equals(StorageController.getParentFolder().toString())) {

					//TODO change folder names
					StorageController.addToList(new File(f.getParentFile().toString()));
				}
				
				
				mContext.sortAdapter();
				
				
			}							
				
			//If it's not a folder, just send the file
		}else {
			
			ItemListActivity.requestOpenFile(f.getAbsolutePath());
			
		}					
	}
	
	private void showOptionDialog(final File f){
		
		AlertDialog.Builder adb = new AlertDialog.Builder(mContext.getActivity());	
	//TODO title
		adb.setTitle(R.string.library_option_dialog_title);
		
		final AlertDialog ad = adb.create();
		
		adb.setAdapter(new ArrayAdapter<String>(mContext.getActivity(), android.R.layout.simple_list_item_1, mDialogOptions), 
				new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				switch (which){
				
				case 0:
					
					
					ArrayList<ModelPrinter> tempList = DevicesListController.getList();
					String[] nameList = new String[tempList.size()];
					
					int i = 0;
					
					for (ModelPrinter p : tempList){		
						nameList[i] = p.getName();
						i++;
					}
					
					AlertDialog.Builder adb2 = new AlertDialog.Builder(mContext.getActivity());
					adb2.setTitle(R.string.library_select_printer_title);
					
					
					adb2.setMultiChoiceItems(nameList, null, null);
					
					/*adb2.setAdapter(new ArrayAdapter<String>(mContext.getActivity(), 
							android.R.layout.simple_list_item_multiple_choice, nameList), null);
					*/
					adb2.setPositiveButton(R.string.library_option_print, new OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							
							
							
						}
					});
					
					adb2.setNegativeButton(R.string.cancel, null);
					
					adb2.show();
					
					break;
				case 1:
					
					//TODO Doesn't work when empty gcodes comeon
					ad.dismiss();
					ItemListActivity.requestOpenFile(f.getAbsolutePath());
					break;
				
				}
				
				//ad.dismiss();
				
			}
		});
		
		adb.show();
		
		
	}
		

}
