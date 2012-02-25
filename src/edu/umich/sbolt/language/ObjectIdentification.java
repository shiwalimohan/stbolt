package edu.umich.sbolt.language;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.umich.sbolt.world.WorkingMemoryUtil;

import sml.Agent;
import sml.Identifier;

public class ObjectIdentification extends LinguisticEntity{
    public static String TYPE = "ObjectIdentification";
    private LingObject object = null;
    
    public LingObject getObject(){
        return object;
    }
    
    public void translateToSoarSpeak(Identifier messageId, String connectingString){
        Identifier objectId = messageId.CreateIdWME(connectingString);
        if(object != null){
            object.translateToSoarSpeak(objectId,"object");
        }
    }

    public void extractLinguisticComponents(String string, Map tagsToWords) {
        Pattern p = Pattern.compile("OBJ\\d*");
        Matcher m = p.matcher(string);
        if(m.find()){
            object = (LingObject) tagsToWords.get(m.group());
        }
    }
}
