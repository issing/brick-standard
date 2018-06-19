package net.isger.brick.auth;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.isger.brick.core.BaseCommand;
import net.isger.brick.core.Command;

public class ShiroAuth extends BaseAuth {

    private static final Logger LOG;

    static {
        LOG = LoggerFactory.getLogger(ShiroAuth.class);
    }

    protected AuthIdentity createIdentity() {
        return new ShiroIdentity();
    }

    protected AuthToken<?> login(AuthIdentity identity, AuthToken<?> token) {
        /* 访问令牌 */
        Object pending = identity.getToken();
        if (!(pending instanceof ShiroToken
                || ((pending = makeToken(token)) != null))) {
            return null;
        }
        /* 登录操作 */
        ShiroToken shiroToken = (ShiroToken) pending;
        try {
            shiroToken.getSubject().login(shiroToken);
            shiroToken.getSubject().getSession(true).touch();
        } catch (Exception e) {
            LOG.warn("Failure to login", e);
            shiroToken = null;
        }
        return super.login(identity, shiroToken);
    }

    private ShiroToken makeToken(Object token) {
        if (!(token instanceof ShiroToken)) {
            if (token instanceof AuthenticationToken) {
                token = new ShiroToken(new AuthToken<AuthenticationToken>(
                        (AuthenticationToken) token) {
                    public Object getPrincipal() {
                        return source.getPrincipal();
                    }

                    public Object getCredentials() {
                        return source.getCredentials();
                    }
                });
            } else if (token instanceof AuthToken) {
                token = new ShiroToken((AuthToken<?>) token);
            } else {
                token = null;
            }
        }
        return (ShiroToken) token;
    }

    protected Object check(AuthIdentity identity, Object token) {
        Object pending = identity.getToken();
        if (!(pending instanceof ShiroToken)) {
            return false;
        }
        boolean result = true;
        try {
            String permission;
            if (token instanceof Command) {
                permission = BaseCommand.cast((Command) token).getPermission();
            } else {
                permission = token.toString();
            }
            Subject subject = ((ShiroToken) pending).getSubject();
            subject.checkPermission(permission);
            // 更新会话时间
            subject.getSession().touch();
        } catch (Throwable e) {
            result = false;
        }
        return result;
    }

    protected void logout(AuthIdentity identity) {
        Object pending = identity.getToken();
        if (pending instanceof ShiroToken) {
            ((ShiroToken) pending).getSubject().logout();
        }
        super.logout(identity);
    }
}
