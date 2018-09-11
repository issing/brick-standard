package net.isger.brick.bind;

import net.isger.brick.StandardConstants;
import net.isger.brick.auth.ShiroModule;
import net.isger.brick.core.Console;
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
        /* 标准内核 */
        this.setupModule(MOD_AUTH, new ShiroModule()); // 认证模块
        this.setupModule(MOD_PLUGIN, new PluginModule(), PluginCommand.class,
                MOD_STUB);// 插件模块
        this.setupModule(MOD_SCHED, new SchedModule(), SchedCommand.class,
                MOD_PLUGIN);// 调度模块
        /* 默认内核 */
        super.loadKernel();
    }

}
