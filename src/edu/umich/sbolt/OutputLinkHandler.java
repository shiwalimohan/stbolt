package edu.umich.sbolt;

import java.util.*;

import com.soartech.bolt.testing.ActionType;

import sml.*;
import sml.Agent.OutputEventInterface;
import sml.Agent.RunEventInterface;

import abolt.lcmtypes.*;
import april.util.TimeUtil;

import edu.umich.sbolt.world.*;
import edu.umich.sbolt.language.AgentMessageParser;
import edu.umich.sbolt.language.Patterns.LingObject;

public class OutputLinkHandler implements OutputEventInterface, RunEventInterface
{
	
    private List<training_label_t> newLabels;
    
    private HashMap<Integer, Identifier> waitingAcks = new HashMap<Integer, Identifier>();
    
    private List<Integer> newAcks = new ArrayList<Integer>();
    
    private List<Integer> receivedAcks = new ArrayList<Integer>();
    
    private int nextAckNum = 1;

    public OutputLinkHandler(BoltAgent boltAgent)
    {
        String[] outputHandlerStrings = { "message", "action", "pick-up", "push-segment", "pop-segment",
                "put-down", "point", "send-message","remove-message","send-training-label", "set-state", 
                "report-interaction", "home"};

        for (String outputHandlerString : outputHandlerStrings)
        {
        	boltAgent.getSoarAgent().AddOutputHandler(outputHandlerString, this, null);
        }
        
        boltAgent.getSoarAgent().RegisterForRunEvent(
                smlRunEventId.smlEVENT_AFTER_OUTPUT_PHASE, this, null);
        
        newLabels = new ArrayList<training_label_t>();
    }
    
    public void runEventHandler(int eventID, Object data, Agent agent, int phase)
    {
    	Identifier outputLink = agent.GetOutputLink();
    	if(outputLink != null){
        	WMElement waitingWME = outputLink.FindByAttribute("waiting", 0);
        	ChatFrame.Singleton().setReady(waitingWME != null);
    	}
    	if(newLabels.size() > 0){
        	SBolt.broadcastTrainingData(newLabels);
        	newLabels.clear();
    	}
    	synchronized(receivedAcks){
        	for(Integer i : receivedAcks){
        		if(waitingAcks.containsKey(i)){
        			System.out.println("I GOT AN ACK!!!!" + i);
        			waitingAcks.get(i).CreateStringWME("status", "complete");
        			waitingAcks.remove(i);
        		}
        	}
        	receivedAcks.clear();
    	}
    }
    
    public void getAcks(Integer i){
    	synchronized(receivedAcks){
    		receivedAcks.add(i);
    	}
    }
    
    public List<training_label_t> extractNewLabels(){
    	if(newLabels.size() == 0){
    		return null;
    	} else {
    		List<training_label_t> retVal = newLabels;
    		newLabels = new ArrayList<training_label_t>();
    		return retVal;
    	}
    }
    
    public training_data_t getTrainingData(){
    	if(newLabels.size() == 0){
    		return null;
    	}
    	training_data_t trainingData = new training_data_t();
    	trainingData.utime = TimeUtil.utime();
    	trainingData.num_labels = newLabels.size();
    	trainingData.labels = new training_label_t[newLabels.size()];
    	for(int i = 0; i < newLabels.size(); i++){
    		trainingData.labels[i] = newLabels.get(i);
    	}
    	String acks = "";
    	for(Integer i : newAcks){
    		if(!acks.isEmpty()){
    			acks += ",";
    		}
    		acks += i;
    	}
    	trainingData.ack_nums = acks;
    	newLabels.clear();
    	newAcks.clear();
    	return trainingData;
    }

