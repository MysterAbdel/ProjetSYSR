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
import java.util.HashSet;
import java.util.Iterator;


public class   JvnCoordImpl
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
  private HashMap<Integer, HashSet<JvnRemoteServer>> mapIdListeServeursLecture;

  // Hashmap Id/Serveur_ayant_le_lock_en_ecriture
  private HashMap<Integer, JvnRemoteServer> mapIdServeurEcriture;

  //EXTENSION [CACHE CLIENT]: taille du cache d'un serveur
  //Idée : avant d'ajouter l'objet au js via jvnLockRead ou jvnLockWrite, on vérifie si le cache est plein
  //Si oui, on fait un jvnFlush qui devrait invalider les existants (mettre les RC et WC à NL)
  //et vider le tableau des objets locaux sur le js
  private int tailleCache = 0;

  
/**
  * Default constructor
  * @throws JvnException
  **/
	private JvnCoordImpl(String[] args) throws Exception {
    super();

    mapNomId = new HashMap<String, Integer>();
    mapIdObject = new HashMap<Integer, JvnObject>();
    mapIdListeServeursLecture = new HashMap<>();
    mapIdServeurEcriture = new HashMap<Integer, JvnRemoteServer>();

    // argument 0 : taille du cache des serveurs
    if (args.length > 0) {
      tailleCache = Integer.parseInt(args[0]);
    }

    LocateRegistry.createRegistry(2001);
    Naming.bind(jvnCoordURL, this);
	}

  public static void main(String[] args) {
    try {
      //-- EXTENSION [PANNE COORDINATEUR]
      JvnCoordImpl jci = JvnCoordCache.getJvnCoordCache().loadCoordinatorFromCache(); 
      if (jci != null) {
        System.out.println("JVC - Coordinator loaded from cache");
        LocateRegistry.createRegistry(2001);
        Naming.bind("//localhost:2001/JvnCoord", jci);
      } else {
        System.out.println("JVC - No cache found");
        new JvnCoordImpl(args);
      }
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
    mapIdListeServeursLecture.put(jo.jvnGetObjectId(), new HashSet<JvnRemoteServer>());

    JvnCoordCache.getJvnCoordCache().saveCoordinatorIntoCache(this);
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

    JvnCoordCache.getJvnCoordCache().saveCoordinatorIntoCache(this);
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
		    Serializable etat = jo.jvnGetSharedObject();

		    JvnRemoteServer possesseurEnEcriture = mapIdServeurEcriture.get(joi);

		    if (possesseurEnEcriture!=null) {
		        try {
		            etat=possesseurEnEcriture.jvnInvalidateWriterForReader(joi);
		        } catch (Exception e) {

		        }

		        if(possesseurEnEcriture.equals(mapIdServeurEcriture.get(joi))) {
		        	System.out.println("LockRead------"+mapIdListeServeursLecture.get(joi));
		            mapIdListeServeursLecture.get(joi).add(possesseurEnEcriture);

		            }
		    }

		    ((JvnObjectImpl) jo).jvnSetSharedObject(etat);
		    mapIdListeServeursLecture.get(joi).add(js);
		    mapIdServeurEcriture.put(joi, null);

		    System.out.println("JVC - jvnLockRead state after: " + ((JvnObjectImpl) mapIdObject.get(joi)).getEtatVerrou());

        JvnCoordCache.getJvnCoordCache().saveCoordinatorIntoCache(this);
        return etat;
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

		    Serializable ret = null;

		    JvnRemoteServer server = null;

		        jo = mapIdObject.get(joi);
		        ret = jo.jvnGetSharedObject();
		        server = mapIdServeurEcriture.get(joi);

		        if (server != null) {
		            try {
		                ret = server.jvnInvalidateWriter(joi);
		            } catch (Exception e) {
		            }
		            if (server.equals(mapIdServeurEcriture.get(joi))) {
		            	System.out.println("LockWrite------"+mapIdListeServeursLecture.get(joi));
		                 mapIdListeServeursLecture.get(joi).add(server);
		            }
		        }
		        ArrayList<JvnRemoteServer> readerList = new ArrayList<>();
		        ((JvnObjectImpl) jo).jvnSetSharedObject(ret);
		        //jo.jvn(ret);
		        // saveState();
		        Iterator<JvnRemoteServer> it = mapIdListeServeursLecture.get(joi).iterator();
		        while (it.hasNext()) {
		            JvnRemoteServer current = it.next();
		            if (!current.equals(js)) {
		                readerList.add(current);
		            }
		        }
		        for (JvnRemoteServer s : readerList) {
		            try {
		                s.jvnInvalidateReader(joi);
		            } catch (Exception e) {
		            }
		        }
		        // Plus personne ne doit pouvoir �tre en mesure de lire
		        mapIdListeServeursLecture.get(joi).clear();
		        mapIdServeurEcriture.put(joi, js);

        JvnCoordCache.getJvnCoordCache().saveCoordinatorIntoCache(this);    
		    return ret;

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

  /**
	* A JVN server flushes its LOCAL cache - EXTENSION [CACHE CLIENT]
	* @param js  : the remote reference of the server
	* @throws java.rmi.RemoteException, JvnException
	**/
    public void jvnFlush(JvnRemoteServer js)
	 throws java.rmi.RemoteException, JvnException {
    System.out.println("JVC - jvnFlush");

    for(Iterator<Integer> it = mapIdObject.keySet().iterator(); it.hasNext(); ) {
      int id = it.next();
      for(Iterator<JvnRemoteServer> it2 = mapIdListeServeursLecture.get(id).iterator(); it2.hasNext(); ) {
        JvnRemoteServer jServer = it2.next();
        if (jServer.equals(js)) {
          jServer.jvnInvalidateReader(id);
        }
      }

      if (mapIdServeurEcriture.get(id) != null && mapIdServeurEcriture.get(id).equals(js)) {
        mapIdServeurEcriture.get(id).jvnInvalidateWriter(id);
      }
    }

    ((JvnServerImpl) js).flushLocalObjects();
  } 

  public void jvnPrintStats(){
    System.out.println("------| JvnCoordinator STATS |----------");
    System.out.println("JVC - Nombre d'objets en cache : " + mapIdObject.size());
    System.out.println("JVC - Noms symboliques enregistrés : " + mapNomId.keySet());
    System.out.println("----------------------------------------");
  }

}



 
