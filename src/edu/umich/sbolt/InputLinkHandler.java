package edu.umich.sbolt;

import java.util.ArrayList;
import java.util.List;
import edu.umich.sbolt.world.*;

import sml.*;
import sml.Agent.RunEventInterface;

public class InputLinkHandler implements RunEventInterface
{
	public static InputLinkHandler Singleton(){
		return instance;
	}
	private static InputLinkHandler instance = null;
    // Root identifier for all messages the robot receives
    private Identifier inputLinkId;
    
    private boolean needToClearLGMessages = false;
    
    private Agent agent;

    public InputLinkHandler(Agent agent)
    {
    	instance = this;
    	this.agent = agent;
        inputLinkId = agent.GetInputLink();

        agent.RegisterForRunEvent(smlRunEventId.smlEVENT_BEFORE_INPUT_PHASE, this, null);
    }

    // Called right before the Agent's Input Phase,
    // Update the Input Link Here
    public void runEventHandler(int eventID, Object data, Agent agent, int phase)
    {    	
    	synchronized(InputLinkHandler.Singleton()){
            World.Singleton().updateInputLink(inputLinkId);
            SVSConnector.Singleton().updateSVS(agent);
            if(needToClearLGMessages){
            	clearLGMessages_internal();
            }

            if (agent.IsCommitRequired())
            {
                agent.Commit();
            }
    	}
    }
    
    public void clearLGMessages(){
    	needToClearLGMessages = true;
    }
    
    private void clearLGMessages_internal(){
    	Identifier lgID = WorkingMemoryUtil.getIdentifierOfAttribute(inputLinkId, "lg");
    	if(lgID != null){
    		List<WMElement> wmesToDestroy = new ArrayList<WMElement>();
    		int i = 0;
    		for(WMElement wme = lgID.GetChild(i); wme != null; wme = lgID.GetChild(++i)){
    			if(wme.GetAttribute().equals("sentence")){
    				wmesToDestroy.add(wme);
    			}
    		}
    		for(WMElement wme : wmesToDestroy){
    			wme.DestroyWME();
    		}
    	}
    	needToClearLGMessages = false;
    }
}
