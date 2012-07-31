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
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import com.soartech.bolt.BOLTLGSupport;
import com.soartech.bolt.script.ui.command.ResetRobotArm;
import com.soartech.bolt.testing.ActionType;
import com.soartech.bolt.testing.Script;
import com.soartech.bolt.testing.ScriptDataMap;
import com.soartech.bolt.testing.Util;

import edu.umich.sbolt.world.World;

public class ChatFrame extends JFrame
{
	// Singleton instance to access the ChatFrame from other places
	public static ChatFrame Singleton(){
		return instance;
	}
	private static ChatFrame instance = null;
	
	// GUI COMPONENTS
	
	private StyledDocument chatDoc;
    // The document which messages are displayed to

    private JTextField chatField;
    // The text field the user can type messages to the agent in
    
    private JTextPane tPane;
    
    private JButton sendButton;
    // The button used to send a message to the soar agent
    
    private JButton startStopButton;
    // The button that you can use to start and stop the agent (toggles between them)
    
    // CHAT MESSAGES AND HISTORY

    private List<String> chatMessages = new ArrayList<String>();
    // A list of all the messages currently in the chatArea
    
    private List<String> history = new ArrayList<String>();
    // A list of all messages typed into the chatField
    
    private int historyIndex = 0;
    // The current index into the history
    
    private BOLTLGSupport lgSupport;
    // Used to send messages to Soar through LG-Soar
    
    
    // AGENT STATUS AND CONTROL
    
    private boolean waitingForAgentResponse = false;
    // True if the script is waiting for an agent response
    
    private boolean waitingForAdvanceScript = false;
    // True if the system is waiting for the script to be advanced
    
    private boolean ready = false;
    // True if the agent is ready for a new message from the user
    
    private Script script;

