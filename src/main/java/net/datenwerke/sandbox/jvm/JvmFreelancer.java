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

import java.rmi.RemoteException;

import net.datenwerke.sandbox.SandboxContext;
import net.datenwerke.sandbox.SandboxService;
import net.datenwerke.sandbox.SandboxedCallResult;
import net.datenwerke.sandbox.SandboxedCallResultImpl;
import net.datenwerke.sandbox.SandboxedEnvironment;
import net.datenwerke.sandbox.jvm.exceptions.JvmKilledUnsafeThreadException;
import net.datenwerke.sandbox.jvm.exceptions.JvmServerDeadException;
import net.datenwerke.sandbox.jvm.exceptions.RemoteTaskExecutionFailed;

/**
 * 
 * @author Arno Mittelbach
 *
 */
public class JvmFreelancer {

	private JvmPoolConfig jvmConfig;
	private Jvm jvm;
	private boolean shutdown;
	private boolean restartOnRelease;

	public JvmFreelancer(JvmPoolConfig jvmConfig) {
		this.jvmConfig = jvmConfig;

		jvm = jvmConfig.getInstantiator().spawnJvm();
	}

	/**
	 * shuts down 
	 */
	protected void shutdown() {
		shutdown = true;
		
		jvm.destroy();
	}
	
	public boolean isShutdown() {
		return shutdown;
	}
	
	public void restartJvm() {
		/* kill jvm */
		jvm.destroy();
		
		/* create new jvm */
		jvm = jvmConfig.getInstantiator().spawnJvm();
	}
	
	/**
	 * Initializes a session for the given context.
	 * 
	 * @param context
	 * @throws RemoteException
	 * @throws JvmServerDeadException
	 */
	public void init(SandboxContext context) throws RemoteException, JvmServerDeadException{
		try{
			jvm.init(context);
		} catch(JvmServerDeadException e){
			restartJvm();
			throw e;
		}
	}

	/**
	 * Resets the {@link JvmFreelancer}.
	 * 
	 * @throws RemoteException
	 * @throws JvmServerDeadException
	 */
	public void reset() throws RemoteException, JvmServerDeadException{
		try{
			jvm.reset();
		} catch(JvmServerDeadException e){
			restartJvm();
			throw e;
		}
	}

	/**
	 * As the method provided by the {@link SandboxService} but on the remote machine.
	 * 
	 * @see SandboxService#runInContext(Class, SandboxContext, Object...)
	 * @param clazz
	 * @param args
	 * @return
	 * @throws RemoteTaskExecutionFailed
	 * @throws JvmServerDeadException
	 * @throws JvmKilledUnsafeThreadException
	 */
	public SandboxedCallResult runInContext(Class<? extends SandboxedEnvironment> clazz, Object... args) throws RemoteTaskExecutionFailed, JvmServerDeadException, JvmKilledUnsafeThreadException {
		try{
			SandboxedCallResult result =  jvm.runInContext(clazz, args);
			return fix(result); 
		} catch(JvmServerDeadException e){
			restartJvm();
			throw e;
		} catch (JvmKilledUnsafeThreadException e) {
			restartOnRelease = true;
			throw e;
		} catch(RemoteException e){
			Throwable cause = e.getCause();
			if(cause instanceof JvmKilledUnsafeThreadException){
				restartOnRelease = true;
				throw (JvmKilledUnsafeThreadException)cause;
			}
			throw new RemoteTaskExecutionFailed(e);
		}
	}
	
	/**
	 * As the method provided by the {@link SandboxService} but on the remote machine.
	 * 
	 * @see SandboxService#runSandboxed(Class, SandboxContext, Object...)
	 * @param clazz
	 * @param args
	 * @return
	 * @throws RemoteTaskExecutionFailed
	 * @throws JvmServerDeadException
	 * @throws JvmKilledUnsafeThreadException
	 */
	public SandboxedCallResult runSandboxed(Class<? extends SandboxedEnvironment> clazz, Object... args) throws RemoteTaskExecutionFailed, JvmServerDeadException, JvmKilledUnsafeThreadException {
		try{
			SandboxedCallResult result = jvm.runSandboxed(clazz, args);
			return fix(result);
		} catch(JvmServerDeadException e){
			restartJvm();
			throw e;
		} catch (JvmKilledUnsafeThreadException e) {
			restartOnRelease = true;
			throw e;
		}  catch(RemoteException e){
			Throwable cause = e.getCause();
			if(cause instanceof JvmKilledUnsafeThreadException){
				restartOnRelease = true;
				throw (JvmKilledUnsafeThreadException)cause;
			}
			throw new RemoteTaskExecutionFailed(e);
		}
	}

	protected SandboxedCallResult fix(SandboxedCallResult result) {
		if(null == result)
			result = new SandboxedCallResultImpl(null);

		return result;
	}

	void released() {
		try{
			if(restartOnRelease)
				restartJvm();
			else {
				try {
					reset();
				} catch (Exception e) {
					restartJvm();
				}
			}
		} finally {
			restartOnRelease = false;
		}
	}

}
