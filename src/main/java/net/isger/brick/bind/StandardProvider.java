package net.isger.brick.bind;

import java.nio.charset.Charset;

import net.isger.brick.Constants;
import net.isger.brick.core.Console;
import net.isger.brick.inject.Container;
import net.isger.brick.inject.ContainerBuilder;
import net.isger.brick.inject.ContainerProvider;
import net.isger.brick.inject.Scope;
import net.isger.util.Callable;

/**
 * 核心供应器
 * 
 * @author issing
 *
 */
public class StandardProvider implements ContainerProvider {

    public boolean isReload() {
        return false;
    }

    public void register(ContainerBuilder builder) {
        builder.factory(Console.class, Constants.SYSTEM, StandardConsole.class, Scope.SINGLETON);
        builder.constant(Constants.BRICK_ENCODING, Constants.DEFAULT_ENCODING);
        builder.constant(Constants.BRICK_RAW, Constants.RAW_JSON);
        builder.factory(Charset.class, Constants.BRICK_ENCODING, new Callable<Charset>() {
            public Charset call(Object... args) {
                Container container = (Container) args[0];
                return Charset.forName(container.getInstance(String.class, Constants.BRICK_ENCODING));
            }
        });
    }

}
