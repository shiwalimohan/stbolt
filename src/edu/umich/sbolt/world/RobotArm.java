package edu.umich.sbolt.world;

import java.util.ArrayList;
import java.util.List;

import abolt.lcmtypes.robot_action_t;

import edu.umich.sbolt.language.BOLTDictionary;
import edu.umich.sbolt.language.Parser;
import sml.Identifier;
import sml.IntElement;
import sml.WMElement;

public class RobotArm implements IInputLinkElement
{
	private Identifier selfId;
	// Identifier of the arm on the input link
	
	private IntElement grabbedId;
	
	private Pose pose;
	// Position of the arm
	
	private robot_action_t robotAction = null;
	// Last received information about the arm
	
	private boolean actionChanged = false;
	
	private int curGrab = -1;
	

    public RobotArm(){
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
        	grabbedId = selfId.CreateIntWME("grabbed-object", 1);
        	//pose.updateInputLink(selfId);
        }
        if(robotAction != null){
            if(robotAction.action.equals("DROP")){
            	curGrab = -1;
            }
            if(robotAction.obj_id != 0 && !robotAction.action.equals("GRABBING")){
            	//grabbedId.Update(robotAction.obj_id);
            } else {
            	//grabbedId.Update(-1);
            }
        }
        	
        grabbedId.Update(curGrab);
        
        
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
    	actionChanged = true;
    }
}
