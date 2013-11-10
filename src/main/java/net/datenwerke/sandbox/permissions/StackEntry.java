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

package net.datenwerke.sandbox.permissions;

import java.io.Serializable;

/**
 * Describes execution stack checks. 
 * 
 * @author Arno Mittelbach
 *
 */
public class StackEntry implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4889093836149908662L;
	
	private int pos;
	private String type;
	private boolean prefix;
	
	public StackEntry(int pos, String type) {
		this(pos, type, false);
	}
	
	public StackEntry(int pos, String type, boolean prefix) {
		if(pos < -1)
			throw new IllegalArgumentException("stack position must be greater or equal to 0 or -1");
		this.pos = pos;
		this.type = type;
		this.prefix = prefix;
	}

	public int getPos() {
		return pos;
	}
	
	public String getType() {
		return type;
	}
	
	public boolean isPrefix() {
		return prefix;
	}
	
	@Override
	public StackEntry clone() {
		return new StackEntry(pos, type, prefix);
	}

	@Override
	public String toString() {
		return "StackEntry [pos=" + pos + ", type=" + type + ", prefix="
				+ prefix + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + pos;
		result = prime * result + (prefix ? 1231 : 1237);
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StackEntry other = (StackEntry) obj;
		if (pos != other.pos)
			return false;
		if (prefix != other.prefix)
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
	
	
	
}