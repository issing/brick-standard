package net.isger.brick.plugin.persist;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.isger.brick.stub.StubCommand;
import net.isger.util.Helpers;
import net.isger.util.Reflects;
import net.isger.util.Strings;
import net.isger.util.anno.Ignore;
import net.isger.util.reflect.BoundMethod;

/**
 * 持久代理
 * 
 * @author issing
 *
 */
@Ignore
public class PersistProxy extends BasePersist {

    public static final String PARAM_IDENTITY = "persist.identity";

    protected final Object target;

    protected PersistProxy() {
        this(null);
    }

    protected PersistProxy(Object target) {
        if (target == null) {
            target = this;
        }
        this.target = target;

    }

    public Object operate(String operate) {
        StubCommand cmd = this.getStubCommand();
        Class<?> targetClass = Reflects.getClass(target);
        cmd.setTable(targetClass);
        String persist = getPluginCommand().getPersist();
        BoundMethod boundMethod;
        if ((boundMethod = Reflects.getBoundMethod(targetClass, persist)) == null
                && (boundMethod = Reflects.getBoundMethod(targetClass, operate)) == null) {
            cmd.setCondition(Strings.empty(
                    (String) cmd.getParameter(PARAM_IDENTITY), operate));
        } else {
            Method method = boundMethod.getMethod();
            Class<?>[] paramTypes = method.getParameterTypes();
            Annotation[][] annos = method.getParameterAnnotations();
            List<Object> params = new ArrayList<Object>();
            int size = paramTypes.length;
            String paramName;
            for (int i = 0; i < size; i++) {
                if (Helpers.hasAliasName(annos[i])) {
                    paramName = Helpers.getAliasName(annos[i]);
                } else {
                    paramName = operate + i;
                }
                params.add(cmd.getParameter(paramName));
            }
            cmd.setCondition(Strings.empty(
                    (String) cmd.getParameter(PARAM_IDENTITY), operate), params
                    .toArray());
            if (!(Reflects.isAbstract(method) || target instanceof Class)) {
                try {
                    Object result = method.invoke(target, params.toArray());
                    if (!Void.TYPE.equals(method.getReturnType())) {
                        cmd.setResult(result);
                    }
                    return result;
                } catch (Exception e) {
                    throw new IllegalStateException(e.getMessage(),
                            e.getCause());
                }
            }
        }
        return toStub().getResult();
    }
}
