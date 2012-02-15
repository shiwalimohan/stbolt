package edu.byu.lgsoar.utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * This class manages constants which hold directory paths of resource locations and flags which
 * determine application behavior. After the constants are loaded from constant.txt (or somewhere
 * else) they can be accessed with their name using {@link getProperty(String key) getProperty).
 * @author Nate Glenn
 *
 */
public class Constants {
	
	private static String constantsPath = "constants.txt";

	//the default values for the properties map
	
	//Set to 1 if you want wme numbers to be shown in the GraphViz output.
	//Set to 0 if you want pretty numberless trees
	private static final String GRAPH_WME_NUM = "1";
	private static final String SOAR_PATH =  "./soar";
	private static final String SENTENCE_FILE =  "./sentence.txt";
	private static final String PERL_PATH = "./perl";
	private static final String WN_DICT_DIR = "./lib/3.0/dict";
	
	private static final String LCS_FILE = "./verbs-English.lcs";
	private static final String AGENT_NAME = "LGSoar";
	private static final String LGP_DATA_PATH = "./data";
	
	private static Map<String, String> properties = new HashMap<String,String>();
	
	//always load constants. This can be done again by calling loadConstants
	static {
		//first load into properties the default values. These may be overrided with the config file
		properties.put("GRAPH_WME_NUM", GRAPH_WME_NUM);
		properties.put("TEMP_DIR", System.getProperty("java.io.tmpdir"));
		properties.put("SOAR_PATH", SOAR_PATH);
		properties.put("SENTENCE_FILE", SENTENCE_FILE);
		properties.put("PERL_PATH", PERL_PATH);
		properties.put("WN_DICT_DIR", WN_DICT_DIR);
		properties.put("LCS_FILE", LCS_FILE);
		properties.put("AGENT_NAME", AGENT_NAME);
		properties.put("DISPLAY_GRAPHS", "true");
		properties.put("LGP_DATA_PATH", LGP_DATA_PATH);
		
		//read the properties from the config file
		//overwrite the default properties
		loadConstants();
		
	}
	/**
	 * Loads constant value from the default location (constants.txt in the project root folder)
	 */
	public static void loadConstants(){
		loadConstants(constantsPath);
	}
	/**
	 * 
	 * @param path Location of the file containing the constant values
	 */
	public static void loadConstants(String path) {
		Scanner sc = FileIO.fileScanner(path);
		String[] args;
		String temp;
		while(sc.hasNextLine()){
			temp = sc.nextLine();
			if(temp.startsWith("#") || temp.startsWith("//") || !temp.contains("="))
				continue;
			args = temp.split("=");
			properties.put(args[0], args[1]);
		}
		//some properties depend on others; reset everything that depends on SOAR_HOME
		properties.put("SOAR_BIN", properties.get("SOAR_HOME") + "/bin");
		//necessary because a user might have an older version of the debugger, or a different SOAR_HOME
		if(!properties.containsKey("SOAR_DEBUGGER"))
			properties.put("SOAR_DEBUGGER", properties.get("SOAR_HOME") + "/share/java/soar-debugger-9.3.1.jar");
		//create the temp directory if it doesn't exist
		new File(getProperty("TEMP_DIR")).mkdirs();
	}
	/**
	 * 
	 * @param key Name of the property
	 * @return Value of the property
	 */
	public static String getProperty(String key){
		String p = properties.get(key);
		if(p == null)
			return "null";
		return properties.get(key);
	}
	/**
	 * 
	 * @param key Name of the property
	 * @param Value of the property
	 */
	public static void setProperty(String key, String value){
		properties.put(key, value);
	}
	
	/**
	 * 
	 * @param key Name of the property
	 * @return true if the property value is equal to 'true' (ignoring case), otherwise false
	 */
	public static boolean isTrue(String key){
		return properties.get(key).toLowerCase().equals("true");
	}
}
