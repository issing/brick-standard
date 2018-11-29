package net.isger.brick.auth;

import org.apache.shiro.session.Session;

public class ShiroIdentity extends AuthIdentity {

    public boolean isLogin() {
        return super.isLogin() && getToken().getSubject().isAuthenticated();
    }

    public void active(boolean create) {
        super.active(create);
        getToken().getSubject().getSession(create).touch();
    }

    public void setTimeout(int timeout) {
        getToken().getSubject().getSession().setTimeout(timeout);
    }

    public ShiroToken getToken() {
        return (ShiroToken) super.getToken();
    }

    public void setToken(AuthToken<?> token) {
        if (!(token == null || token instanceof ShiroToken)) {
            token = new ShiroToken(token);
        }
        super.setToken(token);
    }

    public Object getAttribute(String name) {
        return getToken().getSubject().getSession().getAttribute(name);
    }

    public void setAttribute(String name, Object value) {
        Session session = getToken().getSubject().getSession();
        if (value == null) {
            session.removeAttribute(name);
        } else {
            session.setAttribute(name, value);
        }
    }

    public void clear() {
        Session session = getToken().getSubject().getSession();
        for (Object name : session.getAttributeKeys()) {
            session.removeAttribute(name);
        }
        super.clear();
    }

}
