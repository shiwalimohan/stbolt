package edu.umich.sbolt.language;


import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;

import sml.Agent;
import sml.Identifier;
import sml.Kernel;

public class Parser {
	
	private String languageSentence;
	
	private BOLTDictionary dictionary;
	
	private Map<String,Object> tagsToWords;
	
	private String tagString;
	
	private static int Counter;
	
	public Parser(String instructorSentence, BOLTDictionary dictionary) {
		tagsToWords = new LinkedHashMap();
		this.languageSentence = instructorSentence;
		this.dictionary = dictionary;
		mapTagToWord();
		this.tagString = getPOSTagString();
		Counter = 0;
	}
	
	// create tags to words mappings
	private void mapTagToWord(){
		String[] wordSet = languageSentence.split(" ");
		String tag;
		for (int i = 0; i < wordSet.length; i++){
			tag = dictionary.getTag(wordSet[i]);
			tagsToWords.put(tag.concat(Integer.toString(Counter)),wordSet[i]);
			Counter++;
		}
	}
	
	//create a sentence of POS tags.
	private String getPOSTagString(){
		String tagString = "";
		Iterator itr = tagsToWords.entrySet().iterator();
		while(itr.hasNext()){
			Map.Entry pair = (Map.Entry)itr.next();
			tagString = tagString+pair.getKey().toString()+" ";
		}
		return tagString;
	}
	

	//parse linguistic elements from the POS tagString.
	public String getParse() {
		ParserUtil util = new ParserUtil();
		System.out.println("Sentence POS: " + tagString);
		tagString = util.extractObject(tagString, tagsToWords, Counter);
		System.out.println("Parsed objects: " + tagString);
		tagString = util.extractObjectRelation(tagString, tagsToWords, Counter);
		System.out.println("Parsed relations: " + tagString);
		tagString = util.extractVerbCommand(tagString, tagsToWords, Counter);
		System.out.println("Parsed verb-command: " + tagString);
		tagString = util.extractGoalInfo(tagString, tagsToWords, Counter);
		System.out.println("Parsed goal: " + tagString);
		tagString = util.extractProposalInfo(tagString, tagsToWords, Counter);
		System.out.println("Parsed proposal: " + tagString);
		tagString = util.extractSentence(tagString, tagsToWords, Counter);
		System.out.println("Parsed sentence: " + tagString);
		return tagString;
	}
	
	//Get Soar structure
	public void traslateToSoarSpeak(Identifier messageId, String tagString){
		//System.out.println("Translating sentence");
		((Sentence) tagsToWords.get(tagString)).translateToSoarSpeak(messageId, null);
	}

	public static void getSoarSpeak(String latestMessage, BOLTDictionary dictionary, Identifier messageId) {
		Parser p = new Parser(latestMessage,dictionary);
		p.traslateToSoarSpeak(messageId,p.getParse());
	}

}
