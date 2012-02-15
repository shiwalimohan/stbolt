package edu.byu.lgsoar.graph;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import sml.Kernel;
import sml.Kernel.RhsFunctionInterface;
//import edu.byu.lgsoar.app.base.BatchRunner;
import edu.byu.lgsoar.app.base.SoarApplication;
import edu.byu.lgsoar.utils.Constants;
import edu.byu.lgsoar.utils.Miscellaneous;



/**
 * Class for creating and managing graphers and also creating .gif files from .dot files using 
 * GraphViz. You can create a {@link Grapher}, which includes a display window, by calling
 * {@link #addGrapher(String key) addGrapher(String key)}. Subsequently, other methods may perform operations 
 * with that Grapher object by referring to it with that key. For example, you might create a syntax 
 * display window by calling <code>GraphUtils.addGrapher("syntax")</code>, and then display
 * a syntax graph in that window by calling <code>GraphUtils.display("syntax","c:/temp/syngraph.gif")</code>.<p/>
 * 
 * @author Nathan Glenn
 *
 */
public class GraphUtils{

	private static GraphViz gv = new GraphViz();
	private static Map<String,Grapher> graphers = new HashMap<String,Grapher>();
	
	/**
	 * Creates a {@link Grapher} with a given string key and adds it to an internal index.
	 * After calling this, other functions can be called using this key to refer to the same
	 * Grapher.
	 * @param key String to refer to the new Grapher in subsequent method calls
	 */
	public static void addGrapher(String key){
		if(!graphers.containsKey(key))
			//			destroyGrapher(key);
			graphers.put(key, new Grapher("LG-Soar " + key + " graph"));
	}
	
	/**
	 * Creates graph from dot file and displays it in {@link Grapher} referred to by key.
	 * @param key String referring to specific {@link Grapher} object.
	 * @param graphName Path of file containing graph information
	 */
	public static void graph(String key, String graphName){
		String dotFile = graphName;
		String displayFile = graphName.replace(".dot",".gif");
		createGraph(dotFile, displayFile);
		graphers.get(key).display(displayFile);
	}

	/**
	 * Creates visual graph from dot file.
	 * @param dotFileName Name of file containing dot syntax of graph to be created.
	 * @param outFileName Name to give to output graph file.
	 */
    public static void createGraph(String dotFileName, String outFileName){
		   gv.readSource(dotFileName);
		   File out = new File(outFileName);
		   gv.writeGraphToFile(gv.getGraph(gv.getDotSource()), out);    	
    }
	
    /**
     * Calls {@link Grapher#display(String fileName) display} from the {@link Grapher} object
     * referred to by key
     * @param key String referring to specific {@link Grapher} object
     * @param fileName Name of file containing dot graph
     */
	public static void display(String key, String fileName){
		graphers.get(key).display(fileName);
	}

	/**
	 * Destroys {@link Grapher} specified by key and removes it from the index.
	 * 
	 * @param key String referring to specific {@link Grapher} object
	 * @return False if no {@link Grapher} object with specified key exists, otherwise true.
	 */
	public static boolean destroyGrapher(String key){
		if(graphers.get(key) == null)
			return false;
		graphers.get(key).dispose();
		graphers.remove(key);
		return true;
	}
	
	/**
	 * 
	 * Dumps the working memory structure and graphs it immediately.
	 * @param sa SoarApplication to use in Graphing
	 * @param type Name of graph to use ("syntax", "semantics", etc.)
	 */
	public static void forceSoarGraph(SoarApplication sa, String type){
		Kernel kernel = sa.getKernel();
		String agentName = sa.getAgentName();
		kernel.ExecuteCommandLine("command-to-file " + 
				Miscellaneous.pid() + " print --depth 10 s1", agentName);
		perlGViz().rhsFunctionHandler(-1, sa, null, null, null);
	}
	
	////Perl Soar Handlers////
	//keeps track of the number of graphs made so far
	private static int graphCounter;
	private static String perlcmd = Constants.getProperty("PERL_APP")+ " ";
	private static String perlscript = Constants.getProperty("PERL_PATH") + "/gvizify";
	private static String wmefile = Constants.getProperty("TEMP_DIR") + Miscellaneous.pid();
	
