package com.soartech.bolt.evaluation;

public enum Verb {
	MOVE_TO("move to"),
	MOVE_TO_THE_LEFT_OF("move to the left of"),
	MOVE_TO_THE_RIGHT_OF("move to the right of"),
	DISCARD("discard"),
	STORE("store");
	
	private Verb(String realName) {
        this.name = realName;
    }
	@Override
    public String toString() {
        return name;
    }
    private final String name;
}
