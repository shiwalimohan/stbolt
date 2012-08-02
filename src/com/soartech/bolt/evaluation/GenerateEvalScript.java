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
	private static EvaluationObjects eos = EvaluationObjects.getInstance();
	
	private static enum Cat {
		size, color, shape
	}

	public static void main(String[] args) throws IOException {
		Writer output = new BufferedWriter(new FileWriter(
				new File("evalScript")));
		output.write("#!BechtelFormat\n");
		
		for (int i = 1; i <= 3; i++) {
			try {
				output.write(dm.getChar(ActionType.Comment)
						+ " Start color trial "+i+"\n");
			} catch (UiCommandNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			generateTrial(output, Cat.color);
		}
		
		for (int i = 1; i <= 5; i++) {
			try {
				output.write(dm.getChar(ActionType.Comment)
						+ " Start size trial "+i+"\n");
			} catch (UiCommandNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			generateTrial(output, Cat.size);
		}
		
		for (int i = 1; i <= 5; i++) {
			try {
				output.write(dm.getChar(ActionType.Comment)
						+ " Start shape trial "+i+"\n");
			} catch (UiCommandNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			generateTrial(output, Cat.shape);
		}
		output.close();
	}
	
	public static void generateTrial(Writer output, Cat cat) {
		List<EvaluationObject> obs = eos.randomObjectOrdering();
		for(EvaluationObject o : obs) {
			try {
				output.write(dm.getChar(ActionType.MentorAction)+ " select the "+o.toString()+"\n");
				output.write(dm.getChar(ActionType.Mentor)+ " What is this?"+"\n");
				switch(cat) {
				case color:
					output.write(dm.getChar(ActionType.Mentor)+ " This is a "+o.getColor()+" object"+"\n");
					break;
				case size:
					output.write(dm.getChar(ActionType.Mentor)+ " This is a "+o.getSize()+" object"+"\n");
					break;
				case shape:
					output.write(dm.getChar(ActionType.Mentor)+ " This is a "+o.getShape()+"\n");
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
