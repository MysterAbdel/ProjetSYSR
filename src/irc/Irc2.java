/***
 * Irc2 class : simple implementation of a chat using JAVANAISE 2
 * Contact: 
 *
 * Authors: 
 */

package irc;

import java.awt.Button;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import jvn.JvnProxy;


public class Irc2 {
	public TextArea		text;
	public TextField	data;
	Frame 			frame;
	ISentence       sentence;


  /**
  * main method
  * create an object named IRC for representing the Chat application by Proxy
  **/
	public static void main(String argv[]) {
	   try {
		   
		ISentence jo = (ISentence) JvnProxy.newInstance(new Sentence(), "IRC");
		new Irc2(jo);
	   
	   } catch (Exception e) {
		   System.out.println("IRC problem : " + e.getMessage());
	   }
	}

  /**
   * IRC Constructor
   @param jo the ISentence representing the Chat
   **/
	public Irc2(ISentence jo) {
		sentence = jo;
		frame=new Frame();
		frame.setLayout(new GridLayout(1,1));
		text=new TextArea(10,60);
		text.setEditable(false);
		text.setForeground(Color.red);
		frame.add(text);
		data=new TextField(40);
		frame.add(data);
		Button read_button = new Button("read");
		read_button.addActionListener(new readListener2(this));
		frame.add(read_button);
		Button write_button = new Button("write");
		write_button.addActionListener(new writeListener2(this));
		frame.add(write_button);
		frame.setSize(545,201);
		text.setBackground(Color.black); 
		frame.setVisible(true);
	}
}


 /**
  * Internal class to manage user events (read) on the CHAT application
  **/
 class readListener2 implements ActionListener {
	Irc2 irc;
  
	public readListener2 (Irc2 i) {
		irc = i;
	}
   
 /**
  * Management of user events
  **/
	public void actionPerformed (ActionEvent e) {
	 try {
		
		// invoke the method
		String s = irc.sentence.read();
		
		// display the read value
		irc.data.setText(s);
		irc.text.append(s+"\n");
	   } catch (Exception je) {
		   System.out.println("IRC problem : " + je.getMessage());
	   }
	}
}

 /**
  * Internal class to manage user events (write) on the CHAT application
  **/
 class writeListener2 implements ActionListener {
	Irc2 irc;
  
	public writeListener2 (Irc2 i) {
        	irc = i;
	}
  
  /**
    * Management of user events
   **/
	public void actionPerformed (ActionEvent e) {
	   try {	
		// get the value to be written from the buffer
    	String s = irc.data.getText();
		
		// invoke the method
		irc.sentence.write(s);
	 } catch (Exception je) {
		   System.out.println("IRC problem  : " + je.getMessage());
	 }
	}
}



