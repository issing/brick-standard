package net.isger.brick.bind;

import net.isger.brick.StandardConstants;
import net.isger.brick.auth.ShiroModule;
import net.isger.brick.core.Console;
import net.isger.brick.plugin.PluginCommand;
import net.isger.brick.plugin.PluginModule;
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
        setupModule(MOD_AUTH, new ShiroModule()); // 认证模块
        setupModule(MOD_PLUGIN, new PluginModule(), PluginCommand.class, MOD_STUB);// 插件模块
        /* 默认内核 */
        super.loadKernel();
    }

}
