package jvn;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import irc.Sentence;

public class JvnProxy implements InvocationHandler {

    private JvnObject obj;

    private JvnProxy(Serializable obj, String jon){
        try {
            JvnServerImpl js = JvnServerImpl.jvnGetServer();
            
            // look up the IRC object in the JVN server
            // if not found, create it, and register it in the JVN server
            JvnObject jo = js.jvnLookupObject(jon);
            
            if (jo == null) {
                jo = js.jvnCreateObject((Serializable) new Sentence());
                // after creation, I have a write lock on the object
                jo.jvnUnLock();
                js.jvnRegisterObject(jon, jo);
            }
            
            this.obj = jo;
        } catch (JvnException e) {
            e.printStackTrace();
        }
    }

    public static Object newInstance(Serializable obj,String jon){
        return java.lang.reflect.Proxy.newProxyInstance(
            obj.getClass().getClassLoader(),
            obj.getClass().getInterfaces(),
            new JvnProxy(obj,jon)
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
