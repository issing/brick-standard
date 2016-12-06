package net.isger.brick.auth;

import net.isger.brick.core.BaseCommand;
import net.isger.brick.core.Command;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.subject.Subject;

public class ShiroAuth extends BaseAuth {

    protected Object save(String identity, Object token) {
        Subject subject = (Subject) super.check(identity, token);
        if (subject != null) {
            try {
                subject.logout();
            } catch (Exception e) {
            }
        }
        subject = (new Subject.Builder()).buildSubject();
        try {
            subject.login(makeToken(token));
            super.save(identity, subject);
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

    protected Object check(String identity, Object token) {
        Subject subject = (Subject) super.check(identity, token);
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
            }
        } catch (UnknownSessionException e) {
            super.save(identity, null);
            result = false;
        } catch (AuthorizationException e) {
            throw new AuthException("Failure to check permission", e);
        }
        return result;
    }
}
