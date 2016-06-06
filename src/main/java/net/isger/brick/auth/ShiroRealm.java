package net.isger.brick.auth;

import net.isger.brick.Constants;
import net.isger.brick.core.Console;
import net.isger.brick.plugin.PluginCommand;
import net.isger.util.Helpers;
import net.isger.util.anno.Alias;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShiroRealm extends AuthorizingRealm {

    private static final String AUTH = "auth";

    private static final String PARAM_PRINCIPALS = "principals";

    private static final Logger LOG;

    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.SYSTEM)
    private Console console;

    private String plugin;

    private String operate;

    static {
        LOG = LoggerFactory.getLogger(ShiroRealm.class);
    }

    public ShiroRealm() {
        this.plugin = AUTH;
        this.operate = AUTH;
    }

    public String getName() {
        return Helpers.getAliasName(this.getClass(), "Realm$");
    }

    public boolean supports(AuthenticationToken token) {
        return token instanceof ShiroToken;
    }

    protected AuthorizationInfo doGetAuthorizationInfo(
            PrincipalCollection principals) {
        AuthCommand cmd = new AuthCommand();
        PluginCommand tokenCmd = PluginCommand.newAction();
        tokenCmd.setName(plugin);
        tokenCmd.setOperate(operate);
        tokenCmd.setParameter(PARAM_PRINCIPALS, principals.asList());
        cmd.setToken(tokenCmd);
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        try {
            console.execute(cmd);
            AuthInfo ai = (AuthInfo) tokenCmd.getResult();
            if (ai != null) {
                info.addRoles(ai.getRoles());
                info.addStringPermissions(ai.getPermissions());
            }
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e.getCause());
        }
        return info;
    }

    protected AuthenticationInfo doGetAuthenticationInfo(
            AuthenticationToken token) throws AuthenticationException {
        return new SimpleAuthenticationInfo(token.getPrincipal(),
                token.getCredentials(), getName());
    }

}
