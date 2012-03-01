package edu.byu.lgsoar.en.app;

//import org.eclipse.swt.widgets.Display;

import edu.byu.lgsoar.app.base.GenericHandlers;
import edu.byu.lgsoar.app.base.PredHandlers;
import edu.byu.lgsoar.app.base.SoarApplication;
import edu.byu.lgsoar.utils.Constants;

public class SingleParse extends SoarApplication{

	public void registerEvents(){
		//set sentence by using setCurrentSentence(String)
//		addRHShandler("initwordstack", EnHandlers.initWordStackFromClass());
		//set sentence by reading from sentence.txt
//		addRHShandler("initwordstack",EnHandlers.initWordStackFromFile());
		addRHShandler("readsentence",EnHandlers.getStringFromFile());
		addRHShandler("popword", EnHandlers.popword());
		addRHShandler("getpid", GenericHandlers.getPid());
		addRHShandler("gettempfile", GenericHandlers.getTempFile());
		addRHShandler("getlgparse", EnHandlers.callLGParser());
		
		addRHShandler("predclose", PredHandlers.predClose());
		addRHShandler("predinit", PredHandlers.predInit());
		addRHShandler("collect_pred", PredHandlers.collectPred());
		addRHShandler("output_preds", PredHandlers.outputPreds());
		
	}
	
	public static void main(String[] args) throws InterruptedException {
		SingleParse sp = new SingleParse();
		sp.setAgentName(Constants.getProperty("AGENT_NAME"));
		if (args.length >= 2) {
			boolean debug = false;
			if (args.length >= 3 && args[2].equals("--debug")) {
				debug = true;
			}
			sp.runHeadlessWithFile(args[1], debug);
		}		
		else {
			sp.init();
			sp.registerEvents();
			sp.setGraphing(true);
			sp.debugger();
			sp.shutdown();
		}

	}

}
