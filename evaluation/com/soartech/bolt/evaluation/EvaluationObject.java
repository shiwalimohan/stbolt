package com.soartech.bolt.evaluation;

public class EvaluationObject implements Comparable<EvaluationObject> {
	final private String size;
	final private String color;
	final private String shape;
	
	public EvaluationObject(String size, String color, String shape) {
		this.size = size;
		this.color = color;
		this.shape = shape;
	}
	
	public EvaluationObject(Size size, Color color, Shape shape) {
		this.size = size.toString();
		this.color = color.toString();
		this.shape = shape.toString();
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

	@Override
	public int compareTo(EvaluationObject o) {
		return o.toString().compareTo(toString());
	}
	
	@Override
	public boolean equals(Object o) {
		return o.toString().equals(toString());
	}
}
