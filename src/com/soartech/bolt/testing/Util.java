package com.soartech.bolt.testing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import edu.umich.sbolt.ChatFrame;

public class Util {
	
	public static void saveFile(File f, List<String> history) {
		try {
			Writer output = new BufferedWriter(new FileWriter(f));
			for(String str : history)
				output.write(str+"\n");
			output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
    public static void handleNextScriptAction(Script script, List<String> chatMessages) {
    	if(script == null) {
    		ChatFrame.Singleton().addMessage("No script loaded!");
    		return;
    	}
    	if(!script.hasNextAction()) {
    		ChatFrame.Singleton().addMessage("Script finished.");
    		return;
    	}
    	
    	String observed;
    	if(chatMessages.size() > 0) {
    		observed = chatMessages.get(chatMessages.size()-1);
    	} else {
    		observed = "";
    	}
    	
    	Action next = script.getNextAction();
    	
    	if(next.getType() == ActionType.Mentor) {
    		ChatFrame.Singleton().preSetMentorMessage(next.getAction());
    	}
    	if(next.getType() == ActionType.Agent) {
    		//check if response is correct
    		String expected = next.getAction();
    		if(!observed.contains("Agent:")) {
    			script.insertFirstAction(next);
    			return;
    		}
    		ChatFrame.Singleton().setWaiting(false);
    		if(!observed.contains(expected)) {
    			ChatFrame.Singleton().addMessage("    - Error - Expected: "+expected, ActionType.Incorrect);
    		} else {
    			ChatFrame.Singleton().addMessage("    - Correct -", ActionType.Correct);
    		}
    	}
    	if(next.getType() == ActionType.Comment) {
    		ChatFrame.Singleton().addMessage("Comment: "+next.getAction());
    	}
    	if(next.getType() == ActionType.AgentAction) {
    		ChatFrame.Singleton().addMessage("AgentAction: "+next.getAction());
    	}
    	if(next.getType() == ActionType.MentorAction) {
    		ChatFrame.Singleton().addMessage("MentorAction: "+next.getAction());
    	}
    	if(!script.nextActionRequiresMentorAttention())
    		handleNextScriptAction(script, chatMessages);
    }
}
