package edu.umich.sbolt.language;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sml.Identifier;

public class Sentence implements LinguisticEntity{
	private String type = null;
	private LinguisticEntity component;

	public Identifier translateToSoarSpeak(Map<String, Object>tagsToWords, Identifier messageId) {
		Identifier id = component.translateToSoarSpeak(tagsToWords, messageId);
		return null;
	}

	@Override
	public void extractLinguisticComponents(String string, Map tagsToWords) {
		Pattern p = Pattern.compile("VBC\\d*");
		Matcher m = p.matcher(string);
		if(m.find()){
			type = "verb-command";
			component = (VerbCommand)tagsToWords.get(m.group());
		}
		
		p = Pattern.compile("REL\\d*");
		m = p.matcher(string);
		if(m.find()){
			type = "object-relation-info";
			component = (ObjectRelation)tagsToWords.get(m.group());
		}
		
	}
}
