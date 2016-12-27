package net.isger.brick.plugin.persist;

import net.isger.brick.stub.StubCommand;

/**
 * 持久接口
 * 
 * @author issing
 *
 */
public interface Persist {

    /**
     * 持久入口
     * 
     * @param cmd
     */
    public void persist(StubCommand cmd);

}
