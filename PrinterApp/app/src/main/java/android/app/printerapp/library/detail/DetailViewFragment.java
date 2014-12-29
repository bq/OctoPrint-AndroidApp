package android.app.printerapp.library.detail;

import android.app.Fragment;
import android.app.printerapp.R;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.library.LibraryController;
import android.app.printerapp.model.ModelFile;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

/**
 * This class will create the detail view for every Project.
 * it will contain a list of the files inside the project and a set of options.
 *
 * @author alberto-baeza
 */
public class DetailViewFragment extends Fragment {

    private ModelFile mFile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Reference to View
        View rootView = null;

        //If is not new
        if (savedInstanceState == null) {

            Bundle args = getArguments();

            //Show custom option menu
            setHasOptionsMenu(true);

            //Update the actionbar to show the up carat/affordance
            ((ActionBarActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            //Inflate the fragment
            rootView = inflater.inflate(R.layout.detailview_layout,
                    container, false);


            mFile = (ModelFile) LibraryController.getFileList().get(args.getInt("index"));

            ImageView iv = (ImageView) rootView.findViewById(R.id.detail_iv_preview);
            iv.setImageDrawable(mFile.getSnapshot());

            TextView tv = (TextView) rootView.findViewById(R.id.detail_tv_name);
            tv.setText(mFile.getName());

            ArrayList<File> arrayFiles = new ArrayList<File>();

            //Create a file adapter with every gcode
            if (mFile.getGcodeList() != null) {

                File[] listFiles = new File(mFile.getGcodeList()).getParentFile().listFiles();


                for (int i = 0; i < listFiles.length; i++) {

                    arrayFiles.add(listFiles[i]);

                }
            }


            try {

                //add also de the stl
                arrayFiles.add(new File(mFile.getStl()));

            } catch (Exception e) {

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

        if (DatabaseController.isPreference(DatabaseController.TAG_FAVORITES, mFile.getName())) {
            menu.findItem(R.id.menu_favorite).setIcon(R.drawable.ic_action_star);
        } else menu.findItem(R.id.menu_favorite).setIcon(R.drawable.ic_action_star_outline);

    }

    //Option menu
    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_favorite: //Add a new printer

                if (DatabaseController.isPreference(DatabaseController.TAG_FAVORITES, mFile.getName())) {
                    DatabaseController.handlePreference(DatabaseController.TAG_FAVORITES, mFile.getName(), null, false);
                    item.setIcon(getResources().getDrawable(R.drawable.ic_action_star_outline));
                } else {
                    DatabaseController.handlePreference(DatabaseController.TAG_FAVORITES, mFile.getName(), mFile.getAbsolutePath(), true);
                    item.setIcon(getResources().getDrawable(R.drawable.ic_action_star));
                }

                return true;
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
