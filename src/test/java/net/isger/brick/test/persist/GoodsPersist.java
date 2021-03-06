package net.isger.brick.test.persist;

import net.isger.brick.plugin.persist.Persist;
import net.isger.util.anno.Alias;
import net.isger.util.sql.Page;

public interface GoodsPersist extends Persist {

    public void create();

    public void select(@Alias("name") String name, @Alias("page") Page page);

    public void insert(@Alias("id") String id, @Alias("name") String name);

}
