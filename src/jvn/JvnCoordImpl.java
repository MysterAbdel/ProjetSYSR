/***
 * JAVANAISE Implementation
 * JvnCoordImpl class
 * This class implements the Javanaise central coordinator
 * Contact:  
 *
 * Authors: 
 */

package jvn;

import java.io.Serializable;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;


public class JvnCoordImpl 	
              extends UnicastRemoteObject 
							implements JvnRemoteCoord{
	

  /**
	 * 
	 */
	private static long serialVersionUID = 1L;

  /**
   * - JvnRemoteServer 1 : ["nomObjet1" : objet1, "nomObjet2" : objet2, ...]
   * - [...]
   * - JvnRemoteServer n : ["nomObjet1" : objet1, "nomObjet2" : objet2, ...]
   */
  private HashMap<JvnRemoteServer,HashMap<String,JvnObjectImpl>> objetsPartages;

  private String jvnCoordURL = "//localhost:2001/JvnCoordinatorLink";

/**
  * Default constructor
  * @throws JvnException
  **/
	private JvnCoordImpl() throws Exception {
    super();
    objetsPartages = new HashMap<JvnRemoteServer,HashMap<String,JvnObjectImpl>>();
    LocateRegistry.createRegistry(2001);
    Naming.bind(jvnCoordURL, this);
	}

  /**
  *  Allocate a NEW JVN object id (usually allocated to a 
  *  newly created JVN object)
  * @throws java.rmi.RemoteException,JvnException
  **/
  public int jvnGetObjectId()
  throws java.rmi.RemoteException,jvn.JvnException {
    return (int) serialVersionUID++;
  }
  
  /**
  * Associate a symbolic name with a JVN object
  * @param jon : the JVN object name
  * @param jo  : the JVN object 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the JVNServer
  * @throws java.rmi.RemoteException,JvnException
  **/
  public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js)
  throws java.rmi.RemoteException,jvn.JvnException{
    if(!objetsPartages.containsKey(js)){
      objetsPartages.put(js, new HashMap<String,JvnObjectImpl>());
    }

    if(objetsPartages.get(js).containsKey(jon)){
      throw new JvnException("Objet déja existant");
    }

    objetsPartages.get(js).put(jon, (JvnObjectImpl) jo);
  }
  
  /**
  * Get the reference of a JVN object managed by a given JVN server 
  * @param jon : the JVN object name
  * @param js : the remote reference of the JVNServer
  * @throws java.rmi.RemoteException,JvnException
  **/
  public JvnObject jvnLookupObject(String jon, JvnRemoteServer js)
  throws java.rmi.RemoteException,jvn.JvnException{
    HashMap<String,JvnObjectImpl> objets = objetsPartages.get(js);

    if (objets == null) {
      throw new JvnException("Objet inexistant");
    }

    return objets.get(jon);
  }
  
  /**
  * Get a Read lock on a JVN object managed by a given JVN server 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the server
  * @return the current JVN object state
  * @throws java.rmi.RemoteException, JvnException
  **/
   public Serializable jvnLockRead(int joi, JvnRemoteServer js)
   throws java.rmi.RemoteException, JvnException{
    return null;
   }

  /**
  * Get a Write lock on a JVN object managed by a given JVN server 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the server
  * @return the current JVN object state
  * @throws java.rmi.RemoteException, JvnException
  **/
   public Serializable jvnLockWrite(int joi, JvnRemoteServer js)
   throws java.rmi.RemoteException, JvnException{
    // to be completed
    return null;
   }

	/**
	* A JVN server terminates
	* @param js  : the remote reference of the server
	* @throws java.rmi.RemoteException, JvnException
	**/
    public void jvnTerminate(JvnRemoteServer js)
	 throws java.rmi.RemoteException, JvnException {
      objetsPartages.remove(js);
    }
}

 
