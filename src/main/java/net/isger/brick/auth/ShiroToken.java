package net.isger.brick.auth;

import org.apache.shiro.authc.AuthenticationToken;

public class ShiroToken extends AuthToken<AuthToken<?>> implements
        AuthenticationToken {

    private static final long serialVersionUID = -740763605179398595L;

    public ShiroToken(AuthToken<?> token) {
        super(token);

    }

    public Object getPrincipal() {
        return source.getPrincipal();
    }

    public Object getCredentials() {
        return source.getCredentials();
    }

}
