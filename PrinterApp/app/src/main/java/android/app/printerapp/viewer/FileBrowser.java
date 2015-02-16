package android.app.printerapp.viewer;

import android.app.Activity;
import android.app.Dialog;
import android.app.printerapp.R;
import android.app.printerapp.library.LibraryController;
import android.app.printerapp.library.LibraryModelCreation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * 
 * @author Marina Gonzalez
 */
public class FileBrowser extends Activity  {
	private static Context mContext;
	private static File mCurrentPath;
	private static File[] mDialogFileList;
	private static int mSelectedIndex = -1;
	private static OnFileListDialogListener mFileListListener;
	private static OnClickListener mClickListener;
	private static DialogInterface.OnKeyListener mKeyListener;

	private static String mTitle;
	private static String mExtStl;
	private static String mExtGcode;
	
	public final static int VIEWER = 0;
	public final static int LIBRARY = 1;

	/**
	 * 
	 * @return selected file name
	 */
	public String getSelectedFileName() {
		String ret = "";
		if (mSelectedIndex >= 0) {
			ret = mDialogFileList[mSelectedIndex].getName();
		}
		return ret;
	}

	public static void openFileBrowser(final Context context, int mode, String title, String extStl, String extGcode) {
		mTitle = title;
		mExtStl = extStl;
		mExtGcode = extGcode;
		mContext = context;

		setOnFileListDialogListener(context);		
		setOnClickListener(context);		
		setOnKeyListener (context);
				
		switch (mode) {
		case VIEWER: 
			setOnFileListDialogListenerToOpenFiles(context);
			break;
		case LIBRARY:
			setOnFileListDialogListener(context);
			break;
		}
		
		show(LibraryController.getParentFolder().getAbsolutePath());
	}

	/**
	 * Display the file chooser dialog
	 * 
	 * @param path
	 */
	public static void show(String path) {
		try {
			mCurrentPath = new File(path);
			mDialogFileList = new File(path).listFiles();
			if (mDialogFileList == null) {
				// NG
				if (mFileListListener != null) {
					mFileListListener.onClickFileList(null);
				}
			} else {
				List<String> list = new ArrayList<String>();
				List<File> fileList = new ArrayList<File>();
				// create file list
				Arrays.sort(mDialogFileList, new Comparator<File>() {

					@Override
					public int compare(File object1, File object2) {
						return object1.getName().toLowerCase(Locale.US).compareTo(object2.getName().toLowerCase(Locale.US));
					}
				});
				for (File file : mDialogFileList) {
					if (!file.canRead()) {
						continue;
					}
					String name = null;
					if (file.isDirectory()) {
						if (!file.getName().startsWith(".")) {
							name = file.getName() + File.separator;
						}
					} else {
						if (LibraryController.hasExtension(0,file.getName())) {
							name = file.getName();
						}
						
						if (LibraryController.hasExtension(1,file.getName())) {
							name = file.getName();
						}
					}

					if (name != null) {

                        //Filter by directory, stl or gcode extension
                        if ((LibraryController.hasExtension(0,name))||(LibraryController.hasExtension(1,name))
                                ||file.isDirectory()){
                            list.add(name);
                            fileList.add(file);
                        }

					}
				}

                final Dialog dialog;

                LayoutInflater li = LayoutInflater.from(mContext);
                View view = li.inflate(R.layout.dialog_list, null);

                final uk.co.androidalliance.edgeeffectoverride.ListView listView =
                        (uk.co.androidalliance.edgeeffectoverride.ListView) view.findViewById(R.id.dialog_list_listview);
                listView.setSelector(mContext.getResources().getDrawable(R.drawable.list_selector));
                TextView emptyText = (TextView) view.findViewById(R.id.dialog_list_emptyview);
                listView.setEmptyView(emptyText);

                FileBrowserAdapter fileBrowserAdapter = new FileBrowserAdapter(mContext, list, fileList);
                listView.setAdapter(fileBrowserAdapter);
                listView.setDivider(null);

                mDialogFileList = fileList.toArray(mDialogFileList);

                MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(mContext)
                        .title(mTitle)
                        .customView(view, false)
                        .negativeText(R.string.cancel)
                        .negativeColorRes(R.color.theme_accent_1)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                mFileListListener.onClickFileList(null);
                                dialog.dismiss();
                            }
                        });
                dialogBuilder.keyListener(mKeyListener);

                dialog = dialogBuilder.build();
                dialog.show();

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        // save current position
                        mSelectedIndex = position;
                        if ((mDialogFileList == null) || (mFileListListener == null)) {
                        } else {
                            File file = mDialogFileList[position];

                            if (file.isDirectory()) {
                                // is a directory: display file list-up again.
                                show(file.getAbsolutePath());
                            } else {
                                // file selected. call the event mFileListListener
                                mFileListListener.onClickFileList(file);
                            }
                        }
                        dialog.dismiss();
                    }
                });
			}
		} catch (SecurityException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void setOnKeyListener (final Context context) {
		mKeyListener = new DialogInterface.OnKeyListener() {			
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
					File fileParent = mCurrentPath.getParentFile();
					if (fileParent != null) {
						show(fileParent.getAbsolutePath());
						dialog.dismiss();
					} else {
						// Already the root directory: finish dialog.
						mFileListListener.onClickFileList(null);
						dialog.dismiss();
					}

					return true;
				}
				return false;
			}
		};	
	}

	public static void setOnFileListDialogListener(final Context context) {
		mFileListListener = new OnFileListDialogListener() {			
			@Override
			public void onClickFileList(File file) {
				LibraryModelCreation.createFolderStructure(context, file);
			}
		};
	}
	
	public static void setOnFileListDialogListenerToOpenFiles(final Context context) {
		mFileListListener = new OnFileListDialogListener() {			
			@Override
			public void onClickFileList(File file) {
				if (file!= null) ViewerMainFragment.openFileDialog(file.getPath());
			}
		};
	}
	
	public static void setOnClickListener(final Context context) {
		mClickListener = new OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// save current position
				mSelectedIndex = which;
				if ((mDialogFileList == null) || (mFileListListener == null)) {
				} else {
					File file = mDialogFileList[which];

					if (file.isDirectory()) {
						// is a directory: display file list-up again.
						show(file.getAbsolutePath());
					} else {
						// file selected. call the event mFileListListener
						mFileListListener.onClickFileList(file);
					}
				}
			}
		};
	}

	public interface OnFileListDialogListener {
		public void onClickFileList(File file);
	}

}