package android.app.printerapp;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class ListContent {

    public static final String ID_DEVICES = "Devices";
    public static final String ID_LIBRARY = "Library";
    public static final String ID_VIEWER = "Viewer";
    public static final String ID_SETTINGS = "Settings";
    public static final String ID_DEVICES_SETTINGS = "Devices_settings";
    public static final String ID_DETAIL = "Detail";
    public static final String ID_PRINTVIEW = "PrintView";
    public static final String ID_INITIAL = "Initial";

	/**
	 * A dummy item representing a piece of content.
	 */
	public static class DrawerListItem {
        public String id;
		public String type;
		public String model;
        public String icon;
        public String time;
        public String date;
        public String path;

		public DrawerListItem(String type, String model, String time, String date, String path) {
			this.type = type;
			this.model = model;
            this.time = time;
            this.date = date;
            this.path = path;
		}

		@Override
		public String toString() {
			return model;
		}
	}

}
