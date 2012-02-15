package edu.umich.sbolt.language;


import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class Sentence {
	private String languageSentence;
	private BOLTDictionary dictionary;
	private Map<String,String> tagsToWords;
	private String tagString;
	
	
	public Sentence(String instructorSentence, BOLTDictionary dictionary) {
		tagsToWords = new LinkedHashMap();
		this.languageSentence = instructorSentence;
		this.dictionary = dictionary;
		setTagIndexWord();
		this.tagString = getPOSTagString();
	}
	
	// create tags to words mappings
	private void setTagIndexWord(){
		// splits on spaces
		String[] wordSet = languageSentence.split(" ");
		String tag;
		for (int i = 0; i < wordSet.length; i++){
			//converts a word to its POS tag and appends the word index to it. 
			// "red" -> JJ1
	     // System.out.println("Considering word " + wordSet[i]);
			tag = dictionary.getTag(wordSet[i]);
		//	System.out.println("tag " + tag);
			tagsToWords.put(tag.concat(Integer.toString(i)),wordSet[i]);
		}
	}
	
	private String getPOSTagString(){
		String tagString = "";
		Iterator itr = tagsToWords.entrySet().iterator();
		while(itr.hasNext()){
			Map.Entry pair = (Map.Entry)itr.next();
			System.out.println(pair.getKey().toString());
			tagString = tagString+pair.getKey().toString()+" ";
		}
//		System.out.println("POS tagged string is " + tagString);
		return tagString;
	}
	
	// main function for testing
	public static void main(String[] args) {
		String sentence = "put a red block on the table";
		BOLTDictionary d = new BOLTDictionary();
		Sentence s = new Sentence(sentence,d);
	}
}
