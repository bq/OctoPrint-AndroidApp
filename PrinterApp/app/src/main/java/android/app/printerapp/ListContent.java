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
	public static List<DrawerListItem> ITEMS = new ArrayList<DrawerListItem>();

	/**
	 * A map of sample (dummy) items, by ID.
	 */
	public static Map<String, DrawerListItem> ITEM_MAP = new HashMap<String, DrawerListItem>();

	private static void addItem(DrawerListItem item) {
		ITEMS.add(item);
		ITEM_MAP.put(item.id, item);
	}

	/**
	 * A dummy item representing a piece of content.
	 */
	public static class DrawerListItem {
		public String id;
		public String content;
        public int iconId;

		public DrawerListItem(String id, String content, int iconId) {
			this.id = id;
			this.content = content;
            this.iconId = iconId;
		}

		@Override
		public String toString() {
			return content;
		}
	}
	
	/**
	 * Added method to retrieve list with string values
	 * 
	 * @param context
	 * @return List of items with id + name as content
	 * 
	 *
	 */
	public static List<DrawerListItem> getItemList(Context context){
		
		//We need to clear the list before adding elements to avoid multiple insertion
		ITEMS.clear();
		
		addItem(new DrawerListItem("1", context.getString(R.string.fragment_devices), R.drawable.ic_drawer_devices));
		addItem(new DrawerListItem("2", context.getString(R.string.fragment_print), R.drawable.ic_drawer_printpanel));
		addItem(new DrawerListItem("3", context.getString(R.string.fragment_models), R.drawable.ic_drawer_models));
		
		//TODO: History fragment
//		addItem(new DrawerListItem("4", context.getString(R.string.fragment_history), R.drawable.ic_drawer_history));
		
		addItem(new DrawerListItem("5", context.getString(R.string.fragment_settings), R.drawable.ic_drawer_settings));
		
		return ITEMS;
	
	}
}
