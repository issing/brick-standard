package net.isger.brick.auth;

import net.isger.brick.core.BaseCommand;
import net.isger.brick.plugin.PluginHandler;

public class AuthorizeHandler extends PluginHandler {

    public AuthorizeHandler() {
        this.setName(AuthCommand.OPERATE_AUTH);
        this.setOperate(AuthCommand.OPERATE_AUTH);
    }

    public BaseCommand toCommand(Object message) {
        AuthCommand cmd = (AuthCommand) message;
        Object token = super.toCommand(cmd.getToken());
        cmd.setDomain(null);
        cmd.setOperate(null);
        cmd.setToken(token);
        return cmd;
    }

    protected Object toResult(BaseCommand cmd, Object result) {
        if ((Boolean) result) {
            result = ((BaseCommand) ((AuthCommand) cmd).getToken()).getResult();
        }
        return result;
    }

}
