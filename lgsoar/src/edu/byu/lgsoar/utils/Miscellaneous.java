package edu.byu.lgsoar.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Pattern;

import sml.Kernel;

public class Miscellaneous {

	/**
	 * 
	 * @param doubles to average
	 * @return average value of the Collection of doubles
	 */
	public static double average (Collection<Double> doubles){
		double total = 0.0;
		for(double d : doubles)
			total += d;
		return total/doubles.size();
	}

	/**
	 * 
	 * @param fileName to read into the queue
	 * @return a queue of words from the file, splitting on space.
	 */
	public static Queue<String> readFileToQueue(String fileName){
		Queue<String> data = new LinkedList<String>();
		try{
			Scanner sc = new Scanner(new File(fileName));
			while(sc.hasNext()){
				String s = sc.next();
//				System.out.println("Read from file: " + s);
				data.offer(s);
			}
		} catch(FileNotFoundException e){System.out.println("couldn't open sentence file:\n"+e);}
		return data;
	}

	/**
	 * 
	 * @param queue to turn into a string
	 * @return String filled by the words in the queue, formatted like a sentence (spaces between
	 * words, period at the end).
	 */
	public static String toString(@SuppressWarnings("rawtypes") Queue queue){
		//System.out.println("Stringing queue: " + queue.toString());
		Object[] sa = queue.toArray();
		
		StringBuffer buf = new StringBuffer("\"");
		for(int i = 0; i < sa.length-2; i++){
			buf.append(sa[i].toString());
			buf.append(" ");
		}
		buf.append(sa[sa.length-2]);
		buf.append(".\"");
		
		return buf.toString();
	}

	/**
	 * 
	 * @return a string like '28906@localhost'
	 */
	public static String pid(){
		return ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
	}
	
	/**
	 * 
	 * @param thiswme name of the wme
	 * @param agentName agent's name
	 * @param kernel 
	 * @return the last added wme
	 */
	public static String getLastWME(String thiswme, String agentName, Kernel kernel) {
		// do a WME print
		String wmedump = kernel.ExecuteCommandLine("p -d 0 -i " + thiswme, agentName);
		String[] temp = wmedump.split("\\n");
	    Pattern p2 = Pattern.compile("[\\s]+");
	    //sort the elements by timestamp
		TreeMap<Integer,String> wmeTimeStamps = new TreeMap<Integer,String>();
		for(int i = 0; i < temp.length; i++) {
			// parse out the timestamps and wmeID's
			String tempStr = temp[i];
	        String[] tempStrs = p2.split(tempStr);
	        String timeStamp = tempStrs[0].substring(1,tempStrs[0].length() - 1);
	        int timeStampInt = Integer.parseInt(timeStamp); 
	        String wmeId = tempStrs[3].substring(0,tempStrs[3].length() - 1);

	        wmeTimeStamps.put(timeStampInt, wmeId);
		}
		// get the value of the max timestamp (i.e. the latest one, just created)
		String latestWME = wmeTimeStamps.get(wmeTimeStamps.lastKey());
		return latestWME;
	}
}
