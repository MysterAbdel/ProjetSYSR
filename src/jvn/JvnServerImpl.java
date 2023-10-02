/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Implementation of a Jvn server
 * Contact: 
 *
 * Authors: 
 */

package jvn;

import java.io.Serializable;
import java.rmi.Naming;
import java.rmi.server.UnicastRemoteObject;



public class JvnServerImpl 	
              extends UnicastRemoteObject 
							implements JvnLocalServer, JvnRemoteServer{ 
	
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// A JVN server is managed as a singleton 
	private static JvnServerImpl js = null;

	private JvnCoordImpl jvnCoord = null;
	private static String jvnCoordURL = "//localhost:2001/JvnCoordinatorLink";

  

  /**
  * Default constructor
  * @throws JvnException
  **/
	private JvnServerImpl() throws Exception {
		super();
		jvnCoord = (JvnCoordImpl) Naming.lookup(jvnCoordURL);
	}
	
  /**
    * Static method allowing an application to get a reference to 
    * a JVN server instance
    * @throws JvnException
    **/
	public static JvnServerImpl jvnGetServer() {
		if (js == null){
			try {
				js = new JvnServerImpl();
			} catch (Exception e) {
				return null;
			}
		}
		return js;
	}
	
	/**
	* The JVN service is not used anymore
	* @throws JvnException
	**/
	public  void jvnTerminate()
	throws jvn.JvnException {
		try {
			jvnCoord.jvnTerminate(js);
		} catch (Exception e) {
			e.printStackTrace();
		}
	} 
	
	/**
	* creation of a JVN object
	* @param o : the JVN object state
	* @throws JvnException
	**/
	public  JvnObject jvnCreateObject(Serializable o)
	throws jvn.JvnException { 
		JvnObjectImpl jo = (JvnObjectImpl) o;
		try {
			jo.setUniqueId(jvnCoord.jvnGetObjectId());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (JvnObject) jo; 
	}
	
	/**
	*  Associate a symbolic name with a JVN object
	* @param jon : the JVN object name
	* @param jo : the JVN object 
	* @throws JvnException
	**/
	public  void jvnRegisterObject(String jon, JvnObject jo)
	throws jvn.JvnException {
		try {
			jvnCoord.jvnRegisterObject(jon, jo, js);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	* Provide the reference of a JVN object beeing given its symbolic name
	* @param jon : the JVN object name
	* @return the JVN object 
	* @throws JvnException
	**/
	public  JvnObject jvnLookupObject(String jon)
	throws jvn.JvnException {
		JvnObject jo = null;
		try {
			jo = jvnCoord.jvnLookupObject(jon, js);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jo;
	}	
	
	/**
	* Get a Read lock on a JVN object 
	* @param joi : the JVN object identification
	* @return the current JVN object state
	* @throws  JvnException
	**/
   public Serializable jvnLockRead(int joi)
	 throws JvnException {
		Serializable o = null;
		try {
			o = jvnCoord.jvnLockRead(joi, js);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return o;
	}	
	/**
	* Get a Write lock on a JVN object 
	* @param joi : the JVN object identification
	* @return the current JVN object state
	* @throws  JvnException
	**/
   public Serializable jvnLockWrite(int joi)
	 throws JvnException {
		Serializable o = null;
		try {
			o = jvnCoord.jvnLockWrite(joi, js);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return o;
	}	

	
  /**
	* Invalidate the Read lock of the JVN object identified by id 
	* called by the JvnCoord
	* @param joi : the JVN object id
	* @return void
	* @throws java.rmi.RemoteException,JvnException
	**/
  public void jvnInvalidateReader(int joi)
	throws java.rmi.RemoteException,jvn.JvnException {
		try {
			JvnObject jvnObject = jvnCoord.jvnLookupObject(jvnCoordURL, js);
			jvnObject.jvnInvalidateReader();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	    
	/**
	* Invalidate the Write lock of the JVN object identified by id 
	* @param joi : the JVN object id
	* @return the current JVN object state
	* @throws java.rmi.RemoteException,JvnException
	**/
  public Serializable jvnInvalidateWriter(int joi)
	throws java.rmi.RemoteException,jvn.JvnException { 
		Serializable o = null;
		try {
			JvnObject jvnObject = jvnCoord.jvnLookupObject(jvnCoordURL, js);
			o = jvnObject.jvnInvalidateWriter();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return o;
	}
	
	/**
	* Reduce the Write lock of the JVN object identified by id 
	* @param joi : the JVN object id
	* @return the current JVN object state
	* @throws java.rmi.RemoteException,JvnException
	**/
   public Serializable jvnInvalidateWriterForReader(int joi)
	 throws java.rmi.RemoteException,jvn.JvnException { 
		Serializable o = null;
		try {
			JvnObject jvnObject = jvnCoord.jvnLookupObject(jvnCoordURL, js);
			o = jvnObject.jvnInvalidateWriterForReader();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return o;
	 }

}

 
