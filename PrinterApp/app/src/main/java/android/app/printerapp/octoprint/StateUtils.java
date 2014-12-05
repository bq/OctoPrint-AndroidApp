package android.app.printerapp.octoprint;

/**
 * Class with the type of states the printers can hold at any moment
 * @author alberto-baeza
 *
 */
public class StateUtils {
	public static final int STATE_ADHOC = -2;
	public static final int	STATE_NEW = -1;
    public static final int STATE_NONE = 0;
    public static final int STATE_OPEN_SERIAL = 1;
    public static final int STATE_DETECT_SERIAL = 2;
	public static final int	STATE_DETECT_BAUDRATE = 3;
	public static final int	STATE_CONNECTING = 4;
	public static final int	STATE_OPERATIONAL = 5;
	public static final int	STATE_PRINTING = 6;
	public static final int	STATE_PAUSED = 7;
	public static final int	STATE_CLOSED = 8;
	public static final int	STATE_ERROR = 9;
	public static final int	STATE_CLOSED_WITH_ERROR = 10;
	public static final int	STATE_TRANSFERING_FILE = 11;

    public static final int SLICER_HIDE = -1;
    public static final int SLICER_UPLOAD = 0;
    public static final int SLICER_SLICE = 1;
    public static final int SLICER_DOWNLOAD = 2;

    public static final int TYPE_WITBOX = 1;
    public static final int TYPE_PRUSA = 2;
    public static final int TYPE_CUSTOM = 3;

}
