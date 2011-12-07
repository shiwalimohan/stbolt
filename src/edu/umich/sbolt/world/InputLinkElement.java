package edu.umich.sbolt.world;

import sml.Identifier;
import sml.WMElement;

/**
 * An interface for objects that will get written to the InputLink
 * 
 * @author mininger
 * 
 */
public interface InputLinkElement
{    
    // update the input-link appropriately
    void updateInputLink(Identifier parentIdentifier);
    
    // remove the object from the input-link
    void destroy();
   
}
