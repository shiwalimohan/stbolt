package edu.umich.sbolt.language;

import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

public class BOLTRegex {
	protected static Set<Pattern> regexSet;
	protected static String tag;
	
	public static Pattern getRegex(){
		Iterator itr = regexSet.iterator();
		if (regexSet.size() == 1)
			return (Pattern) itr.next();
		return null;
	}
	
	public static String getTag(){
		return tag;
	}
}
