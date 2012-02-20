package edu.umich.sbolt.language;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.*;

import sml.*;


public class LingObject implements LinguisticEntity {
	private String determiner = null;
	private Set<String> adjective;
	private String noun;
	
	LingObject (){
		
	}
	
	public void extractLinguisticComponents(String string, Map tagsToWords){
		adjective = new HashSet();
		Pattern p = Pattern.compile("DT\\d*");
		Matcher m = p.matcher(string);
		if(m.find()){
			determiner = tagsToWords.get(m.group()).toString();
		}
		
		p = Pattern.compile("JJ\\d*");
		m = p.matcher(string);
		while(m.find()){
			adjective.add(tagsToWords.get(m.group()).toString());
		}
		
		p = Pattern.compile("NN\\d*");
		m = p.matcher(string);
		while(m.find()){
			noun = tagsToWords.get(m.group()).toString();
		}
	}
	
	@Override
	public Identifier translateToSoarSpeak(Map<String, Object> tagsToWords, Identifier MessageId) {
		return null;
	}
}
