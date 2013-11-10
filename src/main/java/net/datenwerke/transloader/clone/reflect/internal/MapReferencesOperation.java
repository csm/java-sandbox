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

import net.datenwerke.transloader.except.Assert;
import net.datenwerke.transloader.reference.Reference;
import net.datenwerke.transloader.reference.ReferenceReflecter;

import org.apache.commons.collections.map.IdentityMap;

import java.util.Map;

/**
 * @author jeremywales
 */
public final class MapReferencesOperation {
    private final ReferenceReflecter reflecter;
    private final Map references = new IdentityMap();

    public MapReferencesOperation(Object original, ReferenceReflecter reflecter) throws IllegalAccessException {
        Assert.areNotNull(original, reflecter);
        this.reflecter = reflecter;
        recursivelyMapAllReferencesFrom(original);
    }

    private void recursivelyMapAllReferencesFrom(Object original) throws IllegalAccessException {
        Reference[] references = mapReferencesFrom(original);
        for (int i = 0; i < references.length; i++)
            if (shouldMap(references[i]))
                recursivelyMapAllReferencesFrom(references[i].getValue());
    }

    private Reference[] mapReferencesFrom(Object original) throws IllegalAccessException {
        Reference[] refs = reflecter.reflectReferencesFrom(original);
        references.put(original, refs);
        return refs;
    }

    private boolean shouldMap(Reference reference) {
        boolean notPrimitive = !reference.getDescription().isOfPrimitiveType();
        boolean notAlreadyMapped = !references.containsKey(reference.getValue());
        return notPrimitive && notAlreadyMapped;
    }

    public Map getReferences() {
        return references;
    }
}