    @Override
    public void outputEventHandler(Object data, String agentName,
            String attributeName, WMElement wme)
    {
    	synchronized(this){
    		if (!(wme.IsJustAdded() && wme.IsIdentifier()))
            {
                return;
            }
    		Identifier id = wme.ConvertToIdentifier();
            System.out.println(wme.GetAttribute());
            

            try{
	            if (wme.GetAttribute().equals("set-state"))
	            {
	                processSetCommand(id);
	            } 
	            else if(wme.GetAttribute().equals("message")) 
	            {
	            	processMessage(id);
	            }
	            else if (wme.GetAttribute().equals("send-message"))
	            {
	                processOutputLinkMessage(id);
	            }
	            else if (wme.GetAttribute().equals("pick-up"))
	            {
	                processPickUpCommand(id);
	            }
	            else if (wme.GetAttribute().equals("put-down"))
	            {
	                processPutDownCommand(id);
	            }
	            else if (wme.GetAttribute().equals("point"))
	            {
	                processPointCommand(id);
	            }
	            else if (wme.GetAttribute().equals("remove-message"))
	            {
	            	processRemoveMesageCommand(id);
	            }
	            else if(wme.GetAttribute().equals("send-training-label"))
	            {
	            	processSendTrainingLabelCommand(id);
	            } 
	            else if(wme.GetAttribute().equals("push-segment"))
	            {
	            	processPushSegmentCommand(id);
	            } 
	            else if(wme.GetAttribute().equals("pop-segment"))
	            {
	            	processPopSegmentCommand(id);
	            }
	            else if(wme.GetAttribute().equals("report-interaction")){
	            	processReportInteraction(id);
	            } else if(wme.GetAttribute().equals("home")){
	            	processHomeCommand(id);
	            }
	            SBolt.Singleton().getBoltAgent().commitChanges();
            } catch (IllegalStateException e){
            	System.out.println(e.getMessage());
            }
    	}
    }
    
    private void processMessage(Identifier messageId) {
        Identifier cur = WMUtil.getIdentifierOfAttribute(messageId, "first");
        String msg = "";
        while(cur != null) {
        	String word = WMUtil.getValueOfAttribute(cur, "value");
        	if(word.equals(".") || word.equals("?") || word.equals("!") || word.equals(")"))
        		msg += word;
        	else if(msg.equals(""))
        		msg += word;
        	else
        		msg += " "+word;
        	cur = WMUtil.getIdentifierOfAttribute(cur, "next");
        }
        
        ChatFrame.Singleton().addMessage(msg, ActionType.Agent);
    }

	private void processRemoveMesageCommand(Identifier messageId) {
		int id = Integer.parseInt(WMUtil.getValueOfAttribute(messageId, "id", "Error (remove-message): No id"));
		World.Singleton().destroyMessage(id);
		messageId.CreateStringWME("status", "complete");
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
        
        if(WMUtil.getIdentifierOfAttribute(messageId, "first") == null){
        	processAgentMessageStructureCommand(messageId);
        } else {
        	processAgentMessageStringCommand(messageId);
        }
    }
	
    private void processAgentMessageStructureCommand(Identifier messageId)
    {
        String type = WMUtil.getValueOfAttribute(messageId, "type",
                "Message does not have ^type");
        String message = "";
        message = AgentMessageParser.translateAgentMessage(messageId);
        if(message != null && !message.equals("")){
            ChatFrame.Singleton().addMessage(message, ActionType.Agent);
        }
        messageId.CreateStringWME("status", "complete");
    }
	
	private void processAgentMessageStringCommand(Identifier messageId){

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
        ChatFrame.Singleton().addMessage(message.substring(0, message.length() - 1), ActionType.Agent);

        messageId.CreateStringWME("status", "complete");
    }

    /**
     * Takes a pick-up command on the output link given as an identifier and
     * uses it to update the internal robot_command_t command. Expects pick-up
     * ^object-id [int]
     */
    private void processPickUpCommand(Identifier pickUpId)
    {
        String objectIdStr = WMUtil.getValueOfAttribute(pickUpId,
                "object-id", "pick-up does not have an ^object-id attribute");
        
        robot_command_t command = new robot_command_t();
        command.utime = TimeUtil.utime();
        command.action = String.format("GRAB=%d", Integer.parseInt(objectIdStr));
        command.dest = new double[6];
        SBolt.broadcastRobotCommand(command);
        pickUpId.CreateStringWME("status", "complete");
    }

