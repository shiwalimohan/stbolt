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
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import sml.Agent;
import abolt.lcmtypes.robot_command_t;
import april.util.TimeUtil;

import com.soartech.bolt.BOLTLGSupport;
import com.soartech.bolt.testing.Action;
import com.soartech.bolt.testing.ActionType;
import com.soartech.bolt.testing.ParseScript;
import com.soartech.bolt.testing.Script;

import edu.umich.sbolt.world.World;

public class ChatFrame extends JFrame
{
	public static ChatFrame Singleton(){
		return instance;
	}
	private static ChatFrame instance = null;

    private JTextArea chatArea;

    private JTextField chatField;
    
    private JButton sendButton;

    private List<String> chatMessages;
    
    private BOLTLGSupport lgSupport;
    
    private ArrayList<String> history;
    
    private int historyIndex = 0;
    
    private InteractionStack stack;
    
    private boolean ready = false;
    
    private Script script;

    public ChatFrame(BOLTLGSupport lg) {
        super("SBolt");
        
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
				resetArm();
			}
        });
        menuBar.add(armResetButton);
        
        JMenu agentMenu = new JMenu("Full Reset");
        JMenuItem initButton = new JMenuItem("Reinitialize");
        initButton.addActionListener(new ActionListener(){
        	@Override
        	public void actionPerformed(ActionEvent e){
        		SBolt.Singleton().reloadAgent(true);
        	}
        });
        agentMenu.add(initButton);
        
        JMenuItem backupButton = new JMenuItem("Backup");
        backupButton.addActionListener(new ActionListener(){
        	@Override
        	public void actionPerformed(ActionEvent e){
        		backup();
        	}
        });
        agentMenu.add(backupButton);  
        
        JMenuItem restoreButton = new JMenuItem("Restore");
        restoreButton.addActionListener(new ActionListener(){
        	@Override
        	public void actionPerformed(ActionEvent e){
        		restore();
        	}
        });
        agentMenu.add(restoreButton);  

        stack = new InteractionStack();
        JMenuItem stackButton = new JMenuItem("Interaction Stack");
        stackButton.addActionListener(new ActionListener(){
        	@Override
			public void actionPerformed(ActionEvent arg0) {
				stack.showFrame();
			}
        });
        menuBar.add(stackButton);
		
		menuBar.add(agentMenu);
        
        JButton btnLoadScript = new JButton("Load Script");
		btnLoadScript.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				int returnVal = chooser.showOpenDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					script = ParseScript.parse(chooser.getSelectedFile());
				}
			}
		});
		menuBar.add(btnLoadScript);
		
		JButton btnNext = new JButton("Next");
		btnNext.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				handleNextScriptAction();
			}
		});
		menuBar.add(btnNext);
       
        setJMenuBar(menuBar);
        
        addWindowListener(new WindowAdapter() {
        	public void windowClosing(WindowEvent w) {
        		exit();
        	}
     	});
        
        setReady(false);
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
    
    private void handleNextScriptAction() {
    	if(script == null) {
    		addMessage("No script loaded!");
    		return;
    	}
    	if(!script.hasNextAction()) {
    		addMessage("Script finished.");
    		return;
    	}
    	Action next = script.getNextAction();
    	if(next.getType() == ActionType.Mentor) {
    		//chatField.setText(next.getAction());
    		sendSoarMessage(next.getAction());
    		history.add(next.getAction());
        	historyIndex = history.size();
            addMessage("Mentor: " + next.getAction());
    	}
    	if(next.getType() == ActionType.Agent) {
    		//check if response is correct
    		String observed = chatMessages.get(chatMessages.size()-1);
    		String expected = next.getAction();
    		if(!observed.contains(expected)) {
    			addMessage("- Error - Expected: "+expected);
    		} else {
    			addMessage("- Correct -");
    		}
    	}
    	if(next.getType() == ActionType.Check) {
    		addMessage("Check: "+next.getAction());
    	}
    	if(next.getType() == ActionType.Direction) {
    		addMessage("Directions: "+next.getAction());
    	}
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
    
    private void resetArm(){
		robot_command_t command = new robot_command_t();
		command.utime = TimeUtil.utime();
		command.action = "RESET";
		command.dest = new double[6];
		SBolt.broadcastRobotCommand(command);
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
    
    private void backup(){
    	Agent agent = SBolt.Singleton().getAgent();
    	System.out.println("Performing backup");
    	System.out.println(agent.ExecuteCommandLine("epmem --backup epmem_backup.db"));
    	System.out.println(agent.ExecuteCommandLine("smem --backup smem_backup.db"));
    	System.out.println(agent.ExecuteCommandLine("command-to-file chunks_backup.soar pc"));
    }
    
    private void restore(){
    	SBolt.Singleton().reloadAgent(false);
    	Agent agent = SBolt.Singleton().getAgent();
    	System.out.println(agent.ExecuteCommandLine("epmem --set path epmem_backup.db"));
    	System.out.println(agent.ExecuteCommandLine("smem --set path smem_backup.db"));
    	agent.LoadProductions("chunks_backup.soar");
    }

}
