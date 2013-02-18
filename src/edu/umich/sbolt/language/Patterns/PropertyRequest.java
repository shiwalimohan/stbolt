package edu.umich.sbolt.language.Patterns;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.umich.sbolt.language.LinguisticEntity;
import edu.umich.sbolt.world.WMUtil;

import sml.Agent;
import sml.Identifier;

public class PropertyRequest extends LinguisticEntity{
    public static String TYPE = "PropertyRequest";
    private String attribute = null;
    private LingObject object = null;
    
    public String getAttribute(){
        return attribute;
    }
    
    public void translateToSoarSpeak(Identifier messageId, String connectingString){
        messageId.CreateStringWME("type", "object-question");
        messageId.CreateStringWME("originator", "mentor");
        Identifier fieldsId = messageId.CreateIdWME("information");
        fieldsId.CreateStringWME("word", attribute);
		object.translateToSoarSpeak(fieldsId, "object");
        fieldsId.CreateStringWME("question-word", "what");
    }

    public void extractLinguisticComponents(String string, Map tagsToWords) {
        Pattern p = Pattern.compile("AT\\d*");
        Matcher m = p.matcher(string);
        if(m.find()){
            attribute = (String)tagsToWords.get(m.group());
        }
        
        p = Pattern.compile("OBJ\\d*");
        m = p.matcher(string);
        if(m.find()){
            object = (LingObject) tagsToWords.get(m.group());
        }
    }
}
