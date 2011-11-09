package edu.umich.lcmtester;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

import lcm.lcm.LCM;
import lcm.lcm.LCMDataInputStream;
import lcm.lcm.LCMSubscriber;

import april.lcmtypes.object_data_t;
import april.lcmtypes.observations_t;
import april.lcmtypes.robot_command_t;

public class LCMTester implements LCMSubscriber
{
    // Timer Definitions

    private Timer timer;

    private TimerTask timerTask;

    private boolean running;

    private int timerCount;

    // LCM Definitions

    private LCM lcm;

    private observations_t observations;

    // Swing + Logging Definitions

    private JFrame logFrame;

    private JTextArea logArea;

    private List<String> log;

    // World State

    Map<String, String> robot;
    
    Boolean robotStopped;

    Map<String, String> redCylinder;

    object_data_t redCylinderObj;

    Map<String, String> blueCylinder;

    object_data_t blueCylinderObj;

    Map<String, String> lightSwitch;
    
    Map<String, String> actionPairs;

    public LCMTester()
    {
        // Initialize Timer
        // Start broadcasting
        timerCount = 0;
        timerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                timerCount++;
                LCMTester.this.sendObservations();
            }
        };

        running = false;

       

        // Initialize Logging + Frame
        initLogFrame();

        // Initialize Robot

        robot = new HashMap<String, String>();
        robot.put("id", "1");
        robot.put("name", "robot");
        robot.put("x", "5");
        robot.put("y", "5");
        robot.put("t", "0");
        robot.put("radius", "4");
        robot.put("gripper", "open");
        robotStopped = true;

        redCylinder = new HashMap<String, String>();
        redCylinder.put("id", "10");
        redCylinder.put("x", "20");
        redCylinder.put("y", "10");
        redCylinder.put("color", "red");
        redCylinderObj = new object_data_t();
        redCylinderObj.nj_len = 1;
        redCylinderObj.nounjective = new String[redCylinderObj.nj_len];

        blueCylinder = new Hashtable<String, String>();
        blueCylinder.put("id", "11");
        blueCylinder.put("x", "5");
        blueCylinder.put("y", "15");
        blueCylinder.put("color", "blue");
        blueCylinderObj = new object_data_t();
        blueCylinderObj.nj_len = 1;
        blueCylinderObj.nounjective = new String[blueCylinderObj.nj_len];

        lightSwitch = new HashMap<String, String>();
        lightSwitch.put("name", "switch");
        lightSwitch.put("x", "20");
        lightSwitch.put("y", "20");
        lightSwitch.put("radius", "5");
        lightSwitch.put("state", "off");

        actionPairs = new HashMap<String, String>();
        
        // Initialize LCM
        try
        {
            lcm = new LCM();
            lcm.subscribe("sbolt_commands", this);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.exit(1);
        }

        observations = new observations_t();
        observations.nobs = 2;
        observations.observations = new object_data_t[observations.nobs];
        observations.nsens = 2;
        observations.sensibles = new String[observations.nobs];
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
    }

    public void stop()
    {
        if (!running)
        {
            return;
        }
        running = false;
        timer.cancel();
    }

    private void initLogFrame()
    {
        log = new ArrayList<String>();

        logFrame = new JFrame("LCM Log");
        logArea = new JTextArea();

        logFrame.add(logArea);
        logFrame.setSize(600, 300);
    }

    private void addMessageToLog(String message)
    {
        log.add(message);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < log.size(); ++i)
        {
            sb.append(log.get(i));
            if (i + 1 < log.size())
            {
                sb.append('\n');
            }
        }
        logArea.setText(sb.toString());
    }

    public void showFrame()
    {
        logFrame.setDefaultCloseOperation(logFrame.EXIT_ON_CLOSE);
        logFrame.setVisible(true);
    }

    public void hideFrame()
    {
        logFrame.setVisible(false);
    }

    @Override
    public void messageReceived(LCM lcm, String channel, LCMDataInputStream ins)
    {
        robot_command_t command = null;
        try
        {
            command = new robot_command_t(ins);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return;
        }
        
        double oldX = Double.valueOf(robot.get("x"));
        double oldY = Double.valueOf(robot.get("y"));
        double oldT = Double.valueOf(robot.get("t"));
        double newX = command.dest[0];
        double newY = command.dest[1];
        double newT = command.dest[2];
        if (oldX != newX || oldY != newY || oldT != newT){
            if(newT < -2*Math.PI || newT > 2*Math.PI){
                if(!robotStopped){
                    this.addMessageToLog("Stopped Robot");
                    robotStopped = true;
                }
            } else {
                robot.put("x", String.valueOf(newX));
                robot.put("y", String.valueOf(newY));
                robot.put("t", String.valueOf(newT));
                this.addMessageToLog("Moved Robot to: " + newX + ", " + newY + ", " + newT);
                robotStopped = false;
            }
        }
        
        Boolean gripper_open = (robot.get("gripper") == "open");
        if(gripper_open != command.gripper_open){
            robot.put("gripper", (command.gripper_open ? "open" : "closed"));
            this.addMessageToLog("Gripper is now " + robot.get("gripper"));
        }
        
        
        if(command.action != ""){
            actionPairs.clear();
            String[] actions = command.action.split(",");
            for(String action : actions){
                String[] pair = action.split("=");
                if(pair.length >= 2){
                    actionPairs.put(pair[0], pair[1]);
                }
            }
            if(actionPairs.get("name").equals("switch")){
                String switchState = actionPairs.get("state");
                if(switchState.equals("on") || switchState.equals("off")){
                    String s = lightSwitch.get("state");
                    if(!switchState.equals(lightSwitch.get("state"))){
                        this.addMessageToLog("Turing light switch " + switchState);
                        lightSwitch.put("state", switchState);
                    }
                }                                              
            }
        }
    }

    private void setLcmObservations(observations_t observations)
    {
        this.observations = observations;
    }

    private void sendObservations()
    {
        redCylinderObj.utime = timerCount;
        redCylinderObj.id = Integer.valueOf(redCylinder.get("id"));
        redCylinderObj.pos[0] = Double.valueOf(redCylinder.get("x"));
        redCylinderObj.pos[1] = Double.valueOf(redCylinder.get("y"));
        redCylinderObj.pos[2] = 0;
        redCylinderObj.nounjective[0] = redCylinder.get("color");

        blueCylinderObj.utime = timerCount;
        blueCylinderObj.id = Integer.valueOf(blueCylinder.get("id"));
        blueCylinderObj.pos[0] = Double.valueOf(blueCylinder.get("x"));
        blueCylinderObj.pos[1] = Double.valueOf(blueCylinder.get("y"));
        blueCylinderObj.pos[2] = 0;
        blueCylinderObj.nj_len = 1;
        blueCylinderObj.nounjective[0] = blueCylinder.get("color");

        String robotString = "";
        for (Entry<String, String> keyValPair : robot.entrySet())
        {
            robotString += keyValPair.getKey() + "=" + keyValPair.getValue() + ",";
        }

        String switchString = "";
        for (Entry<String, String> keyValPair : lightSwitch.entrySet())
        {
            switchString += keyValPair.getKey() + "=" + keyValPair.getValue() + ",";
        }

        observations.observations[0] = redCylinderObj;
        observations.observations[1] = blueCylinderObj;
        observations.sensibles[0] = robotString.substring(0,
                robotString.length() - 1);
        observations.sensibles[1] = switchString.substring(0,
                switchString.length() - 1);
        observations.utime = timerCount;

        broadcastLcmObservations();
    }

    private void broadcastLcmObservations()
    {
        if (observations == null)
        {
            return;
        }
        synchronized (observations)
        {
            lcm.publish("abolt_observations", observations);
        }
    }

    public static void main(String[] args)
    {
        LCMTester tester = new LCMTester();
        tester.showFrame();
        tester.start();
    }

}
