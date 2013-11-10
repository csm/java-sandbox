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

package net.datenwerke.transloader.reference;


import java.util.ArrayList;
import java.util.List;

import net.datenwerke.transloader.except.Assert;
import net.datenwerke.transloader.except.ImpossibleException;


public abstract class AbstractReflecter {
    protected final Object object;

    protected AbstractReflecter(Object object) {
        this.object = Assert.isNotNull(object);
    }

    protected abstract ReferenceDescription[] getAllReferenceDescriptions() throws IllegalAccessException;

    public final Reference[] getAllReferences() throws IllegalAccessException {
        ReferenceDescription[] descriptions = getAllReferenceDescriptions();
        List references = new ArrayList(descriptions.length);
        for (int i = 0; i < descriptions.length; i++)
            add(descriptions[i], references);
        return (Reference[]) references.toArray(new Reference[references.size()]);
    }

    private void add(ReferenceDescription description, List references) {
        Object value = getValueOf(description);
        Reference reference = new Reference(description, value);
        references.add(reference);
    }

    private Object getValueOf(ReferenceDescription description) {
        try {
            Object value = description.getValueFrom(object);
            return value == null ? Reference.NULL : value;
        } catch (NoSuchFieldException e) {
            throw new ImpossibleException(e);
        }
    }
}
