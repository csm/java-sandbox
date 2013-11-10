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
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AllPermission;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.datenwerke.sandbox.permissions.ClassPermission;
import net.datenwerke.sandbox.permissions.FileEqualsPermission;
import net.datenwerke.sandbox.permissions.FilePermission;
import net.datenwerke.sandbox.permissions.FilePrefixPermission;
import net.datenwerke.sandbox.permissions.PackagePermission;
import net.datenwerke.sandbox.permissions.SecurityPermission;
import net.datenwerke.sandbox.permissions.StackEntry;
import sun.security.util.SecurityConstants;

/**
 * Object that describes the configuration of a sandbox. This includes
 * configuration for the classloader ({@link SandboxLoader}) as well as
 * configuration for how code is to be executed (in a thread, or even remotely).
 * 
 * The {@link SandboxSecurityManager} uses {@link SandboxContext} to validate
 * permission requests.
 * 
 * @author Arno Mittelbach
 *
 */
public class SandboxContext implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5253800931844658443L;

	private transient Logger logger = Logger.getLogger(getClass().getName());
	
	/**
	 * 
	 * @author Arno Mittelbach
	 */
	public enum FileAccess{ READ, WRITE, DELETE }
	
	/**
	 * 
	 * @author Arno Mittelbach
	 */
	public enum AccessType{ PERMIT, DENY }
	
	/**
	 * 
	 * @author Arno Mittelbach
	 */
	public enum RuntimeMode{ CPU_TIME, ABSOLUTE_TIME}
	
	/**
	 * 
	 * @author Arno Mittelbach
	 */
	public enum Mode{ NORMAL, PREFIX }
	
	private String name = "";
	
	private boolean debug = false;
	
	/* service */
	private boolean runRemote = false;
	private boolean runInThread = false;
	private int maximumStackDepth = -1;
	private long maximumRunTime = -1;
	private TimeUnit maximumRunTimeUnit = TimeUnit.MILLISECONDS;
	private RuntimeMode maximumRuntimeMode  = RuntimeMode.ABSOLUTE_TIME;
	
	
	/* sandbox loader */
	private Collection<URL> jarsForApplicationLoader = new HashSet<URL>();
	private Collection<String> classPrefixesForApplicationLoader = new HashSet<String>();
	private Collection<String> classesForApplicationLoader = new HashSet<String>();
	
	private Collection<String> classPrefixesForSandboxLoader = new HashSet<String>();
	private Collection<String> classesForSandboxLoader = new HashSet<String>();
	
	private Map<URL, SandboxContext> subLoaderContextByJar = new HashMap<URL, SandboxContext>();
	private Map<String, SandboxContext> subLoaderContextByClass = new HashMap<String, SandboxContext>();
	private Map<String, SandboxContext> subLoaderContextByClassPrefix = new HashMap<String, SandboxContext>();
	
	private String codesource;
	
	private SandboxLoaderEnhancer loaderEnhancer;
	
	private boolean removeFinalizers = true;
	
	/* permissions */
	private Map<String, Collection<SecurityPermission>> permissionWhitelist = new HashMap<String, Collection<SecurityPermission>>();
	private Map<String, Collection<SecurityPermission>> permissionBlacklist = new HashMap<String, Collection<SecurityPermission>>();
	
	private Collection<String> classPrefixWhitelist = new HashSet<String>();
	private Collection<String> classPrefixBlacklist = new HashSet<String>();
	private Collection<String> classBlacklist = new HashSet<String>();
	private Collection<String> classWhitelist = new HashSet<String>();
	private Collection<ClassPermission> complexClassWhitelist = new HashSet<ClassPermission>();
	
	private Collection<String> packagePrefixWhitelist = new HashSet<String>();
	private Collection<String> packagePrefixBlacklist = new HashSet<String>();
	private Collection<String> packageWhitelist = new HashSet<String>();
	private Collection<String> packageBlacklist = new HashSet<String>();
	private Collection<PackagePermission> complexPackageWhitelist = new HashSet<PackagePermission>();
	
	private Collection<FilePermission> fileReadPermissions = new HashSet<FilePermission>();
	private Collection<FilePermission> fileReadDenials = new HashSet<FilePermission>();

	private Collection<FilePermission> fileWritePermissions = new HashSet<FilePermission>();
	private Collection<FilePermission> fileWriteDenials = new HashSet<FilePermission>();
	
	private Collection<FilePermission> fileDeletePermissions = new HashSet<FilePermission>();
	private Collection<FilePermission> fileDeleteDenials = new HashSet<FilePermission>();
	
	private Collection<URL> whitelistedJars = new HashSet<URL>();
	
	private boolean passAll = false;
	private boolean bypassClassAccessChecks = false;
	private boolean bypassPermissionAccessChecks = false;
	private boolean bypassPackageAccessChecks = true;
	
	/**
	 * Initializes a new context.
	 */
	public SandboxContext(){
		this(true);
	}
	
	/**
	 * 
	 * @param initDefault
	 */
	public SandboxContext(boolean initDefault){
		if(initDefault){
			addClasspath();
			
			/* blacklist important objects */
			addSecurityPermission(AccessType.DENY, new SecurityPermission("java.lang.reflect.SuppressAccessCheckPermission", "net.datenwerke.sandbox.SandboxSecurityManager"));
			addSecurityPermission(AccessType.DENY, new SecurityPermission("java.lang.reflect.AccessDeclaredMemberPermission", "net.datenwerke.sandbox.SandboxSecurityManager"));
			
			addSecurityPermission(AccessType.DENY, new SecurityPermission("java.lang.reflect.SuppressAccessCheckPermission", "net.datenwerke.sandbox.SandboxLoader"));
			addSecurityPermission(AccessType.DENY, new SecurityPermission("java.lang.reflect.AccessDeclaredMemberPermission", "net.datenwerke.sandbox.SandboxLoader"));
		}
	}
	
	/**
	 * All classes loadable from the jar can be accessed from within the sandbox.
	 * 
	 * @param url URL pointing to a jar.
	 */
	public void addJarToWhitelist(URL url){
		whitelistedJars.add(url);
	}
	
	/**
	 * 
	 * @see #addJarToWhitelist(URL)
	 * @return
	 */
	public Collection<URL> getWhitelistedJars() {
		return whitelistedJars;
	}
	
	/**
	 * 
	 * @see #checkPackageAccess(Class, Class[])
	 * @see SandboxSecurityManager#checkPackageAccess(String)
	 * @param type
	 * @param pkg
	 */
	public void addPackagePermission(AccessType type, String pkg){
		addPackagePermission(type, Mode.PREFIX, pkg);
	}
	
	/**
	 * 
	 * @see #checkPackageAccess(Class, Class[])
	 * @see SandboxSecurityManager#checkPackageAccess(String)
	 * @param type
	 * @param mode
	 * @param pkg
	 */
	public void addPackagePermission(AccessType type, Mode mode, String pkg){
		switch(type){
		case PERMIT:
			if(mode == Mode.PREFIX || pkg.endsWith("."))
				packagePrefixWhitelist.add(pkg);
			else
				packageWhitelist.add(pkg);
			break;
		case DENY: 
			if(mode == Mode.PREFIX || pkg.endsWith("."))
				packagePrefixBlacklist.add(pkg);
			else
				packageBlacklist.add(pkg);
			break;
		}
	}
	
	/**
	 * @see #checkPackageAccess(Class, Class[])
	 * @see SandboxSecurityManager#checkPackageAccess(String)
	 * @param wpkg
	 */
	public void addPackagePermission(PackagePermission wpkg) {
		setBypassPackageAccessChecks(false);
		complexPackageWhitelist.add(wpkg);
	}
	
	/**
	 * 
	 * @see #checkClassAccess(Class, Class[])
	 * @see SandboxSecurityManager#checkClassAccess(String)
	 * @param type
	 * @param classes
	 */
	public void addClassPermission(AccessType type, String... classes){
		for(String clazz : classes)
			addClassPermission(type, Mode.NORMAL, clazz);
	}
	
	/**
	 * 
	 * @see #checkClassAccess(Class, Class[])
	 * @see SandboxSecurityManager#checkClassAccess(String)
	 * @param type
	 * @param clazz
	 */
	public void addClassPermission(AccessType type, String clazz){
		addClassPermission(type, Mode.NORMAL, clazz);
	}
	
	/**
	 * 
	 * @see #checkClassAccess(Class, Class[])
	 * @see SandboxSecurityManager#checkClassAccess(String)
	 * @param type
	 * @param mode
	 * @param clazz
	 */
	public void addClassPermission(AccessType type, Mode mode,  String clazz){
		switch(type){
		case PERMIT:
			if(mode == Mode.PREFIX || clazz.endsWith("."))
				classPrefixWhitelist.add(clazz);
			else
				classWhitelist.add(clazz);
			break;
		case DENY:
			if(mode == Mode.PREFIX || clazz.endsWith("."))
				classPrefixBlacklist.add(clazz);
			else
				classBlacklist.add(clazz);
			break;
		}
	}
	
	/**
	 * 
	 * @see #checkClassAccess(Class, Class[])
	 * @see SandboxSecurityManager#checkClassAccess(String)
	 * @param wclass
	 */
	public void addClassPermission(ClassPermission wclass) {
		complexClassWhitelist.add(wclass);
	}
	
	/**
	 * 
	 * @see SandboxLoader#doGetSubLoaderByClassContext(String)
	 * @see SandboxLoader#getSubloaderByName(String)
	 * @param clazz
	 * @param context
	 */
	public void addSubloaderContext(String clazz, SandboxContext context){
		addSubloaderContext(clazz, Mode.NORMAL, context);
	}
	
	/**
	 * 
	 * @see SandboxLoader#doGetSubLoaderByClassContext(String)
	 * @see SandboxLoader#getSubloaderByName(String)
	 * @param url
	 * @param context
	 */
	public void addSubloaderContext(URL url, SandboxContext context){
		subLoaderContextByJar.put(url, context);
	}
	
	/**
	 * 
	 * @see SandboxLoader#doGetSubLoaderByClassContext(String)
	 * @see SandboxLoader#getSubloaderByName(String)
	 * @param clazz
	 * @param mode
	 * @param context
	 */
	public void addSubloaderContext(String clazz, Mode mode, SandboxContext context) {
		if(mode == Mode.PREFIX || clazz.endsWith("."))
			subLoaderContextByClassPrefix.put(clazz, context);
		else
			subLoaderContextByClass.put(clazz, context);
	}
	
	/**
	 * 
	 * @return
	 */
	public Map<URL, SandboxContext> getSubLoaderContextByJar() {
		return subLoaderContextByJar;
	}
	
	/**
	 * 
	 * @return
	 */
	public Map<String, SandboxContext> getSubLoaderContextByClassMap() {
		return subLoaderContextByClass;
	}

	/**
	 * 
	 * @return
	 */
	public Map<String, SandboxContext> getSubLoaderContextByClassPrefixMap() {
		return subLoaderContextByClassPrefix;
	}
	
	/**
	 * 
	 * @param loaderEnhancer
	 */
	public void setLoaderEnhancer(SandboxLoaderEnhancer loaderEnhancer) {
		this.loaderEnhancer = loaderEnhancer;
	}
	
	/**
	 * 
	 * @return
	 */
	public SandboxLoaderEnhancer getLoaderEnhancer() {
		return loaderEnhancer;
	}
	
	/**
	 * 
	 * @param clazz
	 */
	public void addClassForApplicationLoader(String clazz){
		addClassForApplicationLoader(clazz, Mode.NORMAL);
	}
	
	/**
	 * 
	 * @param classes
	 */
	public void addClassForApplicationLoader(String... classes){
		for(String clazz : classes)
			addClassForApplicationLoader(clazz, Mode.NORMAL);
	}
	
	/**
	 * 
	 * @param clazz
	 * @param mode
	 */
	public void addClassForApplicationLoader(String clazz, Mode mode){
		if(mode == Mode.PREFIX || clazz.endsWith("."))
			classPrefixesForApplicationLoader.add(clazz);
		else
			classesForApplicationLoader.add(clazz);
	}
	
	/**
	 * 
	 * @param url
	 */
	public void addJarForApplicationLoader(URL url){
		jarsForApplicationLoader.add(url);
	}
	
	/**
	 * 
	 * @return
	 */
	public Collection<URL> getJarsForApplicationLoader() {
		return jarsForApplicationLoader;
	}
	
	/**
	 * 
	 * @return
	 */
	public Collection<String> getClassesForApplicationLoader() {
		return classesForApplicationLoader;
	}

	/**
	 * 
	 * @return
	 */
	public Collection<String> getClassPrefixesForApplicationLoader() {
		return classPrefixesForApplicationLoader;
	}
	
	/**
	 * 
	 * @param clazz
	 */
	public void addClassForSandboxLoader(String clazz){
		addClassForSandboxLoader(clazz, Mode.NORMAL);
	}
	
	/**
	 * 
	 * @param classes
	 */
	public void addClassForSandboxLoader(String... classes){
		for(String clazz : classes)
			addClassForSandboxLoader(clazz, Mode.NORMAL);
	}
	
	/**
	 * 
	 * @param clazz
	 * @param mode
	 */
	public void addClassForSandboxLoader(String clazz, Mode mode){
		if(mode == Mode.PREFIX || clazz.endsWith("."))
			classPrefixesForSandboxLoader.add(clazz);
		else
			classesForSandboxLoader.add(clazz);
	}
	
	/**
	 * 
	 * @return
	 */
	public Collection<String> getClassesForSandboxLoader() {
		return classesForSandboxLoader;
	}
	
	/**
	 * 
	 * @return
	 */
	public Collection<String> getClassPrefixesForSandboxLoader() {
		return classPrefixesForSandboxLoader;
	}
	
	
	/**
	 * Returns true if debug mode is enabled for this sandbox context.
	 * @return
	 */
	public boolean isDebug() {
		return debug;
	}
	
	/**
	 * Enables/Disables debug mode for this context
	 * 
	 * @param debug
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	/**
	 * Returns true if sandboxed code for this context is to be run on a remote agent.
	 * 
	 * @see #setRunRemote(boolean)
	 * @return
	 */
	public boolean isRunRemote() {
		return runRemote;
	}
	
	/**
	 * Enables/Disables that sandboxes are to be run on remote agents.
	 * @param runRemote
	 */
	public void setRunRemote(boolean runRemote) {
		this.runRemote = runRemote;
	}
	
	/**
	 * Returns true if sandboxed code is to be executed in its own thread.
	 * 
	 * @see #setRunInThread(boolean)
	 * @return
	 */
	public boolean isRunInThread() {
		return runInThread;
	}

	/**
	 * Enables/Disables execution of sandboxed code in an own thread.
	 * 
	 * @param runInThread
	 */
	public void setRunInThread(boolean runInThread) {
		this.runInThread = runInThread;
	}

	/**
	 * Returns the restriction on the number of items on the execution stack
	 * during sandboxed code execution.
	 * 
	 * @see #setMaximumStackDepth(int)
	 * @return
	 */
	public int getMaximumStackDepth() {
		return maximumStackDepth;
	}

	/**
	 * Sets a maximum stack depth for sandboxed code.
	 * 
	 * For this restriction to take effect the sandboxed code must be executed
	 * in its own thread. Note that the execution stack is probed only at
	 * discrete time steps and thus execution stacks that violate the
	 * here specified maximum might not be detected.
	 * 
	 * Set to -1 to disable execution stack probing.
	 * 
	 * @see #setRunInThread(boolean)
	 * @see SandboxService#setMonitorDaemonCheckInterval(long)
	 * @return
	 */
	public void setMaximumStackDepth(int maximumStackDepth) {
		this.maximumStackDepth = maximumStackDepth;
	}

	/**
	 * Returns the maximum runtime of sandboxed code.
	 * 
	 * @see #setMaximumRunTime(long)
	 * @see #setMaximumRuntimeMode(RuntimeMode)
	 * @see #setMaximumRunTimeUnit(TimeUnit)
	 * @return
	 */
	public long getMaximumRunTime() {
		return maximumRunTime;
	}

	/**
	 * Sets the maximum runtime of sandboxed code.
	 * 
	 * @see #setMaximumRuntimeMode(RuntimeMode)
	 * @see #setMaximumRunTimeUnit(TimeUnit)
	 * @param maximumRunTime
	 */
	public void setMaximumRunTime(long maximumRunTime) {
		this.maximumRunTime = maximumRunTime;
	}
	
	/**
	 * Sets the maximum runtime of sandboxed code
	 * 
	 * @see #setMaximumRuntimeMode(RuntimeMode)
	 * @see #setMaximumRunTimeUnit(TimeUnit)
	 * @param maximumRunTime
	 * @param unit
	 * @param mode
	 */
	public void setMaximumRunTime(long maximumRunTime, TimeUnit unit, RuntimeMode mode) {
		this.maximumRunTime = maximumRunTime;
		this.maximumRunTimeUnit = unit;
		this.maximumRuntimeMode = mode;
	}
	
	/**
	 * Returns the {@link TimeUnit} for the maximum runtime restriction.
	 *  
	 * @see #setMaximumRunTimeUnit(TimeUnit)
	 * @return
	 */
	public TimeUnit getMaximumRunTimeUnit() {
		return maximumRunTimeUnit;
	}
	
	/**
	 * Sets the {@link TimeUnit} for the runtime restriction of sandboxed code.
	 * The default is TimeUnit.MILLISECONDS
	 * 
	 * @see #setMaximumRunTime(long)
	 * @param maximumRunTimeUnit
	 */
	public void setMaximumRunTimeUnit(TimeUnit maximumRunTimeUnit) {
		this.maximumRunTimeUnit = maximumRunTimeUnit;
	}
	
	/**
	 * Returns the mode of measuring the runtime of sandboxed code.
	 * 
	 * @see #setMaximumRuntimeMode(RuntimeMode)
	 * @return
	 */
	public RuntimeMode getMaximumRuntimeMode() {
		return maximumRuntimeMode;
	}
	
	/**
	 * Sets the mode of measuring the runtime of sandboxed code.
	 * The default is ABSOLUTE_TIME. 
	 * 
	 * CPU_TIME denotes the actual time the thread was using the CPU,
	 * while ABSOLUTE_TIME denotes the time from thread start to 
	 * the current moment in time.
	 * 
	 * @param maximumRuntimeMode
	 */
	public void setMaximumRuntimeMode(RuntimeMode maximumRuntimeMode) {
		this.maximumRuntimeMode = maximumRuntimeMode;
	}

	/**
	 * @see #setPassAll(boolean)
	 * @return True if all access is permitted. 
	 */
	public boolean isPassAll() {
		return passAll;
	}

	/**
	 * Tells the class to allow any request.
	 * This is similar to the {@link AllPermission} for the basic java
	 * {@link SecurityManager}.
	 * 
	 * @param passAll true to allow any request
	 */
	public void setPassAll(boolean passAll) {
		this.passAll = passAll;
	}
	
	/**
	 *
	 * @see #setName(String)
	 * @return The context's name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name of the context. This name is for example used
	 * by the {@link SandboxLoader} to specifiy codesource (unless
	 * a specific codebase is set using {@link #setCodesource(String)}.
	 * 
	 * Also subloaders can be referred to by name.
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * 
	 * @see #setCodesource(String)
	 * @return The codesource used by {@link SandboxLoader} when loading classes in this context.
	 */
	public String getCodesource() {
		return codesource;
	}
	
	/**
	 * Sets the codesource that is to be used by {@link SandboxLoader} when loading classes in this context.
	 * 
	 * @param codesource
	 */
	public void setCodesource(String codesource) {
		this.codesource = codesource;
	}
	
	/**
	 * 
	 * @see #setRemoveFinalizers(boolean)
	 * @return
	 */
	public boolean isRemoveFinalizers() {
		return removeFinalizers;
	}
	
	/**
	 * Enable/Disable whether the {@link SandboxLoader} is to remove finalizers from loaded classes.
	 * 
	 * Note that this only works for classes not in java.*.
	 * 
	 * Defaults to true
	 * 
	 * @param removeFinalizers
	 */
	public void setRemoveFinalizers(boolean removeFinalizers) {
		this.removeFinalizers = removeFinalizers;
	}

	/**
	 * 
	 * @param type
	 * @param perm
	 */
	public void addSecurityPermission(AccessType type, SecurityPermission perm){
		switch (type) {
		case PERMIT:
			if(! permissionWhitelist.containsKey(perm.getType()))
				permissionWhitelist.put(perm.getType(), new ArrayList<SecurityPermission>());
			permissionWhitelist.get(perm.getType()).add(perm);
			break;

		case DENY:
			if(! permissionBlacklist.containsKey(perm.getType()))
				permissionBlacklist.put(perm.getType(), new ArrayList<SecurityPermission>());
			permissionBlacklist.get(perm.getType()).add(perm);
			break;
		}
	}
	
	/**
	 * 
	 * @param fileAccess
	 * @param type
	 * @param permission
	 */
	public void addFilePermission(FileAccess fileAccess, AccessType type, FilePermission permission){
		switch(fileAccess){
		case READ:
			if(AccessType.PERMIT == type)
				fileReadPermissions.add(permission);
			else
				fileReadDenials.add(permission);
			break;
		case WRITE:
			if(AccessType.PERMIT == type)
				fileWritePermissions.add(permission);
			else
				fileWriteDenials.add(permission);
			break;
		case DELETE:
			if(AccessType.PERMIT == type)
				fileDeletePermissions.add(permission);
			else
				fileDeleteDenials.add(permission);
			break;
		}
	}

	/**
	 * Returns true if class access is permitted with this configuration.
	 * 
	 * @param type
	 * @param stack
	 * @return
	 */
	public boolean checkClassAccess(Class type, Class[] stack) {
		return checkClassAccess(type.getName(), stack);
	}
	
	/**
	 * Returns true if class access is permitted with this configuration.
	 * 
	 * @param name
	 * @param stack
	 * @return
	 */
	public boolean checkClassAccess(String name, Class[] stack) {
		if(passAll)
			return true;
		
		boolean found = false;
		for(String wlClass : classPrefixWhitelist){
			if(name.startsWith(wlClass)){
				found = true;
				break;
			}
		}
		found |= classWhitelist.contains(name); 
		
		if(! found){
			for(ClassPermission wclass : complexClassWhitelist){
				if(name.startsWith(wclass.getName()) && checkEntriesAgainstStack(wclass.getEntries(), stack)){
					found = true;
					break;
				}
			}
		}
		
		if(found){
			for(String blClass : classPrefixBlacklist)
				if(name.startsWith(blClass))
					return false;
			if(classBlacklist.contains(name))
				return false;
		}
		
		return found;
	}

	/**
	 * Returns true if pacakge access is permitted with this configuration.
	 * 
	 * @see #checkPackageAccess(String, Class[])
	 * 
	 * @param type
	 * @param stack
	 * @return
	 */
	public boolean checkPackageAccess(Class type, Class[] stack) {
		return checkPackageAccess(type.getName(), stack);
	}

	/**
	 * Returns true if pacakge access is permitted with this configuration.
	 * 
	 * Note that package access does not necessarily mean that classes in the package
	 * may be accessed. For this either class level checks need to be bypassed or
	 * the class must also be accessible.
	 * 
	 * 
	 * @see #checkClassAccess(String, Class[])
	 * @see #bypassClassAccessChecks
	 * 
	 * @param name
	 * @param stack
	 * @return
	 */
	public boolean checkPackageAccess(String name, Class[] stack) {
		if(passAll)
			return true;
		
		boolean found = false;
		for(String wlPkg : packagePrefixWhitelist){
			if(name.startsWith(wlPkg)){
				found = true;
				break;
			}
		}
		found |= packageWhitelist.contains(name); 
		
		if(! found){
			for(PackagePermission wpkg : complexPackageWhitelist){
				if(name.startsWith(wpkg.getName()) && checkEntriesAgainstStack(wpkg.getEntries(), stack)){
					found = true;
					break;
				}
			}
		}
		
		if(found){
			for(String blPkg : packagePrefixBlacklist)
				if(name.startsWith(blPkg))
					return false;
			if(packageBlacklist.contains(name))
				return false;
		}
		
		return found;
	}

	/**
	 * Returns true if the permission is granted by this configuration.
	 * 
	 * @param perm
	 * @param stack
	 * @return
	 */
	public boolean checkPermission(Permission perm, Class[] stack) {
		/* check blacklist */
		Collection<SecurityPermission> blacklistedPermissions = permissionBlacklist.get(perm.getClass().getName());
		if(null != blacklistedPermissions){
			for(SecurityPermission blacklistedPermission : blacklistedPermissions){
				if(permissionMatches(blacklistedPermission, perm, stack))
					return false;
			}
		}
		
		/* specialized checks */
		if(java.io.FilePermission.class.equals(perm.getClass())){
			if(SecurityConstants.FILE_READ_ACTION.equals(perm.getActions()) && null != perm.getName()) {
				return checkFileReadAction(perm.getName());
			} else if(SecurityConstants.FILE_WRITE_ACTION.equals(perm.getActions()) && null != perm.getName()) {
				return checkFileWriteAction(perm.getName());
			} else if(SecurityConstants.FILE_DELETE_ACTION.equals(perm.getActions()) && null != perm.getName()) {
				return checkFileDeleteAction(perm.getName());
			}
		}
		
		/* general checks */
		Collection<SecurityPermission> whitelistedPermissions = permissionWhitelist.get(perm.getClass().getName());
		if(null != whitelistedPermissions){
			for(SecurityPermission whitelistedPermission : whitelistedPermissions){
				if(permissionMatches(whitelistedPermission, perm, stack))
					return true;
			}
		}
		
		return false;
	}
	
	protected boolean permissionMatches(SecurityPermission permission,
			Permission toBeMatched, Class[] stack) {
		if(null != permission.getName() && null == toBeMatched.getName())
			return false;
		if(null != permission.getName() && ! permission.getName().equals(toBeMatched.getName()))
			return false;
		if(null != permission.getActions() && ! permission.getActions().equals(toBeMatched.getActions()))
			return false;
		Collection<StackEntry> entries = permission.getEntries();
		if(null != entries && ! checkEntriesAgainstStack(entries, stack))
			return false;

		return true;
	}

	protected boolean checkFileReadAction(String name) {
		return checkFileAction(name, fileReadPermissions, fileReadDenials);
	}
	
	protected boolean checkFileWriteAction(String name) {
		return checkFileAction(name, fileWritePermissions, fileWriteDenials);
	}
	
	protected boolean checkFileDeleteAction(String name) {
		return checkFileAction(name, fileDeletePermissions, fileDeleteDenials);
	}
	
	protected boolean checkFileAction(String name, Collection<FilePermission> permissions, Collection<FilePermission> prohibition){
		if(passAll)
			return true;
		
		for(FilePermission perm : prohibition)
			if(perm.testPermission(name))
				return false;
		
		for(FilePermission perm : permissions)
			if(perm.testPermission(name))
				return true;
		
		return false;
	}
	
	protected boolean checkEntriesAgainstStack(Collection<StackEntry> entries, Class[] stack) {
		for(StackEntry entry :entries){
			int pos = entry.getPos();
			if(pos >= 0){
				if(stack.length <= pos)
					return false;
				if((entry.isPrefix() && ! stack[pos].getName().startsWith(entry.getType())) || (! entry.isPrefix() && ! entry.getType().equals(stack[pos].getName())))
					return false;
			} else {
				boolean found = false;
				String name = entry.getType();
				for(Class c : stack){
					if( (entry.isPrefix() && c.getName().startsWith(name)) || (! entry.isPrefix() && c.getName().equals(name))){
						found = true;
						break;
					}
				}
				if(! found)
					return false;
			}
		}
		
		return true;
	}

	protected void debug(Level level, String msg) {
		if(null == logger)
			logger = Logger.getLogger(getClass().getName());
		logger.log(level, name + "(" + System.identityHashCode(this) +") : " + msg);
	}
	
	/**
	 * Called by the {@link SandboxSecurityManager} when a permission is checked and
	 * the context is in debug mode.
	 * 
	 * @see #setDebug(boolean)
	 * @param perm
	 */
	public void debugPermissionCheck(Permission perm) {
		debug(Level.INFO, "PermissionCheck: " + perm + "\n");
	}

	/**
	 * Called by the {@link SandboxSecurityManager} when a permission was denied and
	 * the context is in debug mode.
	 *  
	 * @see #setDebug(boolean)
	 * @param perm
	 * @param classes
	 */
	public void debugDeniedPermission(Permission perm, Class[] classes) {
		StringBuffer b = new StringBuffer();
		b.append("DENY: PermissionCheck: " + perm + "\n");
		appendClasses(b, classes);
		debug(Level.WARNING, b.toString());
	}

	protected void appendClasses(StringBuffer b, Class[] classes) {
		int i = 0;
		if(null != classes)
			for(Class c : classes)
				b.append("\t" + i++ + "\t: " + c + "\n");		
	}

	/**
	 * Called by the {@link SandboxSecurityManager} when package access is checked and
	 * the context is in debug mode.
	 * 
	 * @see #setDebug(boolean)
	 * @param pkg
	 */
	public void debugCheckPackageAccess(String pkg) {
		debug(Level.INFO, "PackageAccessCheck: " + pkg + "\n");		
	}
	
	/**
	 * Called by the {@link SandboxSecurityManager} when package access was denied and
	 * the context is in debug mode.
	 * 
	 * @see #setDebug(boolean)
	 * @param pkg
	 * @param classes
	 */
	public void debugDeniedPackageAccess(String pkg, Class[] classes) {
		StringBuffer b = new StringBuffer();
		b.append("DENY: PackageAccessCheck: " + pkg + "\n");
		appendClasses(b, classes);
		debug(Level.WARNING, b.toString());
	}

	/**
	 * Called by the {@link SandboxSecurityManager} when class access is checked and
	 * the context is in debug mode.
	 * 
	 * @see #setDebug(boolean)
	 * @param clazz
	 */
	public void debugCheckClassAccess(String clazz) {
		debug(Level.INFO, "ClassAccessCheck: " + clazz + "\n");
	}

	/**
	 * Called by the {@link SandboxSecurityManager} when class access was denied and
	 * the context is in debug mode.
	 * 
	 * @see #setDebug(boolean)
	 * @param clazz
	 * @param classes
	 */
	public void debugDeniedClassAccess(String clazz, Class[] classes) {
		StringBuffer b = new StringBuffer();
		b.append("DENY: ClassAccessCheck: " + clazz + "\n");
		appendClasses(b, classes);
		debug(Level.WARNING, b.toString());
	}
	
	/**
	 * Convenience method to allow read access to anything on the classpath (java.class.path).
	 */
	public void addClasspath() {
		for(String s : System.getProperty("java.class.path").split(":")){
			if(s.endsWith(".jar"))
				addFilePermission(FileAccess.READ, AccessType.PERMIT, new FileEqualsPermission(s));
			else if(s.endsWith(".class"))
				addFilePermission(FileAccess.READ, AccessType.PERMIT,new FileEqualsPermission(s));
			else
				addFilePermission(FileAccess.READ, AccessType.PERMIT,new FilePrefixPermission(s));
		}
	}
	
	/**
	 * Convenience method to allow read access to the home dir (java.home).
	 */
	public void addHome(){
		String home = System.getProperty("java.home");
		addFilePermission(FileAccess.READ, AccessType.PERMIT,new FilePrefixPermission(home));
	}

	/**
	 * Adds the {@link SandboxLoader}'s default Codesource prefix to be read accessible for
	 * sandboxed code.
	 * 
	 * @see SandboxLoader#DEFAULT_CODESOURCE_PREFIX
	 */
	public void addDefaultCodesourcePrefix(){
		addFilePermission(FileAccess.READ, AccessType.PERMIT,new FilePrefixPermission(SandboxLoader.DEFAULT_CODESOURCE_PREFIX));
	}

	
	/**
	 * Convenience method to allow read and write access to the tmpdir (java.io.tmpdir).
	 */
	public void addTempDir() {
		String dir = System.getProperty("java.io.tmpdir");
		addFilePermission(FileAccess.READ, AccessType.PERMIT,new FilePrefixPermission(dir));
		addFilePermission(FileAccess.WRITE, AccessType.PERMIT,new FilePrefixPermission(dir));
		addFilePermission(FileAccess.DELETE, AccessType.PERMIT,new FilePrefixPermission(dir));
	}

	/**
	 * Convenience method to allow read access to the user's home dir (user.dir).
	 */
	public void addWorkDir() {
		String dir = System.getProperty("user.dir");
		addFilePermission(FileAccess.READ, AccessType.PERMIT, new FilePrefixPermission(dir));
	}

	/**
	 * Returns true if {@link SecurityPermission} are not checked.
	 * 
	 * @see #setBypassPermissionAccessChecks(boolean)
	 * @return
	 */
	public boolean isBypassPermissionAccessChecks() {
		return bypassPermissionAccessChecks;
	}
	
	/**
	 * Enables/Disables the checking of {@link SecurityPermission}.
	 * Default is to check {@link SecurityPermission}s.
	 * 
	 * @param bypassPermissionAccess
	 */
	public void setBypassPermissionAccessChecks(boolean bypassPermissionAccess) {
		this.bypassPermissionAccessChecks = bypassPermissionAccess;
	}
	
	/**
	 * Returns true if checking of package access is disabled.
	 * 
	 * @see #setBypassPackageAccessChecks(boolean)
	 * @return
	 */
	public boolean isBypassPackageAccessChecks() {
		return bypassPackageAccessChecks;
	}
	
	/**
	 * Enables/Disables package access checks.
	 * Default is to not check package access.
	 * 
	 * @param bypassPackageAccess
	 */
	public void setBypassPackageAccessChecks(boolean bypassPackageAccess) {
		this.bypassPackageAccessChecks = bypassPackageAccess;
	}

	/**
	 * Returns true if class access checks are disabled.
	 * 
	 * @see #setBypassClassAccessChecks(boolean)
	 * @return
	 */
	public boolean isBypassClassAccessChecks() {
		return bypassClassAccessChecks;
	}
	
	/**
	 * Enables/Disables class access checks.
	 * Default is to check class access.
	 * 
	 * @param bypassClassAccess
	 */
	public void setBypassClassAccessChecks(boolean bypassClassAccess) {
		this.bypassClassAccessChecks = bypassClassAccess;
	}
	
	protected void mergeClassRestrictions(SandboxContext set){
		this.classBlacklist.addAll(set.classBlacklist);
		this.classPrefixBlacklist.addAll(set.classPrefixBlacklist);
		this.classWhitelist.addAll(set.classWhitelist);
		this.classPrefixWhitelist.addAll(set.classPrefixWhitelist);

		for(ClassPermission wp : set.complexClassWhitelist)
			this.complexClassWhitelist.add(wp.clone());
	}
	
	protected void mergePackageRestrictions(SandboxContext set){
		this.packageBlacklist.addAll(set.packageBlacklist);
		this.packagePrefixBlacklist.addAll(set.packagePrefixBlacklist);
		this.packageWhitelist.addAll(set.packageWhitelist);
		this.packagePrefixWhitelist.addAll(set.packagePrefixWhitelist);
	
		for(PackagePermission wp : set.complexPackageWhitelist)
			this.complexPackageWhitelist.add(wp.clone());
	}

	protected void mergePermissions(SandboxContext restrictSet) {
		for(String key : restrictSet.permissionWhitelist.keySet()){
			if(! permissionWhitelist.containsKey(key))
				permissionWhitelist.put(key, new ArrayList<SecurityPermission>());

			for(SecurityPermission perm : restrictSet.permissionWhitelist.get(key))
				permissionWhitelist.get(key).add(perm.clone());
		}

		for(String key : restrictSet.permissionBlacklist.keySet()){
			if(! permissionBlacklist.containsKey(key))
				permissionBlacklist.put(key, new ArrayList<SecurityPermission>());

			for(SecurityPermission perm : restrictSet.permissionBlacklist.get(key))
				permissionBlacklist.get(key).add(perm.clone());
		}
	}
	
	protected void mergeFilePermissions(SandboxContext restrictSet) {
		for(FilePermission perm : restrictSet.fileReadDenials)
			fileReadDenials.add(perm.clone());
		
		for(FilePermission perm : restrictSet.fileReadPermissions)
			fileReadPermissions.add(perm.clone());
		
		for(FilePermission perm : restrictSet.fileWriteDenials)
			fileWriteDenials.add(perm.clone());

		for(FilePermission perm : restrictSet.fileWritePermissions)
			fileWritePermissions.add(perm.clone());
		
		for(FilePermission perm : restrictSet.fileDeletePermissions)
			fileDeletePermissions.add(perm.clone());
		
		for(FilePermission perm : restrictSet.fileDeleteDenials)
			fileDeleteDenials.add(perm.clone());
	}
	
	/**
	 * Merges the configuration for the application loader.
	 * 
	 * @see #addClassForApplicationLoader(String)
	 * @param set
	 */
	protected void mergeApplicationLoaderConfiguration(SandboxContext context) throws MalformedURLException {
		this.classesForApplicationLoader.addAll(context.classesForApplicationLoader);
		this.classPrefixesForApplicationLoader.addAll(context.classPrefixesForApplicationLoader);
		
		for(URL url : context.jarsForApplicationLoader)
			this.jarsForApplicationLoader.add(new URL(url.toExternalForm()));
		
		this.classesForSandboxLoader.addAll(context.classesForSandboxLoader);
		this.classPrefixesForSandboxLoader.addAll(context.classPrefixesForSandboxLoader);
	}
	
	protected void mergeWhitelistedJars(SandboxContext context) throws MalformedURLException {
		for(URL url : context.whitelistedJars)
			this.whitelistedJars.add(new URL(url.toExternalForm()));
	}
	
	protected void mergeSubloaders(SandboxContext context){
		Map<SandboxContext,SandboxContext> contextCache = new IdentityHashMap<SandboxContext, SandboxContext>();
		
		for(String prefix : context.subLoaderContextByClassPrefix.keySet()){
			SandboxContext subcontext = context.subLoaderContextByClassPrefix.get(prefix);
			if(! contextCache.containsKey(subcontext))
				contextCache.put(subcontext, subcontext.clone());
			
			this.subLoaderContextByClassPrefix.put(prefix, contextCache.get(subcontext));
		}
		
		for(String clazz : context.subLoaderContextByClass.keySet()){
			SandboxContext subcontext = context.subLoaderContextByClass.get(clazz);
			if(! contextCache.containsKey(subcontext))
				contextCache.put(subcontext, subcontext.clone());
			
			this.subLoaderContextByClass.put(clazz, contextCache.get(subcontext));
		}
		
		for(URL url : context.subLoaderContextByJar.keySet()){
			SandboxContext subcontext = context.subLoaderContextByJar.get(url);
			if(! contextCache.containsKey(subcontext))
				contextCache.put(subcontext, subcontext.clone());
			
			this.subLoaderContextByJar.put(url, contextCache.get(subcontext));
		}
	}
	
	public void merge(SandboxContext context) {
		mergeClassRestrictions(context);
		mergePackageRestrictions(context);
		mergeFilePermissions(context);
		mergePermissions(context);
		try{
			mergeApplicationLoaderConfiguration(context);
			mergeWhitelistedJars(context);
		} catch(MalformedURLException e){
			throw new IllegalStateException(e);
		}
		
		debug = context.debug;
		passAll = context.passAll;
		name = context.name;
		codesource = context.codesource;
		bypassClassAccessChecks = context.bypassClassAccessChecks;
		bypassPackageAccessChecks = context.bypassPackageAccessChecks;
		bypassPermissionAccessChecks = context.bypassPermissionAccessChecks;
		
		removeFinalizers = context.removeFinalizers;
		loaderEnhancer = context.loaderEnhancer;
		
		runRemote = context.runRemote;
		runInThread = context.runInThread;
		maximumRunTime = context.maximumRunTime;
		maximumRunTimeUnit = context.maximumRunTimeUnit;
		maximumRuntimeMode = context.maximumRuntimeMode;
		maximumStackDepth = context.maximumStackDepth;
	}

	@Override
	public SandboxContext clone() {
		SandboxContext clone = new SandboxContext();
		
		clone.merge(this);
		
		return clone;
	}
	
}
