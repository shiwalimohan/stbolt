package edu.umich.sbolt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import sml.Agent.OutputEventInterface;
import sml.Identifier;
import sml.WMElement;
import abolt.lcmtypes.robot_command_t;
import edu.umich.sbolt.controller.RobotDestinationListener;
import edu.umich.sbolt.world.Location;
import edu.umich.sbolt.world.WorldObject;

public class OutputLinkHandler implements OutputEventInterface
{
    
    private SBolt sbolt;
    private robot_command_t command;
    private List<RobotDestinationListener> destinationListeners = new ArrayList<RobotDestinationListener>();
    
    public OutputLinkHandler(SBolt sbolt){
        this.sbolt = sbolt;
        this.sbolt.getAgent().AddOutputHandler("stop", this, null);
        this.sbolt.getAgent().AddOutputHandler("grab-object", this, null);
        this.sbolt.getAgent().AddOutputHandler("goto", this, null);
        this.sbolt.getAgent().AddOutputHandler("drop-object", this, null);
        this.sbolt.getAgent().AddOutputHandler("send-message", this, null);
        this.sbolt.getAgent().AddOutputHandler("action", this, null);
        this.sbolt.getAgent().AddOutputHandler("destination", this, null);
        command = new robot_command_t();
        command.action = "";
    }
    
    public void addRobotDestinationListener(RobotDestinationListener listener) {
        destinationListeners.add(listener);
    }
    
    public robot_command_t getCommand(){
        return command;
    }
    
    @Override
    public void outputEventHandler(Object data, String agentName,
            String attributeName, WMElement wme)
    {
        if (!(wme.IsJustAdded() && wme.IsIdentifier()))
        {
            return;
        }

        if (wme.GetAttribute().equals("goto"))
        {
            processGoto(wme.ConvertToIdentifier());
        }
        else if (wme.GetAttribute().equals("action"))
        {
            processActionCommand(wme.ConvertToIdentifier());
        }
        else if (wme.GetAttribute().equals("send-message"))
        {
            processOutputLinkMessage(wme.ConvertToIdentifier());
        }
        else if (wme.GetAttribute().equals("destination"))
        {
            processDestinationCommand(wme.ConvertToIdentifier());
        }
        else if (wme.GetAttribute().equals("stop"))
        {
            processStopCommand(wme.ConvertToIdentifier());
        }
        else if (wme.GetAttribute().equals("grab-object"))
        {
            processGetObjectCommand(wme.ConvertToIdentifier());
        }
        else if (wme.GetAttribute().equals("drop-object"))
        {
            processDropObjectCommand(wme.ConvertToIdentifier());
        }

        if (this.sbolt.getAgent().IsCommitRequired())
        {
            this.sbolt.getAgent().Commit();
        }
    }

    private void processOutputLinkMessage(Identifier messageId)
    {
        if (messageId == null)
        {
            return;
        }

        if (messageId.GetNumberChildren() == 0)
        {
            messageId.CreateStringWME("status", "error");
            throw new IllegalStateException("Message has no children");
        }

        String message = "";
        WMElement wordsWME = messageId.FindByAttribute("first", 0);
        if (wordsWME == null || !wordsWME.IsIdentifier())
        {
            messageId.CreateStringWME("status", "error");
            throw new IllegalStateException("Message has no first attribute");
        }
        Identifier currentWordId = wordsWME.ConvertToIdentifier();

        // Follows the linked list down until it can't find the 'rest' attribute
        // of a WME
        while (currentWordId != null)
        {
            Identifier nextWordId = null;
            for (int i = 0; i < currentWordId.GetNumberChildren(); i++)
            {
                WMElement child = currentWordId.GetChild(i);
                if (child.GetAttribute().equals("word"))
                {
                    message += child.GetValueAsString() + " ";
                }
                else if (child.GetAttribute().equals("next")
                        && child.IsIdentifier())
                {
                    nextWordId = child.ConvertToIdentifier();
                }
            }
            currentWordId = nextWordId;
        }

        if (message == "")
        {
            messageId.CreateStringWME("status", "error");
            throw new IllegalStateException("Message was empty");
        }

        message += ".";
        sbolt.getChatFrame().addMessage(message.substring(0, message.length() - 1));
        messageId.CreateStringWME("status", "complete");
    }

    /**
     * Takes a destination command on the output link given as an identifier and
     * uses it to update the internal robot_command_t command
     */
    private void processDestinationCommand(Identifier destId)
    {
        if (destId == null)
        {
            return;
        }

        double x = 0.0;
        double y = 0.0;
        double t = 0.0;

        if (destId.FindByAttribute("None", 0) != null)
        {
            t = 10.0; // set t to 10 to ignore destination (stop)
        }
        else
        {
            WMElement xWme = destId.FindByAttribute("x", 0);
            WMElement yWme = destId.FindByAttribute("y", 0);
            WMElement tWme = destId.FindByAttribute("t", 0);

            if (xWme == null || yWme == null || tWme == null)
            {
                destId.CreateStringWME("status", "error");
                throw new IllegalStateException(
                        "Command has destination WME missing x, y, or t");
            }

            try
            {
                x = Double.valueOf(xWme.GetValueAsString());
                y = Double.valueOf(yWme.GetValueAsString());
                t = Double.valueOf(tWme.GetValueAsString());
            }
            catch (Exception e)
            {
                destId.CreateStringWME("status", "error");
                throw new IllegalStateException(
                        "Command has an invalid x, y, or t float");
            }
        }

        command.dest = new double[] { x, y, t };
        destId.CreateStringWME("status", "complete");
        
        for (RobotDestinationListener listener : destinationListeners) {
            listener.robotDestinationChanged(x, y, t);
        }
    }
    
