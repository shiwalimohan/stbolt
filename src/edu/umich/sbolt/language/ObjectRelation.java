package edu.umich.sbolt.language;

import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sml.Agent;
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
	public void translateToSoarSpeak(Identifier id) {
		Identifier relId = id.CreateIdWME("object-relation");
		relId.CreateStringWME("word", preposition);
		object1.translateToSoarSpeak(relId);
		object2.translateToSoarSpeak(relId);
	}
	
}
