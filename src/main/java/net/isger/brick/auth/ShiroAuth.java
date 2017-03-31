package net.isger.brick.auth;

import net.isger.brick.core.BaseCommand;
import net.isger.brick.core.Command;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.subject.Subject;

public class ShiroAuth extends BaseAuth {

    protected AuthIdentity createIdentity() {
        return new ShiroIdentity(new Subject.Builder().buildSubject());
    }

    protected Object login(AuthIdentity identity, Object token) {
        Subject subject = (Subject) identity.getToken();
        if (subject == null) {
            identity.setToken(subject = new Subject.Builder().buildSubject());
        }
        try {
            subject.login(makeToken(token));
        } catch (Exception e) {
            token = null;
        }
        return token;
    }

    private AuthenticationToken makeToken(Object token) {
        if (token instanceof AuthenticationToken) {
            return (AuthenticationToken) token;
        } else if (token instanceof ShiroToken) {
            return (ShiroToken) token;
        } else if (token instanceof AuthToken) {
            return new ShiroToken((AuthToken<?>) token);
        }
        return null;
    }

    protected Object check(AuthIdentity identity, Object token) {
        Subject subject = identity == null ? null : (Subject) identity
                .getToken();
        boolean result;
        try {
            if (result = subject != null) {
                String permission;
                if (token instanceof Command) {
                    permission = BaseCommand.cast((Command) token)
                            .getPermission();
                } else {
                    permission = token.toString();
                }
                subject.checkPermission(permission);
                // 更新会话时间
                subject.getSession().touch();
            }
        } catch (UnknownSessionException e) {
            result = false;
        } catch (AuthorizationException e) {
            throw new AuthException("Failure to check permission", e);
        }
        return result;
    }

    protected void logout(AuthIdentity identity) {
        Subject subject = identity == null ? null : (Subject) identity
                .getToken();
        if (subject != null) {
            subject.logout();
        }
        super.logout(identity);
    }
}
