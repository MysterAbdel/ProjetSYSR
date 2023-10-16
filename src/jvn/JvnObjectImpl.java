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

    public synchronized void setEtatVerrou(EtatVerrou etatVerrou) {
        this.etatVerrou = etatVerrou;
    }

    @Override
    public synchronized int jvnGetObjectId() throws JvnException {
        return this.idUnique;
    }

    @Override
    public synchronized Serializable jvnGetSharedObject() throws JvnException {
        return this.objetPartage;
    }

    public synchronized void jvnSetSharedObject(Serializable objetPartage) throws JvnException {
        this.objetPartage = objetPartage;
    }

    @Override
    public synchronized void jvnInvalidateReader() throws JvnException {
        System.out.println("IMPL - jvnInvalidateReader appelé sur l'objet " + this.idUnique + " avec l'état " + this.etatVerrou + "");
        
        if (this.etatVerrou == EtatVerrou.RC){
            this.etatVerrou = EtatVerrou.NL;
            System.out.println("IMPL - etat après jvnInvalidateReader : " + this.etatVerrou + "");
            return;
        }

        while(this.etatVerrou == EtatVerrou.R || this.etatVerrou == EtatVerrou.RWC){
            try {
                wait();
                this.etatVerrou = EtatVerrou.NL;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("IMPL - etat après jvnInvalidateReader : " + this.etatVerrou + "");
    }

    @Override
    public synchronized Serializable jvnInvalidateWriter() throws JvnException {
        System.out.println("IMPL - jvnInvalidateWriter appelé sur l'objet " + this.idUnique + " avec l'état " + this.etatVerrou + "");

        if (this.etatVerrou == EtatVerrou.WC){
            this.etatVerrou = EtatVerrou.NL;
            System.out.println("IMPL - etat après jvnInvalidateWriter : " + this.etatVerrou + "");
            return this.objetPartage;
        }

        if (this.etatVerrou == EtatVerrou.RWC){
            this.etatVerrou = EtatVerrou.R;
            System.out.println("IMPL - etat après jvnInvalidateWriter : " + this.etatVerrou + "");
            return this.objetPartage;
        }

        while(this.etatVerrou == EtatVerrou.W){
            try {
                wait();
                this.etatVerrou = EtatVerrou.NL;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("IMPL - etat après jvnInvalidateWriter : " + this.etatVerrou + "");
        return this.objetPartage;
    }

    @Override
    public synchronized Serializable jvnInvalidateWriterForReader() throws JvnException {
        System.out.println("IMPL - jvnInvalidateWriterForReader appelé sur l'objet " + this.idUnique + " avec l'état " + this.etatVerrou + "");

        if (this.etatVerrou == EtatVerrou.RWC || this.etatVerrou == EtatVerrou.WC){
            this.etatVerrou = EtatVerrou.R;
            System.out.println("IMPL - etat après jvnInvalidateWriterForReader : " + this.etatVerrou + "");
            return this.objetPartage;
        }

        while(this.etatVerrou == EtatVerrou.W){
            try {
                wait();
                this.etatVerrou = EtatVerrou.RC;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("IMPL - etat après jvnInvalidateWriterForReader : " + this.etatVerrou + "");
        return this.objetPartage;
    }

    @Override
    public synchronized void jvnLockRead() throws JvnException {
        System.out.println("IMPL - jvnLockRead appelé sur l'objet " + this.idUnique + " avec l'état " + this.etatVerrou + "");

        //objetPartage = JvnServerImpl.jvnGetServer().jvnLockRead(this.idUnique);

        switch(this.etatVerrou){
            case NL:
                objetPartage = JvnServerImpl.jvnGetServer().jvnLockRead(this.idUnique);
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
                this.etatVerrou = EtatVerrou.RWC;
                break;
            case RWC:
                break;
            default:
                break;
        }


        System.out.println("IMPL - etat après jvnLockRead : " + this.etatVerrou + "");
    }

    @Override
    public synchronized void jvnLockWrite() throws JvnException {
        System.out.println("IMPL - jvnLockWrite appelé sur l'objet " + this.idUnique + " avec l'état " + this.etatVerrou + "");

        // objetPartage = JvnServerImpl.jvnGetServer().jvnLockWrite(this.idUnique);

        switch (this.etatVerrou) {
            case NL:
                objetPartage = JvnServerImpl.jvnGetServer().jvnLockWrite(this.idUnique);
                this.etatVerrou = EtatVerrou.W;
                break;
            case RC:
                objetPartage = JvnServerImpl.jvnGetServer().jvnLockWrite(this.idUnique);
                this.etatVerrou = EtatVerrou.W;
                break;
            case WC:
                this.etatVerrou = EtatVerrou.W;
                break;
            case R:
                objetPartage = JvnServerImpl.jvnGetServer().jvnLockWrite(this.idUnique);
                this.etatVerrou = EtatVerrou.W;
                break;
            case W:
                break;
            case RWC:
                this.etatVerrou = EtatVerrou.W;
                break;
            default:
                break;
        }

        System.out.println("IMPL - etat après jvnLockWrite : " + this.etatVerrou + "");
    }

    @Override
    public synchronized void jvnUnLock() throws JvnException {
        System.out.println("IMPL - jvnUnLock appelé sur l'objet " + this.idUnique + " avec l'état " + this.etatVerrou + "");

        switch (this.etatVerrou) {
            case R:
                this.etatVerrou = EtatVerrou.RC;
                break;
            case W:
                this.etatVerrou = EtatVerrou.WC;
                break;
            case RWC:
                this.etatVerrou = EtatVerrou.WC;
                break;
            default:
                throw new JvnException("Unlock impossible dans cet etat");
        }

        notify();

        System.out.println("IMPL - etat après jvnUnLock : " + this.etatVerrou + "");
    }

    
    
}
