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

package net.datenwerke.transloader.clone.reflect;


import java.util.Map;

import net.datenwerke.transloader.clone.CloningStrategy;
import net.datenwerke.transloader.clone.reflect.internal.InternalCloner;
import net.datenwerke.transloader.except.Assert;

/**
 * A <code>CloningStrategy</code> that uses Java Reflection as its mechanism. Can clone whole object graphs or just
 * necessary parts depending on how it is configured.
 *
 * @author Jeremy Wales
 */
public final class ReflectionCloningStrategy implements CloningStrategy {
    private final InternalCloner cloner;

    /**
     * Constructs a new <code>ReflectionCloningStrategy</code> with its dependencies injected.
     *
     * @param cloner encapsulates the overall reflective cloning algorithm
     */
    public ReflectionCloningStrategy(InternalCloner cloner) {
        Assert.isNotNull(cloner);
        this.cloner = cloner;
    }

    /**
     * {@inheritDoc}
     *
     * @return a completely or partially cloned object graph, depending on the <code>CloningDecisionStrategy</code>
     *         injected, with potentially the <code>original</code> itself being the top-level object in the graph
     *         returned if it was not cloned
     */
    public Object cloneObjectUsing(final ClassLoader targetLoader, final Object original) throws Exception {
        Assert.areNotNull(targetLoader, original);
        Map references = cloner.mapReferencesFrom(original);
        Map clones = cloner.mapClonesOf(references.keySet(), targetLoader);
        cloner.setClonesIn(references, clones);
        return clones.get(original);
    }
}
