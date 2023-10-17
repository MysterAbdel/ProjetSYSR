/***
 * Sentence class : used for keeping the text exchanged between users
 * during a chat application
 * Contact: 
 *
 * Authors: 
 */

package irc;

import jvn.JvnAnnotation;

public class Sentence implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String 	data;
  
	public Sentence() {
		data = new String("");
	}
	
	@JvnAnnotation(nom="WRITE")
	public void write(String text) {
		data = text;
	}

	@JvnAnnotation(nom="READ")
	public String read() {
		return data;	
	}
	
}