package jvn;

import java.io.Serializable;
import java.rmi.Remote;

public class JvnObjectImpl implements JvnObject, Remote{

    private long id;
    private Serializable objectState;


    /* << source : diapos >>
        ❖ NL   : no lock
        ❖ RC   : read lock cached (not currently used)
        ❖ WC   : write lock cached (not currently used)
        ❖ R    : read lock taken
        ❖ W    : write lock taken
        ❖ RWC  : write lock cached & read taken
     */

    private EtatVerrou etatVerrou;

    public JvnObjectImpl(){}
    
    public JvnObjectImpl(Serializable o, int id){
        System.out.println("IMPL - Création d'un objet JvnObjectImpl avec id : " + id);
        setEtatVerrou(EtatVerrou.NL);
        setObjectState(o);
        setUniqueId(id);
    }

    public void setUniqueId(long id){
        this.id = id;
    }

    public void setEtatVerrou(EtatVerrou etatVerrou){
        this.etatVerrou = etatVerrou;
    }

    public EtatVerrou getEtatVerrou(){
        return this.etatVerrou;
    }

    public void setObjectState(Serializable objectState){
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

        System.out.println("IMPL - jvnInvalidateReader sur " + this.id);

        if (this.etatVerrou == EtatVerrou.R){
            try {
                System.out.println("mise en attente sur " + this.id);
                wait();
                this.etatVerrou = EtatVerrou.RC;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }  
    }

    @Override
    public Serializable jvnInvalidateWriter() throws JvnException {
        
        System.out.println("IMPL - jvnInvalidateWriter sur " + this.id);

        if (this.etatVerrou == EtatVerrou.W){
            try {
                System.out.println("mise en attente sur " + this.id);
                wait();
                this.etatVerrou = EtatVerrou.NL;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return objectState;
    }

    @Override
    public Serializable jvnInvalidateWriterForReader() throws JvnException {
        
        System.out.println("IMPL - jvnInvalidateWriterForReader sur " + this.id);

        if (this.etatVerrou == EtatVerrou.W){
            try {
                System.out.println("mise en attente sur " + this.id);
                wait();
                this.etatVerrou = EtatVerrou.NL;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return objectState;
    }

    @Override
    public void jvnLockRead() throws JvnException {

        System.out.println("IMPL - jvnLockRead sur " + this.id);

        if (this.etatVerrou == EtatVerrou.NL || this.etatVerrou == EtatVerrou.RC){
            this.etatVerrou = EtatVerrou.R;
            return;
        }

        if (this.etatVerrou == EtatVerrou.W || this.etatVerrou == EtatVerrou.WC) {
            this.etatVerrou = EtatVerrou.RWC;
            return;
        }

        throw new JvnException("Erreur : etat incompatible");
    }

    @Override
    public void jvnLockWrite() throws JvnException {

        System.out.println("IMPL - jvnLockWrite sur " + this.id);

        if (this.etatVerrou == EtatVerrou.NL 
        || this.etatVerrou == EtatVerrou.RC
        || this.etatVerrou == EtatVerrou.R
        || this.etatVerrou == EtatVerrou.WC
        || this.etatVerrou == EtatVerrou.RWC){

            
            this.etatVerrou = EtatVerrou.W; 
            return;
        }

        throw new JvnException("Erreur : etat incompatible");
    }

    @Override
    public void jvnUnLock() throws JvnException {
        
        System.out.println("IMPL - jvnUnLock sur " + this.id);

        if (this.etatVerrou == EtatVerrou.W || this.etatVerrou == EtatVerrou.R){
            this.etatVerrou = EtatVerrou.NL;
        }
        if (this.etatVerrou == EtatVerrou.NL){
            return;
        }
        notify();
    }

    
}
