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

// package jvn;

// import java.io.Serializable;
// import java.rmi.Remote;
// import static jvn.JvnServerImpl.jvnGetServer;

// public class JvnObjectImpl implements JvnObject, Remote{

//     private int id;
//     private Serializable objectState;


//     /* << source : diapos >>
//         ❖ NL   : no lock
//         ❖ RC   : read lock cached (not currently used)
//         ❖ WC   : write lock cached (not currently used)
//         ❖ R    : read lock taken
//         ❖ W    : write lock taken
//         ❖ RWC  : write lock cached & read taken
//      */

//     private EtatVerrou etatVerrou;

//     public JvnObjectImpl(){}
    
//     public JvnObjectImpl(Serializable o, int id){
//         System.out.println("IMPL - Création d'un objet JvnObjectImpl avec id : " + id);
//         setEtatVerrou(EtatVerrou.W);
//         setObjectState(o);
//         setUniqueId(id);
//     }

//     public void setUniqueId(int id){
//         this.id = id;
//     }

//     public void setEtatVerrou(EtatVerrou etatVerrou){
//         this.etatVerrou = etatVerrou;
//     }

//     public EtatVerrou getEtatVerrou(){
//         return this.etatVerrou;
//     }

//     public void setObjectState(Serializable objectState){
//         this.objectState = objectState;
//     }

//     @Override
//     public int jvnGetObjectId() throws JvnException {
//         return (int) this.id;
//     }

//     @Override
//     public synchronized Serializable jvnGetSharedObject() throws JvnException {
//         return this.objectState;
//     }

//     @Override
//     public synchronized void jvnInvalidateReader() throws JvnException {

//         System.out.println("IMPL - jvnInvalidateReader sur " + this.id);

//         while(this.etatVerrou == EtatVerrou.R || this.etatVerrou == EtatVerrou.RWC) {
//             try {
//                 System.out.println("mise en attente sur " + this.id);
//                 wait();
//             } catch (InterruptedException e) {
//                 e.printStackTrace();
//             }
//         }
//         this.etatVerrou = EtatVerrou.NL;

//     }

//     @Override
//     public synchronized Serializable jvnInvalidateWriter() throws JvnException {
        
//         System.out.println("IMPL - jvnInvalidateWriter sur " + this.id);

//         while(this.etatVerrou == EtatVerrou.W) {
//             try {
//                 System.out.println("mise en attente sur " + this.id);
//                 wait();
                
//             } catch (InterruptedException e) {
//                 e.printStackTrace();
//             }
//         }
//         this.etatVerrou = EtatVerrou.NL;
//         return this.objectState;
//     }

//     @Override
//     public synchronized Serializable jvnInvalidateWriterForReader() throws JvnException {
        
//         System.out.println("IMPL - jvnInvalidateWriterForReader sur " + this.id);

//         switch(this.etatVerrou){
//             case W :
//                 while(this.etatVerrou == EtatVerrou.W) {
//                     try {
//                         System.out.println("mise en attente sur " + this.id);
//                         wait();
//                     } catch (InterruptedException e) {
//                         e.printStackTrace();
//                     }
//                     this.etatVerrou = EtatVerrou.RC;
//                 }
//                 break;
//             case RWC :
//                 while(this.etatVerrou == EtatVerrou.RWC) {
//                     try {
//                         System.out.println("mise en attente sur " + this.id);
//                         wait();
                        
//                     } catch (InterruptedException e) {
//                         e.printStackTrace();
//                     }
//                     this.etatVerrou = EtatVerrou.R;
//                 }
//                 break;
//             default :
//                 throw new JvnException("Erreur : etat incompatible");
//         }

//         return objectState;
//     }

//     @Override
//     public synchronized void jvnLockRead() throws JvnException {

//         System.out.println("IMPL - jvnLockRead sur " + this.id);
//         System.out.println("---- IMPL -  Etat verrou avant LockRead : " + this.etatVerrou + " ----");
        
//         switch(this.etatVerrou){
//             case NL:
//                 objectState = jvnGetServer().jvnLockRead(this.id);
//                 this.etatVerrou = EtatVerrou.R;
//                 break;
//             case RC:
//                 this.etatVerrou = EtatVerrou.R;
//                 break;
//             case WC:
//                 this.etatVerrou = EtatVerrou.RWC;
//                 break;
//             case R:
//                 break;
//             case W:
//                 objectState = jvnGetServer().jvnLockRead(this.id);
//                 this.etatVerrou = EtatVerrou.RWC;
//                 break;
//             case RWC:
//                 break;
//             default :
//                 throw new JvnException("Erreur : etat incompatible");
//         }

//         System.out.println("---- IMPL -  Etat verrou après LockRead : " + this.etatVerrou + " ----");

//     }

//     @Override
//     public synchronized void jvnLockWrite() throws JvnException {

//         System.out.println("IMPL - jvnLockWrite sur " + this.id);
//         System.out.println("---- IMPL -  Etat verrou avant LockWrite : " + this.etatVerrou + " ----");
        
//         switch(this.etatVerrou){
//             case NL:
//                 objectState = jvnGetServer().jvnLockWrite(this.id);
//                 this.etatVerrou = EtatVerrou.W;
//                 break;
//             case RC:
//                 objectState = jvnGetServer().jvnLockWrite(this.id);
//                 this.etatVerrou = EtatVerrou.W;
//                 break;
//             case WC:
//                 this.etatVerrou = EtatVerrou.W;
//                 break;
//             case R:
//                 objectState = jvnGetServer().jvnLockWrite(this.id);
//                 this.etatVerrou = EtatVerrou.W;
//                 break;
//             case W:
//                 break;
//             case RWC:
//                 objectState = jvnGetServer().jvnLockWrite(this.id);
//                 this.etatVerrou = EtatVerrou.W;
//                 break;
//             default :
//                 throw new JvnException("Erreur : etat incompatible");
//         }
        
//         System.out.println("---- IMPL -  Etat verrou après LockWrite : " + this.etatVerrou + " ----");
//     }

//     @Override
//     public synchronized void jvnUnLock() throws JvnException {
        
//         System.out.println("IMPL - jvnUnLock sur " + this.id);
//         System.out.println("---- IMPL -  Etat verrou avant UnLock : " + this.etatVerrou + " ----");

//         switch(this.etatVerrou){
//             case NL:
//                 break;
//             case RC:
//                 break;
//             case WC:
//                 break;
//             case R:
//                 this.etatVerrou = EtatVerrou.RC;
//                 //notify();
//                 break;
//             case W:
//                 this.etatVerrou = EtatVerrou.WC;
//                 //notify();
//                 break;
//             case RWC:
//                 this.etatVerrou = EtatVerrou.WC;
//                 //notify();
//                 break;
//             default :
//                 throw new JvnException("Erreur : etat incompatible");
//         }
//         notify();

//         System.out.println("---- IMPL -  Etat verrou après UnLock : " + this.etatVerrou + " ----");
//     }


    
// }

  
  /**
  * Get a Read lock on a JVN object managed by a given JVN server 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the server
  * @return the current JVN object state
  * @throws java.rmi.RemoteException, JvnException
  **/
  public synchronized Serializable jvnLockRead(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
    return null;
}


  /**
  * Get a Write lock on a JVN object managed by a given JVN server 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the server
  * @return the current JVN object state
  * @throws java.rmi.RemoteException, JvnException
  **/
  public synchronized Serializable jvnLockWrite(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
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

 
