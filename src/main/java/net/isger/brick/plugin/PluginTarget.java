package net.isger.brick.plugin;

import net.isger.brick.core.GateTarget;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

public class PluginTarget extends GateTarget {

    @Ignore(mode = Mode.INCLUDE)
    private String plugin;

    protected final PluginCommand getPluginCommand() {
        PluginCommand cmd = PluginCommand.cast(super.getCommand());
        if (plugin != null) {
            cmd.setDomain(plugin);
        }
        return cmd;
    }

    protected final PluginCommand mockPluginCommand() {
        return PluginCommand.cast(super.mockCommand());
    }

    protected final PluginCommand realPluginCommand() {
        return PluginCommand.cast(super.realCommand());
    }

}
