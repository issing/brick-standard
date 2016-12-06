package net.isger.brick.plugin;

import net.isger.brick.util.CommandOperator;
import net.isger.util.Strings;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

public class PluginOperator extends CommandOperator {

    @Ignore(mode = Mode.INCLUDE)
    private String plugin;

    protected final PluginCommand getPluginCommand() {
        PluginCommand cmd = PluginCommand.getAction();
        if (Strings.isNotEmpty(plugin)) {
            cmd.setDomain(plugin);
        }
        return cmd;
    }

    protected final PluginCommand mockPluginCommand() {
        PluginCommand.mockAction();
        return getPluginCommand();
    }

}
