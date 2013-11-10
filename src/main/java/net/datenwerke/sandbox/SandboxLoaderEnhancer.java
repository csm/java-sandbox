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


/**
 * Allows to tap into the classloading process.
 * 
 * @author Arno Mittelbach
 *
 */
public interface SandboxLoaderEnhancer {

	/**
	 * Informst about that a class is to be loaded.
	 * 
	 * @param sandboxLoader
	 * @param name
	 * @param resolve
	 */
	void classtoBeLoaded(SandboxLoader sandboxLoader, String name, boolean resolve);

	/**
	 * Is called, if a class could not be loaded. If null is returned
	 * a {@link ClassNotFoundException} will be thrown.
	 * 
	 * @param sandboxLoader
	 * @param name
	 * @return
	 */
	byte[] loadClass(SandboxLoader sandboxLoader, String name);

	/**
	 * Allows the enhancer to perform bytecode manipulation.
	 * 
	 * @param sandboxLoader
	 * @param name
	 * @param cBytes
	 * @return
	 */
	byte[] enhance(SandboxLoader sandboxLoader, String name, byte[] cBytes);

	/**
	 * Informs about that a class was loaded.
	 * 
	 * @param sandboxLoader
	 * @param name
	 * @param clazz
	 */
	void classLoaded(SandboxLoader sandboxLoader, String name, Class clazz);

	/**
	 * should return false unless the class in question should be loaded with the application class
	 * loader.
	 * 
	 * @param name
	 * @return
	 */
	boolean isLoadClassWithApplicationLoader(String name);

}
