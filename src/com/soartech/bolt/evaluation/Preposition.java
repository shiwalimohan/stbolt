package com.soartech.bolt.evaluation;

public enum Preposition {
	RIGHT_OF("right of"),
	LEFT_OF("left of"),
	IN_FRONT_OF("in front of"),
	BEHIND("behind"),
	FAR_FROM("far from"),
	NEAR("near");
	
	private Preposition(String realName) {
        this.name = realName;
    }
	@Override
    public String toString() {
        return name;
    }
    private final String name;
}
