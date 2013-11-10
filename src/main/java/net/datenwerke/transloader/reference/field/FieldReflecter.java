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

package net.datenwerke.transloader.reference.field;


import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import net.datenwerke.transloader.except.Assert;
import net.datenwerke.transloader.reference.AbstractReflecter;
import net.datenwerke.transloader.reference.ReferenceDescription;

/**
 * @author jeremywales
*/
public final class FieldReflecter extends AbstractReflecter {
    private final FieldSetter setter;

    public FieldReflecter(Object object, FieldSetter setter) {
        super(object);
        Assert.isNotNull(setter);
        this.setter = setter;
    }

    public ReferenceDescription[] getAllReferenceDescriptions() throws IllegalAccessException {
        return getAllInstanceFieldDescriptionsFor(object.getClass());
    }

    private FieldDescription[] getAllInstanceFieldDescriptionsFor(Class currentClass) {
        List descriptions = new ArrayList();
        while (currentClass != null) {
            List currentDescriptions = getDescriptionsDirectlyFrom(currentClass);
            descriptions.addAll(currentDescriptions);
            currentClass = currentClass.getSuperclass();
        }
        return (FieldDescription[]) descriptions.toArray(new FieldDescription[descriptions.size()]);
    }

    private List getDescriptionsDirectlyFrom(Class currentClass) {
        Field[] fields = currentClass.getDeclaredFields();
        List descriptions = new ArrayList(fields.length);
        for (int i = 0; i < fields.length; i++)
            if (isInstance(fields[i]))
                descriptions.add(new FieldDescription(fields[i], setter));
        return descriptions;
    }

    private boolean isInstance(Field field) {
        return !Modifier.isStatic(field.getModifiers());
    }
}
