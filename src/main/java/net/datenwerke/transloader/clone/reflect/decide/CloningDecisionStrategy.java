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

/**
 * A strategy interface for customising which parts of an object graph {@link net.datenwerke.transloader.clone.reflect.ReflectionCloningStrategy} should actually
 * clone.
 *
 * @author Jeremy Wales
 */
public interface CloningDecisionStrategy {
    /**
     * Determines whether the given object (not considering any objects it references) should be shallow copied.
     *
     * @param original          the candidate for cloning
     * @param targetClassLoader the <code>ClassLoader</code> with which it may be cloned with
     * @return <code>true</code> if the <code>original</code> should be shallow cloned
     * @throws ClassNotFoundException if the <code>targetClassLoader</code> cannot be used to clone the
     *                                <code>original</code> because it cannot find a required <code>Class</code>
     */
    boolean shouldCloneObjectItself(Object original, ClassLoader targetClassLoader) throws ClassNotFoundException;

    /**
     * Determines whether the objects referenced by the given object should themselves be considered for cloning.
     *
     * @param original          the object referencing potential candidates for cloning
     * @param targetClassLoader the <code>ClassLoader</code> with which the objects referenced by
     *                          <code>original</code> may be cloned
     * @return <code>true</code> if the objects referenced by <code>original</code> should themselves be considered
     *         for cloning
     * @throws ClassNotFoundException if the <code>targetClassLoader</code> cannot be used to clone the objects
     *                                refernced by <code>original</code> because it cannot find a required <code>Class</code>
     */
    boolean shouldCloneObjectReferences(Object original, ClassLoader targetClassLoader) throws ClassNotFoundException;
}
