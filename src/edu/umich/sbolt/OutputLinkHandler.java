package edu.umich.sbolt;

import java.util.List;
import java.util.Set;

import sml.Agent.OutputEventInterface;
import sml.Identifier;
import sml.WMElement;
import abolt.lcmtypes.robot_command_t;
import edu.umich.sbolt.world.Robot;
import edu.umich.sbolt.world.WorkingMemoryUtil;
import edu.umich.sbolt.world.WorldObject;
import edu.umich.sbolt.language.AgentMessageParser;

public class OutputLinkHandler implements OutputEventInterface
{

    private SBolt sbolt;

    private robot_command_t command;

    public OutputLinkHandler(SBolt sbolt)
    {
        this.sbolt = sbolt;
        String[] outputHandlerStrings = { "goto", "action", "pick-up",
                "put-down", "point", "send-message", "query" };
        for (String outputHandlerString : outputHandlerStrings)
        {
            this.sbolt.getAgent().AddOutputHandler(outputHandlerString, this,
                    null);
        }

        command = new robot_command_t();
        command.action = "";
    }

    public robot_command_t getCommand()
    {
        if (command.updateDest)
        {
            // Check to see if we've reached our destination and turn off
            // updateDest flag
            Robot robot = sbolt.getWorld().getRobot();
            double x = robot.getPose().getX();
            double y = robot.getPose().getY();
            double z = robot.getPose().getZ();
            double delta = (x - command.dest[0]) * (x - command.dest[0])
                    + (y - command.dest[1]) * (y - command.dest[1])
                    + (z - command.dest[2]) * (z - command.dest[2]);
            if (delta < .01)
            {
                command.updateDest = false;
            }
        }
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
        System.out.println(wme.GetAttribute());

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
        else if (wme.GetAttribute().equals("pick-up"))
        {
            processPickUpCommand(wme.ConvertToIdentifier());
        }
        else if (wme.GetAttribute().equals("put-down"))
        {
            processPutDownCommand(wme.ConvertToIdentifier());
        }
        else if (wme.GetAttribute().equals("point"))
        {
            processPointCommand(wme.ConvertToIdentifier());
        }
        else if (wme.GetAttribute().equals("query"))
        {
            processQueryCommand(wme.ConvertToIdentifier());
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
        sbolt.getChatFrame().addMessage(
                message.substring(0, message.length() - 1));
        messageId.CreateStringWME("status", "complete");
    }

    /**
     * Takes a pick-up command on the output link given as an identifier and
     * uses it to update the internal robot_command_t command. Expects pick-up
     * ^object-id [int]
     */
    private void processPickUpCommand(Identifier pickUpId)
    {
        if (pickUpId == null)
        {
            return;
        }
        String objectIdStr = WorkingMemoryUtil.getValueOfAttribute(pickUpId,
                "object-id", "pick-up does not have an ^object-id attribute");

        String action = String.format("ID=%d,GRAB=%d", sbolt.getWorld()
                .getRobot().getId(), Integer.parseInt(objectIdStr));
        command.action = action;
        pickUpId.CreateStringWME("status", "complete");
    }

    /**
     * Takes a put-down command on the output link given as an identifier and
     * uses it to update the internal robot_command_t command Expects put-down
     * ^location <loc> <loc> ^x [float] ^y [float] ^z [float]
     */
    private void processPutDownCommand(Identifier putDownId)
    {
        if (putDownId == null)
        {
            return;
        }
        
        WMElement locationWME = putDownId.FindByAttribute("location", 0);
        if(locationWME == null){
            putDownId.CreateStringWME("status", "error");
            return;
        }
        
        String action;
        if(locationWME.IsIdentifier()){
            Identifier locationId = WorkingMemoryUtil.getIdentifierOfAttribute(
                    putDownId, "location",
                    "put-down does not have a ^location identifier");
            double x = Double.parseDouble(WorkingMemoryUtil.getValueOfAttribute(
                    locationId, "x",
                    "put-down.location does not have an ^x attribute"));
            double y = Double.parseDouble(WorkingMemoryUtil.getValueOfAttribute(
                    locationId, "y",
                    "put-down.location does not have an ^y attribute"));
            double z = Double.parseDouble(WorkingMemoryUtil.getValueOfAttribute(
                    locationId, "z",
                    "put-down.location does not have an ^z attribute"));

            action = String.format("ID=%d,DROP=[%f %f %f]", sbolt.getWorld()
                    .getRobot().getId(), x, y, z);
        } else {
            action = String.format("ID=%d,GRAB=-1", sbolt.getWorld().getRobot().getId());
        }
        
        command.action = action;
        putDownId.CreateStringWME("status", "complete");
    }

    /**
     * Takes a goto command on the output link given as an identifier and uses
     * it to update the internal robot_command_t command
     */
    private void processGoto(Identifier gotoId)
    {
        if (gotoId == null)
        {
            return;
        }

        Identifier locationId = WorkingMemoryUtil.getIdentifierOfAttribute(
                gotoId, "location",
                "put-down does not have a ^location identifier");
        double x = Double.parseDouble(WorkingMemoryUtil.getValueOfAttribute(
                locationId, "x",
                "put-down.location does not have an ^x attribute"));
        double y = Double.parseDouble(WorkingMemoryUtil.getValueOfAttribute(
                locationId, "y",
                "put-down.location does not have an ^y attribute"));
        double z = Double.parseDouble(WorkingMemoryUtil.getValueOfAttribute(
                locationId, "z",
                "put-down.location does not have an ^z attribute"));

        command.dest = new double[] { x, y, z, 0, 0, 0 };
        command.updateDest = true;
        gotoId.CreateStringWME("status", "complete");
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

        String id = WorkingMemoryUtil.getValueOfAttribute(actionId, "id",
                "action does not have an ^id attribute");
        String attribute = WorkingMemoryUtil.getValueOfAttribute(actionId,
                "attribute", "action does not have an ^attribute attribute");
        String value = WorkingMemoryUtil.getValueOfAttribute(actionId, "value",
                "action does not have a ^value attribute");

        String action = String.format("ID=%s,%s=%s", id, attribute, value);
        command.action = action.toUpperCase();

        actionId.CreateStringWME("status", "complete");
    }

    private void processPointCommand(Identifier pointId)
    {
        if (pointId == null)
        {
            return;
        }
        String objectIdStr = WorkingMemoryUtil.getValueOfAttribute(pointId,
                "object-id", "point does not have an ^object-id attribute");

        String action = String.format("ID=%d,POINT=%d", sbolt.getWorld()
                .getRobot().getId(), Integer.parseInt(objectIdStr));
        command.action = action;
        pointId.CreateStringWME("status", "complete");
    }

    private void processQueryCommand(Identifier queryId)
    {
        String type = WorkingMemoryUtil.getValueOfAttribute(queryId, "type",
                "Query does not have ^type");
        String message = "";
        message = AgentMessageParser.translateAgentMessage(queryId);
        if(!message.equals("")){
            sbolt.getChatFrame().addMessage(message);
        }
    }

    private void processQueryAttributeValue(Identifier queryId)
    {
        String attribute = WorkingMemoryUtil.getValueOfAttribute(queryId,
                "attribute", "Query does not have ^attribute");
        String object = WorkingMemoryUtil.getValueOfAttribute(queryId,
                "object", "Query does not have ^object");

        String message = "What is the " + attribute + " of " + object + "?";
        sbolt.getChatFrame().addMessage(message);
        queryId.CreateStringWME("status", "complete");
    }

    private void processQueryDiffersFromGroup(Identifier queryId)
    {
        String differentObject = WorkingMemoryUtil.getValueOfAttribute(queryId,
                "different-obj", "Query does not have ^different-obj");
        Set<String> exceptions = WorkingMemoryUtil.getAllValuesOfAttribute(
                queryId, "exception");
        Set<String> groupObjs = WorkingMemoryUtil.getAllValuesOfAttribute(
                queryId, "group-obj");

        String exceptionStr = "";
        if (exceptions.size() > 0)
        {
            exceptionStr = "Other than ";
            for(String exception : exceptions){
                exceptionStr += exception + ", ";
            }
            exceptionStr = exceptionStr.substring(0, exceptionStr.length() - 2);
            exceptionStr += "; ";
        }

        String message;
        if (groupObjs.size() == 1)
        {
            message = "in what attribute do " + groupObjs.iterator().next() + " and "
                    + differentObject + " differ?";
        }
        else
        {
            message = "what attribute do objects ";
            for (String obj : groupObjs)
            {
                message += obj + ", ";
            }
            message = message.substring(0, message.length() - 2);
            message += " have in common that is different than object "
                    + differentObject + "?";
        }
        sbolt.getChatFrame().addMessage(exceptionStr + message);
        queryId.CreateStringWME("status", "complete");
    }

    private void processQuerySharedAttVal(Identifier queryId)
    {
        Set<String> exceptions = WorkingMemoryUtil.getAllValuesOfAttribute(
                queryId, "exception");
        Set<String> groupObjs = WorkingMemoryUtil.getAllValuesOfAttribute(
                queryId, "group-obj");

        String exceptionStr = "";
        if (exceptions.size() > 0)
        {
            exceptionStr = "Other than ";
            for(String exception : exceptions){
                exceptionStr += exception + ", ";
            }
            exceptionStr = exceptionStr.substring(0, exceptionStr.length() - 2);
            exceptionStr += "; ";
        }

        String message;
        if (groupObjs.size() == 1)
        {
            message = "what attribute does " + groupObjs.iterator().next() + " have?";
        }
        else
        {
            message = "what attribute do objects ";
            for (String obj : groupObjs)
            {
                message += obj + ", ";
            }
            message = message.substring(0, message.length() - 2);
            message += " have in common?";
        }

        sbolt.getChatFrame().addMessage(exceptionStr + message);
        queryId.CreateStringWME("status", "complete");
    }
    
    private void processQueryTypeOfValue(Identifier queryId)
    {
        String value = WorkingMemoryUtil.getValueOfAttribute(queryId,
                "value", "Query does not have ^value");

        String message = String.format("What type of attribute does %s describe?", value);
        sbolt.getChatFrame().addMessage(message);
        queryId.CreateStringWME("status", "complete");
    }

}
