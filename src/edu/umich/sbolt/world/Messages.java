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
    private int latestMessageId;
    
    // True if a new message was received since the last update
    private Boolean messageChanged;
    
    // Reference to the world (used to get the time)
    private World world;
    
    // Represents an invalid id, or that no message is on the input-link
    private final Integer INVALID_ID = -1;
    
    private BOLTDictionary dictionary = new BOLTDictionary("src/edu/umich/sbolt/language/dictionary.txt"); 
    
    public Messages(World world){
        latestMessage = "";
        latestMessageId = INVALID_ID;   
        messageChanged = false;
        this.world = world;
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
        Parser.getSoarSpeak(latestMessage, dictionary, messageId);
        
        /*
        //Add the new message
        String[] words = latestMessage.split(" ");
        
        
        Identifier rest = messageId.CreateIdWME("words");
        messageId.CreateIntWME("id", latestMessageId);
        messageId.CreateIntWME("time", world.getTime());
        messageId.CreateStringWME("from", "user");
     
        for(int i = 0; i < words.length; i++){
            WorkingMemoryUtil.updateWME(rest, "word", words[i]);
            if(i != words.length - 1){
                rest = rest.CreateIdWME("next");
            }
        } */
        
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
}
