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

    private BoltAgent boltAgent;

    public InputLinkHandler(BoltAgent boltAgent, BOLTLGSupport lgs)
    {
    	instance = this;
    	this.boltAgent = boltAgent;
        inputLinkId = boltAgent.getSoarAgent().GetInputLink();

        lgSupport = lgs;
        boltAgent.getSoarAgent().RegisterForRunEvent(smlRunEventId.smlEVENT_BEFORE_INPUT_PHASE, this, null);
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
    	boltAgent.commitChanges();
    }
}
