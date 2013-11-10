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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import net.datenwerke.sandbox.securitypermissions.SandboxRuntimePermission;

import org.apache.commons.io.IOUtils;

import sun.misc.Resource;
import sun.misc.URLClassPath;


/**
 * The {@link ClassLoader} used by the java-sandbox library.
 * This classloader takes care of loading classes with the correct class loaders
 * and much more. This can be seen to some extend as the heart of the
 * java-sandbox library. 
 * 
 * 
 * @author Arno Mittelbach
 *
 */
final public class SandboxLoader extends ClassLoader {
	
	public static final String DEFAULT_CODESOURCE_PREFIX = "/java-sandbox-default-codesource";

	private final Logger logger = Logger.getLogger(getClass().getName());
	
	private static HashSet<String> BYPASSCLASSES;
	static {
		BYPASSCLASSES = new HashSet<String>();

		BYPASSCLASSES.add("net.datenwerke.sandbox.SandboxLoader");
		BYPASSCLASSES.add("net.datenwerke.sandbox.SandboxServiceImpl");
			
		BYPASSCLASSES.add("net.datenwerke.sandbox.SandboxedCallResult");
		BYPASSCLASSES.add("net.datenwerke.sandbox.SandboxedCallResultImpl");
		BYPASSCLASSES.add("net.datenwerke.sandbox.SandboxService");
		BYPASSCLASSES.add("net.datenwerke.sandbox.SandboxContext");
		
		BYPASSCLASSES.add("net.datenwerke.sandbox.handlers.SandboxHandler");
		BYPASSCLASSES.add("net.datenwerke.sandbox.handlers.BadThreadKillHandler");
		BYPASSCLASSES.add("net.datenwerke.sandbox.handlers.ContextRegisteredHandler");
		
		BYPASSCLASSES.add("net.datenwerke.sandbox.permissions.FileEqualsPermission");
		BYPASSCLASSES.add("net.datenwerke.sandbox.permissions.FilePermission");
		BYPASSCLASSES.add("net.datenwerke.sandbox.permissions.FilePrefixPermission");
		BYPASSCLASSES.add("net.datenwerke.sandbox.permissions.FileRegexPermission");
		BYPASSCLASSES.add("net.datenwerke.sandbox.permissions.FileSuffixPermission");
		BYPASSCLASSES.add("net.datenwerke.sandbox.permissions.StackEntry");
		BYPASSCLASSES.add("net.datenwerke.sandbox.permissions.ClassPermission");
		BYPASSCLASSES.add("net.datenwerke.sandbox.permissions.PackagePermission");
		BYPASSCLASSES.add("net.datenwerke.sandbox.permissions.SecurityPermission");

		BYPASSCLASSES.add("net.datenwerke.sandbox.util.VariableAssignment");
	}
	
	private final SandboxSecurityManager securityManager;

	private Map<String, SandboxLoader> subLoaderCache = new HashMap<String, SandboxLoader>();
	private Map<String, SandboxLoader> subLoaderPrefixCache = new HashMap<String, SandboxLoader>();
	private Map<URLClassPath, SandboxLoader> subLoaderByJar  = new HashMap<URLClassPath, SandboxLoader>();
	
	private boolean debug = false;
	
	private SandboxContext context = new SandboxContext();

	private String name = "";
	
	private URLClassPath whitelistedUcp;
	private URLClassPath bypassUcp;
	
	private Collection<String> classesToLoadWithParent = new HashSet<String>();
	private Collection<String> classesByPrefixToLoadWithParent = new HashSet<String>();
	
	private Collection<String> classesToLoadDirectly = new HashSet<String>();
	private Collection<String> classesByPrefixToLoadDirectly = new HashSet<String>();
	
	private boolean hasSubloaders;

	private String codesource;

	private boolean removeFinalizers;
	
	private SandboxLoaderEnhancer enhancer;
	
	private ClassLoader parent;

	/**
	 * Instantiates a new SandboxLoader with the current ClassLoader as parent.
	 */
	public SandboxLoader() {
		this(SandboxService.class.getClassLoader());
	}

	/**
	 * Instantiates a new SandboxLoader with the given ClassLoader as parent.
	 * @param parent
	 */
	public SandboxLoader(ClassLoader parent) {
		this(parent, System.getSecurityManager());
	}
	
