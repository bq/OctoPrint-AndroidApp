package android.app.printerapp.library;

import android.app.AlertDialog;
import android.app.printerapp.Log;
import android.app.printerapp.R;
import android.app.printerapp.viewer.DataStorage;
import android.app.printerapp.viewer.GcodeFile;
import android.app.printerapp.viewer.StlFile;
import android.app.printerapp.viewer.ViewerMainFragment;
import android.app.printerapp.viewer.ViewerSurfaceView;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;


/**
 * 
 * this method will create a new folder structure from a file in our system
 * @author alberto-baeza
 *
 */
public class LibraryModelCreation {
	
	static AlertDialog mAlert;
	static String mName;
	public static ViewerSurfaceView mSnapshotSurface;
	public static FrameLayout mSnapshotLayout;
	
	private static Handler mHandler = new Handler();
	private static final int WAIT_TIME = 2000;

    private static Context mContext;
    private static File mFile;
    private static ArrayList<File> mFileQueue = null;
    private static int mCount = 0;
	
	//Static method to create a folder structure
	public static void createFolderStructure(Context context, File source){
		//Catch null pointer because file browser buttons aren't implemented
		if (source!=null){
			mName = source.getName().substring(0, source.getName().lastIndexOf('.'));
            mContext = context;
            mFile = source;

			/*File root = new File(LibraryController.getParentFolder().getAbsolutePath() +
					"/Files/" + mName);*/

            File root = new File(LibraryController.getCurrentPath() + "/" + mName);

			File mainFolder;
			File secondaryFolder;
			
			Log.i("OUT", "File " + root.getAbsolutePath() + " source " + mFile.getName());
			
			//root folder
			if (root.mkdirs()){
				
				if ((mFile.getName().contains(".stl")) || (mFile.getName().contains(".STL"))){
					
					mainFolder = new File(root.getAbsolutePath() + "/_stl");
					secondaryFolder = new File(root.getAbsolutePath() + "/_gcode");
					
				} else {
					mainFolder = new File(root.getAbsolutePath() + "/_gcode");
					secondaryFolder = new File(root.getAbsolutePath() + "/_stl");
					
				}
				//gcode folder
				if (secondaryFolder.mkdir()){
					
					
					
				}
				
				//stl folder
				if (mainFolder.mkdir()){
					
					try{
					
						File target = new File(mainFolder.getAbsolutePath() + "/" + mFile.getName());
		
						
						if(mFile.exists()){
		                    
		                    InputStream in = new FileInputStream(mFile);
		                    OutputStream out = new FileOutputStream(target);
		         
		                    // Copy the bits from instream to outstream
		                    byte[] buf = new byte[1024];
		                    int len;
		                     
		                    while ((len = in.read(buf)) > 0) {
		                        out.write(buf, 0, len);
		                    }
		                     
		                    in.close();
		                    out.close();
		                                          
		                }else{
		
		                }
						
						openModel(mContext, target.getAbsolutePath());

					} catch (IOException e){
						e.printStackTrace();
					}	
				}				
			}	
		}
		
	}	
	
	/**
	 * Open model to take screenshot
	 * @param context
	 * @param path
	 */
	private static void openModel (final Context context, final String path) {
		View generatingProjectDialog = LayoutInflater.from(context).inflate(R.layout.dialog_loading_project, null);
		mSnapshotLayout = (FrameLayout) generatingProjectDialog.findViewById (R.id.framesnapshot);

        Log.i("OUT", "Opening to snap " + path);
        String count = context.getString(R.string.generating_project);

        if (mFileQueue!=null) count += " ("  + (mCount - (mFileQueue.size() - 1)) + "/" + mCount + ")";

        final MaterialDialog.Builder createFolderDialog = new MaterialDialog.Builder(mContext);
        createFolderDialog.title(count)
                .customView(generatingProjectDialog, true)
                .cancelable(false)
                .autoDismiss(false);
		
		//We need the alertdialog instance to dismiss it
   		mAlert = createFolderDialog.build();
		mAlert.show();
		
		File file = new File (path);
		List<DataStorage> list = new ArrayList<DataStorage> ();
		DataStorage data = new DataStorage();

        if (StlFile.checkFileSize(file,mContext)){

            if(LibraryController.hasExtension(0, path)) {
                StlFile.openStlFile (context, file, data, ViewerMainFragment.DO_SNAPSHOT);
            } else if (LibraryController.hasExtension(1, path)) {
                GcodeFile.openGcodeFile(context, file, data, ViewerMainFragment.DO_SNAPSHOT);
            }

            mSnapshotSurface = new ViewerSurfaceView (context, list, ViewerSurfaceView.NORMAL, ViewerMainFragment.DO_SNAPSHOT, null);
            list.add(data);

        } else mAlert.dismiss();


	}
	
