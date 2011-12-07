package edu.umich.sbolt.world;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages the ids of objects in the world
 * If an object has a name but no id, this can assign a unique one for it
 * 
 * @author mininger
 * 
 */
public class ObjectIdManager
{
    // Used in place of an invalid id
    public static Integer INVALID_ID = -1;
    
    // The Manager used to assign new ids
    public static ObjectIdManager Manager = new ObjectIdManager();
    
    // Mapping from names to ids
    private Map<String, Integer> objectIds;
    
    // The last Id that was assigned, higher numbers should be unassigned
    private int lastIdAssigned;
    
    public ObjectIdManager(){
        objectIds = new HashMap<String, Integer>();
        // Starts at 1000, assumes that there will be no more than 1000 objects in the world
        lastIdAssigned = 1000;
    }
    
    /**
     * Looks up the id associated with the given name
     * Assigns a new one if no one exists
     */
    public Integer getId(String name){
        Integer id = objectIds.get(name);
        if(id == null){
            lastIdAssigned++;
            objectIds.put(name, lastIdAssigned);
            return lastIdAssigned;
        }
        return id;
    }
}
