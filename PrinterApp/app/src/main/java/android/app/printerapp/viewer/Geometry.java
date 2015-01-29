package android.app.printerapp.viewer;


import android.opengl.Matrix;

import java.util.List;

public class Geometry {
	 private static final float OFFSET = 20f;

	 public static class Point {
		 public final float x, y, z;

	     public Point(float x, float y, float z) {
	    	 this.x = x;
	         this.y = y;
	         this.z = z;
	     }   
	 }
	 
	 public static class Box  {
		 static final int LEFT = 0;
		 static final int RIGHT = 1;
		 static final int FRONT = 2;
		 static final int BEHIND = 3;
		 static final int DOWN = 4;
		 static final int UP = 5;
		 
		 float coordBox [] = new float [6];
		 
		 public Box (float minX, float maxX, float minY, float maxY, float minZ, float maxZ ) {
			 coordBox[LEFT] = minX;
			 coordBox[RIGHT] = maxX;
			 coordBox[FRONT] = minY;
			 coordBox[BEHIND] = maxY;
			 coordBox[DOWN] = minZ;
			 coordBox[UP] = maxZ;
		 }
	 }
	 
	 public static class Vector  {
		 public final float x, y, z;

	     public Vector(float x, float y, float z) {
	    	 this.x = x;
	         this.y = y;
	         this.z = z;
	     }
	     
	     public static Vector substract (Vector v1, Vector v2) {
	    	 float x = v1.x-v2.x;
	    	 float y = v1.y-v2.y;
	    	 float z = v1.z-v2.z;
	    	 
	    	 Vector substract = new Vector (x,y,z);
	    	 return substract;
	    	 
	     }
	     
	     public static Vector crossProduct (Vector v, Vector v2) {
	    	 float x = (v.y*v2.z) - (v.z*v2.y);
	    	 float y = (v.z*v2.x) - (v.x*v2.z);
	    	 float z = (v.x*v2.y) - (v.y*v2.x);
	    	 
	    	 Vector result = new Vector (x, y, z);
	    	 return result;	    	 
	     }
	     
	     public static Vector normalize (Vector v) {
	    	 float length = (float) Math.sqrt(v.x*v.x + v.y*v.y + v.z*v.z);
	    	 Vector result = new Vector (v.x/length, v.y/length, v.z/length);
	    	 return result;    	 
	     }
	 }
	 
	 public static class Ray {
		 public final Point point;
	     public final Vector vector;

	     public Ray(Point point, Vector vector) {
	    	 this.point = point;
	         this.vector = vector;
	     }        
	 }
	    
	 public static boolean intersects(Box box, Ray ray) {
		 int index = 0;
		 float k;
		 float x=Float.MIN_VALUE;
		 float y=Float.MIN_VALUE;
		 float z=Float.MIN_VALUE;
		 
		 while (index< box.coordBox.length ) {
			 switch (index) {
			 case (Box.LEFT):
			 case (Box.RIGHT):
				 k = (box.coordBox[index]-ray.point.x)/ray.vector.x;
			 	 x = box.coordBox[index];
			 	 y = ray.point.y + k*ray.vector.y;
			 	 z = ray.point.z + k*ray.vector.z;	 
			 	 break;
			 case (Box.BEHIND):
			 case (Box.FRONT):
				 k = (box.coordBox[index]-ray.point.y)/ray.vector.y;
		 	 	 x = ray.point.x + k*ray.vector.x;
			 	 y = box.coordBox[index];
		 	 	 z = ray.point.z + k*ray.vector.z;	
		 	 	 break;
			 case (Box.UP):
			 case (Box.DOWN):
				 k = (box.coordBox[index]-ray.point.z)/ray.vector.z;
	 	 	 	 x = ray.point.x + k*ray.vector.x;
	 	 	 	 y = ray.point.y + k*ray.vector.y;
			 	 z = box.coordBox[index];

	 	 	 	 break;
			 }

			 //Check if (x,y,z) is a box point
			 if (x>=box.coordBox[Box.LEFT] && x<=box.coordBox[Box.RIGHT] && 
			     y>=box.coordBox[Box.FRONT] && y<=box.coordBox[Box.BEHIND] && 
				 z>=box.coordBox[Box.DOWN] && z<=box.coordBox[Box.UP]) 	
				 	return true;
			 
			 index++;					 
		 }
		 
		 return false;
	 }
	    
