package jvn;

import java.io.Serializable;
import java.rmi.Remote;

public class JvnObjectImpl implements JvnObject, Remote{

    private long id;
    private Serializable objectState;

    enum EtatVerrou {
        NL, RC, WC, R, W, RWC
    };

    /* << source : diapos >>
        ❖ NL   : no lock
        ❖ RC   : read lock cached (not currently used)
        ❖ WC   : write lock cached (not currently used)
        ❖ R    : read lock taken
        ❖ W    : write lock taken
        ❖ RWC  : write lock cached & read taken
     */
    
    private EtatVerrou etatVerrou;

    public JvnObjectImpl(){
        this.etatVerrou = EtatVerrou.NL;
        this.objectState = this;
    }

    public void setUniqueId(long id){
        this.id = id;
    }

    public void setEtatVerrou(EtatVerrou etatVerrou){
        this.etatVerrou = etatVerrou;
    }

    public void setObjectState(JvnObjectImpl objectState){
        this.objectState = objectState;
    }

    @Override
    public int jvnGetObjectId() throws JvnException {
        return (int) this.id;
    }

    @Override
    public Serializable jvnGetSharedObject() throws JvnException {
        return this.objectState;
    }

    @Override
    public void jvnInvalidateReader() throws JvnException {
        // TODO Auto-generated method stub

        // IDEES
        
    }

    @Override
    public Serializable jvnInvalidateWriter() throws JvnException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Serializable jvnInvalidateWriterForReader() throws JvnException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void jvnLockRead() throws JvnException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void jvnLockWrite() throws JvnException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void jvnUnLock() throws JvnException {
        // TODO Auto-generated method stub
        
    }
    
}
