package edu.umich.sbolt.world;

import edu.umich.sbolt.language.BOLTDictionary;
import edu.umich.sbolt.language.Parser;
import sml.Identifier;
import sml.WMElement;
/**
 * Represents the latest message from the user on the input link
 * 
 * @author mininger
 * 
 */
public class Messages implements IInputLinkElement
{
    // Identifier of the message on the input link
    private Identifier messageId;

    // Latest message received
    private String latestMessage;
    
    // Id of the latest message received
    private static int latestMessageId;
    
    // True if a new message was received since the last update
    private Boolean messageChanged;
    
    // Reference to the world (used to get the time)
    private World world;
    
    // Represents an invalid id, or that no message is on the input-link
    private final Integer INVALID_ID = -1;
    
    private BOLTDictionary dictionary = new BOLTDictionary("src/edu/umich/sbolt/language/dictionary.txt"); 
    

    private int messageNumber;
  

    private Parser parser;
    

    public Messages(World world){
        latestMessage = "";
        latestMessageId = INVALID_ID;   
        messageChanged = false;
        this.world = world;
        parser = new Parser(dictionary);
    }


    @Override
    public synchronized void updateInputLink(Identifier parentIdentifier)
    {
        if(!messageChanged){
            return;
        }
        
        if(messageId != null){
            messageId.DestroyWME();
        }
        
        
        messageId = parentIdentifier.CreateIdWME("message");
        messageNumber = latestMessageId;
        messageId.CreateIntWME("id", latestMessageId);
        parser.getSoarSpeak(latestMessage, messageId);
        messageChanged = false;
    }

    @Override
    public synchronized void destroy()
    {
        if(messageId != null){
            messageId.DestroyWME();
            messageId = null;
        }
    }
    
    
    public synchronized void addMessage(String message){
        latestMessageId++;
        latestMessage = message;
        messageChanged = true;
    }
    
    public Integer getIdNumber(){
    	return messageNumber;
    }
}