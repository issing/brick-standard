package net.isger.brick.auth;

import net.isger.brick.core.BaseCommand;
import net.isger.brick.core.Command;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;

public class ShiroAuth extends BaseAuth {

    protected Object save(String identity, Object token) {
        Subject subject;
        try {
            subject = SecurityUtils.getSubject();
            subject.login(makeToken(token));
        } catch (Exception e) {
            return null;
        }
        return super.save(identity, subject);
    }

    private AuthenticationToken makeToken(Object token) {
        if (token instanceof AuthenticationToken) {
            return (AuthenticationToken) token;
        } else if (token instanceof AuthToken) {
            return new ShiroToken((AuthToken) token);
        }
        return null;
    }

    protected Object auth(String identity, Object token) {
        try {
            Subject subject = (Subject) super.auth(identity, token);
            String permission;
            if (token instanceof Command) {
                permission = BaseCommand.cast((Command) token).getPermission();
            } else {
                permission = token.toString();
            }
            subject.checkPermission(permission);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
