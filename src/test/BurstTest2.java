package test;

import java.util.concurrent.atomic.AtomicInteger;

import irc.ISentence;
import irc.Sentence;
import jvn.JvnException;
import jvn.JvnProxy;

public class BurstTest2 {
    public static void main(String[] args) throws JvnException {
        
        ISentence obj1 = (ISentence) JvnProxy.newInstance(new Sentence(), "IRC");

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
    private ISentence iSentence;
    private AtomicInteger successfulLocks;
    public static final int NUM_LOCK_ATTEMPTS = 500;

    public JvnClientThread(int clientId, ISentence iSentence, AtomicInteger successfulLocks) {
        this.clientId = clientId;
        this.iSentence = iSentence;
        this.successfulLocks = successfulLocks;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < NUM_LOCK_ATTEMPTS; i++) {
                iSentence.read(); // L'anottation sur la méthode read() permet prendre le verrou
                Thread.sleep(1);
                successfulLocks.incrementAndGet();
            }
        } catch (Exception e) {
            System.err.println("Erreur du client " + clientId);
            e.printStackTrace();
        }
    }
}
