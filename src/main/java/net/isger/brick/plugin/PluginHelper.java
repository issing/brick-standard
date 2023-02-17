package net.isger.brick.plugin;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import net.isger.brick.core.BaseCommand;
import net.isger.brick.core.CoreHelper;
import net.isger.brick.plugin.service.Service;
import net.isger.brick.plugin.service.Services;
import net.isger.util.Helpers;
import net.isger.util.Strings;
import net.isger.util.reflect.BoundMethod;
import net.isger.util.reflect.Standin;
import net.isger.util.sql.Page;

/**
 * 插件助手
 * 
 * @author issing
 *
 */
public class PluginHelper extends CoreHelper {

    protected PluginHelper() {
    }

    public static Plugin getPlugin() {
        return (Plugin) CoreHelper.getGate();
    }

    /**
     * 执行插件服务（基于当前插件无法跨域）
     * 
     * @return
     */
    public static Object toService() {
        return toService(PluginCommand.getAction());
    }

    /**
     * 执行插件服务
     * 
     * @param cmd
     * @return
     */
    public static Object toService(PluginCommand cmd) {
        getPlugin().service(cmd);
        return cmd.getResult();
    }

    /**
     * 执行插件服务
     * 
     * @param cmd
     * @param name
     * @return
     */
    public static Object toService(PluginCommand cmd, String name) {
        if (Strings.isNotEmpty(name)) {
            cmd.setName(name);
        }
        return toService(cmd);
    }

    /**
     * 执行插件服务
     * 
     * @param cmd
     * @param clazz
     * @return
     */
    public static Object toService(PluginCommand cmd, Class<? extends Service> clazz) {
        return toService(cmd, Services.getName(clazz));
    }

    /**
     * 执行插件服务
     * 
     * @param cmd
     * @param name
     * @param operate
     * @return
     */
    public static Object toService(PluginCommand cmd, String name, String operate) {
        cmd.setOperate(operate);
        return toService(cmd, name);
    }

    /**
     * 执行插件服务
     * 
     * @param cmd
     * @param clazz
     * @param operate
     * @return
     */
    public static Object toService(PluginCommand cmd, Class<? extends Service> clazz, String operate) {
        return toService(cmd, Services.getName(clazz), operate);
    }

    /**
     * 执行插件持久（基于当前插件无法跨域）
     * 
     * @return
     */
    public static Object toPersist() {
        return toPersist(PluginCommand.getAction());
    }

    /**
     * 执行插件持久
     * 
     * @param cmd
     * @return
     */
    public static Object toPersist(PluginCommand cmd) {
        Object value = cmd.getParameter(PluginConstants.PARAM_STATEMENT_VALUE);
        if (!(value == null || value instanceof Object[])) {
            cmd.setParameter(PluginConstants.PARAM_STATEMENT_VALUE, Helpers.wraps(value));
        }
        getPlugin().persist(cmd);
        return cmd.getResult();
    }

    /**
     * 执行插件持久
     * 
     * @param cmd
     * @param operate
     * @return
     */
    public static Object toPersist(PluginCommand cmd, String operate) {
        cmd.setOperate(operate);
        return toPersist(cmd);
    }

    /**
     * 执行插件持久
     * 
     * @param cmd
     * @param operate
     * @param persist
     * @return
     */
    public static Object toPersist(PluginCommand cmd, String operate, String persist) {
        cmd.setPersist(persist);
        return toPersist(cmd, operate);
    }

    /**
     * 执行插件持久
     * 
     * @param cmd
     * @param operate
     * @param persist
     * @param statement
     * @return
     */
    public static Object toPersist(PluginCommand cmd, String operate, String persist, String statement) {
        cmd.setParameter(PluginConstants.PARAM_STATEMENT_ID, statement);
        return toPersist(cmd, operate, persist);
    }

    /**
     * 执行插件持久
     * 
     * @param cmd
     * @param operate
     * @param persist
     * @param statement
     * @param value
     * @return
     */
    public static Object toPersist(PluginCommand cmd, String operate, String persist, String statement, Object value) {
        cmd.setParameter(PluginConstants.PARAM_STATEMENT_VALUE, value);
        return toPersist(cmd, operate, persist, statement);
    }

    /**
     * 执行插件持久
     *
     * @param cmd
     * @param operate
     * @param persist
     * @param statement
     * @param value
     * @param args
     * @return
     */
    public static Object toPersists(PluginCommand cmd, String operate, String persist, String statement, Object value, Object... args) {
        cmd.setParameter(PluginConstants.PARAM_STATEMENT_ARGS, args);
        return toPersist(cmd, operate, persist, statement, value);
    }

    /**
     * 服务接口
     * 
     * @param clazz
     * @return
     */
    public static <T> T service(Class<T> clazz) {
        return service(null, clazz, null, null);
    }

    /**
     * 服务接口
     * 
     * @param clazz
     * @param domain
     * @return
     */
    public static <T> T service(Class<T> clazz, String domain) {
        return service(null, clazz, domain, null);
    }

    /**
     * 服务接口
     * 
     * @param cmd
     * @param clazz
     * @return
     */
    public static <T> T service(PluginCommand cmd, Class<T> clazz) {
        return service(cmd, clazz, null, null);
    }

    /**
     * 服务接口
     *
     * @param cmd
     * @param clazz
     * @param domain
     * @return
     */
    public static <T> T service(PluginCommand cmd, Class<T> clazz, String domain) {
        return service(cmd, clazz, domain, null);
    }

    /**
     * 服务接口
     *
     * @param cmd
     * @param clazz
     * @param domain
     * @param name
     * @return
     */
    public static <T> T service(PluginCommand cmd, final Class<T> clazz, final String domain, final String name) {
        if (cmd == null) {
            cmd = PluginCommand.getAction();
        }
        final PluginCommand shellCmd = new PluginCommand(cmd);
        return new Standin<T>(clazz) {
            public Object action(Method method, Object[] args) {
                String[] serviceName = Services.getName(clazz).split("[:]");
                if (Strings.isNotEmpty(domain)) {
                    shellCmd.setDomain(domain);
                } else if (serviceName.length > 1) {
                    shellCmd.setDomain(serviceName[0]);
                }
                shellCmd.setName(Strings.isEmpty(name) ? (serviceName.length > 1 ? serviceName[1] : serviceName[0]) : name);
                String operate = BoundMethod.makeMethodDesc(method);
                shellCmd.setOperate(operate);
                Class<?>[] paramTypes = method.getParameterTypes();
                Annotation[][] annos = method.getParameterAnnotations();
                int size = paramTypes.length;
                String paramName;
                for (int i = 0; i < size; i++) {
                    paramName = Helpers.getAliasName(annos[i]);
                    if (Strings.isEmpty(paramName)) {
                        paramName = operate + i;
                    }
                    shellCmd.setParameter(paramName, args[i]);
                }
                return PluginHelper.toConsole(shellCmd);
            }
        }.getSource();
    }

    public static void enablePage(BaseCommand cmd, boolean enabled) {
        cmd.setParameter(PluginConstants.PARAM_PAGE, enabled ? cmd.getParameter(Page.class) : null);
    }

    public static boolean isEnablePage(BaseCommand cmd) {
        return cmd.getParameter(PluginConstants.PARAM_PAGE) instanceof Page;
    }

}
