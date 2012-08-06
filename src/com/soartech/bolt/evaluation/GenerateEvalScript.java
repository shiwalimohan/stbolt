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
		output.write("@ classifier clear\n");
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
				output.write(dm.getChar(ActionType.Mentor)+ " What is this?"+"\n");
				switch(cat) {
				case color:
					String color = o.getColor();
					output.write(dm.getChar(ActionType.Mentor)+ " This is a "+color+" object"+"\n");
					if(!eos.isDefined(color))
						output.write(dm.getChar(ActionType.Mentor)+ " "+eos.define(color)+"\n");
					break;
				case size:
					String size = o.getSize();
					output.write(dm.getChar(ActionType.Mentor)+ " This is a "+size+" object"+"\n");
					if(!eos.isDefined(size))
						output.write(dm.getChar(ActionType.Mentor)+ " "+eos.define(size)+"\n");
					break;
				case shape:
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
