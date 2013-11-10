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

import net.datenwerke.transloader.except.Assert;
import net.datenwerke.transloader.reference.element.ElementReflecter;
import net.datenwerke.transloader.reference.field.FieldReflecter;
import net.datenwerke.transloader.reference.field.FieldSetter;
import net.datenwerke.transloader.reference.field.NoSetter;

/**
 * @author jeremywales
 */
public class DefaultReflecter implements ReferenceReflecter {
    private final FieldSetter setter;

    public DefaultReflecter() {
        this(NoSetter.INSTANCE);
    }

    public DefaultReflecter(FieldSetter setter) {
        Assert.isNotNull(setter);
        this.setter = setter;
    }

    private AbstractReflecter reflecterFor(Object subject) {
        return subject.getClass().isArray() ?
                new ElementReflecter(subject) :
                (AbstractReflecter) new FieldReflecter(subject, setter);
    }

    public Reference[] reflectReferencesFrom(Object referer) throws IllegalAccessException {
        Assert.isNotNull(referer);
        return reflecterFor(referer).getAllReferences();
    }
}
