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

import java.io.Serializable;
import java.security.CodeSource;
import java.security.Permission;
import java.util.concurrent.TimeUnit;

import net.datenwerke.sandbox.handlers.SandboxHandler;
import net.datenwerke.sandbox.jvm.JvmFreelancer;
import net.datenwerke.sandbox.jvm.JvmPool;

import com.google.inject.ImplementedBy;

/**
 * The java-sandbox library's main service's interface definition. 
 * 
 * @author Arno Mittelbach
 *
 */
@ImplementedBy(SandboxServiceImpl.class)
public interface SandboxService {

	/**
	 * A bean to be used with {@link SandboxService#runInContext(Class, SandboxContext, Object...)}
	 * and {@link SandboxService#runSandboxed(Class, SandboxContext, Object...)} to define the type
	 * of an argument  that is to be passed to the constructor and whether or not to bridge the
	 * object to the new classloader.
	 * 
	 * @author Arno Mittelbach
	 *
	 */
	public class TypedArgument implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = 5795033400500165853L;
		private final Class<?> type;
		private final Object value;
		private final boolean bridge;
		
		/**
		 * Default is not to use bridging.
		 * 
		 * @param type
		 * @param value
		 */
		public TypedArgument(Class<?> type, Object value){
			this(type, value, false);
		}
		
		public TypedArgument(Class<?> type, Object value, boolean bridge){
			this.type = type;
			this.value = value;
			this.bridge = bridge;
		}
		
		public Class<?> getType() {
			return type;
		}
		
		public Object getValue() {
			return value;
		}
		
