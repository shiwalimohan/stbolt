package com.soartech.bolt.testing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.swing.JFileChooser;

import abolt.classify.ClassifierManager;
import abolt.lcmtypes.robot_command_t;
import april.util.TimeUtil;
import edu.umich.sbolt.ChatFrame;
import edu.umich.sbolt.SBolt;
import edu.umich.sbolt.world.World;

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
	
	public static Script loadScript() {
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(Settings.getInstance().getSboltDirectory());
		int returnVal = chooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			return ParseScript.parse(chooser.getSelectedFile());
		}
		return null;
	}
	
	public static void saveScript(List<String> chatMessages) {
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(Settings.getInstance().getSboltDirectory());
		int returnVal = chooser.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			Util.saveFile(chooser.getSelectedFile(), chatMessages);
		}
	}
	
    public static void handleNextScriptAction(Script script, List<String> chatMessages) {
    	new ScriptRunner(script, chatMessages).start();
    	if(script != null && script.peekType() == ActionType.Agent) {
    		ChatFrame.Singleton().setWaiting(true);
        }
    }
    
    public static void handleUiAction(String action) throws UnhandledUiAction {
    	if(action.contains("point")) {
    		try {
    			String rep = action.replace("point", "").trim();
    			int id = Integer.parseInt(rep);
        		pointAt(id);
    		} catch (NumberFormatException e) {
    			new UnhandledUiAction("Invalid number in point UiAction: "+action);
    		}
    		return;
    	} else if(action.contains("automated")) {
    		boolean auto = Boolean.parseBoolean(action.replace("automated", "").trim());
    		automateScript(auto);
    	} else if(action.contains("simulator clear")) {
    		clearSimulatorData();
    	}
    	throw new UnhandledUiAction("Unrecognized action.");
    }
    
    public static void resetArm() {
		robot_command_t command = new robot_command_t();
		command.utime = TimeUtil.utime();
		command.action = "RESET";
		command.dest = new double[6];
		SBolt.broadcastRobotCommand(command);
	}
    
    public static void clearSimulatorData() {
    	//TODO implement function
    }
    
    public static void automateScript(boolean isAutomated) {
    	Settings.getInstance().setAutomated(isAutomated);
    }
    
    public static void pointAt(int objectId) {
    	World.Singleton().setPointedObjectID(objectId);
    }
}
