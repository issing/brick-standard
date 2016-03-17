package net.isger.brick.plugin.persist;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import net.isger.brick.stub.StubCommand;
import net.isger.util.Helpers;
import net.isger.util.Reflects;
import net.isger.util.Strings;
import net.isger.util.reflect.BoundMethod;

public class PersistProxy extends BasePersist {

    private Class<?> target;

    protected PersistProxy() {
        this(null);
    }

    protected PersistProxy(Class<?> target) {
        if (target == null) {
            target = this.getClass();
        }
        this.target = target;

    }

    protected void operate(String operate) {
        List<BoundMethod> boundMethods = Reflects.getBoundMethods(target).get(operate);
        if (boundMethods == null) {
            super.operate(operate);
            return;
        }
        StubCommand cmd = this.getStubCommand();
        BoundMethod boundMethod = boundMethods.get(0);
        Class<?>[] paramTypes = boundMethod.getMethod().getParameterTypes();
        Annotation[][] annos = boundMethod.getMethod().getParameterAnnotations();
        List<Object> params = new ArrayList<Object>();
        int size = paramTypes.length;
        String name;
        for (int i = 0; i < size; i++) {
            if (Helpers.hasAliasName(annos[i])) {
                name = Helpers.getAliasName(annos[i]);
            } else {
                name = operate + ":" + (i + 1);
            }
            params.add(cmd.getParameter(name));
        }

        String stubOperate = cmd.getStubOperate();
        if (Strings.isNotEmpty(stubOperate)) {
            cmd.setOperate(stubOperate);
        }
        cmd.setTable(target);
        cmd.setCondition(operate, params.toArray());
        toStub();
    }

}
