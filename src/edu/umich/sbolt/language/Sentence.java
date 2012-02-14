package edu.umich.sbolt.language;

import java.util.Iterator;
import java.util.Map;

public class Sentence {
	private String languageSentence;
	private Dictionary dictionary;
	private Map<Integer,String> indexWords;
	private Map<Integer,String> indexTags;
	private String tagString;
	
	
	public Sentence(String instructorSentence, Dictionary dictionary) {
		languageSentence = instructorSentence;
		dictionary = dictionary;
		setTagIndexWord();
		tagString = getPOSTagString();
	}
	
	// create tag to index mapping and index to word mapping
	private void setTagIndexWord(){
		// splits on spaces, can be easily extended to split on , and .
		String[] wordSet = languageSentence.split(" ");
		for (int i = 0; i < wordSet.length; i++){
			indexWords.put(i, wordSet[i]);
			//converts a word to its POS tag and appends the word index to it. 
			// "red" -> JJ1
			indexTags.put(i, dictionary.getTag(wordSet[i]).concat(Integer.toString(i)));
		}
	}
	
	private String getPOSTagString(){
		String tagString = "";
		for (int i = 0; i < indexTags.size(); i++){
			tagString.concat(indexTags.get(i)+ " ");
		}
		return tagString;
	}
	
}
