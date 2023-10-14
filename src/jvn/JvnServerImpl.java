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
import java.util.HashMap;



public class JvnServerImpl 	
              extends UnicastRemoteObject 
							implements JvnLocalServer, JvnRemoteServer{ 
	
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// A JVN server is managed as a singleton 
	private static JvnServerImpl js = null;

	private JvnRemoteCoord jvnCoord = null;

	private String jvnCoordURL = "//localhost:2001/JvnCoordinatorLink";
	
	HashMap<Integer,JvnObject> jvnObjects;
  

  /**
  * Default constructor
  * @throws JvnException
  **/
	private JvnServerImpl() throws Exception {
		super();
		try {
			jvnCoord = (JvnRemoteCoord) Naming.lookup(jvnCoordURL);
			jvnObjects = new HashMap<Integer,JvnObject>();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		System.out.println("jvnCreateObject-try");
		try {
			System.out.println("JVS - jvnCreateObject");
			int uniqueId = jvnCoord.jvnGetObjectId();

			System.out.println("JVS - uniqueId : "+uniqueId);
			JvnObject jo = new JvnObjectImpl(o,uniqueId);
			
			System.out.println("JVS - sauvegarde locale");
			this.jvnObjects.put(uniqueId, jo); // local save
			return jo;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null; 
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
			System.out.println("JVS - jvnRegisterObject");
			jvnCoord.jvnRegisterObject(jon, jo, js); // coord save

			
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
			System.out.println("JVS - jvnLookupObject");
		 	jo = jvnCoord.jvnLookupObject(jon, js);
			if (jo != null) this.jvnObjects.put(jo.jvnGetObjectId(), jo);
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
   public synchronized Serializable jvnLockRead(int joi)
	 throws JvnException {
		Serializable o = null;
		try {
			System.out.println("JVS - jvnLockRead sur "+joi);
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
   public synchronized Serializable jvnLockWrite(int joi)
	 throws JvnException {
		Serializable o = null;
		try {
			System.out.println("JVS - jvnLockWrite sur "+joi);
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
  public synchronized void jvnInvalidateReader(int joi)
	throws java.rmi.RemoteException,jvn.JvnException {
		System.out.println("JVS - jvnInvalidateReader sur "+joi);
		this.jvnObjects.get(joi).jvnInvalidateReader();
	}
	    
	/**
	* Invalidate the Write lock of the JVN object identified by id 
	* @param joi : the JVN object id
	* @return the current JVN object state
	* @throws java.rmi.RemoteException,JvnException
	**/
  public synchronized Serializable jvnInvalidateWriter(int joi)
	throws java.rmi.RemoteException,jvn.JvnException { 
		System.out.println("JVS - jvnInvalidateWriter sur "+joi);
		return this.jvnObjects.get(joi).jvnInvalidateWriter();
	}
	
	/**
	* Reduce the Write lock of the JVN object identified by id 
	* @param joi : the JVN object id
	* @return the current JVN object state
	* @throws java.rmi.RemoteException,JvnException
	**/
   public synchronized Serializable jvnInvalidateWriterForReader(int joi)
	 throws java.rmi.RemoteException,jvn.JvnException { 
		System.out.println("JVS - jvnInvalidateWriterForReader sur "+joi);
		return this.jvnObjects.get(joi).jvnInvalidateWriterForReader();
	 }

}

 
