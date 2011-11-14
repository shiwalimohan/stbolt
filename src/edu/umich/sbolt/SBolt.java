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
import april.lcmtypes.object_data_t;
import april.lcmtypes.observations_t;
import april.lcmtypes.robot_command_t;

public class SBolt implements LCMSubscriber, OutputEventInterface,
        RunEventInterface
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

    // The most recent observations_t received
    private observations_t currentObservation;

    private observations_t lastObservation;

    // Maps observation IDs onto their sensibles identifiers
    private Map<Integer, Identifier> observationsMap;

    // Maps sensible IDs onto their sensibles identifier
    private Map<Integer, Identifier> sensiblesMap;

    // Root identifier for all sensible observations
    private Identifier sensiblesId;

    // Root identifier for all messages the robot receives
    private Identifier messagesId;

    // A counter that is used for the id for each message
    private int messageIdNum;

    // A queue of the messages received since the last input phase
    private List<String> chatMessageQueue;

    private static Integer INVALID_ID = -1;

    private static String INTEGER_VAL = "int";

    private static String DOUBLE_VAL = "double";

    private static String STRING_VAL = "string";

    public SBolt(String channel, String agentName)
    {
        currentObservation = null;
        lastObservation = null;

        // Initialize instance variables
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
        command = new robot_command_t();
        command.action = "";

        kernel = Kernel.CreateKernelInNewThread();
        agent = kernel.CreateAgent(agentName);
        if (agent == null)
        {
            throw new IllegalStateException("Kernel created null agent");
        }
        Boolean loadResult = agent.LoadProductions("agent/simple_agent.soar");

        agent.AddOutputHandler("command", this, null);
        agent.AddOutputHandler("message", this, null);

        // !!! Important !!!
        // We set AutoCommit to false, and only commit inside of the event
        // handler
        // for the RunEvent right before the next Input Phase
        // Otherwise the system would apparently hang on a commit
        kernel.SetAutoCommit(false);
        agent.RegisterForRunEvent(smlRunEventId.smlEVENT_BEFORE_INPUT_PHASE,
                this, null);

        agent.SpawnDebugger(kernel.GetListenerPort(),
                System.getenv().get("SOAR_HOME"));

        // Set up input link.
        initInputLink();

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
        observationsMap = new HashMap<Integer, Identifier>();
        sensiblesMap = new HashMap<Integer, Identifier>();

        Identifier il = agent.GetInputLink();
        sensiblesId = il.CreateIdWME("sensibles");
        messagesId = il.CreateIdWME("messages");
        il.CreateStringWME("int", "1");
        agent.Commit();
    }

    private void initChatFrame()
    {
        messageIdNum = 100;
        chatMessageQueue = new ArrayList<String>();
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
        chatFrame.setDefaultCloseOperation(chatFrame.EXIT_ON_CLOSE);
        chatFrame.setVisible(true);
    }

    public void hideFrame()
    {
        chatFrame.setVisible(false);
    }

    private void sendUserChat(String message)
    {
        chatMessageQueue.add(message);
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
        currentObservation = obs;
    }

    /********************************************************************
     * InputLink Handling
     *******************************************************************/

    // Called right before the Agent's Input Phase,
    // Update the Input Link Here
    public void runEventHandler(int eventID, Object data, Agent agent, int phase)
    {
        // Only does an update if a new observation was recieved
        if (lastObservation != currentObservation)
        {
            processSensibles();
            processObservations();
            lastObservation = currentObservation;
        }
        updateInputLinkMessages();
        if (agent.IsCommitRequired())
        {
            agent.Commit();
        }
    }

    /**
     * Updates the input-link using the sensibles on the currentObservation
     */
    private void processSensibles()
    {
        if (currentObservation == null)
        {
            return;
        }
        // Id's of sensibles in the current observation
        Set<Integer> observationSensibles = new HashSet<Integer>();

        // Stores the keyValue pairs for each sensible
        Map<String, String> sensibleKeyVals = new HashMap<String, String>();

        // For each sensible, split the string into key,val pairs and update the
        // input-link
        for (String sensible : currentObservation.sensibles)
        {
            sensibleKeyVals.clear();
            String[] keyValPairs = sensible.split(",");

            Integer id = INVALID_ID;
            for (String keyValPair : keyValPairs)
            {
                String[] keyVal = keyValPair.split("=");
                if (keyVal.length < 2)
                {
                    // Note a valid key-val pair, must be "key=value"
                    continue;
                }

                sensibleKeyVals.put(keyVal[0], keyVal[1]);
                if (keyVal[0].toLowerCase().equals("id"))
                {
                    try
                    {
                        id = Integer.parseInt(keyVal[1]);
                        continue;
                    }
                    catch (Exception e)
                    {
                        // Not a valid id
                        id = INVALID_ID;
                        break;
                    }
                }
            }

            if (id == INVALID_ID)
            {
                // That sensible string does not have an id key
                continue;
            }

            observationSensibles.add(id);

            // Find the sensible Identifier in the sensiblesMap,
            // create a new Identifier if needed
            Identifier sensibleId = null;
            if (sensiblesMap.containsKey(id))
            {
                sensibleId = sensiblesMap.get(id);
            }
            else
            {
                sensibleId = sensiblesId.CreateIdWME("sensible");
                sensiblesMap.put(id, sensibleId);
            }
            updateSensibleOnInputLink(sensibleId, sensibleKeyVals);
        }

        // Remove sensibles not in the new observation
        Set<Integer> sensiblesToRemove = new HashSet<Integer>();
        for (Integer id : sensiblesMap.keySet())
        {
            if (!observationSensibles.contains(id))
            {
                sensiblesToRemove.add(id);
            }
        }
        for (Integer id : sensiblesToRemove)
        {
            sensiblesMap.get(id).DestroyWME();
            sensiblesMap.remove(id);
        }
    }

    /**
     * Updates the input-link using the observations on the currentObservation
     */
    private void processObservations()
    {
        if (currentObservation == null)
        {
            return;
        }
        Set<Integer> curObservations = new HashSet<Integer>();
        Map<String, String> observationKeyVals = new HashMap<String, String>();

        // For each observation, build up a list of key value pairs then update
        // the input-link
        for (object_data_t observation : currentObservation.observations)
        {
            curObservations.add(observation.id);

            observationKeyVals.clear();
            observationKeyVals.put("id", String.valueOf(observation.id));
            observationKeyVals.put("x", String.valueOf(observation.pos[0]));
            observationKeyVals.put("y", String.valueOf(observation.pos[1]));
            observationKeyVals.put("t", String.valueOf(observation.pos[2]));
            for (String nounjective : observation.nounjective)
            {
                observationKeyVals.put(nounjective, "nounjective");
            }

            // Find the observation Identifier in the observationsMap,
            // create a new Identifier if needed
            Identifier observationId = null;
            if (observationsMap.containsKey(observation.id))
            {
                observationId = observationsMap.get(observation.id);
            }
            else
            {
                observationId = sensiblesId.CreateIdWME("sensible");
                observationsMap.put(observation.id, observationId);
            }
            updateSensibleOnInputLink(observationId, observationKeyVals);
        }

        // Remove observations not in the currentObservation
        Set<Integer> observationsToRemove = new HashSet<Integer>();
        for (Integer id : observationsMap.keySet())
        {
            if (!curObservations.contains(id))
            {
                observationsToRemove.add(id);
            }
        }
        for (Integer id : observationsToRemove)
        {
            observationsMap.get(id).DestroyWME();
            observationsMap.remove(id);
        }
    }

    /**
     * Updates the given identifier (assumed to be
     * input-link.sensibles.sensible) by making sure the identifier has exactly
     * the given key-value pairs
     */
    private void updateSensibleOnInputLink(Identifier sensibleId,
            Map<String, String> keyValPairs)
    {
        Set<String> existingKeys = new HashSet<String>(); // Set of keys already
                                                          // on the WME

        // Update each attribute on the sensible
        for (int i = 0; i < sensibleId.GetNumberChildren(); i++)
        {
            WMElement attributeWME = sensibleId.GetChild(i);
            if (!attributeWME.GetAttribute().equals("attribute")
                    || !attributeWME.IsIdentifier())
            {
                continue;
            }
            Identifier attributeId = attributeWME.ConvertToIdentifier();

            WMElement keyWME = attributeId.FindByAttribute("key", 0);
            if (keyWME == null)
            {
                // Attribute does not have a key, error
                continue;
            }

            String keyString = keyWME.GetValueAsString();
            if (keyValPairs.containsKey(keyString))
            {
                // That key still exists, update if necessary
                updateValueAttribute(attributeId, keyValPairs.get(keyString));
                existingKeys.add(keyString);
            }
            else
            {
                attributeId.DestroyWME();
            }
        }

        // Create new attribute WMEs for keys not already on the sensible WME
        for (Map.Entry<String, String> keyValPair : keyValPairs.entrySet())
        {
            if (existingKeys.contains(keyValPair.getKey()))
            {
                continue;
            }
            Identifier attributeId = sensibleId.CreateIdWME("attribute");
            attributeId.CreateStringWME("key",
                    String.valueOf(keyValPair.getKey()));
            updateValueAttribute(attributeId, keyValPair.getValue());
        }
    }

    /**
     * Updates the given Identifier so that it becomes: <element> ^value
     * <value>, and is of the appropriate type
     */
    private void updateValueAttribute(Identifier element, String value)
    {
        String valueType = getValueTypeOfString(value);
        WMElement valueWME = element.FindByAttribute("value", 0);

        if (valueWME != null && !valueType.equals(valueWME.GetValueType()))
        {
            // Need to change the type of the WMElement
            valueWME.DestroyWME();
            valueWME = null;
        }

        if (valueWME == null)
        {
            // Create a new value WME of the appropriate type
            if (valueType.equals(INTEGER_VAL))
            {
                element.CreateIntWME("value", Integer.parseInt(value));
            }
            else if (valueType.equals(DOUBLE_VAL))
            {
                element.CreateFloatWME("value", Double.parseDouble(value));
            }
            else
            {
                element.CreateStringWME("value", value);
            }
            return;
        }

        // Otherwise, update the WME using the given value
        if (valueWME.GetValueType().equals(INTEGER_VAL))
        {
            valueWME.ConvertToIntElement().Update(Integer.parseInt(value));
        }
        else if (valueWME.GetValueType().equals(DOUBLE_VAL))
        {
            valueWME.ConvertToFloatElement().Update(Double.parseDouble(value));
        }
        else
        {
            valueWME.ConvertToStringElement().Update(value);
        }

    }

    /**
     * Returns the type of the given string, can be either INTEGER_VAL,
     * DOUBLE_VAL, or STRING_VAL
     */
    private String getValueTypeOfString(String s)
    {
        try
        {
            Integer.parseInt(s);
            return INTEGER_VAL;
        }
        catch (NumberFormatException e)
        {
            try
            {
                Double.parseDouble(s);
                return DOUBLE_VAL;
            }
            catch (NumberFormatException e2)
            {
                return STRING_VAL;
            }
        }
    }

    private void updateInputLinkMessages()
    {
        // if new message(s), remove all current messages on input link first
        if (!chatMessageQueue.isEmpty())
        {
            for (int i = 0; i < messagesId.GetNumberChildren(); i++)
            {
                WMElement childWME = messagesId.GetChild(i);
                if (!(childWME.GetAttribute().equals("message") && childWME
                        .IsIdentifier()))
                {
                    continue;
                }
                Identifier mId = childWME.ConvertToIdentifier();
                mId.DestroyWME();
            }
        }
        
        for (String message : chatMessageQueue)
        {
            
            String[] c = message.split(" and ");
            for (String m : c)
            {
                
                String[] words = m.split(" ");
                
                Identifier mId = messagesId.CreateIdWME("message");
                Identifier rest = mId.CreateIdWME("words");
                mId.CreateIntWME("id", messageIdNum);
                mId.CreateIntWME("time", System.currentTimeMillis());
             
                for (String w : words)  
                {  
                    rest.CreateStringWME("first-word", w);
               
                    rest = rest.CreateIdWME("rest");
                } 
	                
                messageIdNum++;
            }
        }
        chatMessageQueue.clear();
    }

    /********************************************************************
     * OutputLink Handling
     *******************************************************************/

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

        if (agent.IsCommitRequired())
        {
            agent.Commit();
        }
    }

    private void processOutputLinkMessage(Identifier messageId)
    {
        if (messageId == null)
        {
            return;
        }

        if (messageId.GetNumberChildren() == 0)
        {
            messageId.CreateStringWME("status", "error");
            throw new IllegalStateException("Message has no children");
        }

        String message = "";
        WMElement wordsWME = messageId.FindByAttribute("words", 0);
        if (wordsWME == null || !wordsWME.IsIdentifier())
        {
            messageId.CreateStringWME("status", "error");
            throw new IllegalStateException("Message has no words attribute");
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
                    message += child.GetValueAsString() + " ";
                }
                else if (child.GetAttribute().equals("rest")
                        && child.IsIdentifier())
                {
                    nextWordId = child.ConvertToIdentifier();
                }
            }
            currentWordId = nextWordId;
        }

        if (message == "")
        {
            messageId.CreateStringWME("status", "error");
            throw new IllegalStateException("Message was empty");
        }

        message += ".";
        addChatMessage(message.substring(0, message.length() - 1));
        messageId.CreateStringWME("status", "complete");
    }

    private void processOutputLinkCommand(Identifier commandId)
    {
        if (commandId == null || commandId.GetNumberChildren() == 0)
        {
            return;
        }

        int numChildren = commandId.GetNumberChildren();
        for (int i = 0; i < numChildren; ++i)
        {
            WMElement child = commandId.GetChild(i);

            if (child.GetAttribute().equals("action"))
            {
                processActionCommand(child.ConvertToIdentifier());
            }
            else if (child.GetAttribute().equals("destination"))
            {
                processDestinationCommand(child.ConvertToIdentifier());
            }
            else if (child.GetAttribute().equals("gripper"))
            {
                processGripperCommand(child.ConvertToIdentifier());
            }
        }
    }

    /**
     * Takes a destination command on the output link given as an identifier and
     * uses it to update the internal robot_command_t command
     */
    private void processDestinationCommand(Identifier destId)
    {
        if (destId == null)
        {
            return;
        }

        double x = 0.0;
        double y = 0.0;
        double t = 0.0;

        if (destId.FindByAttribute("None", 0) != null)
        {
            t = 10.0; // set t to 10 to ignore destination (stop)
        }
        else
        {
            WMElement xWme = destId.FindByAttribute("x", 0);
            WMElement yWme = destId.FindByAttribute("y", 0);
            WMElement tWme = destId.FindByAttribute("t", 0);

            if (xWme == null || yWme == null || tWme == null)
            {
                destId.CreateStringWME("status", "error");
                throw new IllegalStateException(
                        "Command has destination WME missing x, y, or t");
            }

            try
            {
                x = Double.valueOf(xWme.GetValueAsString());
                y = Double.valueOf(yWme.GetValueAsString());
                t = Double.valueOf(tWme.GetValueAsString());
            }
            catch (Exception e)
            {
                destId.CreateStringWME("status", "error");
                throw new IllegalStateException(
                        "Command has an invalid x, y, or t float");
            }
        }

        command.dest = new double[] { x, y, t };
        destId.CreateStringWME("status", "complete");
    }

    /**
     * Takes an action command on the output link given as an identifier and
     * uses it to update the internal robot_command_t command
     */
    private void processActionCommand(Identifier actionId)
    {
        if (actionId == null)
        {
            return;
        }

        StringBuffer actionBuf = new StringBuffer();

        int numPairs = 0;
        for (int i = 0; i < actionId.GetNumberChildren(); i++)
        {
            WMElement childWME = actionId.GetChild(i);
            if (!(childWME.GetAttribute().equals("pair") && childWME
                    .IsIdentifier()))
            {
                continue;
            }
            Identifier pairId = childWME.ConvertToIdentifier();

            // Get key of pair
            WMElement keyWME = pairId.FindByAttribute("key", 0);
            if (keyWME == null || keyWME.GetValueAsString().length() == 0)
            {
                actionId.CreateStringWME("status", "error");
                throw new IllegalStateException("Action has a pair with no key");
            }
            String key = keyWME.GetValueAsString();

            // Get value of pair
            WMElement valueWME = pairId.FindByAttribute("value", 0);
            if (valueWME == null || valueWME.GetValueAsString().length() == 0)
            {
                actionId.CreateStringWME("status", "error");
                throw new IllegalStateException(
                        "Action has a pair with no value");
            }
            String value = valueWME.GetValueAsString();

            actionBuf.append(key + "=" + value + ",");
            numPairs++;
        }

        if (numPairs == 0)
        {
            actionId.CreateStringWME("status", "error");
            throw new IllegalStateException("Action has no pairs");
        }

        command.action = actionBuf.toString();
        command.action = command.action.substring(0,
                command.action.length() - 1);
        actionId.CreateStringWME("status", "complete");
    }

    /**
     * Takes a gripper command on the output link given as an identifier and
     * uses it to update the internal robot_command_t command
     */
    private void processGripperCommand(Identifier gripperId)
    {
        if (gripperId == null)
        {
            return;
        }

        WMElement performWME = gripperId.FindByAttribute("perform", 0);
        if (performWME == null)
        {
            gripperId.CreateStringWME("status", "error");
            throw new IllegalStateException(
                    "Gripper command does not have a perform WME");

        }

        String gripperAction = performWME.GetValueAsString();
        if (!gripperAction.equals("open") && !gripperAction.equals("close"))
        {
            gripperId.CreateStringWME("status", "error");
            throw new IllegalStateException(
                    "Gripper command is not 'open' or 'close'");
        }

        command.gripper_open = gripperAction.equals("open");
        gripperId.CreateStringWME("status", "complete");
    }

    /**
     * Sends out the robot_command_t command via LCM
     */
    private void broadcastLcmCommand()
    {
        if (command == null)
        {
            return;
        }
        synchronized (command)
        {
            lcm.publish("sbolt_commands", command);
        }
    }

    public static void main(String[] args)
    {
        SBolt sbolt = new SBolt("abolt_observations", "sbolt");
        sbolt.showFrame();
        sbolt.start();
    }

}