	 public static Vector vectorBetween(Point from, Point to) {
		 return new Vector(
				 to.x - from.x, 
				 to.y - from.y, 
				 to.z - from.z);
	 }
	 
	 public static Point intersectionPointWitboxPlate(Ray ray) {    
		 //plane is z=centerZ
		 float k = (0-ray.point.z)/ray.vector.z;
	 	 float x = ray.point.x + k*ray.vector.x;
	 	 float y = ray.point.y + k*ray.vector.y;
	 	 float z = 0;
	 	 
	 	 return new Point (x,y,z);
	 }
	 
	 public static boolean overlaps (float maxX, float minX, float maxY,  float minY, DataStorage d) {
		 float maxX2 = d.getMaxX();
		 float maxY2 = d.getMaxY();
		 
		 float minX2 = d.getMinX();
		 float minY2 = d.getMinY();

		 if (((maxX>=minX2 && maxX<=maxX2) || (minX<=maxX2 && minX>=minX2) || (minX>=minX2 && maxX<=maxX2)) && 				 
			 ((maxY>=minY2 && maxY<=maxY2) || (minY<=maxY2 && minY>=minY2) || (minY>=minY2 && maxY<=maxY2))) {
			 return true;
		 }
		 
		 if (((maxX2>=minX && maxX2<=maxX) || (minX2<=maxX && minX2>=minX) || (minX2>=minX && maxX2<=maxX)) && 				 
			((maxY2>=minY && maxY2<=maxY) || (minY2<=maxY && minY2>=minY) || (minY2>=minY && maxY2<=maxY))) {
			 return true;
		 }
		 
		 //New cases that were not being considered 
		 if (((minX>=minX2 && maxX<=maxX2) && (maxY>=maxY2 && minY<=minY2)) ||
			((minX2>=minX && maxX2<=maxX) && (maxY2>=maxY && minY2<=minY) )) {
			 return true;
		 }
		 
		 return false;

	 }
	 
	 public static boolean relocateIfOverlaps (List<DataStorage> objects) {
		 int objectToFit = objects.size()-1;

         DataStorage data;

         //TODO random crash
         try{

             data = objects.get(objectToFit);

         } catch (ArrayIndexOutOfBoundsException e){

             e.printStackTrace();
             return false;
         }

		 boolean overlaps = false;
		 
		 for (int i=0;i<objects.size();i++) {
			 if(i!= objectToFit && Geometry.overlaps(data.getMaxX(), data.getMinX(), data.getMaxY(), data.getMinY(), objects.get(i))) {
				 overlaps = true;
				 break;
			 }
		 }

		 if (!overlaps) return false;
		 
		float width = data.getMaxX() - data.getMinX();
		float deep = data.getMaxY() - data.getMinY();
		
		float setMinX=Float.MAX_VALUE;
		int index =-1;
		
		float newMaxX;
		float newMinX;
		float newMaxY;
		float newMinY;
		
		for (int i=0; i<objects.size(); i++) {
			if (i!= objectToFit) {
				DataStorage d = objects.get(i); 
				if (d.getMinX()<setMinX) {
					setMinX = d.getMinX();
					index = i;
				}
				//UP
				newMaxX = d.getMaxX();
				newMinX = d.getMinX();
				newMaxY = d.getLastCenter().y + Math.abs(d.getMaxY() - d.getLastCenter().y) + deep + OFFSET;
				newMinY = d.getLastCenter().y + Math.abs(d.getMaxY() - d.getLastCenter().y) +OFFSET; 
							
				if (isValidPosition(newMaxX, newMinX, newMaxY, newMinY, objects, objectToFit)) {
					changeModelToFit(newMaxX, newMinX, newMaxY, newMinY, data);
					break;
				}
				
				//RIGHT
				newMaxX = d.getLastCenter().x + Math.abs(d.getMaxX() - d.getLastCenter().x) + width + OFFSET;
				newMinX = d.getLastCenter().x + Math.abs(d.getMaxX() - d.getLastCenter().x) + OFFSET;
				newMaxY = d.getMaxY();
				newMinY = d.getMinY();	
						
				if (isValidPosition(newMaxX, newMinX, newMaxY, newMinY, objects, objectToFit)) {
					changeModelToFit(newMaxX, newMinX, newMaxY, newMinY, data);
					break;
				}
				
				//DOWN
				newMaxX = d.getMaxX();
				newMinX = d.getMinX();
				newMaxY = d.getLastCenter().y - (Math.abs(d.getMinY() - d.getLastCenter().y) + OFFSET);
				newMinY = d.getLastCenter().y - (Math.abs(d.getMinY() - d.getLastCenter().y) + deep + OFFSET); 	
						
				if (isValidPosition(newMaxX, newMinX, newMaxY, newMinY, objects,  objectToFit)) {
					changeModelToFit(newMaxX, newMinX, newMaxY, newMinY, data);
					break;
				} 
				
				//LEFT
				newMaxX = d.getLastCenter().x - (Math.abs(d.getMinX() - d.getLastCenter().x)+ OFFSET);
				newMinX = d.getLastCenter().x - (Math.abs(d.getMinX() - d.getLastCenter().x) + width + OFFSET);
				newMaxY = d.getMaxY();
				newMinY = d.getMinY();		
						
				if (isValidPosition(newMaxX, newMinX, newMaxY, newMinY, objects, objectToFit)) {
					changeModelToFit(newMaxX, newMinX, newMaxY, newMinY, data);
					break;
				} else if (i==objects.size()-2) {

                    return false;

					/*newMaxX = setMinX - OFFSET;
					newMinX = setMinX - (width + OFFSET);
					newMaxY = objects.get(index).getMaxY()+OFFSET;
					newMinY = objects.get(index).getMinY()+OFFSET;	
					
					data.setStateObject(ViewerRenderer.OUT_NOT_TOUCHED);


					changeModelToFit(newMaxX, newMinX, newMaxY, newMinY, data);*/
				}					
			}
		}
		
		return true;
	 }
		
