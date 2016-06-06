package net.isger.brick.plugin;

import net.isger.brick.core.BaseCommand;
import net.isger.brick.core.Command;
import net.isger.brick.core.GateCommand;
import net.isger.util.reflect.BoundMethod;

public class PluginCommand extends GateCommand {

    public static final String KEY_NAME = "plugin-name";

    public static final String KEY_PERSIST = "plugin-persist";

    public static final String OPERATE_INITIAL = "initial";

    public static final String OPERATE_DESTROY = "destroy";

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
        return cmd == null || cmd.getClass() == PluginCommand.class ? (PluginCommand) cmd
                : cmd.infect(new PluginCommand(false));
    }

    protected String getAccess() {
        return getName();
    }

    public String getName() {
        return getHeader(KEY_NAME);
    }

    public void setName(String name) {
        setHeader(KEY_NAME, name);
    }

    public String getPersist() {
        return getHeader(KEY_PERSIST);
    }

    public void setPersist(String name) {
        setHeader(KEY_PERSIST, name);
    }

    public void setPersist(String name, Class<?> resultType,
            Class<?>... argTypes) {
        setPersist(BoundMethod.makeMethodName(name, resultType, argTypes));
    }

}
