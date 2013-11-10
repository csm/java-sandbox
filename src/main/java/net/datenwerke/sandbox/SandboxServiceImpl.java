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

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import net.datenwerke.sandbox.annotations.EnableSandboxing;
import net.datenwerke.sandbox.exception.SandboxConfigurationException;
import net.datenwerke.sandbox.exception.SandboxException;
import net.datenwerke.sandbox.exception.SandboxedTaskKilledException;
import net.datenwerke.sandbox.handlers.BadThreadKillHandler;
import net.datenwerke.sandbox.handlers.ContextRegisteredHandler;
import net.datenwerke.sandbox.handlers.SandboxHandler;
import net.datenwerke.sandbox.jvm.JvmFreelancer;
import net.datenwerke.sandbox.jvm.JvmPool;
import net.datenwerke.sandbox.jvm.JvmPoolConfigImpl;
import net.datenwerke.sandbox.jvm.JvmPoolImpl;
import net.datenwerke.sandbox.jvm.JvmSandboxTask;
import net.datenwerke.sandbox.jvm.exceptions.JvmKilledThreadRuntimeException;
import net.datenwerke.sandbox.jvm.exceptions.JvmKilledUnsafeThreadRuntimeException;
import net.datenwerke.sandbox.securitypermissions.SandboxRuntimePermission;
import net.datenwerke.transloader.DefaultTransloader;
import net.datenwerke.transloader.ObjectWrapper;
import net.datenwerke.transloader.Transloader;
import net.datenwerke.transloader.configure.CloningStrategy;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * The java-sandbox. 
 * 
 * The java-sandbox allows to securely execute untrusted code, such as third-party or 
 * user generated code from within your application. It allows to specify resources 
 * and classes that may be used by the code, thus, separating the execution from the 
 * application's execution environment.
 *
 * <h2>Singleton</h2>
 * The {@link SandboxServiceImpl} should to be treated as a singleton. On the first invocation
 * the security manager is installed.
 *  
 * @author Arno Mittelbach
 */

/* note the singleton is important as otherwise the security manager is reset */
@Singleton
public class SandboxServiceImpl implements SandboxService {

	private final Logger logger = Logger.getLogger(getClass().getName());
	
	private static SandboxService INSTANCE;
	
	protected final SandboxCleanupService cleanupService;
	protected final boolean enabled;
	
	protected Map<String, SandboxContext> registeredContexts = new Hashtable<String, SandboxContext>();

	protected JvmPool jvmPool;

	private Set<SandboxHandler> handlers = new HashSet<SandboxHandler>();

	protected final ConcurrentLinkedQueue<SandboxMonitoredThread> monitorQueue;
	private int monitorCnt = 1;
	private SandboxMonitorDaemon monitorDaemon;
	private Thread monitorDaemonThread;
	private SandboxMonitorWatchdog monitorWatchdog;
	private Thread monitorWatchdogThread;
	private long monitorDaemonCheckInterval = 10;
	private long monitorWatchdogCheckInterval = 10000;



	/**
	 * 
	 */
	public SandboxServiceImpl(){
		this(true, 
			new SandboxCleanupServiceImpl(), 
			new JvmPoolImpl(new JvmPoolConfigImpl(1, 1)));
	}
	
	public SandboxServiceImpl(File pathToSandCastle){
		this(true, 
			new SandboxCleanupServiceImpl(), 
			new JvmPoolImpl(new JvmPoolConfigImpl(1, 1, "-Xbootclasspath/p:" + pathToSandCastle.getAbsolutePath())));
	}
	
