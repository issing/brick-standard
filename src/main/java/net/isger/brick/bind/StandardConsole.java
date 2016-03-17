package net.isger.brick.bind;

import net.isger.brick.Constants;
import net.isger.brick.core.Console;
import net.isger.brick.core.Module;
import net.isger.brick.plugin.PluginCommand;
import net.isger.brick.plugin.PluginModule;
import net.isger.brick.sched.SchedCommand;
import net.isger.brick.sched.SchedModule;
import net.isger.brick.stub.StubCommand;
import net.isger.brick.stub.StubModule;
import net.isger.util.anno.Ignore;

/**
 * 默认控制台
 * 
 * @author issing
 * 
 */
@Ignore
public class StandardConsole extends Console {

    protected void loadKernel() {
        /* 默认内核 */
        // 调度模块
        Module module = getModule(Constants.MOD_SCHED);
        if (module == null) {
            addModule(Constants.MOD_SCHED, new SchedModule());
        }
        addCommand(Constants.MOD_SCHED, SchedCommand.class);
        // 插件模块
        module = getModule(Constants.MOD_PLUGIN);
        if (module == null) {
            addModule(Constants.MOD_PLUGIN, new PluginModule());
        }
        addCommand(Constants.MOD_PLUGIN, PluginCommand.class);
        // 存根模块
        module = getModule(Constants.MOD_STUB);
        if (module == null) {
            addModule(Constants.MOD_STUB, new StubModule());
        }
        addCommand(Constants.MOD_STUB, StubCommand.class);
        /* 配置内核 */
        super.loadKernel();
    }

}