    /**
     * Takes a put-down command on the output link given as an identifier and
     * uses it to update the internal robot_command_t command Expects put-down
     * ^location <loc> <loc> ^x [float] ^y [float] ^z [float]
     */
    private void processPutDownCommand(Identifier putDownId)
    {
        Identifier locationId = WMUtil.getIdentifierOfAttribute(
                putDownId, "location",
                "Error (put-down): No ^location identifier");
        double x = Double.parseDouble(WMUtil.getValueOfAttribute(
                locationId, "x", "Error (put-down): No ^location.x attribute"));
        double y = Double.parseDouble(WMUtil.getValueOfAttribute(
                locationId, "y", "Error (put-down): No ^location.y attribute"));
        double z = Double.parseDouble(WMUtil.getValueOfAttribute(
                locationId, "z", "Error (put-down): No ^location.z attribute"));
        robot_command_t command = new robot_command_t();
        command.utime = TimeUtil.utime();
        command.action = "DROP";
        command.dest = new double[]{x, y, z, 0, 0, 0};
        SBolt.broadcastRobotCommand(command);
        putDownId.CreateStringWME("status", "complete");
    }

    /**
     * Takes a set-state command on the output link given as an identifier and
     * uses it to update the internal robot_command_t command
     */
    private void processSetCommand(Identifier id)
    {
        String objId = WMUtil.getValueOfAttribute(id, "id",
                "Error (set-state): No ^id attribute");
        String name = WMUtil.getValueOfAttribute(id,
                "name", "Error (set-state): No ^name attribute");
        String value = WMUtil.getValueOfAttribute(id, "value",
                "Error (set-state): No ^value attribute");

        String action = String.format("ID=%s,%s=%s", objId, name, value);
        robot_command_t command = new robot_command_t();
        command.utime = TimeUtil.utime();
        command.action = action;
        command.dest = new double[6];
        SBolt.broadcastRobotCommand(command);

        id.CreateStringWME("status", "complete");
    }

    private void processPointCommand(Identifier pointId)
    {
        Identifier poseId = WMUtil.getIdentifierOfAttribute(pointId, "pose",
        		"Error (point): No ^pose identifier");
        String x = WMUtil.getValueOfAttribute(poseId, "x",
        		"Error (point): No ^pose.x identifier");
        String y = WMUtil.getValueOfAttribute(poseId, "y",
        		"Error (point): No ^pose.y identifier");
        String z = WMUtil.getValueOfAttribute(poseId, "z",
        		"Error (point): No ^pose.z identifier");
        
        robot_command_t command = new robot_command_t();
        command.utime = TimeUtil.utime();
        command.dest = new double[]{Double.parseDouble(x), Double.parseDouble(y), Double.parseDouble(z), 0, 0, 0};
    	command.action = "POINT";
    	SBolt.broadcastRobotCommand(command);
        
        pointId.CreateStringWME("status", "complete");
    }
    
    private void processSendTrainingLabelCommand(Identifier id){
    	String label = WMUtil.getValueOfAttribute(id, "label", 
    			"Error (send-training-label): No ^label attribute");
    	String category = WMUtil.getValueOfAttribute(id, "category", 
    			"Error (send-training-label): No ^category attribute");
    	
    	String objId = WMUtil.getValueOfAttribute(id, "id");
    	Identifier fId = WMUtil.getIdentifierOfAttribute(id, "features");
    	
    	training_label_t newLabel = new training_label_t();
    	newLabel.label = label;
    	newLabel.cat = new category_t();
    	Integer catID = PerceptualProperty.getCategoryID(category);
    	if(catID == null){
    		id.CreateStringWME("status", "error");
    		return;
    	} else {
    		newLabel.cat.cat = catID;
    	}
    	
    	if(objId != null){
    		newLabel.id = Integer.parseInt(objId);
    		newLabel.num_features = 0;
    		newLabel.features = new double[0];
    	} else if(fId != null){
    		newLabel.id = -1;
    		ArrayList<Double> features = new ArrayList<Double>();
    		for(int i = 0; i < fId.GetNumberChildren(); i++){
    			WMElement childWME = fId.GetChild(i);
    			if(!childWME.IsIdentifier() || !childWME.GetAttribute().equals("feature")){
    				continue;
    			}
    			Identifier childId = childWME.ConvertToIdentifier();
    			Integer index = Integer.parseInt(childId.FindByAttribute("index", 0).GetValueAsString());
    			double val = Double.parseDouble(childId.FindByAttribute("value", 0).GetValueAsString());
    			features.add(index, val);
    		}
    		newLabel.num_features = features.size();
    		newLabel.features = new double[newLabel.num_features];
    		for(int i = 0; i < features.size(); i++){
    			newLabel.features[i] = features.get(i);
    		}
    	} else {
    		id.CreateStringWME("status", "error");
    		return;
    	}
    	
    	newLabels.add(newLabel);
    	newAcks.add(nextAckNum);
    	waitingAcks.put(nextAckNum, id);
    	nextAckNum++;
    }
    
