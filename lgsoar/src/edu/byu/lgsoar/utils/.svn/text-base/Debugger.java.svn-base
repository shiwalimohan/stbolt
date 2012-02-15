package edu.byu.lgsoar.utils;

import edu.umich.soar.debugger.SWTApplication;

/**
 * Used for creating graphical debuggers.
 * @author Nathan Glenn
 *
 */
public class Debugger{
	/**
	 * Creates a graphical debugger with specified arguments.
	 * @param args to be passed to the debugger
	 */
  public Debugger(String[] args)
  {
    try
    {
      SWTApplication swtApp = new SWTApplication();

      swtApp.startApp(args);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  /**
   * Create a debugger and connect to an existing kernel on the default port.
   */
  public static void remoteDebug()
  {
    new Debugger(new String[]{"-remote"});
  }
}