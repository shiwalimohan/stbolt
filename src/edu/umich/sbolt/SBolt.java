package edu.umich.sbolt;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lcm.lcm.LCM;
import lcm.lcm.LCMDataInputStream;
import lcm.lcm.LCMSubscriber;
import sml.Agent;
import sml.Kernel;
import abolt.lcmtypes.observations_t;
import abolt.lcmtypes.robot_command_t;
import edu.umich.sbolt.controller.GamepadController;
import edu.umich.sbolt.world.World;
import edu.umich.soar.SoarProperties;

public class SBolt implements LCMSubscriber
        
{
    private LCM lcm;
    private Kernel kernel;
    private Agent agent;
    private Timer timer;
    private TimerTask timerTask;
    private boolean running;

    private InputLinkHandler inputLinkHandler;
    
    private OutputLinkHandler outputLinkHandler;
    
    private ChatFrame chatFrame;
    
    private World world;
    
    private GamepadController gamepadController;
    
    public SBolt(String channel, String agentName)
    {
        // LCM Channel, listen for observations_t
        try
        {
            lcm = new LCM();
            lcm.subscribe(channel, this);
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
        
        File f = new File("agent/obj-analyzer/obj-analyzer.soar");
        if(f.exists()) { 
            agent.LoadProductions("agent/obj-analyzer/obj-analyzer.soar");
        } else {
            agent.LoadProductions("agent/simple-responder/responder.soar");
        }
        

        // !!! Important !!!
        // We set AutoCommit to false, and only commit inside of the event
        // handler
        // for the RunEvent right before the next Input Phase
        // Otherwise the system would apparently hang on a commit
        kernel.SetAutoCommit(false);

        agent.SpawnDebugger(kernel.GetListenerPort(), new SoarProperties().getPrefix());
        
        gamepadController = new GamepadController(0.0, 0.0, 0.0);
        
        world = new World();
        world.addRobotPositionListener(gamepadController);
        
        // Setup InputLink
        inputLinkHandler = new InputLinkHandler(world, this);
        
        // Setup OutputLink
        outputLinkHandler = new OutputLinkHandler(this);
        outputLinkHandler.addRobotDestinationListener(gamepadController);
        
        // Setup ChatFrame
        chatFrame = new ChatFrame(this);
        
        // Start broadcasting
        timerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                SBolt.this.broadcastLcmCommand();
            }
        };

        running = false;
        chatFrame.showFrame();
    }
    
    public Agent getAgent(){
        return agent;
    }
    
    public ChatFrame getChatFrame(){
        return chatFrame;
    }
    
    public InputLinkHandler getInputLink(){
        return inputLinkHandler;
    }
    
    public OutputLinkHandler getOutputLink(){
        return outputLinkHandler;
    }
    
    public World getWorld(){
        return world;
    }

    public void start()
    {
        if (running)
        {
            return;
        }
        running = true;
        timer = new Timer();
        timer.schedule(timerTask, 1000, 500);
        gamepadController.start();
        agent.RunSelf(1);
    }

    public void stop()
    {
        if (!running)
        {
            return;
        }
        running = false;
        agent.StopSelf();
        timer.cancel();
    }

    @Override
    public void messageReceived(LCM lcm, String channel, LCMDataInputStream ins)
    {
        if (inputLinkHandler == null) return;
        observations_t obs = null;
        try
        {
            obs = new observations_t(ins);
            world.newObservation(obs);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return;
        }
    }

 
    /**
     * Sends out the robot_command_t command via LCM
     */
    private void broadcastLcmCommand()
    {
        robot_command_t command = outputLinkHandler.getCommand();
        if (command == null)
        {
            return;
        }
        synchronized (command)
        {
            lcm.publish("ROBOT_COMMAND", command);
        }
    }

    public static void main(String[] args)
    {
        
        SBolt sbolt = new SBolt("OBSERVATIONS", "sbolt");
        sbolt.start();
    }

}
