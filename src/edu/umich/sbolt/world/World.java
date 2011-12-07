package edu.umich.sbolt.world;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.umich.sbolt.InputLinkHandler;
import edu.umich.sbolt.controller.RobotPositionListener;

import abolt.lcmtypes.observations_t;
import sml.Identifier;

public class World implements InputLinkElement
{
    
    private Identifier parentId;
    
    private ObjectCollection objects;
    
    private Robot robot;
    
    private WorldTime worldTime;
    
    private Messages messages;
    
    private Set<InputLinkElement> inputLinkElements;
    private List<RobotPositionListener> positionListeners = new ArrayList<RobotPositionListener>();
    
    
    public World(){
        inputLinkElements = new HashSet<InputLinkElement>();
        
        objects = new ObjectCollection();
        
        robot = null;
        
        worldTime = new WorldTime();
        
        messages = new Messages(this);
        
        inputLinkElements.add(objects);
        inputLinkElements.add(worldTime);
        inputLinkElements.add(messages);
    }
    

    @Override
    public synchronized void updateInputLink(Identifier parentIdentifier)
    {
        for(InputLinkElement element : inputLinkElements){
            element.updateInputLink(parentIdentifier);
        }
    }

    @Override
    public synchronized void destroy()
    {
        for(InputLinkElement element : inputLinkElements){
            element.destroy();
        }
    }
    
    public synchronized void newObservation(observations_t observation){
        objects.newObservation(observation);
        for(String sensable : observation.sensables){
            if(sensable.toLowerCase().contains("robot_pos")){
                if(robot == null){
                    robot = new Robot(sensable);
                    inputLinkElements.add(robot);
                } else {
                    robot.newSensableString(sensable);
                }
                Location loc = robot.getLocation();
                for(RobotPositionListener listener : positionListeners){
                    listener.robotPositionChanged(loc.x, loc.y, loc.t);
                }
                break;
            }
        }
        worldTime.newObservation(observation);
    }
    
    public synchronized void newMessage(String message){
        messages.addMessage(message);
    }
    
    public synchronized Robot getRobot(){
        return robot;
    }
    
    public synchronized Object getObject(Integer id){
        return objects.getObject(id);
    }
    
    public synchronized Object getObject(String name){
        return objects.getObject(name);
    }
    
    public synchronized long getTime(){
        return worldTime.getMicroseconds();
    }
    
    public synchronized int getSteps(){
        return worldTime.getSteps();
    }
    
    
    public void addRobotPositionListener(RobotPositionListener listener) 
    {
        positionListeners.add(listener);
    }
}
