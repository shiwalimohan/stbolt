package edu.umich.sbolt.world;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import abolt.lcmtypes.object_data_t;
import abolt.lcmtypes.observations_t;
import sml.Identifier;

/**
 * Represents the collection of objects in the world, and adds them to the input link
 * Combines both sensables and objects into a uniform WorldObject type
 * 
 * @author mininger
 * 
 */
public class ObjectCollection implements IInputLinkElement
{
    // Root Identifier to add objects to
    private Identifier objectsId;
    
    // Mapping from object ids to objects
    private Map<Integer, WorldObject> objects;
    
    // True if a new observations_t has arrived since the last update
    private boolean hasChanged;
    
    Set<Integer> svsObjectsToRemove;
    
    private World world;
    
    public ObjectCollection(World world){
        objectsId = null;
        svsObjectsToRemove = new HashSet<Integer>();
        objects = new HashMap<Integer, WorldObject>();
        hasChanged = false;
        this.world = world;
    }
    

    @Override
    public synchronized void updateInputLink(Identifier parentIdentifier)
    {
        if(objectsId == null){
            objectsId = parentIdentifier.CreateIdWME("objects");
        }
        
        if(hasChanged){
            for(WorldObject object : objects.values()){
                object.updateInputLink(objectsId);
            }
        }
        hasChanged = false;
    }

    @Override
    public synchronized void destroy()
    {
        if(objectsId != null){
            for(WorldObject object : objects.values()){
                object.destroy();
            }
            objectsId.DestroyWME();
            objectsId = null;
        }
    }
    
    public synchronized void newObservation(observations_t observation){
    	Set<Integer> objectsToRemove = new HashSet<Integer>();
        Set<Integer> observedIds = new HashSet<Integer>();
        
        // update each object from the object_data_t
        for(object_data_t objectData : observation.observations){
            observedIds.add(objectData.id);
            WorldObject object = objects.get(objectData.id);
            if(object == null){
                object = new WorldObject(objectData);
                objects.put(objectData.id, object);
            } else {
                object.newObjectData(objectData);
            }   
        }
        
        // update each object from the sensables
        for(String sensable : observation.sensables){
            sensable = sensable.toLowerCase();
            
            Integer id = Integer.parseInt(WorldObject.getSensableId(sensable));
            if(id == null){
                continue;
            }
            WorldObject object = objects.get(id);
            observedIds.add(id);
            if(object == null){
                object = new WorldObject(sensable);
                objects.put(id, object);
            } else {
                object.newSensableString(sensable);
            }   
        }
        
        objectsToRemove.clear();
        for(Integer id : objects.keySet()){
            if(!observedIds.contains(id)){
                objectsToRemove.add(id);
            }
        }
        
        // don't remove the object currently being grabbed
        if(objectsToRemove.contains(world.getRobotArm().getGrabbedId())){
        	objectsToRemove.remove(world.getRobotArm().getGrabbedId());
        }
        
        for(Integer id : objectsToRemove){
            objects.get(id).destroy();
            objects.remove(id);
        }
        hasChanged = true;
        svsObjectsToRemove.addAll(objectsToRemove);
    }
    public Set<Integer> getObjectsToRemove()
    {     
        return svsObjectsToRemove;
    }
    
    //for SVS use
    public void clearObjectsToRemove()
    {     
    	svsObjectsToRemove.clear();
    }
    public synchronized WorldObject getObject(Integer id){
        return objects.get(id);
    }
    
    //svs added
    public synchronized WorldObject getNextNewObject(){
        for(WorldObject object : objects.values()){
            if (object.isNew)
            {
                object.isNew = false;
                return object;
            }
        }
        //else no new objects
        return null;
    }
    
    public synchronized WorldObject getNextChangedObject(){
        for(WorldObject object : objects.values()){
            if ((object.hasChanged) && (object.isNew == false))
            {
                object.hasChanged = false;
                return object;
            }
        }
        //else no changed objects
        return null; 
    }
    
}
