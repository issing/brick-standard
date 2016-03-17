package net.isger.brick.stub;

import net.isger.brick.core.Gate;
import net.isger.brick.core.GateModule;
import net.isger.util.Asserts;

/**
 * 存根模块
 * 
 * @author issing
 * 
 */
public class StubModule extends GateModule {

    public Class<? extends Gate> getTargetClass() {
        Class<? extends Gate> targetClass = (Class<? extends Gate>) super
                .getTargetClass();
        if (targetClass == null) {
            targetClass = Stub.class;
        } else {
            Asserts.argument(Stub.class.isAssignableFrom(targetClass),
                    "The stub " + targetClass + " must implement the "
                            + Stub.class);
        }
        return targetClass;
    }

    public Class<? extends Gate> getImplementClass() {
        return SqlStub.class;
    }

}
