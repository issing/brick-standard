package net.isger.brick.auth;

import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;

public class ShiroIdentity extends AuthIdentity {

    public ShiroIdentity(Subject subject) {
        super(subject);
    }

    public boolean isLogin() {
        return super.isLogin() && getToken().isAuthenticated();
    }

    public Subject getToken() {
        return (Subject) super.getToken();
    }

    public Object getAttribute(String name) {
        return getToken().getSession().getAttribute(name);
    }

    public void setAttribute(String name, Object value) {
        getToken().getSession().setAttribute(name, value);
    }

    public void clear() {
        super.clear();
        Session session = getToken().getSession();
        for (Object name : session.getAttributeKeys()) {
            session.removeAttribute(name);
        }
    }

}
