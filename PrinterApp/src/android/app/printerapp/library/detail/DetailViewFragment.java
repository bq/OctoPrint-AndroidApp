package android.app.printerapp.library.detail;

import java.io.File;
import java.util.ArrayList;
import android.app.printerapp.R;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.library.StorageController;
import android.app.printerapp.model.ModelFile;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**This class will create the detail view for every Project.
 * it will contain a list of the files inside the project and a set of options.
 * 
 * @author alberto-baeza
 *
 */
public class DetailViewFragment extends Fragment {
	
	private ModelFile mFile;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
			//Reference to View
			View rootView = null;
	
			//If is not new
			if (savedInstanceState==null){
				
				Bundle args = getArguments();
				
				//Show custom option menu
				setHasOptionsMenu(true);
				
				//Inflate the fragment
				rootView = inflater.inflate(R.layout.detailview_layout,
						container, false);
			
				
				mFile = (ModelFile) StorageController.getFileList().get(args.getInt("index"));
				
				ImageView iv = (ImageView) rootView.findViewById(R.id.detail_iv_preview);
				iv.setImageDrawable(mFile.getSnapshot());
								
				TextView tv = (TextView) rootView.findViewById(R.id.detail_tv_name);
				tv.setText(mFile.getName());
				
				ArrayList<File> arrayFiles = new ArrayList<File>();
				
				//Create a file adapter with every gcode
				if (mFile.getGcodeList()!=null){
					
					File[] listFiles = new File(mFile.getGcodeList()).getParentFile().listFiles();
					
					
					
					for (int i = 0; i<listFiles.length; i++){
						
						arrayFiles.add(listFiles[i]);
						
					}
				}
				
				
				
				try{
					
					//add also de the stl
					arrayFiles.add(new File(mFile.getStl()));
					
				} catch (Exception e){
					
					e.printStackTrace();
				}
				
				
				DetailViewAdapter adapter = new DetailViewAdapter(getActivity(), R.layout.detailview_list_element, arrayFiles, mFile.getSnapshot());
				ListView lv = (ListView) rootView.findViewById(R.id.detail_lv);
				lv.setAdapter(adapter);
				
				
				
				
				
				/***********************************************************************************/
				
				//TODO: COMMENTS ARE DISABLED FOR NOW
				
				/*	
				TextView tvd = (TextView) rootView.findViewById(R.id.detail_tv_description);
				ArrayList<ModelComment> commentArray = new ArrayList<ModelComment>();
				
				try {

					BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(mFile.getInfo())));
					
					tvd.setText(bufferedReader.readLine());
					
					String line;
					
					while ((line = bufferedReader.readLine()) != null){
						
						String[] comment = line.split(";");				
						commentArray.add(new ModelComment(comment[0], comment[1], comment[2]));
					}
					
					bufferedReader.close();
					
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				DetailViewCommAdapter commadapter = new DetailViewCommAdapter(getActivity(), R.layout.detailview_list_comment, commentArray);
				ListView lvc = (ListView) rootView.findViewById(R.id.detail_lv_comments);
				
				lvc.setAdapter(commadapter);
				
				TextView tvc = (TextView) rootView.findViewById(R.id.detailview_tv_num);
				tvc.setText(String.valueOf(commentArray.size()));
					*/
				
				
			}

		return rootView;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.detailview_menu, menu);
		
		if (DatabaseController.isFavorite(mFile.getName())){
			menu.findItem(R.id.menu_favorite).setIcon(android.R.drawable.btn_star_big_on);
		} else menu.findItem(R.id.menu_favorite).setIcon(android.R.drawable.btn_star_big_off);
		
	}
	
	//Option menu
   @Override
	public boolean onOptionsItemSelected(android.view.MenuItem item) {
	   
	   switch (item.getItemId()) {
	   
	   case R.id.menu_favorite: //Add a new printer
		  			
		   if (DatabaseController.isFavorite(mFile.getName())){
				DatabaseController.handleFavorite(mFile, false);
				item.setIcon(android.R.drawable.btn_star_big_off);
			} else {
				DatabaseController.handleFavorite(mFile, true);
				item.setIcon(android.R.drawable.btn_star_big_on);
			}
		  
			return true;
			
   
              
          
       default:
           return super.onOptionsItemSelected(item);
	   }
	}
   
}
