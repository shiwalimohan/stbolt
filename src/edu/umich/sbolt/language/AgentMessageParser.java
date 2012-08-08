package edu.umich.sbolt.language;

import java.util.*;

import edu.umich.sbolt.language.Patterns.LingObject;
import edu.umich.sbolt.world.WorkingMemoryUtil;
import sml.Identifier;

public class AgentMessageParser
{
    public static String translateAgentMessage(Identifier id){
        String message = null;
        String type = WorkingMemoryUtil.getValueOfAttribute(id, "type");
        System.out.println(type);
        Identifier fieldsId = WorkingMemoryUtil.getIdentifierOfAttribute(id, "fields");
        if(type == null){
            return null;
        } else if(type.equals("different-attribute-question")){
            message = translateDifferentAttributeQuestion(fieldsId);
        } else if(type.equals("value-question")){
            message = translateValueQuestion(fieldsId);
        } else if(type.equals("common-attribute-question")){
            message = translateCommonAttributeQuestion(fieldsId);
        } else if(type.equals("attribute-presence-question")){
            message = translateAttributePresenceQuestion(fieldsId);
        } else if(type.equals("category-of-word")){
            message = translateCategoryQuestion(fieldsId);
        } else if(type.equals("category-of-property")){
            message = translateCategoryPropertyQuestion(fieldsId);
        } else if(type.equals("how-to-measure")){
        	message = String.format("How do I measure %s?", WorkingMemoryUtil.getValueOfAttribute(fieldsId, "property"));
        } else if(type.equals("ambiguous-category")){
        	message = translateAmbiguousCategory(fieldsId);
        } else if(type.equals("describe-object")){
            message = translateDescription(fieldsId);
        } else if(type.equals("dont-know")){
        	message = "I don't know";
        } else if(type.equals("no-prep")){
            message = "I don't know that preposition.";
        } else if(type.equals("single-word-response")){
        	message = WorkingMemoryUtil.getValueOfAttribute(fieldsId, "word");
        } else if(type.equals("no-object")){
        	message = "I do not see the object you are talking about";
        } else if(type.equals("count-response")){
        	int count = Integer.parseInt(WorkingMemoryUtil.getValueOfAttribute(fieldsId, "count"));
        	message = "There " + (count == 1 ? "is" : "are") + " " + count;
        } else if(type.equals("unknown-message")){
        	message = "I was not able to understand your last message";
        } else if(type.equals("teaching-request")){
        	message = translateTeachingRequest(fieldsId);
        } else if(type.equals("which-question")){
        	message = translateWhichQuestion(fieldsId);
        } else if(type.equals("get-next-task")){
        	message = "Waiting for next command...";
        } else if(type.equals("get-next-subaction")){
        	message = "What action should I take next?";
        } else if(type.equals("confirmation")){
        	message = "Okay.";
        } else if (type.equals("get-goal")){
        	message = "What is the goal of the action?";
        } else if (type.equals("restart-task-instruction")){
        	message = "The provided instruction sequence does not lead to the provided goal. Please give the instructions again.";
        } else if(type.equals("request-index-confirmation")){
        	message = translateRequestIndexConfirmation(fieldsId);
        } else if(type.equals("describe-scene")){
            message = translateSceneQuestion(fieldsId);
        } else if(type.equals("describe-scene-objects")){
            message = translateSceneObjectsQuestion(fieldsId);
        } else if(type.equals("list-objects")){
            message = translateObjectsQuestion(fieldsId);
        } else if(type.equals("location-unknown")){
            message = "Relative location of object unknown";
        }
        return message;
    }
    
    private static String translateTeachingRequest(Identifier id){
    	LingObject obj = LingObject.createFromSoarSpeak(id, "description");
    	//JK different syntax for prepositions
    	if (obj.toString().contains("preposition"))
    	    return "I don't know the " + obj.toString() + ". Please teach me with examples";
    	else
    	    return "I don't see " + obj.toString() + ". Please teach me to recognize one";
    }
    
