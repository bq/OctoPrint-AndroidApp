package android.app.printerapp.model;

/**
 * This class will hold all the information about profiles o save locally or upload to the server
 * Created by alberto-baeza on 10/23/14.
 */
public class ModelProfile {

    /** Profile info ************/
    private String mDisplayName;
    private String mKey;
    private boolean mDefault;

    /** Profile data ***********/

    //Basic
    private int mFillDensity;
    private String mSupport;

    //Speed
    private int mTravelSpeed;
    private float mBottomLayerSpeed;
    private float mInfillSpeed;
    private float mOuterShellSpeed;
    private float mInnerShellSpeed;

    //Cooling
    private int mCoolMinLayerTime;
    private boolean mFanEnabled;


    //Constructor
    public ModelProfile(String display, String key, boolean def){

        mDisplayName = display;
        mKey = key;
        mDefault = def;

    }



}
