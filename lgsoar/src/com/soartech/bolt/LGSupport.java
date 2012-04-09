package com.soartech.bolt;

import java.io.IOException;

import net.sf.jlinkgrammar.Linkage;
import net.sf.jlinkgrammar.Sentence;
import sml.Agent;
import sml.Identifier;

public class LGSupport {
	private static Agent agent;
	public static String dictionaryPath = ""; //"/opt/bolt/stbolt/lgsoar/data/link";
	//private static String lgSoarLoaderPath = "/opt/bolt/stbolt/lgsoar/soarcode/simple-init.soar";
	private static Identifier lgInputRoot;
	private static int sentenceCount = 0;
	
	public LGSupport(Agent _agent, String dictionary) {
		agent = _agent;
		dictionaryPath = dictionary;
		
		// make a root lg-input WME
        lgInputRoot = agent.CreateIdWME(agent.GetInputLink(), "lg");
		
	}
	
	public void handleSentence(String sentence) {
		try {
			net.sf.jlinkgrammar.parser.doIt(new String[]{sentence});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// called from parser.java
	public static void loadLinkage(Linkage thisLinkage, Sentence sent) {
		int     rWordIndex;
        int     lWordIndex;
        String  linkLabel;
        
        String outstr = thisLinkage.linkage_print_diagram();
		System.out.println(outstr);
		
        // Normally you loop through sublinkages
        // int n = thisLinkage.linkage_get_num_sublinkages();
        // For now only choose the first sublinkage
        thisLinkage.linkage_set_current_sublinkage(0);
        int numLinks = thisLinkage.linkage_get_num_links();
        
       // make a root for this sentence
        Identifier sentenceRoot = agent.CreateIdWME(lgInputRoot, "sentence");
        
        // make a wme for the count
        agent.CreateIntWME(sentenceRoot, "count", sentenceCount);
        sentenceCount++;
        	
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
            String lsubtype = linkLabel;
            lsubtype = lsubtype.replaceAll(pattern, "$2");
            
            // add ^link information for this link
            Identifier linkWME = agent.CreateIdWME(linksWME, "link");
                        
            agent.CreateStringWME(linkWME, "lvalue", linkLabel);
            agent.CreateIntWME(linkWME, "lwleft", lWordIndex);
            agent.CreateIntWME(linkWME, "lwright", rWordIndex);
            agent.CreateStringWME(linkWME, "ltype", ltype);
           
            if (lsubtype.length() > 0) {
                agent.CreateStringWME(linkWME, "ltypesub", lsubtype);
            }
        }		
	}
}
