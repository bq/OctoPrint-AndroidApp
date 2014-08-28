package android.app.printerapp.library;

import java.io.File;

import android.app.printerapp.devices.DevicesListController;
import android.app.printerapp.model.ModelPrinter;
import android.app.printerapp.octoprint.OctoprintLoadAndPrint;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class StoragePrinterOnClickListener implements OnItemClickListener {
	
	private LibraryFragment mContext;

	
	public StoragePrinterOnClickListener(LibraryFragment context){
		
		this.mContext = context;

	}


	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		
		//Logic for getting file type
		File f = StorageController.getFileList().get(arg2);
		
		if (f.getParent().contains("printer")){
			
			StorageController.retrievePrinterFiles(f.getName());
			mContext.notifyAdapter();
			
			
		} else {
			
			ModelPrinter p = DevicesListController.getPrinter(StorageController.getCurrentPath().getName());
			
			if (p!=null) {
				
				if (f.getParent().equals("sd")){
					OctoprintLoadAndPrint.printInternalFile(p.getAddress(), f.getName(), true);
				}else OctoprintLoadAndPrint.printInternalFile(p.getAddress(), f.getName(), false);
			
			}
			else Log.i("OUT","ERROR WHILE PRINTING INTERNAL FILE");
			
			Toast.makeText(mContext.getActivity(), "Loading " + f.getName() + " in " + p.getName(), Toast.LENGTH_LONG).show();
	
		}
		
	}

}
