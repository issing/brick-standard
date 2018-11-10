package net.isger.brick.test.service;

import net.isger.brick.plugin.PluginCommand;
import net.isger.brick.plugin.PluginHelper;
import net.isger.brick.plugin.service.BaseService;
import net.isger.util.Reflects;

public class EmployService extends BaseService {

    private boolean isCreate;

    public void initial() {
        PluginHelper.toPersist(); /* 初始化 */
    }

    public void test() {
        PluginCommand cmd = PluginCommand.getAction();
        String test = (String) cmd.getParameter("test");
        System.out.println("EmployService.test(): " + test);
        PluginHelper.toPersist();
        if ("stub".equalsIgnoreCase(test)) {
            System.out.println("EmployService.test() result: "
                    + Reflects.toList((Object[]) cmd.getResult()));
        }
        cmd.setName("chain");
        cmd.setOperate("test");
        PluginHelper.toPersist();
        System.out.println("EmployService.test() result: " + cmd.getResult());
        cmd.setOperate("chain");
        PluginHelper.toPersist();
        System.out.println("EmployService.test() result: " + cmd.getResult());
    }

    public void action() {
        if (isCreate) {
            return;
        }
        isCreate = true;
        System.out.println("EmployService.schedule(): create dynamic task");
        // SchedCommand cmd = SchedCommand.getAction();
        // cmd.setDomain(null);
        // cmd.setOperate(SchedCommand.OPERATE_CREATE);
        // Map<String, Object> config = new HashMap<String, Object>();
        // config.put("interval", "*/1 * * * * ?");
        // Map<String, Object> params = new HashMap<String, Object>();
        // params.put(BaseSched.PARAM_DOMAIN, "test");
        // params.put(BaseSched.PARAM_NAME, "dynamic");
        // config.put("parameters", params);
        // cmd.setParameter("employ.dynamic", config);
        // PluginHelper.toConsole(cmd);
    }

}
