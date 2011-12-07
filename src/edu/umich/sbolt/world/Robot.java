package edu.umich.sbolt.world;

import sml.Identifier;
import abolt.lcmtypes.object_data_t;

public class Robot extends WorldObject
{
    private boolean hasChanged;
    
    private Location lastLocation;

    public Robot(object_data_t object)
    {
        super(object);
        hasChanged = true;
        lastLocation = new Location();
    }
    
    public Robot(String sensable){
        super(sensable);
        hasChanged = true;
        lastLocation = new Location();
    }

    @Override
    public synchronized void updateInputLink(Identifier parentIdentifier)
    {
        if(objectId == null){
            objectId = parentIdentifier.CreateIdWME("self");
            locationId = objectId.CreateIdWME("location");
        } 
        if(hasChanged){
            super.updateInputLink(parentIdentifier);
        }
        hasChanged = false;
    }
    
    @Override
    public synchronized void newSensableString(String sensable){
        super.newSensableString(sensable.toLowerCase().replace("robot_pos=", ""));
        
        if(lastLocation == null){
            lastLocation = new Location();
        }
        
        if(lastLocation.dist2(location) < .00001 && lastLocation.dTheta(location) < .001){
            attributes.put("stopped", "true");
        } else {
            attributes.put("stopped", "false");
        }
        
        
        lastLocation.set(location);
        hasChanged = true;
    }

}
