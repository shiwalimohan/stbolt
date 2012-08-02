package com.soartech.bolt.evaluation;

public class EvaluationObject {
	final private String size;
	final private String color;
	final private String shape;
	
	public EvaluationObject(String size, String color, String shape) {
		this.size = size;
		this.color = color;
		this.shape = shape;
	}

	public String getSize() {
		return size;
	}

	public String getColor() {
		return color;
	}

	public String getShape() {
		return shape;
	}
	
	@Override
	public String toString() {
		return size+" "+color+" "+shape;
	}
}
