package edu.umich.sbolt.world;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.umich.sbolt.InputLinkHandler;

import abolt.lcmtypes.observations_t;
import sml.Identifier;

public class World implements IInputLinkElement
{
    
    private Identifier parentId;
    
    private ObjectCollection objects;
    
    private Robot robot;
    
    private WorldTime worldTime;
    
    private Messages messages;
    
    private PointedObject pointedObject;
    
    private Set<IInputLinkElement> inputLinkElements;
    
    public World(){
        inputLinkElements = new HashSet<IInputLinkElement>();
        
        objects = new ObjectCollection();
        
        robot = null;
        
        worldTime = new WorldTime();
        
        messages = new Messages(this);
        
        pointedObject = new PointedObject(-1);
        
        inputLinkElements.add(objects);
        inputLinkElements.add(worldTime);
        inputLinkElements.add(messages);
        inputLinkElements.add(pointedObject);
    }
    

    @Override
    public synchronized void updateInputLink(Identifier parentIdentifier)
    {
        for(IInputLinkElement element : inputLinkElements){
            element.updateInputLink(parentIdentifier);
        }
    }

    @Override
    public synchronized void destroy()
    {
        for(IInputLinkElement element : inputLinkElements){
            element.destroy();
        }
    }
    
    public synchronized void newObservation(observations_t observation){
        objects.newObservation(observation);
        for(String sensable : observation.sensables){
            if(Robot.IsRobotSensable(sensable)){
                if(robot == null){
                    robot = new Robot(sensable);
                    inputLinkElements.add(robot);
                } else {
                    robot.newSensableString(sensable);
                }
                break;
            }
        }
        worldTime.newObservation(observation);
        pointedObject.setObjectID(observation.click_id);
    }
    
    public synchronized void newMessage(String message){
        messages.addMessage(message);
    }
    
    public Robot getRobot(){
        return robot;
    }
    
    public int getPointedObjectID(){
        return pointedObject.getObjectID();
    }
    
    public WorldObject getObject(Integer id){
        return objects.getObject(id);
    }
    
    public long getTime(){
        return worldTime.getMicroseconds();
    }
    
    public int getSteps(){
        return worldTime.getSteps();
    }
}
