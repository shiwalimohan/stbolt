package edu.umich.sbolt.world;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides a way to lookup an attribute based on a value
 * For example, you could lookup the attribute 'color' for the value 'red'
 * 
 * @author mininger
 * 
 */
public class AttributeManager
{
    // static Manager used to lookup an attribute based on a value
    public static AttributeManager Manager = new AttributeManager();
    
    // mapping of values to the associated attribute
    private Map<String, String> attributes;
    
    public AttributeManager(){
        attributes = new HashMap<String, String>();
        addAttribute("color", "red,green,black,beige,tan,blue,gray");
        addAttribute("shape", "cube,sphere,cylinder");
        addAttribute("size", "small,medium,large");
        addAttribute("cooked-state", "raw,cooked");
        addAttribute("clean-state", "dirty,clean");
    }

    /**
     * valueList is a comma-separated list of values that the given
     * attributes can take
     */
    public void addAttribute(String attribute, String valueList){
        attribute = attribute.toLowerCase();
        valueList = valueList.toLowerCase();
        String[] values = valueList.split(",");
        for(String value : values){
            value.trim();
            attributes.put(value, attribute);
        }
    }

    /**
     * looks up the attribute based on the value
     * For example: getAttribute("red") would return "color"
     */
    public String getAttribute(String value){
        return attributes.get(value.toLowerCase());
    }
}
