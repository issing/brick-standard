package net.isger.brick.test.service;

import net.isger.brick.core.BaseCommand;
import net.isger.brick.plugin.PluginCommand;
import net.isger.brick.plugin.PluginHelper;
import net.isger.brick.plugin.service.BaseService;
import net.isger.util.Reflects;

public class GoodsService extends BaseService {

    private boolean isCreate;

    public void insert(PluginCommand cmd) {
        if (!isCreate) {
            try {
                PluginHelper.toPersist(mockPluginCommand(), "create");
            } finally {
                BaseCommand.realAction();
            }
            isCreate = true;
        }
        PluginHelper.toPersist();
    }

    public void select(PluginCommand cmd) {
        Object[] result = (Object[]) PluginHelper.toPersist(cmd);
        System.out.println(Reflects.toListMap(result));
        System.out.println("Search Count: " + result[2]);
    }

}
