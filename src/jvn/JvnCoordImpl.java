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
  
  private int uniqueId = 1;

  /**
   * - JvnRemoteServer 1 : ["nomObjet1" : objet1, "nomObjet2" : objet2, ...]
   * - [...]
   * - JvnRemoteServer n : ["nomObjet1" : objet1, "nomObjet2" : objet2, ...]
   */
  private HashMap<JvnRemoteServer,HashMap<String,JvnObjectImpl>> objetsPartages;
  private HashMap<Integer,HashMap<JvnRemoteServer,EtatVerrou>> etatsVerrous;
  private HashMap<Integer,String> idNoms;

  private String jvnCoordURL = "//localhost:2001/JvnCoordinatorLink";

/**
  * Default constructor
  * @throws JvnException
  **/
	private JvnCoordImpl() throws Exception {
    super();
    
    objetsPartages = new HashMap<JvnRemoteServer,HashMap<String,JvnObjectImpl>>();
    etatsVerrous = new HashMap<Integer,HashMap<JvnRemoteServer,EtatVerrou>>();
    idNoms = new HashMap<Integer,String>();

    LocateRegistry.createRegistry(2001);
    Naming.bind(jvnCoordURL, this);
	}

  public static void main(String[] args) {
    try {
      new JvnCoordImpl();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
  *  Allocate a NEW JVN object id (usually allocated to a 
  *  newly created JVN object)
  * @throws java.rmi.RemoteException,JvnException
  **/
  public int jvnGetObjectId()
  throws java.rmi.RemoteException,jvn.JvnException {
    return uniqueId++;
  }
  
  public void updateEtatVerrou(int id, JvnRemoteServer js, EtatVerrou etatVerrou){
    if(!etatsVerrous.containsKey(id)){
      etatsVerrous.put(id, new HashMap<JvnRemoteServer,EtatVerrou>());
    }

    etatsVerrous.get(id).put(js, etatVerrou);
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

    System.out.println("JVC - jvnRegisterObject de nom "+ jon);
    
    if(!objetsPartages.containsKey(js)){
      objetsPartages.put(js, new HashMap<String,JvnObjectImpl>());
    }

    if(objetsPartages.get(js).containsKey(jon)){
      throw new JvnException("Objet déja existant");
    }

    objetsPartages.get(js).put(jon, (JvnObjectImpl) jo);
    
    // sauvegarder l'id et son nom
    if(!idNoms.containsKey(jo.jvnGetObjectId())) {
      idNoms.put(jo.jvnGetObjectId(), jon);
    }
  }
  
  /**
  * Get the reference of a JVN object managed by a given JVN server 
  * @param jon : the JVN object name
  * @param js : the remote reference of the JVNServer
  * @throws java.rmi.RemoteException,JvnException
  **/
  public JvnObject jvnLookupObject(String jon, JvnRemoteServer js)
  throws java.rmi.RemoteException,jvn.JvnException{
    System.out.println("JVC - jvnLookupObject de nom "+ jon);

    if(objetsPartages.containsKey(js) && objetsPartages.get(js).containsKey(jon)){
      System.out.println("JVC - jvnLookupObject : objet trouvé");
      return objetsPartages.get(js).get(jon);
    }

    System.out.println("JVC - jvnLookupObject : objet non trouvé");
    return null;
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
    Serializable objectState = null;

    return objectState;
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
    System.out.println("JVC - jvnLockWrite sur + " + joi);

    Serializable objectState = null;
    return objectState;
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

 
