package com.soartech.bolt.evaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class EvaluationData {
	private HashSet<String> colors;
	private HashSet<String> sizes;
	private HashSet<String> shapes;
	private HashSet<String> definedLabels;
	private HashSet<String> prepositions;
	private HashMap<Integer, EvaluationObject> objects;
	private EvaluationObject primaryObject;
	private EvaluationObject referenceObject;
	private final Random rnd;
	private int maxId;
	
	public EvaluationData() {
		colors = new HashSet<String>();
		sizes = new HashSet<String>();
		shapes = new HashSet<String>();
		prepositions = new HashSet<String>();
		definedLabels = new HashSet<String>();
		objects = new HashMap<Integer, EvaluationObject>();
		rnd = new Random(System.currentTimeMillis());
		maxId = -1;
//		setupObjects();
//		setupCommands();
	}
	
//	private void setupObjects() {
//		addObject(1, "small", "yellow", "circle");
//		addObject(2, "medium", "yellow", "triangle");
//		addObject(3, "large", "yellow", "triangle");
//		addObject(4, "medium", "red", "square");
//		addObject(5, "small", "red", "arch");
//		addObject(6, "large", "red", "rectangle");
//		addObject(7, "medium", "green", "triangle");
//		addObject(8, "small", "green", "rectangle");
//		addObject(9, "large", "green", "arch");
//	}
	
//	private void setupCommands() {
//		ScriptDataMap dm = ScriptDataMap.getInstance();
//		for(String color : colors) {
//			dm.addUiCommand("check color "+color, new CheckColor(color));
//		}
//	}
	public void addPreposition(String prep) {
		prepositions.add(prep);
	}
	
	public void addObject(int id, String size, String color, String shape) {
		sizes.add(size);
		colors.add(color);
		shapes.add(shape);
		objects.put(new Integer(id), new EvaluationObject(size, color, shape));
		if(id > maxId) {
			maxId = id;
		}
	}
	
	public void addObject(Integer id, EvaluationObject eo) {
		sizes.add(eo.getShape());
		colors.add(eo.getColor());
		shapes.add(eo.getShape());
		objects.put(id, eo);
		if(id > maxId) {
			maxId = id;
		}
	}
	
	public void addObject(Color color, Shape shape, Size size) {
		sizes.add(size.toString());
		colors.add(color.toString());
		shapes.add(shape.toString());
		objects.put(new Integer(++maxId), new EvaluationObject(size.toString(), color.toString(), shape.toString()));
	}
	
	public boolean isDefined(String label) {
		return definedLabels.contains(label);
	}
	
	public String define(String label) {
		definedLabels.add(label);
		if(colors.contains(label)) {
			return label + " is a color";
		} else if(sizes.contains(label)) {
			return label + " is a size";
		} else if(shapes.contains(label)) {
			return label + " is a shape";
		}
		return null;
	}
	
	public List<EvaluationObject> randomObjectOrdering() {
		ArrayList<EvaluationObject> random = new ArrayList<EvaluationObject>();
		for(EvaluationObject o : objects.values()) {
			random.add(o);
		}
		Collections.shuffle(random, rnd);
		return random;
	}
	
	public List<String> randomPrepositionOrdering() {
		ArrayList<String> random = new ArrayList<String>();
		for(String s : prepositions) {
			random.add(s);
		}
		Collections.shuffle(random, rnd);
		return random;
	}
	
	public String getPrimaryObjectString() {
		return primaryObject.getColor()+" object";
	}

	public void setPrimaryObject(EvaluationObject primaryObject) {
		if((referenceObject == null || this.primaryObject == null) ||
				!referenceObject.getColor().equals(primaryObject.getColor())) {
			this.primaryObject = primaryObject;
		} else {
			throw new RuntimeException("Reference and primary objects must have different colors.");
		}
	}

	public String getReferenceObjectString() {
		return referenceObject.getColor()+" object";
	}

	public void setReferenceObject(EvaluationObject referenceObject) {
		if((this.referenceObject == null || primaryObject == null) ||
				!referenceObject.getColor().equals(primaryObject.getColor())) {
			this.referenceObject = referenceObject;
		} else {
			throw new RuntimeException("Reference and primary objects must have different colors.");
		}
	}
}
