package edu.umich.sbolt;

import java.awt.MenuShortcut;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import static java.awt.event.InputEvent.CTRL_DOWN_MASK;
import static java.awt.event.InputEvent.SHIFT_DOWN_MASK;

import com.soartech.bolt.BOLTLGSupport;

public class ChatFrame extends JFrame
{

    private JTextArea chatArea;

    private JTextField chatField;

    private List<String> chatMessages;

    private SBolt sbolt;
    
    private BOLTLGSupport lgSupport;

    public ChatFrame(SBolt sbolt, BOLTLGSupport lg) {
        super("SBolt");

        this.sbolt = sbolt;
        lgSupport = lg;
        
        chatMessages = new ArrayList<String>();

        chatArea = new JTextArea();
        JScrollPane pane = new JScrollPane(chatArea);
        chatField = new JTextField();
        JButton button = new JButton("Send Message");
        button.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                addMessage(chatField.getText());
                sendSoarMessage(chatField.getText());
                chatField.setText("");
                chatField.requestFocus();
            }
        });

        JSplitPane pane2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                chatField, button);
        JSplitPane pane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pane,
                pane2);

        pane1.setDividerLocation(325);
        pane2.setDividerLocation(600);

        this.add(pane1);
        this.setSize(800, 450);
        this.getRootPane().setDefaultButton(button);
        
        JMenuBar menuBar = new JMenuBar();        
        
        JButton clearButton  = new JButton("Clear");
        clearButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				clear();
			}
        });
        menuBar.add(clearButton);
        
        setJMenuBar(menuBar);
        
        addWindowListener(new WindowAdapter() {
        	public void windowClosing(WindowEvent w) {
        		exit();
        	}
     	});
    }
    
    public void clear(){
    	chatMessages.clear();
    	chatField.setText("");
    	chatArea.setText("");
    	sbolt.getInputLink().clearLGMessages();
    	sbolt.getWorld().destroyMessage();
    }
    
    public void exit(){
    	sbolt.getAgent().KillDebugger();
    	sbolt.getKernel().DestroyAgent(sbolt.getAgent());
    	System.exit(0);
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
    }

    private void sendSoarMessage(String message)
    {
    	if (lgSupport == null) {
    		sbolt.getWorld().newMessage(message);
    	}
    	else if(message.length() > 0 && message.charAt(0) == ':'){
    		// Prefixing with a : goes to Soar's message processing
    		sbolt.getWorld().newMessage(message.substring(1));
    	} else {
    		lgSupport.handleInput(message);
    		// LGSupport has access to the agent object and handles all WM interaction from here
    	}
    }

    public void showFrame()
    {
        this.setDefaultCloseOperation(this.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    public void hideFrame()
    {
        this.setVisible(false);
    }

}
