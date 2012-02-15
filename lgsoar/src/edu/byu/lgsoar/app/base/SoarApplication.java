package edu.byu.lgsoar.app.base;

import sml.Agent;
import sml.Agent.PrintEventInterface;
import sml.Kernel;
import sml.Kernel.RhsFunctionInterface;
import sml.smlAgentEventId;
import sml.smlPrintEventId;
import sml.smlSystemEventId;
import edu.byu.lgsoar.graph.GraphUtils;
import edu.byu.lgsoar.utils.Constants;
import edu.byu.lgsoar.utils.Debugger;
/**
 * Contains the basic functions for accessing essential parts of a Soar Application. This class
 * is especially useful for the various SML handler interfaces. This is because all of the 
 * handlers take any object as one of their parameters. If the handler will need access to the
 * kernel or agent being used, one can simply pass the calling object (assuming it extends
 * SoarApplication). For example:
 * <code>agent.RegisterForPrintEvent(smlPrintEventId.smlEVENT_FIRST_PRINT_EVENT, p, this);</code>
 * Then, in the handler, we cast the data argument to a SoarApplication and access the kernel:
 * <code>
 * 	new Kernel.RhsFunctionInterface() {
		\@Override
		public String rhsFunctionHandler(int eventID, Object data,
			String agentName, String functionName, String argument) {
			Kernel kernel = ((SoarApplication) data).getKernel();
		}
	}
	</code>
 * 
 * @author Nathan Glenn
 *
 */
public class SoarApplication {
	private Agent agent = null;
	private Kernel kernel = null;
	private String smDB = null;
	private String agentName = "LGSoar Application";
	private boolean graphing = Boolean.parseBoolean(Constants.getProperty("GRAPHING"));
	private String currentSentence;
	
	private long graphCallback;
	private long initGrapherCallback;
	private long[] printCallbacks = new long[4];
	/**
	 * Creates kernel and agent, if currently null, initializes Jaws and adds 
	 * graphing handlers, if graphing is set to true. Also creates language
	 * WMEs, if {@link language} is not null.
	 */
	public void init(){
		if(getKernel() == null)
			makeKernel();
		if(getAgent() == null)
			makeAgent();
		if(graphing){
			if(getAgent() != null){
				setGraphing(true);
			}
		}
//		makeLanguageWme();
	}
		
	/**
	 * Executes init-soar in the agent, and sets the language ID, if there is any.
	 */
	public void initSoar(){
		if(agent == null)
			return;
		agent.InitSoar();
//		makeLanguageWme();
	}
	
	/**
	 * Destroys the kernel and the agent.
	 */
	public void shutdown(){
		Agent agent = getAgent();
		Kernel kernel = getKernel();
		if(agent != null)
			kernel.DestroyAgent(agent);
		agent = null;
		if(kernel != null){
			kernel.Shutdown();
			kernel.delete();
		}
		kernel = null;
		//just in case we are doing debug graphing;
		//if this doesn't get closed then the last report will be lost!
		GenericHandlers.closeSmlFile();
	}
	
	/**
	 * 
	 * @param funcName name to be used for function in Soar code (with the <code>exec</code> function)
	 * @param r rhs function to be executed
	 * @return Unique ID for this callback. Required when unregistering this callback.
	 */
	public long addRHShandler(String funcName, RhsFunctionInterface r){
		if(kernel == null)
			throw new IllegalStateException("Kernel needs to be created before attempting to add any event handlers");
		return kernel.AddRhsFunction(funcName, r, this);
	}
	
	/**
	 * 
	 * @param p Handler to register.
	 * All handlers are registered to smlEVENT_PRINT, smlEVENT_ECHO, smlEVENT_FIRST_PRINT_EVENT, 
	 * and smlEVENT_FIRST_PRINT_EVENT.
	 */
	public void addSMLprintHandlers(PrintEventInterface p){
		Agent agent = getAgent();
		if(agent == null)
			throw new IllegalStateException("Agent needs to be created before attempting to add any event handlers");
		printCallbacks[0] = agent.RegisterForPrintEvent(smlPrintEventId.smlEVENT_PRINT, p, this);
		printCallbacks[1] = agent.RegisterForPrintEvent(smlPrintEventId.smlEVENT_ECHO, p, this);
		printCallbacks[2] = agent.RegisterForPrintEvent(smlPrintEventId.smlEVENT_FIRST_PRINT_EVENT, p, this);
		printCallbacks[3] = agent.RegisterForPrintEvent(smlPrintEventId.smlEVENT_FIRST_PRINT_EVENT, p, this);
	}
	
	/**
	 * Removes all currently registered sml print handlers
	 */
	public void removeSMLprintHandlers(){
		Agent agent = getAgent();
		if(agent == null)
			throw new IllegalStateException("Agent needs to be created before attempting to add any event handlers");
		for(long l : printCallbacks)
			agent.RemoveOutputHandler(l);
		if(agent.HadError())
			System.err.println(agent.GetLastErrorDescription());
	}
	
	public long addAgentEventHandler(Kernel.AgentEventInterface h,smlAgentEventId id){
		return kernel.RegisterForAgentEvent(id, h, this);
	}
	public long addSystemEventHandler(Kernel.SystemEventInterface h, smlSystemEventId id){
		return kernel.RegisterForSystemEvent(id, h, this);
	}

