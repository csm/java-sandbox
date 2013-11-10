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
import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Configures a {@link Permission}
 * 
 * @author Arno Mittelbach
 *
 */
public class SecurityPermission implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3937793105683659672L;
	
	private String type;
	private String name;
	
	private Collection<StackEntry> entries;
	private String actions;

	public SecurityPermission(String type) {
		this(type, null, null, emptyEntriesCollection());
	}
	
	public SecurityPermission(String type, String name) {
		this(type, name, null, emptyEntriesCollection());
	}

	public SecurityPermission(String type,String name, String actions) {
		this(type, name, actions, emptyEntriesCollection());
	}
	
	public SecurityPermission(String type,String name,
			StackEntry... entries) {
		this(type, name, null, null == entries || entries.length == 0 ? emptyEntriesCollection() : Arrays.asList(entries));
	}
	
	public SecurityPermission(String type,String name, String actions,
			StackEntry... entries) {
		this(type, name, actions, null == entries || entries.length == 0 ? emptyEntriesCollection() : Arrays.asList(entries));
	}
	
	public SecurityPermission(String type,String name, String actions,
			Collection<StackEntry> entries) {
		this.name = name;
		this.type = type;
		this.actions = actions;
		this.entries = entries;
	}

	private static Collection<StackEntry> emptyEntriesCollection() {
		return Collections.emptySet();
	}
	
	public String getName() {
		return name;
	}
	
	public String getType() {
		return type;
	}
	
	public Collection<StackEntry> getEntries() {
		return entries;
	}

	public boolean isEmpty() {
		return entries.isEmpty();
	}
	
	public String getActions() {
		return actions;
	}
	
	@Override
	public SecurityPermission clone() {
		Collection<StackEntry> clonedEntries = new ArrayList<StackEntry>();
		for(StackEntry e : entries)
			clonedEntries.add(e.clone());
		return new SecurityPermission(type, name, actions, clonedEntries);
	}

	@Override
	public String toString() {
		return "SecurityPermission [type=" + type + ", name=" + name
				+ ", entries=" + entries
				+ ", actions=" + actions + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((actions == null) ? 0 : actions.hashCode());
		result = prime * result + ((entries == null) ? 0 : entries.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		SecurityPermission other = (SecurityPermission) obj;
		if (actions == null) {
			if (other.actions != null)
				return false;
		} else if (!actions.equals(other.actions))
			return false;
		if (entries == null) {
			if (other.entries != null)
				return false;
		} else if (!entries.equals(other.entries))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
	
	

	
}
