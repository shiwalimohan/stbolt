package edu.umich.sbolt.world;

import sml.Identifier;
import sml.WMElement;

public class Messages implements InputLinkElement
{
    private Identifier messageId;

    private String latestMessage;
    
    private int latestMessageId;
    
    private final Integer INVALID_ID = -1;
    
    private Boolean messageChanged;
    
    private World world;
    
    public Messages(World world){
        latestMessage = "";
        latestMessageId = 1;   
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
        
        //Add the new message
        String[] words = latestMessage.split(" ");
        
        messageId = parentIdentifier.CreateIdWME("message");
        Identifier rest = messageId.CreateIdWME("words");
        messageId.CreateIntWME("id", latestMessageId);
        messageId.CreateIntWME("time", world.getTime());
        messageId.CreateStringWME("from", "user");
     
        for(int i = 0; i < words.length; i++){
            WorkingMemoryUtil.updateWME(rest, "word", words[i]);
            if(i != words.length - 1){
                rest = rest.CreateIdWME("next");
            }
        }  
        
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
