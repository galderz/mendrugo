package org.example.elytron.graal;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import org.wildfly.common.Assert;

import javax.security.auth.Destroyable;
import java.security.Key;
import java.util.function.UnaryOperator;

/**
 * Not needed if KeyUtil.CLONER_CREATOR would be instance level field rather than static.
 * Because it's static, the substitutions don't get applied so the calls have to be replicated manually.
 */
@TargetClass(className = "org.wildfly.security.key.KeyUtil")
final class Target_org_wildfly_security_key_KeyUtil
{
    @Substitute
    public static <T extends Key> T cloneKey(Class<T> expectType, T key)
    {
        Assert.checkNotNullParam("expectType", expectType);
        if (key instanceof Destroyable)
        {
            // medium path
            if (((Destroyable) key).isDestroyed())
            {
                return expectType.cast(key);
            }
            else
            {
                final var CLONER_CREATOR = new Target_org_wildfly_security_key_KeyUtil_KeyClonerCreator();
                System.out.printf("Call cloner creator compute with key type %s%n", key.getClass());
                return expectType.cast(CLONER_CREATOR.computeValue(key.getClass()).apply(key));
            }
        }
        else
        {
            // fast path
            return expectType.cast(key);
        }
    }
}
