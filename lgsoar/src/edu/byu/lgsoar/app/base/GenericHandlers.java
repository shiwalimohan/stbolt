package edu.byu.lgsoar.app.base;

import java.io.FileWriter;
import java.io.PrintStream;

import sml.Agent;
import sml.Agent.PrintEventInterface;
import sml.Kernel;
import sml.Kernel.RhsFunctionInterface;
import edu.byu.lgsoar.graph.GraphUtils;
import edu.byu.lgsoar.utils.Constants;
import edu.byu.lgsoar.utils.FileIO;
import edu.byu.lgsoar.utils.Miscellaneous;

/**
 * 
 * This class contains handler methods that are expected to be usable across several languages;
 * as such, none of them deal with lexical access or morphological processing.
 * @author Nathan Glenn
 *
 */
public class GenericHandlers {

	/**
	 * @return handler that will open a graphing window for syntax, semantics, and the arset.
	 */
	public static Kernel.AgentEventInterface initGrapher(){
		return new Kernel.AgentEventInterface() {
			@Override
			public void agentEventHandler(int eventID, Object data, String agentName) {
				GraphUtils.addGrapher("drs");
			}
		};
	}

	/**
	 * 
	 * @return a handler which returns the process id number
	 */
	public static RhsFunctionInterface getPid() {
		return new Kernel.RhsFunctionInterface() {
			@Override
			public String rhsFunctionHandler(int eventID, Object data,
					String agentName, String functionName, String argument) {
				return Miscellaneous.pid();
			}
		};
	}
	
	/**
	 * 
	 * @return the path and name of the file where WME dumps are printed. This is used in graphing.
	 */
	public static RhsFunctionInterface getTempFile() {
		return new Kernel.RhsFunctionInterface() {
			@Override
			public String rhsFunctionHandler(int eventID, Object data,
					String agentName, String functionName, String argument) {
				String pidval = Miscellaneous.pid();
				return Constants.getProperty("TEMP_DIR") + pidval;
			}
		};
	}
	
	/////////SML PRINT HANDLER METHODS AND VARIABLES//////////
	/**
	 * 
	 * @param p PrintStream to print to. Print streams can be created for printing to files or
	 * otherwise; System.out and System.err are both print streams.
	 * @return a {@link sml.Agent.PrintEventInterface} that simply prints messages to p
	 */
	public static PrintEventInterface smlToStream(final PrintStream p){
		return new PrintEventInterface(){

			@Override
			public void printEventHandler(int eventID, Object data,
					Agent agent, String message) {
				p.println(message);
			}
		};
	}
	
	private static FileWriter fw;
	/**
	 * Only use this with {@link BatchRunner}s. 
	 * @return {@link  sml.Kernel.AgentEventInterface} which creates a new file to print to with a path based on 
	 * the current pid and the number of sentences procesed so far. The file will be called "trace.txt"
	 * and will contain a complete trace of the soar run, provided that you also add smlToFile to the
	 * print handlers via (@link #SoarApplication.addSMLprintHandlers(PrintEventInterface p)}. The following
	 * two lines will print traces:
	 * <code>
	 * 		addAgentEventHandler(GenericHandlers.renewSmlPrintFile(), 
					smlAgentEventId.smlEVENT_BEFORE_AGENT_REINITIALIZED);
			addSMLprintHandlers(GenericHandlers.smlToFile());
	 * </code>
	 * 
	 */
//	public static AgentEventInterface renewSmlPrintFile(){
//		return new AgentEventInterface(){
//
//			@Override
//			public void agentEventHandler(int eventID, Object data,
//					String agentName) {
//				if(fw != null)
//					FileIO.close(fw);
//				BatchRunner br = (BatchRunner) data;
//				//make sure directory exists
//				new File(Constants.getProperty("TEMP_DIR")+ Miscellaneous.pid()
//						+ "DEBUG/" + br.getNumProcessed()).mkdirs();
//				fw = FileIO.fileWriter(Constants.getProperty("TEMP_DIR")+ Miscellaneous.pid()
//				+ "DEBUG/" + br.getNumProcessed() + "/" + "trace.txt");
//			}
//		};
//	}
	
	/**
	 * For use with {@link BatchRunner}s.
	 * @return a PrintEventInterface that simply prints the message to a file (the file being printed
	 * to is specified by {@link #renewSmlPrintFile()).
	 */
	public static PrintEventInterface smlToFile(){
		return new PrintEventInterface(){

			/**
			 * @param message String to print to fw.
			 */
			@Override
			public void printEventHandler(int eventID, Object data,
					Agent agent, String message) {
				if(fw == null)
					return;
				FileIO.write(fw, message);
			}
		};
	}
	
	/**
	 * Closes the currently open SML printout file
	 */
	public static void closeSmlFile(){
		if(fw == null)
			return;
		FileIO.close(fw);
	}

}