	/**
	 * Initializes the Sandbox service. 
	 * 
	 * Note that this service should be treated as a singleton.
	 * 
	 * @param enabled true to install the security manager
	 */
	@Inject
	public SandboxServiceImpl(
		@EnableSandboxing boolean enabled,
		SandboxCleanupService cleanupService,
		@Nullable JvmPool jvmPool
		){
		this.enabled = enabled;
		this.cleanupService = cleanupService;
		this.jvmPool = jvmPool;
				
		if(null == INSTANCE)
			INSTANCE = this;
		else
			throw new IllegalStateException("SandboxService already instantiated");
		
		if(enabled)
			enable();
		
		monitorQueue = new ConcurrentLinkedQueue<SandboxMonitoredThread>();
		startMonitorDaemon();
		
		Runtime.getRuntime().addShutdownHook(new Thread(){
			@Override
			public void run() {
				shutdown();
			}
		});
	}

	final private void enable() {
		if(! (System.getSecurityManager() instanceof SandboxSecurityManager)){
			try {
				SandboxSecurityManager securityManager = SandboxSecurityManager.newInstance(this);
				System.setSecurityManager(securityManager);
			} catch (Exception e) {
				throw new RuntimeException("Could not initialize security manager: ", e);
			}
		}
	}

	/**
	 * Returns the instance of the {@link SandboxServiceImpl}.
	 * @return
	 */
	public static SandboxService getInstance(){
		if(null == INSTANCE)
			new SandboxServiceImpl();
		return INSTANCE;
	}
	
	public static SandboxService initInstance(File pathToSandcastle){
		if(null != INSTANCE)
			throw new IllegalStateException("already initialized");
		return new SandboxServiceImpl(pathToSandcastle);
	}
	
	/**
	 * Initializes a {@link SandboxServiceImpl} without remote jvm agents.
	 * 
	 * @return
	 */
	public static SandboxService initLocalSandboxService(){
		if(null != INSTANCE)
			throw new IllegalStateException("already initialized");
		return new SandboxServiceImpl(true, 
			new SandboxCleanupServiceImpl(), 
			null);
	}
	
	@Override
	public void setCodesourceSecurityChecks(boolean enable){
		SandboxSecurityManager securityManager = (SandboxSecurityManager) System.getSecurityManager();
		if(null != securityManager)
			securityManager.setCodesourceSecurityChecks(enable);
	}
	
	@Override
	public boolean getCodesourceSecurityChecks(){
		SandboxSecurityManager securityManager = (SandboxSecurityManager) System.getSecurityManager();
		if(null != securityManager)
			return securityManager.isCodesourceSecurityChecks();
		return false;
	}
	
	@Override
	public void startMonitorDaemon() {
		if(isMonitorDaemonActive())
			return;
			
		if(null != monitorDaemon)
			monitorDaemon.shutdown();
		
		monitorDaemon = new SandboxMonitorDaemon(this, monitorQueue);
		monitorDaemon.setCheckInterval(monitorDaemonCheckInterval);
		
		monitorDaemonThread = Executors.defaultThreadFactory().newThread(monitorDaemon);
		monitorDaemonThread.setDaemon(true);
		monitorDaemonThread.setName("sandboxMonitor-" + monitorCnt ++);
		monitorDaemonThread.start();
		
		startMonitorWatchdog();
	}
	
	@Override
	public boolean isMonitorDaemonActive() {
		return null != monitorDaemonThread && monitorDaemonThread.isAlive() && null != monitorDaemon && ! monitorDaemon.isShutdown();
	}
	
	@Override
	public void shutdownMonitorDaemon(){
		if(null != monitorDaemon)
			monitorDaemon.shutdown();
	}

	@Override
	public void startMonitorWatchdog() {
		if(isMonitorWatchdogActive())
			return;
		
		if(null != monitorWatchdog)
			monitorWatchdog.shutdown();
		
		monitorWatchdog = new SandboxMonitorWatchdog(this);
		monitorWatchdog.setCheckInterval(monitorWatchdogCheckInterval);
		
		monitorWatchdogThread = Executors.defaultThreadFactory().newThread(monitorWatchdog);
		monitorWatchdogThread.setDaemon(true);
		monitorWatchdogThread.setName("sandboxMonitorWatchdog");
		monitorWatchdogThread.start();
	}
	
