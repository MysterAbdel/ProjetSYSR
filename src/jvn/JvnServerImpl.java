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

	private JvnRemoteCoord jvnCoord= null;
	private static String jvnCoordURL = "//localhost:2001/JvnCoordinatorLink";
	
	private HashMap<Integer,JvnObject> objects;

  

  /**
  * Default constructor
  * @throws JvnException
  **/
	private JvnServerImpl() throws Exception {
		super();
		try {
			jvnCoord = (JvnRemoteCoord) Naming.lookup(jvnCoordURL);
		} catch (Exception e) {
			e.printStackTrace();
		}
		objects = new HashMap<Integer,JvnObject>();
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
			System.out.println("jvnCreateObject-uniqueId : "+uniqueId);
			JvnObject jo = new JvnObjectImpl(o,uniqueId);
			objects.put(jo.jvnGetObjectId(), jo);
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
			System.out.println("JVS - jvnLookupObject");
		 	jo = jvnCoord.jvnLookupObject(jon, js);
		 	//maybe do sth
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
   public Serializable jvnLockWrite(int joi)
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
  public void jvnInvalidateReader(int joi)
	throws java.rmi.RemoteException,jvn.JvnException {
		try {
			JvnObject jvnObject = jvnCoord.jvnLookupObject(jvnCoordURL, js);
			System.out.println("JVS - jvnInvalidateReader sur "+joi);
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
			System.out.println("JVS - jvnInvalidateWriter sur "+joi);
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
			System.out.println("JVS - jvnInvalidateWriterForReader sur "+joi);
			o = jvnObject.jvnInvalidateWriterForReader();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return o;
	 }

}

 
