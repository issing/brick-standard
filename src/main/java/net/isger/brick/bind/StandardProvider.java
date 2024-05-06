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
        builder.constant(Constants.BRICK_ENCODING, Constants.ENCODING_UTF_8);
        builder.constant(Constants.BRICK_RAW, Constants.RAW_JSON);
    }

}