	/**
	 * Instantiates a new SandboxLoader with the given ClassLoader as parent and using the given security manager.
	 * @param parent
	 * @param securityManager
	 */
	public SandboxLoader(ClassLoader parent, SecurityManager securityManager) {
		super(parent);
		this.parent = parent;
		this.securityManager = (SandboxSecurityManager) securityManager;
	}
	
	/**
	 * Initializes this classloader with the config provided by the SandboxContext
	 * 
	 * @param context
	 */
	public void init(SandboxContext context) {
		securityManager.checkPermission(new SandboxRuntimePermission("initSandboxLoader"));
		
		/* name */
		this.name = context.getName();
		
		/* jars */
		if(null != context.getWhitelistedJars() && ! context.getWhitelistedJars().isEmpty())
			whitelistedUcp = new URLClassPath(context.getWhitelistedJars().toArray(new URL[]{}));
		else 
			whitelistedUcp = null;
		
		if(null != context.getJarsForApplicationLoader() && ! context.getJarsForApplicationLoader().isEmpty())
			bypassUcp = new URLClassPath(context.getJarsForApplicationLoader().toArray(new URL[]{}));
		else 
			bypassUcp = null;
		
		/* load configuration */
		classesToLoadWithParent = new HashSet<String>(context.getClassesForApplicationLoader());
		classesToLoadWithParent.addAll(BYPASSCLASSES);
		classesByPrefixToLoadWithParent = new HashSet<String>(context.getClassPrefixesForApplicationLoader());
		
		classesToLoadDirectly = new HashSet<String>(context.getClassesForSandboxLoader());
		classesByPrefixToLoadDirectly = new HashSet<String>(context.getClassPrefixesForSandboxLoader());
		
		/* subloaders */
		this.hasSubloaders = ! context.getSubLoaderContextByClassMap().isEmpty() || 
							 ! context.getSubLoaderContextByClassPrefixMap().isEmpty() ||
							 ! context.getSubLoaderContextByJar().isEmpty();
		if(hasSubloaders){
			IdentityHashMap<SandboxContext, SandboxLoader> loaderMap = new IdentityHashMap<SandboxContext, SandboxLoader>();
			
			for(Entry<String, SandboxContext> e :context.getSubLoaderContextByClassMap().entrySet())
				subLoaderCache.put(e.getKey(), initSubLoader(loaderMap, e.getValue()));
			for(Entry<String, SandboxContext> e : context.getSubLoaderContextByClassPrefixMap().entrySet())
				subLoaderPrefixCache.put(e.getKey(), initSubLoader(loaderMap, e.getValue()));
			for(Entry<URL, SandboxContext> e : context.getSubLoaderContextByJar().entrySet())
				subLoaderByJar.put(new URLClassPath(new URL[]{e.getKey()}), initSubLoader(loaderMap, e.getValue()));
		}
		
		/* debug */
		this.debug = context.isDebug();
		
		this.codesource = context.getCodesource();
		if(null == this.codesource)
			this.codesource = DEFAULT_CODESOURCE_PREFIX.concat("/").concat(null == name || "".equals(name) ? "default" : name).concat("/");
		
		this.removeFinalizers = context.isRemoveFinalizers();
		
		this.enhancer = context.getLoaderEnhancer();
		
		/* store context */
		this.context = context;
	}
	
	private SandboxLoader initSubLoader(IdentityHashMap<SandboxContext, SandboxLoader> loaderMap, SandboxContext context) {
		if(loaderMap.containsKey(context))
			return loaderMap.get(context);
		
		SandboxLoader subLoader =  new SandboxLoader(this, securityManager);
		subLoader.init(context);
		
		loaderMap.put(context, subLoader);
		
		return subLoader;
	}

	/**
	 * Returns the context used to initialize this {@link SandboxLoader}
	 * 
	 * @see #init(SandboxContext)
	 * @return
	 */
	public SandboxContext getContext() {
		getSecurityManager().checkPermission(new SandboxRuntimePermission("getSandboxLoaderContext"));
		
		return context;
	}
	
