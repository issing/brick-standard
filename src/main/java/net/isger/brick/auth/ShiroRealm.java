package net.isger.brick.auth;

import net.isger.brick.Constants;
import net.isger.brick.core.Console;
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

/**
 * 授权域
 * 
 * @author issing
 *
 */
public class ShiroRealm extends AuthorizingRealm {

    @Ignore(mode = Mode.INCLUDE)
    @Alias(Constants.SYSTEM)
    private Console console;

    public String getName() {
        return Helpers.getAliasName(this.getClass(), "Realm$");
    }

    public boolean supports(AuthenticationToken token) {
        return token instanceof ShiroToken;
    }

    protected AuthorizationInfo doGetAuthorizationInfo(
            PrincipalCollection principals) {
        AuthCommand cmd = AuthCommand.newAction();
        cmd.setOperate(AuthCommand.OPERATE_AUTH);
        cmd.setToken(principals.asList());
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        try {
            console.execute(cmd);
            AuthInfo ai = (AuthInfo) cmd.getResult();
            if (ai != null) {
                info.addRoles(ai.getRoles());
                info.addStringPermissions(ai.getPermissions());
            }
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failure to get authorization info", e);
        }
        return info;
    }

    protected AuthenticationInfo doGetAuthenticationInfo(
            AuthenticationToken token) throws AuthenticationException {
        return new SimpleAuthenticationInfo(token.getPrincipal(),
                token.getCredentials(), getName());
    }

}
