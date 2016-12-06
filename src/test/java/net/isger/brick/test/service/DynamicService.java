package net.isger.brick.test.service;

import net.isger.brick.plugin.PluginHelper;
import net.isger.brick.plugin.service.BaseService;
import net.isger.brick.sched.SchedCommand;

public class DynamicService extends BaseService {

    private int amount;

    public void action() {
        System.out.println("DynamicService.action()");
        SchedCommand cmd = SchedCommand.getAction();
        cmd.setDomain(null);
        synchronized (this) {
            ++amount;
        }
        if (amount % 3 == 0) {
            System.out.println("DynamicService.action() to pause");
            cmd.setOperate(SchedCommand.OPERATE_PAUSE);
            PluginHelper.toConsole(cmd);
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
            }
            System.out.println("DynamicService.action() to resume");
            cmd.setOperate(SchedCommand.OPERATE_RESUME);
            PluginHelper.toConsole(cmd);
        }
        if (amount >= 7) {
            System.out.println("DynamicService.action() to remove");
            cmd.setOperate(SchedCommand.OPERATE_REMOVE);
            PluginHelper.toConsole(cmd);
        }
    }

}