	/**
	 * This method is called from STlFile or GcodeFile when data is ready to render. Add the view to the layout.
	 */
	public static void takeSnapshot () {
		mSnapshotSurface.setZOrderOnTop(true);
		mSnapshotLayout.addView(mSnapshotSurface);	
	}
	
	/**
	 * Creates the snapshot of the model
	 */
	public static void saveSnapshot (final int width, final int height, final ByteBuffer bb ) {
		int screenshotSize = width * height;
	      
        int pixelsBuffer[] = new int[screenshotSize];
        bb.asIntBuffer().get(pixelsBuffer);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        bitmap.setPixels(pixelsBuffer, screenshotSize-width, -width, 0, 0, width, height);
        pixelsBuffer = null;

        short sBuffer[] = new short[screenshotSize];
        ShortBuffer sb = ShortBuffer.wrap(sBuffer);
        bitmap.copyPixelsToBuffer(sb);

        //Making created bitmap (from OpenGL points) compatible with Android bitmap
        for (int i = 0; i < screenshotSize; ++i) {                  
            short v = sBuffer[i];
            sBuffer[i] = (short) (((v&0x1f) << 11) | (v&0x7e0) | ((v&0xf800) >> 11));
        }
        sb.rewind();
        bitmap.copyPixelsFromBuffer(sb);
        
        try {
            FileOutputStream fos = new FileOutputStream(LibraryController.getCurrentPath() +"/" + mName + "/" + mName + ".thumb");
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }  
        
        dismissSnapshotAlert ();
    }	
	
	/**
	 * Dismiss the loading project dialog after a few seconds.
	 */
	private static void dismissSnapshotAlert () {

		mHandler.postDelayed(new Runnable() {
            public void run() {
                mAlert.dismiss();

                //Only show delete dialog if there is no queue //TODO
                if (mFileQueue==null) deleteFileDialog();
                else checkQueue();

                Intent intent = new Intent("notify");
                intent.putExtra("message", "Files");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);


            }
        }, WAIT_TIME);

	}

    private static void deleteFileDialog(){

        AlertDialog.Builder adb = new AlertDialog.Builder(mContext);
        adb.setTitle(R.string.library_delete_dialog_original);
        adb.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                LibraryController.deleteFiles(mFile);

            }
        });
        adb.setNegativeButton(R.string.cancel, null);
        adb.show();

        /**
         * Use an intent because it's an asynchronous static method without any reference (yet)
         */
        Intent intent = new Intent("notify");
        intent.putExtra("message", "Files");
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

    }

    /************************************************************
     *
     * JOB QUEUE
     *
     ***********************************************************/

    //Send a file list to enqueue jobs
    public static void enqueueJobs(Context context, ArrayList<File> q){

        mFileQueue = q;
        mCount = mFileQueue.size();
        createFolderStructure(context, mFileQueue.get(0));

    }

    //Check if there are more files in the queue
    public static void checkQueue(){

        if (mFileQueue != null) {

            mFileQueue.remove(0); //Remove last file

            if (mFileQueue.size() > 0) { //If there are more

                createFolderStructure(mContext, mFileQueue.get(0)); //Create folder again

            } else {

                mFileQueue = null; //Remove queue
                mCount = 0;
            }


        }
    }

}
