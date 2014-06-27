package android.app.printerapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	static {
		
		addItem(new ListItem("1", "Devices"));
		addItem(new ListItem("2", "Print panel"));
		addItem(new ListItem("3", "Models"));
		addItem(new ListItem("4", "History"));
	}

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
}