		public boolean isBridge() {
			return bridge;
		}
	}
	
	/**
	 * 
	 * @return the security manager used by the sandbox.
	 */
	public SandboxSecurityManager getManager();
	
	/**
	 * 
	 * @return true if the security manager has been installed
	 */
	public boolean isActive();
	
	/**
	 * 
	 * @param handler
	 */
	public void attachHandler(SandboxHandler handler);
	
	/**
	 * 
	 * @param handler
	 * @return
	 */
	public boolean dettachHandler(SandboxHandler handler);
	
	/**
	 * Enables the sandbox for the current thread.
	 * 
	 * @param pw The password needed to disable the sandbox.
	 * @param context The {@link SandboxContext} to configure the sandbox.
	 * @return the password
	 */
	public String restrict(String pw, SandboxContext context);

	/**
	 * Enables the sandbox for the current thread.
	 * 
	 * @param pw The password
	 * @param context The name of a predefined {@link SandboxContext}
	 * 
	 * @see #registerContext(String, SandboxContext)
	 * 
	 * @return
	 */
	public String restrict(String pw, String context);
	
	/**
	 * Enables the sandbox for the current thread.
	 * 
	 * @see #restrict(String, SandboxContext)
	 * 
	 * @return the password
	 */
	public String restrict();
	
	/**
	 * Enables the sandbox for the current thread.
	 * 
	 * @see #restrict(String, SandboxContext)
	 * 
	 * @return the password
	 */
	String restrict(String context);
	
	/**
	 * @see #restrict(String, SandboxContext)
	 * 
	 * @param context
	 * @return The password
	 */
	public String restrict(SandboxContext context);

	/**
	 * Returns true if the sandbox is active for the current thread.
	 * 
	 * @return true if the sandbox is currently enabled
	 */
	public boolean isRestricted();
	
	/**
	 * Disables the sandbox if the password is correct.
	 * 
	 * @param pw The password
	 */
	public void releaseRestriction(String pw);

	/**
	 * Allows to register predefined {@link SandboxContext}s that can later on be referenced by name.
	 * 
	 * @param name
	 * @param context
	 */
	public void registerContext(String name, SandboxContext context);
	
	/**
	 * Transforms objects from the {@link SandboxLoader} to the application class loader.
	 * 
	 * @param result
	 * @return
	 */
	public Object bridge(Object result);

	/**
	 * Runs the code wrapped by the {@link SandboxedEnvironment} class in a sandbox using
	 * the context as configuration and a fresh {@link SandboxLoader} to load the sandbox. The objects
	 * given as arguments are passed to the constructor of the {@link SandboxedEnvironment} class.
	 * 
	 * Use {@link TypedArgument}s as arguments to specify the type and whether or not to use bridging
	 * ({@link #bridge(Object)}).
	 * 
	 * @see TypedArgument
	 * @see #runInContext(Class, SandboxContext, Object...)
	 * 
	 * @param call
	 * @param context
	 * @param args
	 * @return
	 */
	public <V> SandboxedCallResult<V> runSandboxed(Class<? extends SandboxedEnvironment> call,
			SandboxContext context, Object... args);

	/**
	 * Runs the code wrapped by the {@link SandboxedEnvironment} class in a sandbox using
	 * the context as configuration and the specified classloader to load any classes. The objects
	 * given as arguments are passed to the constructor of the {@link SandboxedEnvironment} class.
	 * 
	 * Use {@link TypedArgument}s as arguments to specify the type and whether or not to use bridging
	 * ({@link #bridge(Object)}).
	 * 
	 * @see TypedArgument
	 * @see #runInContext(Class, SandboxContext, Object...)
	 * 
	 * @param call The wrapper containing the to be executed code.
	 * @param context The configuration of the Sandbox
	 * @param loader Should usually be an instance of {@link SandboxLoader}
	 * @param args Arguments given to the constructor of {@link SandboxedEnvironment}
	 * @return
	 */
	public <V> SandboxedCallResult<V> runSandboxed(Class<? extends SandboxedEnvironment> call,
			SandboxContext context, ClassLoader loader, Object... args);
	
	/**
	 * Runs the code wrapped by the {@link SandboxedEnvironment} class in the context defined by
	 * context as configuration and a fresh {@link SandboxLoader} to load any classes. Note that such a
	 * call does not invoke a sandbox! The objects
	 * given as arguments are passed to the constructor of the {@link SandboxedEnvironment} class.
	 * 
	 * Use {@link TypedArgument}s as arguments to specify the type and whether or not to use bridging
	 * ({@link #bridge(Object)}).
	 * 
	 * @see TypedArgument
	 * @see #runSandboxed(Class, SandboxContext, Object...)
	 * 
	 * @param all The wrapper containing the to be executed code.
	 * @param context The configuration of the Sandbox
	 * @param args Arguments given to the constructor of {@link SandboxedEnvironment}
	 * @return
	 */
	public <V> SandboxedCallResult<V> runInContext(Class<? extends SandboxedEnvironment> call,
			SandboxContext context, Object... args);
	
	/**
	 * Runs the code wrapped by the {@link SandboxedEnvironment} class in the context defined by
	 * context as configuration and the ClassLoader loader to load any classes. Note that such a
	 * call does not invoke a sandbox! The objects
	 * given as arguments are passed to the constructor of the {@link SandboxedEnvironment} class.
	 * 
	 * Use {@link TypedArgument}s as arguments to specify the type and whether or not to use bridging
	 * ({@link #bridge(Object)}).
	 * 
	 * @see TypedArgument
	 * @see #runSandboxed(Class, SandboxContext, Object...)
	 * 
	 * @param call The wrapper containing the to be executed code.
	 * @param context The configuration of the Sandbox
	 * @param loader Should usually be an instance of {@link SandboxLoader}
	 * @param args Arguments given to the constructor of {@link SandboxedEnvironment}
	 * @return
	 */
	public <V> SandboxedCallResult<V> runInContext(Class<? extends SandboxedEnvironment> call,
			SandboxContext context, ClassLoader loader, Object... args);


	/**
	 * Provides access to registered predefined {@link SandboxContext}s.
	 * 
	 * @param name The name of the {@link SandboxContext}
	 * @return The {@link SandboxContext}
	 */
	public SandboxContext getContext(String name);
	
	/**
	 * Initializes a {@link SandboxLoader} with the given context.
	 * 
	 * @param context
	 * @return
	 */
	public SandboxLoader initClassLoader(SandboxContext context);


	/**
	 * Initializes a {@link SandboxLoader} with the given classloader as parent
	 * the given context.
	 * 
	 * @param parent
	 * @param context
	 * @return
	 */
	SandboxLoader initClassLoader(ClassLoader parent, SandboxContext context);

	/**
	 * Initializes a {@link SandboxLoader} with 
	 * the context regiesterd under context.
	 * 
	 * @param context
	 * @return
	 */
	SandboxLoader initClassLoader(String context);

	/**
	 * Initializes a {@link SandboxLoader} with the given classloader as parent and
	 * the context regiesterd under context.
	 * 
	 * @param parent
	 * @param context
	 * @return
	 */
	SandboxLoader initClassLoader(ClassLoader parent, String context);

	/**
	 * Tries to convert object value to an object recognizable by the given Classloader.
	 * 
	 * @param value
	 * @param loader
	 * @return
	 */
	Object bridge(Object value, ClassLoader loader);

	/**
	 * Blocks to acquire a {@link JvmFreelancer}.
	 * 
	 * @see JvmPool#acquireFreelancer
	 * @return
	 * @throws InterruptedException
	 */
	JvmFreelancer acquireFreelancer() throws InterruptedException;

	/**
	 * Blocks to acquire a {@link JvmFreelancer}. Throws {@link InterruptedException} if in the
	 * specified time no {@link JvmFreelancer} became available.
	 * 
	 * @see JvmPool#acquireFreelancer(long, TimeUnit)
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws InterruptedException
	 */
	JvmFreelancer acquireFreelancer(long timeout, TimeUnit unit)
			throws InterruptedException;

	/**
	 * Returns the {@link JvmFreelancer} object to the jvm pool.
	 * 
	 * @see JvmPool#releaseFreelancer(JvmFreelancer)
	 * @param freelancer
	 */
	void releaseFreelancer(JvmFreelancer freelancer);

	/**
	 * Shuts down the jvm pool
	 */
	public void shutdownJvmPool();
	
	/**
	 * Restarts the jvm pool needed to execute remote sandboxes
	 */
	public void restartJvmPool();

	/**
	 * Starts a new monitor daemon.
	 */
	void startMonitorDaemon();

	/**
	 * Returns true if the monitor daemon is active.
	 * @return
	 */
	boolean isMonitorDaemonActive();

	/**
	 * Shuts down the monitor daemon.
	 */
	void shutdownMonitorDaemon();

	/**
	 * Starts a new monitor watchdog.
	 */
	void startMonitorWatchdog();

	/**
	 * Returns true if the monitor watchdog is active
	 * @return
	 */
	boolean isMonitorWatchdogActive();

	/**
	 * Shuts down the monitor watchdog.
	 */
	void shutdownMonitorWatchdog();

	/**
	 * 
	 * @see #setMonitorDaemonCheckInterval(long)
	 * @return
	 */
	long getMonitorDaemonCheckInterval();

	/**
	 * Sets the interval (in milliseconds) between checks by the monitor daemon thread.
	 * @param monitorDaemonCheckInterval
	 */
	void setMonitorDaemonCheckInterval(long monitorDaemonCheckInterval);

	/**
	 * 
	 * @see #setMonitorWatchdogCheckInterval(long)
	 * @return
	 */
	long getMonitorWatchdogCheckInterval();

	/**
	 * Sets the interval (in milliseconds) between checks by the monitor watchdog
	 * which monitors the state of the the {@link SandboxMonitorDaemon} thread.
	 * 
	 * @param monitorWatchdogCheckInterval
	 */
	void setMonitorWatchdogCheckInterval(long monitorWatchdogCheckInterval);

	/**
	 * Shuts down the current {@link JvmPool} and initializes a new one with the given
	 * configuration.
	 * 
	 * @param pool
	 */
	void initJvmPool(JvmPool pool);

	/**
	 * Closes all resources held open by the {@link SandboxService}. This includes the {@link JvmPool}
	 * and monitoring threads.
	 * 
	 * @see #shutdownJvmPool()
	 * @see #shutdownMonitorDaemon()
	 * @see #shutdownMonitorWatchdog()
	 */
	void shutdown();

	/**
	 * Enables/Disables the use java's own security checks using the default implementation
	 * of {@link SecurityManager} and {@link CodeSource} based checking of {@link Permission}s.
	 * Default is not to use java's {@link SecurityManager}.
	 * 
	 * @param enable
	 */
	void setCodesourceSecurityChecks(boolean enable);

	/**
	 * Returns true if java's own security checks are enabled.
	 * 
	 * @see #setCodesourceSecurityChecks(boolean)
	 * @return
	 */
	boolean getCodesourceSecurityChecks();

	/**
	 * Returns true if this is the sandbox service of a remote jvm agent.
	 * @return
	 */
	boolean isRemoteService();

	/**
	 * Returns true if this sandbox has remote agents
	 * @return
	 */
	public boolean hasRemoteAgents();


}
