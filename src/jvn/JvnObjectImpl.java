package jvn;

import java.io.Serializable;
import java.rmi.Remote;

public class JvnObjectImpl implements Remote , JvnObject{

    // identifiant unique
    private int idUnique;

    // dernier état validé de l'objet
    private Serializable objetPartage;

    // etat du verrou
    private EtatVerrou etatVerrou;

    public JvnObjectImpl(int idUnique, Serializable objetPartage) {
        this.idUnique = idUnique;
        this.objetPartage = objetPartage;
        this.etatVerrou = EtatVerrou.W;
    }

    public synchronized EtatVerrou getEtatVerrou() {
        return etatVerrou;
    }

    @Override
    public synchronized int jvnGetObjectId() throws JvnException {
        return this.idUnique;
    }

    @Override
    public synchronized Serializable jvnGetSharedObject() throws JvnException {
        return this.objetPartage;
    }

    @Override
    public synchronized void jvnInvalidateReader() throws JvnException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public synchronized Serializable jvnInvalidateWriter() throws JvnException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public synchronized Serializable jvnInvalidateWriterForReader() throws JvnException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public synchronized void jvnLockRead() throws JvnException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public synchronized void jvnLockWrite() throws JvnException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public synchronized void jvnUnLock() throws JvnException {
        // TODO Auto-generated method stub
        
    }

    
    
}
