package jvn;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Cache pour le coordinator -- EXTENSION [PANNE COORDINATEUR]
 * - Crée un fichier cache_coord.ser qui contient une sauvegarde sérialisée du coordinator
 */
public class JvnCoordCache {
    
    private static JvnCoordCache jCC = null;

    public JvnCoordCache() {}

    public static JvnCoordCache getJvnCoordCache() {
        if (jCC == null) {
            jCC = new JvnCoordCache();
        }
        return jCC;
    }

    public void saveCoordinatorIntoCache(JvnCoordImpl coord) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("cache_coord.ser"))) {
            oos.writeObject(coord);
            System.out.println("JCC - Coordinator saved into cache");
        } catch (IOException e) {
            System.out.println("JCC - Error while saving coordinator into cache");
            e.printStackTrace();
        }
    }

    public JvnCoordImpl loadCoordinatorFromCache() {
        JvnCoordImpl coordImpl;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("cache_coord.ser"))) {
            coordImpl = (JvnCoordImpl) ois.readObject();
            System.out.println("JCC - Coordinator loaded from cache");
            return coordImpl;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("JCC - No cache found");
        }
        return null;
    }

}
