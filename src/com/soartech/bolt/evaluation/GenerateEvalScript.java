package com.soartech.bolt.evaluation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import com.soartech.bolt.testing.ActionType;
import com.soartech.bolt.testing.ScriptDataMap;
import com.soartech.bolt.testing.UiCommandNotFoundException;

public class GenerateEvalScript {
	private static ScriptDataMap dm = ScriptDataMap.getInstance();
	
	private static enum Cat {
		size, color, shape
	}

	public static void main(String[] args) throws IOException {
		generateColorTrial();
		generateSizeTrial();
		generateShapeTrial();
	}
	
	public static void generateColorTrial() throws IOException {
		EvaluationObjects eos = new EvaluationObjects();
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
		EvaluationObjects eos = new EvaluationObjects();
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
		EvaluationObjects eos = new EvaluationObjects();
		eos.addObject(Color.blue, Shape.arch, Size.large);
		eos.addObject(Color.green, Shape.arch, Size.large);
		eos.addObject(Color.red, Shape.triangle, Size.large);
		eos.addObject(Color.red, Shape.rectangle, Size.large);
		eos.addObject(Color.green, Shape.rectangle, Size.small);
		eos.addObject(Color.blue, Shape.triangle, Size.small);
		eos.addObject(Color.green, Shape.triangle, Size.small);
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
	
	public static void generateTrial(Writer output, Cat cat, EvaluationObjects eos) {
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
}
