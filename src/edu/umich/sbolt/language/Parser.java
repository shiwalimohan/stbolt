package edu.umich.sbolt.language;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import sml.Agent;
import sml.Identifier;
import sml.Kernel;

public class Parser {
	
	private String languageSentence;
	
	private BOLTDictionary dictionary;
	
	private Map<String,Object> tagsToWords;
	
	private String tagString;
	
	private List<EntityPattern> entityPatterns;
	
	public Parser(BOLTDictionary dictionary) {
		this.dictionary = dictionary;
		parseXMLGrammar();
	}
	
	private void parseXMLGrammar(){
        entityPatterns = new ArrayList<EntityPattern>();
        
        //get the factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {

            //Using factory get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();

            //parse using builder to get DOM representation of the XML file
            Document dom = db.parse("src/edu/umich/sbolt/language/grammar.xml");

            //get the root element
            Element docEle = dom.getDocumentElement();

            //get a nodelist of elements
            NodeList nl = docEle.getElementsByTagName("Pattern");
            if(nl == null){
                return;
            }
            
            //Go through each pattern in the xml document and create an EntityPattern
            for(int i = 0; i < nl.getLength(); i++){
                EntityPattern pattern = new EntityPattern();
                
                Element patternElement = (Element)nl.item(i);
                NodeList children = patternElement.getChildNodes();
                for(int j = 0; j < children.getLength(); j++){
                    Node childNode = children.item(j);
                    if(childNode.getNodeName().equals("Regex")){
                        pattern.regex = Pattern.compile(childNode.getTextContent());
                    } else if(childNode.getNodeName().equals("Tag")){
                        pattern.tag = childNode.getTextContent();
                    } else if(childNode.getNodeName().equals("EntityType")){
                        pattern.entityType = childNode.getTextContent();
                    }
                }
                //System.out.println(pattern.entityType + "; " + pattern.tag + "; " + pattern.regex);
                entityPatterns.add(pattern);
            }

        }catch(ParserConfigurationException pce) {
            pce.printStackTrace();
        }catch(SAXException se) {
            se.printStackTrace();
        }catch(IOException ioe) {
            ioe.printStackTrace();
        }
	}
	
    public void getSoarSpeak(String latestMessage, Identifier messageId) {
        this.languageSentence = latestMessage;
        System.out.println(latestMessage);
        tagsToWords = new LinkedHashMap();
        mapTagToWord();
        this.tagString = getPOSTagString();
        traslateToSoarSpeak(messageId, getParse());
    }
	
	// create tags to words mappings
	private void mapTagToWord(){
		String[] wordSet = languageSentence.split(" ");
		String tag;
		for (int i = 0; i < wordSet.length; i++){
			tag = dictionary.getTag(wordSet[i]);
			tagsToWords.put(tag.concat(Integer.toString(i)),wordSet[i]);
		}
	}
	
	//create a sentence of POS tags.
	private String getPOSTagString(){
		String tagString = "";
		Iterator itr = tagsToWords.entrySet().iterator();
		while(itr.hasNext()){
			Map.Entry pair = (Map.Entry)itr.next();
			tagString = tagString+pair.getKey().toString()+" ";
		}
		return tagString.trim();
	}
	

	//parse linguistic elements from the POS tagString.
	public String getParse() {
		ParserUtil util = new ParserUtil();
		for(EntityPattern pattern : entityPatterns){
		    System.out.println(pattern.entityType);
            System.out.println(tagString);
		    tagString = util.extractPattern(pattern, tagString, tagsToWords);
            System.out.println(tagString);
            System.out.println(tagsToWords);
		}
		/*
//		System.out.println("Sentence POS: " + tagString);
		tagString = util.extractObject(tagString, tagsToWords, Counter);
//		System.out.println("Parsed objects: " + tagString);
		tagString = util.extractObjectRelation(tagString, tagsToWords, Counter);
//		System.out.println("Parsed relations: " + tagString);
		tagString = util.extractVerbCommand(tagString, tagsToWords, Counter);
//		System.out.println("Parsed verb-command: " + tagString);
		tagString = util.extractGoalInfo(tagString, tagsToWords, Counter);
//		System.out.println("Parsed goal: " + tagString);
		tagString = util.extractProposalInfo(tagString, tagsToWords, Counter);
//		System.out.println("Parsed proposal: " + tagString);
		tagString = util.extractSentence(tagString, tagsToWords, Counter);
//		System.out.println("Parsed sentence: " + tagString);
 * */
		return tagString;
	}
	
	//Get Soar structure
	public void traslateToSoarSpeak(Identifier messageId, String tagString){
		//System.out.println("Translating sentence");
	    Object o = tagsToWords.get(tagString);
	    Sentence s = (Sentence)o;
		((Sentence) tagsToWords.get(tagString)).translateToSoarSpeak(messageId, null);
	}
}
