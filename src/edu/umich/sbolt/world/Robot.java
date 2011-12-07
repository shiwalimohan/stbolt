package edu.umich.sbolt.world;

import sml.Identifier;
import abolt.lcmtypes.object_data_t;

public class Robot extends Object
{
    private boolean hasChanged;

    public Robot(object_data_t object)
    {
        super(object);
        hasChanged = false;
    }
    
    public Robot(String sensable){
        super(sensable);
    }

    @Override
    public synchronized void updateInputLink(Identifier parentIdentifier)
    {
        if(objectId == null){
            objectId = parentIdentifier.CreateIdWME("self");
            locationId = objectId.CreateIdWME("location");
        } 
        super.updateInputLink(parentIdentifier);
        hasChanged = false;
    }
    
    @Override
    public synchronized void newSensableString(String sensable){
        super.newSensableString(sensable.toLowerCase().replace("robot_pos=", ""));
        hasChanged = true;
    }

}
