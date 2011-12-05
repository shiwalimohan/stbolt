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
    private Map<String, Integer> objectIds;
    
    // number to start assigning sensible IDs with
    private int nextObjectId;
    
    // Maps observation IDs onto their sensibles identifiers
    private Map<Integer, Identifier> observationsMap;

    // Maps sensible IDs onto their sensibles identifier
    private Map<Integer, Identifier> objectsMap;

    // Root identifier for all sensible observations
    private Identifier objectsId;

    // Root identifier for all messages the robot receives
    private Identifier inputLinkId;
    
    // The most recent observations_t received
    private observations_t currentObservation;

    // The observation used in the last input-link update
    private observations_t lastObservation;
    
    private String latestMessage;
    
    private int latestMessageId;
    
    // A counter that is used for the id for each message
    private int messageIdNum;
    
    // Indicates an invalid ID number
    private static Integer INVALID_ID = -1;

    // These are the three types that a WME can be, conform to Soar
    private static String INTEGER_VAL = "int";

    private static String DOUBLE_VAL = "double";

    private static String STRING_VAL = "string";
    
    
    public InputLinkHandler(SBolt sbolt){
        observationsMap = new HashMap<Integer, Identifier>();
        objectsMap = new HashMap<Integer, Identifier>();

        objectIds = new HashMap<String, Integer>();
        nextObjectId = 1000;
        
        inputLinkId = sbolt.getAgent().GetInputLink();
        objectsId = inputLinkId.CreateIdWME("objects");
        sbolt.getAgent().Commit();

        currentObservation = null;
        lastObservation = null;
        
        messageIdNum = 100;
        latestMessage = "";
        latestMessageId = INVALID_ID;
        
        sbolt.getAgent().RegisterForRunEvent(smlRunEventId.smlEVENT_BEFORE_INPUT_PHASE,
                this, null);
    }
    
    public void addMessage(String message){
        messageIdNum++;
        latestMessage = message;
        latestMessageId = messageIdNum;
        
    }
    
    public void removeMessage(int idToRemove){
        if(idToRemove == latestMessageId){
            latestMessage = "";
            latestMessageId = INVALID_ID;
        }
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
        processMessages();
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
                    if(objectIds.containsKey(name)){
                        id = objectIds.get(name);
                    } else {
                        objectIds.put(name, nextObjectId);
                        nextObjectId++;
                    }
                } else {
                    continue;
                }
            }

            observationSensibles.add(id);

            // Find the sensible Identifier in the sensiblesMap,
            // create a new Identifier if needed
            Identifier sensibleId = null;
            if (objectsMap.containsKey(id))
            {
                sensibleId = objectsMap.get(id);
            }
            else
            {
                sensibleId = objectsId.CreateIdWME("sensible");
                objectsMap.put(id, sensibleId);
            }
            updateSensibleOnInputLink(sensibleId, sensibleKeyVals);
        }

        // Remove sensibles not in the new observation
        Set<Integer> sensiblesToRemove = new HashSet<Integer>();
        for (Integer id : objectsMap.keySet())
        {
            if (!observationSensibles.contains(id))
            {
                sensiblesToRemove.add(id);
            }
        }
        for (Integer id : sensiblesToRemove)
        {
            objectsMap.get(id).DestroyWME();
            objectsMap.remove(id);
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
                observationId = objectsId.CreateIdWME("sensible");
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
        Set<Identifier> attributesToDestroy = new HashSet<Identifier>();

        // Update each attribute on the sensible
        for (int i = 0; i < sensibleId.GetNumberChildren(); i++)
        {
            WMElement attributeWME = sensibleId.GetChild(i);
            if (!attributeWME.IsIdentifier())
            {
                continue;
            }
            Identifier attributeId = attributeWME.ConvertToIdentifier();
            String attribute = attributeId.GetAttribute();
            
            if(!keyValPairs.containsKey(attribute)){
                attributesToDestroy.add(attributeId);
            }
        }
        
        for(Map.Entry<String, String> keyValPair : keyValPairs.entrySet()){
            updateWME(sensibleId, keyValPair.getKey(), keyValPair.getValue());
        }
        
        for(Identifier attributeId : attributesToDestroy){
            attributeId.DestroyWME();
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
    
    private void processMessages(){
        WMElement message = inputLinkId.FindByAttribute("message", 0);
        
        // If no latest message, remove the existing message (if there) and quit
        if(latestMessageId == INVALID_ID){
            if(message != null){
                message.DestroyWME();
            }
            return;
        }
        
        // If the message has changed remove the old one
        if(message != null){
            Identifier messageId = message.ConvertToIdentifier();
            WMElement idId = messageId.FindByAttribute("id", 0);
            if(idId != null){
                String currentId = idId.GetValueAsString();
                if(currentId.equals(latestMessageId)){
                    //Message hasn't changed
                    return;
                }
            }
            message.DestroyWME();
        }
        
        //Add the new message
        String[] words = latestMessage.split(" ");
        
        Identifier mId = inputLinkId.CreateIdWME("message");
        Identifier rest = mId.CreateIdWME("words");
        mId.CreateIntWME("id", latestMessageId);
        mId.CreateIntWME("time", System.currentTimeMillis());
        mId.CreateStringWME("from", "user");
     
        for(int i = 0; i < words.length; i++){
            updateWME(rest, "word", words[i]);
            if(i != words.length - 1){
                rest = rest.CreateIdWME("next");
            }
        }  
    }
}
