package edu.umich.sbolt.language;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.*;

import sml.*;


public class LingObject extends BOLTRegex {
	private String determiner = null;
	private Set<String> adjective;
	private String noun;
	static {
		regexSet = new HashSet<Pattern>();
		regexSet.add(Pattern.compile("(DT\\d* )?(JJ\\d* )*(NN\\d*)"));
		tag = "OBJ";
	}
	
	LingObject (String string, Map tagsToWords){
		adjective = new HashSet();
		// extract determiner 
		Pattern p = Pattern.compile("DT\\d*");
		Matcher m = p.matcher(string);
		if(m.find()){
			determiner = tagsToWords.get(m.group()).toString();
	//		System.out.println(determiner);
		}
		
		// extract adjectives
		p = Pattern.compile("JJ\\d*");
		m = p.matcher(string);
		while(m.find()){
			adjective.add(tagsToWords.get(m.group()).toString());
	//		System.out.println(tagsToWords.get(m.group()).toString());
		}
		
		// extract nouns
		p = Pattern.compile("NN\\d*");
		m = p.matcher(string);
		while(m.find()){
			noun = tagsToWords.get(m.group()).toString();
	//		System.out.println(noun);
		}
	}
	public Identifier getSoarParse(Identifier id){	
		return id;
	}
	
	public static String extract(String tagString, Map<String, Object> tagsToWords, int Counter){
		Matcher m = getRegex().matcher(tagString);
		if(m.find()){
			StringBuffer sb = new StringBuffer();
			do{	
				//	System.out.println(m.group());
				LingObject obj = new LingObject(m.group(),tagsToWords);
				String newTag = tag+Integer.toString(Counter);
				tagsToWords.put(newTag, obj);
				m.appendReplacement(sb, newTag);
				Counter++;
			}while(m.find());
			return sb.toString();
		}
		return tagString;
	}
}
