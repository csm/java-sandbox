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
import java.util.Iterator;

import net.datenwerke.transloader.except.Assert;
import net.datenwerke.transloader.reference.Reference;
import net.datenwerke.transloader.reference.ReferenceDescription;

/**
 * @author jeremywales
 */
public class SetCloneReferencesOperation {
    private final Map references;
    private final Map clones;

    public SetCloneReferencesOperation(Map references, Map clones) throws NoSuchFieldException {
        Assert.areNotNull(references, clones);
        this.references = references;
        this.clones = clones;
        setClonesReferences();
    }

    public void setClonesReferences() throws NoSuchFieldException {
        Iterator originals = references.keySet().iterator();
        while (originals.hasNext())
            setReferencesForCloneOf(originals.next());
    }

    private void setReferencesForCloneOf(Object original) throws NoSuchFieldException {
        Object clone = getCloneOf(original);
        setCloneReferencesFrom(original, clone);
    }

    private void setCloneReferencesFrom(Object original, Object clone) throws NoSuchFieldException {
        Reference[] originalReferences = (Reference[]) references.get(original);
        for (int i = 0; i < originalReferences.length; i++)
            setCloneOf(originalReferences[i], clone);
    }

    private void setCloneOf(Reference originalReference, Object clone) throws NoSuchFieldException {
        Object cloneValue = getCloneOf(originalReference.getValue());
        ReferenceDescription description = originalReference.getDescription();
        if (shouldSet(cloneValue))
            description.setValueIn(clone, cloneValue);
    }

    private boolean shouldSet(Object transformation) throws NoSuchFieldException {
        return transformation != Reference.NULL;
    }

    private Object getCloneOf(Object original) {
        return clones.containsKey(original) ? clones.get(original) : original;
    }
}
