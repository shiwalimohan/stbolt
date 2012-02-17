package com.soartech.bolt;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import sml.Agent;
import sml.smlPrintEventId;
import sml.Agent.PrintEventInterface;
import sml.Kernel.RhsFunctionInterface;
import sml.Kernel;

import edu.umich.soar.debugger.SWTApplication;


public class SoarRunner implements PrintEventInterface {
	public static String dictionaryPath = "/opt/bolt/stbolt/lgsoar/data/link";
	private static String lgSoarLoaderPath = "/opt/bolt/stbolt/lgsoar/soarfiles/lg93init.soar";
	
	private Agent agent = null;
	private Kernel kernel = null;
	
	private static String sentence = "";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// two args: first a sentence (quoted) to parse
		// second is a (optional) --debug flag
		if (args.length < 1) {
			System.out.println("SoarRunner needs a sentence to parse!");
			return;
		}
		
		boolean debug = false;
		if (args.length >= 2 && args[1].equals("--debug")) {
			debug = true;
		}
		
		SoarRunner sr = new SoarRunner(args[0], debug);
	}
	
	public SoarRunner(String theSentence, boolean debug) {
		sentence = theSentence;
		kernel = Kernel.CreateKernelInCurrentThread();
		agent = kernel.CreateAgent("LGSoar");
		kernel.AddRhsFunction("readsentence", readSentence(), this);
		kernel.AddRhsFunction("getlgparse", callLGParser(), this);
		kernel.AddRhsFunction("predclose", predClose(), this);
		kernel.AddRhsFunction("predinit", predInit(), this);
		kernel.AddRhsFunction("collect_pred", collectPred(), this);
		kernel.AddRhsFunction("output_preds", outputPreds(), this);

		agent.RegisterForPrintEvent(smlPrintEventId.smlEVENT_PRINT, this, this);
		agent.RegisterForPrintEvent(smlPrintEventId.smlEVENT_ECHO, this, this);
		agent.RegisterForPrintEvent(smlPrintEventId.smlEVENT_FIRST_PRINT_EVENT, this, this);
		agent.RegisterForPrintEvent(smlPrintEventId.smlEVENT_LAST_PRINT_EVENT, this, this);

		//agent.ExecuteCommandLine("source " + lgSoarLoaderPath);
		
		try {
			SWTApplication swtApp = new SWTApplication();
			swtApp.startApp(new String[]{"-remote"});
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		/*agent.RunSelf(1000);
		if (agent.GetDecisionCycleCounter() > 990) {
			System.out.println("Agent reached dc " + agent.GetDecisionCycleCounter() + ", terminating.");
		}*/
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
	
	
	public static Kernel.RhsFunctionInterface callLGParser(){
		return new Kernel.RhsFunctionInterface() {
			@Override
			public String rhsFunctionHandler(int eventID, Object data,
					String agentName, String functionName, String argument) {
				
				Kernel kern = ((SoarRunner) data).kernel;
				//agName = agentName;
				
				int space1 = argument.indexOf(" ");
				String lgsentWME = argument.substring(0, space1);
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

}
