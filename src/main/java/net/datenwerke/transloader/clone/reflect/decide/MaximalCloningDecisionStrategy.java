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

import net.datenwerke.transloader.except.Assert;

/**
 * When injected into a {@link net.datenwerke.transloader.clone.reflect.ReflectionCloningStrategy}, decides that all given objects and those that they reference
 * should be cloned.
 *
 * @author Jeremy Wales
 */
public final class MaximalCloningDecisionStrategy implements CloningDecisionStrategy {
    /**
     * Decides that all objects should be shallow copied.
     *
     * @param original          ignored; returns <code>true</code> regardless
     * @param targetClassLoader ignored; returns <code>true</code> regardless
     * @return <code>true</code> always
     */
    public boolean shouldCloneObjectItself(Object original, ClassLoader targetClassLoader) {
        Assert.areNotNull(original, targetClassLoader);
        return true;
    }

    /**
     * Decides that all objects have their references copied.
     *
     * @param original          ignored; returns <code>true</code> regardless
     * @param targetClassLoader ignored; returns <code>true</code> regardless
     * @return <code>true</code> always
     */
    public boolean shouldCloneObjectReferences(Object original, ClassLoader targetClassLoader) {
        return shouldCloneObjectItself(original, targetClassLoader);
	}
}
