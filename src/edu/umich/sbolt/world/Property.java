package edu.umich.sbolt.world;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import sml.FloatElement;
import sml.Identifier;
import sml.StringElement;
import abolt.lcmtypes.categorized_data_t;
import abolt.lcmtypes.category_t;

public class Property implements IInputLinkElement{
    
    // Root identifier for the category
    protected Identifier propertyID;
    
    // String WME for the type
    protected String type;
    protected StringElement typeWME;
    
    // String WME for the name
    protected String name;
    protected StringElement nameWME;
    
    // String WME for the value
    protected String value;
    protected StringElement valueWME;
    
    public Property(String type, String name, String value){
    	propertyID = null;
    	
    	this.type = type.toLowerCase();
    	typeWME = null;
    	
    	this.name = name.toLowerCase();
    	nameWME = null;
    	
    	this.value = value.toLowerCase();
    	valueWME = null;
    }

    // Accessors
    public String getName(){
        return name;
    }
    
    public String getValue(){
    	return value;
    }

    // Mutators
    public void update(String value){
    	this.value = value.toLowerCase();
    }

    @Override
    public synchronized void updateInputLink(Identifier parentIdentifier)
    {
    	if(propertyID == null){
    		propertyID = parentIdentifier.CreateIdWME("property");
    		typeWME = propertyID.CreateStringWME("type", type);
    		nameWME = propertyID.CreateStringWME("name", name);
    		valueWME = propertyID.CreateStringWME("value", value);
    	}
    	
    	if(!typeWME.GetValueAsString().equals(type)){
    		typeWME.Update(type);
    	}
    	if(!nameWME.GetValueAsString().equals(name)){
    		nameWME.Update(name);
    	}
    	if(!valueWME.GetValueAsString().equals(value)){
    		valueWME.Update(value);
    	}
    }

    @Override
    public synchronized void destroy()
    {
    	if(propertyID != null){
    		valueWME.DestroyWME();
    		valueWME = null;
    		
    		nameWME.DestroyWME();
    		nameWME = null;
    		
    		typeWME.DestroyWME();
    		typeWME = null;
    		
    		propertyID.DestroyWME();
    		propertyID = null;
    	}
    }
}
