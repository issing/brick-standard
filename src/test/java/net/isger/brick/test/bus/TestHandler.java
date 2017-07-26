package net.isger.brick.test.bus;

import net.isger.brick.auth.AuthIdentity;
import net.isger.brick.bus.IdentityHandler;

public class TestHandler implements IdentityHandler {

    private String prefix;

    private int amount;

    public Object handle(Object message) {
        System.out.println(prefix + ": " + message);
        if (++amount == 10) {
            return null;
        }
        return message;
    }

    public Object handle(AuthIdentity identity, Object message) {
        return handle(message);
    }

}
