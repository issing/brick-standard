package net.isger.brick.plugin.service;

import net.isger.brick.plugin.PluginCommand;
import net.isger.brick.plugin.PluginOperator;
import net.isger.util.anno.Ignore;

@Ignore
public class BaseService extends PluginOperator implements Service {

    public void service(PluginCommand cmd) {
        super.operate(cmd);
    }

}
