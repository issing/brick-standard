package net.isger.brick.plugin;

import net.isger.brick.plugin.persist.PersistsConversion;
import net.isger.brick.plugin.service.ServicesConversion;
import net.isger.brick.util.AbstractDesigner;

public class PluginDesigner extends AbstractDesigner {

    protected void prepare() {
        addConversion(ServicesConversion.getInstance());
        addConversion(PersistsConversion.getInstance());
    }

}
