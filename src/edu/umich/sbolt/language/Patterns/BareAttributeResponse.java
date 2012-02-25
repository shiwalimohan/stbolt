package edu.umich.sbolt.language.Patterns;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.umich.sbolt.language.LinguisticEntity;
import edu.umich.sbolt.world.WorkingMemoryUtil;

import sml.Agent;
import sml.Identifier;

public class BareAttributeResponse extends LinguisticEntity{
    public static String TYPE = "BareAttributeResponse";
    private String attribute = null;
    
    public String getAttribute(){
        return attribute;
    }
    
    public void translateToSoarSpeak(Identifier messageId, String connectingString){
        messageId.CreateStringWME("type", "bare-attribute-response");
        messageId.CreateStringWME("originator", "mentor");
        Identifier fieldsId = messageId.CreateIdWME("fields");
        fieldsId.CreateStringWME("attribute", attribute);
    }

    public void extractLinguisticComponents(String string, Map tagsToWords) {
        Pattern p = Pattern.compile("AT\\d*");
        Matcher m = p.matcher(string);
        if(m.find()){
            attribute = (String)tagsToWords.get(m.group());
        }
    }
}
