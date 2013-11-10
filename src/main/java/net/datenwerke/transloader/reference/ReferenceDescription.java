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

package net.datenwerke.transloader.reference;

public interface ReferenceDescription {
    Object getValueFrom(Object referer) throws NoSuchFieldException;

    void setValueIn(Object referer, Object value) throws NoSuchFieldException;

    /**
     * Indicates whether or not the reference is to a primitive rather than an {@link Object}.
     *
     * @return <code>true</code> if the reference is of primitive type
     */
    boolean isOfPrimitiveType();

    boolean isTransient();

    String getName();
}
