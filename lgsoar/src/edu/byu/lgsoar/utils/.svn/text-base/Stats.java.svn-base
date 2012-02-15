package edu.byu.lgsoar.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import sml.Agent;

/**
 * This class holds the statistics from a Soar run. The constructors take either an 
 * agent or a string. If an agent is passed, the "stats" command is run on that agent
 * and the output is parsed. If the string from a previous "stats" command is passed,
 * that string will be parsed. The class then provides methods to access the individual
 * statistics.
 */
public class Stats {
	private final String statistics;
	private final String version;
	private final String compName;
	private final SimpleDateFormat dateFormat= new SimpleDateFormat("EEE MMM dd hh:mm:ss yyyy");
	private Date date;
	private int productions;
	private int defaultProductions;
	private int userProductions;
	private int chunks;
	private int justifications;
	private Map<String, Double> kernelPhaseTimes = new HashMap<String, Double>();
	private Map<String, Double> inputFnPhaseTimes = new HashMap<String, Double>();
	private Map<String, Double> outputFnPhaseTimes = new HashMap<String, Double>();
	private Map<String, Double> callbackPhaseTimes = new HashMap<String, Double>();
	private Map<String, Double> totalPhaseTimes = new HashMap<String, Double>();
	private double kernelCPUTime;
	private double totalCPUTime;
	private int decisions;
	private double msecPerDecision;
	private int elaborationCycles;
	private double ecPerDc;
	private double msecPerEc;
	private int innerEc;
	private double msecPerPe;
	private double pePerDc;
	private int pElabCycles;
	private double msecPerPf;
	private double pfPerEc;
	private int productionFirings;
	private int wmeAdditions;
	private int wmeChanges;
	private int wmeRemovals;
	private int currentWmSize;
	private double meanWmSize;
	private int maxWmSize;

	/**
	 * Retrieves and parses the statistics for the input agent
	 * @param agent Agent to get statistics from
	 */
	public Stats(Agent agent){
		this(agent.ExecuteCommandLine("stats"));
	}

