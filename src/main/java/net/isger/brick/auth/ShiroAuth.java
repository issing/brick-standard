package net.isger.brick.auth;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.WildcardPermission;
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

    protected ShiroToken login(AuthIdentity identity, AuthToken<?> token) {
        /* 访问令牌 */
        Object pending = identity.getToken();
        if (!(pending instanceof ShiroToken || ((pending = transform(token)) != null))) return null;
        /* 登录操作 */
        return this.login(identity, (ShiroToken) pending);
    }

    private ShiroToken login(AuthIdentity identity, ShiroToken token) {
        try {
            token.getSubject().login(token);
        } catch (Exception e) {
            LOG.warn("(!) Failure to login", e);
            return null;
        }
        return (ShiroToken) super.login(identity, token);
    }

    protected Object check(AuthIdentity identity, Object token) {
        ShiroToken shiroToken = this.login(identity, identity.getToken());
        if (shiroToken == null) return false;
        boolean result = true;
        try {
            String permission;
            if (token instanceof Command) permission = BaseCommand.cast((Command) token).getPermission();
            else permission = token.toString();
            Subject subject = shiroToken.getSubject();
            subject.getSession().touch(); // 更新会话时间
            subject.checkPermission(permission);
        } catch (Throwable e) {
            result = false;
        }
        return result;
    }

    protected boolean isIgnore(Object token) {
        if (token instanceof Command) {
            Permission permission = new WildcardPermission(BaseCommand.cast((Command) token).getPermission().replaceAll("[.]", ":"));
            for (String ignore : this.checker.getIgnores()) {
                if (new WildcardPermission(ignore.replaceAll("[.]", ":")).implies(permission)) {
                    return true;
                }
            }
        }
        return super.isIgnore(token);
    }

    protected void logout(AuthIdentity identity) {
        Object pending = identity.getToken();
        if (pending instanceof ShiroToken) {
            try {
                ((ShiroToken) pending).getSubject().logout();
            } catch (Exception e) {
                LOG.warn("Failure to logout [{}]", e.getMessage(), e.getCause());
            }
        }
        super.logout(identity);
    }

    /**
     * 令牌转换
     * 
     * @param token
     * @return
     */
    public static ShiroToken transform(Object token) {
        if (!(token instanceof ShiroToken)) {
            if (token instanceof AuthenticationToken) {
                token = new ShiroToken(new AuthToken<AuthenticationToken>((AuthenticationToken) token) {
                    public Object getPrincipal() {
                        return this.source.getPrincipal();
                    }

                    public Object getCredentials() {
                        return this.source.getCredentials();
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
}
