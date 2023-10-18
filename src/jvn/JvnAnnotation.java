package jvn;
import java.lang.annotation.*;

/*
 * << Ces annotations sont présentes dans les classes chargées,
 * donc accessibles durant l'exécution >>
 */
@Retention(RetentionPolicy.RUNTIME)

/*
 * << Ces annotations permettent de connaître le type 
 * des méthodes des objets Jvn (Read/Write) >>
 */
@Target(ElementType.METHOD)

public @interface JvnAnnotation {
    String nom();
}
