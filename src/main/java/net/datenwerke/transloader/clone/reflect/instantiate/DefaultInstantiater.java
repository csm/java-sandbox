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


import java.lang.reflect.Array;

import net.datenwerke.transloader.ClassWrapper;
import net.datenwerke.transloader.except.Assert;

/**
 * @author jeremywales
 */
public class DefaultInstantiater implements CloneInstantiater {
    private final InstantiationStrategy strategy;

    public DefaultInstantiater(InstantiationStrategy strategy) {
        Assert.isNotNull(strategy);
        this.strategy = strategy;
    }

    public Object instantiateShallowCloneOf(Object original, ClassLoader targetLoader) throws Exception {
        Assert.areNotNull(original, targetLoader);
        return original.getClass().isArray() ?
                instantiateArray(original, targetLoader) :
                instantiateObject(original, targetLoader);
    }

    private Object instantiateArray(Object original, ClassLoader targetLoader) {
        Class componentType = original.getClass().getComponentType();
        Class cloneComponentType = ClassWrapper.getClassFrom(targetLoader, componentType.getName());
        int length = Array.getLength(original);
        return Array.newInstance(cloneComponentType, length);
    }

    private Object instantiateObject(Object original, ClassLoader targetLoader) throws Exception {
        String className = original.getClass().getName();
        Class cloneClass = ClassWrapper.getClassFrom(targetLoader, className);
        return strategy.newInstanceOf(cloneClass);
    }
}
