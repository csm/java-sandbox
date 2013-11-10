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


/**
 * The <code>RuntimeException</code> thrown by the Transloader library itself.
 *
 * @author Jeremy Wales
 */
public class TransloaderException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = -6532744981702790470L;

	/**
     * Constructs a new <code>TransloaderException</code> with the given detail message and nested
     * <code>Throwable</code>.
     *
     * @param message the error message
     * @param cause   the <code>Exception</code> that caused this one to be thrown
     */
    public TransloaderException(String message, Exception cause) {
        super((String) Assert.isNotNull(message), (Exception) Assert.isNotNull(cause));
	}
}
