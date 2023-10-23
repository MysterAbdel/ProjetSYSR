package test;

import irc.Sentence;
import jvn.JvnException;
import jvn.JvnServerImpl;
import jvn.JvnObject;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public class BurstTest {
    public static void main(String[] args) throws JvnException {
        // Initialisation du serveur JVN
        JvnServerImpl jvnServer = JvnServerImpl.jvnGetServer();

        // Recherche ou création d'objets JVN
        JvnObject obj1 = jvnServer.jvnLookupObject("IRC");

        if (obj1 == null) {
            // Création d'un nouvel objet JVN et enregistrement
            obj1 = jvnServer.jvnCreateObject((Serializable) new Sentence());
            obj1.jvnUnLock(); // Libération du verrou initial, en supposant qu'un verrou d'écriture est acquis lors de la création
            jvnServer.jvnRegisterObject("IRC", obj1);
        }

        int numClients = 500; // Nombre de threads clients
        Thread[] clientThreads = new Thread[numClients];
        AtomicInteger successfulLocks = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < numClients; i++) {
            clientThreads[i] = new Thread(new JvnClientThread(i, obj1, successfulLocks));
            clientThreads[i].start();
        }

        for (Thread thread : clientThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        int totalLockAttempts = numClients * JvnClientThread.NUM_LOCK_ATTEMPTS;
        int successfulLocksCount = successfulLocks.get();
        double successRate = (double) successfulLocksCount / totalLockAttempts;

        // Afficher les résultats des performances
        System.out.println("Temps d'exécution total : " + executionTime + " ms");
        System.out.println("Tentatives de verrouillage réussies : " + successfulLocksCount);
        System.out.println("Tentatives de verrouillage totales : " + totalLockAttempts);
        System.out.println("Taux de réussite : " + (successRate * 100) + "%");
    }
}

class JvnClientThread implements Runnable {
    private int clientId;
    private JvnObject jvnObject;
    private AtomicInteger successfulLocks;
    public static final int NUM_LOCK_ATTEMPTS = 5000;

    public JvnClientThread(int clientId, JvnObject jvnObject, AtomicInteger successfulLocks) {
        this.clientId = clientId;
        this.jvnObject = jvnObject;
        this.successfulLocks = successfulLocks;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < NUM_LOCK_ATTEMPTS; i++) {
                jvnObject.jvnLockWrite();

                // Simuler un traitement
                Thread.sleep(1);

                jvnObject.jvnUnLock();
                successfulLocks.incrementAndGet();
            }
        } catch (Exception e) {
            System.err.println("Erreur du client " + clientId);
            e.printStackTrace();
        }
    }
}
