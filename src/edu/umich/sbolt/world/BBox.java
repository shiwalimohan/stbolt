package edu.umich.sbolt.world;

import sml.FloatElement;
import sml.Identifier;

public class BBox implements IInputLinkElement
{
	private static String[] wmeStrings = {"x", "y", "z"};
	private static String[] typeStrings = {"min", "max"};
    private enum ElementIndex{X, Y, Z};
    private enum TypeIndex{MIN, MAX};
    private double[][] bounds;
    private Identifier[] typeIDs;
    private FloatElement[][] wmes;
    private Identifier bboxID;
    
    public BBox(){     
        initBBox();
    	for(int i = 0; i < 3; i++){
    		for(int j = 0; j < 2; j++){
    			bounds[i][j] = 0;
    		}
    	}
    }
    
    public BBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ){     
        initBBox();
    	bounds[ElementIndex.X.ordinal()][TypeIndex.MIN.ordinal()] = minX;
    	bounds[ElementIndex.Y.ordinal()][TypeIndex.MIN.ordinal()] = minX;
    	bounds[ElementIndex.Z.ordinal()][TypeIndex.MIN.ordinal()] = minX;
    	bounds[ElementIndex.X.ordinal()][TypeIndex.MAX.ordinal()] = minX;
    	bounds[ElementIndex.Y.ordinal()][TypeIndex.MAX.ordinal()] = minX;
    	bounds[ElementIndex.Z.ordinal()][TypeIndex.MAX.ordinal()] = minX;
    }
    
    public BBox(double[][] boundsValues){     
        initBBox();
        updateWithArray(boundsValues);
    }
    
    public BBox(String s){      
        initBBox();
        updateWithString(s);            
    }
    
    private void initBBox(){
    	wmes = null;
    	typeIDs = null;
    	bboxID = null;
    	bounds = new double[3][2];
    }
    
    public double getMinX(){
        return bounds[ElementIndex.X.ordinal()][TypeIndex.MIN.ordinal()];
    }
    public void setMinX(double minX){
    	bounds[ElementIndex.X.ordinal()][TypeIndex.MIN.ordinal()] = minX;
    }
    

    public double getMinY(){
        return bounds[ElementIndex.Y.ordinal()][TypeIndex.MIN.ordinal()];
    }
    public void setMinY(double minY){
    	bounds[ElementIndex.Y.ordinal()][TypeIndex.MIN.ordinal()] = minY;
    }
    

    public double getMinZ(){
        return bounds[ElementIndex.Z.ordinal()][TypeIndex.MIN.ordinal()];
    }
    public void setMinZ(double minZ){
    	bounds[ElementIndex.Z.ordinal()][TypeIndex.MIN.ordinal()] = minZ;
    }
    

    public double getMaxX(){
        return bounds[ElementIndex.X.ordinal()][TypeIndex.MAX.ordinal()];
    }
    public void setMaxX(double MaxX){
    	bounds[ElementIndex.X.ordinal()][TypeIndex.MAX.ordinal()] = MaxX;
    }
    

    public double getMaxY(){
        return bounds[ElementIndex.Y.ordinal()][TypeIndex.MAX.ordinal()];
    }
    public void setMaxY(double MaxY){
    	bounds[ElementIndex.Y.ordinal()][TypeIndex.MAX.ordinal()] = MaxY;
    }
    

    public double getMaxZ(){
        return bounds[ElementIndex.Z.ordinal()][TypeIndex.MAX.ordinal()];
    }
    public void setMaxZ(double MaxZ){
    	bounds[ElementIndex.Z.ordinal()][TypeIndex.MAX.ordinal()] = MaxZ;
    }
    
    
    public void updateWithString(String s){
        s = s.replace("[", "");
        s = s.replace("]", "");
        String[] poseInfo = s.split(" ");
        if(poseInfo[0].equals("")){
            poseInfo = new String[0];
        }
        
        for(int i = 0; i < 6; i++){
            if(poseInfo.length <= i){
                bounds[i%3][i/3] = 0;
            } else {
            	bounds[i%3][i/3] = Double.parseDouble(poseInfo[i].trim());
            }
        }         
    }
    
    public void updateWithArray(double[][] boundsInfo){
    	for(int i = 0; i < 3; i++){
    		for(int j = 0; j < 2; j++){
    			if(boundsInfo.length <= i || boundsInfo[i].length <= j){
    				bounds[i][j] = 0;
    			} else {
    				bounds[i][j] = boundsInfo[i][j];
    			}
    		}
    	}    
    }
    
    @Override
    public String toString(){
        return String.format("[%f %f %f %f %f %f]", getMinX(), getMinY(), getMinZ(), getMaxX(), getMaxY(), getMaxZ());
    }
    

    // update the input-link appropriately
    @Override
    public synchronized void updateInputLink(Identifier parentIdentifier){
        if(bboxID == null){
            // Create the pose on the input link
            bboxID = parentIdentifier.CreateIdWME("bbox");
            typeIDs = new Identifier[2];
            wmes = new FloatElement[3][2];
            for(int i = 0; i < 2; i++){
            	typeIDs[i] = bboxID.CreateIdWME(typeStrings[i]);
            	for(int j = 0; j < 3; j++){
            		wmes[j][i] = typeIDs[i].CreateFloatWME(wmeStrings[j], bounds[j][i]);
            	}
            }
        } else {
            // Update the pose on the input link
        	for(int i = 0; i < 3; i++){
        		for(int j = 0; j < 2; j++){
        			if(wmes[i][j].GetValue() != bounds[i][j]){
                        wmes[i][j].Update(bounds[i][j]);
                    }
        		}
        	}
        }
    }
    
    // remove the object from the input-link
    @Override
    public synchronized void destroy(){
        if(wmes != null){
        	for(int i = 0; i < 3; i++){
        		for(int j = 0; j < 2; j++){
        			wmes[i][j].DestroyWME();
        		}
        		typeIDs[i].DestroyWME();
        	}
        	bboxID.DestroyWME();
        	
        	wmes = null;
        	typeIDs = null;
        	bboxID = null;
        }
    }
}
