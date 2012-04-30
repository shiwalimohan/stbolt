package edu.umich.sbolt.world;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.umich.sbolt.InputLinkHandler;

import abolt.lcmtypes.observations_t;
import sml.Identifier;
import sml.Agent;
public class World implements IInputLinkElement
{
    
    private Identifier parentId;
    
    private ObjectCollection objects;
    
    private Robot robot;
    
    private WorldTime worldTime;
    
    private Messages messages;
    
    private PointedObject pointedObject;
    
    private Set<IInputLinkElement> inputLinkElements;
    

    private RobotArm robotArm;


    public World(){
        inputLinkElements = new HashSet<IInputLinkElement>();
        
        objects = new ObjectCollection(this);
        
        robot = null;

        worldTime = new WorldTime();
        
        messages = new Messages(this);
        
        pointedObject = new PointedObject(-1);
        
        robotArm = new RobotArm();
        
        inputLinkElements.add(objects);
        inputLinkElements.add(worldTime);
        inputLinkElements.add(messages);
        inputLinkElements.add(pointedObject);
        inputLinkElements.add(robotArm);
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
    
    public void destroyMessage(){
    	messages.destroy();
    }
    
    public void destroyMessage(Integer id){
    	if (messages.getIdNumber() == id){
    		messages.destroy();
    	}
    	else return;
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
    
    public RobotArm getRobotArm(){
    	return robotArm;
    }
    
    
    public synchronized void updateSVS(Agent agent)
    {
        WorldObject object;
        String s = "";
       
        Set<Integer> removeObjects = objects.getObjectsToRemove();
        for(Integer id : removeObjects){
            s+= "d " + id + "\n";
            System.out.println("d " + id + "\n");
        }
        if (!removeObjects.isEmpty())
        {
        	agent.SendSVSInput(s);
        	objects.clearObjectsToRemove();
        	return;
        }
        objects.clearObjectsToRemove();
        
        while ((object = objects.getNextChangedObject()) != null)
        {
            Pose pose = object.pose;

            //System.out.println("c " + object.getId() + " p " + pose.getX() + " " + 
            //            pose.getY() + " " + pose.getZ() + "\n");
            s+= "c " + object.getId() + " p " + pose.getX() + " " + 
                        pose.getY() + " " + pose.getZ() + "\n";

            
            //System.out.println("c " + object.getId() + " p " + pose.getX() + " " + pose.getY() + " " + pose.getZ() + "\n");
        }

        while ((object = objects.getNextNewObject()) != null)
        {
            Pose pose = object.pose;
            
            s+= "a " + object.getId() + " world v ";
            //System.out.println(object.getId());
            s+= object.getBBox().getFullPoints();
            System.out.println("a " + object.getId() + " world v " + object.getBBox().getFullPoints());
            s+= " p " + pose.getX() + " " + pose.getY() + " " + pose.getZ() + "\n";
        }
        agent.SendSVSInput(s);
    }
    
}
