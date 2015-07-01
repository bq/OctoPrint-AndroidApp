package android.app.printerapp.viewer;

import android.app.AlertDialog;
import android.app.printerapp.Log;
import android.app.printerapp.R;
import android.app.printerapp.library.LibraryController;
import android.app.printerapp.library.LibraryModelCreation;
import android.app.printerapp.viewer.Geometry.Point;
import android.app.printerapp.viewer.Geometry.Vector;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.alertdialogpro.ProgressDialogPro;
import com.devsmart.android.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;


public class StlFile {

    private static final String TAG = "gcode";

    private static File mFile;

    String mStringAux = "";

    private static ProgressDialogPro mProgressDialog;
    private static DataStorage mData;
    private static Context mContext;

    private static Thread mThread;
    private static boolean mContinueThread = true;

    private static final int COORDS_PER_TRIANGLE = 9;
    private static int mMode;

    private static final int MAX_SIZE = 50000000; //50Mb


    public static void openStlFile(Context context, File file, DataStorage data, int mode) {
        Log.i(TAG, "Open STL File");

        mContext = context;

        mMode = mode;
        mContinueThread = true;

        if (mMode != ViewerMainFragment.DO_SNAPSHOT)
            mProgressDialog = prepareProgressDialog(context);

        mData = data;

        mFile = file;
        Uri uri = Uri.fromFile(file);

        mData.setPathFile(mFile.getAbsolutePath());
        mData.initMaxMin();


        startThreadToOpenFile(context, uri);


    }

    public static void startThreadToOpenFile(final Context context, final Uri uri) {

        mThread = new Thread() {
            @Override
            public void run() {
                byte[] arrayBytes = toByteArray(context, uri);

                try {
                    if (isText(arrayBytes)) {
                        Log.e(TAG, "trying text... ");
                        if (mContinueThread) processText(mFile);
                    } else {
                        Log.e(TAG, "trying binary...");
                        if (mContinueThread) processBinary(arrayBytes);
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (mContinueThread) mHandler.sendEmptyMessage(0);
            }
        };

        mThread.start();


    }


    private static byte[] toByteArray(Context context, Uri filePath) {
        InputStream inputStream = null;
        byte[] arrayBytes = null;
        try {
            inputStream = context.getContentResolver().openInputStream(filePath);
            arrayBytes = IOUtils.toByteArray(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return arrayBytes;
    }

    private static boolean isText(byte[] bytes) {
        for (byte b : bytes) {
            if (b == 0x0a || b == 0x0d || b == 0x09) {
                // white spaces
                continue;
            }
            if (b < 0x20 || (0xff & b) >= 0x80) {
                // control codes
                return false;
            }
        }
        return true;
    }


    /**
     * Progress Dialog
     * ----------------------------------
     */
    private static ProgressDialogPro prepareProgressDialog(Context context) {

        AlertDialog dialog = new ProgressDialogPro(context, R.style.Theme_AlertDialogPro_Material_Light_Green);
        dialog.setTitle(R.string.loading_stl);
        dialog.setMessage(context.getResources().getString(R.string.be_patient));

        ProgressDialogPro progressDialog = (ProgressDialogPro) dialog;
        progressDialog.setProgressStyle(ProgressDialogPro.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(false);

        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mContinueThread = false;
                try {
                    mThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                ViewerMainFragment.resetWhenCancel();
            }
        });

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        if (mMode!= ViewerMainFragment.DO_SNAPSHOT) {
            dialog.show();
            dialog.getWindow().setLayout(500, LinearLayout.LayoutParams.WRAP_CONTENT);
        }

        return progressDialog;
    }

    private static int getIntWithLittleEndian(byte[] bytes, int offset) {
        return (0xff & bytes[offset]) | ((0xff & bytes[offset + 1]) << 8) | ((0xff & bytes[offset + 2]) << 16) | ((0xff & bytes[offset + 3]) << 24);
    }

    private static Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mData.getCoordinateListSize() < 1) {
                Toast.makeText(mContext, R.string.error_opening_invalid_file, Toast.LENGTH_SHORT).show();
                ViewerMainFragment.resetWhenCancel();
                if (mMode != ViewerMainFragment.DO_SNAPSHOT) mProgressDialog.dismiss();
                return;
            }

            //only center again if it's a new file
            if ((mFile.getName().substring(0, 3)).contains("tmp")) {
                mData.fillVertexArray(true);
            } else mData.fillVertexArray(true);
            mData.fillNormalArray();

            mData.clearNormalList();
            mData.clearVertexList();

    		//Finish
			if (mMode== ViewerMainFragment.DONT_SNAPSHOT) {
				ViewerMainFragment.draw();
                ViewerMainFragment.doPress();
				mProgressDialog.dismiss();

                //TODO better filtering
                if (!(mFile.getName().substring(0, 3)).contains("tmp"))
                    ViewerMainFragment.slicingCallback();
            } else if (mMode == ViewerMainFragment.DO_SNAPSHOT) {
                LibraryModelCreation.takeSnapshot();
            }
        }
    };

