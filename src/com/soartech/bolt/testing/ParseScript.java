package com.soartech.bolt.testing;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ParseScript {
	public static Script parse(File f) {
		Script script = new Script();

		try {
			Scanner s = new Scanner(f);
			while(s.hasNextLine()) {
				script.addAction(new Action(s.nextLine()));
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return script;
	}
}
