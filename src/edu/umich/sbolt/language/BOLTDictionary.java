package edu.umich.sbolt.language;


import java.util.HashSet;
import java.util.Set;


// This class maintains a user defined dictionary and returns POS tags using that dictionary.

public class BOLTDictionary {
	private Set<String> noun;
	private Set<String> adjective;
	private Set<String> verb;
	private Set<String> determiner;
	private Set<String> preposition;
	
	// relevant pos tags 
	private String nounTag = "NN";
	private String adjectiveTag = "JJ";
	private String verbTag = "VB";
	private String determinerTag = "DT";
	private String prepositionTag = "PP";


	public BOLTDictionary(){
	//	System.out.println(string);
		noun = new HashSet();
		noun.add("block");
		noun.add("table");
		
		adjective = new HashSet();
		adjective.add("red");
		
		determiner = new HashSet();
		determiner.add("a");
		determiner.add("the");
		
		preposition = new HashSet();
		preposition.add("on");
		
		verb = new HashSet();
		verb.add("put");
	}
	
	private boolean isNoun(String string){
		System.out.println("word " + string);
		if (noun.contains(string))
			return true;
		return false;
	}
	
	private boolean isAdjective(String string){
		if (adjective.contains(string))
			return true;
		return false;
	}
	
	private boolean isVerb(String string){
		if (verb.contains(string))
			return true;
		return false;
	}
	
	private boolean isDeterminer(String string){
		if (determiner.contains(string))
			return true;
		return false;
	}
	
	private boolean isPreposition(String string){
		if (preposition.contains(string))
			return true;
		return false;
	}
	
	public String getTag(String string){
		String tag;
		if (isNoun(string)) return nounTag;
		if (isAdjective(string)) return adjectiveTag;
		if (isVerb(string)) return verbTag;
		if (isDeterminer(string)) return determinerTag;
		if (isPreposition(string)) return prepositionTag;
		// return the string back for words that appear verbatim in the MIISI document
		return string;
	}
}
