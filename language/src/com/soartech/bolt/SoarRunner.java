package com.soartech.bolt;

import java.io.IOException;
import java.util.ArrayList;

import net.sf.jlinkgrammar.Linkage;
import net.sf.jlinkgrammar.Sentence;

import sml.Agent;
import sml.Identifier;
import sml.smlPrintEventId;
import sml.Agent.PrintEventInterface;
import sml.Kernel;

import edu.umich.soar.debugger.SWTApplication;


public class SoarRunner implements PrintEventInterface {

	
	private static Agent agent = null;
	private static Kernel kernel = null;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// at least one arg: first a (optional) --debug or --silent flag
		// then a sentence
		if (args.length < 2) {
			System.out.println("SoarRunner needs a sentence to parse!");
			return;
		}
		
		String soarFile = args[0];
		
		boolean debug = false;
		boolean silent = false;
		boolean runSoar = true;
		String whitelist = null;
		int sentenceStart = args.length;

		int argNum = 1;
		while (argNum < args.length) {
			if (args[argNum].equals("--debug")) {
				debug = true;
			}
			else if (args[argNum].equals("--silent")) {
				silent = true;
			}
			else if (args[argNum].equals("--parse-only")) {
				runSoar = false;
			}
			else if (args[argNum].equals("--whitelist")) {
				whitelist = args[argNum+1];
				argNum++;
			}
			else {
				sentenceStart = argNum;
				argNum = args.length;
			}
			argNum++;
		}

		ArrayList<String> sentences = new ArrayList<String>();
		for (int i=sentenceStart; i<args.length; i++) {
			sentences.add(args[i]);
		}
		SoarRunner sr = new SoarRunner(soarFile, sentences, debug, silent, runSoar, whitelist);
	}
	
	public SoarRunner(String soarFile, ArrayList<String> sentences, boolean debug, boolean silent, boolean runSoar, String whitelist) {
		if (runSoar) {
			if (debug) {
				kernel = Kernel.CreateKernelInNewThread();
			}
			else {
				kernel = Kernel.CreateKernelInCurrentThread();
			}
			agent = kernel.CreateAgent("LGSoar");
	
			if (silent) {
				agent.ExecuteCommandLine("watch 0");
			}
			
			agent.RegisterForPrintEvent(smlPrintEventId.smlEVENT_PRINT, this, this);
			agent.LoadProductions(soarFile);
		}
	
		LGSupport lgSupport = new LGSupport(agent, "data/link", whitelist);
		for (String sentence: sentences) {
			lgSupport.handleSentence(sentence);
		}
		
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
		else if (runSoar) {
			agent.RunSelf(1000);
			if (agent.GetDecisionCycleCounter() > 990) {
				System.out.println("Agent reached dc " + agent.GetDecisionCycleCounter() + ", terminating.");
			}
		}
	}

	
	@Override
	public void printEventHandler(int eventID, Object data, Agent agent,
			String message) {
		// remove extraneous newline
		message = message.substring(1, message.length());
	
		String[] lines = message.split("\n");
		for (int i=0; i<lines.length; i++) {
			// filter out "the agent halted." messages
			if (!lines[i].endsWith("halted.")) {
				System.out.println(lines[i]);
			}
		}
	}
	
	
}
