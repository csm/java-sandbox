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
import net.datenwerke.transloader.reference.ReferenceDescription;

/**
 * Describes an array element by its index and whether or not it is of a primitive type.
 *
 * @author Jeremy Wales
 */
public final class ElementDescription implements ReferenceDescription {
    private final int elementIndex;
    private final boolean primitive;

    /**
     * Constructs an <code>ArrayElementDescription</code> with the given element index.
     *
     * @param elementIndex the index within the array of element being described
     * @param primitive    indicator of whether the reference is to a primitive rather than an {@link Object}
     */
    public ElementDescription(int elementIndex, boolean primitive) {
        this.elementIndex = elementIndex;
        this.primitive = primitive;
    }

    /**
     * Retrieves the value from the given array at the element index in <code>this</code> description.
     *
     * @param array the array from which to retrieve the value
     * @return the value at the relevant element index
     */
    public Object getValueFrom(Object array) {
        return Array.get(Assert.isArray(array), elementIndex);
    }

    /**
     * Assigns the given value to the field matching <code>this</code> description on the given object.
     *
     * @param array the object on which to set the field
     * @param value the value to set (can be <code>null</code>)
     */
    public void setValueIn(Object array, Object value) {
        Assert.areNotNull(array, value);
        Array.set(Assert.isArray(array), elementIndex, value);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isOfPrimitiveType() {
        return primitive;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isTransient() {
        return false;
    }

    /**
     * Retreives the element index.
     *
     * @return the index within the array of the element reference.
     */
    public String getName() {
        return elementIndex + "";
    }
}