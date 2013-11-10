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

import net.datenwerke.transloader.reference.DefaultReflecter;
import net.datenwerke.transloader.reference.ReferenceReflecter;
import net.datenwerke.transloader.reference.field.FieldSetter;
import net.datenwerke.transloader.reference.field.SerializationSetter;
import net.datenwerke.transloader.reference.field.SimpleSetter;

/**
 * @author jeremywales
 */
public final class ReflecterFactory {
    public static final ReferenceReflecter DEFAULT;

    // TODO revisit FieldSetter decision
    static {
        FieldSetter setter;
        try {
            setter = new SerializationSetter();
        } catch (Exception e) {
            setter = new SimpleSetter();
        }
        DEFAULT = new DefaultReflecter(setter);
    }

    private ReflecterFactory() {
    }
}