	/**
	 * Parses the input statistics for quick retrieval
	 * @param stats Printout of "stats" Soar command
	 */
	public Stats(String stats) {
		statistics = stats;
		String[] lines = stats.split("\n");
		
		version = lines[0].substring(0, lines[0].indexOf(" on"));
		compName = lines[0].substring(lines[0].indexOf(" on ")+4,lines[0].indexOf(" at "));
		String dateString = lines[0].substring(lines[0].indexOf(" at ")+4,lines[0].length());
		try {
			date = dateFormat.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
			date = null;
		}
		productions = Integer.parseInt(lines[2].substring(0,lines[2].indexOf(" pro")));
		defaultProductions = Integer.parseInt(lines[2].substring(lines[2].indexOf("(")+1,lines[2].indexOf(" d")));
		//t, 71 user, 0 chunks)
		userProductions = Integer.parseInt(lines[2].substring(lines[2].indexOf("t,")+3,lines[2].indexOf(" u")));
		chunks = Integer.parseInt(lines[2].substring(lines[2].indexOf("r,")+3,lines[2].indexOf(" c")));
		justifications = Integer.parseInt(lines[3].substring(lines[3].indexOf("+")+2,lines[3].indexOf(" j")));

		kernelPhaseTimes.put("Input", Double.parseDouble(lines[7].substring(13,18)));
		kernelPhaseTimes.put("Propose", Double.parseDouble(lines[7].substring(22,27)));
		kernelPhaseTimes.put("Decide", Double.parseDouble(lines[7].substring(31,36)));
		kernelPhaseTimes.put("Apply", Double.parseDouble(lines[7].substring(40,45)));
		kernelPhaseTimes.put("Output", Double.parseDouble(lines[7].substring(49,54)));
		kernelPhaseTimes.put("Total", Double.parseDouble(lines[7].substring(63,68)));

		inputFnPhaseTimes.put("Input", Double.parseDouble(lines[9].substring(13,18)));
		inputFnPhaseTimes.put("Total", Double.parseDouble(lines[9].substring(63,68)));

		outputFnPhaseTimes.put("Output", Double.parseDouble(lines[11].substring(49,54)));
		outputFnPhaseTimes.put("Total", Double.parseDouble(lines[11].substring(63,68)));

		callbackPhaseTimes.put("Input", Double.parseDouble(lines[13].substring(13,18)));
		callbackPhaseTimes.put("Propose", Double.parseDouble(lines[13].substring(22,27)));
		callbackPhaseTimes.put("Decide", Double.parseDouble(lines[13].substring(31,36)));
		callbackPhaseTimes.put("Apply", Double.parseDouble(lines[13].substring(40,45)));
		callbackPhaseTimes.put("Output", Double.parseDouble(lines[13].substring(49,54)));
		callbackPhaseTimes.put("Total", Double.parseDouble(lines[13].substring(63,68)));

		totalPhaseTimes.put("Input", Double.parseDouble(lines[16].substring(13,18)));
		totalPhaseTimes.put("Propose", Double.parseDouble(lines[16].substring(22,27)));
		totalPhaseTimes.put("Decide", Double.parseDouble(lines[16].substring(31,36)));
		totalPhaseTimes.put("Apply", Double.parseDouble(lines[16].substring(40,45)));
		totalPhaseTimes.put("Output", Double.parseDouble(lines[16].substring(49,54)));
		totalPhaseTimes.put("Total", Double.parseDouble(lines[16].substring(63,68)));
		
		kernelCPUTime = Double.parseDouble(lines[19].substring(24,29));
		totalCPUTime = Double.parseDouble(lines[20].substring(24,29));
		
		decisions = Integer.parseInt(lines[22].substring(0,lines[22].indexOf(" d")));
		msecPerDecision = Double.parseDouble(lines[22].substring(lines[22].indexOf("(")+1,lines[22].indexOf(" m")));
		
		elaborationCycles = Integer.parseInt(lines[23].substring(0,lines[23].indexOf(" ")));
		ecPerDc = Double.parseDouble(lines[23].substring(lines[23].indexOf("(")+1,lines[23].indexOf(" ec")));
		msecPerEc = Double.parseDouble(lines[23].substring(lines[23].indexOf(", ")+2,lines[23].indexOf(" m")));
		
		innerEc = Integer.parseInt(lines[24].substring(0,lines[24].indexOf(" ")));
		
		pElabCycles = Integer.parseInt(lines[25].substring(0,lines[25].indexOf(" ")));
		pePerDc = Double.parseDouble(lines[25].substring(lines[25].indexOf("(")+1,lines[25].indexOf(" pe")));
		msecPerPe = Double.parseDouble(lines[25].substring(lines[25].indexOf(", ")+2,lines[25].indexOf(" m")));
		
		productionFirings = Integer.parseInt(lines[26].substring(0,lines[26].indexOf(" ")));
		pfPerEc = Double.parseDouble(lines[26].substring(lines[26].indexOf("(")+1,lines[26].indexOf(" pf")));
		msecPerPf = Double.parseDouble(lines[26].substring(lines[26].indexOf(", ")+2,lines[26].indexOf(" m")));
		
		wmeChanges = Integer.parseInt(lines[27].substring(0,lines[27].indexOf(" ")));
		wmeAdditions = Integer.parseInt(lines[27].substring(lines[27].indexOf("(")+1,lines[27].indexOf(" a")));
		wmeRemovals = Integer.parseInt(lines[27].substring(lines[27].indexOf(", ")+2,lines[27].indexOf(" r")));
		
		currentWmSize = Integer.parseInt(lines[28].substring(lines[28].indexOf(": ")+2, lines[28].indexOf(" c")));
		meanWmSize = Double.parseDouble(lines[28].substring(lines[28].indexOf(", ")+2, lines[28].indexOf(" m")));
		maxWmSize = Integer.parseInt(lines[28].substring(lines[28].indexOf("n,")+3, lines[28].indexOf(" ma")));
	}
	/**
	 * @return String output of "stats" Soar command
	 */
	public String getStatistics(){return statistics;}
	/**
	 * @return version number
	 */
	public String getVersion() {return version;}
	/**
	 * @return name of computer
	 */
	public String getCompName() {return compName;}
	/**
	 * @return date
	 */
	public Date getDate() {return date;}
	/**
	 * @return number of productions
	 */
	public int getProductions() {return productions;}
	/**
	 * @return number of default productions
	 */
	public int getDefaultProductions() {return defaultProductions;}
	/**
	 * @return number of user productions
	 */
	public int getUserProductions() {return userProductions;}
	/**
	 * @return number of user justifications
	 */
	public int getUserJustifications() {return justifications;}
	/**
	 * @return number of chunks
	 */
	public int getChunks(){return chunks;}
	/**
	 * @return kernel CPU time
	 */
	public double getKernelCPUTime(){return kernelCPUTime;}
	/**
	 * @return total CPU time
	 */
	public double getTotalCPUTime(){return totalCPUTime;}
	/**
	 * @return number of decisions
	 */
	public int getDecisions(){return decisions;}
	/**
	 * @return milliseconds per decision
	 */
	public double getMsecPerDecision(){return msecPerDecision;}
	/**
	 * @return number of elaboration cycles
	 */
	public int getElaborationCycles(){return elaborationCycles;}
	/**
	 * @return elaboration cycles per decision cycles
	 */
	public double getEcPerDc(){return ecPerDc;}
	/**
	 * @return milliseconds per elaboration cycle
	 */
	public double getMsecPerEc(){return msecPerEc;}
	/**
	 * @return inner elaboration cycles
	 */
	public int innerEc(){return innerEc;}
	/**
	 * @return p-elaboration cycles
	 */
	public double getPElabCycles(){return pElabCycles;}
	/**
	 * @return milliseconds per p-elaboration cycle
	 */
	public double getMsecPerPe(){return msecPerPe;}
	/**
	 * @return p-elaboration cycles per decision cycles
	 */
	public double getPePerDc(){return pePerDc;}
	/**
	 * @return production firings
	 */
	public double getProductionFirings(){return productionFirings;}
	/**
	 * @return production firings per elaboration cycle
	 */
	public double getPfPerEc(){return pfPerEc;}
	/**
	 * @return milliseconds per production firing
	 */
	public double getMsecPerPf(){return msecPerPf;}
	/**
	 * @return number of working memory addictions
	 */
	public double getWmeAdditions(){return wmeAdditions;}
	/**
	 * @return number of working memory changes
	 */
	public double getWmeChanges(){return wmeChanges;}
	/**
	 * @return number of working memory removals
	 */
	public double getWmeRemovals(){return wmeRemovals;}
	/**
	 * @return current working memory size
	 */
	public double getCurrentWmSize(){return currentWmSize;}
	/**
	 * @return mean working memory size
	 */
	public double getMeanWmSize(){return meanWmSize;}
	/**
	 * @return maximum working memory size
	 */
	public double getMaxWmSize(){return maxWmSize;}
	
	//access the following methods values from these keys:
	//Input, Propose, Decide, Apply, Output, Total
	
	public Map<String, Double> getKernelPhaseTimes(){return kernelPhaseTimes;}
	public Map<String, Double> getInputFnPhaseTimes(){return inputFnPhaseTimes;}
	public Map<String, Double> getOutputFnPhaseTimes(){return outputFnPhaseTimes;}
	public Map<String, Double> getCallbackPhaseTimes(){return callbackPhaseTimes;}
	public Map<String, Double> getTotalPhaseTimes(){return totalPhaseTimes;}
}
