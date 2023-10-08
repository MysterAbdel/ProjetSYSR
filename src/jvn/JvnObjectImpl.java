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
        try {
            jvnLockWrite();
        } catch (JvnException e) {
            e.printStackTrace();
        }
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

        while(this.etatVerrou == EtatVerrou.R || this.etatVerrou == EtatVerrou.RWC) {
            try {
                System.out.println("mise en attente sur " + this.id);
                wait();
                this.etatVerrou = EtatVerrou.NL;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public Serializable jvnInvalidateWriter() throws JvnException {
        
        System.out.println("IMPL - jvnInvalidateWriter sur " + this.id);

        while(this.etatVerrou == EtatVerrou.W) {
            try {
                System.out.println("mise en attente sur " + this.id);
                wait();
                this.etatVerrou = EtatVerrou.NL;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return this.objectState;
    }

    @Override
    public Serializable jvnInvalidateWriterForReader() throws JvnException {
        
        System.out.println("IMPL - jvnInvalidateWriterForReader sur " + this.id);

        switch(this.etatVerrou){
            case W :
                while(this.etatVerrou == EtatVerrou.W) {
                    try {
                        System.out.println("mise en attente sur " + this.id);
                        wait();

                        this.etatVerrou = EtatVerrou.RC;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case RWC :
                while(this.etatVerrou == EtatVerrou.RWC) {
                    try {
                        System.out.println("mise en attente sur " + this.id);
                        wait();
                        this.etatVerrou = EtatVerrou.R;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                break;
            default :
                throw new JvnException("Erreur : etat incompatible");
        }

        return objectState;
    }

    @Override
    public void jvnLockRead() throws JvnException {

        System.out.println("IMPL - jvnLockRead sur " + this.id);

        switch(this.etatVerrou){
            case NL:
                this.etatVerrou = EtatVerrou.R;
                break;
            case RC:
                this.etatVerrou = EtatVerrou.R;
                break;
            case WC:
                this.etatVerrou = EtatVerrou.RWC;
                break;
            case R:
                break;
            case W:
                break;
            case RWC:
                break;
            default :
                throw new JvnException("Erreur : etat incompatible");
        }

    }

    @Override
    public void jvnLockWrite() throws JvnException {

        System.out.println("IMPL - jvnLockWrite sur " + this.id);

        synchronized(this){
            switch(this.etatVerrou){
            case NL:
                this.etatVerrou = EtatVerrou.W;
                break;
            case RC:
                this.etatVerrou = EtatVerrou.W;
                break;
            case WC:
                this.etatVerrou = EtatVerrou.W;
                break;
            case R:
                break;
            case W:
                break;
            case RWC:
                this.etatVerrou = EtatVerrou.W;
                break;
            default :
                throw new JvnException("Erreur : etat incompatible");
            }
        }
    }

    @Override
    public void jvnUnLock() throws JvnException {
        
        System.out.println("IMPL - jvnUnLock sur " + this.id);

        switch(this.etatVerrou){
            case NL:
                break;
            case RC:
                break;
            case WC:
                break;
            case R:
                this.etatVerrou = EtatVerrou.RC;
                notify();
                break;
            case W:
                this.etatVerrou = EtatVerrou.WC;
                notify();
                break;
            case RWC:
                this.etatVerrou = EtatVerrou.WC;
                notify();
                break;
            default :
                throw new JvnException("Erreur : etat incompatible");
        }

    }

    
}
