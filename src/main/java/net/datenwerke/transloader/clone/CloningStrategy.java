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


/**
 * The API by which an object can be cloned using a different <code>ClassLoader</code> than that/those which loaded
 * the <code>Class</code>es it references. It can be used directly or serves as the strategy interface for
 * customising the behaviour of {@link net.datenwerke.transloader.ObjectWrapper}s.
 *
 * @author Jeremy Wales
 */
public interface CloningStrategy {

    /**
     * Clones the given object using the given <code>ClassLoader</code>.
     *
     * @param targetLoader the <code>ClassLoader</code> by which to load <code>Class</code>es for clones
     * @param original          the original object to be cloned (can be <code>null</code>).
     * @return the result of cloning the object graph
     * @throws Exception can throw any <code>Exception</code> depending on the implementation
     */
	Object cloneObjectUsing(ClassLoader targetLoader, Object original) throws Exception;
}