	@Override
	public boolean isMonitorWatchdogActive() {
		return null != monitorWatchdogThread && monitorWatchdogThread.isAlive() && ! monitorWatchdog.isShutdown();
	}
	
	@Override
	public void shutdownMonitorWatchdog(){
		if(null != monitorWatchdog)
			monitorWatchdog.shutdown();
	}
	
	@Override
	public long getMonitorDaemonCheckInterval() {
		return monitorDaemonCheckInterval;
	}

	@Override
	public void setMonitorDaemonCheckInterval(long monitorDaemonCheckInterval) {
		this.monitorDaemonCheckInterval = monitorDaemonCheckInterval;
	}

	@Override
	public long getMonitorWatchdogCheckInterval() {
		return monitorWatchdogCheckInterval;
	}

	@Override
	public void setMonitorWatchdogCheckInterval(long monitorWatchdogCheckInterval) {
		this.monitorWatchdogCheckInterval = monitorWatchdogCheckInterval;
	}

	@Override
	public void shutdown() {
		shutdownJvmPool();
		shutdownMonitorWatchdog();
		shutdownMonitorDaemon();
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.datenwerke.rs.utils.sandbox.SandboxingService#getManager()
	 */
	@Override
	public SandboxSecurityManager getManager() {
		if(! isActive())
			return null;
		
		SecurityManager manager = System.getSecurityManager();
		return (SandboxSecurityManager) manager;
	}

	/*
	 * (non-Javadoc)
	 * @see net.datenwerke.rs.utils.sandbox.SandboxingService#isActive()
	 */
	@Override
	public boolean isActive() {
		return enabled;
	}

	/*
	 * (non-Javadoc)
	 * @see net.datenwerke.rs.utils.sandbox.SandboxingService#restrict()
	 */
	@Override
	public String restrict() {
		if(! isActive())
			return null;
		
		String pw = UUID.randomUUID().toString();
		SandboxContext baseSet = new SandboxContext();
		
		getManager().restrictAccess(pw, baseSet);
		
		return pw;
	}


	/*
	 * (non-Javadoc)
	 * @see net.datenwerke.rs.utils.sandbox.SandboxingService#restrict(java.lang.String, java.lang.String)
	 */
	@Override
	public synchronized String restrict(String context){
		SandboxContext set = registeredContexts.get(context);
		if(null == set) 
			set = new SandboxContext();
		return restrict(set);
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.datenwerke.rs.utils.sandbox.SandboxingService#restrict(java.lang.String, java.lang.String)
	 */
	@Override
	public synchronized String restrict(String pw, String context){
		if(! isActive())
			return pw;
		SandboxContext set = registeredContexts.get(context);
		if(null == set) 
			set = new SandboxContext();
		return restrict(pw, set);
	}
	
	@Override
	public String restrict(SandboxContext context) {
		String pw = UUID.randomUUID().toString();
		restrict(pw, context);
		return pw;
	}
	
	@Override
	public String restrict(String pw, SandboxContext context) {
		if(! isActive())
			return null;
		getManager().restrictAccess(pw, context);
		return pw;
	}
	
	@Override
	public JvmFreelancer acquireFreelancer() throws InterruptedException{
		return jvmPool.acquireFreelancer();
	}
	
	@Override
	public JvmFreelancer acquireFreelancer(long timeout, TimeUnit unit) throws InterruptedException{
		return jvmPool.acquireFreelancer(timeout, unit);
	}
	
	@Override
	public void releaseFreelancer(JvmFreelancer freelancer){
		jvmPool.releaseFreelancer(freelancer);
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.datenwerke.sandbox.SandboxService#shutdownPool()
	 */
	@Override
	public void shutdownJvmPool() {
		if(null != jvmPool)
			jvmPool.shutdown();
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.datenwerke.sandbox.SandboxService#restartPool()
	 */
	@Override
	public void restartJvmPool() {
		if(null != jvmPool)
			jvmPool.restart();
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.datenwerke.sandbox.SandboxService#initJvmPool(net.datenwerke.sandbox.jvm.JvmPool)
	 */
	@Override
	public void initJvmPool(JvmPool pool){
		shutdownJvmPool();
		jvmPool = pool;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.datenwerke.rs.utils.sandbox.SandboxingService#isRestricted()
	 */
	@Override
	public boolean isRestricted() {
		return isActive() && getManager().isRestricted();
	}
	
	public SandboxSecurityManager getSecurityManager(){
		return (SandboxSecurityManager) System.getSecurityManager();
	}
	
	@Override
	public synchronized void registerContext(String name, SandboxContext context){
		getSecurityManager().checkPermission(new SandboxRuntimePermission("registerSandboxContext"));
		
		for(ContextRegisteredHandler handler : getHandlers(ContextRegisteredHandler.class))
			handler.contextRegistered(name, context);
		
		registeredContexts.put(name, context);			
	}
	
	@Override
	public void releaseRestriction(String pw) {
		if(! isActive())
			return;
		getManager().releaseRestriction(pw);
	}

	/*
	 * (non-Javadoc)
	 * @see net.datenwerke.sandbox.SandboxService#initClassLoader(java.lang.String)
	 */
	@Override
	public SandboxLoader initClassLoader(String restrictSet) {
		return initClassLoader(getContext(restrictSet));
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.datenwerke.sandbox.SandboxService#initClassLoader(net.datenwerke.sandbox.SandboxContext)
	 */
	@Override
	public SandboxLoader initClassLoader(SandboxContext restrictSet) {
		return initClassLoader(getClass().getClassLoader(), restrictSet);
	}

	/*
	 * (non-Javadoc)
	 * @see net.datenwerke.sandbox.SandboxService#initClassLoader(java.lang.ClassLoader, java.lang.String)
	 */
	@Override
	public SandboxLoader initClassLoader(ClassLoader loader, String context) {
		return initClassLoader(loader, getContext(context));
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.datenwerke.sandbox.SandboxService#initClassLoader(java.lang.ClassLoader, net.datenwerke.sandbox.SandboxContext)
	 */
	@Override
	public SandboxLoader initClassLoader(ClassLoader loader, SandboxContext context) {
		SandboxLoader sandboxLoader = new SandboxLoader(loader);
		sandboxLoader.init(context);
		
		return sandboxLoader;
	}

	@Override
	public SandboxContext getContext(String name){
		getSecurityManager().checkPermission(new SandboxRuntimePermission("getSandboxContext"));
		
		if(! registeredContexts.containsKey(name))
			return null;
		return registeredContexts.get(name);
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.datenwerke.sandbox.SandboxService#runSandboxed(java.lang.Class, net.datenwerke.sandbox.SandboxContext, java.lang.Object[])
	 */
	@Override
	public <V> SandboxedCallResult<V> runSandboxed(Class<? extends SandboxedEnvironment> call, SandboxContext context, Object... args){
		SandboxLoader loader = initClassLoader(context);

		return runSandboxed(call, context, loader, args);
	}
		
	/*
	 * (non-Javadoc)
	 * @see net.datenwerke.sandbox.SandboxService#runSandboxed(java.lang.Class, net.datenwerke.sandbox.SandboxContext, java.lang.ClassLoader, java.lang.Object[])
	 */
	@Override
	public <V> SandboxedCallResult<V> runSandboxed(Class<? extends SandboxedEnvironment> call, SandboxContext context, ClassLoader loader, Object... args){
		return run(call, context, loader, false, args);
	}

	
	protected <V> SandboxedCallResult<V> run(Class<? extends SandboxedEnvironment> call,
			SandboxContext context, ClassLoader loader, boolean runInContext, Object[] args) {
		if(null == call)
			return null;

		if(isRemoteService() && context.isDebug())
			logger.log(Level.INFO, "run remote");
		
		try{
			Object result = null;
			if(! isRemoteService() && context.isRunRemote() && null != jvmPool ){
				result = jvmPool.addTask(new JvmSandboxTask(call, context, runInContext, args)).get().getRaw();
			} else if(context.isRunInThread()){
				Method runMethod = getCalleableMethod(call, loader);
				Object instance = getCalleableInstance(call, loader, args);
				
				SandboxedThread thread = new SandboxedThread(this, runMethod, instance, context, runInContext);
				thread.setContextClassLoader(loader);
				
				/* put in monitor queue */
				monitorQueue.add(new SandboxMonitoredThread(Thread.currentThread(), thread, context));
				
				/* start thread and wait */
				try{
					thread.start();
					thread.join();
				} catch(InterruptedException ignore){
				}

				/* obtain result */
				if(thread.isSuccess())
					result = thread.getResult();
				else {
					if(isRemoteService()&& thread.isKilled() &&! thread.isKilledSafely())
						throw new JvmKilledUnsafeThreadRuntimeException();

					Exception e = thread.getException();
					if(null == e)
						throw new JvmKilledThreadRuntimeException();
					
					throw e;
				}
			} else {
				Method runMethod = getCalleableMethod(call, loader);
				Object instance = getCalleableInstance(call, loader, args);
				
				if(runInContext)
					result = runMethod.invoke(instance);
				else {
					String pw = restrict(context);
					try{
						result = runMethod.invoke(instance);
					} finally {
						releaseRestriction(pw);
					}
				}
			}
			
			return new SandboxedCallResultImpl<V>(result);
		} catch(Exception e){
			if(e instanceof JvmKilledUnsafeThreadRuntimeException)
				throw (JvmKilledUnsafeThreadRuntimeException)e;
			if(e instanceof SandboxException)
				throw (SandboxException)e;
			if(e instanceof InvocationTargetException && null != e.getCause())
				throw new SandboxException(e.getCause().getClass().getName() + ": " + e.getCause().getMessage(), e);
			throw new SandboxException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.datenwerke.sandbox.SandboxService#isRemoteService()
	 */
	@Override
	public boolean isRemoteService(){
		return false;
	}

	@Override
	public boolean hasRemoteAgents() {
		return null != jvmPool;
	}

	/*
	 * (non-Javadoc)
	 * @see net.datenwerke.sandbox.SandboxService#runInContext(java.lang.Class, net.datenwerke.sandbox.SandboxContext, java.lang.Object[])
	 */
	@Override
	public <V> SandboxedCallResult<V> runInContext(Class<? extends SandboxedEnvironment> call,
			SandboxContext context, Object... args){
		SandboxLoader loader = initClassLoader(context);
		return runInContext(call, context, loader, args);
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.datenwerke.sandbox.SandboxService#runInContext(java.lang.Class, net.datenwerke.sandbox.SandboxContext, java.lang.ClassLoader, java.lang.Object[])
	 */
	@Override
	public <V> SandboxedCallResult<V> runInContext(Class<? extends SandboxedEnvironment> call,
			SandboxContext context, ClassLoader loader, Object... args){
		return run(call, context, loader, true, args);
	}
	
	/**
	 * 
	 */
	protected Method getCalleableMethod(Class<?> call, ClassLoader loader) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
		Class<?> clazz = Class.forName(call.getName(), true, loader);
		Method m = clazz.getMethod("execute");
		m.setAccessible(true);

		return m;
	}
	
	protected Object getCalleableInstance(Class<?> call, ClassLoader loader, Object... args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
		Class<?> clazz = Class.forName(call.getName(), true, loader);

		/* find constructor */
		Constructor constructor = null;
		Object[] values = null;
		if(null == args || args.length == 0) 
			constructor = clazz.getDeclaredConstructor((Class<?>[])null);
		else {
			int length = args.length;
			Class<?>[] typeArray = new Class<?>[length];
			values = new Object[length];
			for(int i = 0; i<length; i++){
				Object o = args[i];
				if(o instanceof TypedArgument){
					typeArray[i] = Class.forName(((TypedArgument)o).getType().getName(), true, loader);
					if(((TypedArgument)o).isBridge())
						values[i] = bridge(((TypedArgument)o).getValue(), loader);
					else 
						values[i] = ((TypedArgument)o).getValue();
				} else {
					typeArray[i] = Class.forName(o.getClass().getName(), true, loader);
					values[i] = o; 
				}
			}
			
			try{
				constructor = clazz.getDeclaredConstructor(typeArray);
			} catch(NoSuchMethodException e){
				throw new SandboxConfigurationException(e.getMessage()  + ": Could not load constructor for the SandboxedEnvironment object. I tried to find a constructor " +
						"with the following types: " + typeArray +
						" Is there a classloader problem?", e);
			}
		}
			
		/* generate instance */
		constructor.setAccessible(true);
		if(null == values)
			return constructor.newInstance();
		try{
			return constructor.newInstance(values);
		} catch(IllegalArgumentException e){
			StringBuffer buf = new StringBuffer("[");
			for(Object o : values)
				buf.append(o).append(", ");
			buf.append("]");
			throw new SandboxConfigurationException(e.getMessage() + ": Could not instantiate the SandboxedEnvironment object. " +
					"Is there a classloader problem? The SandboxedEnvironment's constructor expects " + 
					constructor + " but I got: " + buf, e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.datenwerke.sandbox.SandboxService#bridge(java.lang.Object)
	 */
	@Override
	public Object bridge(Object result) {
		return bridge(result, getClass().getClassLoader());
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.datenwerke.rs.utils.sandbox.SandboxingService#bridge(java.lang.Object)
	 */
	@Override
	public Object bridge(Object result, ClassLoader loader) {
		if(null == result)
			return null;
		
		if(result.getClass().getClassLoader() == null && loader == null)
			return result;
		if(null != result.getClass().getClassLoader() && result.getClass().getClassLoader().equals(loader))
			return result;
		
		Transloader transloader = new DefaultTransloader(CloningStrategy.newMinimalInstance());
		ObjectWrapper resultWrapped = transloader.wrap(result);
		
		return resultWrapped.cloneWith(loader);
	}

	void kill(SandboxMonitoredThread monitor, SandboxedTaskKilledException exception) {
		boolean safe = false;
		try{
			BadKillInfo killInfo = cleanupService.kill(monitor, exception);
			if(null == killInfo)
				safe = true;
			else {
				for(BadThreadKillHandler handler : getHandlers(BadThreadKillHandler.class))
					handler.badThreadKilled(killInfo);
			}
		} catch(Exception ignore){}
		
		if(! safe){
			if(! isRemoteService())
				logger.log(Level.SEVERE, "killed potentially unsafe thread");
		}
	}

	@Override
	public synchronized void attachHandler(SandboxHandler handler) {
		getSecurityManager().checkPermission(new SandboxRuntimePermission("attachHandler"));
		
		handlers.add(handler);
	}
	
	@Override
	public synchronized boolean dettachHandler(SandboxHandler handler) {
		getSecurityManager().checkPermission(new SandboxRuntimePermission("dettachHandler"));
		
		return handlers.remove(handler);
	}
	
	protected synchronized <H extends SandboxHandler>  Collection<H> getHandlers(Class<H> clazz) {
		Collection<H> col = new HashSet<H>();
		for(SandboxHandler handler : handlers)
			if(clazz.isAssignableFrom(handler.getClass()))
				col.add((H) handler);
		return col;
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		shutdownJvmPool();
	}
	
}
