package edu.umich.sbolt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.umich.sbolt.controller.RobotPositionListener;

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
    // Gives ID numbers to a object based on its name
    private Map<String, Integer> objectIds;
    
    // number to start assigning object IDs with
    private int nextObjectId;
    
    // Maps observation IDs onto their objects identifiers
    private Map<Integer, Identifier> observationsMap;

    // Maps object IDs onto their objects identifier
    private Map<Integer, Identifier> objectsMap;

    // Root identifier for all object observations
    private Identifier objectsId;

    // Root identifier for all messages the robot receives
    private Identifier inputLinkId;
    
    // Root identifier for the robot
    private Identifier robotId;
    
    // Root identifier for the time
    private Identifier timeId;
    
    // The most recent observations_t received
    private observations_t currentObservation;

    // The observation used in the last input-link update
    private observations_t lastObservation;
    
    private String latestMessage;
    
    private int latestMessageId;
    
    // A counter that is used for the id for each message
    private int messageIdNum;
    
    private long startTime = 0;
    
    private int stepNumber = 0;
    
    // Indicates an invalid ID number
    private static Integer INVALID_ID = -1;
    
    private static Integer ROBOT_ID;

    // These are the three types that a WME can be, conform to Soar
    private static String INTEGER_VAL = "int";

    private static String DOUBLE_VAL = "double";

    private static String STRING_VAL = "string";
    
    private List<RobotPositionListener> positionListeners = new ArrayList<RobotPositionListener>();
    
    private Map<String, String> propertyCategories;
    
    public InputLinkHandler(SBolt sbolt){
        observationsMap = new HashMap<Integer, Identifier>();
        objectsMap = new HashMap<Integer, Identifier>();

        objectIds = new HashMap<String, Integer>();
        nextObjectId = 1000;
        ROBOT_ID = 999;
        objectIds.put("robot", ROBOT_ID);
        
        inputLinkId = sbolt.getAgent().GetInputLink();
        objectsId = inputLinkId.CreateIdWME("objects");
        robotId = inputLinkId.CreateIdWME("self");
        robotId.CreateIdWME("pose");
        timeId = inputLinkId.CreateIdWME("time");
        
        sbolt.getAgent().Commit();

        currentObservation = null;
        lastObservation = null;
        
        messageIdNum = 100;
        latestMessage = "";
        latestMessageId = INVALID_ID;
        
        sbolt.getAgent().RegisterForRunEvent(smlRunEventId.smlEVENT_BEFORE_INPUT_PHASE,
                this, null);
        
        propertyCategories = new HashMap<String, String>();
        addCategory("color", "red,green,black,beige,tan");
        addCategory("shape", "cube,round,cylinder");
        addCategory("size", "small,medium,large");
        addCategory("cooked-state", "raw,cooked");
        addCategory("clean-state", "dirty,clean");
    }
    
    private void addCategory(String category, String propertyList){
        String[] properties = propertyList.split(",");
        for(String property : properties){
            propertyCategories.put(property, category);
        }
    }
    
    public void addRobotPositionListener(RobotPositionListener listener) 
    {
        positionListeners.add(listener);
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
        if(startTime == 0){
            startTime = currentObservation.utime;
        }
    }

    // Called right before the Agent's Input Phase,
    // Update the Input Link Here
    public void runEventHandler(int eventID, Object data, Agent agent, int phase)
    {
        // Only does an update if a new observation was recieved
        if (lastObservation != currentObservation)
        {
            updateSensibles();
            updateObservations();
            updateTime();
            lastObservation = currentObservation;
        }
        updateMessages();
        if (agent.IsCommitRequired())
        {
            agent.Commit();
        }
    }

    /**
     * Updates the input-link using the sensibles on the currentObservation
     */
    private void updateSensibles()
    {
        if (currentObservation == null)
        {
            return;
        }
        // Id's of sensibles in the current observation
        Set<Integer> observationSensibles = new HashSet<Integer>();

        // For each sensible, split the string into key,val pairs and update the
        // input-link
        for (String sensible : currentObservation.sensables)
        {
            Integer sensibleId = updateSensible(sensible);
            if(sensibleId != INVALID_ID && sensibleId != ROBOT_ID){
                observationSensibles.add(sensibleId);
            }
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
    
    private Integer updateSensible(String sensible){
        // Stores the keyValue pairs for each sensible
        Map<String, String> sensibleKeyVals = new HashMap<String, String>();
        sensible = sensible.toLowerCase();
        String[] keyValPairs = sensible.split(",");

        Integer id = INVALID_ID;
        
        for (String keyValPair : keyValPairs)
        {
            if(keyValPair.startsWith("[")){
                //Processing pose information
                String[] pose = (keyValPair.substring(1, keyValPair.length() - 1)).split(" ");
                sensibleKeyVals.put("x", pose[0]);
                sensibleKeyVals.put("y", pose[1]);
                sensibleKeyVals.put("t", pose[2]);
                continue;
            }
            
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
                id = ROBOT_ID;
                sensibleKeyVals.put("name", "robot");
                keyVal[1] = keyVal[1].substring(1, keyVal[1].length() - 1);
                String[] params = keyVal[1].split(" ");
                sensibleKeyVals.put("x", params[0]);
                sensibleKeyVals.put("y", params[1]);
                sensibleKeyVals.put("t", params[2]);
                for (RobotPositionListener listener : positionListeners)
                {
                    try
                    {
                        double x = Double.valueOf(params[0]);
                        double y = Double.valueOf(params[1]);
                        double t = Double.valueOf(params[2]);
                        listener.robotPositionChanged(x, y, t);
                    }
                    catch (NumberFormatException e)
                    {
                        e.printStackTrace();
                    }
                }
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
                return INVALID_ID;
            }
        }
        
        if(id == ROBOT_ID){
            updateRobot(sensibleKeyVals);
        } else {
            // Find the object Identifier in the objectsMap,
            // create a new Identifier if needed
            Identifier objectId = null;
            if (objectsMap.containsKey(id))
            {
                objectId = objectsMap.get(id);
            }
            else
            {
                objectId = objectsId.CreateIdWME("object");
                objectsMap.put(id, objectId);
            }
            updateObjectOnInputLink(objectId, sensibleKeyVals);
        }

        return id;
    }
    
    private void updateRobot(Map<String, String> robotInfo){
        WMElement poseWME = robotId.FindByAttribute("pose", 0);
        Identifier poseId = null;
        if(poseWME != null && poseWME.IsIdentifier()){
            poseId = poseWME.ConvertToIdentifier();
        }
        for(Map.Entry<String, String> keyValPair : robotInfo.entrySet()){
            if(keyValPair.getKey().equals("x") ||
                    keyValPair.getKey().equals("y") ||keyValPair.getKey().equals("t")){
                updateWME(poseId, keyValPair.getKey(), keyValPair.getValue());
            } else {
                updateWME(robotId, keyValPair.getKey(), keyValPair.getValue());
            }
        }
    }

    /**
     * Updates the input-link using the observations on the currentObservation
     */
    private void updateObservations()
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
            for (String property : observation.nounjectives)
            {
                property = property.toLowerCase();
                if(propertyCategories.containsKey(property)){
                    observationKeyVals.put(propertyCategories.get(property), property);
                } else {
                    observationKeyVals.put("unknown", property);
                }
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
                observationId = objectsId.CreateIdWME("object");
                observationsMap.put(observation.id, observationId);
            }
            updateObjectOnInputLink(observationId, observationKeyVals);
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
    
    private void updateTime(){
        stepNumber++;
        updateWME(timeId, "steps", String.valueOf(stepNumber));
        if(currentObservation != null){
            long timeDif = currentObservation.utime - startTime;
            long milliseconds = timeDif/1000;
            long seconds = timeDif/1000000;
            updateWME(timeId, "seconds", String.valueOf(seconds));
            updateWME(timeId, "milliseconds", String.valueOf(milliseconds));
        }
    }

    /**
     * Updates the given identifier (assumed to be
     * input-link.objects.object) by making sure the identifier has exactly
     * the given key-value pairs
     */
    private void updateObjectOnInputLink(Identifier objectId,
            Map<String, String> keyValPairs)
    {
        Set<Identifier> attributesToDestroy = new HashSet<Identifier>();

        // Update each attribute on the sensible
        for (int i = 0; i < objectId.GetNumberChildren(); i++)
        {
            WMElement attributeWME = objectId.GetChild(i);
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
            updateWME(objectId, keyValPair.getKey(), keyValPair.getValue());
        }
        
        for(Identifier attributeId : attributesToDestroy){
            attributeId.DestroyWME();
        }
    }

    /**
     * Updates the given Identifier so that it becomes: identifier ^attribute
     * value, and is of the appropriate type
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
    
    private void updateMessages(){
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
