package net.isger.brick.test.service;

import net.isger.brick.plugin.service.BaseService;
import net.isger.util.Reflects;

public class GoodsService extends BaseService {

    private boolean isCreate;

    public void insert() {
        if (!isCreate) {
            toPersist("goods", "create");
            isCreate = true;
        }
        toPersist();
    }

    public void search() {
        Object[] result = (Object[]) toPersist().getResult();
        System.out.println(Reflects.toListMap(result));
        System.out.println("Search Count: " + result[2]);
    }

}
