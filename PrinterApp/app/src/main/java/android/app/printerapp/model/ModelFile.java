package android.app.printerapp.model;

import android.app.printerapp.R;
import android.app.printerapp.library.LibraryController;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;

import java.io.File;

/**
 * Model class to define a printable element, with a reference to its STL, GCODE and storage.
 *
 * @author alberto-baeza
 */
@SuppressWarnings("serial")
public class ModelFile extends File {

    //Reference to its original stl
    private String mPathStl;

    //Reference to its gcode list
    private String mPathGcode;

    //Reference to storage
    private String mStorage;

    //Reference to image
    private Drawable mSnapshot;

    public ModelFile(String path, String storage) {
        super(path);

        mStorage = storage;

        //TODO: Move this to the ModelFile code
        setPathStl(LibraryController.retrieveFile(path, "_stl"));
        setPathGcode(LibraryController.retrieveFile(path, "_gcode"));
        setSnapshot(path + "/" + getName() + ".thumb");

    }

    /**
     * ************
     * GETS
     * *************
     */

    public String getStl() {
        return mPathStl;
    }

    //TODO Multiple gcodes!
    public String getGcodeList() {
        return mPathGcode;
    }

    public String getStorage() {
        return mStorage;
    }

    public Drawable getSnapshot() {
        return mSnapshot;
    }

    //TODO: Temporary info path
    public String getInfo() {
        String infopath = getAbsolutePath() + "/" + getName() + ".info";
        return infopath;
    }

    /**
     * *****************
     * SETS
     * ****************
     */

    public void setPathStl(String path) {
        mPathStl = path;
    }

    public void setPathGcode(String path) {
        mPathGcode = path;
    }

    public void setSnapshot(String path) {

        try {
            Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(path), 256, 256);

            mSnapshot = new BitmapDrawable(Resources.getSystem(),ThumbImage);
            //mSnapshot = Drawable.createFromPath(path);
        } catch (Exception e) {
            mSnapshot = Resources.getSystem().getDrawable(R.drawable.ic_file_gray);
        }

    }

}
