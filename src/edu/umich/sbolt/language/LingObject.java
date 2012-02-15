package edu.umich.sbolt.language;

import java.util.HashMap;
import java.util.Set;
import java.util.regex.*;
import sml.*;


public class LingObject {
	private String determiner = null;
	private Set<String> adjective;
	private String noun;
	private Pattern regex = Pattern.compile("(DT\\d* )?(JJ\\d* )*(NN\\d* )");
	
	LingObject (String string, HashMap tagsToWords){
		// extract determiner 
		Pattern p = Pattern.compile("DT\\d*");
		Matcher m = p.matcher(string);
		if(m.find()){
			determiner = tagsToWords.get(m.group()).toString();
		}
		
		// extract adjectives
		p = Pattern.compile("JJ\\d*");
		m = p.matcher(string);
		while(m.find()){
			adjective.add(tagsToWords.get(m.group()).toString());
		}
		
		
		// extract nouns
		p = Pattern.compile("NN\\d*");
		m = p.matcher(string);
		while(m.find()){
			noun = tagsToWords.get(m.group()).toString();
		}
	}
	
	public Pattern getRegex(){
		return regex;
	}
	
	public Identifier getSoarParse(Identifier id){
		
		
		return id;
	}
	
}
