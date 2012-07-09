package edu.umich.sbolt;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import sml.Agent;
import sml.Identifier;
import sml.WMElement;
import sml.smlRunEventId;
import sml.Agent.RunEventInterface;
import abolt.lcmtypes.robot_command_t;
import april.util.TimeUtil;

import com.soartech.bolt.BOLTLGSupport;

import edu.umich.sbolt.world.SVSConnector;
import edu.umich.sbolt.world.World;

public class ChatFrame extends JFrame implements RunEventInterface
{
	// Singleton instance to access the ChatFrame from other places
	public static ChatFrame Singleton(){
		return instance;
	}
	private static ChatFrame instance = null;
	
	// GUI COMPONENTS

    private JTextArea chatArea;
    // The box which messages are displayed to

    private JTextField chatField;
    // The text field the user can type messages to the agent in
    
    private JButton sendButton;
    // The button used to send a message to the soar agent
    
    private JButton startStopButton;
    // The button that you can use to start and stop the agent (toggles between them)
    
    private InteractionStack stack;
    // Used to display a visual representation of the current state of the interaction stack
    
    
    // CHAT MESSAGES AND HISTORY

    private List<String> chatMessages = new ArrayList<String>();
    // A list of all the messages currently in the chatArea
    
    private ArrayList<String> history = new ArrayList<String>();
    // A list of all messages typed into the chatField
    
    private int historyIndex = 0;
    // The current index into the history
    
    private BOLTLGSupport lgSupport;
    // Used to send messages to Soar through LG-Soar
    
    
    // AGENT STATUS AND CONTROL
    
    private boolean ready = false;
    // True if the agent is ready for a new message from the user
    
    private boolean isAgentRunning = false;
    // True if the agent is currently running
    
    private boolean stopAgent = false;
    // True if the user has indicated he wants to stop the agent
    
    private boolean clearAgent = false;
    // True if the user has indicated he wants to clear the text information

