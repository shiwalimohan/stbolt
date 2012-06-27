package com.soartech.bolt.testing;

public class Action {
	
	private ActionType type;
	private String action;
	
	public Action(String scriptLine) {
		String[] lineType = scriptLine.split(":");
		
		if(lineType[0].equals("Agent")) {
			type = ActionType.Agent;
		} else if(lineType[0].equals("Mentor")) {
			type = ActionType.Mentor;
		} else if(lineType[0].equals("Direction")) {
			type = ActionType.Direction;
		} else if(lineType[0].equals("Check")) {
			type = ActionType.Check;
		}
		
		if(type == null)
			throw new RuntimeException("Invalid script line: "+scriptLine);
		
		action = scriptLine.substring(lineType[0].length()+2);
	}
	
	public ActionType getType() {
		return type;
	}

	public void setType(ActionType type) {
		this.type = type;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
}
