package edu.umich.sbolt.language;

import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sml.Identifier;


// will parse only positive predicates for now
public class ObjectRelation implements LinguisticEntity{
	String preposition;
	private LingObject object1;
	private LingObject object2;

	@Override
	public void extractLinguisticComponents(String string, Map tagsToWords) {
		//get preposition 
		Pattern p = Pattern.compile("PP\\d*");
		Matcher m = p.matcher(string);
		if(m.find()){
			preposition = tagsToWords.get(m.group()).toString();
		//	System.out.println(preposition);
		}
		
		//get object1
		p = Pattern.compile("OBJ\\d*");
		m = p.matcher(string);
		if(m.find()){
			object1 = (LingObject) tagsToWords.get(m.group());
		}
		if(m.find()){
			object2 = (LingObject) tagsToWords.get(m.group());
		}
		
		
	}


	@Override
	public Identifier translateToSoarSpeak(Map<String, Object> tagsToWords,
			Identifier messageId) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
