package net.isger.brick.stub;

import net.isger.brick.core.Gate;

public interface Stub extends Gate {

    /**
     * 创建
     * 
     */
    public void create();

    public void create(StubCommand cmd);

    /**
     * 新增
     * 
     */
    public void insert();

    public void insert(StubCommand cmd);

    /**
     * 删除
     * 
     */
    public void delete();

    public void delete(StubCommand cmd);

    /**
     * 修改
     * 
     */
    public void update();

    public void update(StubCommand cmd);

    /**
     * 查询
     * 
     */
    public void search();

    public void search(StubCommand cmd);

    /**
     * 移除
     * 
     */
    public void remove();

    public void remove(StubCommand cmd);

}
