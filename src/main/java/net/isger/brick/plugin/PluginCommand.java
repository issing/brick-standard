package net.isger.brick.plugin;

import net.isger.brick.core.BaseCommand;
import net.isger.brick.core.Command;
import net.isger.brick.core.GateCommand;

public class PluginCommand extends GateCommand {

    public static final String KEY_NAME = "plugin-name";

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

    public static PluginCommand cast(BaseCommand cmd) {
        return cmd == null || cmd.getClass() == PluginCommand.class ? (PluginCommand) cmd
                : cmd.infect(new PluginCommand(false));
    }

    public String getName() {
        return getHeader(KEY_NAME);
    }

    public void setName(String name) {
        setHeader(KEY_NAME, name);
    }

}
