package edu.umich.sbolt.language;

import java.util.Set;

import edu.umich.sbolt.language.Patterns.LingObject;
import edu.umich.sbolt.world.WorkingMemoryUtil;
import sml.Identifier;

public class AgentMessageParser
{
    public static String translateAgentMessage(Identifier id){
        String message = null;
        String type = WorkingMemoryUtil.getValueOfAttribute(id, "type");
        Identifier fieldsId = WorkingMemoryUtil.getIdentifierOfAttribute(id, "fields");
        if(type == null || fieldsId == null){
            return null;
        } else if(type.equals("different-attribute-question")){
            message = translateDifferentAttributeQuestion(fieldsId);
        } else if(type.equals("value-question")){
            message = translateValueQuestion(fieldsId);
        } else if(type.equals("common-attribute-question")){
            message = translateCommonAttributeQuestion(fieldsId);
        } else if(type.equals("attribute-presence-question")){
            message = translateAttributePresenceQuestion(fieldsId);
        } else if(type.equals("attribute-question")){
            message = translateAttributeQuestion(fieldsId);
        }
        return message;
    }
    
    private static String translateDifferentAttributeQuestion(Identifier id){
        Set<String> exceptions = WorkingMemoryUtil.getAllValuesOfAttribute(id, "exception");
        String exceptionStr = getExceptionString(exceptions);
        LingObject similarObject = LingObject.createFromSoarSpeak(id, "similar-object");
        Set<LingObject> differentObjects = LingObject.createAllFromSoarSpeak(id, "different-object");
        String message = String.format("How does %s differ from ", similarObject.toString());
        for(LingObject obj : differentObjects){
            message += obj.toString() + "; ";
        }
        return exceptionStr + message;
    }
    
    private static String translateCommonAttributeQuestion(Identifier id){
        Set<String> exceptions = WorkingMemoryUtil.getAllValuesOfAttribute(id, "exception");
        String exceptionStr = getExceptionString(exceptions);
        Set<LingObject> similarObjects = LingObject.createAllFromSoarSpeak(id, "object");
        String message = "What do ";
        for(LingObject obj : similarObjects){
            message += obj.toString() + "; ";
        }
        
        return exceptionStr + message + " have in common?";
    }
    
    private static String translateAttributePresenceQuestion(Identifier id){
        Set<String> exceptions = WorkingMemoryUtil.getAllValuesOfAttribute(id, "exception");
        String exceptionStr = getExceptionString(exceptions);
        LingObject object = LingObject.createFromSoarSpeak(id, "object");
        String message = String.format("What attribute does %s have?", object.toString());

        return exceptionStr + message;
    }
    
    private static String translateAttributeQuestion(Identifier id){
        String value = WorkingMemoryUtil.getValueOfAttribute(id, "value");
        return String.format("What attribute does %s describe?", value);
    }
    
    private static String translateValueQuestion(Identifier id){
        Identifier attRelationId = WorkingMemoryUtil.getIdentifierOfAttribute(id, "attribute-relation");
        String objString = LingObject.createFromSoarSpeak(attRelationId, "object1").toString();
        String attribute = WorkingMemoryUtil.getValueOfAttribute(attRelationId, "word");
        
        return String.format("What %s is %s?", attribute, objString);
    }
    
    private static String getExceptionString(Set<String> exceptions){
        String exceptionStr = "";
        if (exceptions.size() > 0)
        {
            exceptionStr = "Other than ";
            for(String exception : exceptions){
                exceptionStr += exception + ", ";
            }
            exceptionStr = exceptionStr.substring(0, exceptionStr.length() - 2);
            exceptionStr += "; ";
        }
        return exceptionStr;
    }
    

}
