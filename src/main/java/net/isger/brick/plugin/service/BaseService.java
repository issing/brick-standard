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

    public static final String OPERATE_INITIAL = "initial";

    public static final String OPERATE_DESTROY = "destroy";

    /** 控制台 */
    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.SYSTEM)
    private Console console;

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
