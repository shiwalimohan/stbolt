package edu.umich.sbolt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.umich.sbolt.world.WorkingMemoryUtil;
import edu.umich.sbolt.world.World;

import sml.Agent;
import sml.Agent.RunEventInterface;
import sml.Identifier;
import sml.WMElement;
import sml.smlRunEventId;
import sml.Agent.OutputEventInterface;
import abolt.lcmtypes.object_data_t;
import abolt.lcmtypes.observations_t;

public class InputLinkHandler implements RunEventInterface
{
    // Root identifier for all messages the robot receives
    private Identifier inputLinkId;

    private World world;
    
    private boolean needToClearLGMessages = false;

    public InputLinkHandler(World world, SBolt sbolt)
    {

        inputLinkId = sbolt.getAgent().GetInputLink();

        sbolt.getAgent().RegisterForRunEvent(
                smlRunEventId.smlEVENT_BEFORE_INPUT_PHASE, this, null);

        this.world = world;
    }

    // Called right before the Agent's Input Phase,
    // Update the Input Link Here
    public void runEventHandler(int eventID, Object data, Agent agent, int phase)
    {
    	SBolt.lockInputLink();
        world.updateInputLink(inputLinkId);
        
        if(needToClearLGMessages){
        	clearLGMessages_internal();
        }

        if (agent.IsCommitRequired())
        {
            agent.Commit();
        }
        SBolt.unlockInputLink();
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
