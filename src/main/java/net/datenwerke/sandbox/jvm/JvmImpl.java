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

import java.rmi.Naming;
import java.rmi.RemoteException;

import net.datenwerke.sandbox.SandboxContext;
import net.datenwerke.sandbox.SandboxedCallResult;
import net.datenwerke.sandbox.SandboxedEnvironment;
import net.datenwerke.sandbox.jvm.exceptions.JvmInstantiatonException;
import net.datenwerke.sandbox.jvm.exceptions.JvmKilledUnsafeThreadException;
import net.datenwerke.sandbox.jvm.exceptions.JvmServerDeadException;
import net.datenwerke.sandbox.jvm.exceptions.RemoteTaskExecutionFailed;
import net.datenwerke.sandbox.jvm.server.SandboxRemoteServer;

/**
 * The implementation of a remote agent handler.
 * 
 * @author Arno Mittelbach
 *
 */
public class JvmImpl implements Jvm {

	private static final int MAX_TRIES = 10;
	
	private final String namePrefix;
	private final Process process;
	private final int port;
	private final String host;

	private SandboxRemoteServer server;
	private boolean destroyed;


	public JvmImpl(String namePrefix, int port, Process process){
		this.namePrefix = namePrefix;
		this.process = process;
		this.port = port;
		this.host = "localhost";
		
		int tries = 0;
		while(null == server){
			try{
				server = (SandboxRemoteServer)Naming.lookup("//" + host + ":" + port + "/" + SandboxRemoteServer.NAME + namePrefix);
			} catch(Exception e){
				if(tries > MAX_TRIES){
					/* kill process */
					process.destroy();
					
					/* throw exception */
					throw new JvmInstantiatonException(e);
				}
				tries++;
				try{
					Thread.sleep(1000);
				}catch(InterruptedException ignore){
				}
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.datenwerke.sandbox.jvm.Jvm#getProcess()
	 */
	@Override
	public Process getProcess() {
		return process;
	}

	/*
	 * (non-Javadoc)
	 * @see net.datenwerke.sandbox.jvm.Jvm#isDestroyed()
	 */
	@Override
	public boolean isDestroyed() {
		return destroyed;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.datenwerke.sandbox.jvm.Jvm#getHost()
	 */
	@Override
	public String getHost() {
		return host;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.datenwerke.sandbox.jvm.Jvm#getPort()
	 */
	@Override
	public int getPort() {
		return port;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.datenwerke.sandbox.jvm.Jvm#destroy()
	 */
	@Override
	public synchronized void destroy() {
		if(destroyed)
			return;
		
		destroyed = true;
		process.destroy();
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.datenwerke.sandbox.jvm.Jvm#execute(net.datenwerke.sandbox.jvm.JvmTask)
	 */
	@Override
	public synchronized SandboxedCallResult execute(JvmTask task) throws JvmServerDeadException, RemoteTaskExecutionFailed, JvmKilledUnsafeThreadException {
		/* test server */
		testStatus();
		
		try{
			return server.execute(task);
		} catch(JvmKilledUnsafeThreadException e) {
			throw e;
		} catch(RemoteException e){
			Throwable cause = e.getCause();
			if(cause instanceof JvmKilledUnsafeThreadException)
				throw (JvmKilledUnsafeThreadException)cause;
			throw new RemoteTaskExecutionFailed(e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.datenwerke.sandbox.jvm.Jvm#reset()
	 */
	@Override
	public void reset() throws JvmServerDeadException, RemoteException {
		/* test server */
		testStatus();
		
		server.reset();
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.datenwerke.sandbox.jvm.Jvm#init(net.datenwerke.sandbox.SandboxContext)
	 */
	@Override
	public void init(SandboxContext context) throws JvmServerDeadException, RemoteException {
		/* test server */
		testStatus();
		
		server.init(context);
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.datenwerke.sandbox.jvm.Jvm#runInContext(java.lang.Class, java.lang.Object[])
	 */
	@Override
	public SandboxedCallResult runInContext(java.lang.Class<? extends SandboxedEnvironment> task, Object... args) throws JvmServerDeadException, RemoteTaskExecutionFailed, JvmKilledUnsafeThreadException {
		/* test server */
		testStatus();
		
		try{
			return server.runInContext(task, args);
		} catch(JvmKilledUnsafeThreadException e) {
			throw e;
		} catch(RemoteException e){
			Throwable cause = e.getCause();
			if(cause instanceof JvmKilledUnsafeThreadException)
				throw (JvmKilledUnsafeThreadException)cause;
			throw new RemoteTaskExecutionFailed(e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.datenwerke.sandbox.jvm.Jvm#runSandboxed(java.lang.Class, java.lang.Object[])
	 */
	@Override
	public SandboxedCallResult runSandboxed(
			Class<? extends SandboxedEnvironment> task, Object... args) throws JvmServerDeadException, RemoteTaskExecutionFailed, JvmKilledUnsafeThreadException {
		/* test server */
		testStatus();
		
		try{
			return server.runSandboxed(task, args);
		} catch(JvmKilledUnsafeThreadException e) {
			throw e;
		} catch(RemoteException e){
			Throwable cause = e.getCause();
			if(cause instanceof JvmKilledUnsafeThreadException)
				throw (JvmKilledUnsafeThreadException)cause;
			throw new RemoteTaskExecutionFailed(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.datenwerke.sandbox.jvm.Jvm#getRmiUrl()
	 */
	@Override
	public String getRmiUrl() {
		return "//" + host + ":" + port + "/" + SandboxRemoteServer.NAME + namePrefix;
	}
	
	protected void testStatus() throws JvmServerDeadException {
		try{
			server.isAlive();
		} catch(Exception e){
			try{
				destroy();
			} catch(Exception ignore){
			}
			throw new JvmServerDeadException(e);
		}
	}


}
