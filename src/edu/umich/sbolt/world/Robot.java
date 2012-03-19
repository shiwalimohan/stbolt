package edu.umich.sbolt.world;

import sml.Identifier;
import abolt.lcmtypes.object_data_t;

public class Robot extends WorldObject
{
    private boolean hasChanged;
    
    public static boolean IsRobotSensable(String s){
        return s.toLowerCase().contains("name=bolt_robot");
    }

    public Robot(object_data_t object)
    {
        super(object);
        hasChanged = true;
    }
    
    public Robot(String sensable){
        super(sensable);
        hasChanged = true;
    }

    @Override
    public synchronized void updateInputLink(Identifier parentIdentifier)
    {
        if(objectId == null){
            objectId = parentIdentifier.CreateIdWME("self");
        } 
        if(hasChanged){
            super.updateInputLink(parentIdentifier);
        }
        hasChanged = false;
    }
    
    @Override
    public synchronized void newSensableString(String sensable){
        sensable = sensable.toLowerCase();
        sensable = sensable.replace("grab", "grabbed-object");
        sensable = sensable.replace("point", "pointing-at");
        super.newSensableString(sensable);
        hasChanged = true;
    }

}
