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

package net.datenwerke.transloader.load;

/**
 * A <code>ClassLoader</code> which only loads <code>Class</code>es from the virtual machine's class loader.
 */
public final class BootClassLoader extends ClassLoader {
    /**
     * An instance of <code>BootClassLoader</code> which can be shared.
     */
    public static final BootClassLoader INSTANCE = new BootClassLoader();

    /**
     * Constructs a new <code>BootClassLoader</code> which has <code>null</code> as its parent <code>ClassLoader</code>.
     *
     * @see ClassLoader#ClassLoader(ClassLoader)
     * @see ClassLoader#loadClass(String)
     */
    private BootClassLoader() {
        super(null);
    }
}
