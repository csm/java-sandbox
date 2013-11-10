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

import net.datenwerke.sandbox.SandboxContext;
import net.datenwerke.sandbox.SandboxService;
import net.datenwerke.sandbox.SandboxServiceImpl;
import net.datenwerke.sandbox.SandboxedCallResult;
import net.datenwerke.sandbox.SandboxedEnvironment;

/**
 * An implementation of {@link JvmTask} to execute sandboxed code on a remote agent.
 * @author Arno Mittelbach
 *
 */
public class JvmSandboxTask implements JvmTask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8995776366037937614L;
	
	private final Class<? extends SandboxedEnvironment> call;
	private final SandboxContext context;
	private final boolean runInContext;
	private final Object[] args;


	public JvmSandboxTask(Class<? extends SandboxedEnvironment> call, SandboxContext context, boolean runInContext, Object... args){
		this.call = call;
		this.context = context;
		this.runInContext = runInContext;
		this.args = args;
	}
	
	@Override
	public SandboxedCallResult call() throws Exception {
		SandboxService instance = SandboxServiceImpl.getInstance();
		
		SandboxedCallResult result = null;
		if(runInContext)
			result = instance.runInContext(call, context, args);
		else
			result = instance.runSandboxed(call, context, args);
		
		return result;
	}

}
