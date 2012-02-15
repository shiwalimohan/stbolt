package edu.umich.sbolt.language;

import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


// will parse only positive predicates for now
public class ObjectRelation extends BOLTRegex{
	String preposition;
	private LingObject object1;
	private LingObject object2;
	static {
		regexSet = new HashSet<Pattern>();
		regexSet.add( Pattern.compile("(OBJ\\d* )(is\\d* )(PP\\d* )(OBJ\\d*)"));
		tag = "REL";
	}
	
	ObjectRelation (String string, Map tagsToWords){
		//get preposition 
		Pattern p = Pattern.compile("PP\\d*");
		Matcher m = p.matcher(string);
		if(m.find()){
			preposition = tagsToWords.get(m.group()).toString();
		//	System.out.println(preposition);
		}
		
		//get object1
		p = Pattern.compile("OBJ\\d*");
		m = p.matcher(string);
		if(m.find()){
			object1 = (LingObject) tagsToWords.get(m.group());
		}
		if(m.find()){
			object2 = (LingObject) tagsToWords.get(m.group());
		}
		
	}
	
	public static String extract(String tagString, Map<String, Object> tagsToWords, int Counter){
		Matcher m = getRegex().matcher(tagString);
		if(m.find()){
			StringBuffer sb = new StringBuffer();
			do {
				//	System.out.println(m.group());
				ObjectRelation rel = new ObjectRelation(m.group(),tagsToWords);
				String newTag = tag+Integer.toString(Counter);
				tagsToWords.put(newTag, rel);
				m.appendReplacement(sb, newTag);
				Counter++;
			}while(m.find());
			return sb.toString();
		}
		return tagString;
	}
}
