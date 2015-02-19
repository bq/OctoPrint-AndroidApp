package android.app.printerapp.library.detail;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.printerapp.R;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.library.LibraryController;
import android.app.printerapp.model.ModelFile;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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

            //Inflate the fragment
            rootView = inflater.inflate(R.layout.library_model_detail_right_panel,
                    container, false);

            mFile = (ModelFile) LibraryController.getFileList().get(args.getInt("index"));

            //Share button
            ImageButton shareButton = (ImageButton) rootView.findViewById(R.id.detail_share_button);
            shareButton.setColorFilter(getResources().getColor(R.color.body_text_2), PorterDuff.Mode.MULTIPLY);
            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //Sharing intent
                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    String shareBody = getString(R.string.share_content_text);
                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.share_subject_text));
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                    sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(mFile.getStl())));
                    startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_via_text)));


                }
            });

            //Favorite button
            final ImageButton favButton = (ImageButton) rootView.findViewById(R.id.detail_fav_button);
            if (DatabaseController.isPreference(DatabaseController.TAG_FAVORITES, mFile.getName()))
                favButton.setImageResource(R.drawable.ic_action_star);
            else favButton.setImageResource(R.drawable.ic_action_star_outline);
            favButton.setColorFilter(getResources().getColor(R.color.body_text_2), PorterDuff.Mode.MULTIPLY);
            favButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addToFavorite(favButton);
                }
            });

            //Close button
            ImageButton closeButton = (ImageButton) rootView.findViewById(R.id.detail_close_button);
            closeButton.setColorFilter(getResources().getColor(R.color.body_text_2), PorterDuff.Mode.MULTIPLY);
            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeRightPanel();
                }
            });

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

            //Set header and footer of the listview to allow all the view to scrolling
            View lheader = View.inflate(getActivity(), R.layout.detailview_list_header, null);
            ImageView iv = (ImageView) lheader.findViewById(R.id.detail_iv_preview);
            iv.setImageDrawable(mFile.getSnapshot());
            lv.addHeaderView(lheader);

            //FIXME Uncomment this in the final version
//            View lfooter = View.inflate(getActivity(), R.layout.detailview_list_footer, null);
//            TextView footertv = (TextView) lfooter.findViewById(R.id.detail_tv_description);
//            footertv.setText(getResources().getString(R.string.lorem_ipsum));
//            lv.addFooterView(lfooter);

            lv.setAdapter(adapter);
        }

        return rootView;
    }

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        super.onCreateOptionsMenu(menu, inflater);
//
//        inflater.inflate(R.menu.detailview_menu, menu);
//
//        if (DatabaseController.isPreference(DatabaseController.TAG_FAVORITES, mFile.getName())) {
//            menu.findItem(R.id.menu_favorite).setIcon(R.drawable.ic_action_star);
//        } else menu.findItem(R.id.menu_favorite).setIcon(R.drawable.ic_action_star_outline);
//
//    }

    private void addToFavorite(final ImageButton button) {
        if (DatabaseController.isPreference(DatabaseController.TAG_FAVORITES, mFile.getName())) {
            DatabaseController.handlePreference(DatabaseController.TAG_FAVORITES, mFile.getName(), null, false);
            button.setImageResource(R.drawable.ic_action_star_outline);
        } else {
            DatabaseController.handlePreference(DatabaseController.TAG_FAVORITES, mFile.getName(), mFile.getAbsolutePath(), true);
            button.setImageResource(R.drawable.ic_action_star);
        }
    }

    public void removeRightPanel() {
        FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fragment_slide_in_right, R.anim.fragment_slide_out_right);
        fragmentTransaction.remove(this).commit();
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
