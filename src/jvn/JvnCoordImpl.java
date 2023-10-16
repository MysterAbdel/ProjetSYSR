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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;


public class JvnCoordImpl 	
              extends UnicastRemoteObject 
							implements JvnRemoteCoord{
	

  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

  // Url 
  private String jvnCoordURL = "//localhost:2001/JvnCoord";
  // Compteur
  private int compteurIdUnique = 0;

  // Hashmap Nom/Id
  private HashMap<String, Integer> mapNomId;
  // Hashmap Id/Object
  private HashMap<Integer, JvnObject> mapIdObject;
  
  // Hashmap Id/Liste_serveurs_ayant_le_lock_en_lecture
  private HashMap<Integer, List<JvnRemoteServer>> mapIdListeServeursLecture;

  // Hashmap Id/Serveur_ayant_le_lock_en_ecriture
  private HashMap<Integer, JvnRemoteServer> mapIdServeurEcriture;

  
/**
  * Default constructor
  * @throws JvnException
  **/
	private JvnCoordImpl() throws Exception {
    super();

    mapNomId = new HashMap<String, Integer>();
    mapIdObject = new HashMap<Integer, JvnObject>();
    mapIdListeServeursLecture = new HashMap<Integer, List<JvnRemoteServer>>();
    mapIdServeurEcriture = new HashMap<Integer, JvnRemoteServer>();

    LocateRegistry.createRegistry(2001);
    Naming.bind(jvnCoordURL, this);
	}

  public static void main(String[] args) {
    try {
      new JvnCoordImpl();
      System.out.println("JvnCoordImpl ready");
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
    System.out.println("JVC - jvnGetObjectId appelé");
    System.out.println("JVC - jvnGetObjectId retourne " + compteurIdUnique);
    return compteurIdUnique++;
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
    System.out.println("JVC - jvnRegisterObject de nom " + jon + " et d'id " + jo.jvnGetObjectId());

    if (mapNomId.containsKey(jon)) {
      System.out.println("JVC - jvnRegisterObject : le nom " + jon + " est déjà utilisé");
      return;
    }

    mapNomId.put(jon, jo.jvnGetObjectId());
    mapIdObject.put(jo.jvnGetObjectId(), jo);
    mapIdServeurEcriture.put(jo.jvnGetObjectId(), js);
  }
  
  /**
  * Get the reference of a JVN object managed by a given JVN server 
  * @param jon : the JVN object name
  * @param js : the remote reference of the JVNServer
  * @throws java.rmi.RemoteException,JvnException
  **/
  public JvnObject jvnLookupObject(String jon, JvnRemoteServer js)
  throws java.rmi.RemoteException,jvn.JvnException{
    
    if (!mapNomId.containsKey(jon)) {
      System.out.println("JVC - jvnLookupObject : le nom " + jon + " n'existe pas");
      return null;
    }

    int id = mapNomId.get(jon);
    ((JvnObjectImpl) mapIdObject.get(id)).setEtatVerrou(EtatVerrou.NL);
    System.out.println("JVC - Verrou after lookup OK : " + ((JvnObjectImpl) mapIdObject.get(id)).getEtatVerrou());
    return mapIdObject.get(id);
  }
  
  /**
  * Get a Read lock on a JVN object managed by a given JVN server 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the server
  * @return the current JVN object state
  * @throws java.rmi.RemoteException, JvnException
  **/
  public synchronized Serializable jvnLockRead(int joi, JvnRemoteServer js)
  throws java.rmi.RemoteException, JvnException {
    System.out.println("JVC - jvnLockRead of object " + joi);
    System.out.println("JVC - jvnLockRead state before: " + ((JvnObjectImpl) mapIdObject.get(joi)).getEtatVerrou());

    JvnObject jo = mapIdObject.get(joi);

    if (!mapIdListeServeursLecture.containsKey(joi)) {
      mapIdListeServeursLecture.put(joi, new ArrayList<JvnRemoteServer>());
    }

    boolean possedeVerrouEnLecture = mapIdListeServeursLecture.get(joi).contains(js);
    boolean possedeVerrouEnEcriture = mapIdServeurEcriture.get(joi) != null && mapIdServeurEcriture.get(joi).equals(js);

    Serializable currentState = jo.jvnGetSharedObject();

    for(Iterator<JvnRemoteServer> it = mapIdListeServeursLecture.get(joi).iterator(); it.hasNext(); ) {
      JvnRemoteServer jServer = it.next();
      if (!jServer.equals(js)) {
        jServer.jvnInvalidateReader(joi);
        it.remove();
      }
    }

    if (mapIdServeurEcriture.get(joi) != null && !possedeVerrouEnEcriture) {
      currentState = mapIdServeurEcriture.get(joi).jvnInvalidateWriterForReader(joi);
      ((JvnObjectImpl) jo).jvnSetSharedObject(currentState);
      mapIdObject.put(joi, jo);
      //mapIdServeurEcriture.put(joi, null);
    }

    if (!possedeVerrouEnLecture) {
      mapIdListeServeursLecture.get(joi).add(js);
    }

    System.out.println("JVC - jvnLockRead state after: " + ((JvnObjectImpl) mapIdObject.get(joi)).getEtatVerrou());

    return currentState;
}

  /**
  * Get a Write lock on a JVN object managed by a given JVN server 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the server
  * @return the current JVN object state
  * @throws java.rmi.RemoteException, JvnException
  **/
  public synchronized Serializable jvnLockWrite(int joi, JvnRemoteServer js)
  throws java.rmi.RemoteException, JvnException {
    System.out.println("JVC - jvnLockWrite of object " + joi);
    System.out.println("JVC - jvnLockWrite state before: " + ((JvnObjectImpl) mapIdObject.get(joi)).getEtatVerrou());

    JvnObject jo = mapIdObject.get(joi);

    boolean possedeVerrouEnEcriture = mapIdServeurEcriture.get(joi) != null && mapIdServeurEcriture.get(joi).equals(js);

    Serializable currentState = jo.jvnGetSharedObject();

    if (!possedeVerrouEnEcriture) {
      currentState = mapIdServeurEcriture.get(joi).jvnInvalidateWriter(joi);
      ((JvnObjectImpl) jo).jvnSetSharedObject(currentState);
      mapIdObject.put(joi, jo);
      mapIdServeurEcriture.put(joi, js);
    }

    System.out.println("JVC - jvnLockWrite state after: " + ((JvnObjectImpl) mapIdObject.get(joi)).getEtatVerrou());

    return currentState;
  }

	/**
	* A JVN server terminates
	* @param js  : the remote reference of the server
	* @throws java.rmi.RemoteException, JvnException
	**/
    public void jvnTerminate(JvnRemoteServer js)
	 throws java.rmi.RemoteException, JvnException {
    System.out.println("JVC - jvnTerminate");

    for(Iterator<Integer> it = mapIdObject.keySet().iterator(); it.hasNext(); ) {
      int id = it.next();
      for(Iterator<JvnRemoteServer> it2 = mapIdListeServeursLecture.get(id).iterator(); it2.hasNext(); ) {
        JvnRemoteServer jServer = it2.next();
        if (jServer.equals(js)) {
          jServer.jvnInvalidateReader(id);
          it2.remove();
        }
      }

      if (mapIdServeurEcriture.get(id) != null && mapIdServeurEcriture.get(id).equals(js)) {
        mapIdServeurEcriture.get(id).jvnInvalidateWriter(id);
        mapIdServeurEcriture.remove(id);
      }
    }
  } 

}


 
