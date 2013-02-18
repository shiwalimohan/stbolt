package edu.umich.sbolt.language.Patterns;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.umich.sbolt.language.LinguisticEntity;

import sml.Agent;
import sml.Identifier;

public class Sentence extends LinguisticEntity{
    public static String TYPE = "Sentence";
	private String type = null;
	private LinguisticEntity component;

	public void translateToSoarSpeak(Identifier messageId, String connectingString) {
		component.translateToSoarSpeak(messageId,type);
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
			type = null;
			component = (ObjectRelation)tagsToWords.get(m.group());
		}
		
		p = Pattern.compile("GS\\d*");
		m = p.matcher(string);
		if(m.find()){
			type = "goal-info";
			component = (GoalInfo)tagsToWords.get(m.group());
		}
		
		p = Pattern.compile("PS\\d*");
		m = p.matcher(string);
		if(m.find()){
			type = "proposal-info";
			component = (ProposalInfo)tagsToWords.get(m.group());
		}
	}
}