	/**
	 * Spawns a debugger for this application. This method will block until the debugger is closed.
	 */
	public void debugger() {
		try{
			Debugger.remoteDebug();
		}catch(Exception e){e.printStackTrace();}
	}
	
	/**
	 * Sources a list of .soar files.
	 * @param paths of *.soar files to source
	 */
	public void source(String[] paths){
		for (String file : paths){
			agent.LoadProductions(file);
			if(agent.HadError()){
				System.out.println(agent.GetLastErrorDescription());
			}
		}
	}
	
	/**
	 * Sources a .soar file.
	 * @param path of .soar file to source
	 */
	public void source(String path){
		agent.LoadProductions(path);
		if(agent.HadError()){
			System.out.println(agent.GetLastErrorDescription());
		}
	}
	
	/**
	 * 
	 * Sentences often need to end in a " ." to be parsed correctly. This method makes sure that a string
	 * meets that requirement.
	 * @param sentence String that needs to be checked for correct period placement
	 * @return input sentence with fixed period
	 */
	public String fixPeriod(String sentence){
		if(!sentence.endsWith(" ."))
			if(!sentence.endsWith("."))
				sentence = sentence.concat(" .");
			else{
				sentence = sentence.substring(0, sentence.length()-1);
				sentence = sentence.concat(" .");
			}
		return sentence;
	}
	
	////GETTERS/SETTERS////
	
	/**
	 * 
	 * @param a the agent to use
	 */
	public void setAgent(Agent a){
		agent = a;
	}
	/**
	 * 
	 * @return the currently used agent
	 */
	public Agent getAgent(){
		return agent;
	}
	/**
	 * 
	 * @param k Kernel to use
	 */
	public void setKernel(Kernel k){
		kernel = k;
	}
	/**
	 * 
	 * @return the currently used kernel object
	 */
	public Kernel getKernel(){
		return kernel;
	}
	
	/**
	 * Sets the kernel's semantic memory database
	 * @param path path of the semantic memory database to use
	 */
//	public void setSemDatabase(String path){
//		smDB = path;
//		kernel.ExecuteCommandLine("smem --set path " + smDB, agent.GetAgentName());
//		if(smDB != null)
//			kernel.ExecuteCommandLine("smem --set learning on", agent.GetAgentName());
//	}
	/**
	 * 
	 * @return path of the semantic memory database currently being used
	 */
//	public String getSemDatabase(){
//		return smDB;
//	}

	public void setGraphing(boolean g) {
		graphing = g;
		if(graphing){
			GraphUtils.addGrapher("syntax");
			GraphUtils.addGrapher("semantics");
			// SBW added
			GraphUtils.addGrapher("arset");
			GraphUtils.addGrapher("drs");
			initGrapherCallback = addAgentEventHandler(GenericHandlers.initGrapher(), smlAgentEventId.smlEVENT_BEFORE_AGENT_REINITIALIZED);
			if(getAgent() != null){
					graphCallback = addRHShandler("perlmkgviz", GraphUtils.perlGViz());
			}
		}
		else{
			try{
				kernel.RemoveRhsFunction(initGrapherCallback);
				kernel.RemoveRhsFunction(graphCallback);
			}catch(Exception e){}//ignore errors. The callbacks might not exist if graphing was never on.
		}
	}
	public boolean isGraphing() {
		return graphing;
	}
	
	/**
	 * @return the name of the agent.
	 */
	public String getAgentName() {
		return agentName;
	}
	/**
	 * 
	 * @param name Name to be used when creating agent. Note that it is impossible to set the
	 * agent's name after it has been created, so this must be called before init() is called.
	 */
	public void setAgentName(String name) {
		agentName = name;
	}
	
	/**
	 * 
	 * @param s sentence to be processed
	 */
	public void setCurrentSentence(String s){
		currentSentence = s;
	}
	/**
	 * 
	 * @return sentence being processed
	 */
	public String getCurrentSentence(){
		return currentSentence;
	}
	
////////PRIVATE METHODS////////

	private void makeKernel(){
		try
	    {
			setKernel(Kernel.CreateKernelInNewThread());
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
		if (getKernel().HadError())
		{
			System.err.println("Error creating kernel: " + getKernel().GetLastErrorDescription());
		}
	}
	private void makeAgent() {
		setAgent(getKernel().CreateAgent(agentName));
		if(getAgent().HadError()){
			System.err.println("Error creating agent: " + getAgent().GetLastErrorDescription());
		}
		//because we ALWAYS use it
		agent.ExecuteCommandLine("alias matches ms");
	}
//	private void makeLanguageWme(){
////		if(language != null){
////			System.err.println(agent.ExecuteCommandLine("add-wme S1 ^language *"));
////			if(agent.HadError())
////				System.err.println(agent.GetLastErrorDescription());
////			String latestWME = Miscellaneous.getLastWME("S1", agentName, kernel);
////			System.err.println(latestWME);
////			System.err.println(agent.ExecuteCommandLine("add-wme " + latestWME + "^name " + language));
////		}
//	}
}
