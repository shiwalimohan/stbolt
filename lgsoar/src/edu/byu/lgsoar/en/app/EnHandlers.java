package edu.byu.lgsoar.en.app;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import sml.Kernel;
import edu.byu.lgsoar.app.base.SoarApplication;

import edu.byu.lgsoar.utils.Constants;
import edu.byu.lgsoar.utils.Miscellaneous;

import net.sf.jlinkgrammar.Linkage;
import net.sf.jlinkgrammar.Sentence;

public class EnHandlers {
	private static String currentSentence;
	private static Queue<String> wordQueue;
	
	/**
	 * Reads in a sentence from an external file. The file path is specified by Constants.SENTENCE_FILE.
	 */
	public static Kernel.RhsFunctionInterface initWordStackFromFile(){
		return new Kernel.RhsFunctionInterface() {
			@Override
			public String rhsFunctionHandler(int eventID, Object data,
					String agentName, String functionName, String argument) {
				wordQueue = Miscellaneous.readFileToQueue(Constants.getProperty("SENTENCE_FILE"));
//				System.out.println("Initializing word queue to: " + wordQueue);
				currentSentence = Miscellaneous.toString(wordQueue);
//				System.out.println("current sentence is " + currentSentence);
				return wordQueue.toString();
			}
		};
	}

	/**
	 * Reads in a sentence from an external file. The file path is specified by Constants.SENTENCE_FILE.
	 */
	public static Kernel.RhsFunctionInterface getStringFromFile(){
		return new Kernel.RhsFunctionInterface() {
			@Override
			public String rhsFunctionHandler(int eventID, Object data,
					String agentName, String functionName, String argument) {
				wordQueue = Miscellaneous.readFileToQueue(Constants.getProperty("SENTENCE_FILE"));
//				System.out.println("Initializing word queue to: " + wordQueue);
				currentSentence = Miscellaneous.toString(wordQueue);
				return currentSentence;
			}
		};
	}
	
	
	/**
	 * @return handler to poll the word queue and return the next word.
	 */
	public static Kernel.RhsFunctionInterface popword(){
		return new Kernel.RhsFunctionInterface() {
			@Override
			public String rhsFunctionHandler(int eventID, Object data,
					String agentName, String functionName, String argument) {
				String temp = wordQueue.poll();
//				System.out.println("returning word " + temp);
				return temp;
			}
		};
	}
	
	/**
	 * Creates the word queue using the value of nextSentence, which can
	 * be set using setNextSentence.
	 */
	public static Kernel.RhsFunctionInterface initWordStackFromClass(){
		return new Kernel.RhsFunctionInterface() {
			@Override
			public String rhsFunctionHandler(int eventID, Object data,
					String agentName, String functionName, String argument) {
				wordQueue = new LinkedList<String>();
				SoarApplication sa = (SoarApplication) data;
				for(String s :  sa.getCurrentSentence().split(" "))
					wordQueue.offer(s);
				currentSentence = sa.getCurrentSentence();
				return wordQueue.toString();
			}
		};
	}
	
	private static String lgsentWME;
	private static Kernel kern;
	private static String agName;
	
	public static Kernel.RhsFunctionInterface callLGParser(){
		return new Kernel.RhsFunctionInterface() {
			@Override
			public String rhsFunctionHandler(int eventID, Object data,
					String agentName, String functionName, String argument) {
				
				kern = ((SoarApplication) data).getKernel();
				agName = agentName;
				
				int space1 = argument.indexOf(" ");
				lgsentWME = argument.substring(0, space1);
				String lgIn = argument.substring(space1+2);
				String[] argsx2 = {lgIn};
				
				try {
					net.sf.jlinkgrammar.parser.doIt(argsx2);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return "success";
			}
		};
	}
	
	// called from parser.java
	public static void loadLinkage (Linkage thisLinkage, Sentence sent) {
		
        int     rWordIndex;
        int     lWordIndex;
        String  leftWord;
        String  rightWord;
        String  linkLabel;
        
        String outstr = thisLinkage.linkage_print_diagram();
		System.out.println(outstr);
		
        // Normally you loop through sublinkages
        int n = thisLinkage.linkage_get_num_sublinkages();
        // For now only choose the first sublinkage
        thisLinkage.linkage_set_current_sublinkage(0);
        int numLinks = thisLinkage.linkage_get_num_links();
        
        // make a wme for the count
        kern.ExecuteCommandLine("add-wme " + lgsentWME + " count 0", agName);
        	
        // make a wme for the words
        kern.ExecuteCommandLine("add-wme " + lgsentWME + " words *", agName);
        String wordsWME = Miscellaneous.getLastWME(lgsentWME, agName, kern);
        int numWords = sent.sentence_length();
        // now load the words
        for (int wordx = 0; wordx < numWords; wordx++) {
            // add ^word information for this link
            String wordval = sent.sentence_get_word(wordx);
            // there's some kind of bug in the add-wme code...
            if (wordval.equals(".")) {
            	wordval = "|.|";
            }
            kern.ExecuteCommandLine("add-wme " + wordsWME + " word *", agName);
            String latestWME = Miscellaneous.getLastWME(wordsWME, agName, kern);
            kern.ExecuteCommandLine("add-wme " + latestWME + " wcount " + wordx, agName); 
            kern.ExecuteCommandLine("add-wme " + latestWME + " lg-wvalue " + wordval, agName); 
        }
        
        // DWL: we had tokens in the old code, but they don't work in the java version; we're not really using them anyway         
    
        // make a wme for the links
        kern.ExecuteCommandLine("add-wme " + lgsentWME + " links *", agName);
        String linksWME = Miscellaneous.getLastWME(lgsentWME, agName, kern);
        // now load the links
        for (int linkIndex = 0; linkIndex < numLinks; linkIndex++) {
            rWordIndex = thisLinkage.linkage_get_link_rword(linkIndex);
            lWordIndex = thisLinkage.linkage_get_link_lword(linkIndex);
            rightWord = thisLinkage.word[rWordIndex];
            leftWord = thisLinkage.word[lWordIndex];
            linkLabel = thisLinkage.linkage_get_link_label(linkIndex);
           
            String pattern = "([A-Z]+)([a-z]*)";
            String ltype = linkLabel;
            ltype = ltype.replaceAll(pattern, "$1");
            String lsubtype = linkLabel;
            lsubtype = lsubtype.replaceAll(pattern, "$2");
            
            // add ^link information for this link
            kern.ExecuteCommandLine("add-wme " + linksWME + " link *", agName);
            String latestWME = Miscellaneous.getLastWME(linksWME, agName, kern);
            kern.ExecuteCommandLine("add-wme " + latestWME + " lvalue " + linkLabel, agName);
            kern.ExecuteCommandLine("add-wme " + latestWME + " lwleft " + lWordIndex, agName);
            kern.ExecuteCommandLine("add-wme " + latestWME + " lwright " + rWordIndex, agName);            
            kern.ExecuteCommandLine("add-wme " + latestWME + " ltype " + ltype, agName);
            if (lsubtype.length() > 0) {
                kern.ExecuteCommandLine("add-wme " + latestWME + " ltypesub " + lsubtype, agName);	
            }
        }		
	}
}