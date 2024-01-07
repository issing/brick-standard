package net.isger.brick.auth;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SessionsSecurityManager;

import net.isger.brick.core.Gate;
import net.isger.util.anno.Ignore;

public class ShiroModule extends AuthModule {

    private SessionsSecurityManager security;

    private ShiroRealm realm;

    public Class<? extends Gate> getBaseClass() {
        return ShiroAuth.class;
    }

    @Ignore
    public void initial() {
        super.initial();
        if (this.security == null) this.security = new DefaultSecurityManager();
        if (this.realm == null) this.realm = this.container.inject(new ShiroRealm());
        this.security.setRealm(this.realm);
        SecurityUtils.setSecurityManager(this.security);
    }

    @Ignore
    public void destroy() {
        super.destroy();
    }

}
