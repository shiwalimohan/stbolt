package edu.umich.sbolt.language;


import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;

import sml.Identifier;

public class Sentence {
	private String languageSentence;
	private BOLTDictionary dictionary;
	private Map<String,Object> tagsToWords;
	private String tagString;
	private static int Counter;
	
	
	public Sentence(String instructorSentence, BOLTDictionary dictionary) {
		tagsToWords = new LinkedHashMap();
		this.languageSentence = instructorSentence;
		this.dictionary = dictionary;
		mapTagToWord();
		this.tagString = getPOSTagString();
		Counter = 0;
	}
	
	// create tags to words mappings
	private void mapTagToWord(){
		// splits on spaces
		String[] wordSet = languageSentence.split(" ");
		String tag;
		for (int i = 0; i < wordSet.length; i++){
			//converts a word to its POS tag and appends the word index to it. 
			// "red" -> JJ1
	     // System.out.println("Considering word " + wordSet[i]);
			tag = dictionary.getTag(wordSet[i]);
		//	System.out.println("tag " + tag);
			tagsToWords.put(tag.concat(Integer.toString(Counter)),wordSet[i]);
			Counter++;
		}
	}
	
	private String getPOSTagString(){
		String tagString = "";
		Iterator itr = tagsToWords.entrySet().iterator();
		while(itr.hasNext()){
			Map.Entry pair = (Map.Entry)itr.next();
	//		System.out.println(pair.getKey().toString());
			tagString = tagString+pair.getKey().toString()+" ";
		}
//		System.out.println("POS tagged string is " + tagString);
		return tagString;
	}
	

	public Identifier getSoarParse() {
		// get SoarParse for all objects in the sentence
		System.out.println("Sentence POS: " + tagString);
		tagString = LingObject.extract(tagString, tagsToWords, Counter);
		System.out.println("Parsed objects: " + tagString);
		tagString = ObjectRelation.extract(tagString, tagsToWords, Counter);
		System.out.println("Parsed relations: " + tagString);
		return null;
	}
	
	
	// main function for testing
	public static void main(String[] args) {
		String sentence = "the block is on the table";
		BOLTDictionary d = new BOLTDictionary("/home/shiwali/soar/sbolt/src/edu/umich/sbolt/language/dictionary.txt");
		Sentence s = new Sentence(sentence,d);
		s.getSoarParse();
	}


}