    private static void processText(File file) {
        String line;
        try {
            int maxLines = 0;
            StringBuilder allLines = new StringBuilder("");
            BufferedReader countReader = new BufferedReader(new FileReader(file));

            float milis = SystemClock.currentThreadTimeMillis();

            while ((line = countReader.readLine()) != null && mContinueThread) {
                if (line.trim().startsWith("vertex ")) {
                    line = line.replaceFirst("vertex ", "").trim();
                    allLines.append(line + "\n");
                    maxLines++;
                    if (maxLines % 1000 == 0 && mMode != ViewerMainFragment.DO_SNAPSHOT)
                        mProgressDialog.setMax(maxLines);
                }
            }

            Log.i(TAG, "STL [Text] Read in: " + (SystemClock.currentThreadTimeMillis() - milis));

            if (mMode != ViewerMainFragment.DO_SNAPSHOT) mProgressDialog.setMax(maxLines);

            countReader.close();


            int lines = 0;

            int firstVertexIndex = 0;
            int secondVertexIndex = 0;
            int thirdVertexIndex = 0;
            int initialVertexIndex = -1;

            float milis2 = SystemClock.currentThreadTimeMillis();

            while (lines < maxLines && mContinueThread) {
                firstVertexIndex = allLines.indexOf("\n", thirdVertexIndex + 1);
                secondVertexIndex = allLines.indexOf("\n", firstVertexIndex + 1);
                thirdVertexIndex = allLines.indexOf("\n", secondVertexIndex + 1);

                line = allLines.substring(initialVertexIndex + 1, thirdVertexIndex);
                initialVertexIndex = thirdVertexIndex;

                processTriangle(line);
                lines += 3;

                if (lines % (maxLines / 10) == 0) {
                    if (mMode != ViewerMainFragment.DO_SNAPSHOT) mProgressDialog.setProgress(lines);
                }
            }

            Log.i(TAG, "STL [Text] Processed in: " + (SystemClock.currentThreadTimeMillis() - milis2));


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void processTriangle(String line) throws Exception {
        String[] vertex = line.split("\n");

        String[] vertexValues = vertex[0].split("\\s+");
        float x = Float.parseFloat(vertexValues[0]);
        float y = Float.parseFloat(vertexValues[1]);
        float z = Float.parseFloat(vertexValues[2]);
        Vector v0 = new Vector(x, y, z);
        mData.adjustMaxMin(x, y, z);
        mData.addVertex(x);
        mData.addVertex(y);
        mData.addVertex(z);

        vertexValues = vertex[1].split("\\s+");
        x = Float.parseFloat(vertexValues[0]);
        y = Float.parseFloat(vertexValues[1]);
        z = Float.parseFloat(vertexValues[2]);
        Vector v1 = new Vector(x, y, z);
        mData.adjustMaxMin(x, y, z);
        mData.addVertex(x);
        mData.addVertex(y);
        mData.addVertex(z);

        vertexValues = vertex[2].split("\\s+");
        x = Float.parseFloat(vertexValues[0]);
        y = Float.parseFloat(vertexValues[1]);
        z = Float.parseFloat(vertexValues[2]);
        Vector v2 = new Vector(x, y, z);
        mData.adjustMaxMin(x, y, z);
        mData.addVertex(x);
        mData.addVertex(y);
        mData.addVertex(z);

        //Calculate triangle normal vector
        Vector normal = Vector.normalize(Vector.crossProduct(Vector.substract(v1, v0), Vector.substract(v2, v0)));

        mData.addNormal(normal.x);
        mData.addNormal(normal.y);
        mData.addNormal(normal.z);

    }

    private static void processBinary(byte[] stlBytes) throws Exception {

        int vectorSize = getIntWithLittleEndian(stlBytes, 80);

        if (mMode != ViewerMainFragment.DO_SNAPSHOT) mProgressDialog.setMax(vectorSize);

        float milis = SystemClock.currentThreadTimeMillis();

        for (int i = 0; i < vectorSize; i++) {
            if (!mContinueThread) break;

            float x = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 12));
            float y = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 16));
            float z = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 20));
            Vector v0 = new Vector(x, y, z);

            mData.adjustMaxMin(x, y, z);
            mData.addVertex(x);
            mData.addVertex(y);
            mData.addVertex(z);


            x = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 24));
            y = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 28));
            z = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 32));
            Vector v1 = new Vector(x, y, z);

            mData.adjustMaxMin(x, y, z);
            mData.addVertex(x);
            mData.addVertex(y);
            mData.addVertex(z);

            x = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 36));
            y = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 40));
            z = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 44));
            Vector v2 = new Vector(x, y, z);

            mData.adjustMaxMin(x, y, z);
            mData.addVertex(x);
            mData.addVertex(y);
            mData.addVertex(z);

            //Calculate triangle normal vector
            Vector normal = Vector.normalize(Vector.crossProduct(Vector.substract(v1, v0), Vector.substract(v2, v0)));

            mData.addNormal(normal.x);
            mData.addNormal(normal.y);
            mData.addNormal(normal.z);


            if (i % (vectorSize / 10) == 0) {
                if (mMode != ViewerMainFragment.DO_SNAPSHOT) mProgressDialog.setProgress(i);
            }
        }
        Log.i(TAG, "STL [BINARY] Read & Processed in: " + (SystemClock.currentThreadTimeMillis() - milis));

        Log.i("Slicer", "Sizes: \n" +
                "Width" + (mData.getMaxX() - mData.getMinX()) + "\n" +
                "Depth" + (mData.getMaxY() - mData.getMinY()) + "\n" +
                "Height" + (mData.getMaxZ() - mData.getMinZ()));


    }

    private static float[] setTransformationVector(float x, float y, float z, float[] rotationMatrix, float scaleFactorX, float scaleFactorY, float scaleFactorZ, float adjustZ, Point center) {
        float[] vector = new float[4];
        float[] result = new float[4];

        vector[0] = x;
        vector[1] = y;
        vector[2] = z;

        Matrix.multiplyMV(result, 0, rotationMatrix, 0, vector, 0);

        result[0] = result[0] * scaleFactorX + center.x;
        result[1] = result[1] * scaleFactorY + center.y;
        result[2] = result[2] * scaleFactorZ + center.z + adjustZ;

        return result;
    }

    public static boolean checkIfNameExists(String projectName) {
        File check = new File(LibraryController.getParentFolder().getAbsolutePath() + "/Files/" + projectName);
        if (check.exists()) return true;

        return false;
    }

    /**
     * This method will save the model to a file, either to slice or to make a new project.
     * I made a few adjustment to select between the two types of file creation. (Alberto)
     *
     * @param dataList
     * @param projectName
     */
    public static boolean saveModel(List<DataStorage> dataList, String projectName, SlicingHandler slicer) {
        float[] coordinates = null;
        int coordinateCount = 0;
        float[] rotationMatrix = new float[16];
        float scaleFactorX = 0;
        float scaleFactorY = 0;
        float scaleFactorZ = 0;
        Point center = new Point(0, 0, 0);
        float adjustZ = 0;
        float[] vector = new float[3];

        //Calculating buffer size
        for (int i = 0; i < dataList.size(); i++)
            coordinateCount += dataList.get(i).getVertexArray().length;

        if (coordinateCount == 0) {
            return false;
        }

        //Each triangle has 3 vertex with 3 coordinates each. COORDS_PER_TRIANGLE = 9
        int normals = (coordinateCount / COORDS_PER_TRIANGLE) * 3; //number of normals coordinates in the file

        //The file consists of the header, the vertex and normal coordinates (4 bytes per component) and
        //a flag (2 bytes per triangle) to indicate the final of the triangle.
        ByteBuffer bb = ByteBuffer.allocate(84 + (coordinateCount + normals) * 4 + coordinateCount * 2);
        bb.order(ByteOrder.LITTLE_ENDIAN);

        //TODO Out of Memory when saving file to slice

        //Header
        byte[] header = new byte[80];
        bb.put(header);

        //Number of triangles
        bb.putInt(coordinateCount / COORDS_PER_TRIANGLE);

        Log.i("Slicer", "Saving new model");

        for (int i = 0; i < dataList.size(); i++) {
            DataStorage data = dataList.get(i);
            rotationMatrix = data.getRotationMatrix();
            scaleFactorX = data.getLastScaleFactorX();
            scaleFactorY = data.getLastScaleFactorY();
            scaleFactorZ = data.getLastScaleFactorZ();
            adjustZ = data.getAdjustZ();
            center = data.getLastCenter();
            coordinates = data.getVertexArray();

            for (int j = 0; j < coordinates.length; j += 9) {

                //Normal data. It is not necessary to store the info
                bb.putFloat(0);
                bb.putFloat(0);
                bb.putFloat(0);

                //Triangle Data, 3 vertex with 3 coordinates (x,y,z) each one.
                vector = setTransformationVector(coordinates[j], coordinates[j + 1], coordinates[j + 2], rotationMatrix, scaleFactorX, scaleFactorY, scaleFactorZ, adjustZ, center);
                bb.putFloat(vector[0]);
                bb.putFloat(vector[1]);
                bb.putFloat(vector[2]);

                vector = setTransformationVector(coordinates[j + 3], coordinates[j + 4], coordinates[j + 5], rotationMatrix, scaleFactorX, scaleFactorY, scaleFactorZ, adjustZ, center);
                bb.putFloat(vector[0]);
                bb.putFloat(vector[1]);
                bb.putFloat(vector[2]);

                vector = setTransformationVector(coordinates[j + 6], coordinates[j + 7], coordinates[j + 8], rotationMatrix, scaleFactorX, scaleFactorY, scaleFactorZ, adjustZ, center);
                bb.putFloat(vector[0]);
                bb.putFloat(vector[1]);
                bb.putFloat(vector[2]);

                bb.putShort((short) 0); // end of triangle
            }
        }

        bb.position(0);
        byte[] data = bb.array();

        Log.i("Slicer", "Saved");

        if (slicer != null) {

            slicer.setData(data);
            //slicer.sendTimer();

        } else {
            String path = LibraryController.getParentFolder().getAbsolutePath() + "/" + projectName + ".stl";
            try {
                FileOutputStream fos = new FileOutputStream(path);
                fos.write(data);
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            File file = new File(path);
            LibraryModelCreation.createFolderStructure(mContext, file);
            file.delete();
        }


        return true;
    }

    /**
     * **********************************************************************************
     */

    /*
    Check file size or issue a notification
     */
    public static boolean checkFileSize(File file, Context context) {

        if (file.length() < MAX_SIZE) return true;
        else return false;

    }

}