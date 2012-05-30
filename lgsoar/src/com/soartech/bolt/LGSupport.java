package com.soartech.bolt;

import net.sf.jlinkgrammar.Linkage;
import net.sf.jlinkgrammar.Sentence;
import net.sf.jlinkgrammar.parser;
import sml.Agent;
import sml.Agent.OutputEventInterface;
import sml.Identifier;
import sml.WMElement;

public class LGSupport implements OutputEventInterface {
	private static Agent agent;
	public static String dictionaryPath = ""; 
	private static Identifier lgInputRoot;
	private static int sentenceCount = -1;
	private static int currentOutputSentenceCount = -1;
	
	public static parser theParser;
	
	public LGSupport(Agent _agent, String dictionary) {
		agent = _agent;
		dictionaryPath = dictionary;
		theParser = new parser();
		
		// make a root lg-input WME
		if (agent != null) {
			lgInputRoot = agent.CreateIdWME(agent.GetInputLink(), "lg");

			agent.AddOutputHandler("preprocessed-sentence", this, null);
		}
	}
	
	public void handleSentence(String sentence) {
		
		if (agent == null) {
			// no Soar, run parser directly on sentence
			theParser.parseSentence(sentence);
		}
		else {
			// load the sentence into WM
			sentenceCount++;
			originalSentenceToWM(sentence);
			
			// Soar rules may modify it
			
			// wait for preprocessed sentence on output
		}
	}
	
	private void originalSentenceToWM(String sentence) {
		Identifier root = agent.CreateIdWME(lgInputRoot, "original-sentence");
        agent.CreateIntWME(root, "sentence-count", sentenceCount);
        Identifier wordsWME = agent.CreateIdWME(root, "words");
        sentence = sentence.replaceAll("(\\W)", " $1");
        //System.out.println("padded: " + sentence);
        String[] words = sentence.split("\\s+");
        
        for (int i=0; i<words.length; i++) {
        	Identifier wordWME = agent.CreateIdWME(wordsWME, "word");
        	agent.CreateStringWME(wordWME, "wvalue", words[i]);
        	agent.CreateIntWME(wordWME, "wcount", i);
        	//System.out.println("wd " + words[i]);
        }

	}
	
	
	public void outputEventHandler(Object data, String agentName,
			String attributeName, WMElement pWmeAdded) {
		String sentence = preprocessedSentenceFromWM(pWmeAdded);

		// call LG Parser
		theParser.parseSentence(sentence);
	}
	
	private String preprocessedSentenceFromWM(WMElement pWmeAdded) {
		String result = "";
		
		WMElement currentWME = pWmeAdded.ConvertToIdentifier().FindByAttribute("start", 0);
		
		currentOutputSentenceCount = Integer.parseInt(pWmeAdded.ConvertToIdentifier().GetParameterValue("sentence-count"));
		
		while (currentWME != null) {
			String word = currentWME.ConvertToIdentifier().GetParameterValue("word");
			if (word != null) {
				result += " " + word;
			}
			currentWME = currentWME.ConvertToIdentifier().FindByAttribute("next", 0);
		}
		
		//System.out.println("got sentence: " + result);
		return result;
	}
	
	// called from parser.java
	// doIt (called above) will call this once for each linkage (parse)
	public static void loadLinkage(Linkage thisLinkage, int idx, Sentence sent) {
		int     rWordIndex;
        int     lWordIndex;
        String  linkLabel;
        
        // combine all sublinkages to one
        thisLinkage.linkage_compute_union();
        
        String message = thisLinkage.linkage_print_diagram();
        System.out.println(message);

		// this ideally should be injected into the Soar print stream,
		// but that doesn't seem possible. Echo command doesn't seem to do it.
		
		if (agent == null) {
			// valid if run to print the parse alone

			return;
		}
		
        int numLinks = thisLinkage.linkage_get_num_links();
        
       // make a root for this sentence
        Identifier sentenceRoot = agent.CreateIdWME(lgInputRoot, "parsed-sentence");
        
        // make a wme for the count
        agent.CreateIntWME(sentenceRoot, "sentence-count", currentOutputSentenceCount);
       
        agent.CreateIntWME(sentenceRoot, "parse-count", nextParseCount(currentOutputSentenceCount));
        	
        // make a wme for the words
        Identifier wordsWME = agent.CreateIdWME(sentenceRoot, "words");
        
        int numWords = sent.sentence_length();
        // now load the words
        for (int wordx = 0; wordx < numWords; wordx++) {
            // add ^word information for this link
            String wordval = sent.sentence_get_word(wordx);
 
            Identifier wordWME = agent.CreateIdWME(wordsWME, "word");
            agent.CreateIntWME(wordWME, "wcount", wordx);
            agent.CreateStringWME(wordWME, "wvalue", wordval);
        }
            
        // make a wme for the links
        Identifier linksWME = agent.CreateIdWME(sentenceRoot, "links");
        
        String noStarsPattern = "\\*";
        String idiomPattern = "ID.*";
        String pattern = "([A-Z]+)([a-z]*)";
        
        // now load the links
        for (int linkIndex = 0; linkIndex < numLinks; linkIndex++) {
            rWordIndex = thisLinkage.linkage_get_link_rword(linkIndex);
            lWordIndex = thisLinkage.linkage_get_link_lword(linkIndex);
            linkLabel = thisLinkage.linkage_get_link_label(linkIndex);
           
            // SBW 3/8/12
            // remove all *'s from the link names
            // these indicate "any subtype in this position"
            // not sure what to do with them, but they definitely shouldn't be stuck to the main type
            linkLabel = linkLabel.replaceAll(noStarsPattern, "");
            
            String ltype = linkLabel;
            ltype = ltype.replaceAll(pattern, "$1");
            ltype = ltype.replaceAll(idiomPattern, "ID");
            String lsubtype = linkLabel;
            lsubtype = lsubtype.replaceAll(pattern, "$2");
            
            // add ^link information for this link
            Identifier linkWME = agent.CreateIdWME(linksWME, "link");
                        
            agent.CreateStringWME(linkWME, "lvalue", linkLabel);
            agent.CreateIntWME(linkWME, "lwleft", lWordIndex);
            agent.CreateIntWME(linkWME, "lwright", rWordIndex);
            agent.CreateStringWME(linkWME, "ltype", ltype);
           
            // make a separate WME for each subtype
            // assumption: subtype ordering doesn't matter
        	for (int i=0; i< lsubtype.length(); i++) {
        		agent.CreateStringWME(linkWME, "ltypesub", lsubtype.substring(i, i+1));
        	}
          
        }		
	}
	
	private static int lastSentenceCount = -1;
	private static int currentParseCount = -1;
	
	private static int nextParseCount(int sentenceCount) {
		// can't just use LG parse indices, since the same sentence can be reparsed at Soar's request,
		// reparses should have the same sentence count and extend the parse indices of the original
	
		if (sentenceCount != lastSentenceCount) {
			currentParseCount = 0;
			lastSentenceCount = sentenceCount;
		}
		else {
			currentParseCount++;
		}
		return currentParseCount;
	}
}
