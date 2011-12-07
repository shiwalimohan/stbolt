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

public class ObjectCollection implements InputLinkElement
{
    private Identifier objectsId;
    
    private Map<Integer, Object> objects;
    
    private boolean hasChanged;
    
    public ObjectCollection(){
        objectsId = null;
        objects = new HashMap<Integer, Object>();
        hasChanged = false;
    }
    

    @Override
    public synchronized void updateInputLink(Identifier parentIdentifier)
    {
        if(objectsId == null){
            objectsId = parentIdentifier.CreateIdWME("objects");
        }
        
        for(Object object : objects.values()){
            object.updateInputLink(objectsId);
        }
        hasChanged = false;
    }

    @Override
    public synchronized void destroy()
    {
        if(objectsId != null){
            objectsId.DestroyWME();
        }
        objectsId = null;
    }
    
    public synchronized void newObservation(observations_t observation){
        hasChanged = true;
        Set<Integer> observedIds = new HashSet<Integer>();
        
        for(object_data_t objectData : observation.observations){
            observedIds.add(objectData.id);
            Object object = objects.get(objectData.id);
            if(object == null){
                object = new Object(objectData);
                objects.put(objectData.id, object);
            } else {
                object.newObjectData(objectData);
            }   
        }
        
        for(String sensable : observation.sensables){
            sensable = sensable.toLowerCase();
            if(sensable.contains("robot_pos")){
                continue;
            }
            
            String name = Object.getSensableName(sensable);
            if(name == null){
                continue;
            }
            
            Integer id = ObjectIdManager.Manager.getId(name);
            observedIds.add(id);
            Object object = objects.get(id);
            if(object == null){
                object = new Object(sensable);
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
    }
    
    public synchronized Object getObject(Integer id){
        return objects.get(id);
    }
    
    public synchronized Object getObject(String name){
        Integer id = ObjectIdManager.Manager.getId(name);
        return objects.get(id);
    }
}
