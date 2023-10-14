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
import java.util.List;


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
      throw new JvnException("Le nom " + jon + " est déjà utilisé");
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
      throw new JvnException("Le nom " + jon + " n'existe pas");
    }

    int id = mapNomId.get(jon);
    return mapIdObject.get(id);
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

    /**
     * // Hashmap Nom/Id
        private HashMap<String, Integer> mapNomId;
        // Hashmap Id/Object
        private HashMap<Integer, JvnObject> mapIdObject;
        
        // Hashmap Id/Liste_serveurs_ayant_le_lock_en_lecture
        private HashMap<Integer, List<JvnRemoteServer>> mapIdListeServeursLecture;

        // Hashmap Id/Serveur_ayant_le_lock_en_ecriture
        private HashMap<Integer, JvnRemoteServer> mapIdServeurEcriture;
     */

    JvnObject jo = mapIdObject.get(joi);

    boolean possedeVerrouEnLecture = mapIdListeServeursLecture.get(joi).contains(js);
    boolean possedeVerrouEnEcriture = mapIdServeurEcriture.get(joi).equals(js);

    
    if (possedeVerrouEnEcriture){
      JvnRemoteServer jServer = mapIdServeurEcriture.get(joi);
      Serializable o = jServer.jvnInvalidateWriterForReader(joi);
      ((JvnObjectImpl) jo).jvnSetSharedObject(o);
      mapIdListeServeursLecture.get(joi).add(jServer);
      mapIdObject.put(joi, jo);
    }

    if (!possedeVerrouEnLecture){
      mapIdListeServeursLecture.get(joi).add(js);
    }

    return jo;
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

    /**
     * // Hashmap Nom/Id
        private HashMap<String, Integer> mapNomId;
        // Hashmap Id/Object
        private HashMap<Integer, JvnObject> mapIdObject;
        
        // Hashmap Id/Liste_serveurs_ayant_le_lock_en_lecture
        private HashMap<Integer, List<JvnRemoteServer>> mapIdListeServeursLecture;

        // Hashmap Id/Serveur_ayant_le_lock_en_ecriture
        private HashMap<Integer, JvnRemoteServer> mapIdServeurEcriture;
     */

    JvnObject jo = mapIdObject.get(joi);

    boolean possedeVerrouEnLecture = mapIdListeServeursLecture.get(joi).contains(js);
    boolean possedeVerrouEnEcriture = mapIdServeurEcriture.get(joi).equals(js);

    
    if (!possedeVerrouEnEcriture){
        JvnRemoteServer jServer = mapIdServeurEcriture.get(joi);
        Serializable o = jServer.jvnInvalidateWriter(joi);
        ((JvnObjectImpl) jo).jvnSetSharedObject(o);
        mapIdServeurEcriture.put(joi, js);
        mapIdObject.put(joi, jo);
    }

    for (JvnRemoteServer jServer : mapIdListeServeursLecture.get(joi)) {
      if (!jServer.equals(js)){
        jServer.jvnInvalidateReader(joi);
        mapIdListeServeursLecture.get(joi).remove(jServer);
      }
    }

    return jo;
   }

	/**
	* A JVN server terminates
	* @param js  : the remote reference of the server
	* @throws java.rmi.RemoteException, JvnException
	**/
    public void jvnTerminate(JvnRemoteServer js)
	 throws java.rmi.RemoteException, JvnException {
	 // to be completed
    }
}

 
