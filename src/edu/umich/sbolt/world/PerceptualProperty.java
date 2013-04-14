package edu.umich.sbolt.world;

import java.util.*;
import abolt.lcmtypes.*;
import sml.*;

/**
 * A category for each object, which contains several possible labels and their confidences
 * 
 * @author mininger
 * 
 */
public class PerceptualProperty implements IInputLinkElement
{   
	protected static HashMap<Integer, String> categoryNames = null;
	public static String getCategoryName(Integer categoryID){
		if(categoryNames == null){
			categoryNames = new HashMap<Integer, String>();
			categoryNames.put(category_t.CAT_COLOR, "color");
			categoryNames.put(category_t.CAT_SHAPE, "shape");
			categoryNames.put(category_t.CAT_SIZE, "size");
			categoryNames.put(category_t.CAT_TEXTURE, "texture");
			categoryNames.put(category_t.CAT_WEIGHT, "weight");
		}
		return categoryNames.get(categoryID);
	}
	
	public static Integer getCategoryID(String categoryName){
		if(categoryName.equals("color")){
			return category_t.CAT_COLOR;
		} else if(categoryName.equals("shape")){
			return category_t.CAT_SHAPE;
		} else if(categoryName.equals("size")){
			return category_t.CAT_SIZE;
		} else if(categoryName.equals("texture")){
			return category_t.CAT_TEXTURE;
		} else if(categoryName.equals("weight")){
			return category_t.CAT_WEIGHT;
		} else {
			return null;
		}
	}
	
	public static String getCategoryType(String categoryName){
		if(categoryName.equals("weight")){
			return "measurable";
		} else {
			return "visual";
		}
	}
    
    // Root identifier for the property
    protected Identifier propertyID;
    
    // Type (visual or measurable)
    protected StringElement typeWME;
    
    // Name
    protected int nameID;
    protected String name;
    protected StringElement nameWME;
    
    // Values
    protected Identifier valuesID;
    protected HashMap<String, Double> values;
    protected HashMap<String, FloatElement> valueWMEs;
    
    // Features
    protected Identifier featuresID;
    protected ArrayList<Double> features;
    protected ArrayList<FloatElement> featureWMEs;

    public PerceptualProperty(categorized_data_t category){
    	propertyID = null;
    	
    	typeWME = null;
    	
    	nameID = category.cat.cat;
    	name = getCategoryName(category.cat.cat);
    	nameWME = null;
    	
    	valuesID = null;
    	values = new HashMap<String, Double>();
    	valueWMEs = new HashMap<String, FloatElement>();
    	
    	featuresID = null;
    	features = new ArrayList<Double>();
    	featureWMEs = new ArrayList<FloatElement>();
    	
    	updateCategoryInfo(category);
    }
    
    // Accessors
    public String getName(){
        return name;
    }
    
    public HashMap<String, Double> getValues(){
    	return values;
    }

    @Override
    public synchronized void updateInputLink(Identifier parentIdentifier)
    {
    	if(propertyID == null){
    		// Root
    		propertyID = parentIdentifier.CreateIdWME("property");
    		
    		// Type
    		typeWME = propertyID.CreateStringWME("type", getCategoryType(name));
    		
    		// Name
    		nameWME = propertyID.CreateStringWME("name", name);
    		
    		// Values
    		valuesID = propertyID.CreateIdWME("values");
    		
    		// Features
    		if(features.size() > 0){
        		featuresID = propertyID.CreateIdWME("features");
        		for(int i = 0; i < features.size(); i++){
        			Identifier featureID = featuresID.CreateIdWME("feature");
        			featureID.CreateIntWME("index", i);
        			FloatElement featureWME = featureID.CreateFloatWME("value", features.get(i));
        			featureWMEs.add(featureWME);
        		}
    		}
    		
    	}
    	
    	for(int i = 0; i < features.size(); i++){
    		if(featureWMEs.get(i).GetValue() != features.get(i)){
    			featureWMEs.get(i).Update(features.get(i));
    		}
    	}
    	
    	Set<String> valuesToDestroy = new HashSet<String>();
    	for(String label : valueWMEs.keySet()){
    		valuesToDestroy.add(label);
    	}
    	
    	for(Map.Entry<String, Double> label : values.entrySet()){
    		if(valuesToDestroy.contains(label.getKey())){
    			// That WME already exists, update it
    			valuesToDestroy.remove(label.getKey());
    			FloatElement labelWME = valueWMEs.get(label.getKey());
    			if(labelWME.GetValue() != label.getValue()){
    				labelWME.Update(label.getValue());
    			}
    		} else {
    			valueWMEs.put(label.getKey(), valuesID.CreateFloatWME(label.getKey(), label.getValue()));
    		}
    	}
    	
    	for(String label : valuesToDestroy){
    		valueWMEs.get(label).DestroyWME();
    		valueWMEs.remove(label);
    	}
    }

    @Override
    public synchronized void destroy()
    {
    	if(propertyID != null){
    		valueWMEs.clear();
    		values.clear();
    		featureWMEs.clear();
    		features.clear();
    		propertyID.DestroyWME();
    		propertyID = null;
    	}
    }
    
    public synchronized void updateCategoryInfo(categorized_data_t category){
    	if(category.cat.cat != nameID){
    		return;
    	}
    	Set<String> valuesToRemove = new HashSet<String>();
    	for(String label : values.keySet()){
    		valuesToRemove.add(label);
    	}
    	for(int i = 0; i < category.len; i++){
    		if(category.confidence[i] < .05){
    			// Ignore values that are really low
    			continue;
    		}
    		if(valuesToRemove.contains(category.label[i].toLowerCase())){
    			valuesToRemove.remove(category.label[i].toLowerCase());
    		}
			values.put(category.label[i].toLowerCase(), category.confidence[i]);
			// AM: only consider the first one
			break;
    	}
    	for(String label : valuesToRemove){
    		values.remove(label);
    	}
    	for(int i = 0; i < category.num_features; i++){
    		if(i >= features.size()){
    			features.add(category.features[i]);
    		} else {
    			features.set(i, category.features[i]);
    		}
    	}
    }
}
