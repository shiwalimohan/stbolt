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

import edu.umich.sbolt.world.World;

public class ChatFrame extends JFrame implements RunEventInterface
{
	
	public static ChatFrame Singleton(){
		return instance;
	}
	private static ChatFrame instance = null;
	
	private Agent agent;

    private JTextArea chatArea;

    private JTextField chatField;
    
    private JButton sendButton;
    
    private JButton startStopButton;

    private List<String> chatMessages;
    
    private BOLTLGSupport lgSupport;
    
    private ArrayList<String> history;
    
    private int historyIndex = 0;
    
    private InteractionStack stack;
    
    private boolean ready = false;
    
    private Thread agentThread = null;
    
    private boolean isAgentRunning = false;
    
    private boolean stopAgent = false;

    public ChatFrame(BOLTLGSupport lg, Agent agent) {
        super("SBolt");
        
        this.agent = agent;

        agent.RegisterForRunEvent(
                smlRunEventId.smlEVENT_AFTER_OUTPUT_PHASE, this, null);
        
        instance = this;
        
        history = new ArrayList<String>();

        lgSupport = lg;
        
        chatMessages = new ArrayList<String>();

        chatArea = new JTextArea();
        chatArea.setFont(new Font("Serif",Font.PLAIN,18));
        JScrollPane pane = new JScrollPane(chatArea);
        
        chatField = new JTextField();
        chatField.setFont(new Font("Serif",Font.PLAIN,18));
        chatField.addKeyListener(new KeyAdapter(){
			@Override
			public void keyPressed(KeyEvent arg0) {
				if(arg0.getKeyCode() == KeyEvent.VK_UP) {
					if(historyIndex > 0){
						historyIndex--;
					}
					if(history.size() > 0){
						chatField.setText(history.get(historyIndex));
					}
				} else if(arg0.getKeyCode() == KeyEvent.VK_DOWN){
					historyIndex++;
					if(historyIndex > history.size()){
						historyIndex = history.size();
					}
					if(historyIndex == history.size()){
						chatField.setText("");
					} else {
						chatField.setText(history.get(historyIndex));
					}
				}
			}
        });
        
        
        sendButton = new JButton("Send Message");
        sendButton.addActionListener(new ActionListener()
        {
            @Override
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


        startStopButton  = new JButton("START");
        startStopButton.addActionListener(new ActionListener(){
			@Override
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
			@Override
			public void actionPerformed(ActionEvent arg0) {
				clear();
			}
        });
        menuBar.add(clearButton);
        
        JButton armResetButton  = new JButton("Reset Arm");
        armResetButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				robot_command_t command = new robot_command_t();
				command.utime = TimeUtil.utime();
				command.action = "RESET";
				command.dest = new double[6];
				SBolt.broadcastRobotCommand(command);
			}
        });
        menuBar.add(armResetButton);

        JMenu agentMenu = new JMenu("Agent");

        
        JMenuItem resetButton = new JMenuItem("Full Reset");
        resetButton.addActionListener(new ActionListener(){
        	@Override
        	public void actionPerformed(ActionEvent e){
        		if(!isAgentRunning){
        			SBolt.Singleton().reloadAgent(true);
        		}
        	}
        });
        agentMenu.add(resetButton);
        
//        JMenuItem commandButton = new JMenuItem("Enter SML Command");
//        commandButton.addActionListener(new ActionListener(){
//        	@Override
//        	public void actionPerformed(ActionEvent e){
//        		String command = JOptionPane.showInputDialog(null, 
//            			  "Enter the SML Command to execute",
//            			  "Enter SML Command",
//            			  JOptionPane.QUESTION_MESSAGE);
//        	}
//        });
//        agentMenu.add(commandButton);
        
        
        agentMenu.addSeparator();

        
        JMenuItem backupButton = new JMenuItem("Backup");
        backupButton.addActionListener(new ActionListener(){
        	@Override
        	public void actionPerformed(ActionEvent e){
        		if(!isAgentRunning){
        			backup("default");
        		} else {
        			JOptionPane.showMessageDialog(null, "The agent must be stopped");
        		}
        	}
        });
        agentMenu.add(backupButton);  
        
        JMenuItem restoreButton = new JMenuItem("Restore");
        restoreButton.addActionListener(new ActionListener(){
        	@Override
        	public void actionPerformed(ActionEvent e){
        		if(!isAgentRunning){
        			restore("default");
        		} else {
        			JOptionPane.showMessageDialog(null, "The agent must be stopped");
        		}
        	}
        });
        agentMenu.add(restoreButton);  
        
        JMenuItem backupToFileButton = new JMenuItem("Backup To File");
        backupToFileButton.addActionListener(new ActionListener(){
        	@Override
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
        
        JMenuItem restoreFromFileButton = new JMenuItem("Restore From File");
        restoreFromFileButton.addActionListener(new ActionListener(){
        	@Override
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

        stack = new InteractionStack();
        JMenuItem stackButton = new JMenuItem("Interaction Stack");
        stackButton.addActionListener(new ActionListener(){
        	@Override
			public void actionPerformed(ActionEvent arg0) {
				stack.showFrame();
			}
        });
        agentMenu.add(stackButton);
        
        menuBar.add(agentMenu);
       
        setJMenuBar(menuBar);
        
        addWindowListener(new WindowAdapter() {
        	public void windowClosing(WindowEvent w) {
        		exit();
        	}
     	});
        
        setReady(false);
    }
    
    private void runAgent(){
    	class AgentThread implements Runnable{
    		public void run(){
    			SBolt.Singleton().getAgent().ExecuteCommandLine("run");
    			
    		}
    	}
    	agentThread = new Thread(new AgentThread());
    	agentThread.start();
    	isAgentRunning = true;
		startStopButton.setText("STOP");
    }
    
    public void runEventHandler(int eventID, Object data, Agent agent, int phase)
    {
    	if(stopAgent){
    		agent.ExecuteCommandLine("stop");
    		isAgentRunning = false;
    		startStopButton.setText("START");
    		stopAgent = false;
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
    	System.out.println("epmem:" + agent.ExecuteCommandLine(String.format("epmem --backup backups/%s_epmem.db", name)));
    	System.out.println("smem:" + agent.ExecuteCommandLine(String.format("smem --backup backups/%s_smem.db", name)));
    	System.out.println("chunks:" + agent.ExecuteCommandLine(String.format("command-to-file backups/%s_chunks.soar pc", name)));
    }
    
    private void restore(String name){
    	Agent agent = SBolt.Singleton().getAgent();
    	System.out.println("Restoring agent: " + name);
    	SBolt.Singleton().reloadAgent(false);
    	System.out.println("epmem:" + agent.ExecuteCommandLine(String.format("epmem --set path backups/%s_epmem.db", name)));
    	System.out.println("smem:" + agent.ExecuteCommandLine(String.format("smem --set path backups/%s_smem.db", name)));
    	agent.LoadProductions(String.format("backups/%s_chunks.soar", name));
    	System.out.println("chunks loaded");
    }
}