    public ChatFrame(BOLTLGSupport lg, Agent agent) {
        super("SBolt");
        instance = this;
        lgSupport = lg;

        agent.RegisterForRunEvent(smlRunEventId.smlEVENT_AFTER_OUTPUT_PHASE, this, null);
 

        chatArea = new JTextArea();
        chatArea.setFont(new Font("Serif",Font.PLAIN,18));
        JScrollPane pane = new JScrollPane(chatArea);
        
        chatField = new JTextField();
        chatField.setFont(new Font("Serif",Font.PLAIN,18));
        chatField.addKeyListener(new KeyAdapter(){
			public void keyPressed(KeyEvent arg0) {
				if(arg0.getKeyCode() == KeyEvent.VK_UP) {
					upPressed();
				} else if(arg0.getKeyCode() == KeyEvent.VK_DOWN){
					downPressed();
				}
			}
        });
        
        
        sendButton = new JButton("Send Message");
        sendButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
            	sendButtonClicked();
            }
        });

        JSplitPane pane2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                chatField, sendButton);
        JSplitPane pane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pane,
                pane2);

        pane1.setDividerLocation(325);
        pane2.setDividerLocation(600);

        this.add(pane1);
        this.setSize(800, 450);
        this.getRootPane().setDefaultButton(sendButton);
        
        JMenuBar menuBar = new JMenuBar();     


        startStopButton = new JButton("START");
        startStopButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
		    	if(isAgentRunning){
		    		stopAgent = true;
		    	} else {
		    		runAgent();
		    	}
			}
        });
        menuBar.add(startStopButton);
        
        JButton clearButton  = new JButton("Clear Text");
        clearButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				if(!isAgentRunning){
					clear();
				} else {
        			clearAgent = true;
				}
			}
        });
        menuBar.add(clearButton);
        
        JButton armResetButton  = new JButton("Reset Arm");
        armResetButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				robot_command_t command = new robot_command_t();
				command.utime = TimeUtil.utime();
				command.action = "RESET";
				command.dest = new double[6];
				SBolt.broadcastRobotCommand(command);
			}
        });
        menuBar.add(armResetButton);

        createAgentMenu(menuBar);
        
       
        setJMenuBar(menuBar);
        
        addWindowListener(new WindowAdapter() {
        	public void windowClosing(WindowEvent w) {
        		exit();
        	}
     	});
        
        setReady(false);
    }
    
    /*** 
     * Creates the Agent menu in the ChatFrame, which consists of 
     * functions to backup/restore/reset the agent
     */
    private void createAgentMenu(JMenuBar menuBar){
    	JMenu agentMenu = new JMenu("Agent");
        
    	// Full Reset: Completely clears all memories and re-sources the agent
        JMenuItem resetButton = new JMenuItem("Full Reset");
        resetButton.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		if(!isAgentRunning){
        			clear();
        			SBolt.Singleton().reloadAgent(true);
        	    	SVSConnector.Singleton().reset();
        		} else {
        			JOptionPane.showMessageDialog(null, "The agent must be stopped first");
        		}
        	}
        });
        agentMenu.add(resetButton);        
        
        agentMenu.addSeparator();

        // Backup: Creates a backup of all memories and chunks to the default location
        JMenuItem backupButton = new JMenuItem("Backup");
        backupButton.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		if(!isAgentRunning){
        			backup("default");
        		} else {
        			JOptionPane.showMessageDialog(null, "The agent must be stopped first");
        		}
        	}
        });
        agentMenu.add(backupButton);  

        // Restore: Restores all memories and chunks from the default location (last time Backup was pressed)
        JMenuItem restoreButton = new JMenuItem("Restore");
        restoreButton.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		if(!isAgentRunning){
        			restore("default");
        		} else {
        			JOptionPane.showMessageDialog(null, "The agent must be stopped first");
        		}
        	}
        });
        agentMenu.add(restoreButton);  
        
        // Backup To File: Allows the user to specify the name to use when backing up
        JMenuItem backupToFileButton = new JMenuItem("Backup To File");
        backupToFileButton.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		if(!isAgentRunning){
            		String name = JOptionPane.showInputDialog(null, 
                			  "Enter the session name to backup",
                			  "Backup To File",
                			  JOptionPane.QUESTION_MESSAGE);
        			backup(name);
        		} else {
        			JOptionPane.showMessageDialog(null, "The agent must be stopped");
        		}
        	}
        });
        agentMenu.add(backupToFileButton);  
        
        // Restore From File: Allows the user to specify the name to use when restoring
        JMenuItem restoreFromFileButton = new JMenuItem("Restore From File");
        restoreFromFileButton.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		if(!isAgentRunning){
            		String name = JOptionPane.showInputDialog(null, 
              			  "Enter the session name to restore",
              			  "Restore From File",
              			  JOptionPane.QUESTION_MESSAGE);
        			restore(name);
        		} else {
        			JOptionPane.showMessageDialog(null, "The agent must be stopped");
        		}
        	}
        });
        agentMenu.add(restoreFromFileButton);  
        
        agentMenu.addSeparator();

        // Interaction Stack: Shows a represetnation of the current interaction stack
        stack = new InteractionStack();
        JMenuItem stackButton = new JMenuItem("Interaction Stack");
        stackButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				stack.showFrame();
			}
        });
        agentMenu.add(stackButton);
        
        // Restore From File: Allows the user to specify the name to use when restoring
        JMenuItem commandButton = new JMenuItem("Enter SML Command");
        commandButton.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		String name = JOptionPane.showInputDialog(null, 
            			  "Enter the command",
            			  "SML COmmand",
            			  JOptionPane.QUESTION_MESSAGE);
      			System.out.println(SBolt.Singleton().getAgent().ExecuteCommandLine(name));
        	}
        });
        agentMenu.add(commandButton);  

        menuBar.add(agentMenu);
    }
    
    /**
     * Spawns a new thread that invokes a run command on the agent
     */
    private void runAgent(){
    	class AgentThread implements Runnable{
    		public void run(){
    			SBolt.Singleton().getAgent().ExecuteCommandLine("run");
    		}
    	}
    	Thread agentThread = new Thread(new AgentThread());
    	agentThread.start();
    	isAgentRunning = true;
		startStopButton.setText("STOP");
    }
    
    /** 
     * Handles a run event at the end of a decision cycle
     * May stop or clear the agent if such an action is queued
     */
    public void runEventHandler(int eventID, Object data, Agent agent, int phase)
    {
    	if(stopAgent){
    		agent.ExecuteCommandLine("stop");
    		isAgentRunning = false;
    		startStopButton.setText("START");
    		stopAgent = false;
    	}
    	if(clearAgent){
    		clear();
    		clearAgent = false;
    	}
    }

    public void showFrame()
    {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    public void hideFrame()
    {
        this.setVisible(false);
    }
    
    public InteractionStack getStack(){
    	return stack;
    }
    
    public void setReady(boolean isReady){
    	ready = isReady;
    	if(ready){
    		sendButton.setBackground(new Color(150, 255, 150));
    		sendButton.setText("Send Message");
    	} else {
    		sendButton.setBackground(new Color(255, 100, 100));
    		sendButton.setText("Not Ready");
    	}
    }
    
    public void clear(){
    	System.out.println("CLEAR: All messages have been cleared");
    	chatMessages.clear();
    	chatField.setText("");
    	chatArea.setText("");
    	InputLinkHandler.Singleton().clearLGMessages();
    	World.Singleton().destroyMessage();
    }

    public void addMessage(String message)
    {
        chatMessages.add(message);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < chatMessages.size(); ++i)
        {
            sb.append(chatMessages.get(i));
            if (i + 1 < chatMessages.size())
            {
                sb.append('\n');
            }
        }
        chatArea.setText(sb.toString());
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
    
    public void exit(){
    	SBolt.Singleton().getAgent().KillDebugger();
    	// sbolt.getKernel().DestroyAgent(sbolt.getAgent());
    	
    	// SBW removed DestroyAgent call, it hangs in headless mode for some reason
    	// (even when the KillDebugger isn't there)
    	// I don't think there's any consequence to simply exiting instead.
    	
    	System.exit(0);
    }
    
    private void sendButtonClicked(){
    	if(!ready){
    		return;
    	}
    	history.add(chatField.getText());
    	historyIndex = history.size();
        addMessage("Mentor: " + chatField.getText());
        sendSoarMessage(chatField.getText());
        chatField.setText("");
        chatField.requestFocus();
    }
    
    private void upPressed(){
		if(historyIndex > 0){
			historyIndex--;
		}
		if(history.size() > 0){
			chatField.setText(history.get(historyIndex));
		}
    }
    
    private void downPressed(){
		historyIndex++;
		if(historyIndex >= history.size()){
			historyIndex = history.size();
			chatField.setText("");
		} else {
			chatField.setText(history.get(historyIndex));
		}
    }

    private void sendSoarMessage(String message)
    {
    	if (lgSupport == null) {
    		World.Singleton().newMessage(message);
    	} else if(message.length() > 0){
    		if(message.charAt(0) == ':'){
    			World.Singleton().newMessage(message.substring(1));
    		} else {
        		// LGSupport has access to the agent object and handles all WM interaction from here
        		lgSupport.handleInput(message);
    		}
    	}
    }
    
    private void backup(String name){
    	Agent agent = SBolt.Singleton().getAgent();
    	System.out.println("Performing backup: " + name);
    	System.out.println("  epmem: " + agent.ExecuteCommandLine(String.format("epmem --backup backups/%s_epmem.db", name)));
    	System.out.println("  smem: " + agent.ExecuteCommandLine(String.format("smem --backup backups/%s_smem.db", name)));
    	System.out.println("  chunks: " + agent.ExecuteCommandLine(String.format("command-to-file backups/%s_chunks.soar pc -f", name)));
    	System.out.println("Completed Backup");
    }
    
    private void restore(String name){
    	clear();
    	Agent agent = SBolt.Singleton().getAgent();
    	System.out.println("Restoring agent: " + name);
    	SBolt.Singleton().reloadAgent(false);
    	System.out.println("  epmem:" + agent.ExecuteCommandLine(String.format("epmem --set path backups/%s_epmem.db", name)));
    	System.out.println("  smem:" + agent.ExecuteCommandLine(String.format("smem --set path backups/%s_smem.db", name)));
    	agent.LoadProductions(String.format("backups/%s_chunks.soar", name));
    	System.out.println("  source: " + String.format("backups/%s_chunks.soar", name));
    	SVSConnector.Singleton().reset();
    	System.out.println("Completed Restore");
    }
}
