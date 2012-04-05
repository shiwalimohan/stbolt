package edu.umich.sbolt;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

//import com.soartech.bolt.LGSupport;

public class ChatFrame extends JFrame
{

    private JTextArea chatArea;

    private JTextField chatField;

    private List<String> chatMessages;

    private SBolt sbolt;
    
    //private LGSupport lgSupport;

    public ChatFrame(SBolt sbolt){//, LGSupport lg) {
        super("SBolt");

        this.sbolt = sbolt;
       // lgSupport = lg;
        
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

        pane1.setDividerLocation(200);
        pane2.setDividerLocation(450);

        this.add(pane1);
        this.setSize(600, 300);
        this.getRootPane().setDefaultButton(button);
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
		sbolt.getWorld().newMessage(message.substring(1));
    	/*if (lgSupport == null) {
    		sbolt.getWorld().newMessage(message);
    	}
    	else if(message.charAt(0) == ':'){
    		// Prefixing with a : goes to Soar's message processing
    	} else {
    		//lgSupport.handleSentence(message);
    		// LGSupport has access to the agent object and handles all WM interaction from here
    	}*/
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
