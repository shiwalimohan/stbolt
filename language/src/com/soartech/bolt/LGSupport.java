package com.soartech.bolt;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

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
	private static boolean phraseMode = false;
	private static Vector<Identifier> inputWMEs = new Vector<Identifier>();
	
	
	public static parser theParser;
	private static boolean filterWords = false;
	private static Set<String> legalWords;
	

	public LGSupport(Agent _agent, String dictionary, String legalWordList) {
		agent = _agent;
		dictionaryPath = dictionary;
		
		if (legalWordList != null) {
			fillLegalWordSet(legalWordList);
		}
		
		theParser = new parser();
		
		// make a root lg-input WME
		if (agent != null) {
			lgInputRoot = agent.CreateIdWME(agent.GetInputLink(), "lg");

			agent.AddOutputHandler("preprocessed-sentence", this, null);
		}
	}
	
	private void fillLegalWordSet(String file) {
		// format for legal word file:
		// each line is a single word (spaces will be removed)
		// if the first character (in the first col) is #, it will be skipped
		
		legalWords = new HashSet<String>();
		
		// fake words used in the LG algorithm
		legalWords.add("LEFT-WALL");
		legalWords.add("RIGHT-WALL");
		legalWords.add("UNKNOWN-WORD");
		legalWords.add("NOUN-PHRASE-WALL");
		
		// punctuation
		legalWords.add("?");
		legalWords.add(".");
		legalWords.add(",");
		legalWords.add(";");
		legalWords.add(":");
		legalWords.add("!");
		
		// add words read from file
		
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(file));
		}
		catch (FileNotFoundException e) {
			System.out.println("ERROR: whitelist file does not exist: " + file);
			return;
		}
		String line;
		try {
			while((line = reader.readLine() ) != null) {
				if (line.length() > 0 && line.charAt(0) != '#') {
					// remove all chars that aren't a unicode letter
					line = line.replaceAll("[^\\p{L}]", "");
					legalWords.add(line);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		filterWords = true;
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
        inputWMEs.add(root);
        
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
	
	public void clear() {
		for (Identifier id: inputWMEs) {
			 agent.DestroyWME(id);
		}
		sentenceCount = -1;
	}
	
	public void outputEventHandler(Object data, String agentName,
			String attributeName, WMElement pWmeAdded) {
		String sentence = preprocessedSentenceFromWM(pWmeAdded);
		if(sentence == null){
			return;
		}
		sentence = sentence.trim(); // some substitutions in Soar may cause leading spaces
		
		// call LG Parser
		theParser.parseSentence(sentence);
		
		// add noun-phrase parses
		// NOUN-PHRASE-WALL should be in words.v.4.1, it is a verb that will let any valid noun phrase attach to it
		// Also make sure the first letter of the input isn't capitalized, otherwise LG won't recognize it since it
		// is a capitalized word in the middle of the sentence.
		phraseMode = true;
		theParser.parseSentence("NOUN-PHRASE-WALL " + sentence.substring(0,2).toLowerCase() + sentence.substring(2)); 
		phraseMode = false;
	}	
	
	private String preprocessedSentenceFromWM(WMElement pWmeAdded) {
		String result = "";
		
		if (pWmeAdded == null) {
			System.err.println("LGSupport.preprocessedSentenceFromWM: pWmeAdded is null");
			return null;
		}
		
		WMElement currentWME = pWmeAdded.ConvertToIdentifier().FindByAttribute("start", 0);
		String param = pWmeAdded.ConvertToIdentifier().GetParameterValue("sentence-count");
		if (param == null) {
			System.err.println("LGSupport.preprocessedSentenceFromWM: no sentence-count");
			return null;
		}
		currentOutputSentenceCount = Integer.parseInt(param);
		
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
        
        int numWords = sent.sentence_length();
        
        // identify guessed words. AFAIK, the only way these show up is in the linkage words,
        // which are what printed in the parse picture:

        //        +------------------Xp-----------------+
        //        |         +---------Os---------+      |
        //        +----Wi---+         +-----A----+      |
        //        |         |         |          |      |
        //    LEFT-WALL xget[?].v xthe[?].a xblock[?].n .
        
        // So we need to go through each word and look for a ? as the fourth-to-last character.
        
        int numGuesses = 0;
        boolean isGuess[] = new boolean[numWords];
        boolean isSkip[] = new boolean[numWords];
        for (int i=0; i<numWords; i++) {
        	String wordAsPrinted = thisLinkage.word[i];
        	char c = 'x';
        	if (wordAsPrinted.length() > 3) {
        		c = wordAsPrinted.charAt(wordAsPrinted.length()-4);
        	}
        	if (c == '?') {
        		isGuess[i] = true;
        		numGuesses++;
        	}
        	else {
        		isGuess[i] = false;
        	}
        	c = wordAsPrinted.charAt(0);
        	if (c == '[') {
        		isSkip[i] = true;
        	} else {
        		isSkip[i] = false;
        	}
        }
        
        String message = thisLinkage.linkage_print_diagram();
        System.out.println(message);
        
        int disCost = thisLinkage.linkage_disjunct_cost();
        int unusedCost = thisLinkage.linkage_unused_word_cost();
        int parseCount = nextParseCount(currentOutputSentenceCount);
        System.out.println("^^ parse " + parseCount + ": DIS = " + disCost + " UNUSED = " + unusedCost);

		// this ideally should be injected into the Soar print stream,
		// but that doesn't seem possible. Echo command doesn't seem to do it.
		
		if (agent == null) {
			// valid if run to print the parse alone
			return;
		}
		
        int numLinks = thisLinkage.linkage_get_num_links();
        
       // make a root for this sentence
        Identifier sentenceRoot = agent.CreateIdWME(lgInputRoot, "parsed-sentence");
        inputWMEs.add(sentenceRoot);
        
        // make a wme for the count
        agent.CreateIntWME(sentenceRoot, "sentence-count", currentOutputSentenceCount);
       
        agent.CreateIntWME(sentenceRoot, "parse-count", parseCount);
        	
        // make a wme for the words
        Identifier wordsWME = agent.CreateIdWME(sentenceRoot, "words");
        
        // now load the words
        for (int wordx = 0; wordx < numWords; wordx++) {
            // add ^word information for this link
            String wordval = sent.sentence_get_word(wordx);
 
            Identifier wordWME = agent.CreateIdWME(wordsWME, "word");

            agent.CreateIntWME(wordWME, "wcount", wordx);
            agent.CreateStringWME(wordWME, "wvalue", wordval);
            
            if (isGuess[wordx]) {
            	agent.CreateStringWME(wordWME, "guessed", "true");
            }
            if(isSkip[wordx]) {
            	agent.CreateStringWME(wordWME, "skipped", "true");
            }
            
            // if parsing as a phrase, we have added an extra word at the beginning
            // Soar needs to know which words are equivalent across parses, so the
            // phraseMode flag allows this to start phrases at index 0 rather than 1
            // so word indices are equivalent
            
            // don't change the wcount, though, since LGSoar wants that to always start at 0
            if (!phraseMode) {
            	agent.CreateIntWME(wordWME, "global-wcount", wordx);
            }
            else {
            	agent.CreateIntWME(wordWME, "global-wcount", wordx - 1);
            }
        }
            
        // make a wme for the links
        Identifier linksWME = agent.CreateIdWME(sentenceRoot, "links");
        agent.CreateIntWME(sentenceRoot, "unused-word-cost", unusedCost);
        agent.CreateIntWME(sentenceRoot, "expensive-link-cost", disCost);
        agent.CreateIntWME(sentenceRoot, "unknown-word-cost", numGuesses);
        
        String noStarsPattern = "\\*";
        String noCaratPattern = "\\^"; // carat is apparently "match nothing except *". occurs for lots of conjunctions.
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
            linkLabel = linkLabel.replaceAll(noCaratPattern, "");
            
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
	
	public static boolean isAllowedDictionaryWord(String word) {
		// return true if we will permit the LG parser to use its dictionary entry (assuming there is one) for the word
		if (!filterWords) {
			return true;
		}
		
		if (word.length() == 0) {
			return false;
		}
		
		if (word.substring(0,1).equals("*")) {
			// all generics/placeholders are prefixed with a star
			return true;
		}
		
		else {
			return legalWords.contains(word);
		}
	}
}
