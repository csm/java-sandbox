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

package net.datenwerke.transloader.configure;

import net.datenwerke.transloader.clone.reflect.decide.MaximalCloningDecisionStrategy;
import net.datenwerke.transloader.clone.reflect.decide.MinimalCloningDecisionStrategy;
import net.datenwerke.transloader.clone.reflect.instantiate.DefaultInstantiater;
import net.datenwerke.transloader.clone.reflect.instantiate.ObjenesisInstantiationStrategy;
import net.datenwerke.transloader.clone.reflect.internal.DefaultCloner;

/**
 * @author jeremywales
 */
public class InternalCloner {
   
	public static final net.datenwerke.transloader.clone.reflect.internal.InternalCloner MINIMAL =
            new DefaultCloner(
                    new MinimalCloningDecisionStrategy(),
                    CloneInstantiater.DEFAULT,
                    ReferenceReflecter.DEFAULT
            );

    public static final net.datenwerke.transloader.clone.reflect.internal.InternalCloner MAXIMAL =
            new DefaultCloner(
                    new MaximalCloningDecisionStrategy(),
                    CloneInstantiater.DEFAULT,
                    ReferenceReflecter.DEFAULT
            );

    private InternalCloner() {}

    /**
     * 
     * @author arno
     * 
     * @return
     */
	public static net.datenwerke.transloader.clone.reflect.internal.InternalCloner newMinimalInstance() {
		 return new DefaultCloner(
                  new MinimalCloningDecisionStrategy(),
                  new DefaultInstantiater(new ObjenesisInstantiationStrategy()),
                  ReferenceReflecter.DEFAULT
          );
	}
}
