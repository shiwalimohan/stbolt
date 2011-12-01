package edu.umich.sbolt;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import edu.umich.soar.SoarProperties;

import lcm.lcm.LCM;
import lcm.lcm.LCMDataInputStream;
import lcm.lcm.LCMSubscriber;
import sml.Agent;
import sml.Agent.OutputEventInterface;
import sml.Agent.RunEventInterface;
import sml.FloatElement;
import sml.Identifier;
import sml.Kernel;
import sml.smlRunEventId;
import sml.WMElement;
import sun.security.util.Debug;

import abolt.lcmtypes.object_data_t;
import abolt.lcmtypes.observations_t;
import abolt.lcmtypes.robot_command_t;

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
        agent.LoadProductions("agent/simple-responder/responder.soar");

        // !!! Important !!!
        // We set AutoCommit to false, and only commit inside of the event
        // handler
        // for the RunEvent right before the next Input Phase
        // Otherwise the system would apparently hang on a commit
        kernel.SetAutoCommit(false);

        agent.SpawnDebugger(kernel.GetListenerPort(),
                System.getenv().get("SOAR_HOME"));
        
        
        // Setup InputLink
        inputLinkHandler = new InputLinkHandler(this);
        
        // Setup OutputLink
        outputLinkHandler = new OutputLinkHandler(this);
        
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

    public void start()
    {
        if (running)
        {
            return;
        }
        running = true;
        timer = new Timer();
        timer.schedule(timerTask, 1000, 500);
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
        observations_t obs = null;
        try
        {
            obs = new observations_t(ins);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return;
        }
        inputLinkHandler.updateObservation(obs);
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
