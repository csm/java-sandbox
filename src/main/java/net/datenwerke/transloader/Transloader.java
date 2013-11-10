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

/**
 * The API by which to wrap objects that reference <code>Class</code>es from foreign <code>ClassLoader</code>s.
 *
 * @author Jeremy Wales
 */
public interface Transloader {
    /**
     * The default implementation of <code>Transloader</code> which will produce {@link ObjectWrapper}s configured
     * with {@link net.datenwerke.transloader.configure.CloningStrategy#MAXIMAL} for <code>Object</code>s and {@link ClassWrapper}s for
     * <code>Class</code>es.
     */
    Transloader DEFAULT = new DefaultTransloader(net.datenwerke.transloader.configure.CloningStrategy.MAXIMAL);

    /**
     * Wraps the given object in an <code>ObjectWrapper</code>.
     *
     * @param objectToWrap the object to wrap
     * @return the wrapper around the given object
     */
    ObjectWrapper wrap(Object objectToWrap);

    /**
     * Wraps the given <code>Class</code> in a <code>ClassWrapper</code>.
     *
     * @param classToWrap the <code>Class</code> to wrap
     * @return the wrapper around the given <code>Class</code>
     */
    ClassWrapper wrap(Class classToWrap);
}
