package com.soartech.bolt;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

import net.sf.jlinkgrammar.Linkage;
import net.sf.jlinkgrammar.Sentence;

import sml.Agent;
import sml.smlPrintEventId;
import sml.Agent.PrintEventInterface;
import sml.Kernel;

import edu.umich.soar.debugger.SWTApplication;


public class SoarRunner implements PrintEventInterface {
	public static String dictionaryPath = "/opt/bolt/stbolt/lgsoar/data/link";
	private static String lgSoarLoaderPath = "/opt/bolt/stbolt/lgsoar/soarcode/lg93init.soar";
	
	private static Agent agent = null;
	private static Kernel kernel = null;
	
	private static String sentence = "";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// two args: first a sentence (quoted) to parse
		// second is a (optional) --debug or --silent flag
		if (args.length < 1) {
			System.out.println("SoarRunner needs a sentence to parse!");
			return;
		}
		
		boolean debug = false;
		boolean silent = false;
		if (args.length >= 2 && args[1].equals("--debug")) {
			debug = true;
		}
		else if (args.length >= 2 && args[1].equals("--silent")) {
			silent = true;
		}
		SoarRunner sr = new SoarRunner(args[0], debug, silent);
	}
	
	public SoarRunner(String theSentence, boolean debug, boolean silent) {
		sentence = theSentence;
		if (debug) {
			kernel = Kernel.CreateKernelInNewThread();
		}
		else {
			kernel = Kernel.CreateKernelInCurrentThread();
		}
		agent = kernel.CreateAgent("LGSoar");
		kernel.AddRhsFunction("readsentence", readSentence(), this);
		kernel.AddRhsFunction("getlgparse", callLGParser(), this);
		kernel.AddRhsFunction("predclose", predClose(), this);
		kernel.AddRhsFunction("predinit", predInit(), this);
		kernel.AddRhsFunction("collect_pred", collectPred(), this);
		kernel.AddRhsFunction("output_preds", outputPreds(), this);

		if (!silent) {
			agent.RegisterForPrintEvent(smlPrintEventId.smlEVENT_PRINT, this, this);
		}
		
		agent.LoadProductions(lgSoarLoaderPath, debug);

		if (debug) {
			try {
				SWTApplication swtApp = new SWTApplication();
				swtApp.startApp(new String[]{"-remote"});
				System.exit(0); // is there a better way to get the Soar thread to stop? 
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			agent.RunSelf(1000);
			if (agent.GetDecisionCycleCounter() > 990) {
				System.out.println("Agent reached dc " + agent.GetDecisionCycleCounter() + ", terminating.");
			}
		}
	}

	
	@Override
	public void printEventHandler(int eventID, Object data, Agent agent,
			String message) {
		// remove first character, which is an extraneous newline
		System.out.println(message.substring(1, message.length()));		
	}
	
	public static Kernel.RhsFunctionInterface readSentence(){
		return new Kernel.RhsFunctionInterface() {
			@Override
			public String rhsFunctionHandler(int eventID, Object data,
					String agentName, String functionName, String argument) {
				return sentence;
			}
		};
	}
	
	
	private static String predList = "";
	private static HashMap<String,String> hm=new HashMap<String,String>();

	public static Kernel.RhsFunctionInterface predClose(){
		return new Kernel.RhsFunctionInterface() {
			@Override
			public String rhsFunctionHandler(int eventID, Object data,
					String agentName, String functionName, String argument) {
				predList = predList + argument;

				return "";
			}
		};
	}
	
	public static Kernel.RhsFunctionInterface predInit(){
		return new Kernel.RhsFunctionInterface() {
			@Override
			public String rhsFunctionHandler(int eventID, Object data,
					String agentName, String functionName, String argument) {

				predList = "";
				hm.clear();
				
				return "";
			}
		};
	}
	
	

	public static Kernel.RhsFunctionInterface collectPred(){
		return new Kernel.RhsFunctionInterface() {
			@Override
			public String rhsFunctionHandler(int eventID, Object data,
					String agentName, String functionName, String argument) {

		        hm.put(argument, "t");
		
				predList = predList + argument;
				
				return "";
			}
		};
	}
	
	public static Kernel.RhsFunctionInterface outputPreds(){
		return new Kernel.RhsFunctionInterface() {
			@Override
			public String rhsFunctionHandler(int eventID, Object data,
					String agentName, String functionName, String argument) {

				Kernel kernel = ((SoarRunner) data).kernel;
				
				String ioWME = argument;
				
				
		        Set s=hm.entrySet();
		        Iterator it=s.iterator();

		        while(it.hasNext())
		        {
		            Map.Entry m =(Map.Entry)it.next();
		            String key= (String) m.getKey();

		            String cmd = "add-wme " + ioWME + " predicate |" + key + "|";
		            kernel.ExecuteCommandLine(cmd, agentName);
		            System.out.println(key);
		        }
						
				return "";
			}
		};
	}
	private static String lgsentWME;
	private static String agName;
	
	public static Kernel.RhsFunctionInterface callLGParser(){
		return new Kernel.RhsFunctionInterface() {
			@Override
			public String rhsFunctionHandler(int eventID, Object data,
					String agentName, String functionName, String argument) {
				
				agName = agentName;
				
				int space1 = argument.indexOf(" ");
				lgsentWME = argument.substring(0, space1);
				String lgIn = argument.substring(space1+1);
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
        String  linkLabel;
        
        String outstr = thisLinkage.linkage_print_diagram();
		System.out.println(outstr);
		
        // Normally you loop through sublinkages
        // int n = thisLinkage.linkage_get_num_sublinkages();
        // For now only choose the first sublinkage
        thisLinkage.linkage_set_current_sublinkage(0);
        int numLinks = thisLinkage.linkage_get_num_links();
        
        // make a wme for the count
        kernel.ExecuteCommandLine("add-wme " + lgsentWME + " count 0", agName);
        	
        // make a wme for the words
        kernel.ExecuteCommandLine("add-wme " + lgsentWME + " words *", agName);
        String wordsWME = getLastWME(lgsentWME, agName, kernel);
        int numWords = sent.sentence_length();
        // now load the words
        for (int wordx = 0; wordx < numWords; wordx++) {
            // add ^word information for this link
            String wordval = sent.sentence_get_word(wordx);
            // there's some kind of bug in the add-wme code...
            if (wordval.equals(".")) {
            	wordval = "|.|";
            }
            kernel.ExecuteCommandLine("add-wme " + wordsWME + " word *", agName);
            String latestWME = getLastWME(wordsWME, agName, kernel);
            kernel.ExecuteCommandLine("add-wme " + latestWME + " wcount " + wordx, agName); 
            kernel.ExecuteCommandLine("add-wme " + latestWME + " lg-wvalue " + wordval, agName); 
        }
        
        // DWL: we had tokens in the old code, but they don't work in the java version; we're not really using them anyway         
    
        // make a wme for the links
        kernel.ExecuteCommandLine("add-wme " + lgsentWME + " links *", agName);
        String linksWME = getLastWME(lgsentWME, agName, kernel);
        // now load the links
        for (int linkIndex = 0; linkIndex < numLinks; linkIndex++) {
            rWordIndex = thisLinkage.linkage_get_link_rword(linkIndex);
            lWordIndex = thisLinkage.linkage_get_link_lword(linkIndex);
            linkLabel = thisLinkage.linkage_get_link_label(linkIndex);
           
            String pattern = "([A-Z]+)([a-z]*)";
            String ltype = linkLabel;
            ltype = ltype.replaceAll(pattern, "$1");
            String lsubtype = linkLabel;
            lsubtype = lsubtype.replaceAll(pattern, "$2");
            
            // add ^link information for this link
            kernel.ExecuteCommandLine("add-wme " + linksWME + " link *", agName);
            String latestWME = getLastWME(linksWME, agName, kernel);
            kernel.ExecuteCommandLine("add-wme " + latestWME + " lvalue " + linkLabel, agName);
            kernel.ExecuteCommandLine("add-wme " + latestWME + " lwleft " + lWordIndex, agName);
            kernel.ExecuteCommandLine("add-wme " + latestWME + " lwright " + rWordIndex, agName);            
            kernel.ExecuteCommandLine("add-wme " + latestWME + " ltype " + ltype, agName);
            if (lsubtype.length() > 0) {
                kernel.ExecuteCommandLine("add-wme " + latestWME + " ltypesub " + lsubtype, agName);	
            }
        }		
	}
	public static String getLastWME(String thiswme, String agentName, Kernel kernel) {
		// do a WME print
		String wmedump = kernel.ExecuteCommandLine("p -d 0 -i " + thiswme, agentName);
		String[] temp = wmedump.split("\\n");
	    Pattern p2 = Pattern.compile("[\\s]+");
	    //sort the elements by timestamp
		TreeMap<Integer,String> wmeTimeStamps = new TreeMap<Integer,String>();
		for(int i = 0; i < temp.length; i++) {
			// parse out the timestamps and wmeID's
			String tempStr = temp[i];
	        String[] tempStrs = p2.split(tempStr);
	        String timeStamp = tempStrs[0].substring(1,tempStrs[0].length() - 1);
	        int timeStampInt = Integer.parseInt(timeStamp); 
	        String wmeId = tempStrs[3].substring(0,tempStrs[3].length() - 1);

	        wmeTimeStamps.put(timeStampInt, wmeId);
		}
		// get the value of the max timestamp (i.e. the latest one, just created)
		String latestWME = wmeTimeStamps.get(wmeTimeStamps.lastKey());
		return latestWME;
	}
}