	/**
	 * Add graphing capbility to a Soar agent by adding the return value of this method to its
	 * RHS events. Use {@link SoarApplication#addRHShandler(String funcName, RhsFunctionInterface r)}.
	 * @return an RHS method which calls perl and graphviz to create graphs of previously dumped
	 * wme files. The rhs function expects the language code to be an argument.
	 */
	public static RhsFunctionInterface perlGViz() {
		return new Kernel.RhsFunctionInterface() {
			@Override
			public String rhsFunctionHandler(int eventID, Object data,
					String agentName, String functionName, String language) {
				if(Boolean.parseBoolean(Constants.getProperty("DEBUG_GRAPHING"))){
					if(Boolean.parseBoolean(Constants.getProperty("GRAPH_SYNTAX")))
						debugGraph((SoarApplication)data, "syntax",language);
					if(Boolean.parseBoolean(Constants.getProperty("GRAPH_SEMANTICS")))
						debugGraph((SoarApplication)data, "semantics",language);
					if(Boolean.parseBoolean(Constants.getProperty("GRAPH_ARSET")))
						debugGraph((SoarApplication)data, "arset",language);
					if(Boolean.parseBoolean(Constants.getProperty("GRAPH_DRS")))
						debugGraph((SoarApplication)data, "drs",language);
					return null;
				}
				if(Boolean.parseBoolean(Constants.getProperty("GRAPH_SYNTAX")))
					normalGraph("syntax",language);
				if(Boolean.parseBoolean(Constants.getProperty("GRAPH_SEMANTICS")))
					normalGraph("semantics",language);
				if(Boolean.parseBoolean(Constants.getProperty("GRAPH_ARSET")))
					normalGraph("arset",language);
				if(Boolean.parseBoolean(Constants.getProperty("GRAPH_DRS")))
					normalGraph("drs",language);
				return null;
			}
		};
	}
	
	private static String dir = Constants.getProperty("TEMP_DIR") + Miscellaneous.pid() + "DEBUG/";
	/**
	 * Calls creates a new graph through perl and graphviz, then displays it using 
	 * {@link #display(String key, String fileName)}. Assumes that a Soar WME dump 
	 * [<code>(cmd command-to-file <pidno> print --depth 10 s1)</code>] is located in TEMP_DIR 
	 * (from {@link Constants}/process id/"DEBUG"/<{@link BatchRunner#getNumProcessed()}>/key.<p/>
	 * Creates graphs in a way more suited to debugging; the output files are numbered,
	 * so they are not overridden by subsequent graphing, and they are put
	 * in a more organized directory structure. The outputs are also labeled with key. the directory
	 * can be examined after application execution to see all of the graphs generated at intermediate
	 * steps.
	 * 
	 * @param sa BatchRunner object from which graph information can be taken.
	 * @param key Identifier for specific {@link Grapher} object.
	 */
	public static void debugGraph(SoarApplication sa, String key, String argument){
		String tempDir;
//		if(sa instanceof BatchRunner){
//			tempDir = dir + ((BatchRunner)sa).getNumProcessed() + "/" + key + "/";
//			new File(tempDir).mkdirs();//creates the directory, if it doesn't already exist
//			tempDir = tempDir+ graphCounter++;
//		}
//		else{
			tempDir = dir + key + "/";
			new File(tempDir).mkdirs();//creates the directory, if it doesn't already exist
			tempDir = tempDir +	graphCounter++;
			
//		}			
		try {
			Scanner sc = null;
			Process r = Runtime.getRuntime().exec(perlcmd + perlscript + key + ".pl " + wmefile + 
					" " + Constants.getProperty("GRAPH_WME_NUM") + " " +
					Constants.getProperty("LANGUAGE") + " " + tempDir);
//			System.out.println("Calling perl: " + perlcmd + perlscript + key + ".pl " + wmefile + 
//					" " + Constants.getProperty("GRAPH_WME_NUM") + " " +
//					Constants.getProperty("LANGUAGE") + " " + tempDir);
			sc = new Scanner(r.getErrorStream());
			while(sc.hasNextLine())
				System.err.print(sc.nextLine());
			r.waitFor();//make sure image is finished writing before trying to display it
			if(Boolean.parseBoolean(Constants.getProperty("DISPLAY_GRAPHS")))
				GraphUtils.display(key,tempDir + ".jpg");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * Calls creates a new graph through perl and graphviz, then displays it using 
	 * {@link #display(String key, String fileName)}. Assumes that a Soar WME dump 
	 * [<code>(cmd command-to-file <pidno> print --depth 10 s1)</code>] is located in TEMP_DIR
	 * (from {@link Constants}/process id/key.
	 * @param key String referring to specific {@link Grapher} object
	 */
	public static void normalGraph(String key, String argument){
		try {
			Process r = Runtime.getRuntime().exec(perlcmd + perlscript + key + ".pl "  + wmefile + 
					" " + Constants.getProperty("GRAPH_WME_NUM") + " " +
					Constants.getProperty("LANGUAGE") + " " + wmefile);// +  " " + currentSentence);
			r.waitFor();//make sure image is finished writing before trying to display it
			if(Boolean.parseBoolean(Constants.getProperty("DISPLAY_GRAPHS")))
				GraphUtils.display(key,wmefile + ".jpg");
		} catch (IOException e) {
		e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}