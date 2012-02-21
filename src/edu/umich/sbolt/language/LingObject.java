package edu.umich.sbolt.language;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.*;

import edu.umich.sbolt.world.WorkingMemoryUtil;

import sml.*;


public class LingObject extends LinguisticEntity {
	private String determiner = null;
	private Set<String> adjective;
	private String noun;
	
	LingObject (){
		
	}
	
	public String getDeterminer(){
	    return determiner;
	}
	
	public Set<String> getAdjectives(){
	    return adjective;
	}
	
	public String getNoun(){
	    return noun;
	}
	
	public void extractLinguisticComponents(String string, Map tagsToWords){
		adjective = new HashSet();
		Pattern p = Pattern.compile("DT\\d*");
		Matcher m = p.matcher(string);
		if(m.find()){
			determiner = tagsToWords.get(m.group()).toString();
		}
		
		p = Pattern.compile("JJ\\d*");
		m = p.matcher(string);
		while(m.find()){
			adjective.add(tagsToWords.get(m.group()).toString());
		}
		
		p = Pattern.compile("NN\\d*");
		m = p.matcher(string);
		while(m.find()){
			noun = tagsToWords.get(m.group()).toString();
		}
	}
	
	@Override
	public void translateToSoarSpeak(Identifier id, String connectingString) {
		Identifier objectId = id.CreateIdWME(connectingString);
	    objectId.CreateStringWME("word", noun);
		if (determiner != null){
			objectId.CreateStringWME("determiner", determiner);
		}
		if (adjective != null){
			String adj;
			Iterator itr = adjective.iterator();
			while (itr.hasNext()){
				objectId.CreateStringWME("adjective", itr.next().toString());
			}
		}
	}
	
	public static LingObject createFromSoarSpeak(Identifier id){
	    // Assumes id is the root of the object
        if(id == null){
            return null;
        }
	    LingObject lingObject = new LingObject();
        lingObject.noun = WorkingMemoryUtil.getValueOfAttribute(id, "word");
        lingObject.adjective = WorkingMemoryUtil.getAllValuesOfAttribute(id, "adjective");
        lingObject.determiner = WorkingMemoryUtil.getValueOfAttribute(id, "determiner");
        return lingObject;
	}
	
	public static LingObject createFromSoarSpeak(Identifier id, String name){
	    // Assumes id ^name <objectId> is the root of the object
        if(id == null){
            return null;
        }
        Identifier objectId = WorkingMemoryUtil.getIdentifierOfAttribute(id, name);
        return LingObject.createFromSoarSpeak(objectId);
	}
	
    public static Set<LingObject> createAllFromSoarSpeak(Identifier id, String name){
        Set<LingObject> lingObjects = new HashSet<LingObject>();
        for(int index = 0; index < id.GetNumberChildren(); index++){
            WMElement wme = id.GetChild(index);
            if(wme.GetAttribute().equals(name)){
                lingObjects.add(LingObject.createFromSoarSpeak(wme.ConvertToIdentifier()));
            }
        }
        return lingObjects;
    }
    
    @Override
    public String toString(){
        String adjString = "";
        for(Iterator<String> i = adjective.iterator(); i.hasNext(); ){
            adjString += i.next() + " ";
        }        
        return String.format("the %s%s", adjString, noun);
    }
}
