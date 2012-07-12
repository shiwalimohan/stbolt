package com.soartech.bolt.testing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class Util {
	
	public static void saveFile(File f, List<String> history) {
		try {
			Writer output = new BufferedWriter(new FileWriter(f));
			for(String str : history)
				output.write(str+"\n");
			output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}