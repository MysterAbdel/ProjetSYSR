# ProjetSYSR

### Ce qui a été fait :

- Javanaise 1
- Javanaise 2 (proxy & annotations)
- Tests :
    - Burst test de la Javanaise 1
    - Burst test de la Javanaise 2
    - Essai de la Javanaise 2 sur une version adaptée de Irc
- Extensions : 
    - Traitement des pannes du coordinateur via sauvegarde sur fichier .ser

### Lancer le serveur d'objets partagés :

Sur VisualStudioCode : click droit sur le fichier JvnCoordImpl.java puis "Run Java".

### Comment utiliser la Javanaise 2 ?

#### Création d'Objets partagés :

Importer la classe JvnProxy :
```java
import jvn.JvnProxy
```
Créer une instance d'objet via le proxy :
Pour chaque objet créé, définir un nom symbolique unique.
```java
IMyObject o = (IMyObject) JvnProxy.newInstance(new MyObject(), "myobject")
```

#### Annotations :

L'annotation "READ" : Permet l'acquisition d'un verrou en lecture sur l'objet à l'éxecution de la méthode annotée.
```java
@JvnAnnotation(name = "READ")
public void myMethod() {
    // ...
}
```

L'annotation "WRITE" : Permet l'acquisition d'un verrou en écriture sur l'objet à l'éxecution de la méthode annotée.
```java
@JvnAnnotation(name = "WRITE")
public void myMethod() {
    // ...
}
```
Si une méthode ne possède pas d'annotation, son exécution ne nécessite pas de verrou.

### Les tests ajoutés :

#### Burst Test de la Javanaise 1 :

La partie test est composée de deux classes principales : BurstTest et JvnClientThread.

La classe BurstTest est la classe principale du test. Elle initialise le serveur Javanaise, recherche ou crée un objet JVN, crée un certain nombre de threads clients (numClients), les démarre, attend que tous les threads clients aient terminé, puis affiche les résultats des performances.

La classe JvnClientThread représente un thread client individuel. Chaque thread client tente de verrouiller un objet JVN, effectue un traitement simulé (ici, une pause de 1 milliseconde), puis libère le verrou. Le nombre de tentatives est défini par NUM_LOCK_ATTEMPTS.

Après avoir exécuté le test, on observe des résultats de performance affichés dans la console. Ces résultats incluent le temps d'exécution total, le nombre de tentatives de verrouillage réussies, le nombre total de tentatives de verrouillage et le taux de réussite en pourcentage.

#### Burst Test de la Javanaise 2 :

Fichier de test BurstTest2. Même méchanisme que le premier.

#### Essai de la Javanaise 2 (IRC):

Nous avons créé la classe Irc2, duplication de Irc pour tester la Javanaise 2, dans laquelle nous avons adapté le code pour correspondre à une utilisation avec proxy et annotations.

Nous avons également étendu la classe Sentence avec l'ajout de l'interface ISentence, dans laquelle mis les prototypes annotés.

```java
import jvn.JvnAnnotation;

public interface ISentence {
    
    @JvnAnnotation(nom="WRITE")
	public void write(String text);

	@JvnAnnotation(nom="READ")
	public String read();
}
```

### Les extensions :

#### Traitement des pannes du coordinateur via sauvegarde sur fichier .ser :

Nous avons créé une classe singleton JvnCoordCache qui permet de sauvegarder et récupérer une copie sérialisée du coordinateur dans un fichier d'extension .ser.

Lors de l'instanciation de la classe JvnCoordImpl, si une sauvegarde est présente, le coordinateur est récupéré depuis le fichier, sinon il est créé.

La sauvegarde du coordinateur est effectuée après "register", "lockRead", et "lockWrite". De plus, côté client, une tentative de reconnexion en continu est effectuée en cas de perte de connexion avec le coordinateur.
Ainsi, en cas de panne du coordinateur, les données sont sauvegardées et peuvent être récupérées.

```java
public class JvnCoordCache {

    private static JvnCoordCache jCC = null; //-- Singleton

    private JvnCoordCache() {} //-- Constructeur privé

    public static JvnCoordCache getJvnCoordCache() {
        //-- Instanciation du singleton
    }

    public void saveCoordinatorIntoCache(JvnCoordImpl coord) {
        //-- Sauvegarde du coordinateur dans le cache
    }

    public JvnCoordImpl loadCoordinatorFromCache() {
        //-- Récupération du coordinateur depuis le cache
    }
}
```


