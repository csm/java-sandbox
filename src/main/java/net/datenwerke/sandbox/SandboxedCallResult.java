/*
*  java-sandbox
*  Copyright (c) 2012 datenwerke Jan Albrecht
*  http://www.datenwerke.net
*  
*  This file is part of the java-sandbox: https://sourceforge.net/p/dw-sandbox/
*
*
*  This program is free software: you can redistribute it and/or modify
*  it under the terms of the GNU General Public License as published by
*  the Free Software Foundation, either version 3 of the License, or
*  (at your option) any later version.

*  This program is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU General Public License for more details.
*
*  You should have received a copy of the GNU General Public License
*  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package net.datenwerke.sandbox;

import java.io.Serializable;

/**
 * Object contaning the result of a sandboxed execution
 * 
 * @author Arno Mittelbach
 *
 * @param <V>
 */
public interface SandboxedCallResult<V> extends Serializable {
	
	/**
	 * Returns the raw object 
	 * 
	 * @return
	 */
	Object getRaw();
	
	/**
	 * Returns the object in the scope of the classloader that has loaded this instance
	 * of {@link SandboxedCallResult}
	 * 
	 * @return
	 */
	V get();
	
	/**
	 * Returns the object in the scope of the given classloader.
	 * 
	 * @param  loader
	 * @return
	 */
	Object get(ClassLoader loader);
	
	/**
	 * Returns the wrapped object in the scope of the classloader that has loaded object obj.
	 * 
	 * @param obj
	 * @return
	 */
	Object get(Object obj);

	
}