	@Override
	protected Class<?> loadClass(final String name, boolean resolve) throws ClassNotFoundException {
		Class clazz = null;

		if(debug)
			logger.log(Level.INFO, getName() + "(" + System.identityHashCode(this) + ")" + " about to load class: " + name);
		
		if(null != enhancer)
			enhancer.classtoBeLoaded(this, name, resolve);
		
		boolean trustedSource = false;
		
	    if(name.startsWith("java.") || bypassClazz(name) ) {
	    	clazz = super.loadClass(name, resolve);
	    	
	    	/* check if it comes from an available jar */
			if(! name.startsWith("java.") && null != whitelistedUcp){
				String path = name.replace('.', '/').concat(".class");
				
				Resource res = whitelistedUcp.getResource(path, false);
				if (res != null) 
					trustedSource = true;
			}
	    	
	    } else {
			/* check subcontext */
			if(hasSubloaders){
				SandboxLoader subLoader = doGetSubLoaderByClassContext(name);
				if(null != subLoader)
					return subLoader.loadClass(name, resolve);
			}
	    	
	    	/* check if we have already handeled this class */
			clazz = findLoadedClass(name);
			if( clazz != null ){
				if(null != whitelistedUcp){
					String path = name.replace('.', '/').concat(".class");
					Resource res = whitelistedUcp.getResource(path, false);
					if (res != null) 
						trustedSource = true;
				}
			} else {
				try {
					String basePath = name.replace('.', '/');
					String path = basePath.concat(".class");
					
					ProtectionDomain domain = null;
					try {
						CodeSource codeSource = new CodeSource(new URL("file", "", codesource.concat(basePath)), (java.security.cert.Certificate[]) null);
						domain = new ProtectionDomain(codeSource, new Permissions(), this, null);
			        }
			        catch (MalformedURLException e) {
			            throw new RuntimeException("Could not create protection domain.");
			        }
					
					/* define package */
					int i = name.lastIndexOf('.');
					if (i != -1) {
						String pkgName = name.substring(0, i);
						java.lang.Package pkg = getPackage(pkgName);
						if (pkg == null) {
							definePackage(pkgName, null, null, null, null, null, null, null);
						}
					}
					
					
					/* first strategy .. check jars */
					if(null != whitelistedUcp){
						Resource res = whitelistedUcp.getResource(path, false);
						if (res != null) {
							byte[] cBytes = enhance(name, res.getBytes());
							clazz = defineClass(name, cBytes, 0, cBytes.length, domain);
							trustedSource = true;
						}
					}
					
					/* load class */
					if( clazz == null ){
						InputStream in = null;
						try{
							/* we only load from local sources */
							in = parent.getResourceAsStream(path);
							byte[] cBytes = null;
							if( in != null )
								 cBytes = IOUtils.toByteArray(in);	
								
							if(null == cBytes && null != enhancer)
								cBytes = enhancer.loadClass(this, name);
							if(null == cBytes)
								throw new ClassNotFoundException("Could not find " + name);
							
							/* load and define class */
							cBytes = enhance(name, cBytes);
							clazz = defineClass(name, cBytes, 0, cBytes.length, domain);
						} finally {
							if(null != in) {
								try {
									in.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
					}
							
					/* do we need to resolve */
					if( resolve ) 
						resolveClass(clazz);
				} catch (IOException e) {
					throw new ClassNotFoundException("Could not load "+name, e);
				} catch (Exception e) {
					throw new ClassNotFoundException("Could not load "+name, e);
				}
			} 
	    }
	    
	    if(! trustedSource && null != clazz && null != securityManager)
	    	securityManager.checkClassAccess(name);
	    
	    if(null != enhancer)
	    	enhancer.classLoaded(this, name, clazz);
	    
	    return clazz;
	}


	private byte[] enhance(String name, byte[] cBytes) throws IOException, RuntimeException, CannotCompileException, NotFoundException {
		if(removeFinalizers){
			CtClass clazz = new ClassPool().makeClass(new ByteArrayInputStream(cBytes));
			if(! clazz.isInterface()) {
				try{
					CtMethod method = clazz.getMethod("finalize", "()V");
					if(null != method && ! method.isEmpty()){
						clazz.removeMethod(method);
						cBytes = clazz.toBytecode();	
					}
				} catch(NotFoundException ignore){}
			}
		}
		if(null != enhancer)
			cBytes = enhancer.enhance(this, name, cBytes);
		
		return cBytes;
		
	}

	public SandboxLoader getSubLoaderByClassContext(String clazz){
		getSecurityManager().checkPermission(new SandboxRuntimePermission("getSubLoader"));
		
		return doGetSubLoaderByClassContext(clazz);
	}
	
	private SandboxLoader doGetSubLoaderByClassContext(String clazz) {
		String path = clazz.replace('.', '/').concat(".class");
		for(Entry<URLClassPath, SandboxLoader> e : subLoaderByJar.entrySet()){
			Resource res = e.getKey().getResource(path, false);
			if (res != null)
				return e.getValue();
		}
		
		SandboxLoader subLoader = subLoaderCache.get(clazz);
		if(null != subLoader)
			return subLoader;
		for(String prefix : subLoaderPrefixCache.keySet())
			if(clazz.startsWith(prefix))
				return subLoaderPrefixCache.get(prefix);
		
		
		return null;
	}

	private boolean bypassClazz(String name) {
		if(null != enhancer && enhancer.isLoadClassWithApplicationLoader(name))
			return true;
		
		if(classesToLoadWithParent.contains(name))
			return ! prohibitBypass(name);
		for(String bp : classesByPrefixToLoadWithParent)
			if(name.startsWith(bp))
				return ! prohibitBypass(name);
		
		/* check if it comes from an available jar */
		if(! name.startsWith("java.") && null != bypassUcp){
			String path = name.replace('.', '/').concat(".class");
			
			Resource res = bypassUcp.getResource(path, false);
			if (res != null) 
				return ! prohibitBypass(name);
		}
		
		return false;
	}

	private boolean prohibitBypass(String name) {
		if(classesToLoadDirectly.contains(name))
			return true;
		for(String bp : classesByPrefixToLoadDirectly)
			if(name.startsWith(bp))
				return true;
	
		return false;
	}
	

	public SandboxLoader getSubloaderByName(String name){
		getSecurityManager().checkPermission(new SandboxRuntimePermission("getSubLoader"));
		
		return doGetSubloaderByName(name);
	}

	private SandboxLoader doGetSubloaderByName(String name){
		if(name.equals(getName()))
			return this;
		
		for(SandboxLoader loader : subLoaderByJar.values())
			if(name.equals(loader.getName()))
				return loader;
		for(SandboxLoader loader : subLoaderCache.values())
			if(name.equals(loader.getName()))
				return loader;
		for(SandboxLoader loader : subLoaderPrefixCache.values())
			if(name.equals(loader.getName()))
				return loader;
		
		for(SandboxLoader loader : subLoaderByJar.values()){
			SandboxLoader subLoader = loader.getSubloaderByName(name);
			if(null != subLoader)
				return subLoader;
		}
		
		for(SandboxLoader loader : subLoaderCache.values()){
			SandboxLoader subLoader = loader.getSubloaderByName(name);
			if(null != subLoader)
				return subLoader;
		}
		
		for(SandboxLoader loader : subLoaderPrefixCache.values()){
			SandboxLoader subLoader = loader.getSubloaderByName(name);
			if(null != subLoader)
				return subLoader;
		}
		
		return null;
	}
	
	/**
	 * Returns the name of the ClassLoader.
	 * 
	 * @return
	 */
	public String getName(){
		return name;
	}
	
	public SandboxSecurityManager getSecurityManager() {
		securityManager.checkPermission(new SandboxRuntimePermission("getSecurityManager"));
		
		return securityManager;
	}
	
	public Class<?> defineClass(String name, byte[] classBytes) {
		return defineClass(name, classBytes, true);
	}
	
	public Class<?> defineClass(String name, byte[] classBytes, boolean enhanceClass) {
		securityManager.checkPermission(new SandboxRuntimePermission("defineClass"));
		
		Class<?> clazz = findLoadedClass(name);
		if(null != clazz)
			return clazz;
		
		if(enhanceClass){
			try {
				classBytes = enhance(name, classBytes);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		ProtectionDomain domain = null;
		try {
			CodeSource codeSource = new CodeSource(new URL("file", "", codesource), (java.security.cert.Certificate[]) null);
			domain = new ProtectionDomain(codeSource, new Permissions(), this, null);
        }
        catch (MalformedURLException e) {
            throw new RuntimeException("Could not create protection domain.");
        }
		
		return defineClass(name, classBytes, 0, classBytes.length, domain);
	}
		
	
}