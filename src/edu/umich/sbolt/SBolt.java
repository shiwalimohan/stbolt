package edu.umich.sbolt;

import java.io.*;
import java.util.List;
import java.util.Properties;
import lcm.lcm.*;
import abolt.lcmtypes.*;
import sml.Agent;
import sml.Agent.PrintEventInterface;
import sml.Agent.RunEventInterface;
import sml.Kernel;
import sml.smlPrintEventId;
import sml.smlRunEventId;
import april.util.TimeUtil;
import edu.umich.sbolt.world.World;
import com.soartech.bolt.BOLTLGSupport;

public class SBolt implements LCMSubscriber, PrintEventInterface, RunEventInterface
{
	public static SBolt Singleton(){
		return sboltInstance;
	}
	private static SBolt sboltInstance = null;
	
	private InputLinkHandler inputLink;
	
	private OutputLinkHandler outputLink;

    private ChatFrame chatFrame;

    private World world;
	
    private LCM lcm;

    private Kernel kernel;

    private Agent agent;
   
    private PrintWriter logWriter;
    
    private int throttleMS = 0;
    
    private boolean running = false;
    
    private String agentSource = null;
    
    private String lgSoarSource = null;
    
    private String smemSource = null;
    

    public SBolt(String agentName, boolean headless)
    {
    	sboltInstance = this;
        // LCM Channel, listen for observations_t
        try
        {
            lcm = new LCM();
            lcm.subscribe("OBSERVATIONS", this);
            lcm.subscribe("ROBOT_ACTION", this);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.exit(1);
        }

        // Initialize Soar Agent
        kernel = Kernel.CreateKernelInNewThread();
        agent = kernel.CreateAgent(agentName);
        if (agent == null)
        {
           throw new IllegalStateException("Kernel created null agent");
        }

        // Load the properties file
        Properties props = new Properties();
        try {
			props.load(new FileReader("sbolt.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        // Source the agent code
        agentSource = props.getProperty("agent");
        
        if (agentSource != null) {
        	agent.LoadProductions(agentSource);
        }
        
        smemSource = props.getProperty("smem-source");
        if(smemSource != null){
        	agent.LoadProductions(smemSource);
        }
        
        if (!headless) {
        	System.out.println("Spawn Debugger: " + agent.SpawnDebugger(kernel.GetListenerPort()));
        	// Requires the SOAR_HOME environment variable
        }
        
        String useLGProp = props.getProperty("enable-lgsoar");
        
        boolean useLG = false;
        String lgSoarDictionary = "";
        if (useLGProp != null && useLGProp.equals("true")) {
        	lgSoarSource = props.getProperty("language-productions");
        	lgSoarDictionary = props.getProperty("lgsoar-dictionary");
        	
        	if (lgSoarSource != null && lgSoarDictionary != null) {
        		useLG = true;
        		agent.LoadProductions(lgSoarSource);
        	}
        	else {
        		System.out.println("ERROR: LGSoar misconfigured, not enabled.");
        	}
        }
        
        BOLTLGSupport lgSupport = null;
        
        if (useLG) {
        	lgSupport = new BOLTLGSupport(agent, lgSoarDictionary);
        }
        
        // !!! Important !!!
        // We set AutoCommit to false, and only commit inside of the event
        // handler
        // for the RunEvent right before the next Input Phase
        // Otherwise the system would apparently hang on a commit
        kernel.SetAutoCommit(false);

		String doLog = props.getProperty("enable-log");
		if (doLog != null && doLog.equals("true")) {
			try {
				logWriter = new PrintWriter(new FileWriter("sbolt-log.txt"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			agent.RegisterForPrintEvent(smlPrintEventId.smlEVENT_PRINT, this, this);
		}
		
		String watchLevel = props.getProperty("watch-level");
		if (watchLevel != null) {
			agent.ExecuteCommandLine("watch " + watchLevel);
		}

		String throttleMSString = props.getProperty("decision-throttle-ms");
		if (throttleMSString != null) {
			throttleMS = Integer.parseInt(throttleMSString);
			agent.RegisterForRunEvent(smlRunEventId.smlEVENT_AFTER_DECISION_CYCLE, this, this);
		}
      
        world = new World();

        // Setup InputLink
        inputLink = new InputLinkHandler(agent, lgSupport);


        // Setup OutputLink
        outputLink = new OutputLinkHandler(agent);

        // Setup ChatFrame
        chatFrame = new ChatFrame(lgSupport, agent);

        chatFrame.showFrame();
        
    }
    
    public Kernel getKernel(){
    	return kernel;
    }

    public Agent getAgent()
    {
        return agent;
    }
    
    public void reloadAgent(boolean loadSmem){
    	System.out.println("Re-initializing the agent");
    	System.out.println("  smem --init:  " + agent.ExecuteCommandLine("smem --init"));
    	System.out.println("  epmem --init: " + agent.ExecuteCommandLine("epmem --init"));
    	if(loadSmem && smemSource != null){
        	agent.ExecuteCommandLine("smem --set database memory");
        	agent.ExecuteCommandLine("epmem --set database memory");
    		agent.LoadProductions(smemSource);
    		System.out.println("  source " + smemSource);
    	}
    	if(agentSource != null){
    		agent.LoadProductions(agentSource);
    		System.out.println("  source " + agentSource);
    	}
    	if(lgSoarSource != null){
    		agent.LoadProductions(lgSoarSource);
    		System.out.println("  source " + lgSoarSource);
    	}
    	System.out.println("Agent re-initialized");
    }

    @Override
    public void messageReceived(LCM lcm, String channel, LCMDataInputStream ins)
    {
    	if(channel.equals("ROBOT_ACTION") && world != null && world.getRobotArm() != null){
    		try {
    			robot_action_t action = new robot_action_t(ins);
				world.getRobotArm().newRobotAction(action);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	} else if(channel.equals("OBSERVATIONS") && inputLink != null){
    		synchronized(inputLink){
                observations_t obs = null;
                try {
                    obs = new observations_t(ins);
                    world.newObservation(obs);
                }
                catch (IOException e){
                    e.printStackTrace();
                    return;
                }
    		}
    	}
    }

    /**
     * Sends out training_data_t over LCM
     */
    public static void broadcastTrainingData(List<training_label_t> newLabels)
    {
    	if(newLabels != null){
        	training_data_t trainingData = new training_data_t();
        	trainingData.utime = TimeUtil.utime();
        	trainingData.num_labels = newLabels.size();
        	trainingData.labels = new training_label_t[newLabels.size()];
        	for(int i = 0; i < newLabels.size(); i++){
        		trainingData.labels[i] = newLabels.get(i);
        	}
        	LCM.getSingleton().publish("TRAINING_DATA", trainingData);
    	}
    }
    
    public static void broadcastRobotCommand(robot_command_t command){
        LCM.getSingleton().publish("ROBOT_COMMAND", command);
    }

    public static void main(String[] args)
    {    	
    	boolean headless = false;
    	if (args.length > 0 && args[0].equals("--headless")) {
    		// it might make sense to instead always make the parameter
    		// be the properties filename, and load all others there
    		// (currently, properties filename is hardcoded)
    		headless = true;
    	}
    	sboltInstance = new SBolt("sbolt", headless);
//        if (headless) {
//        	sboltInstance.agent.RunSelfForever();
//        }
//        sboltInstance.agent.RunSelfForever();
//        System.out.println("IM DONE");
    }
    
	@Override
	public void printEventHandler(int eventID, Object data, Agent agent, String message) {
		synchronized(logWriter) {
			logWriter.print(message);
		}
	}
	@Override
	public void runEventHandler(int arg0, Object arg1, Agent arg2, int arg3) {
		try {
			Thread.sleep(throttleMS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
