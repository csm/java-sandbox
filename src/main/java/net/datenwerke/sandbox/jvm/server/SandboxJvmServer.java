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

package net.datenwerke.sandbox.jvm.server;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.datenwerke.sandbox.SandboxContext;
import net.datenwerke.sandbox.SandboxLoader;
import net.datenwerke.sandbox.SandboxServiceImpl;
import net.datenwerke.sandbox.SandboxedCallResult;
import net.datenwerke.sandbox.SandboxedEnvironment;
import net.datenwerke.sandbox.jvm.JvmTask;
import net.datenwerke.sandbox.jvm.exceptions.JvmInitializedTwiceException;
import net.datenwerke.sandbox.jvm.exceptions.JvmKilledUnsafeThreadException;
import net.datenwerke.sandbox.jvm.exceptions.JvmKilledUnsafeThreadRuntimeException;
import net.datenwerke.sandbox.jvm.exceptions.JvmNotInitializedException;

public class SandboxJvmServer extends UnicastRemoteObject implements SandboxRemoteServer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3625471545043099538L;
	
	private final Logger logger = Logger.getLogger(getClass().getName());
	
	private final  String name;

	private SandboxContext context;

	private SandboxLoader classloader;

	public SandboxJvmServer(String namePrefix)  throws RemoteException {
		this.name = NAME + namePrefix;
		
		logger.log(Level.INFO, "started sandbox server: " + getName());
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public boolean isAlive() {
		return true;
	}
	
	@Override
	public SandboxedCallResult execute(JvmTask task) throws RemoteException {
		try {
			return task.call();
		} catch(JvmKilledUnsafeThreadRuntimeException e){
			throw new JvmKilledUnsafeThreadException();
		} catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
	}
	
	@Override
	public void destroy() {
		System.exit(0);
	}
	
	@Override
	public void init(SandboxContext context) throws RemoteException {
		if(null != this.context)
			throw new JvmInitializedTwiceException();
		
		this.context = context;
		this.classloader = SandboxServiceImpl.getInstance().initClassLoader(context);
	}
	
	@Override
	public void reset() throws RemoteException {
		this.context = null;
		this.classloader = null;
	}
	
	@Override
	public SandboxedCallResult runInContext(
			Class<? extends SandboxedEnvironment> call, Object... args) throws RemoteException {
		if(null == context)
			throw new JvmNotInitializedException();
		
		try{
			return SandboxServiceImpl.getInstance().runInContext(call, context, classloader, args);
		} catch(JvmKilledUnsafeThreadRuntimeException e){
			throw new JvmKilledUnsafeThreadException();
		}
	}
	
	@Override
	public SandboxedCallResult runSandboxed(
			Class<? extends SandboxedEnvironment> call, Object... args) throws RemoteException {
		if(null == context)
			throw new JvmNotInitializedException();
		
		try{
			return SandboxServiceImpl.getInstance().runSandboxed(call, context, classloader, args);
		} catch(JvmKilledUnsafeThreadRuntimeException e){
			throw new JvmKilledUnsafeThreadException();
		}
	}
	
	@Override
	public void registerContext(String name, SandboxContext context)
			throws RemoteException {
		 SandboxServiceImpl.getInstance().registerContext(name, context);
	}

	public static void main(String[] args) {
		if(args.length < 2)
			System.exit(-1);
		
		String namePrefix = args[0];
		String host = "localhost";
		int port = Integer.parseInt(args[1]);
		try {
			SandboxJvmServer server = new SandboxJvmServer(namePrefix);
			
			new SandboxRemoteServiceImpl();
			
			LocateRegistry.createRegistry(port);
			Naming.rebind("//" + host + ":" + port + "/" + server.getName(), server);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	

	
}
