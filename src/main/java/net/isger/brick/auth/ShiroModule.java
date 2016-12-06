package net.isger.brick.auth;

import net.isger.brick.core.Gate;
import net.isger.util.anno.Ignore;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SessionsSecurityManager;

public class ShiroModule extends AuthModule {

    private SessionsSecurityManager security;

    private ShiroRealm realm;

    public Class<? extends Gate> getBaseClass() {
        return ShiroAuth.class;
    }

    @Ignore
    public void initial() {
        super.initial();
        if (security == null) {
            security = new DefaultSecurityManager();
        }
        if (realm == null) {
            realm = container.inject(new ShiroRealm());
        }
        security.setRealm(realm);
        SecurityUtils.setSecurityManager(security);
    }

    @Ignore
    public void destroy() {
        super.destroy();
    }

}
