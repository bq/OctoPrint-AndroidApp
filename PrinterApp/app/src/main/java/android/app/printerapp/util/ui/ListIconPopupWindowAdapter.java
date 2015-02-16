package android.app.printerapp.util.ui;

import android.app.printerapp.Log;
import android.app.printerapp.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;


public class ListIconPopupWindowAdapter extends BaseAdapter {

	private static String TAG = "ListPopupWindowAdapter";

	private Context mContext;
	private LayoutInflater mInflater;

	private String [] mSelectableOptions;
	private String mCurrentSelectedOption;
	private TypedArray mListDrawables;
	private int [] mListDrawablesId;

	public ListIconPopupWindowAdapter(Context context, String [] selectableOptions, TypedArray listDrawables, String currentSelectedOption) {
		this.mContext = context;
		this.mSelectableOptions = selectableOptions;
		this.mListDrawables = listDrawables;
		this.mCurrentSelectedOption = currentSelectedOption;
		this.mInflater = LayoutInflater.from(mContext);
	}

	public ListIconPopupWindowAdapter(Context context, String [] selectableOptions, int [] listDrawablesId, String currentSelectedOption) {
		this.mContext = context;
		this.mSelectableOptions = selectableOptions;
		this.mListDrawablesId = listDrawablesId;
		this.mCurrentSelectedOption = currentSelectedOption;
		this.mInflater = LayoutInflater.from(mContext);
	}

	@Override
	public int getCount() {
		return mSelectableOptions.length;
	}

	@Override
	public Object getItem(int i) {
		return mSelectableOptions[i];
	}

	@Override
	public long getItemId(int i) {
		return 0;
	}  

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}


	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		ViewHolder holder;
		if (convertView == null) {
			//Create a custom view with the action icon
			convertView = mInflater.inflate(R.layout.item_list_popup_action_icon, null);
			holder = new ViewHolder();
			holder.listImageButton = (ImageView) convertView.findViewById(R.id.item_list_button);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		//Set the info of the button
		holder.listImageButton.setContentDescription(mSelectableOptions[position]);
		if (mListDrawables != null) {
			holder.listImageButton.setImageResource(mListDrawables.getResourceId(position, -1));
			holder.listImageButton.setTag(mListDrawables.getResourceId(position, -1));
		} else if (mListDrawablesId != null) {
			holder.listImageButton.setImageResource(mListDrawablesId[position]);
			holder.listImageButton.setTag(mListDrawablesId[position]);
		}
						
		if(mCurrentSelectedOption != null && mCurrentSelectedOption.equals(mSelectableOptions[position])) {
			Log.d(TAG, "Selected option: " + mCurrentSelectedOption + " Position " + position + ": " + mSelectableOptions[position]);
			holder.listImageButton.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.oval_background_green));
		}

		return convertView;
	}

	static class ViewHolder {
		ImageView listImageButton;
	}
}