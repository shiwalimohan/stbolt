package edu.byu.lgsoar.graph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
//import java.awt.event.ComponentEvent;
//import java.awt.event.ComponentListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;

/**
 * 
 * This class  is a {@link javax.swing#JFrame JFrame} which is used to display graphs. Each instantiation of a 
 * Grapher will have one display window (since it extends JFrame), and in practice is used to 
 * display graphs from one aspect of NL-Soar parsing (syntax, semantics, etc.). The frame will
 * not be visible until the first time {@link #display(String fileName) display} is called.
 * @author Nate Glenn
 */
public class Grapher extends JFrame{

	private static final long serialVersionUID = 2641048401951228835L;
	
	private	JScrollPane scrollPane;
	private JPanel topPanel;
	private JLabel label;
	private ImageIcon image;
	private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();	
	private double heightMax = screenSize.getHeight()-30;
	private double widthMax = screenSize.getWidth()-30;
	
	/**
	 * the JFrame's height must be this much taller than the image at all times
	 */
	private int heightDiff = 100;
	/**
	 * the JFrame's width must be this much wider than the image at all times
	 */
	private int widthDiff = 40;
	
	public Grapher(String title){
		System.out.println("grapher init!");
		setVisible(true);

		setTitle(title);
		setBackground( Color.gray );
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		topPanel = new JPanel();
		topPanel.setLayout( new BorderLayout() );
		getContentPane().add( topPanel );
		label = new JLabel();
		
		scrollPane = new JScrollPane();
		scrollPane.getViewport().add( label );
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		topPanel.add( scrollPane, BorderLayout.CENTER );
//		topPanel.addComponentListener(new Listeners());
		topPanel.setName("Soar Grapher Panel");
		

	}

	/**
	 * Displays a picture file in the grapher window. 
	 * @param fileName Name of file to be displayed.
	 */
    public void display(String fileName){
    	System.out.println("display");
    	//steal focus the first time an image is displayed
    	if(!new File(fileName).exists())
    		new Exception("Error- file doesn't exist: " + fileName).printStackTrace();
//    		System.err.println("Error- file doesn't exist: " + fileName);
    	//if(image == null)//means that we are opening this for the first time
    	//	setVisible(true);
    	image = new ImageIcon(fileName);
		image.getImage().flush();
		setSize(getPreferredSize());
		//pack();
    	label.setIcon(image);
		label.repaint();
    }
    
    /**
     * Reset the size of the grapher and remove the currently displayed image.
     */
    public void clear(){
    	image = null;
    	setSize(getPreferredSize());
    	label.repaint();
    }
    
    /**
     * This method is meant for internal use only. Determines the size of the graphing window based
     * on the size of the picture to be displayed and the size of the monitor (does not take into 
     * account more than one monitor). If necessary, the display picture will be resized (in the
     *  window, not on the disk).
     */
    @Override
    public Dimension getPreferredSize(){
    	if (image == null)
    		return new Dimension(100,100);

    	//recalculate image size
    	double imageHeight = image.getIconHeight();
    	double imageWidth = image.getIconWidth();
 	
    	//calculate scale according to screen size
    	double scale = 1;
    	if(imageHeight > heightMax - heightDiff){
    		scale = (heightMax - heightDiff)/imageHeight;
    		imageHeight = imageHeight*scale;
    		imageWidth = imageWidth*scale;
    	}
    	if(imageWidth > widthMax - widthDiff){
    			scale = (widthMax - widthDiff)/imageWidth;
        		imageHeight = imageHeight*scale;
        		imageWidth = imageWidth*scale;
    	}
    	
    	//resize according to calculated scale
    	if(scale != 1)
			try{
				//adjust size of the image according to screen size, then recurse.
				image = new ImageIcon(image.getImage().getScaledInstance(
						(int)Math.floor(imageWidth),
						(int)Math.floor(imageHeight),
						Image.SCALE_SMOOTH));
			}catch(Exception e){e.printStackTrace();}
    	
    	
    	//calculate frame size; bigger than image, max size is size of screen
    	//d
    	if(getExtendedState() != JFrame.MAXIMIZED_BOTH){
	    	double windowHeight = image.getIconHeight() > getHeight() + heightDiff
				? image.getIconHeight() + heightDiff : getHeight();
	    	if(windowHeight > heightMax)
	    		windowHeight = heightMax;
	    	
	    	double windowWidth = image.getIconWidth() > getWidth()  + widthDiff
				? image.getIconWidth() + widthDiff : getWidth();
	    	if(windowWidth > widthMax)
	    		windowWidth = widthMax;
	    	
	    	return new Dimension((int)Math.floor(windowWidth), (int)Math.floor(windowHeight));
    	}
    	
    	return getSize();
    }


}
