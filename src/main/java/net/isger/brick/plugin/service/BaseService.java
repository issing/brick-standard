package net.isger.brick.plugin.service;

import net.isger.brick.core.Command;
import net.isger.brick.plugin.Plugin;
import net.isger.brick.plugin.PluginCommand;
import net.isger.brick.plugin.PluginTarget;
import net.isger.util.anno.Ignore;

@Ignore
public class BaseService extends PluginTarget implements Service {

    public void initial() {
    }

    protected <T extends Command> T toExecute(T cmd) {
        getConsole().execute(cmd);
        return cmd;
    }

    protected PluginCommand toPlugin(String domain) {
        PluginCommand cmd = mockPluginCommand();
        cmd.setDomain(domain);
        try {
            toExecute(cmd);
        } finally {
            realCommand();
        }
        return cmd;
    }

    protected PluginCommand toPlugin(String domain, String name) {
        PluginCommand cmd = mockPluginCommand();
        cmd.setDomain(domain);
        cmd.setName(name);
        try {
            toExecute(cmd);
        } finally {
            realCommand();
        }
        return cmd;
    }

    protected PluginCommand toPlugin(String domain, String name, String operate) {
        PluginCommand cmd = mockPluginCommand();
        cmd.setDomain(domain);
        cmd.setOperate(operate);
        cmd.setName(name);
        try {
            toExecute(cmd);
        } finally {
            realCommand();
        }
        return cmd;
    }

    protected PluginCommand toService(String name) {
        PluginCommand cmd = mockPluginCommand();
        cmd.setName(name);
        try {
            getModule().execute();
        } finally {
            realCommand();
        }
        return cmd;
    }

    protected PluginCommand toService(String name, String operate) {
        PluginCommand cmd = mockPluginCommand();
        cmd.setName(name);
        cmd.setOperate(operate);
        try {
            getModule().execute();
        } finally {
            realCommand();
        }
        return cmd;
    }

    protected PluginCommand toPersist() {
        ((Plugin) getGate()).persist();
        return getPluginCommand();
    }

    protected PluginCommand toPersist(String operate) {
        PluginCommand cmd = mockPluginCommand();
        cmd.setOperate(operate);
        try {
            toPersist();
        } finally {
            realCommand();
        }
        return cmd;
    }

    protected PluginCommand toPersist(String name, String operate) {
        PluginCommand cmd = mockPluginCommand();
        cmd.setName(name);
        cmd.setOperate(operate);
        try {
            toPersist();
        } finally {
            realCommand();
        }
        return cmd;
    }

    public void destroy() {
    }

}
