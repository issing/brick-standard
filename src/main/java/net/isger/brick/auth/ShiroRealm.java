package net.isger.brick.auth;

import java.util.List;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import net.isger.brick.Constants;
import net.isger.brick.core.Console;
import net.isger.util.Asserts;
import net.isger.util.Helpers;
import net.isger.util.anno.Alias;
import net.isger.util.anno.Ignore;
import net.isger.util.anno.Ignore.Mode;

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

    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        AuthCommand cmd = AuthCommand.newAction();
        cmd.setOperate(AuthCommand.OPERATE_AUTH);
        List<?> token = principals.asList();
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        switch (token.size()) {
        case 1:
            if (token.get(0) instanceof AuthInfo) this.addAuthInfo(info, (AuthInfo) token.get(0));
            else break;
        case 0:
            return info;
        }
        cmd.setToken(token);
        try {
            this.console.execute(cmd);
            if (cmd.getResult() instanceof AuthInfo) this.addAuthInfo(info, (AuthInfo) cmd.getResult());
        } catch (Exception e) {
            throw Asserts.state("Failure to get authorization info", e);
        }
        return info;
    }

    private void addAuthInfo(SimpleAuthorizationInfo info, AuthInfo authInfo) {
        info.addRoles(authInfo.getRoles());
        info.addStringPermissions(authInfo.getPermissions());
    }

    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        return new SimpleAuthenticationInfo(token.getPrincipal(), token.getCredentials(), getName());
    }

}
