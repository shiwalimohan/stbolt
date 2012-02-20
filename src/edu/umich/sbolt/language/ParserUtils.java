package edu.umich.sbolt.language;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserUtils {
	
	public static String extractObject(String tagString, Map<String, Object> tagsToWords, int Counter) {
		Pattern regex = Pattern.compile("(DT\\d* )?(JJ\\d* )*(NN\\d*)");
		String tag = "OBJ";
		Matcher m = regex.matcher(tagString);
		if(m.find()){
			StringBuffer sb = new StringBuffer();
			do{	
				LingObject obj = new LingObject();
				obj.extractLinguisticComponents(m.group(),tagsToWords);
				String newTag = tag+Integer.toString(Counter);
				tagsToWords.put(newTag, obj);
				m.appendReplacement(sb, newTag);
				Counter++;
			}while(m.find());
			return sb.toString();
		}
		return tagString;
	}
	
	public static String extractObjectRelation(String tagString, Map<String, Object> tagsToWords, int Counter){
		Pattern regex = Pattern.compile("(OBJ\\d* )(is\\d* )(PP\\d* )(OBJ\\d*)");
		String tag = "REL";
		Matcher m = regex.matcher(tagString);
		if(m.find()){
			StringBuffer sb = new StringBuffer();
			do {
				//	System.out.println(m.group());
				ObjectRelation rel = new ObjectRelation();
				rel.extractLinguisticComponents(m.group(),tagsToWords);
				String newTag = tag+Integer.toString(Counter);
				tagsToWords.put(newTag, rel);
				m.appendReplacement(sb, newTag);
				Counter++;
			}while(m.find());
			return sb.toString();
		}
		return tagString;
	}
	
	public static String extractSentence(String tagString, Map<String, Object> tagsToWords, int Counter){
		Pattern regex = Pattern.compile("(VBC\\d*)|(GS\\d*)|(PS\\d*)|(REL\\d*)");
		String tag = "SEN";
		Matcher m = regex.matcher(tagString);
		if(m.find()){
			StringBuffer sb = new StringBuffer();
			do {
				//	System.out.println(m.group());
				Sentence sen = new Sentence();
				sen.extractLinguisticComponents(m.group(), tagsToWords);
				String newTag = tag+Integer.toString(Counter);
				tagsToWords.put(newTag, sen);
				m.appendReplacement(sb, newTag);
				Counter++;
			}while(m.find());
			return sb.toString();
		}
		return tagString;
	}
	
	public static String extractVerbCommand(String tagString, Map<String, Object> tagsToWords, int Counter) {
		Pattern regex = Pattern.compile("((VB\\d* )(OBJ\\d* )(PP\\d* )(OBJ\\d*))|((VB\\d* )(OBJ\\d*))|(VB\\d*)");
		String tag = "VBC";
		Matcher m = regex.matcher(tagString);
		if(m.find()){
			StringBuffer sb = new StringBuffer();
			do {
				//	System.out.println(m.group());
				VerbCommand rel = new VerbCommand();
				rel.extractLinguisticComponents(m.group(),tagsToWords);
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
