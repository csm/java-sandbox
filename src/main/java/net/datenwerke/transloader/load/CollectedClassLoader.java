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


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.datenwerke.transloader.ClassWrapper;
import net.datenwerke.transloader.except.TransloaderException;
import net.datenwerke.transloader.reference.DefaultReflecter;
import net.datenwerke.transloader.reference.Reference;
import net.datenwerke.transloader.reference.ReferenceReflecter;

// TODO test-drive this spiked CollectedClassLoader
public final class CollectedClassLoader extends ClassLoader {
    private final ReferenceReflecter reflecter = new DefaultReflecter();
    private final List classLoaders = new ArrayList();
    private final Set alreadyVisited = new HashSet();

    public CollectedClassLoader(Object objectGraph) {
        super(BootClassLoader.INSTANCE);
        try {
            collectClassLoadersFrom(objectGraph);
        } catch (IllegalAccessException e) {
            throw new TransloaderException("Cannot create CollectedClassLoader for '" + objectGraph + "'.", e);
        }
        alreadyVisited.clear();
    }

    protected Class findClass(String name) throws ClassNotFoundException {
        ClassNotFoundException finalException = null;
        for (int i = 0; i < classLoaders.size(); i++) {
            ClassLoader classLoader = (ClassLoader) classLoaders.get(i);
            try {
                return classLoader.loadClass(name);
            } catch (ClassNotFoundException e) {
                finalException = e;
            }
        }
        throw finalException;
    }

    private void collectClassLoadersFrom(Object currentObjectInGraph) throws IllegalAccessException {
        if (shouldCollectFrom(currentObjectInGraph)) {
            collectFrom(currentObjectInGraph);
            Reference[] references = reflecter.reflectReferencesFrom(currentObjectInGraph);
            for (int i = 0; i < references.length; i++)
                collectClassLoadersFrom(references[i].getValue());
        }
    }

    private boolean shouldCollectFrom(Object object) {
        boolean notNull = object != null;
        
        /* arno: fixed, don't want to call null object */
        //boolean notAlreadyVisited = !alreadyVisited.contains(object);
        //return notNull && notAlreadyVisited;
        
        return notNull && !alreadyVisited.contains(object);
    }

    private void collectFrom(Object object) {
        alreadyVisited.add(object);
        ClassLoader classLoader = ClassWrapper.getClassLoaderFrom(object);
        if (!classLoaders.contains(classLoader))
            classLoaders.add(classLoader);
    }
}
