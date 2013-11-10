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

package net.datenwerke.transloader.clone.reflect.internal;

import net.datenwerke.transloader.clone.reflect.decide.CloningDecisionStrategy;
import net.datenwerke.transloader.clone.reflect.instantiate.CloneInstantiater;
import net.datenwerke.transloader.except.Assert;
import net.datenwerke.transloader.reference.Reference;

import org.apache.commons.collections.map.IdentityMap;

import java.util.Map;
import java.util.Set;
import java.util.Iterator;

/**
 * @author jeremywales
 */
public final class MapClonesOperation {
    private final ClassLoader targetLoader;
    private final CloningDecisionStrategy decider;
    private final CloneInstantiater instantiater;

    private final Map clones = new IdentityMap();

    MapClonesOperation(Set originals, ClassLoader targetLoader, CloningDecisionStrategy decider, CloneInstantiater instantiater) throws Exception {
        Assert.areNotNull(originals, targetLoader, decider, instantiater);
        this.targetLoader = targetLoader;
        this.decider = decider;
        this.instantiater = instantiater;
        mapClonesFrom(originals);
    }

    private void mapClonesFrom(Set originals) throws Exception {
        for (Iterator iterator = originals.iterator(); iterator.hasNext();)
            mapCloneFrom(iterator.next());
    }

    private void mapCloneFrom(Object original) throws Exception {
        Object clone = original;
        if (shouldClone(original))
            clone = instantiater.instantiateShallowCloneOf(original, targetLoader);
        clones.put(original, clone);
    }

    private boolean shouldClone(Object original) throws ClassNotFoundException {
        boolean notNull = original != Reference.NULL;
        boolean deciderSaysSo = decider.shouldCloneObjectItself(original, targetLoader);
        return notNull && deciderSaysSo;
    }

    public Map getClones() {
        return clones;
    }
}
