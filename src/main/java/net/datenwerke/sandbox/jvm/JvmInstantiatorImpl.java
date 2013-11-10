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

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.datenwerke.sandbox.jvm.exceptions.JvmInstantiatonException;
import net.datenwerke.sandbox.jvm.server.SandboxJvmServer;

/**
 * Default implementation of {@link JvmInstantiator} to instantiate
 * java virtual machines to be used as remote agents for sandboxing.
 * 
 * @author Arno Mittelbach
 *
 */
public class JvmInstantiatorImpl implements JvmInstantiator {

	private final transient Logger logger = Logger.getLogger(getClass().getName());
	
	private final String jvmArgs;
	private int number = 1;
	private int currentPortNumber;
	
	private int minPortNumber;
	private int maxPortNumber;

	
	public JvmInstantiatorImpl(){
		this(10000, 10200, null);
	}
	
	public JvmInstantiatorImpl(String jvmArgs){
		this(10000, 10200, jvmArgs);
	}
	
	public JvmInstantiatorImpl(int minPortNumber, int maxPortNumber, String jvmArgs) {
		this.currentPortNumber = minPortNumber;
		this.minPortNumber = minPortNumber;
		this.maxPortNumber = maxPortNumber;
		this.jvmArgs = jvmArgs;
	}

	/*
	 * (non-Javadoc)
	 * @see net.datenwerke.sandbox.jvm.JvmInstantiator#spawnJvm()
	 */
	@Override
	public Jvm spawnJvm() {
		String separator = System.getProperty("file.separator");
		String classpath = System.getProperty("java.class.path");
		String path = System.getProperty("java.home")
	                + separator + "bin" + separator + "java";
		
		String namePrefix = initNamePrefix();
		int portNumber = initPortNumber(0);
		
		ProcessBuilder processBuilder = null;
		if(null != jvmArgs){
			processBuilder =
		                new ProcessBuilder(path, "-cp", 
		                classpath, jvmArgs, 
		                SandboxJvmServer.class.getName(),
		                namePrefix, String.valueOf(portNumber));
		} else {
			processBuilder =
	                new ProcessBuilder(path, "-cp", 
	                classpath, SandboxJvmServer.class.getName(),
	                namePrefix, String.valueOf(portNumber));
		}
		
		processBuilder.redirectError(Redirect.INHERIT);
		processBuilder.redirectOutput(Redirect.INHERIT);
		try {
			Process process = processBuilder.start();
			
			return new JvmImpl(namePrefix, portNumber, process);
		} catch (IOException e) {
			throw new JvmInstantiatonException(e);
		}
	}

	protected synchronized String initNamePrefix(){
		int n = number;
		number++;
		return "Nr" + n;
	}
	
	protected synchronized int initPortNumber(int tries){
		if(tries > maxPortNumber - minPortNumber)
			throw new IllegalStateException("Could not find free port");
		
		int port = currentPortNumber;
		currentPortNumber++;
		if(currentPortNumber > maxPortNumber)
			currentPortNumber = minPortNumber;
		
		if(! portAvailable(port))
			return initPortNumber(tries ++);
		
		return port;
	}

	private boolean portAvailable(int port) {
	    ServerSocket socket = null;
	    try {
	        socket = new ServerSocket(port);
	    } catch (IOException e) {
	    	return false;
	    } finally {
	        if (socket != null) {
	            try {
	                socket.close();
	            } catch (IOException e) { 
	            	logger.log(Level.WARNING, "Could not close socket for port availability test", e);
            	}
	        }
	    }
	    return true;
	}
	

	
	
}
