package android.app.printerapp.devices;

import android.app.printerapp.model.ModelFile;
import android.graphics.drawable.Drawable;

/**
 * Object that is included in the quick print slide up panel.
 * Includes the image resource, the title and the description of the model.
 */
public class QuickPrintModel extends ModelFile {

    private Drawable mModelImageDrawable; //Image id of the model
    private String mModelName;
    private String mModelDescription;

    public QuickPrintModel(String fileName, String storage, Drawable modelImageId, String modelName, String modelDescription) {
        super(fileName, storage);
        this.mModelImageDrawable = modelImageId;
        this.mModelName = modelName;
        this.mModelDescription = modelDescription;
    }

    public Drawable getModelImageDrawable() {
        return mModelImageDrawable;
    }

    public void setModelImageDrawable(Drawable mModelImageDrawable) {
        this.mModelImageDrawable = mModelImageDrawable;
    }

    public String getModelName() {
        return mModelName;
    }

    public void setModelName(String mModelName) {
        this.mModelName = mModelName;
    }

    public String getModelDescription() {
        return mModelDescription;
    }

    public void setModelDescription(String mModelDescription) {
        this.mModelDescription = mModelDescription;
    }

    @Override
    public String toString() {
        return "QuickPrintModel{" +
                "mModelImageDrawable=" + mModelImageDrawable +
                ", mModelName='" + mModelName + '\'' +
                ", mModelDescription='" + mModelDescription + '\'' +
                '}';
    }
}
