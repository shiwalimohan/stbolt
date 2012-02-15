package edu.umich.sbolt.language.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.umich.sbolt.language.LingObject;

public class ObjectRegex {
	private static Pattern regex = Pattern.compile("(DT\\d* )?(JJ\\d* )*(NN\\d* )");
	
	public contains(String string){
		Matcher m = LingObject.getRegex().matcher(string);
	}
}
