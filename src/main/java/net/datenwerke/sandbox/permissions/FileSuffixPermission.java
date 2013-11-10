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

/**
 * A file permission checking the file suffix.
 * 
 * @author Arno Mittelbach
 *
 */
public class FileSuffixPermission implements FilePermission {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5151278384775390340L;
	
	private final String suffix;
	private boolean negate = false;
	
	public FileSuffixPermission(String suffix) {
		this(suffix, false);
	}
	
	public FileSuffixPermission(String suffix, boolean negate) {
		this.suffix = suffix;
		this.negate = negate;
	}
	
	@Override
	public boolean testPermission(String file) {
		return file.endsWith(suffix) ^ negate;
	}
	
	@Override
	public FileSuffixPermission clone() {
		return new FileSuffixPermission(suffix, negate);
	}

	@Override
	public String toString() {
		return "FileSuffixPermission [suffix=" + suffix + ", negate=" + negate
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (negate ? 1231 : 1237);
		result = prime * result + ((suffix == null) ? 0 : suffix.hashCode());
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
		FileSuffixPermission other = (FileSuffixPermission) obj;
		if (negate != other.negate)
			return false;
		if (suffix == null) {
			if (other.suffix != null)
				return false;
		} else if (!suffix.equals(other.suffix))
			return false;
		return true;
	}

	
}
