package com.soartech.bolt.testing;

public class ActionTypeMap {
	private static final ActionTypeMap instance = new ActionTypeMap();
	private BijectiveMap<ActionType, Character> charMap = new BijectiveMap<ActionType, Character>();
	private BijectiveMap<ActionType, String> stringMap = new BijectiveMap<ActionType, String>();
	
	public static ActionTypeMap getInstance() {
		return instance;
	}
	
	private ActionTypeMap() {
		add(ActionType.Comment, "Comment:", '#');
		add(ActionType.AgentAction, "AgentAction:", '{');
		add(ActionType.MentorAction, "MentorAction:", '}');
		add(ActionType.Agent, "Agent:", '<');
		add(ActionType.Mentor, "Mentor:", '>');
		add(ActionType.UiAction, "UiAction:", '@');
	}
	
	public void add(ActionType type, String s, char c) {
		charMap.add(type, new Character(c));
		stringMap.add(type, s);
	}
	
	private ActionType checkNull(ActionType type) {
		if(type == null) {
			return type = ActionType.Invalid;
		} else {
			return type;
		}
	}
	
	public Character getChar(String startString) {
		return charMap.getLeft(stringMap.getRight(startString));
	}
	
	/**
	 * Get the string associated with an ActionType.
	 * @param type
	 * @return The string associated with an ActionType,
	 * or a string containing 4 spaces if no string is associated.
	 */
	public String getString(ActionType type) {
		String r = stringMap.getLeft(type);
		if(r != null) {
			return r;
		}
		return "   ";
	}
	
	/**
	 * Get the ActionType associated with a starting string.
	 * For example, ActionType.Mentor would return <code>Mentor:</code>
	 * @param startString
	 * @return The associated ActionType, or ActionType.Invalid if there
	 * is not one.
	 */
	public ActionType getType(String startString) {
		return checkNull(stringMap.getRight(startString));
		
	}
	
	public ActionType getType(char c) {
		return checkNull(charMap.getRight(new Character(c)));
	}
}
