package edu.umich.sbolt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.umich.sbolt.controller.RobotPositionListener;
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
    
    public InputLinkHandler(World world, SBolt sbolt){
        
        inputLinkId = sbolt.getAgent().GetInputLink();
        
        sbolt.getAgent().RegisterForRunEvent(smlRunEventId.smlEVENT_BEFORE_INPUT_PHASE,
                this, null);
        
        this.world = world;
    }

    // Called right before the Agent's Input Phase,
    // Update the Input Link Here
    public void runEventHandler(int eventID, Object data, Agent agent, int phase)
    {
        world.updateInputLink(inputLinkId);
        
        if (agent.IsCommitRequired())
        {
            agent.Commit();
        }
    }
}
