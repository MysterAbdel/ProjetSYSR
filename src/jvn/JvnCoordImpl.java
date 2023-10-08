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
  private HashMap<Integer,ArrayList<JvnRemoteServer>> etatsVerrous;
  private HashMap<Integer,String> idNoms;
  private HashMap<Integer,JvnObjectImpl> idObjects;

  private String jvnCoordURL = "//localhost:2001/JvnCoordinatorLink";

/**
  * Default constructor
  * @throws JvnException
  **/
	private JvnCoordImpl() throws Exception {
    super();
    
    objetsPartages = new HashMap<JvnRemoteServer,ArrayList<JvnObjectImpl>>();
    etatsVerrous = new HashMap<Integer,ArrayList<JvnRemoteServer>>();
    idNoms = new HashMap<Integer,String>();
    idObjects = new HashMap<Integer,JvnObjectImpl>();

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
  
  // public void updateEtatVerrou(int id, JvnRemoteServer js, EtatVerrou etatVerrou){
  //   if(!etatsVerrous.containsKey(id)){
  //     etatsVerrous.put(id, new HashMap<JvnRemoteServer,EtatVerrou>());
  //   }

  //   etatsVerrous.get(id).put(js, etatVerrou);
  // }

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
    if(!idNoms.containsKey(jo.jvnGetObjectId())) {
      idNoms.put(jo.jvnGetObjectId(), jon);
    }

    // sauvegarder l'id et l'objet
    if(!idObjects.containsKey(jo.jvnGetObjectId())) {
      idObjects.put(jo.jvnGetObjectId(), (JvnObjectImpl) jo);
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

    if(objetsPartages.containsKey(js)){
      for (JvnObjectImpl jvnObject : objetsPartages.get(js)) {
        if(idNoms.get(jvnObject.jvnGetObjectId()).equals(jon)){
          System.out.println("JVC - jvnLookupObject : objet trouvé");
          return jvnObject;
        }
      }
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

    System.out.println("JVC - appel à LockRead");

    Serializable objectState = null;

    // vérifier quel serveur a un verrou sur l'objet
    if(etatsVerrous.containsKey(joi)){
      for(JvnRemoteServer jvnRemoteServer : etatsVerrous.get(joi)){
        if(jvnRemoteServer != js){
          for(JvnObjectImpl jvnObjectImpl : objetsPartages.get(jvnRemoteServer)){
            switch(jvnObjectImpl.getEtatVerrou()){
              case R :
                jvnRemoteServer.jvnInvalidateReader(joi);
                objectState = jvnObjectImpl.jvnGetSharedObject();
                break;
              case RWC :
                objectState = jvnRemoteServer.jvnInvalidateWriterForReader(joi);
                break;
              default :
                throw new JvnException("Erreur de verrouillage");
            }
            break;
          }
          break;
        }
      }
    }

    idObjects.get(joi).setObjectState(objectState);
    idObjects.get(joi).jvnLockRead();

    if(!etatsVerrous.containsKey(joi)){
      etatsVerrous.put(joi, new ArrayList<JvnRemoteServer>());
    } 

    if(!etatsVerrous.get(joi).contains(js)){
      etatsVerrous.get(joi).add(js);
    }

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

    // vérifier quel serveur a un verrou sur l'objet
    if(etatsVerrous.containsKey(joi)){
      for(JvnRemoteServer jvnRemoteServer : etatsVerrous.get(joi)){
        if(jvnRemoteServer != js){
          for(JvnObjectImpl jvnObjectImpl : objetsPartages.get(jvnRemoteServer)){
            switch(jvnObjectImpl.getEtatVerrou()){
              case R :
                jvnRemoteServer.jvnInvalidateReader(joi);
                objectState = jvnObjectImpl.jvnGetSharedObject();
                break;
              case W :
                objectState = jvnRemoteServer.jvnInvalidateWriter(joi);
                break;
              default :
                throw new JvnException("Erreur de verrouillage");
            }
            break;
          }
          break;
        }
      }
    }

    idObjects.get(joi).setObjectState(objectState);
    idObjects.get(joi).jvnLockWrite();

    if(!etatsVerrous.containsKey(joi)){
      etatsVerrous.put(joi, new ArrayList<JvnRemoteServer>());
    } 

    if(!etatsVerrous.get(joi).contains(js)){
      etatsVerrous.get(joi).add(js);
    }

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

 
