package net.isger.brick.plugin.persist;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.isger.brick.auth.AuthIdentity;
import net.isger.brick.core.BaseCommand;
import net.isger.brick.core.Command;
import net.isger.brick.plugin.PluginCommand;
import net.isger.brick.plugin.PluginConstants;
import net.isger.brick.plugin.PluginHelper;
import net.isger.brick.stub.StubCommand;
import net.isger.util.Asserts;
import net.isger.util.Helpers;
import net.isger.util.Reflects;
import net.isger.util.Strings;
import net.isger.util.anno.Ignore;
import net.isger.util.reflect.BoundMethod;
import net.isger.util.reflect.Converter;

/**
 * 持久代理
 * 
 * @author issing
 *
 */
@Ignore
public class PersistProxy extends BasePersist {

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

    public final void persist(StubCommand cmd) {
        String operate = cmd.getOperate();
        Object table = cmd.getTable();
        Class<?> targetClass = Reflects.getClass(target);
        if (table == null) {
            cmd.setTable(targetClass);
        }
        String statement = Strings.join(true, ":", new String[] { operate, cmd.getParameter(PluginConstants.PARAM_STATEMENT_ID) });
        Object statementValue = cmd.getParameter(PluginConstants.PARAM_STATEMENT_VALUE);
        String persist = PluginCommand.getPersist(cmd);
        BoundMethod boundMethod;
        if ((boundMethod = Reflects.getBoundMethod(targetClass, persist, true)) == null && (boundMethod = Reflects.getBoundMethod(targetClass, operate, true)) == null) {
            cmd.setCondition(statement);
        } else {
            Method method = boundMethod.getMethod();
            Class<?>[] paramTypes = method.getParameterTypes();
            Annotation[][] annos = method.getParameterAnnotations();
            List<Object> values = new ArrayList<Object>();
            List<Object> params = new ArrayList<Object>();
            int size = paramTypes.length;
            String paramName;
            Object paramValue;
            for (int i = 0; i < size; i++) {
                paramName = Helpers.getAliasName(annos[i]);
                if (Strings.isEmpty(paramName)) {
                    paramValue = getParameter(paramTypes[i], cmd, operate + i);
                } else {
                    values.add(paramValue = getParameter(paramTypes[i], cmd, paramName));
                }
                params.add(paramValue);
            }
            cmd.setCondition(new Object[] { statement, new Object[] { statementValue, values.toArray() }, cmd.getParameter(PluginConstants.PARAM_STATEMENT_ARGS) });
            if (!(Reflects.isAbstract(method) || target instanceof Class)) {
                try {
                    Object result = method.invoke(target, params.toArray());
                    if (!Void.TYPE.equals(method.getReturnType())) {
                        cmd.setResult(result);
                    }
                    return;
                } catch (Exception e) {
                    Throwable cause = e;
                    if (!(cause instanceof RuntimeException)) {
                        cause = e.getCause();
                    }
                    throw cause instanceof RuntimeException ? (RuntimeException) cause : Asserts.state(cause.getMessage(), cause.getCause());
                }
            }
        }
        PluginHelper.toConsole(cmd);
        cmd.setTable(table);
    }

    /**
     * 获取类型参数
     *
     * @param type
     * @param cmd
     * @param name
     * @return
     */
    @Ignore
    protected Object getParameter(Class<?> type, BaseCommand cmd, String name) {
        Object value;
        AuthIdentity identity = cmd.getIdentity();
        if (type.isInstance(cmd) && Command.class.isAssignableFrom(type)) {
            value = cmd;
        } else if (type.isInstance(identity) && AuthIdentity.class.isAssignableFrom(type)) {
            value = identity;
        } else {
            // TODO 不完善（参考BaseScreen适配）
            value = cmd.getParameter(name);
            if (value != null && !type.isInstance(value)) {
                value = Converter.convert(type, value);
            }
        }
        return value;
    }

}
