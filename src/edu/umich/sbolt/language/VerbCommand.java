package edu.umich.sbolt.language;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sml.Agent;
import sml.Identifier;

public class VerbCommand extends LinguisticEntity{
	private String verb = null;
	private LingObject directObject = null;
	private String preposition = null;
	private LingObject secondObject = null;

	public void translateToSoarSpeak(Identifier messageId, String connectingString){
		Identifier verbId = messageId.CreateIdWME(connectingString);
		verbId.CreateStringWME("word", verb);
		if(directObject != null)
			directObject.translateToSoarSpeak(verbId,"direct-object");
		if(preposition != null){
			Identifier prepId = verbId.CreateIdWME("preposition");
			prepId.CreateStringWME("word", preposition);
			secondObject.translateToSoarSpeak(prepId,"object");
		}
	}

	public void extractLinguisticComponents(String string, Map tagsToWords) {
		Pattern p = Pattern.compile("VB\\d*");
		Matcher m = p.matcher(string);
		if(m.find()){
			verb = tagsToWords.get(m.group()).toString();
		}
		
		p = Pattern.compile("PP\\d*");
		m = p.matcher(string);
		if(m.find()){
			preposition = tagsToWords.get(m.group()).toString();
		}
		
		p = Pattern.compile("OBJ\\d*");
		m = p.matcher(string);
		if(m.find()){
			directObject = (LingObject) tagsToWords.get(m.group());
		}
		if(m.find()){
			secondObject = (LingObject) tagsToWords.get(m.group());
		}
	}
}
