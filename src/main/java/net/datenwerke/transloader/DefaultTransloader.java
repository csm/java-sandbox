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

package net.datenwerke.transloader;

import net.datenwerke.transloader.clone.CloningStrategy;
import net.datenwerke.transloader.except.Assert;
import net.datenwerke.transloader.load.CollectedClassLoader;

/**
 * The default implementation of <code>Transloader</code>.
 *
 * @author Jeremy Wales
 */
public final class DefaultTransloader implements Transloader {
    private final CloningStrategy cloningStrategy;

    /**
     * Constructs a new <code>Transloader</code> to produce wrappers, the <code>ObjectWrapper</code>s being
     * configured with the given <code>CloningStrategy</code>.
     *
     * @param cloningStrategy the <code>CloningStrategy</code> with which to configure <code>ObjectWrapper</code>s
     */
    public DefaultTransloader(CloningStrategy cloningStrategy) {
        Assert.isNotNull(cloningStrategy);
        this.cloningStrategy = cloningStrategy;
    }

    /**
     * {@inheritDoc}
     *
     * @return an <code>ObjectWrapper</code> around the given object, configured with the {@link CloningStrategy} that
     *         this factory is configured with and with a {@link CollectedClassLoader} for the given object as the
     *         parameter <code>ClassLoader</code>
     */
    public ObjectWrapper wrap(Object objectToWrap) {
        return new ObjectWrapper(objectToWrap, cloningStrategy, new CollectedClassLoader(objectToWrap));
    }

    /**
     * {@inheritDoc}
     *
     * @return a <code>ClassWrapper</code> around the given <code>Class</code>
     */
    public ClassWrapper wrap(Class classToWrap) {
        return new ClassWrapper(classToWrap);
	}
}
