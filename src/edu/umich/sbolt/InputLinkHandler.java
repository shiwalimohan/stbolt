package edu.umich.sbolt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sml.Agent;
import sml.Agent.RunEventInterface;
import sml.Identifier;
import sml.WMElement;
import sml.smlRunEventId;
import sml.Agent.OutputEventInterface;
import abolt.lcmtypes.object_data_t;
import abolt.lcmtypes.observations_t;

public class InputLinkHandler implements RunEventInterface
{
    // Gives ID numbers to a sensible based on its name
    private Map<String, Integer> sensibleIds;
    
    // number to start assigning sensible IDs with
    private int nextSensibleId;
    
    // Maps observation IDs onto their sensibles identifiers
    private Map<Integer, Identifier> observationsMap;

    // Maps sensible IDs onto their sensibles identifier
    private Map<Integer, Identifier> sensiblesMap;

    // Root identifier for all sensible observations
    private Identifier sensiblesId;

    // Root identifier for all messages the robot receives
    private Identifier messagesId;
    
    // The most recent observations_t received
    private observations_t currentObservation;

    // The observation used in the last input-link update
    private observations_t lastObservation;
    
    // A counter that is used for the id for each message
    private int messageIdNum;

    // A queue of the messages received since the last input phase
    private List<String> chatMessageQueue;
    
    // Indicates an invalid ID number
    private static Integer INVALID_ID = -1;

    // These are the three types that a WME can be, conform to Soar
    private static String INTEGER_VAL = "int";

    private static String DOUBLE_VAL = "double";

    private static String STRING_VAL = "string";
    
    
    public InputLinkHandler(SBolt sbolt){
        observationsMap = new HashMap<Integer, Identifier>();
        sensiblesMap = new HashMap<Integer, Identifier>();

        sensibleIds = new HashMap<String, Integer>();
        nextSensibleId = 1000;
        
        Identifier il = sbolt.getAgent().GetInputLink();
        sensiblesId = il.CreateIdWME("sensibles");
        messagesId = il.CreateIdWME("messages");
        sbolt.getAgent().Commit();

        currentObservation = null;
        lastObservation = null;
        
        messageIdNum = 100;
        chatMessageQueue = new ArrayList<String>();
        
        sbolt.getAgent().RegisterForRunEvent(smlRunEventId.smlEVENT_BEFORE_INPUT_PHASE,
                this, null);
    }
    
    public void addMessage(String message){
        chatMessageQueue.add(message);
    }
    
    public void removeMessage(int id){
        
    }
    
    public void updateObservation(observations_t observation){
        currentObservation = observation;
    }

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
        for (String sensible : currentObservation.sensables)
        {
            sensible = sensible.toLowerCase();
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

                if (keyVal[0].equals("id"))
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
                } else if(keyVal[0].equals("robot_pos")){
                    sensibleKeyVals.put("name", "robot");
                    keyVal[1] = keyVal[1].substring(1, keyVal[1].length() - 1);
                    String[] params = keyVal[1].split(" ");
                    sensibleKeyVals.put("x", params[0]);
                    sensibleKeyVals.put("y", params[1]);
                    sensibleKeyVals.put("t", params[2]);
                } else {
                    sensibleKeyVals.put(keyVal[0], keyVal[1]);
                }
            }

            if (id == INVALID_ID)
            {
                // That sensible string does not have an id key
                if(sensibleKeyVals.containsKey("name")){
                    String name = sensibleKeyVals.get("name");
                    if(sensibleIds.containsKey(name)){
                        id = sensibleIds.get(name);
                    } else {
                        sensibleIds.put(name, nextSensibleId);
                        nextSensibleId++;
                    }
                } else {
                    continue;
                }
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
            for (String nounjective : observation.nounjectives)
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
                updateWME(attributeId, "value", keyValPairs.get(keyString));
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
            updateWME(attributeId, "value", keyValPair.getValue());
        }
    }

    /**
     * Updates the given Identifier so that it becomes: <identifier> ^<attribute>
     * <value>, and is of the appropriate type
     */
    private void updateWME(Identifier identifier, String attribute, String value){
        String valueType = getValueTypeOfString(value);
        WMElement valueWME = identifier.FindByAttribute(attribute, 0);

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
                identifier.CreateIntWME(attribute, Integer.parseInt(value));
            }
            else if (valueType.equals(DOUBLE_VAL))
            {
                identifier.CreateFloatWME(attribute, Double.parseDouble(value));
            }
            else
            {
                identifier.CreateStringWME(attribute, value);
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
                    updateWME(rest, "first-word", w);
               
                    rest = rest.CreateIdWME("rest");
                } 
                    
                messageIdNum++;
            }
        }
        chatMessageQueue.clear();
    }
}
