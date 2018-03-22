package net.isger.brick.bind;

import net.isger.brick.StandardConstants;
import net.isger.brick.auth.ShiroModule;
import net.isger.brick.core.Console;
import net.isger.brick.core.Module;
import net.isger.brick.plugin.PluginCommand;
import net.isger.brick.plugin.PluginModule;
import net.isger.brick.sched.SchedCommand;
import net.isger.brick.sched.SchedModule;
import net.isger.util.anno.Ignore;

/**
 * 标准版控制台
 * 
 * @author issing
 * 
 */
@Ignore
public class StandardConsole extends Console implements StandardConstants {

    protected void loadKernel() {
        /* 默认内核 */
        // 认证模块
        Module module = getModule(MOD_AUTH);
        if (module == null) {
            addModule(MOD_AUTH, new ShiroModule());
        }
        // 插件模块
        module = getModule(MOD_PLUGIN);
        if (module == null) {
            addModule(MOD_PLUGIN, new PluginModule());
        }
        addDependencies(MOD_PLUGIN, MOD_STUB);
        addCommand(MOD_PLUGIN, PluginCommand.class);
        // 调度模块
        module = getModule(MOD_SCHED);
        if (module == null) {
            addModule(MOD_SCHED, new SchedModule());
        }
        addDependencies(MOD_SCHED, MOD_PLUGIN);
        addCommand(MOD_SCHED, SchedCommand.class);
        /* 配置内核 */
        super.loadKernel();
    }

}
