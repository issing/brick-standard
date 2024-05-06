package net.isger.brick.plugin.service;

import net.isger.brick.Constants;
import net.isger.brick.core.Console;
import net.isger.brick.plugin.PluginCommand;
import net.isger.brick.plugin.PluginOperator;
import net.isger.util.anno.Alias;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

@Ignore
public class BaseService extends PluginOperator implements Service {

    /** 控制台 */
    @Alias(Constants.SYSTEM)
    @Ignore(mode = Mode.INCLUDE, serialize = false)
    protected Console console;

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
