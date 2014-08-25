package android.app.printerapp.model;

/**
 * Class to define Comments in the Detailed View
 * @author alberto-baeza
 *
 */
public class ModelComment {
	
	private String mAuthor;
	private String mDate;
	private String mComment;
	
	public ModelComment(String a, String d, String c){
		
		mAuthor = a;
		mDate = d;
		mComment = c;
		
	}
	
	public String getAuthor(){
		return mAuthor;
	}
	
	public String getDate(){
		return mDate;
	}
	
	public String getComment(){
		return mComment;
	}
	

}