    public ChatFrame(BOLTLGSupport lg, BoltAgent agent) {
        super("SBolt");
        System.out.println("Set object");
        World.Singleton().setPointedObjectID(0);
        instance = this;
        lgSupport = lg;
 
        /*
        chatArea = new JTextArea();
        chatArea.setFont(new Font("Serif",Font.PLAIN,18));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane pane = new JScrollPane(chatArea);
        */
        
        tPane = new JTextPane();
        tPane.setEditable(false);
        JScrollPane pane = new JScrollPane(tPane);
        DefaultCaret caret = (DefaultCaret)tPane.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		pane.setViewportView(tPane);
		chatDoc = (StyledDocument)tPane.getDocument();

		setupStyles();
        
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
				BoltAgent agent = SBolt.Singleton().getBoltAgent();
				if(agent.isRunning()){
					startStopButton.setText("START");
					agent.stop();
				} else {
					startStopButton.setText("STOP");
					agent.start();
				}
			}
        });
        menuBar.add(startStopButton);
        
        JButton clearButton  = new JButton("Clear Text");
        clearButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				if(SBolt.Singleton().getBoltAgent().isRunning()){
        			JOptionPane.showMessageDialog(null, "The agent must be stopped first");
				} else {
					clear();
				}
			}
        });
        menuBar.add(clearButton);
        
        JButton armResetButton  = new JButton("Reset Arm");
        armResetButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				new ResetRobotArm().execute();
			}
        });
        menuBar.add(armResetButton);
        
        BoltAgent.createAgentMenu(menuBar);
        
        JButton btnLoadScript = new JButton("Load Script");
		btnLoadScript.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				script = Util.loadScript();
			}
		});
		menuBar.add(btnLoadScript);
		
		JButton btnNext = new JButton("Next");
		btnNext.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Util.handleNextScriptAction(script, chatMessages);
			}
		});
		menuBar.add(btnNext);
		
		JButton btnSaveScript = new JButton("Save Script");
		btnSaveScript.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Util.saveScript(chatMessages);
			}
		});
		menuBar.add(btnSaveScript);
       
        setJMenuBar(menuBar);
        
        addWindowListener(new WindowAdapter() {
        	public void windowClosing(WindowEvent w) {
        		exit();
        	}
     	});
        
        setReady(false);
    }
    
    private void setupStyles() {
    	Style defaultStyle = chatDoc.addStyle(ActionType.Default.toString(), null);
    	Style agentStyle = chatDoc.addStyle(ActionType.Agent.toString(), defaultStyle);
        Style agentActionStyle = chatDoc.addStyle(ActionType.AgentAction.toString(), defaultStyle);
        Style commentStyle = chatDoc.addStyle(ActionType.Comment.toString(), defaultStyle);
        Style mentorStyle = chatDoc.addStyle(ActionType.Mentor.toString(), defaultStyle);
        Style mentorActionStyle = chatDoc.addStyle(ActionType.MentorAction.toString(), defaultStyle);
        Style correctStyle = chatDoc.addStyle(ActionType.Correct.toString(), defaultStyle);
        Style incorrectStyle = chatDoc.addStyle(ActionType.Incorrect.toString(), defaultStyle);
        
        StyleConstants.setForeground(defaultStyle, Color.BLACK);
        StyleConstants.setFontSize(defaultStyle, 18);
        StyleConstants.setFontFamily(defaultStyle, "SansSerif");
        
        StyleConstants.setForeground(agentActionStyle, new Color(225, 225, 0));
        StyleConstants.setForeground(agentStyle, Color.BLACK);
        StyleConstants.setItalic(agentStyle, true);
        StyleConstants.setFontFamily(agentStyle, "Serif");
        StyleConstants.setForeground(commentStyle, Color.BLUE);
        StyleConstants.setForeground(mentorStyle, Color.BLACK);
        StyleConstants.setForeground(mentorActionStyle, new Color(205, 0, 0));
        StyleConstants.setForeground(correctStyle, new Color(0, 200, 0));
        StyleConstants.setForeground(incorrectStyle, new Color(205, 0, 0));   
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
    
    public void setReady(boolean isReady){
    	ready = isReady;
    	updateSendButtonStatus();
    }
    
    public void setWaiting(boolean isWaiting) {
    	waitingForAgentResponse = isWaiting;
    	updateSendButtonStatus();
    }
    
    public void setWaitingForScript(boolean waiting) {
    	waitingForAdvanceScript = waiting;
    	updateSendButtonStatus();
    }
    
    private void updateSendButtonStatus() {
    	if(waitingForAdvanceScript) {
    		sendButton.setBackground(new Color(100, 255, 255));
    		sendButton.setText("Next Script Entry");
    	} else if(ready) {
    		if(waitingForAgentResponse) {
    			sendButton.setBackground(new Color(255, 255, 100));
        		sendButton.setText("Waiting for response");
    		} else {
    			sendButton.setBackground(new Color(150, 255, 150));
    			sendButton.setText("Send Message");
    		}
    	} else {
    		sendButton.setBackground(new Color(255, 100, 100));
    		sendButton.setText("Not Ready");
    	}
    }
    
    public void clear(){
    	chatMessages.clear();
    	chatField.setText("");
    	try {
			chatDoc.remove(0, chatDoc.getLength());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
    	InputLinkHandler.Singleton().clearLGMessages();
    	World.Singleton().destroyMessage();
    }
    
    public void addMessage(String message, ActionType type) {
    	message = ScriptDataMap.getInstance().getString(type)+" "+message.trim();
    	if(chatDoc.getStyle(type.toString()) == null) {
    		type = ActionType.Default;
    	}
    	chatMessages.add(message);
        try {
			chatDoc.insertString(chatDoc.getLength(), message+"\n", chatDoc.getStyle(type.toString()));
			// AM: Will make it auto scroll to bottom
			int end = chatDoc.getLength();
			tPane.select(end, end);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        if(type == ActionType.Agent && script != null && script.hasNextAction()) {
        	Util.handleNextScriptAction(script, chatMessages);
        }
    }

    public void addMessage(String message)
    {
        addMessage(message, ActionType.Default);
    }
    
    public void preSetMentorMessage(String message) {
    	chatField.setText(message);
    }
    
    public void exit(){
    	SBolt.Singleton().getBoltAgent().getSoarAgent().KillDebugger();
    	// sbolt.getKernel().DestroyAgent(sbolt.getAgent());
    	
    	// SBW removed DestroyAgent call, it hangs in headless mode for some reason
    	// (even when the KillDebugger isn't there)
    	// I don't think there's any consequence to simply exiting instead.
    	
    	System.exit(0);
    }
    
    private void sendButtonClicked(){
    	if(waitingForAdvanceScript) {
    		setWaitingForScript(false);
    		Util.handleNextScriptAction(script, chatMessages);
    		return;
    	}
    	if(!ready || waitingForAgentResponse){
    		return;
    	}
    	String msg = chatField.getText();
    	history.add(msg);
    	historyIndex = history.size();
    	if(msg.length() > 0 && msg.charAt(0) == '#') {
    		addMessage(msg.substring(1).trim(), ActionType.Comment);
    		chatField.setText("");
            chatField.requestFocus();
    		return;
    	}
        addMessage(msg, ActionType.Mentor);
        sendSoarMessage(msg);
        chatField.setText("");
        chatField.requestFocus();
        if(script != null && script.peekType() == ActionType.Agent) {
    		ChatFrame.Singleton().setWaiting(true);
        }
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

    public void sendSoarMessage(String message)
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
}
