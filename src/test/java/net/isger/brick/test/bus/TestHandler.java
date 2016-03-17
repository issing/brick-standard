package net.isger.brick.test.bus;

import net.isger.brick.core.Handler;

public class TestHandler implements Handler {

    private String prefix;

    private int amount;

    public Object handle(Object message) {
        System.out.println(prefix + ": " + message);
        if (++amount == 10) {
            return null;
        }
        return message;
    }

}
