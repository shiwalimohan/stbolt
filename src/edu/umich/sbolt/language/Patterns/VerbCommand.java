package edu.umich.sbolt.language.Patterns;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.umich.sbolt.language.LinguisticEntity;
import edu.umich.sbolt.world.WorkingMemoryUtil;

import sml.Agent;
import sml.Identifier;

public class VerbCommand extends LinguisticEntity{
    public static String TYPE = "VerbCommand";
	private String verb = null;
	private LingObject directObject = null;
	private String preposition = null;
	private LingObject secondObject = null;
	

    public String getVerb()
    {
        return verb;
    }


    public LingObject getDirectObject()
    {
        return directObject;
    }

    public String getPreposition()
    {
        return preposition;
    }

    public LingObject getSecondObject()
    {
        return secondObject;
    }

	
	
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
	
	public static VerbCommand createFromSoarSpeak(Identifier id, String name){
        if(id == null){
            return null;
        }
        Identifier verbId = WorkingMemoryUtil.getIdentifierOfAttribute(id, name);
        if(verbId == null){
            return null;
        }
	    VerbCommand verbCommand = new VerbCommand();
	    verbCommand.verb = WorkingMemoryUtil.getValueOfAttribute(verbId, "word");
        verbCommand.directObject = LingObject.createFromSoarSpeak(verbId, "direct-object");
        Identifier prepositionId = WorkingMemoryUtil.getIdentifierOfAttribute(verbId, "preposition");
        if(prepositionId != null){
            verbCommand.preposition = WorkingMemoryUtil.getValueOfAttribute(prepositionId, "word");
            verbCommand.secondObject = LingObject.createFromSoarSpeak(prepositionId, "object");
        }
	    return verbCommand;
	}
}
