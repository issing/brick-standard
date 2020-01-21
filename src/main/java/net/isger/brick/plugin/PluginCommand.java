package net.isger.brick.plugin;

import net.isger.brick.core.BaseCommand;
import net.isger.brick.core.Command;
import net.isger.brick.core.GateCommand;
import net.isger.util.reflect.BoundMethod;

public class PluginCommand extends GateCommand {

    public static final String CTRL_NAME = "plugin-name";

    public static final String CTRL_PERSIST = "plugin-persist";

    public PluginCommand() {
    }

    public PluginCommand(Command source) {
        super(source);
    }

    public PluginCommand(boolean hasShell) {
        super(hasShell);
    }

    public static PluginCommand getAction() {
        return cast(BaseCommand.getAction());
    }

    public static PluginCommand newAction() {
        return cast(BaseCommand.newAction());
    }

    public static PluginCommand mockAction() {
        return cast(BaseCommand.mockAction());
    }

    public static PluginCommand realAction() {
        return cast(BaseCommand.realAction());
    }

    public static PluginCommand cast(BaseCommand cmd) {
        return cmd == null || cmd.getClass() == PluginCommand.class ? (PluginCommand) cmd : cmd.infect(new PluginCommand(false));
    }

    protected String getAccess() {
        return getName();
    }

    public static String getName(BaseCommand cmd) {
        return cmd.getHeader(CTRL_NAME);
    }

    public static void setName(BaseCommand cmd, String name) {
        cmd.setHeader(CTRL_NAME, name);
    }

    public String getName() {
        return getName(this);
    }

    public void setName(String name) {
        setName(this, name);
    }

    public static String getPersist(BaseCommand cmd) {
        return cmd.getHeader(CTRL_PERSIST);
    }

    public static void setPersist(BaseCommand cmd, String name) {
        cmd.setHeader(CTRL_PERSIST, name);
    }

    public String getPersist() {
        return getPersist(this);
    }

    public void setPersist(String name) {
        setPersist(this, name);
    }

    public void setPersist(String name, Class<?> resultType, Class<?>... argTypes) {
        setPersist(BoundMethod.makeMethodDesc(name, resultType, argTypes));
    }

    public PluginCommand clone() {
        return (PluginCommand) super.clone();
    }

}