    /**
     * Takes a stop command on the output link given as an identifier and
     * uses it to update the internal robot_command_t command
     */
    private void processStopCommand(Identifier stopId)
    {
        if (stopId == null)
        {
            return;
        }
        //command.dest = null;
        for (RobotDestinationListener listener : destinationListeners) {
            listener.setEStop(true);
        }
        stopId.CreateStringWME("status", "complete");
    }
    /**
     * Takes a grab-object command on the output link given as an identifier and
     * uses it to update the internal robot_command_t command
     */
    private void processGetObjectCommand(Identifier getId)
    {
        if (getId == null)
        {
            return;
        }
        /*
        for (RobotDestinationListener listener : destinationListeners) {
            listener.setAction(true);
        }
        */
        String action = "NAME=0,HELD=TRUE";
        command.action = action;//actionBuf.toString();
        getId.CreateStringWME("status", "complete");
    }

    /**
     * Takes a drop-object command on the output link given as an identifier and
     * uses it to update the internal robot_command_t command
     */
    private void processDropObjectCommand(Identifier dropId)
    {
        if (dropId == null)
        {
            return;
        }
        /*
        for (RobotDestinationListener listener : destinationListeners) {
            listener.setAction(true);
        }
        */
        
        String action = "NAME=0,HELD=FALSE";
        command.action = action;//actionBuf.toString();
        dropId.CreateStringWME("status", "complete");
    }
    
    /**
     * Takes a goto command on the output link given as an identifier and
     * uses it to update the internal robot_command_t command
     */
    private void processGoto(Identifier gotoId)
    {
        if (gotoId == null)
        {
            return;
        }
        
        
        //find the place in the inputlink map of objects
        WMElement placeWme = gotoId.FindByAttribute("place", 0);

        if (placeWme == null)
        {
            gotoId.CreateStringWME("status", "error");
            throw new IllegalStateException(
                    "Command has destination missing");
        }
        String name = placeWme.GetValueAsString();
        
        WorldObject place = sbolt.getWorld().getObject(name);
        if(place == null){
            gotoId.CreateStringWME("status", "error");
            return;
        }
        Location placeLoc = place.getLocation();

        double x = placeLoc.x;
        double y = placeLoc.y;
        double t = placeLoc.t;
        
        command.dest = new double[] { x, y, t };
        gotoId.CreateStringWME("status", "complete");

        for (RobotDestinationListener listener : destinationListeners) {
            listener.robotDestinationChanged(x, y, t);
        }
    }

    /**
     * Takes an action command on the output link given as an identifier and
     * uses it to update the internal robot_command_t command
     */
    private void processActionCommand(Identifier actionId)
    {
        if (actionId == null)
        {
            return;
        }
        
        /*
        StringBuffer actionBuf = new StringBuffer();

        int numPairs = 0;
        for (int i = 0; i < actionId.GetNumberChildren(); i++)
        {
            WMElement childWME = actionId.GetChild(i);
            if (!(childWME.GetAttribute().equals("pair") && childWME
                    .IsIdentifier()))
            {
                continue;
            }
            Identifier pairId = childWME.ConvertToIdentifier();

            // Get key of pair
            WMElement keyWME = pairId.FindByAttribute("key", 0);
            if (keyWME == null || keyWME.GetValueAsString().length() == 0)
            {
                actionId.CreateStringWME("status", "error");
                throw new IllegalStateException("Action has a pair with no key");
            }
            String key = keyWME.GetValueAsString();

            // Get value of pair
            WMElement valueWME = pairId.FindByAttribute("value", 0);
            if (valueWME == null || valueWME.GetValueAsString().length() == 0)
            {
                actionId.CreateStringWME("status", "error");
                throw new IllegalStateException(
                        "Action has a pair with no value");
            }
            String value = valueWME.GetValueAsString();

            actionBuf.append(key + "=" + value + ",");
            numPairs++;
        }

        if (numPairs == 0)
        {
            actionId.CreateStringWME("status", "error");
            throw new IllegalStateException("Action has no pairs");
        }
*/
        WMElement nameWME = actionId.FindByAttribute("name", 0);
        if (nameWME == null || nameWME.GetValueAsString().length() == 0)
        {
            actionId.CreateStringWME("status", "error");
            throw new IllegalStateException("Action has a pair with no key");
        }
        String name = nameWME.GetValueAsString();
        
        WMElement attributeWME = actionId.FindByAttribute("attribute", 0);
        if (attributeWME == null || attributeWME.GetValueAsString().length() == 0)
        {
            actionId.CreateStringWME("status", "error");
            throw new IllegalStateException("Action has a pair with no key");
        }
        String attribute = attributeWME.GetValueAsString();
        
        WMElement valueWME = actionId.FindByAttribute("value", 0);
        if (valueWME == null || valueWME.GetValueAsString().length() == 0)
        {
            actionId.CreateStringWME("status", "error");
            throw new IllegalStateException("Action has a pair with no key");
        }
        String value = valueWME.GetValueAsString();
        String action = "NAME=" + name + "," + attribute + "=" + value;
        command.action = action;//actionBuf.toString();
        
        actionId.CreateStringWME("status", "complete");
    }

}
