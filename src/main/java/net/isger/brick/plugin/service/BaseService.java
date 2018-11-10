package net.isger.brick.plugin.service;

import net.isger.brick.plugin.PluginCommand;
import net.isger.brick.plugin.PluginOperator;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

@Ignore
public class BaseService extends PluginOperator implements Service {

    @Ignore(mode = Mode.INCLUDE)
    public void initial() {
    }

    public void service(PluginCommand cmd) {
        super.operate(cmd);
    }

    @Ignore(mode = Mode.INCLUDE)
    public void destroy() {
    }

}
