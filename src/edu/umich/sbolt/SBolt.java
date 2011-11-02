package edu.umich.sbolt;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
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

import lcm.lcm.LCM;
import lcm.lcm.LCMDataInputStream;
import lcm.lcm.LCMSubscriber;
import sml.Agent;
import sml.Agent.OutputEventInterface;
import sml.Identifier;
import sml.Kernel;
import sml.WMElement;
import sun.security.util.Debug;
import april.lcmtypes.object_data_t;
import april.lcmtypes.observations_t;
import april.lcmtypes.robot_command_t;

public class SBolt implements LCMSubscriber, OutputEventInterface
{
    private LCM lcm;

    private Kernel kernel;

    private Agent agent;

    private robot_command_t command;

    private Timer timer;

    private TimerTask timerTask;

    private boolean running;

    private JFrame chatFrame;

    private JTextArea chatArea;

    private JTextField chatField;

    private List<String> chatMessages;

    // Identifiers for input link

    // Root identifier for all object oberservations
    private Identifier observationsId;

    // Maps observation IDs onto their object-oberservation identifiers
    private Map<Integer, Identifier> observationsMap;

    // Root identifier for all sensible observations
    private Identifier sensiblesId;

    public SBolt(String channel, String agentName)
    {

        // Initialize instance variables
        observationsMap = new HashMap<Integer, Identifier>();
        try
        {
            lcm = new LCM();
            lcm.subscribe("abolt_observations", this);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.exit(1);
        }

        kernel = Kernel.CreateKernelInNewThread();
        agent = kernel.CreateAgent(agentName);
        if (agent == null)
        {
            throw new IllegalStateException("Kernel created null agent");
        }
        Boolean loadResult = agent.LoadProductions("agent/simple_agent.soar");

        agent.AddOutputHandler("command", this, null);
        agent.AddOutputHandler("message", this, null);
        System.out.println(kernel.GetListenerPort());

        // Set up input link.
        initInputLink();

        // Start broadcasting
        timerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                // SBolt.this.broadcastLcmCommand();
            }
        };

        running = false;

        // Set up chat frame
        initChatFrame();
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
        agent.RunSelfForever();
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

    private void initInputLink()
    {
        Identifier il = agent.GetInputLink();
        observationsId = il.CreateIdWME("objects");
        sensiblesId = il.CreateIdWME("sensibles");
    }

    private void initChatFrame()
    {
        chatMessages = new ArrayList<String>();
        chatFrame = new JFrame("SBolt");
        chatArea = new JTextArea();
        JScrollPane pane = new JScrollPane(chatArea);
        chatField = new JTextField();
        JButton button = new JButton("Send Message");
        button.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                sendUserChat(chatField.getText());
                chatField.setText("");
                chatField.requestFocus();
            }
        });

        JSplitPane pane2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                chatField, button);
        JSplitPane pane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pane,
                pane2);

        pane1.setDividerLocation(200);
        pane2.setDividerLocation(450);

        chatFrame.add(pane1);
        chatFrame.setSize(600, 300);
        chatField.getRootPane().setDefaultButton(button);
    }

    public void showFrame()
    {
        chatFrame.setVisible(true);
    }

    public void hideFrame()
    {
        chatFrame.setVisible(false);
    }

    private void sendUserChat(String message)
    {
        addChatMessage(message);
    }

    private void sendSoarChat(String message)
    {
        addChatMessage(message);
    }

    private void addChatMessage(String message)
    {
        chatMessages.add(message);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < chatMessages.size(); ++i)
        {
            sb.append(chatMessages.get(i));
            if (i + 1 < chatMessages.size())
            {
                sb.append('\n');
            }
        }
        chatArea.setText(sb.toString());
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

        updateInputLink(obs);
    }

    private void updateInputLink(observations_t obs)
    {
        // Create a set of all the ids of all observations.
        Set<Integer> obsIds = new HashSet<Integer>();
        for (object_data_t objData : obs.observations)
        {
            obsIds.add(objData.id);
        }

        // Remove observation wmes that aren't currently being observed.
        List<Integer> toRemove = new ArrayList<Integer>();
        for (Integer key : observationsMap.keySet())
        {
            if (!obsIds.contains(key))
            {
                toRemove.add(key);
            }
        }
        for (Integer key : toRemove)
        {
            Identifier identifier = observationsMap.get(key);
            identifier.DestroyWME();
            observationsMap.remove(key);
        }

        // For each observation, either update it if it exists or create it if
        // it doesn't.
        for (object_data_t obj : obs.observations)
        {
            int id = obj.id;
            Identifier obsId = null;
            if (observationsMap.containsKey(id))
            {
                obsId = observationsMap.get(id);
                int numChildren = obsId.GetNumberChildren();
                List<WMElement> wmesToRemove = new ArrayList<WMElement>();
                for (int i = 0; i < numChildren; ++i)
                {
                    WMElement childWme = obsId.GetChild(i);
                    if (childWme.GetAttribute().equals("id"))
                    {
                        continue;
                    }
                    wmesToRemove.add(obsId.GetChild(i));
                }
                for (WMElement wme : wmesToRemove)
                {
                    wme.DestroyWME();
                }
            }
            else
            {
                obsId = observationsId.CreateIdWME("object");
                observationsMap.put(id, obsId);
                obsId.CreateIntWME("id", id);
            }
            for (String nounjective : obj.nounjective)
            {
                obsId.CreateStringWME("nounjective", nounjective);
            }
            Identifier positionId = obsId.CreateIdWME("position");
            positionId.CreateFloatWME("x", obj.pos[0]);
            positionId.CreateFloatWME("y", obj.pos[1]);
            positionId.CreateFloatWME("t", obj.pos[2]);
        }

        // Remove all sensible WMEs and replace them with new ones
        List<WMElement> sensiblesToRemove = new ArrayList<WMElement>();
        for (int i = 0; i < observationsId.GetNumberChildren(); ++i)
        {
            WMElement child = sensiblesId.GetChild(i);
            if (child.GetAttribute().equals("sensible"))
            {
                sensiblesToRemove.add(child);
            }
        }
        for (WMElement child : sensiblesToRemove)
        {
            child.DestroyWME();
        }

        for (String sensibleStr : obs.sensibles)
        {
            Identifier sensibleId = sensiblesId.CreateIdWME("sensible");
            String[] tokens = sensibleStr.split(",");
            for (String token : tokens)
            {
                String[] pair = token.split("=");
                if (pair.length != 2)
                {
                    throw new IllegalStateException(
                            "Choked on parsing sensible message.");
                }
                String key = pair[0];
                String value = pair[1];
                Identifier attribute = sensibleId.CreateIdWME("attribute");
                attribute.CreateStringWME("key", key);
                attribute.CreateStringWME("value", value);
            }
        }
    }

    @Override
    public void outputEventHandler(Object data, String agentName,
            String attributeName, WMElement wme)
    {
        if (!(wme.IsJustAdded() && wme.IsIdentifier()))
        {
            return;
        }

        if (wme.GetAttribute().equals("command"))
        {
            processOutputLinkCommand(wme.ConvertToIdentifier());
        }
        else if (wme.GetAttribute().equals("message"))
        {
            processOutputLinkMessage(wme.ConvertToIdentifier());
        }
    }

    private void processOutputLinkMessage(Identifier messageId)
    {
        if (messageId.GetNumberChildren() == 0)
        {
            // System.out.println("No children of message");
            return;
        }

        String message = "";
        WMElement wordsWME = messageId.FindByAttribute("words", 0);
        if (wordsWME == null || !wordsWME.IsIdentifier())
        {
            return;
        }
        Identifier currentWordId = wordsWME.ConvertToIdentifier();

        // Follows the linked list down until it can't find the 'rest' attribute
        // of a WME
        while (currentWordId != null)
        {
            Identifier nextWordId = null;
            for (int i = 0; i < currentWordId.GetNumberChildren(); i++)
            {
                WMElement child = currentWordId.GetChild(i);
                if (child.GetAttribute().equals("first-word"))
                {
                    String currentWord = child.GetValueAsString();
                    if (message == "")
                    {
                        message = currentWord;
                    }
                    else
                    {
                        message += " " + currentWord;
                    }
                }
                else if (child.GetAttribute().equals("rest")
                        && child.IsIdentifier())
                {
                    nextWordId = child.ConvertToIdentifier();
                }
            }
            currentWordId = nextWordId;
        }

        if (message != "")
        {
            message += ".";
            addChatMessage(message);
        }

    }

    private void processOutputLinkCommand(Identifier commandId)
    {
        if(commandId.GetNumberChildren() == 0){
            return;
        }
        
        StringBuffer actionBuf = new StringBuffer();

        double x = 0.0;
        double y = 0.0;
        double t = 10.0;

        boolean gripperOpen = true;

        int numChildren = commandId.GetNumberChildren();
        for (int i = 0; i < numChildren; ++i)
        {
            WMElement child = commandId.GetChild(i);

            if (child.GetAttribute().equals("action"))
            {
                Identifier actionId = child.ConvertToIdentifier();
                WMElement keyWme = actionId.FindByAttribute("key", 0);
                if (keyWme == null)
                {
                    throw new IllegalStateException(
                            "Command has action with no key");
                }
                String key = keyWme.GetValueAsString();
                WMElement valueWme = actionId.FindByAttribute("value", 0);
                if (valueWme == null)
                {
                    throw new IllegalStateException(
                            "Command has action with no value");
                }
                String value = valueWme.GetValueAsString();
                actionBuf.append(key + "=" + value + ",");
            }
            else if (child.GetAttribute().equals("destination"))
            {
                Identifier destinationId = child.ConvertToIdentifier();
                WMElement xWme = destinationId.FindByAttribute("x", 0);
                WMElement yWme = destinationId.FindByAttribute("y", 0);
                WMElement tWme = destinationId.FindByAttribute("t", 0);
                if (xWme == null || yWme == null || tWme == null)
                {
                    throw new IllegalStateException(
                            "Command has destination WME missing x, y, or t");
                }
                x = xWme.ConvertToFloatElement().GetValue();
                y = yWme.ConvertToFloatElement().GetValue();
                t = tWme.ConvertToFloatElement().GetValue();
            }
            else if (child.GetAttribute().equals("gripper"))
            {
                String value = child.GetValueAsString();
                gripperOpen = !value.equals("closed");
            }
        }

        robot_command_t command = new robot_command_t();
        command.action = actionBuf.toString();
        if (command.action.length() > 0)
        {
            // Remove trailing comma
            command.action = command.action.substring(0,
                    command.action.length() - 1);
        }
        command.dest = new double[] { x, y, t };
        command.gripper_open = gripperOpen;
        setLcmCommand(command);
    }

    private void setLcmCommand(robot_command_t command)
    {
        this.command = command;
    }

    private void broadcastLcmCommand()
    {
        synchronized (command)
        {
            if (command == null)
                return;
            lcm.publish("sbolt-commands", command);
        }
    }

    public static void main(String[] args)
    {
        SBolt sbolt = new SBolt("abolt-perceptions", "sbolt");
        sbolt.showFrame();
        sbolt.start();
    }

}
