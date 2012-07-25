package edu.umich.sbolt;

import sml.Agent;
import sml.Agent.RunEventInterface;
import sml.Identifier;
import sml.smlRunEventId;

import com.soartech.bolt.BOLTLGSupport;

import edu.umich.sbolt.world.SVSConnector;
import edu.umich.sbolt.world.World;

public class InputLinkHandler implements RunEventInterface
{
	public static InputLinkHandler Singleton(){
		return instance;
	}
	private static InputLinkHandler instance = null;
    // Root identifier for all messages the robot receives
    private Identifier inputLinkId;
    
    private boolean needToClearLGMessages = false;
    
    private BOLTLGSupport lgSupport;

    private Agent agent;

    public InputLinkHandler(Agent agent, BOLTLGSupport lgs)
    {
    	instance = this;
    	this.agent = agent;
        inputLinkId = agent.GetInputLink();

        lgSupport = lgs;
        agent.RegisterForRunEvent(smlRunEventId.smlEVENT_BEFORE_INPUT_PHASE, this, null);
    }

    // Called right before the Agent's Input Phase,
    // Update the Input Link Here
    public void runEventHandler(int eventID, Object data, Agent agent, int phase)
    {    	
    	synchronized(InputLinkHandler.Singleton()){
            World.Singleton().updateInputLink(inputLinkId);
            SVSConnector.Singleton().updateSVS(agent);
            if (agent.IsCommitRequired())
            {
                agent.Commit();
            }
    	}
    }
    
    public void clearLGMessages(){
    	lgSupport.clear();
        if (agent.IsCommitRequired())
        {
            agent.Commit();
        }
    }
}
