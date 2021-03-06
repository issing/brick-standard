package net.isger.brick.plugin;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import net.isger.brick.core.CoreHelper;
import net.isger.brick.plugin.service.Service;
import net.isger.brick.plugin.service.Services;
import net.isger.util.Helpers;
import net.isger.util.Strings;
import net.isger.util.reflect.Standin;

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
    public static Object toService(PluginCommand cmd,
            Class<? extends Service> clazz) {
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
    public static Object toService(PluginCommand cmd, String name,
            String operate) {
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
    public static Object toService(PluginCommand cmd,
            Class<? extends Service> clazz, String operate) {
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
        Object value = cmd.getParameter(PluginConstants.PARAM_VALUE);
        if (!(value == null || value instanceof Object[])) {
            cmd.setParameter(PluginConstants.PARAM_VALUE,
                    new Object[] { value });
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
    public static Object toPersist(PluginCommand cmd, String operate,
            String persist) {
        cmd.setPersist(persist);
        return toPersist(cmd, operate);
    }

    /**
     * 执行插件持久
     * 
     * @param cmd
     * @param operate
     * @param persist
     * @param opcode
     * @return
     */
    public static Object toPersist(PluginCommand cmd, String operate,
            String persist, String opcode) {
        cmd.setParameter(PluginConstants.PARAM_OPCODE, opcode);
        return toPersist(cmd, operate, persist);
    }

    /**
     * 执行插件持久
     * 
     * @param cmd
     * @param operate
     * @param persist
     * @param opcode
     * @param value
     * @return
     */
    public static Object toPersist(PluginCommand cmd, String operate,
            String persist, String opcode, Object value) {
        cmd.setParameter(PluginConstants.PARAM_VALUE, value);
        return toPersist(cmd, operate, persist, opcode);
    }

    /**
     * 服务接口
     * 
     * @param pcmd
     * @param clazz
     * @return
     */
    public static <T> T service(final PluginCommand cmd, Class<T> clazz) {
        return new Standin<T>(clazz) {
            public Object action(Method method, Object[] args) {
                PluginCommand pcmd = new PluginCommand(cmd);
                String operate = method.getName();
                pcmd.setOperate(operate);
                Class<?>[] paramTypes = method.getParameterTypes();
                Annotation[][] annos = method.getParameterAnnotations();
                int size = paramTypes.length;
                String paramName;
                for (int i = 0; i < size; i++) {
                    paramName = Helpers.getAliasName(annos[i]);
                    if (Strings.isEmpty(paramName)) {
                        paramName = operate + i;
                    }
                    pcmd.setParameter(paramName, args[i]);
                }
                return PluginHelper.toConsole(pcmd);
            }
        }.getSource();
    }

}
