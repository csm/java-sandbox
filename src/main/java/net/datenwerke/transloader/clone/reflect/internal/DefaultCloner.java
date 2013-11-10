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


import java.util.Map;
import java.util.Set;

import net.datenwerke.transloader.clone.reflect.decide.CloningDecisionStrategy;
import net.datenwerke.transloader.clone.reflect.instantiate.CloneInstantiater;
import net.datenwerke.transloader.except.Assert;
import net.datenwerke.transloader.reference.ReferenceReflecter;

/**
 * @author jeremywales
 */
public class DefaultCloner implements InternalCloner {
    private final CloningDecisionStrategy decider;
    private final CloneInstantiater instantiater;
    private final ReferenceReflecter reflecter;

    /**
     * Constructs a new <code>InternalCloner</code> with its dependencies injected.
     *
     * @param decider      the strategy by which the decision to clone or not to clone a particular given object is made
     * @param instantiater the strategy by which to instantiate shallow clones
     * @param reflecter    the stategy by which {@link net.datenwerke.transloader.reference.Reference}s are are created
     */
    public DefaultCloner(CloningDecisionStrategy decider, CloneInstantiater instantiater, ReferenceReflecter reflecter) {
        Assert.areNotNull(decider, instantiater, reflecter);
        this.decider = decider;
        this.instantiater = instantiater;
        this.reflecter = reflecter;
    }

    public Map mapReferencesFrom(Object original) throws IllegalAccessException {
        Assert.isNotNull(original);
        MapReferencesOperation operation = new MapReferencesOperation(original, reflecter);
        return operation.getReferences();
    }

    public Map mapClonesOf(Set originals, ClassLoader targetLoader) throws Exception {
        Assert.areNotNull(originals, targetLoader);
        MapClonesOperation operation = new MapClonesOperation(originals, targetLoader, decider, instantiater);
        return operation.getClones();
    }

    public void setClonesIn(Map references, Map clones) throws NoSuchFieldException {
        Assert.areNotNull(references, clones);
        new SetCloneReferencesOperation(references, clones);
    }
}
