package jvn;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class JvnProxy implements InvocationHandler {

    private JvnObject obj;

    private JvnProxy(Object obj){
        this.obj = (JvnObject) obj;
    }

    public static Object newInstance(Object obj){
        return java.lang.reflect.Proxy.newProxyInstance(
            obj.getClass().getClassLoader(),
            obj.getClass().getInterfaces(),
            new JvnProxy(obj)
        );
    }

    @Override
    public Object invoke(Object proxy, Method m, Object[] args) throws JvnException {
        Object result = null;
        try {
            
            JvnAnnotation annotation = m.getAnnotation(JvnAnnotation.class);

            switch(annotation.nom()){
                case "READ":
                    this.obj.jvnLockRead();
                    break;
                case "WRITE":
                    this.obj.jvnLockWrite();
                    break;
                default :
                    throw new JvnException("Erreur d'annotation");
            }

            result = m.invoke(obj.jvnGetSharedObject(), args);
            this.obj.jvnUnLock();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    
}
