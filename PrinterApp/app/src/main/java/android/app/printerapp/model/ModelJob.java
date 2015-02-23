package android.app.printerapp.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class defines a new Printing Job as a Status listener. Basically it's a reference to the current
 * status of the printer. If there is currently an ongoing job, it'll show the printing status, if there's nothing, 
 * it won't show anything.
 * @author alberto-baeza
 *
 */
public class ModelJob {
	
	//Printer status
	private String mFile;
	private String mFilament;
	private String mSize;
	private String mEstimated;
	private String mTimelapse;
	private String mHeight;
	private String mPrintTime;
	private String mPrintTimeLeft;
	private String mPrinted;
	private String mProgress = "0";
	
	private boolean mFinished = false;
	
	public ModelJob(){
				
	}
	
	/*************
	 * GETS
	 *************/
	
	public String getFilename(){
		return mFile;
	}
	
	public String getFilament(){
		return mFilament;
	}
	
	public String getSize(){
		return mSize;
	}
	
	public String getEstimated(){
		return mEstimated;
	}
	
	public String getPrintTime(){
		return mPrintTime;
	}
	
	public String getPrintTimeLeft(){
		return mPrintTimeLeft;
	}
	
	public String getPrinted(){
		return mPrinted;
	}
	
	public String getProgress(){
		return mProgress;
	}
		
	
	/***************
	 * 	SETS
	 *****************/
		
	public void updateJob(JSONObject status){
		JSONObject job, progress;
		try {

			//Current job status filesize/filament/estimated print time
			job = status.getJSONObject("job");
			
			mFile = job.getJSONObject("file").getString("name");
			mFilament = job.getString("filament");
			mSize = job.getJSONObject("file").getString("size");

			//Progress time/timelapse
			progress = status.getJSONObject("progress");
			
			mPrinted = progress.getString("filepos");
			mPrintTime = progress.getString("printTime");
			mPrintTimeLeft = progress.getString("printTimeLeft");
			mProgress = progress.getString("completion");
			
			if (!mProgress.equals("null")){
				Double n = Double.parseDouble(mProgress);
				if (n.intValue() == 100) mFinished = true;
				else mFinished = false;
			} else mFinished = false;

			//Log.i("MODEL", "Timelapse: " + mTimelapse + " Height: " + mHeight + " Print time: " + mPrintTime +
					//" Print time left: " + mPrintTimeLeft);
			

		
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Finish job
	 */
	
	public boolean getFinished(){
		return mFinished;
	}
	
	public void setFinished(){
		
		mFinished = true;
				
	}

}
