package com.soartech.bolt.testing;

import java.util.LinkedList;

public class Script {
	private LinkedList<Action> actions;
	
	public Script() {
		actions = new LinkedList<Action>();
	}
	
	public void addAction(Action a) {
		actions.add(a);
	}
	
	public Action getNextAction() {
		return actions.pop();
	}
	
	public boolean hasNextAction() {
		return !(actions.peek() == null);
	}
}
