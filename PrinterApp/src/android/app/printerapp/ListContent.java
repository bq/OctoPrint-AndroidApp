package android.app.printerapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class ListContent {

	/**
	 * An array of sample (dummy) items.
	 */
	public static List<ListItem> ITEMS = new ArrayList<ListItem>();

	/**
	 * A map of sample (dummy) items, by ID.
	 */
	public static Map<String, ListItem> ITEM_MAP = new HashMap<String, ListItem>();

	private static void addItem(ListItem item) {
		ITEMS.add(item);
		ITEM_MAP.put(item.id, item);
	}

	/**
	 * A dummy item representing a piece of content.
	 */
	public static class ListItem {
		public String id;
		public String content;

		public ListItem(String id, String content) {
			this.id = id;
			this.content = content;
		}

		@Override
		public String toString() {
			return content;
		}
	}
	
	/**Added method to retrieve list with string values
	 * 
	 * @param context
	 * @return
	 */
	public static List<ListItem> getItemList(Context context){
		
		addItem(new ListItem("1", context.getString(R.string.fragment_devices)));
		addItem(new ListItem("2", context.getString(R.string.fragment_print)));
		addItem(new ListItem("3", context.getString(R.string.fragment_models)));
		addItem(new ListItem("4", context.getString(R.string.fragment_history)));
		addItem(new ListItem("5", context.getString(R.string.fragment_settings)));
		
		return ITEMS;
	
	}
}
