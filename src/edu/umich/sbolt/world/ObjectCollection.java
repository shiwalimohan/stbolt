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
import sun.misc.Regexp;

/**
 * Represents the collection of objects in the world, and adds them to the input link
 * Combines both sensables and objects into a uniform WorldObject type
 * 
 * @author mininger
 * 
 */
public class ObjectCollection implements InputLinkElement
{
    // Root Identifier to add objects to
    private Identifier objectsId;
    
    // Mapping from object ids to objects
    private Map<Integer, WorldObject> objects;
    
    // True if a new observations_t has arrived since the last update
    private boolean hasChanged;
    
    public ObjectCollection(){
        objectsId = null;
        objects = new HashMap<Integer, WorldObject>();
        hasChanged = false;
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
            objectsId.DestroyWME();
            objectsId = null;
        }
    }
    
    public synchronized void newObservation(observations_t observation){
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
            if(sensable.contains("robot_pos")){
                //Ignore the robot sensable
                continue;
            }
            
            String name = WorldObject.getSensableName(sensable);
            if(name == null){
                continue;
            }
            
            Integer id = ObjectIdManager.Manager.getId(name);
            observedIds.add(id);
            WorldObject object = objects.get(id);
            if(object == null){
                object = new WorldObject(sensable);
                objects.put(id, object);
            } else {
                object.newSensableString(sensable);
            }   
        }
        
        Set<Integer> objectsToRemove = new HashSet<Integer>();
        for(Integer id : objects.keySet()){
            if(!observedIds.contains(id)){
                objectsToRemove.add(id);
            }
        }
        
        for(Integer id : objectsToRemove){
            objects.get(id).destroy();
            objects.remove(id);
        }
        hasChanged = true;
    }
    
    public synchronized WorldObject getObject(Integer id){
        return objects.get(id);
    }
    
    public synchronized WorldObject getObject(String name){
        Integer id = ObjectIdManager.Manager.getId(name);
        return objects.get(id);
    }
}
