package edu.umich.sbolt.world;

import abolt.lcmtypes.robot_action_t;
import sml.*;

public class RobotArm implements IInputLinkElement
{
	private Identifier selfId;
	// Identifier of the arm on the input link
	
	private IntElement grabbedId;
	
	private StringElement actionId;
	
	private StringElement prevActionId;
	
	private Pose pose;
	// Position of the arm
	
	private robot_action_t robotAction = null;
	// Last received information about the arm
	
	private boolean actionChanged = false;
	
	private int curGrab = -1;
	
	private String prevAction = "";
	

    public RobotArm(){
    	pose = new Pose();
    }
    
    public void pickup(int id){
    	actionChanged = true;
    	curGrab = id;
    }
    
    public int getGrabbedId(){
    	return curGrab;
    }


    @Override
    public synchronized void updateInputLink(Identifier parentIdentifier)
    {        
        if(!actionChanged){
            return;
        }
        
        if(selfId == null){
        	selfId = parentIdentifier.CreateIdWME("self");
        	grabbedId = selfId.CreateIntWME("grabbed-object", -1);
        	actionId = selfId.CreateStringWME("action", "wait");
        	prevActionId = selfId.CreateStringWME("prev-action", "wait");
        	pose.updateInputLink(selfId);
        }
        if(robotAction != null){
        	if(robotAction.obj_id != grabbedId.GetValue()){
            	grabbedId.Update(robotAction.obj_id);
        	}
        	if(!actionId.GetValue().equals(robotAction.action.toLowerCase())){
            	actionId.Update(robotAction.action.toLowerCase());
        	}
        	if(!prevActionId.GetValue().equals(prevAction.toLowerCase())){
        		prevActionId.Update(prevAction.toLowerCase());
        	}
        	pose.updateInputLink(selfId);
        }
        	

  //      grabbedId.Update(curGrab);


    	prevAction = (robotAction == null ? "wait" : robotAction.action);
        actionChanged = false;
    }

    @Override
    public synchronized void destroy()
    {
        if(selfId != null){
        	selfId.DestroyWME();
        	selfId = null;
        }
        pose.destroy();
        actionChanged = false;
    }
    
    public void newRobotAction(robot_action_t action){
    	robotAction = action;
    	pose.updateWithArray(action.xyz);
    	actionChanged = true;
    }
}
