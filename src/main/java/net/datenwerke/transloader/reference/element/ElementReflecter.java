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

package net.datenwerke.transloader.reference.element;


import java.lang.reflect.Array;

import net.datenwerke.transloader.except.Assert;
import net.datenwerke.transloader.reference.AbstractReflecter;
import net.datenwerke.transloader.reference.ReferenceDescription;

/**
 * @author jeremywales
*/
public final class ElementReflecter extends AbstractReflecter {
    public ElementReflecter(Object array) {
        super(Assert.isArray(array));
    }

    public ReferenceDescription[] getAllReferenceDescriptions() {
        ElementDescription[] descriptions = new ElementDescription[Array.getLength(object)];
        for (int i = 0; i < descriptions.length; i++)
            descriptions[i] = new ElementDescription(i, isPrimitive());
        return descriptions;
    }

    private boolean isPrimitive() {
        return object.getClass().getComponentType().isPrimitive();
    }
}
