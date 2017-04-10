package net.isger.brick.plugin;

import net.isger.brick.core.BaseCommand;
import net.isger.brick.core.CommandHandler;
import net.isger.util.Strings;

public class PluginHandler extends CommandHandler {

    private String domain;

    private String name;

    private String operate;

    /**
     * 插件处理
     */
    public final Object handle(Object message) {
        BaseCommand cmd = toCommand(message);
        return toResult(cmd, super.handle(cmd));
    }

    /**
     * 插件命令
     * 
     * @param value
     * @return
     */
    public BaseCommand toCommand(Object value) {
        return toCommand(PluginCommand.newAction(), value);
    }

    /**
     * 插件命令
     * 
     * @param cmd
     * @param value
     * @return
     */
    public BaseCommand toCommand(PluginCommand cmd, Object value) {
        cmd.setParameter(PluginConstants.PARAM_VALUE, value);
        return toCommand(cmd);
    }

    /**
     * 插件命令
     * 
     * @param cmd
     * @return
     */
    public BaseCommand toCommand(PluginCommand cmd) {
        String domain = getDomain();
        if (Strings.isNotEmpty(domain)) {
            cmd.setDomain(domain);
        }
        cmd.setName(getName());
        cmd.setOperate(getOperate());
        return cmd;
    }

    protected Object toResult(BaseCommand cmd, Object result) {
        return result;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOperate() {
        return operate;
    }

    public void setOperate(String operate) {
        this.operate = operate;
    }

}
