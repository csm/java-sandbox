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

import java.lang.management.LockInfo;
import java.lang.management.MonitorInfo;
import java.util.Arrays;

/**
 * 
 * @author Arno Mittelbach
 *
 */
public class BadKillInfo{
	private final MonitorInfo[] lockedMonitors;
	private final LockInfo[] lockedSynchronizers;
	private final StackTraceElement[] stackTrace;
	
	public BadKillInfo(MonitorInfo[] lockedMonitors,
			LockInfo[] lockedSynchronizers, StackTraceElement[] stackTrace) {
		this.lockedMonitors = lockedMonitors;
		this.lockedSynchronizers = lockedSynchronizers;
		this.stackTrace = stackTrace;
	}
	public MonitorInfo[] getLockedMonitors() {
		return lockedMonitors;
	}
	public LockInfo[] getLockedSynchronizers() {
		return lockedSynchronizers;
	}
	public StackTraceElement[] getStackTrace() {
		return stackTrace;
	}
	
	@Override
	public String toString() {
		return "BadKillInfo [lockedMonitors=" + Arrays.toString(lockedMonitors)
				+ ", lockedSynchronizers="
				+ Arrays.toString(lockedSynchronizers) + ", stackTrace="
				+ Arrays.toString(stackTrace) + "]";
	}
	
	
}