    private void processPushSegmentCommand(Identifier id){
    	String type = WMUtil.getValueOfAttribute(id, "type", "Error (push-segment): No ^type attribute");
    	String originator = WMUtil.getValueOfAttribute(id, "originator", "Error (push-segment): No ^originator attribute");
    	SBolt.Singleton().getBoltAgent().getStack().pushSegment(type, originator);
    	id.CreateStringWME("status", "complete");
    }
    
    private void processPopSegmentCommand(Identifier id){
    	SBolt.Singleton().getBoltAgent().getStack().popSegment();
    	id.CreateStringWME("status", "complete");
    }
    
    private void processHomeCommand(Identifier id){
    	robot_command_t command = new robot_command_t();
        command.utime = TimeUtil.utime();
        command.dest = new double[6];
    	command.action = "HOME";
    	SBolt.broadcastRobotCommand(command);
        
        id.CreateStringWME("status", "complete");
    }
    
    private void processReportInteraction(Identifier id){
    	String type = WMUtil.getValueOfAttribute(id, "type");
    	String originator = WMUtil.getValueOfAttribute(id, "originator");
    	Identifier sat = WMUtil.getIdentifierOfAttribute(id, "satisfaction");
    	String eventName = sat.GetChild(0).GetAttribute();
    	WMElement eventTypeWME = sat.GetChild(0).ConvertToIdentifier().FindByAttribute("type", 0);
    	Identifier context = WMUtil.getIdentifierOfAttribute(id, "context");
    	
    	String message = "";
    	if(type.equals("get-next-task")){
    		message = "I am idle and waiting for you to initiate a new interaction";
    	} else if(type.equals("get-next-subaction")){
    		String verb = WMUtil.getValueOfAttribute(context, "verb");
    		message = "What is the next step in performing '" + verb + "'?";
    	} else if(type.equals("category-of-word")){
    		String word = WMUtil.getValueOfAttribute(context, "word");
    		message = "I do not know the category of " + word + ". " + 
    		"You can say something like 'a shape' or 'blue is a color'";
    	} else if(type.equals("which-question")){
    		String objStr = LingObject.createFromSoarSpeak(context, "description").toString();
    		message = "I see multiple examples of '" + objStr + "' and I need clarification";
    	} else if(type.equals("teaching-request")){
    		String objStr = LingObject.createFromSoarSpeak(context, "description").toString();
    		message = "Please give me teaching examples of '" + objStr + "' and tell me 'finished' when you are done.";
    	} else if(type.equals("get-goal")){
    		String verb = WMUtil.getValueOfAttribute(context, "verb");
    		message = "Please tell me what the goal of '" + verb + "'is.";
    	}
    	
    	ChatFrame.Singleton().addMessage(message, ActionType.Agent);
    	
    	// AM: Added for referents testing
    	String numIndexes = WMUtil.getValueOfAttribute(id, "num-indexes");
    	String numFailures = WMUtil.getValueOfAttribute(id, "num-failures");
    	if(numIndexes != null && numFailures != null){
    		Integer ni = Integer.parseInt(numIndexes);
    		Integer nf = Integer.parseInt(numFailures);
    		String report = "INDEXING SUCCESS: (" + (ni-nf) + "/" + ni + ")";
    		ChatFrame.Singleton().addMessage(report, ActionType.UiAction);
    	}
    	
    	
        id.CreateStringWME("status", "complete");
    }
}
