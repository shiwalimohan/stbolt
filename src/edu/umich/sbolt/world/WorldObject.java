package edu.umich.sbolt.world;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import abolt.lcmtypes.*;
import sml.*;

/**
 * A single Object in the world, can be created using either a sensable or an object_data_t
 * 
 * @author mininger
 * 
 */
public class WorldObject implements IInputLinkElement
{    
    
    // Root identifier for the object
    protected Identifier objectId;
    
    // Name of the object (may be null if not named)
    protected String name;
    protected StringElement nameWME;
    
    // Id of the object 
    protected Integer id;
    protected IntElement idWME;
    
    // Pose of the object
    protected Pose pose;
    
    // Bounding box of the object
    protected BBox bbox;
    
    protected Map<String, PerceptualProperty> perceptualProperties;
    
    protected Map<String, StateProperty> stateProperties;

     // svs added
    protected boolean isNew;
    protected boolean hasChanged;
    
    
    public WorldObject(object_data_t object){
    	objectId = null;
        idWME = null;
        nameWME = null;
        
        name = null;
        id = -1;
        pose = new Pose();
        bbox = new BBox();
        stateProperties = new HashMap<String, StateProperty>();
        perceptualProperties = new HashMap<String, PerceptualProperty>();
        isNew = true;
        
        newObjectData(object);
    }
    
    // Accessors
    public String getName(){
        return name;
    }
    
    public Integer getId(){
        return id;
    }
    
    public Pose getPose(){
        return pose;
    }
    
    public BBox getBBox(){
    	return bbox;
    }
    
    public synchronized String getValue(String attribute){
        return stateProperties.get(attribute).getValue();
    }
    

    // Mutators

    @Override
    public synchronized void updateInputLink(Identifier parentIdentifier)
    {
        if(objectId == null){
            objectId = parentIdentifier.CreateIdWME("object");
            idWME = objectId.CreateIntWME("id", id);
        } 
        if(name != null){
        	if(nameWME == null){
        		nameWME = objectId.CreateStringWME("name", name);
        	}
        	if(!nameWME.GetValueAsString().equals(name)){
        		nameWME.Update(name);
        	}
        }
        
        for(PerceptualProperty category : perceptualProperties.values()){
        	category.updateInputLink(objectId);
        }
        
        pose.updateInputLink(objectId);
        bbox.updateInputLink(objectId);
        
        for(StateProperty prop : stateProperties.values()){
        	prop.updateInputLink(objectId);
        }
    }

    @Override
    public synchronized void destroy()
    {
        if(objectId != null){
        	for(PerceptualProperty prop : perceptualProperties.values()){
        		prop.destroy();
        	}
        	pose.destroy();
        	bbox.destroy();
        	idWME.DestroyWME();
        	idWME = null;
        	if(nameWME != null){
            	nameWME.DestroyWME();
            	nameWME = null;
        	}
            objectId.DestroyWME();
            objectId = null;
        }
    }
    
    public void updateProperty(String name, String value){
    	name = name.toLowerCase();
    	value = value.toLowerCase();
    	if(stateProperties.containsKey(name)){
    		stateProperties.get(name).update(value);
    	} else {
    		stateProperties.put(name, new StateProperty(name, value));
    	}
    }

    public synchronized void newObjectData(object_data_t objectData){        
        id = objectData.id;
        
        //used for svs
        if (!pose.equals(objectData.pos))
           hasChanged = true;
        pose.updateWithArray(objectData.pos);
        bbox.updateWithArray(objectData.bbox);
        
        HashSet<String> propertiesToRemove = new HashSet<String>();
        for(String propName : perceptualProperties.keySet()){
        	propertiesToRemove.add(propName);
        }
        
        for(categorized_data_t category : objectData.cat_dat){
        	String propName = PerceptualProperty.getCategoryName(category.cat.cat);
        	if(perceptualProperties.containsKey(propName)){
        		perceptualProperties.get(propName).updateCategoryInfo(category);
        		propertiesToRemove.remove(propName);
        	} else {
        		perceptualProperties.put(propName, new PerceptualProperty(category));
        	}
        }
        
        for(String label : objectData.labels){
        	String[] split = label.toLowerCase().split("=");
        	if(split[0].equals("name")){
        		name = split[1];
        	} else {
        		updateProperty(split[0], split[1]);
        	}
        }
        
        for(String propName : propertiesToRemove){
        	perceptualProperties.get(propName).destroy();
        	perceptualProperties.remove(propName);
        }
    }
}
