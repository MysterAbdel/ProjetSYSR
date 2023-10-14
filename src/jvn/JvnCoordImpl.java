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
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
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


  private HashMap<JvnRemoteServer,ArrayList<JvnObjectImpl>> objetsPartages;
  private HashMap<Integer,ArrayList<JvnRemoteServer>> rsAvecVerrouSurObjet;

  // nomSymbolique -> idObjet
  private HashMap<String,Integer> idParNom;
  // idObjet -> objet
  private HashMap<Integer,JvnObjectImpl> objetsParId;

  private String jvnCoordURL = "//localhost:2001/JvnCoordinatorLink";

/**
  * Default constructor
  * @throws JvnException
  **/
	private JvnCoordImpl() throws Exception {
    super();
    
    objetsPartages = new HashMap<JvnRemoteServer,ArrayList<JvnObjectImpl>>();
    rsAvecVerrouSurObjet = new HashMap<Integer,ArrayList<JvnRemoteServer>>();

    idParNom = new HashMap<String,Integer>();
    objetsParId = new HashMap<Integer,JvnObjectImpl>();

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
      objetsPartages.put(js, new ArrayList<JvnObjectImpl>());
    }

    if(objetsPartages.get(js).contains(jo)){
      throw new JvnException("Objet déja existant");
    }

    objetsPartages.get(js).add((JvnObjectImpl) jo);
    
    // sauvegarder l'id et son nom
    if (!idParNom.containsKey(jon)) {
      idParNom.put(jon, jo.jvnGetObjectId());
    }

    // sauvegarder l'id et l'objet
    if(!objetsParId.containsKey(jo.jvnGetObjectId())) {
      objetsParId.put(jo.jvnGetObjectId(), (JvnObjectImpl) jo);
    }

    // sauvegarder l'id et l'état du verrou // liste des serveurs qui ont le verrou
    if (!rsAvecVerrouSurObjet.containsKey(jo.jvnGetObjectId())) {
      rsAvecVerrouSurObjet.put(jo.jvnGetObjectId(), new ArrayList<JvnRemoteServer>());
      rsAvecVerrouSurObjet.get(jo.jvnGetObjectId()).add(js);
    }

    System.out.println("Apres jvnRegisterObject : ");
    System.out.println("Objets partagés : "+objetsPartages);
    System.out.println("Etats verrous : "+rsAvecVerrouSurObjet);
    System.out.println("Id noms : "+idParNom);
    System.out.println("Id objects : "+objetsParId);
    System.out.println("----------------------");

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

    try {
        int idObjet = idParNom.get(jon);
        JvnObjectImpl objet = objetsParId.get(idObjet);

        if (objet == null)
            System.out.println("JVC - jvnLookupObject : objet non trouvé");

        return (JvnObject) objet;
    } catch (Exception e) {
        e.printStackTrace();
        return null;
    }
  }
  
  /**
  * Get a Read lock on a JVN object managed by a given JVN server 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the server
  * @return the current JVN object state
  * @throws java.rmi.RemoteException, JvnException
  **/
  public synchronized Serializable jvnLockRead(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
    
    System.out.println("JVC - Appel a lockRead");

    ArrayList <JvnRemoteServer> rsAvecVerrou = rsAvecVerrouSurObjet.get(joi);
    JvnObjectImpl objet = objetsParId.get(joi);

    System.out.println("JVC - lockRead : etat verrou avant  : "+ objet.getEtatVerrou());

    for (JvnRemoteServer rs : rsAvecVerrou) {
        Serializable o = rs.jvnInvalidateWriterForReader(joi);
        objet.setObjectState(o);
        objet.setEtatVerrou(EtatVerrou.R);
        rsAvecVerrou.add(js);
        break;
    }

    rsAvecVerrouSurObjet.put(joi, rsAvecVerrou);
    objetsParId.put(joi, objet);

    System.out.println("JVC - lockRead : etat verrou apres  : "+ objet.getEtatVerrou());
    return objet.jvnGetSharedObject();
}


  /**
  * Get a Write lock on a JVN object managed by a given JVN server 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the server
  * @return the current JVN object state
  * @throws java.rmi.RemoteException, JvnException
  **/
  public synchronized Serializable jvnLockWrite(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
    
    System.out.println("JVC - Appel a lockWrite ");

    ArrayList <JvnRemoteServer> rsAvecVerrou = rsAvecVerrouSurObjet.get(joi);
    JvnObjectImpl objet = objetsParId.get(joi);

    System.out.println("JVC - lockWrite : etat verrou avant "+ objet.getEtatVerrou());

    for (JvnRemoteServer rs : rsAvecVerrou) {
        Serializable o = rs.jvnInvalidateWriter(joi);
        js.jvnInvalidateReader(joi);
        objet.setEtatVerrou(EtatVerrou.W);
        objet.setObjectState(o);
        rsAvecVerrou.add(js);
        break;
    }

    System.out.println("JVC - lockWrite : etat verrou apres "+ objet.getEtatVerrou());
    
    rsAvecVerrouSurObjet.put(joi, rsAvecVerrou);
    objetsParId.put(joi, objet);
    return objet.jvnGetSharedObject();
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

 
