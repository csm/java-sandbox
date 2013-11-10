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

package net.datenwerke.transloader.clone;

import net.datenwerke.transloader.except.Assert;

import org.apache.commons.io.input.ClassLoaderObjectInputStream;
import org.apache.commons.lang.SerializationException;
import org.apache.commons.lang.SerializationUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * A <code>CloningStrategy</code> that uses Java Serialization as its mechanism.
 *
 * @author Jeremy Wales
 */
public final class SerializationCloningStrategy implements CloningStrategy {
    /**
     * {@inheritDoc}
     *
     * @throws ClassCastException     if the given <code>original</code> object is not {@link Serializable}
     * @throws SerializationException if serialization fails
     * @throws IOException            if input fails during deserialization
     * @throws ClassNotFoundException if the <code>targetClassLoader</code> cannot find a required class
     */
    public Object cloneObjectUsing(ClassLoader targetLoader, Object original) throws ClassCastException, SerializationException, IOException, ClassNotFoundException {
        Assert.areNotNull(targetLoader, original);
        byte[] serializedOriginal = SerializationUtils.serialize((Serializable) original);
        return deserialize(serializedOriginal, targetLoader);
	}

    private Object deserialize(byte[] serializedOriginal, ClassLoader targetLoader) throws ClassNotFoundException, IOException {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(serializedOriginal);
        ClassLoaderObjectInputStream objectStream = new ClassLoaderObjectInputStream(targetLoader, byteStream);
        return objectStream.readObject();
    }
}
