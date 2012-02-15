package edu.byu.lgsoar.test;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;


import org.junit.BeforeClass;
import org.junit.Test;

import edu.byu.lgsoar.utils.Constants;


public class ConstantsTest {
	
	@BeforeClass
	public static void testLoadLibrary(){
		try{
			System.loadLibrary("Java_sml_ClientInterface");
		}catch(UnsatisfiedLinkError e){
			fail("Could not load Java_sml_ClientInterface.dll; make sure that java.library.path (or your path " +
					"environment variable) contains the directory containing it, and make sure you have not " +
					"set the native library locations of any of the jar files in the classpath.");
		}
	}
	
	@Test
	public void testSoarLocation(){
		//look for $SOAR_BIN/Java_sml_ClientInterface.dll
		try{
			new File(Constants.getProperty("SOAR_BIN")+"/Java_sml_ClientInterface.dll");
		}
		catch(Exception e){
			fail("Constants.SOAR_HOME is set incorrectly.");
		}
	}	
	@Test
	public void testPerl(){
		//test for perl path
		try {
			Process p;
			p = Runtime.getRuntime().exec(Constants.getProperty("PERL_APP")+" -v");
			Scanner sc;
			sc = new Scanner(p.getInputStream());
			boolean foundPerl = false;
			//
			while(sc.hasNextLine())
				if(sc.nextLine().contains("Copyright")){//perl version information will print the copyright information
					foundPerl = true;
			}
			if(!foundPerl)
				fail("Could not execute perl from '"+Constants.getProperty("PERL_APP")+"';Constants.PERL_APP is incorrect.");
			
			//test for GraphViz installation
			p = Runtime.getRuntime().exec("perl -e \"use GraphViz; print \\\"ok\\\"\"");
			sc = new Scanner(p.getInputStream());
			//System.out.println(sc.nextLine());
			if(!sc.hasNextLine() || !sc.nextLine().contains("ok"))
				fail("You haven't installed the perl GraphViz module. Try typing 'cpan', ENTER, 'install GraphViz'.");
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testGraphViz(){
		Scanner sc = null;
		try {
//			System.out.println("executing " + Constants.GV_DIR+"dot.exe -V");
			Process p = Runtime.getRuntime().exec(Constants.getProperty("GV_DIR") + "dot.exe -V");
			sc = new Scanner(p.getErrorStream());//graphviz prints to stderr
			if(!sc.hasNextLine() || !sc.nextLine().contains("graphviz version"))
				fail("Could not find \"" + Constants.getProperty("GV_DIR") + "dot.exe\". Constants.GV_DIR is set incorrectly.");
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testTempDirectory(){
		if(!new File(Constants.getProperty("TEMP_DIR")).exists())
			fail("The directory \"" + Constants.getProperty("TEMP_DIR") + "\" does not exist. " +
					"Make sure that Constants.TEMP_DIR is a valid path name.");
	}
}