    private static String translateDifferentAttributeQuestion(Identifier id){
        Set<String> exceptions = WorkingMemoryUtil.getAllValuesOfAttribute(id, "exception");
        String exceptionStr = getExceptionString(exceptions);
        LingObject differentObject = LingObject.createFromSoarSpeak(id, "different-object");
        Set<LingObject> similarObjects = LingObject.createAllFromSoarSpeak(id, "similar-object");
        String message = String.format("How does %s differ from ", differentObject.toString());
        for(LingObject obj : similarObjects){
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
    
    private static String translateCategoryQuestion(Identifier id){
        String word = WorkingMemoryUtil.getValueOfAttribute(id, "word");
        return String.format("What kind of attribute is %s?", word);
    }

    private static String translateCategoryPropertyQuestion(Identifier id){
        String word = WorkingMemoryUtil.getValueOfAttribute(id, "word");
        return String.format("What type of property is %s?", word);
    }
    
    private static String translateAmbiguousCategory(Identifier id){
    	Set<String> cats = WorkingMemoryUtil.getAllValuesOfAttribute(id, "result");
    	String word = WorkingMemoryUtil.getValueOfAttribute(id, "word");
    	String s = "By " + word + " do you mean ";
    	int i = 0;
    	for(String cat : cats){
    		if((++i) == cats.size()){
    			s += "or " + cat + "?";
    		} else {
    			s += cat + ", ";
    		}
    	}
    	return s;
    }
    
    private static String translateSceneObjectsQuestion(Identifier id){
        Identifier objects = WorkingMemoryUtil.getIdentifierOfAttribute(id, "objects");
        
        Set<LingObject> object = LingObject.createAllFromSoarSpeak(objects, "object");
        String message = "The objects in the scene are";
        Iterator<LingObject> it = object.iterator();
        if (object.isEmpty())
            return "There are no objects in the scene.";
        while(it.hasNext())
        {
            String obj = it.next().toString();
            if (!it.hasNext() && object.size() > 1)
                message+= " and";
            if (obj.startsWith(" a") || obj.startsWith(" e") || obj.startsWith(" i") || 
                    obj.startsWith(" o") || obj.startsWith(" u"))
            {
                message += " an";
            }
            else
            {
                message += " a";
            }
            message += obj;
            if (it.hasNext() && object.size() > 2)
                message+= ",";
        }
        return message;
    }
    
    private static String translateObjectsQuestion(Identifier id){
        Identifier objects = WorkingMemoryUtil.getIdentifierOfAttribute(id, "objects");
        
        Set<LingObject> object = LingObject.createAllFromSoarSpeak(objects, "object");
        String message = "";
        
        Iterator<LingObject> it = object.iterator();
        if (object.isEmpty())
            return "Nothing.";
        while(it.hasNext())
        {
            String obj = it.next().toString();
            if (!it.hasNext() && object.size() > 1)
                message+= " and";
            if (obj.startsWith(" a") || obj.startsWith(" e") || obj.startsWith(" i") || 
                    obj.startsWith(" o") || obj.startsWith(" u"))
            {
                message += " an";
            }
            else
            {
                message += " a";
            }
            message += obj;
            if (it.hasNext() && object.size() > 2)
                message+= ",";
        }
        return message;
    }
    private static String translateSceneQuestion(Identifier id){
      String prep = WorkingMemoryUtil.getValueOfAttribute(id, "prep");
      String prep2 = prep.replaceAll("-", " ");
      String object1 = LingObject.createFromSoarSpeak(id, "object1").toString();
      String object2 = LingObject.createFromSoarSpeak(id, "object2").toString();
      return "The" + object1 + " is " + prep2 + " the" + object2;
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
    
    private static String translateDescription(Identifier id){
    	
    	if(id == null){
            return null;
        }
    	
    	//kind of a hack :(
    	Identifier objectId = WorkingMemoryUtil.getIdentifierOfAttribute(id, "object");
    	if (objectId == null)
    		return "nothing";
    	// CK: choose a/an correctly
    	String ret = LingObject.createFromSoarSpeak(id, "object").toString();
    	if(ret.matches("^ [aeiouAEIOU].*")) {
    		ret = "An"+ret;
    	} else {
    		ret = "A"+ret;
    	}
        return ret;
    }
    
    
    private static String translateWhichQuestion(Identifier id){
    	Identifier objectId = WorkingMemoryUtil.getIdentifierOfAttribute(id, "description");
    	if (objectId == null)
    		return "Which one?";
        return "Which " + LingObject.createFromSoarSpeak(id, "description") + "?";
    }

    private static String translateRequestIndexConfirmation(Identifier id){
    	LingObject obj = LingObject.createFromSoarSpeak(id, "object");
    	return "Is this " + obj.toString() + "?";
    }
    
}
