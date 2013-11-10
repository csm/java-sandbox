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


import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import net.datenwerke.transloader.except.Assert;
import net.datenwerke.transloader.except.TransloaderException;
import net.datenwerke.transloader.reference.ReferenceDescription;

/**
 * Describes a field by its name, declaring class name and whether or not it is of a primitive type and/or transient.
 *
 * @author Jeremy Wales
 */
public final class FieldDescription implements ReferenceDescription {
    private final String declaringClassName;
    private final String fieldName;
    private final boolean isPrimitive;
    private final boolean isTransient;
    private final FieldSetter setter;

    /**
     * Constructs a <code>FieldDescription</code> derived from the given <code>Field</code>.
     *
     * @param field  the field from which this description is derived
     * @param setter the strategy by which the value of the field is set
     */
    public FieldDescription(Field field, FieldSetter setter) {
        this(
                ((Field) Assert.isNotNull(field)).getDeclaringClass().getName(),
                field.getName(),
                field.getType().isPrimitive(),
                Modifier.isTransient(field.getModifiers()),
                setter
        );
    }

    /**
     * Constructs a <code>FieldDescription</code> with the given declaring class name, field name,
     * isPrimitive indicator and isTransient indicator.
     *
     * @param declaringClassName the name of the <code>Class</code> that declares the field
     * @param fieldName          the name of the field
     * @param isPrimitive        indicator of whether the reference is to a primitive rather than an {@link Object}
     * @param isTransient        indicator of whether the reference is marked transient, excluded from serialization
     * @param setter             the strategy by which the value of the field is set
     */
    public FieldDescription(String declaringClassName, String fieldName, boolean isPrimitive, boolean isTransient, FieldSetter setter) {
        Assert.areNotNull(declaringClassName, fieldName, setter);
        this.declaringClassName = declaringClassName;
        this.fieldName = fieldName;
        this.isPrimitive = isPrimitive;
        this.isTransient = isTransient;
        this.setter = setter;
    }

    /**
     * Retrieves the value assigned to the field matching <code>this</code> description on the given object.
     *
     * @param referer the object from which to retrieve the field
     * @return the value of the field on the <code>referer</code>
     * @throws NoSuchFieldException if the <code>Field</code> named by <code>this</code> description does not exist on
     *                              the <code>Class</code> named by <code>this</code> description or the
     *                              <code>Class</code> named by <code>this</code> description is not in the
     *                              <code>referer</code>'s <code>Class</code> hierarchy
     * @throws TransloaderException if the field cannot be located on the <code>referer</code> or the configured
     *                              {@link FieldSetter} cannot set the <code>value</code>
     */
    public Object getValueFrom(Object referer) throws NoSuchFieldException {
        Assert.isNotNull(referer);
        try {
            return getFieldFrom(referer).get(referer);
        } catch (IllegalAccessException e) {
            throw new TransloaderException("Unable to get value for '" + fieldName + "' from '" + referer + "'.", e);
        }
    }

    /**
     * Assigns the given value to the field matching <code>this</code> description on the given object.
     *
     * @param referer the object on which to set the field
     * @param value   the value to set (can be <code>null</code>)
     * @throws TransloaderException if the installed Security Manager does not allow access to the field
     */
    public void setValueIn(Object referer, Object value) {
        Assert.areNotNull(referer, value);
        try {
            setter.set(value, getFieldFrom(referer), referer);
        } catch (Exception e) {
            throw new TransloaderException("Unable to set value for '" + fieldName + "' to '" + value + "' on '" + referer + "'.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isOfPrimitiveType() {
        return isPrimitive;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isTransient() {
        return isTransient;
    }

    /**
     * Retrieves the name of the field.
     *
     * @return the name of the field within the class.
     */
    public String getName() {
        return fieldName;
    }

    private Field getFieldFrom(Object referer) throws NoSuchFieldException {
        Class refererClass = referer.getClass();
        Class declaringClass = findMatchingClassFrom(refererClass);
        checkClassWasFoundFrom(refererClass, declaringClass);
        return getFieldFrom(declaringClass);
    }

    private Field getFieldFrom(Class declaringClass) throws NoSuchFieldException {
        Field field = declaringClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }

    private void checkClassWasFoundFrom(Class refererClass, Class declaringClass) throws NoSuchFieldException {
        if (declaringClass == null)
            throw new NoSuchFieldException(
                    "No Class named '" + declaringClassName + "' in the hierarchy of '" + refererClass.getName() + "' " +
                            "in ClassLoader '" + refererClass.getClassLoader() + "'."
            );
    }

    private Class findMatchingClassFrom(Class subclass) {
        if (subclass == Object.class)
            return null;
        return declaringClassName.equals(subclass.getName()) ? subclass : findMatchingClassFrom(subclass.getSuperclass());
    }
}
