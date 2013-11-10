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

package net.datenwerke.transloader.clone.reflect.instantiate;

import net.datenwerke.transloader.except.Assert;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.objenesis.ObjenesisSerializer;

import java.io.Serializable;

/**
 * Uses {@link Objenesis} to create new instances of <code>Class</code>es without invoking their constructors.
 *
 * @author Jeremy Wales
 */
public final class ObjenesisInstantiationStrategy implements InstantiationStrategy {
    private final Objenesis standard = new ObjenesisStd();
    private final Objenesis serializer = new ObjenesisSerializer();

    /**
     * {@inheritDoc}
     */
    public Object newInstanceOf(Class type) throws Exception {
        Assert.isNotNull(type);
        Objenesis objenesis = Serializable.class.isAssignableFrom(type) ? serializer : standard;
        return objenesis.newInstance(type);
    }
}
