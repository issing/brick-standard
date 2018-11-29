package net.isger.brick.auth;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;

public class ShiroToken extends AuthToken<AuthToken<?>>
        implements AuthenticationToken {

    private static final long serialVersionUID = -740763605179398595L;

    private Subject subject;

    public ShiroToken() {
        this(null);
    }

    public ShiroToken(AuthToken<?> token) {
        super(token);
        this.subject = new Subject.Builder().buildSubject();
    }

    public Object getPrincipal() {
        return source.getPrincipal();
    }

    public Object getCredentials() {
        return source.getCredentials();
    }

    public Subject getSubject() {
        return subject;
    }

}
