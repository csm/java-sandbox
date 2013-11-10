/*
*  transloader
*    
*  This file is part of transloader http://code.google.com/p/transloader/ as part
*  of the java-sandbox https://sourceforge.net/p/dw-sandbox/
*
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/

package net.datenwerke.transloader.clone.reflect.decide;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import net.datenwerke.transloader.ClassWrapper;
import net.datenwerke.transloader.except.Assert;
import net.datenwerke.transloader.primitive.Wrapper;

/**
 * When injected into a {@link net.datenwerke.transloader.clone.reflect.ReflectionCloningStrategy}, decides that only those objects whose <code>Class</code>es
 * would be different if loaded through the other <code>ClassLoader</code> should be cloned.
 *
 * @author Jeremy Wales
 */
public final class MinimalCloningDecisionStrategy implements CloningDecisionStrategy {
    /**
     * The list of primitive wrapper <code>Class</code>es.
     */
    public static final List PRIMITIVE_WRAPPERS = Arrays.asList(Wrapper.LIST);

    // TODO test BigInteger and BigDecimal
    // TODO make the list of non-cloned Classes injectable
    private static final List KNOWN_SHARED_IMMUTABLES = Arrays.asList(new Class[]{String.class, BigInteger.class, BigDecimal.class});

    /**
     * Decides that the given object should be shallow copied if its <code>Class</code> would be different when loaded
     * through the given <code>ClassLoader</code>.
     *
     * @param original          the candidate for cloning
     * @param targetClassLoader the <code>ClassLoader</code> it may be cloned with
     * @return <code>true</code> if <code>original</code>'s <code>Class</code> would be different when loaded
     *         through <code>targetClassLoader</code>
     */
    public boolean shouldCloneObjectItself(Object original, ClassLoader targetClassLoader) {
        Assert.areNotNull(original, targetClassLoader);
        return !isSameInClassLoader(original.getClass(), targetClassLoader);
    }

    private boolean isSameInClassLoader(Class originalClass, ClassLoader targetClassLoader) {
        return originalClass.equals(ClassWrapper.getClassFrom(targetClassLoader, originalClass.getName()));
    }

    /**
     * Decides to not even consider cloning the objects referenced by the given object if it is known to an immutable
     * object shared of a type shared among all <code>ClassLoader</code>s e.g. primitive wrappers and
     * <code>String</code>s.
     *
     * @param original          the candidate for cloning
     * @param targetClassLoader the <code>ClassLoader</code> it may be cloned with; ignored in this implementation
     * @return <code>true</code> unless <code>original</code>'s <code>Class</code> makes it a known immutable of
     *         type shared among all <code>ClassLoader</code>s
     */
    public boolean shouldCloneObjectReferences(Object original, ClassLoader targetClassLoader) {
        Assert.areNotNull(original, targetClassLoader);
        return !isEffectivelyPrimitive(original.getClass());
    }

    private boolean isEffectivelyPrimitive(Class originalClass) {
        return PRIMITIVE_WRAPPERS.contains(originalClass) || KNOWN_SHARED_IMMUTABLES.contains(originalClass);
	}
}
