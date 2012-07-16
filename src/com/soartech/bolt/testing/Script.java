package com.soartech.bolt.testing;

import java.util.LinkedList;

public class Script {
	private LinkedList<Action> actions;
	
	public Script() {
		actions = new LinkedList<Action>();
	}
	
	public void insertFirstAction(Action a) {
		actions.addFirst(a);
	}
	
	public void addAction(Action a) {
		actions.add(a);
	}
	
	public Action getNextAction() {
		return actions.pop();
	}
	
	public ActionType peekType() {
		if(actions.peek() != null)
			return actions.peek().getType();
		else
			return null;
	}
	
	public boolean hasNextAction() {
		return !(actions.peek() == null);
	}
	
	public boolean nextActionRequiresMentorAttention() {
		ActionType type = actions.peek().getType();
		if(type == null)
			return false;
		switch(type) {
		case Mentor: return false;
		case MentorAction: return true;
		case AgentAction: return true;
		default: return false;
		}
	}
}
