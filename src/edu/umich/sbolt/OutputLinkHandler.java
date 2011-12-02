package edu.umich.sbolt;

import abolt.lcmtypes.robot_command_t;
import sml.Agent;
import sml.Identifier;
import sml.WMElement;
import sml.Agent.OutputEventInterface;

public class OutputLinkHandler implements OutputEventInterface
{
    
    private SBolt sbolt;
    private robot_command_t command;
    
    public OutputLinkHandler(SBolt sbolt){
        this.sbolt = sbolt;
        this.sbolt.getAgent().AddOutputHandler("command", this, null);
        this.sbolt.getAgent().AddOutputHandler("message", this, null);
        command = new robot_command_t();
        command.action = "";
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

        if (wme.GetAttribute().equals("command"))
        {
            processOutputLinkCommand(wme.ConvertToIdentifier());
        }
        else if (wme.GetAttribute().equals("message"))
        {
            processOutputLinkMessage(wme.ConvertToIdentifier());
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
        WMElement wordsWME = messageId.FindByAttribute("words", 0);
        if (wordsWME == null || !wordsWME.IsIdentifier())
        {
            messageId.CreateStringWME("status", "error");
            throw new IllegalStateException("Message has no words attribute");
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
                if (child.GetAttribute().equals("first-word"))
                {
                    message += child.GetValueAsString() + " ";
                }
                else if (child.GetAttribute().equals("rest")
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

    private void processOutputLinkCommand(Identifier commandId)
    {
        if (commandId == null || commandId.GetNumberChildren() == 0)
        {
            return;
        }

        int numChildren = commandId.GetNumberChildren();
        for (int i = 0; i < numChildren; ++i)
        {
            WMElement child = commandId.GetChild(i);

            if (child.GetAttribute().equals("action"))
            {
                processActionCommand(child.ConvertToIdentifier());
            }
            else if (child.GetAttribute().equals("destination"))
            {
                processDestinationCommand(child.ConvertToIdentifier());
            }
            else if (child.GetAttribute().equals("gripper"))
            {
                processGripperCommand(child.ConvertToIdentifier());
            }
        }
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

        command.action = actionBuf.toString();
        command.action = command.action.substring(0,
                command.action.length() - 1);
        actionId.CreateStringWME("status", "complete");
    }

    /**
     * Takes a gripper command on the output link given as an identifier and
     * uses it to update the internal robot_command_t command
     */
    private void processGripperCommand(Identifier gripperId)
    {
        if (gripperId == null)
        {
            return;
        }

        WMElement performWME = gripperId.FindByAttribute("perform", 0);
        if (performWME == null)
        {
            gripperId.CreateStringWME("status", "error");
            throw new IllegalStateException(
                    "Gripper command does not have a perform WME");

        }

        String gripperAction = performWME.GetValueAsString();
        if (!gripperAction.equals("open") && !gripperAction.equals("close"))
        {
            gripperId.CreateStringWME("status", "error");
            throw new IllegalStateException(
                    "Gripper command is not 'open' or 'close'");
        }

        command.gripper_open = gripperAction.equals("open");
        gripperId.CreateStringWME("status", "complete");
    }
}
