package net.isger.brick.bind;

import net.isger.brick.inject.ContainerProvider;

/**
 * 核心绑定器
 * 
 * @author issing
 *
 */
public class BrickCoreBinder {

    private static final String MAGIC = "Standard 1.0";

    public static final BrickCoreBinder BINDER;

    static {
        BINDER = new BrickCoreBinder();
    }

    private ContainerProvider provider;

    private BrickCoreBinder() {
        provider = new StandardProvider();
    }

    public static BrickCoreBinder getBinder() {
        return BINDER;
    }

    public ContainerProvider getProvider() {
        return provider;
    }

    public String toString() {
        return MAGIC;
    }

}
