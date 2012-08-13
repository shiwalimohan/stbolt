package com.soartech.bolt.evaluation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

import com.soartech.bolt.testing.ActionType;
import com.soartech.bolt.testing.ScriptDataMap;
import com.soartech.bolt.testing.UiCommandNotFoundException;

public class GenerateEvalScript {
	private static ScriptDataMap dm = ScriptDataMap.getInstance();
	
	private static enum Cat {
		size, color, shape
	}

	public static void main(String[] args) throws IOException, UiCommandNotFoundException {
		generateColorTrial();
		generateSizeTrial();
		generateShapeTrial();
		generateObjectDescriminationTrial();
		generatePrepositionTrial();
		generatePrepositionSelectivenessTrial();
	}
	
	public static void generateColorTrial() throws IOException {
		EvaluationData eos = new EvaluationData();
		eos.addObject(Color.red, Shape.triangle, Size.large);
		eos.addObject(Color.red, Shape.rectangle, Size.large);
		eos.addObject(Color.red, Shape.circle, Size.small);
		eos.addObject(Color.blue, Shape.circle, Size.small);
		eos.addObject(Color.blue, Shape.triangle, Size.small);
		eos.addObject(Color.blue, Shape.square, Size.large);
		eos.addObject(Color.green, Shape.circle, Size.small);
		eos.addObject(Color.green, Shape.triangle, Size.small);
		eos.addObject(Color.green, Shape.rectangle, Size.large);
		eos.addObject(Color.yellow, Shape.circle, Size.small);
		eos.addObject(Color.yellow, Shape.triangle, Size.small);
		eos.addObject(Color.yellow, Shape.triangle, Size.large);
		
		Writer output = new BufferedWriter(new FileWriter(
				new File("scripts/colorEvaluation.bolt")));
		output.write("#!BechtelFormat\n");
		output.write("@ classifier clear\n");
		for (int i = 1; i <= 20; i++) {
			try {
				output.write(dm.getChar(ActionType.Comment)
						+ " Start color trial "+i+"\n");
			} catch (UiCommandNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			generateTrial(output, Cat.color, eos);
		}
		output.close();
	}
	
	public static void generateShapeTrial() throws IOException {
		EvaluationData eos = new EvaluationData();
		eos.addObject(Color.yellow, Shape.triangle, Size.small);
		eos.addObject(Color.green, Shape.triangle, Size.small);
		eos.addObject(Color.blue, Shape.triangle, Size.large);
		eos.addObject(Color.red, Shape.circle, Size.small);
		eos.addObject(Color.blue, Shape.circle, Size.small);
		eos.addObject(Color.green, Shape.circle, Size.small);
		eos.addObject(Color.yellow, Shape.rectangle, Size.large);
		eos.addObject(Color.green, Shape.rectangle, Size.small);
		eos.addObject(Color.red, Shape.rectangle, Size.large);
		eos.addObject(Color.red, Shape.arch, Size.small);
		eos.addObject(Color.green, Shape.arch, Size.large);
		eos.addObject(Color.blue, Shape.arch, Size.large);
		
		Writer output = new BufferedWriter(new FileWriter(
				new File("scripts/shapeEvaluation.bolt")));
		output.write("#!BechtelFormat\n");
		output.write("@ classifier clear\n");
		for (int i = 1; i <= 20; i++) {
			try {
				output.write(dm.getChar(ActionType.Comment)
						+ " Start shape trial "+i+"\n");
			} catch (UiCommandNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			generateTrial(output, Cat.shape, eos);
		}
		output.close();
	}
	
	public static void generateSizeTrial() throws IOException {
		EvaluationData eos = new EvaluationData();
		eos.addObject(Color.blue, Shape.arch, Size.large);
		eos.addObject(Color.yellow, Shape.triangle, Size.large);
		eos.addObject(Color.red, Shape.rectangle, Size.large);
		eos.addObject(Color.green, Shape.rectangle, Size.small);
		eos.addObject(Color.blue, Shape.triangle, Size.small);
		eos.addObject(Color.red, Shape.arch, Size.small);
		
		Writer output = new BufferedWriter(new FileWriter(
				new File("scripts/sizeEvaluation.bolt")));
		output.write("#!BechtelFormat\n");
		output.write("@ classifier clear\n");
		for (int i = 1; i <= 20; i++) {
			try {
				output.write(dm.getChar(ActionType.Comment)
						+ " Start size trial "+i+"\n");
			} catch (UiCommandNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			generateTrial(output, Cat.size, eos);
		}
		output.close();
	}
	
	public static void generateTrial(Writer output, Cat cat, EvaluationData eos) {
		List<EvaluationObject> obs = eos.randomObjectOrdering();
		int i = 1;
		try {
			output.write(dm.getChar(ActionType.Comment)+" The random ordering for this trial is:\n");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UiCommandNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for(EvaluationObject o: obs) {
			try {
				output.write(dm.getChar(ActionType.Comment)+" Object "+i+": "+o.toString()+"\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UiCommandNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			i++;
		}
		for(EvaluationObject o : obs) {
			try {
				output.write(dm.getChar(ActionType.MentorAction)+ " select the "+o.toString()+"\n");
				switch(cat) {
				case color:
					output.write(dm.getChar(ActionType.Mentor)+ " What color is this?"+"\n");
					String color = o.getColor();
					output.write(dm.getChar(ActionType.Mentor)+ " This is a "+color+" object"+"\n");
					if(!eos.isDefined(color))
						output.write(dm.getChar(ActionType.Mentor)+ " "+eos.define(color)+"\n");
					break;
				case size:
					output.write(dm.getChar(ActionType.Mentor)+ " What size is this?"+"\n");
					String size = o.getSize();
					output.write(dm.getChar(ActionType.Mentor)+ " This is a "+size+" object"+"\n");
					if(!eos.isDefined(size))
						output.write(dm.getChar(ActionType.Mentor)+ " "+eos.define(size)+"\n");
					break;
				case shape:
					output.write(dm.getChar(ActionType.Mentor)+ " What shape is this?"+"\n");
					String shape = o.getShape();
					output.write(dm.getChar(ActionType.Mentor)+ " This is a "+shape+"\n");
					if(!eos.isDefined(shape))
						output.write(dm.getChar(ActionType.Mentor)+ " "+eos.define(shape)+"\n");
					break;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UiCommandNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void generatePrepositionTrial() throws IOException, UiCommandNotFoundException {
		EvaluationData ed = new EvaluationData();
		ed.setPrimaryObject(new EvaluationObject(Size.small, Color.blue, Shape.circle));
		ed.setReferenceObject(new EvaluationObject(Size.small, Color.red, Shape.circle));
		
		for(Preposition p : Preposition.values()) {
			ed.addPreposition(p.toString());
		}
		
		FiveByFiveBoard board = new FiveByFiveBoard();
		
		Writer output = new BufferedWriter(new FileWriter(
				new File("scripts/prepositionEvaluation.bolt")));
		output.write("#!BechtelFormat\n");
		output.write("@ classifier clear\n");
		output.write(dm.getChar(ActionType.MentorAction) + " select the "+ed.getPrimaryObjectString().toString()+"\n");
		output.write(dm.getChar(ActionType.Mentor) + " this is a "+ed.getPrimaryObjectString()+"\n");
		output.write(dm.getChar(ActionType.Mentor) + " a color"+"\n");
		output.write(dm.getChar(ActionType.Mentor) + " this is a "+ed.getPrimaryObjectString()+"\n");
		output.write(dm.getChar(ActionType.Mentor) + " this is a "+ed.getPrimaryObjectString()+"\n");
		output.write(dm.getChar(ActionType.MentorAction) + " select the "+ed.getReferenceObjectString().toString()+"\n");
		output.write(dm.getChar(ActionType.Mentor) + " this is a "+ed.getReferenceObjectString()+"\n");
		output.write(dm.getChar(ActionType.Mentor) + " a color"+"\n");
		output.write(dm.getChar(ActionType.Mentor) + " this is a "+ed.getReferenceObjectString()+"\n");
		output.write(dm.getChar(ActionType.Mentor) + " this is a "+ed.getReferenceObjectString()+"\n");
		for (int i = 1; i <= 20; i++) {
			output.write(dm.getChar(ActionType.Comment) + " Start preposition trial "+i+"\n");
			List<String> preps = ed.randomPrepositionOrdering();
			for(String prep : preps) {
				ThreeByThreeConfig conf = board.getRandomLocation(prep);
				output.write(dm.getChar(ActionType.Comment) + " Testing "+prep+"\n");
				output.write(dm.getChar(ActionType.MentorAction) + " place the "
						+ed.getPrimaryObjectString()+" at "+conf.getPrimeObjLoc().getDescription() 
						+" place the "
						+ed.getReferenceObjectString()+" at "+conf.getRefObjLoc().getDescription()+"\n");
				output.write(dm.getChar(ActionType.Mentor)+ " describe the scene"+"\n");
				String relation = "the "+ed.getPrimaryObjectString()+" is "+prep+" the "+ed.getReferenceObjectString();
				output.write(dm.getChar(ActionType.MentorAction)+ " check for: "+relation+"\n");
				output.write(dm.getChar(ActionType.Comment)+" if the relation is not in the agent's response correct the agent\n");
				output.write(dm.getChar(ActionType.Mentor)+" "+relation+"\n");
			}
		}
		output.close();
	}
	
	public static void generatePrepositionSelectivenessTrial() throws IOException, UiCommandNotFoundException {
		EvaluationData ed = new EvaluationData();
		ed.setPrimaryObject(new EvaluationObject(Size.small, Color.blue, Shape.circle));
		ed.setReferenceObject(new EvaluationObject(Size.small, Color.red, Shape.circle));
		
		for(Preposition p : Preposition.values()) {
			ed.addPreposition(p.toString());
		}
		
		FiveByFiveBoard board = new FiveByFiveBoard();
		
		Writer output = new BufferedWriter(new FileWriter(
				new File("scripts/prepositionEvaluationPart2.bolt")));
		output.write("#!BechtelFormat\n");
		for(ThreeByThreeConfig conf : board.getLocationList()) {
			output.write(dm.getChar(ActionType.MentorAction) + " place the "
					+ed.getPrimaryObjectString()+" at "+conf.getPrimeObjLoc().getDescription() 
					+" place the "
					+ed.getReferenceObjectString()+" at "+conf.getRefObjLoc().getDescription()+"\n");
			output.write(dm.getChar(ActionType.Mentor)+ " describe the scene"+"\n");
			output.write(dm.getChar(ActionType.MentorAction)+ " check the relations starting with "+ed.getPrimaryObjectString()+"\n");
		}
		output.write(dm.getChar(ActionType.Comment) + " starting final preposition test using pick up/put\n");
		for(Preposition prep : Preposition.values()) {
			output.write(dm.getChar(ActionType.MentorAction) + " place the "
					+ ed.getPrimaryObjectString() + " such that it is not "+prep.toString()+" the "
					+ ed.getReferenceObjectString()+"\n");
			output.write(dm.getChar(ActionType.Mentor) + " pick up the "+ed.getPrimaryObjectString()+"\n");
			output.write(dm.getChar(ActionType.Mentor) + " put the "+ed.getPrimaryObjectString() + " "
					+ prep.toString() + " the "+ed.getReferenceObjectString()+"\n");
		}
		output.close();
	}
	
	public static void generateObjectDescriminationTrial() throws IOException, UiCommandNotFoundException {
		List<EvaluationObject> objects = new LinkedList<EvaluationObject>();
		EvaluationObject one = new EvaluationObject(Size.large, Color.blue, Shape.arch);
		EvaluationObject two = new EvaluationObject(Size.small, Color.blue, Shape.circle);
		EvaluationObject three = new EvaluationObject(Size.small, Color.red, Shape.arch);
		objects.add(one);
		objects.add(two);
		objects.add(three);
		
		Writer output = new BufferedWriter(new FileWriter(
				new File("scripts/objectDiscriminationEvaluation.bolt")));
		output.write("#!BechtelFormat\n");
		output.write("@ classifier clear\n");
		for(EvaluationObject eo : objects) {
			output.write(dm.getChar(ActionType.MentorAction) + " place a " + eo.toString()+" on the board\n");
		}
		

		output.write(dm.getChar(ActionType.Mentor) + " point to the " + one.getSize()+" object\n");
		output.write(dm.getChar(ActionType.MentorAction) + " check that the arm is pointing at the " + one.toString()+"\n");
		output.write(dm.getChar(ActionType.UiAction) + " arm reset\n");
		output.write(dm.getChar(ActionType.Mentor) + " point to the " + one.getColor()+" object\n");
		output.write(dm.getChar(ActionType.Mentor) + " the large one\n");
		output.write(dm.getChar(ActionType.MentorAction) + " check that the arm is pointing at the " + one.toString()+"\n");
		output.write(dm.getChar(ActionType.UiAction) + " arm reset\n");
		output.write(dm.getChar(ActionType.Mentor) + " point to the " + one.getShape()+"\n");
		output.write(dm.getChar(ActionType.Mentor) + " the blue one\n");
		output.write(dm.getChar(ActionType.MentorAction) + " check that the arm is pointing at the " + one.toString()+"\n");
		output.write(dm.getChar(ActionType.UiAction) + " arm reset\n");
		
		output.write(dm.getChar(ActionType.Mentor) + " point to the " + two.getSize()+" object\n");
		output.write(dm.getChar(ActionType.Mentor) + " the blue one\n");
		output.write(dm.getChar(ActionType.MentorAction) + " check that the arm is pointing at the " + two.toString()+"\n");
		output.write(dm.getChar(ActionType.UiAction) + " arm reset\n");
		output.write(dm.getChar(ActionType.Mentor) + " point to the " + two.getColor()+" object\n");
		output.write(dm.getChar(ActionType.Mentor) + " the circle\n");
		output.write(dm.getChar(ActionType.MentorAction) + " check that the arm is pointing at the " + two.toString()+"\n");
		output.write(dm.getChar(ActionType.UiAction) + " arm reset\n");
		output.write(dm.getChar(ActionType.Mentor) + " point to the " + two.getShape()+"\n");
		output.write(dm.getChar(ActionType.MentorAction) + " check that the arm is pointing at the " + two.toString()+"\n");
		output.write(dm.getChar(ActionType.UiAction) + " arm reset\n");
		
		output.write(dm.getChar(ActionType.Mentor) + " point to the " + two.getSize()+" object\n");
		output.write(dm.getChar(ActionType.Mentor) + " the red one\n");
		output.write(dm.getChar(ActionType.MentorAction) + " check that the arm is pointing at the " + three.toString()+"\n");
		output.write(dm.getChar(ActionType.UiAction) + " arm reset\n");
		output.write(dm.getChar(ActionType.Mentor) + " point to the " + two.getColor()+" object\n");
		output.write(dm.getChar(ActionType.MentorAction) + " check that the arm is pointing at the " + three.toString()+"\n");
		output.write(dm.getChar(ActionType.UiAction) + " arm reset\n");
		output.write(dm.getChar(ActionType.Mentor) + " point to the " + two.getShape()+"\n");
		output.write(dm.getChar(ActionType.Mentor) + " the small one\n");
		output.write(dm.getChar(ActionType.MentorAction) + " check that the arm is pointing at the " + three.toString()+"\n");
		output.write(dm.getChar(ActionType.UiAction) + " arm reset\n");

		output.close();
	}
}
