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

package net.datenwerke.sandbox.jvm;


/**
 * Simple implementation of the {@link JvmPoolConfig} Bean.
 * 
 * @author Arno Mittelbach
 *
 */
public class JvmPoolConfigImpl implements JvmPoolConfig {

	private final int poolsize;
	private final JvmInstantiator jvmInstantiator;
	private int freelancers;

	public JvmPoolConfigImpl(
		int poolsize,
		int freelancers
		){
		this(poolsize, freelancers, new JvmInstantiatorImpl());
	}
	
	public JvmPoolConfigImpl(
			int poolsize,
			int freelancers,
			String remoteVmArgs
			){
		this(poolsize, freelancers, new JvmInstantiatorImpl(remoteVmArgs));
	}
	
	public JvmPoolConfigImpl(
		int poolsize,
		int freelancers,
		JvmInstantiator instantiatior
		){
		this.poolsize = poolsize;
		this.freelancers = freelancers;
		this.jvmInstantiator = instantiatior;
	}
	
	@Override
	public JvmInstantiator getInstantiator() {
		return jvmInstantiator;
	}

	@Override
	public int getPoolSize() {
		return poolsize;
	}
	
	@Override
	public int getFreelancerSize() {
		return freelancers;
	}

}
