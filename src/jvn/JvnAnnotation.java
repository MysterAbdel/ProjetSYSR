package jvn;
import java.lang.annotation.*;

/*
 * << Le générateur de classes d’interposition jvnc interprête
 * les annotations présentes dans les classes des objets Jvn.
 * On utilise donc des annotations dont la rétention est de 
 * type Class >>
 */
@Retention(RetentionPolicy.CLASS)
//@Retention(RetentionPolicy.RUNTIME)

/*
 * << Ces annotations permettent de connaître le type 
 * des méthodes des objets Jvn (Read/Write) >>
 */
@Target(ElementType.METHOD)

public @interface JvnAnnotation {
    String nom();
}
