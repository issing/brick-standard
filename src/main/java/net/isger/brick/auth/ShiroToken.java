package net.isger.brick.auth;

import org.apache.shiro.authc.AuthenticationToken;

public class ShiroToken extends AuthToken implements AuthenticationToken {

    private static final long serialVersionUID = -740763605179398595L;

    public ShiroToken(AuthToken token) {
        super(token);

    }

    public AuthToken getSource() {
        return (AuthToken) super.getSource();
    }

    public Object getPrincipal() {
        return getSource().getPrincipal();
    }

    public Object getCredentials() {
        return getSource().getCredentials();
    }

}
