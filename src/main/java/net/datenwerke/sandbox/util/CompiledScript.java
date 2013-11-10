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

package net.datenwerke.sandbox.util;

import java.io.Serializable;
import java.util.Collection;

/**
 * 
 * @author Arno Mittelbach
 *
 */
public class CompiledScript implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5686691819592719431L;
	
	private byte[] classBytes;
	private String name;
	private Collection<CompiledScript> subScripts;
	
	public CompiledScript(byte[] classBytes, String name) {
		super();
		this.classBytes = classBytes;
		this.name = name;
	}
	public byte[] getClassBytes() {
		return classBytes;
	}
	public String getName() {
		return name;
	}
	public void setSubScripts(Collection<CompiledScript> subScripts) {
		this.subScripts = subScripts;
	}
	public Collection<CompiledScript> getSubScripts() {
		return subScripts;
	}
}