	 public static boolean isValidPosition (float newMaxX, float newMinX, float newMaxY, float newMinY, List<DataStorage> objects, int object) {
		 boolean overlaps = false; 
		 boolean outOfPlate = false;
		 int k = 0;

         int[] auxPlate = ViewerMainFragment.getCurrentPlate();

		 if (newMaxX > auxPlate[0] || newMinX < -auxPlate[0]
				|| newMaxY > auxPlate[1] || newMinY < -auxPlate[1]) outOfPlate = true;
			
		 while (!outOfPlate && !overlaps && k <objects.size()) {	
			 if (k!=object) {
				 if (Geometry.overlaps(newMaxX, newMinX, newMaxY, newMinY, objects.get(k)))  overlaps = true;
			 }		
			 k++;
		 }

		 if (!outOfPlate && !overlaps) 					
			 return true;
		
		 else return false;
	 }
	
	public static void changeModelToFit (float newMaxX, float newMinX, float newMaxY, float newMinY, DataStorage d) {		
		d.setMaxX(newMaxX);
		d.setMinX(newMinX);
		d.setMaxY(newMaxY);
		d.setMinY(newMinY);
				
		float newCenterX = newMinX + (newMaxX-newMinX)/2;
		float newCenterY = newMinY + (newMaxY-newMinY)/2;
		float newCenterZ = d.getLastCenter().z;

		Point newCenter = new Point (newCenterX, newCenterY, newCenterZ );

		d.setLastCenter(newCenter);
		
		float [] temporaryModel = new float[16];
		Matrix.setIdentityM(temporaryModel, 0);
        Matrix.translateM(temporaryModel, 0, d.getLastCenter().x, d.getLastCenter().y, d.getLastCenter().z);  
        Matrix.scaleM(temporaryModel, 0, d.getLastScaleFactorX(), d.getLastScaleFactorY(), d.getLastScaleFactorZ());
        
        Matrix.translateM(temporaryModel, 0, 0, 0, d.getAdjustZ());

        //Object rotation  
        float [] rotateObjectMatrix = d.getRotationMatrix();

        //Multiply the model by the accumulated rotation
		float [] modelMatrix = new float[16];

        Matrix.multiplyMM(modelMatrix, 0, temporaryModel, 0,rotateObjectMatrix, 0); 
		d.setModelMatrix(modelMatrix);
	}
}
