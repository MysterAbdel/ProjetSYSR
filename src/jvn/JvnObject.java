/***
 * JAVANAISE API
 * Contact: 
 *
 * Authors: 
 */

package jvn;

import java.io.*;

/**
 * Interface of a JVN object. 
 * A JVN object is used to acquire read/write locks to access a given shared object
 */

public interface JvnObject extends Serializable {
	/* A JvnObject should be serializable in order to be able to transfer 
       a reference to a JVN object remotely */

	/**
	* Get a Read lock on the shared object 
	* @throws JvnException
	**/
	public void jvnLockRead()
	throws jvn.JvnException; 

	/**
	* Get a Write lock on the object 
	* @throws JvnException
	**/
	public void jvnLockWrite()
     	throws jvn.JvnException; 

	/**
	* Unlock  the object 
	* @throws JvnException
	**/
	public void jvnUnLock()
	throws jvn.JvnException; 
	
	
	/**
	* Get the object identification
	* @throws JvnException
	**/
	public int jvnGetObjectId()
	throws jvn.JvnException; 
	
	/**
	* Get the shared object associated to this JvnObject
	* @throws JvnException
	**/
	public Serializable jvnGetSharedObject()
	throws jvn.JvnException; 
	
	
	/**
	* Invalidate the Read lock of the JVN object 
	* @throws JvnException
	**/
  public void jvnInvalidateReader()
	throws jvn.JvnException;
	    
	/**
	* Invalidate the Write lock of the JVN object  
	* @return the current JVN object state
	* @throws JvnException
	**/
  public Serializable jvnInvalidateWriter()
	throws jvn.JvnException;
	
	/**
	* Reduce the Write lock of the JVN object 
	* @return the current JVN object state
	* @throws JvnException
	**/
   public Serializable jvnInvalidateWriterForReader()
	 throws jvn.JvnException;	
}
