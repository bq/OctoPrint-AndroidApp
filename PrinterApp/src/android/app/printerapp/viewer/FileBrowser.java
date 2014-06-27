package android.app.printerapp.viewer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.KeyEvent;
import android.view.View;
import android.app.printerapp.R;

/**
 * 
 * @author Marina Gonzalez
 */
public class FileBrowser extends Activity implements View.OnClickListener, DialogInterface.OnClickListener, DialogInterface.OnKeyListener {
	private Context mContext;
	private File mCurrentPath;
	private File[] mDialogFileList;
	private int mSelectedIndex = -1;
	private OnFileListDialogListener mListener;
	private boolean mIsDirectorySelect = false;
	private String mTitle;
	private String mExtStl;
	private String mExtGcode;

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

	/**
	 * Constructor
	 * 
	 * @param mContext
	 * @param mIsDirectorySelect
	 * @param mTitle
	 * @param extFilter
	 */
	public FileBrowser(Context mContext, boolean mIsDirectorySelect, String mTitle, String mExtStl, String mExtGcode) {
		this.mIsDirectorySelect = mIsDirectorySelect;
		this.mTitle = mTitle;
		this.mExtStl = mExtStl;
		this.mExtGcode = mExtGcode;
		this.mContext = mContext;
	}

	@Override
	public void onClick(View v) {
		// do nothing
	}


	@Override
	public void onClick(DialogInterface dialog, int which) {
		// save current position
		mSelectedIndex = which;
		if ((mDialogFileList == null) || (mListener == null)) {
		} else {
			File file = mDialogFileList[which];

			if (file.isDirectory() && !mIsDirectorySelect) {
				// is a directory: display file list-up again.
				show(file.getAbsolutePath());
			} else {
				// file selected. call the event mListener
				mListener.onClickFileList(file);
			}
		}
	}

	/**
	 * Display the file chooser dialog
	 * 
	 * @param path
	 */
	public void show(String path) {

		try {
			mCurrentPath = new File(path);
			mDialogFileList = new File(path).listFiles();
			if (mDialogFileList == null) {
				// NG
				if (mListener != null) {
					mListener.onClickFileList(null);
				}
			} else {
				List<String> list = new ArrayList<String>();
				List<File> fileList = new ArrayList<File>();
				// create file list
				Arrays.sort(mDialogFileList, new Comparator<File>() {

					@Override
					public int compare(File object1, File object2) {
						return object1.getName().toLowerCase(Locale.US).compareTo(object2.getName().toLowerCase());
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
						if (file.getName().toLowerCase(Locale.US).endsWith(mExtStl.toLowerCase()) || file.getName().toLowerCase().endsWith(mExtGcode.toLowerCase())) {
							name = file.getName();
						}
					}
					if (name != null) {
						list.add(name);
						fileList.add(file);
					}
				}

				mDialogFileList = fileList.toArray(mDialogFileList);

				// Build file chooser dialog
				Builder dialog = new AlertDialog.Builder(mContext).setTitle(mTitle).setItems(list.toArray(new String[] {}), this).setOnKeyListener(this)
									.setNeutralButton(mContext.getResources().getString(R.string.close_browser), new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// finish the dialog
						mListener.onClickFileList(null);
						dialog.dismiss();
					}
				});
				if (mCurrentPath.getParentFile() != null) {
					dialog = dialog.setPositiveButton(mContext.getResources().getString(R.string.open), new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							File fileParent = mCurrentPath.getParentFile();
							if (fileParent != null) {
								show(fileParent.getAbsolutePath());
								dialog.dismiss();
							} else {
								// Already the root directory: finish dialog.
								mListener.onClickFileList(null);
								dialog.dismiss();
							}

						}
					});
				}
				dialog.show();
			}
		} catch (SecurityException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
			File fileParent = mCurrentPath.getParentFile();
			if (fileParent != null) {
				show(fileParent.getAbsolutePath());
				dialog.dismiss();
			} else {
				// Already the root directory: finish dialog.
				mListener.onClickFileList(null);
				dialog.dismiss();
			}

			return true;
		}
		return false;
	}

	public void setOnFileListDialogListener(OnFileListDialogListener mListener) {
		this.mListener = mListener;
	}

	public interface OnFileListDialogListener {
		public void onClickFileList(File file);
	}

}
