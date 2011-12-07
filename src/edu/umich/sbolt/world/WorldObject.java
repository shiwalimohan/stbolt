package edu.umich.sbolt.world;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.umich.sbolt.InputLinkHandler;
import edu.umich.sbolt.controller.RobotPositionListener;

import abolt.lcmtypes.object_data_t;
import sml.Identifier;
import sml.IntElement;
import sml.StringElement;
import sml.WMElement;

/**
 * A single Object in the world, can be created using either a sensable or an object_data_t
 * 
 * @author mininger
 * 
 */
public class WorldObject implements InputLinkElement
{
    public static String getSensableName(String sensable){
        // Matches against name=NAME followed by a comma, whitespace, or end of string
        // Assumes NAME consists of letters and underscores
        Pattern p = Pattern.compile("name=(\\p{Alpha}|_)+(,|\\s|\\Z)");
        Matcher m = p.matcher(sensable);
        if(!m.find()){
            return null;
        }
        // m.group() returns a string like "name=NAME,"
        // we trim, then split to get the actual NAME
        String[] name = m.group().trim().split("(name=)|,");
        if(name.length < 2){
            return null;
        }
        //Note that the first element will be the empty string, we want the second
        return name[1];
    }
    
    
    // Root identifier for the object
    protected Identifier objectId;
    
    // Identifier for the location WME
    protected Identifier locationId;
    
    // Name of the object (may be empty if not named)
    protected String name;
    
    // Id of the object 
    protected Integer id;
    
    // Location of the object in x,y,t
    protected Location location;
    
    // Attributes, value pairs of the object
    protected Map<String, String> attributes;

    
    public WorldObject(String sensable){
        initMembers();
        newSensableString(sensable);
    }
    
    public WorldObject(object_data_t object){
        initMembers();
        newObjectData(object);
    }
    
    private void initMembers(){
        objectId = null;
        locationId = null;
        
        name = "";
        id = ObjectIdManager.INVALID_ID;
        location = new Location();
        attributes = new HashMap<String, String>();
    }
    
    // Accessors
    public synchronized String getName(){
        return name;
    }
    
    public synchronized Integer getId(){
        return id;
    }
    
    public synchronized Location getLocation(){
        return location;
    }
    
    public synchronized String getValue(String attribute){
        return attributes.get(attribute);
    }
    

    // Mutators

    @Override
    public synchronized void updateInputLink(Identifier parentIdentifier)
    {
        if(objectId == null){
            objectId = parentIdentifier.CreateIdWME("object");
            locationId = objectId.CreateIdWME("location");
        } 

        WorkingMemoryUtil.updateFloatWME(locationId, "x", location.x);
        WorkingMemoryUtil.updateFloatWME(locationId, "y", location.y);
        WorkingMemoryUtil.updateFloatWME(locationId, "t", location.t);
        
        // Children of the object 
        Set<WMElement> elementsToDestroy = new HashSet<WMElement>();
        
        for (int i = 0; i < objectId.GetNumberChildren(); i++)
        {
            WMElement attributeWME = objectId.GetChild(i);
            String attribute = attributeWME.GetAttribute();
            if(attribute.equals("location")){
                continue;
            }
            
            if(!attributes.containsKey(attribute)){
                elementsToDestroy.add(attributeWME);
            }
        }     
        
        for(WMElement element : elementsToDestroy){
            element.DestroyWME();
        }
        
        for(Map.Entry<String, String> keyValPair : attributes.entrySet()){
            WorkingMemoryUtil.updateWME(objectId, keyValPair.getKey(), keyValPair.getValue());
        }
    }

    @Override
    public synchronized void destroy()
    {
        if(objectId != null){
            objectId.DestroyWME();
            objectId = null;
        }
    }
    
    public synchronized void newSensableString(String sensable){
        sensable = sensable.toLowerCase();
        String[] keyValPairs = sensable.split(",");

        Set<String> newAttributes = new HashSet<String>();
        
        for (String keyValPair : keyValPairs)
        {
            if(keyValPair.startsWith("[")){
                //Processing location information
                String[] pose = (keyValPair.substring(1, keyValPair.length() - 1)).split(" ");
                location.x = Double.parseDouble(pose[0]);
                location.y = Double.parseDouble(pose[1]);
                location.t = Double.parseDouble(pose[2]);
                continue;
            }
            
            String[] keyVal = keyValPair.split("=");
            if (keyVal.length < 2)
            {
                // Note a valid key-val pair, must be "key=value"
                continue;
            }

            if (keyVal[0].equals("id"))
            {
                id = Integer.parseInt(keyVal[1]);
            } else if(keyVal[0].equals("name")){
                name = keyVal[1];
            } 
            newAttributes.add(keyVal[0]);
            attributes.put(keyVal[0], keyVal[1]);
        }

        if (id == ObjectIdManager.INVALID_ID)
        {
            if(!name.isEmpty()){
                id = ObjectIdManager.Manager.getId(name);
            }
        }
        if (id != ObjectIdManager.INVALID_ID){
            newAttributes.add("id");
            attributes.put("id", id.toString());
        }
        
        removeUnusedAttributes(newAttributes);
    }

    public synchronized void newObjectData(object_data_t objectData){
        Set<String> newAttributes = new HashSet<String>();
        
        id = objectData.id;
        location.x = objectData.pos[0];
        location.y = objectData.pos[1];
        location.t = objectData.pos[2];
        
        attributes.put("id", String.valueOf(objectData.id));
        newAttributes.add("id");
        
        for (String value : objectData.nounjectives)
        {
            value = value.toLowerCase();
            String attribute = AttributeManager.Manager.getAttribute(value);
            if(attribute == null){
                attribute = "unknown";
            }
            newAttributes.add(attribute);
            attributes.put(attribute, value);
        }
        
        removeUnusedAttributes(newAttributes);
    }
    
    protected void removeUnusedAttributes(Set<String> newAttributes){
        Set<String> attributesToRemove = new HashSet<String>();
        for(Map.Entry<String, String> keyValPair : attributes.entrySet()){
            if(!newAttributes.contains(keyValPair.getKey())){
                attributesToRemove.add(keyValPair.getKey());
            }
        }
        for(String key : attributesToRemove){
            attributes.remove(key);
        }
    }
}
