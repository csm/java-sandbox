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

package net.datenwerke.transloader.primitive;

import net.datenwerke.transloader.except.Assert;

import org.apache.commons.lang.ClassUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectStreamField;

/**
 * @author jeremywales
 */
public final class WrapperConverter {
    private static final String IRRELEVANT = "irrelevant";

    public byte[] asBytes(Object wrapper) throws IOException {
        Assert.isNotNull(wrapper);
        Assert.contains(wrapper.getClass(), Wrapper.LIST);
        return convert(wrapper);
    }

    private static byte[] convert(Object wrapper) throws IOException {
        ByteArrayOutputStream arrayStream = new ByteArrayOutputStream();
        DataOutputStream dataStream = new DataOutputStream(arrayStream);
        write(wrapper, dataStream);
        return arrayStream.toByteArray();
    }

    private static void write(Object wrapper, DataOutputStream dataStream) throws IOException {
        char typeCode = getTypeCode(wrapper);
        switch (typeCode) {
            case 'B':
                dataStream.writeByte(intValue(wrapper));
                break;
            case 'C':
                dataStream.writeChar(charValue(wrapper));
                break;
            case 'S':
                dataStream.writeShort(intValue(wrapper));
                break;
            case 'I':
                dataStream.writeInt(intValue(wrapper));
                break;
            case 'J':
                dataStream.writeLong(number(wrapper).longValue());
                break;
            case 'F':
                dataStream.writeFloat(number(wrapper).floatValue());
                break;
            case 'D':
                dataStream.writeDouble(number(wrapper).doubleValue());
                break;
            case 'Z':
                dataStream.writeBoolean(booleanValue(wrapper));
        }
    }

    private static char charValue(Object wrapper) {
        return ((Character) wrapper).charValue();
    }

    private static char getTypeCode(Object wrapper) {
        Class primitiveType = ClassUtils.wrapperToPrimitive(wrapper.getClass());
        ObjectStreamField typeCodeProvider = new ObjectStreamField(IRRELEVANT, primitiveType);
        return typeCodeProvider.getTypeCode();
    }

    private static Number number(Object wrapper) {
        return ((Number) wrapper);
    }

    private static int intValue(Object wrapper) {
        return number(wrapper).intValue();
    }

    private static boolean booleanValue(Object wrapper) {
        return Boolean.valueOf(wrapper.toString()).booleanValue();
    }
}
