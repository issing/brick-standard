package net.isger.brick.test.bus;

import net.isger.brick.auth.AuthIdentity;
import net.isger.brick.bus.Endpoint;
import net.isger.brick.bus.IdentityHandler;

public class TestHandler implements IdentityHandler {

    private String prefix;

    private int amount;

    public int getStatus() {
        return 1;
    }

    public void open(Endpoint endpoint, AuthIdentity identity) {
    }

    public void reload(Endpoint endpoint, AuthIdentity identity) {
    }

    public Object handle(Object message) {
        System.out.println(prefix + ": " + message);
        if (++amount == 10) {
            return null;
        }
        return message;
    }

    public Object handle(Endpoint endpoint, AuthIdentity identity, Object message) {
        return handle(message);
    }

    public void unload(Endpoint endpoint, AuthIdentity identity) {
    }

    public void close(Endpoint endpoint, AuthIdentity identity) {
    }

}
