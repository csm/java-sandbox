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

package net.datenwerke.transloader.except;

import java.util.Arrays;
import java.util.List;

/**
 * Static utility for making assertions.
 *
 * @author Jeremy Wales
 */
public final class Assert {

    private Assert() {
    }

    /**
     * Asserts that the given parameter is not <code>null</code>.
     *
     * @param parameter the parameter to check
     * @return the given <code>parameter</code> (if an <code>Exception</code> was not already thrown because it was
     *         <code>null</code>)
     * @throws IllegalArgumentException if <code>parameter</code> is <code>null</code>
     */
    public static Object isNotNull(Object parameter) {
        areNotNull(new Object[]{parameter});
        return parameter;
    }

    /**
     * Asserts that the given parameters are not <code>null</code>.
     *
     * @param parameter1 the first parameter to check
     * @param parameter2 the second parameter to check
     * @throws IllegalArgumentException if either <code>parameter1</code> or <code>parameter2</code> is
     *                                  <code>null</code>
     */
    public static void areNotNull(Object parameter1, Object parameter2) {
        areNotNull(new Object[]{parameter1, parameter2});
    }

    /**
     * Asserts that the given parameters are not <code>null</code>.
     *
     * @param parameter1 the first parameter to check
     * @param parameter2 the second parameter to check
     * @param parameter3 the third parameter to check
     * @throws IllegalArgumentException if either <code>parameter1</code>, <code>parameter2</code> or
     *                                  <code>parameter3</code> is <code>null</code>
     */
    public static void areNotNull(Object parameter1, Object parameter2, Object parameter3) {
        areNotNull(new Object[]{parameter1, parameter2, parameter3});
    }

    /**
     * Asserts that the given parameters are not <code>null</code>.
     *
     * @param parameter1 the first parameter to check
     * @param parameter2 the second parameter to check
     * @param parameter3 the third parameter to check
     * @param parameter4 the fourth parameter to check
     * @throws IllegalArgumentException if either <code>parameter1</code>, <code>parameter2</code>, <code>parameter3</code> or
     *                                  <code>parameter4</code> is <code>null</code>
     */
    public static void areNotNull(Object parameter1, Object parameter2, Object parameter3, Object parameter4) {
        areNotNull(new Object[]{parameter1, parameter2, parameter3, parameter4});
    }

    /**
     * Asserts that the given parameters are not <code>null</code>.
     *
     * @param parameter1 the first parameter to check
     * @param parameter2 the second parameter to check
     * @param parameter3 the third parameter to check
     * @param parameter4 the fourth parameter to check
     * @param parameter5 the fifth parameter to check
     * @throws IllegalArgumentException if either <code>parameter1</code>, <code>parameter2</code>, <code>parameter3</code>,
     *                                  <code>parameter4</code> or <code>parameter5</code> is <code>null</code>
     */
    public static void areNotNull(Object parameter1, Object parameter2, Object parameter3, Object parameter4, Object parameter5) {
        areNotNull(new Object[]{parameter1, parameter2, parameter3, parameter4, parameter5});
    }

    /**
     * Asserts that the given parameters are not <code>null</code>.
     *
     * @param parameters the parameters to check
     * @throws IllegalArgumentException if any elements of <code>parameters</code> are <code>null</code>
     */
    public static void areNotNull(Object[] parameters) {
        if (parameters == null) throw newNullParameterException(parameters);
        List parameterList = Arrays.asList(parameters);
        if (parameterList.contains(null)) throw newNullParameterException(parameterList);
    }

    private static IllegalArgumentException newNullParameterException(Object parameters) {
        return new IllegalArgumentException("Expecting no null parameters but received " + parameters + ".");
    }

    /**
     * Asserts that the given object is an array.
     *
     * @param parameter the parameter to check
     * @return the given <code>parameter</code> (if an <code>Exception</code> was not already thrown because it was
     *         not an array)
     * @throws IllegalArgumentException if <code>parameter</code> is not an array
     */
    public static Object isArray(Object parameter) {
        if (!isNotNull(parameter).getClass().isArray())
            throw new IllegalArgumentException("Expecting an array but received '" + parameter + "'.");
        return parameter;
    }

    /**
     * Asserts that the given arrays are of the same length.
     *
     * @param expected the number to compare to
     * @param actual the actual number to compare
     * @param name the name of the number being compared
     * @throws IllegalArgumentException if actual is not equal to expected
     */
    public static void isEqualTo(long expected, long actual, String name) {
        areNotNull(wrap(expected), wrap(actual), name);
        if (!(expected == actual))
            throw new IllegalArgumentException("Expecting " + name + " equal to " + expected + " but received " + actual + ".");
    }

    public static void contains(Object element, Object[] array) {
        areNotNull(element, array);
        List list = Arrays.asList(array);
        if (!(list.contains(element)))
            throw new IllegalArgumentException("Expecting one of " + list + " but received " + element + ".");
    }

    private static Long wrap(long primitive) {
        return new Long(primitive);
    }
}
