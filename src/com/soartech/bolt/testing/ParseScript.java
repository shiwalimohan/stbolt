package com.soartech.bolt.testing;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ParseScript {
	public static Script parse(File f) {
		Script script = new Script();

		try {
			Scanner s = new Scanner(f);
			if(s.hasNext("#!BechtelFormat")) {
				s.nextLine();
				return parseBechtelFormatScript(script, s);
			} else {
				return parseDefaultFormatScript(script, s);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return script;
	}
	
	public static Script parseDefaultFormatScript(Script script, Scanner s) {
		while(s.hasNextLine()) {
			String scriptLine = s.nextLine();
			String[] lineType = scriptLine.split(":");
			ActionType type = null;
			
			if(lineType[0].equals("Agent")) {
				type = ActionType.Agent;
			} else if(lineType[0].equals("Mentor")) {
				type = ActionType.Mentor;
			} else if(lineType[0].equals("Comment")) {
				type = ActionType.Comment;
			} else if(lineType[0].equals("AgentAction")) {
				type = ActionType.AgentAction;
			} else if(lineType[0].equals("MentorAction")) {
				type = ActionType.MentorAction;
			}
			
			if(type == null)
				throw new RuntimeException("Invalid script line: "+scriptLine);
			
			String action = scriptLine.substring(lineType[0].length()+1).trim();
			script.addAction(new Action(type, action));
		}
		return script;
	}
	public static Script parseBechtelFormatScript(Script script, Scanner s) {
		while(s.hasNextLine()) {
			String line = s.nextLine();
			char lineType = line.charAt(0);
			ActionType type = null;
			switch(lineType) {
			case '#':
				type = ActionType.Comment;
				break;
			case '{':
				type = ActionType.AgentAction;
				break;
			case '}':
				type = ActionType.MentorAction;
				break;
			case '<':
				type = ActionType.Agent;
				break;
			case '>':
				type = ActionType.Mentor;
				break;
			}
			
			if(type == null)
				throw new RuntimeException("Invalid script line: "+line);
			
			String action = line.substring(1);
			action = action.trim();
			
			script.addAction(new Action(type, action));
		}
		return script;
	}
}
