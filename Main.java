package batMaker;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Main {
	
	JFrame frame;	//window
	JPanel panel;	//window panel
	
	//buttons
	JButton loadButton;
	JButton generateButton;
	
	JTextArea tf;
	
	String selectedFile;
	File xmlFile;
	
	ArrayList<String> used;
	
	public static void main(String[] args) {
		new Main();
	}
	
	public Main() {
		used = new ArrayList<String>();
		
		frame = new JFrame();	//create the window.
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	//when the user presses [X], terminate application
		
		panel = new JPanel();	//the main panel of the window.
		panel.setLayout(null);	//setting the layout.
		
		makeButtons();	//create and place buttons
		
		tf = new JTextArea();	//the textfield that is gonna play as the output
		tf.setBounds(10, 44, 517, 512);
		
		JScrollPane scroll = new JScrollPane(tf);
		scroll.setBounds(10, 44, 517, 512);
		panel.add(scroll);
		
		
		//attach buttons to the panel
		panel.add(loadButton);
		panel.add(generateButton);
		
		//panel.add(tf);	//attach text field to window
		
		frame.add(panel);	//attach panel to window.
		
		frame.setSize(536, 600);	//set the window size
		frame.setLocationRelativeTo(null);	//place the window center of screen
		frame.setResizable(false);	//user cannot resize window.
		frame.setVisible(true);	//display the window
	}
	
	//create buttons then add click listeners
	public void makeButtons() {
		loadButton = new JButton("Load File");	//button to load a file
		loadButton.setBounds(10, 10, 256, 32);
		
		generateButton = new JButton("Generate .bat file");	//button to start the generator
		generateButton.setBounds(270, 10, 256, 32);
		generateButton.setEnabled(false);
		
		//button listeners
		
		loadButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {	//when the user presses this button, show load dialog
				JFileChooser chooser = new JFileChooser();
				int returnVal = chooser.showOpenDialog(frame);
				
				//if the user pressed ok
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					xmlFile = chooser.getSelectedFile();
					generateButton.setEnabled(true);
					System.out.println("Loaded: " + xmlFile.getName());
				}
			}
		});
		
		
		//when the user presses this button. create output.
		generateButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				generateOutput(xmlFile);
			}
		});
	}
	
	public void generateOutput(File f) {
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dbBuilder = dbFactory.newDocumentBuilder();
			
			Document doc = dbBuilder.parse(f);	//grab the chosen xml file
			
			doc.getDocumentElement().normalize();
			spitOut(doc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void spitOut(Document doc) {
		NodeList nl = doc.getElementsByTagName("user");
		
		String txt = "rem @echo off \n" + "@echo off \n \n";
		
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			
			if(node.getNodeType() == Node.ELEMENT_NODE) {
				Element e = (Element)node;
				
				String dep = e.getElementsByTagName("department").item(0).getTextContent();	//department
				String drive = e.getElementsByTagName("drive").item(0).getTextContent();	//drive
				String folder = e.getElementsByTagName("folder").item(0).getTextContent();	//folder
				//System.out.println("I am in here!2");
				
				//just add the first department since the array is empty
				if(used.size() == 0) {
					txt += ":" + dep + " \n";
					txt += "@echo Mapper drev til " + dep + "afdelingen \n";
					txt += "net use " + drive + " \\\\" + "DCserver" + "\\" + folder + " \n";
					txt += "goto nexttjek \n \n";
					//System.out.println("adding: " + dep);
					used.add(dep);
				}else {
					//make sure that only one department function is created.
					for(int y = 0; y < used.size(); y++) {
						if(used.contains(dep)) {
							
						}else {
							txt += ":" + dep + " \n";
							txt += "@echo Mapper drev til " + dep + "afdelingen \n";
							txt += "net use " + drive + " \\\\" + "DCserver" + "\\" + folder + " /PERSISTENT:NO" + " \n";
							txt += "goto nexttjek \n \n";
							System.out.println("adding: " + dep);
							used.add(dep);
						}
					}
				}
				
				
			}
			
		}
		
		txt += ":nexttjek \n rem @echo Naeste tjek \n";
		
		//generate if's for each user.
		for(int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			
			if(node.getNodeType() == Node.ELEMENT_NODE) {
				Element e = (Element)node;
				
				String un = e.getElementsByTagName("name").item(0).getTextContent();	//Username
				String dep = e.getElementsByTagName("department").item(0).getTextContent();	//department
				
				txt += "if %USERNAME% == " + un + " goto " + dep + "\n";
				//txt += ":" + dep + " \n";
				//txt += "";
			}
		}
		
		tf.setText(txt);
	}
}
