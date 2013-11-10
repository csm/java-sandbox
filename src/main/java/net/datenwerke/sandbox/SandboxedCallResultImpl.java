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

import java.util.concurrent.Callable;




/**
 * An implementation of the SandboxedCallResult that allows to access the results code executed in the sandbox.
 * 
 * @see SandboxServiceImpl#runSandboxed(Callable, SandboxContext, ClassLoader, Object...)
 * @see SandboxServiceImpl#getScriptEngine(String, SandboxContext)
 * @author Arno Mittelbach
 *
 * @param <V>
 */
public class SandboxedCallResultImpl<V> implements SandboxedCallResult<V> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2221868658839523140L;
	
	/**
	 * 
	 */
	public final Object raw;
	
	public SandboxedCallResultImpl(Object raw) {
		this.raw = raw;
	}
	
	@Override
	public Object getRaw() {
		return raw;
	}
	
	@Override
	public V get(){
		return (V) get(getClass().getClassLoader());
	}
	
	@Override
	public Object get(Object obj){
		return get(obj.getClass().getClassLoader());
	}
	
	@Override
	public Object get(ClassLoader loader){
		return SandboxServiceImpl.getInstance().bridge(raw, null != loader ? loader : ClassLoader.getSystemClassLoader());
	}

	
}