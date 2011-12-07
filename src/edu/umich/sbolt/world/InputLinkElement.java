package edu.umich.sbolt.world;

import sml.Identifier;
import sml.WMElement;

public interface InputLinkElement
{    
    void updateInputLink(Identifier parentIdentifier);
    
    void destroy();
   
}
