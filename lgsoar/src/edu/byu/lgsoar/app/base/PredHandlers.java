package edu.byu.lgsoar.app.base;

import sml.Kernel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * 
 * For now just collect and output raw predicates.
 * @author Deryle Lonsdale
 *
 */
public class PredHandlers {
	
	private static String predList = "";
	private static HashMap<String,String> hm=new HashMap<String,String>();

	public static Kernel.RhsFunctionInterface predClose(){
		return new Kernel.RhsFunctionInterface() {
			@Override
			public String rhsFunctionHandler(int eventID, Object data,
					String agentName, String functionName, String argument) {
				
				predList = predList + argument;

				return "";
			}
		};
	}
	
	public static Kernel.RhsFunctionInterface predInit(){
		return new Kernel.RhsFunctionInterface() {
			@Override
			public String rhsFunctionHandler(int eventID, Object data,
					String agentName, String functionName, String argument) {
				
				predList = "";
//				System.out.println("Resetting predList");
				
//				HashMap<String,String> hm=new HashMap<String,String>();
				hm.clear();
				
				return "";
			}
		};
	}
	
	

	public static Kernel.RhsFunctionInterface collectPred(){
		return new Kernel.RhsFunctionInterface() {
			@Override
			public String rhsFunctionHandler(int eventID, Object data,
					String agentName, String functionName, String argument) {
				
		        hm.put(argument, "t");
		
				predList = predList + argument;
				
				//System.out.println("added pred: " + argument + " pl " + predList);

				return "";
			}
		};
	}
	
	public static Kernel.RhsFunctionInterface outputPreds(){
		return new Kernel.RhsFunctionInterface() {
			@Override
			public String rhsFunctionHandler(int eventID, Object data,
					String agentName, String functionName, String argument) {
				
				Kernel kernel = ((SoarApplication) data).getKernel();
				
				String ioWME = argument;
				
				
		        Set s=hm.entrySet();
		        Iterator it=s.iterator();

		        while(it.hasNext())
		        {
		            Map.Entry m =(Map.Entry)it.next();
		            String key= (String) m.getKey();

		            String cmd = "add-wme " + ioWME + " predicate |" + key + "|";
		            kernel.ExecuteCommandLine(cmd, agentName);
		            //System.out.println("called " + cmd);
		            System.out.println(key);
		        }
						
				return "";
			}
		};
	}
	
	
}
