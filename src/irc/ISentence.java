package irc;

import jvn.JvnAnnotation;

public interface ISentence {
    
    @JvnAnnotation(nom="WRITE")
	public void write(String text);

	@JvnAnnotation(nom="READ")
	public String read();
}
