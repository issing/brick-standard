package net.isger.brick.test.service;

import net.isger.brick.plugin.PluginCommand;
import net.isger.brick.plugin.PluginHelper;
import net.isger.brick.plugin.service.BaseService;
import net.isger.util.Reflects;

public class GoodsService extends BaseService {

    private boolean isCreate;

    public void insert(PluginCommand cmd) {
        if (!isCreate) {
            try {
                PluginHelper.toPersist(cmd.clone(), "create");
            } catch (Exception e) {
            }
            isCreate = true;
        }
        PluginHelper.toPersist(cmd);
    }

    public void select(PluginCommand cmd) {
        Object[] result = (Object[]) PluginHelper.toPersist(cmd);
        System.out.println(Reflects.toList(result));
        System.out.println("Search Count: " + result[2]);
    }

}
