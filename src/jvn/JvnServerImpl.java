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
import java.rmi.RemoteException;
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

	// le coordonnateur
	private JvnRemoteCoord jvnCoord;
	// url du coordonnateur
	private String jvnCoordURL = "//localhost:2001/JvnCoord";

	// Hashmap Id/Object
	private HashMap<Integer, JvnObject> mapIdObject;

  /**
  * Default constructor
  * @throws JvnException
  **/
	private JvnServerImpl() throws Exception {
		super();
		try {
			jvnCoord = (JvnRemoteCoord) Naming.lookup(jvnCoordURL);
			mapIdObject = new HashMap<Integer, JvnObject>();
			System.out.println("JVS - jvnCoord récupéré : " + jvnCoord);
		} catch (Exception e) {
			System.out.println("Erreur de récupération du coordonnateur");
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
			System.out.println("JVS - jvnTerminate");
			jvnCoord.jvnTerminate(js);
		} catch (RemoteException e) {
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
		try {
			System.out.println("JVS - jvnCreateObject");

			int idUnique = jvnCoord.jvnGetObjectId();
			System.out.println("JVS - identifiant unique : " + idUnique);

			JvnObject jo = new JvnObjectImpl(idUnique, o);
			System.out.println("JVS - objet créé : " + jo);

			mapIdObject.put(idUnique, jo);

			return jo;
		} catch (Exception e) {
			System.out.println("JVS - Erreur de création de l'objet");
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
			System.out.println("JVS - jvnRegisterObject de nom " + jon);
			jvnCoord.jvnRegisterObject(jon, jo, js);
		} catch (RemoteException e) {
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
		try {
			System.out.println("JVS - jvnLookupObject de nom " + jon);
			JvnObject jo = jvnCoord.jvnLookupObject(jon, js);

			if (jo != null) {
				System.out.println("JVS - objet trouvé : " + jo);
				mapIdObject.put(jo.jvnGetObjectId(), jo);
			} else {
				System.out.println("JVS - objet non trouvé");
			}
			
			return jo;
		} catch (Exception e) {
			System.out.println("JVS - Erreur de récupération de l'objet");
			e.printStackTrace();
		}
		return null;
	}	
	
	/**
	* Get a Read lock on a JVN object 
	* @param joi : the JVN object identification
	* @return the current JVN object state
	* @throws  JvnException
	**/
   public Serializable jvnLockRead(int joi)
	 throws JvnException {
		try {
			System.out.println("JVS - jvnLockRead de l'objet " + joi);
			return jvnCoord.jvnLockRead(joi, js);
		} catch (Exception e) {
			System.out.println("JVS - Erreur de lock read");
			e.printStackTrace();
		}
		return null;
	}	
	/**
	* Get a Write lock on a JVN object 
	* @param joi : the JVN object identification
	* @return the current JVN object state
	* @throws  JvnException
	**/
   public Serializable jvnLockWrite(int joi)
	 throws JvnException {
		try {
			System.out.println("JVS - jvnLockWrite de l'objet " + joi);
			return jvnCoord.jvnLockWrite(joi, js);
		} catch (Exception e) {
			System.out.println("JVS - Erreur de lock write");
			e.printStackTrace();
		}
		return null;
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
		System.out.println("JVS - jvnInvalidateReader");
		this.mapIdObject.get(joi).jvnInvalidateReader();
	}
	    
	/**
	* Invalidate the Write lock of the JVN object identified by id 
	* @param joi : the JVN object id
	* @return the current JVN object state
	* @throws java.rmi.RemoteException,JvnException
	**/
  public Serializable jvnInvalidateWriter(int joi)
	throws java.rmi.RemoteException,jvn.JvnException { 
		System.out.println("JVS - jvnInvalidateWriter");
		return this.mapIdObject.get(joi).jvnInvalidateWriter();
	}
	
	/**
	* Reduce the Write lock of the JVN object identified by id 
	* @param joi : the JVN object id
	* @return the current JVN object state
	* @throws java.rmi.RemoteException,JvnException
	**/
   public Serializable jvnInvalidateWriterForReader(int joi)
	 throws java.rmi.RemoteException,jvn.JvnException { 
		System.out.println("JVS - jvnInvalidateWriterForReader");
		return this.mapIdObject.get(joi).jvnInvalidateWriterForReader();
	 };

}

 
