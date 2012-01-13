package edu.umich.sbolt.world;

import sml.FloatElement;
import sml.Identifier;
import sml.IntElement;
import sml.StringElement;
import sml.WMElement;

/**
 * Provides some utility functions to better manipulate working memory
 * 
 * @author mininger
 * 
 */
public class WorkingMemoryUtil
{

    // These are the three types that a WME can be, conform to Soar
    public static String INTEGER_VAL = "int";

    public static String FLOAT_VAL = "double";

    public static String STRING_VAL = "string";
    
    /**
     * Updates the given Identifier so that it becomes: identifier ^attribute
     * value, and is of the appropriate type
     */
    public static void updateWME(Identifier identifier, String attribute, String value){
        String valueType = getValueTypeOfString(value);

        // Create a new value WME of the appropriate type
        if (valueType.equals(INTEGER_VAL))
        {
            updateIntWME(identifier, attribute, Integer.parseInt(value));
        }
        else if (valueType.equals(FLOAT_VAL))
        {
            updateFloatWME(identifier, attribute, Double.parseDouble(value));
        }
        else
        {
            updateStringWME(identifier, attribute, value);
        }
    }
    
    /**
     * Updates the given Identifier so that it becomes: identifier ^attribute
     * value, using a FloatWME
     */
    public static void updateFloatWME(Identifier identifier, String attribute, double value){
        WMElement valueWME = identifier.FindByAttribute(attribute, 0);

        if (valueWME == null){
            identifier.CreateFloatWME(attribute, value);
        } else if(valueWME.GetValueType().equals(FLOAT_VAL)){
            FloatElement floatWME = valueWME.ConvertToFloatElement();
            if(floatWME.GetValue() != value){
                floatWME.Update(value);
            }
        } else {
            valueWME.DestroyWME();
            identifier.CreateFloatWME(attribute, value);
        }
    }
    
    /**
     * Updates the given Identifier so that it becomes: identifier ^attribute
     * value, using a IntWME
     */
    public static void updateIntWME(Identifier identifier, String attribute, int value){
        WMElement valueWME = identifier.FindByAttribute(attribute, 0);

        if (valueWME == null){
            identifier.CreateIntWME(attribute, value);
        } else if(valueWME.GetValueType().equals(INTEGER_VAL)){
            IntElement intWME = valueWME.ConvertToIntElement();
            if(intWME.GetValue() != value){
                intWME.Update(value);
            }
        } else {
            valueWME.DestroyWME();
            identifier.CreateIntWME(attribute, value);
        }
    }
    
    /**
     * Updates the given Identifier so that it becomes: identifier ^attribute
     * value, using a StringWME
     */
    public static void updateStringWME(Identifier identifier, String attribute, String value){
        WMElement valueWME = identifier.FindByAttribute(attribute, 0);

        if (valueWME == null){
            identifier.CreateStringWME(attribute, value);
        } else if(valueWME.GetValueType().equals(STRING_VAL)){
            StringElement stringWME = valueWME.ConvertToStringElement();
            if(!stringWME.GetValue().equals(value)){
                stringWME.Update(value);
            }
        } else {
            valueWME.DestroyWME();
            identifier.CreateStringWME(attribute, value);
        }
    }
    
    /**
     * If the given identifier has the given attribute, the associated WME will
     * be destroyed (only the first)
     */
    public static void removeWME(Identifier identifier, String attribute){
        WMElement valueWME = identifier.FindByAttribute(attribute, 0);
        if(valueWME != null){
            valueWME.DestroyWME();
        }
    }

    /**
     * Returns the type of the given string, can be either INTEGER_VAL,
     * DOUBLE_VAL, or STRING_VAL
     */
    public static  String getValueTypeOfString(String s)
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
                return FLOAT_VAL;
            }
            catch (NumberFormatException e2)
            {
                return STRING_VAL;
            }
        }
    }
    
    public static String getAttributeString(Identifier id, String attribute){
        WMElement wme = id.FindByAttribute(attribute, 0);
        if(wme == null || wme.GetValueAsString().length() == 0){
            return null;
        }
        return wme.GetValueAsString();
    }
    
    public static String getAttributeString(Identifier id, String attribute, String errorMessage){
        WMElement wme = id.FindByAttribute(attribute, 0);
        if(wme == null || wme.GetValueAsString().length() == 0){
            id.CreateStringWME("status", "error");
            throw new IllegalStateException(errorMessage);
        }
        return wme.GetValueAsString();
    }
}
