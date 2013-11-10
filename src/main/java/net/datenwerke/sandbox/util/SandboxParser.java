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

package net.datenwerke.sandbox.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import net.datenwerke.sandbox.SandboxContext;
import net.datenwerke.sandbox.SandboxContext.AccessType;
import net.datenwerke.sandbox.SandboxContext.FileAccess;
import net.datenwerke.sandbox.SandboxContext.Mode;
import net.datenwerke.sandbox.SandboxContext.RuntimeMode;
import net.datenwerke.sandbox.SandboxService;
import net.datenwerke.sandbox.jvm.JvmInstantiatorImpl;
import net.datenwerke.sandbox.jvm.JvmPoolConfigImpl;
import net.datenwerke.sandbox.jvm.JvmPoolImpl;
import net.datenwerke.sandbox.permissions.ClassPermission;
import net.datenwerke.sandbox.permissions.FileEqualsPermission;
import net.datenwerke.sandbox.permissions.FilePrefixPermission;
import net.datenwerke.sandbox.permissions.FileRegexPermission;
import net.datenwerke.sandbox.permissions.FileSuffixPermission;
import net.datenwerke.sandbox.permissions.PackagePermission;
import net.datenwerke.sandbox.permissions.SecurityPermission;
import net.datenwerke.sandbox.permissions.StackEntry;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;

/**
 * A helper class to configure a {@link SandboxContext} using an XML file.
 * 
 * The class makes use of the apache-commons {@link Configuration} framework
 * expecting a {@link HierarchicalConfiguration} object as input.
 * 
 * <h2>Here a sample configuration</h2>
 * <pre>
 * {@code
 * <?xml version="1.0" encoding="UTF-8"?>
 * <configuration>
 *   <security>
 *     <properties>
 *       <remote enable="false" configureService="false">
 *         <jvm poolsize="2" freelancersize"2">
 *          <vmargs></vmargs>
 *          <rmi minport="10000" maxport="10200" />
 *         </jvm>
 *       </remote>
 *       <monitor checkinterval="10" watchdogCheckinterval="10000"/>
 *       <codesourceSecurity enable="false"/>
 *     </properties>
 *     <sandbox name="BASE" allowAll="false" debug="false" bypassPackageAccess="true">
 *       <applicationLoader>
 *         <classPrefix>sun.</classPrefix>
 *         <classPrefix>javax.script.</classPrefix>
 *         <classPrefix>javax.el.</classPrefix>
 *         <classPrefix>javax.xml.</classPrefix>
 *       </applicationLoader>
 *       <classes>
 *         <whitelist>
 *           <entry>java.lang.</entry>
 *           <entry>java.util.</entry>
 *           <entry>java.io.</entry>
 *           <entry>java.sql.Connection</entry>
 *         </whitelist>
 *       </classes>
 *       <fileAccess home="true" work="false" classpath="true" temp="false" />
 *     </sandbox>
 *      <sandbox name="JUEL" allowAll="true" debug="true" bypassPackageAccess="true" basedOn="BASE">
 *       <applicationLoader>
 *        <jar>file:/PATH-TO-JUEL/juel-impl-2.2.4.jar</jar>
 * 	   <jar>file:/PATH-TO-JUEL/juel-api-2.2.4.jar</jar>
 *       </applicationLoader>
 *     <classes>
 *       <whitelist>
 *         <jar>file:/PATH-TO-JUEL/juel-impl-2.2.4.jar</jar>
 *  	    <jar>file:/PATH-TO-JUEL/juel-api-2.2.4.jar</jar>
 *       </whitelist>
 *     </classes>
 *     <permission>
 *       <whitelist>
 *         <entry>
 *           <type>java.lang.RuntimePermission</type>
 *           <name>accessDeclaredMembers</name>
 *         </entry>
 *         <entry>
 *           <type>java.lang.RuntimePermission</type>
 *           <name>reflectionFactoryAccess</name>
 *         </entry>       
 *         <entry>
 *           <type>java.lang.RuntimePermission</type>
 *           <name>createClassLoader</name>
 * 		  <stack>
 *             <check type="net.datenwerke.transloader.load.CollectedClassLoader" pos="4"/>
 *           </stack>          
 *         </entry> 
 *         <entry>
 *           <type>java.lang.RuntimePermission</type>
 *           <name>createClassLoader</name>
 * 		  <stack>
 *             <check type="net.datenwerke.transloader.load.BootClassLoader" pos="4"/>
 *           </stack>          
 *         </entry>
 *         <entry>
 *           <type>java.lang.RuntimePermission</type>
 *           <name>createClassLoader</name>
 * 		  <stack>
 *             <check type="javax.el.ExpressionFactory" pos="14"/>
 *           </stack>          
 *         </entry>
 * 	    <entry>
 *           <type>java.lang.RuntimePermission</type>
 *           <name>createClassLoader</name>
 * 		  <stack>
 *             <check type="javax.el.ExpressionFactory" pos="17"/>
 *           </stack>          
 *         </entry>
 * 	    <entry>
 *           <type>java.lang.RuntimePermission</type>
 *           <name>createClassLoader</name>
 * 		  <stack>
 *             <check type="net.datenwerke.transloader.clone.reflect.instantiate.ObjenesisInstantiationStrategy" pos="-1"/>
 *           </stack>          
 *         </entry>        
 * 	    <entry>
 *           <type>java.lang.reflect.ReflectPermission</type>
 *           <name>suppressAccessChecks</name>
 *         </entry>  
 * 	    <entry>
 *           <type>java.net.NetPermission</type>
 *           <name>specifyStreamHandler</name>
 *         </entry>          
 *         <entry>
 *           <type>java.util.PropertyPermission</type>
 *           <actions>read</actions>
 *         </entry> 
 *       </whitelist>
 *     </permission>
 *   </sandbox>
 * </security>
 * </configuration>
 * }
 * </pre>
 * 
 * @see HierarchicalConfiguration
 * @author Arno Mittelbach
 *
 */
public class SandboxParser {

	private Map<String, SandboxContext> restrictionSets = new HashMap<String, SandboxContext>();
	
	public void configureSandboxService(SandboxService sandboxService, Configuration config){
		if(! (config instanceof HierarchicalConfiguration))
			throw new IllegalArgumentException("Expected HierarchicalConfiguration format");
		
		HierarchicalConfiguration conf = (HierarchicalConfiguration) config;
		
		/* general properties */
		SubnodeConfiguration properties = null;
		try{
			properties = conf.configurationAt("security.properties");
		} catch(IllegalArgumentException e){
		}
		if(null != properties){
			/* remote */
			boolean remoteConfig = properties.getBoolean("remote.configureService", false);
			if(remoteConfig){
				boolean remote = properties.getBoolean("remote[@enable]", false);
				if(remote){
					Integer poolsize = properties.getInteger("remote.jvm[@poolsize]", 2);
					Integer freelancersize = properties.getInteger("remote.jvm[@freelancersize]", 2);
					
					String vmArgs = properties.getString("remote.jvm.vmargs", null);
					Integer rmiMinPort = properties.getInteger("remote.jvm.rmi[@minport]", 10000);
					Integer rmiMaxPort = properties.getInteger("remote.jvm.rmi[@maxport]", 10200);
					
					sandboxService.shutdownJvmPool();
					JvmPoolImpl jvmPoolImpl = new JvmPoolImpl(
						new JvmPoolConfigImpl(poolsize, freelancersize, 
							new JvmInstantiatorImpl(rmiMinPort, rmiMaxPort, null != vmArgs && "".equals(vmArgs.trim()) ? null : vmArgs)
						)
					);
					sandboxService.initJvmPool(jvmPoolImpl);
				} else {
					sandboxService.shutdownJvmPool();
				}
			}
			
			/* daemons */
			Integer monitorCheckInterval = properties.getInteger("monitor[@checkinterval]", 10);
			sandboxService.setMonitorDaemonCheckInterval(monitorCheckInterval);
			Integer monitorWatchdogCheckInterval = properties.getInteger("monitor[@watchdogCheckinterval]", 10000);
			sandboxService.setMonitorWatchdogCheckInterval(monitorWatchdogCheckInterval);
			
			/* codesource */
			boolean enableCodesource = properties.getBoolean("codesource[@enable]", false);
			sandboxService.setCodesourceSecurityChecks(enableCodesource);
		}
		
		/* sandboxes */
		parse(config);
		for(Entry<String, SandboxContext> e : getRestrictionSets().entrySet())
			sandboxService.registerContext(e.getKey(), e.getValue());
	}
	
	public void parse(Configuration config) {
		if(! (config instanceof HierarchicalConfiguration))
			throw new IllegalArgumentException("Expected HierarchicalConfiguration format");
		
		HierarchicalConfiguration conf = (HierarchicalConfiguration) config;

		/* sandboxes */
		for(HierarchicalConfiguration rs : conf.configurationsAt("security.sandbox")){
			String name = rs.getString("[@name]");
			if(null == name)
				throw new IllegalArgumentException("no name for sandbox given");

			SandboxContext set = loadSandbox(name, conf, rs, new HashSet<String>());
			restrictionSets.put(name,set);
		}
	}
	
	protected SandboxContext loadSandbox(String name, HierarchicalConfiguration conf, HierarchicalConfiguration contextConf, HashSet<String> basedOnProcessed) {
		SandboxContext context = new SandboxContext();
		String basedOn = contextConf.getString("[@basedOn]");
		if(null != basedOn){
			if(basedOnProcessed.contains(basedOn))
				throw new IllegalStateException("Loop detected: there seems to be a loop in the sandbox configuration at" + basedOn + " and " + name);
			basedOnProcessed.add(basedOn);
			
			HierarchicalConfiguration baseConf = null;
			for(HierarchicalConfiguration c : conf.configurationsAt("security.sandbox")){
				if(name.equals(contextConf.getString("[@name]"))){
					baseConf = c;
					break;
				}
			}
			if(null == baseConf)
				throw new IllegalStateException("Could not find config for " + basedOn);
			
			SandboxContext basis = loadSandbox( name, conf, baseConf, basedOnProcessed);
			context = basis.clone();
		}
		
		Boolean allAccess = contextConf.getBoolean("[@allowAll]", false);
		if(allAccess)
			context.setPassAll(allAccess);
		
		boolean bypassClassAccesss = contextConf.getBoolean("[@bypassClassAccess]", false);
		context.setBypassClassAccessChecks(bypassClassAccesss);
		
		boolean bypassPackageAccesss = contextConf.getBoolean("[@bypassPackageAccess]", false);
		context.setBypassPackageAccessChecks(bypassPackageAccesss);
		
		Boolean debug = contextConf.getBoolean("[@debug]", false);
		if(debug)
			context.setDebug(debug);
		
		String codesource = contextConf.getString("[@codesource]", null);
		if(null != codesource && ! "".equals(codesource.trim()))
			context.setCodesource(codesource);
		
		/* run in */
		Boolean runInThread = contextConf.getBoolean("[@runInThread]", false);
		if(runInThread)
			context.setRunInThread(runInThread);
		Boolean runRemote = contextConf.getBoolean("[@runRemote]", false);
		if(runRemote)
			context.setRunRemote(runRemote);
		
		/* finalizers */
		Boolean removeFinalizers = contextConf.getBoolean("[@removeFinalizers]", false);
		if(removeFinalizers)
			context.setRemoveFinalizers(removeFinalizers);
		
		/* thread */
		configureThreadRestrictions(context, contextConf);
		
		/* packages */
		configurePackages(context, contextConf);
		
		/* class access */
		try {
			configureClasses(context, contextConf);
			
			/* application loader */
			configureClassesForApplicationLoader(context, contextConf);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Could not generate URL", e);
		}
		
		/* permissions */
		configurePermissions(context, contextConf);
		
		/* file access */
		configureFileAccess(context, contextConf);		
		
		return context;
	}

	protected void configureThreadRestrictions(SandboxContext context,
			HierarchicalConfiguration contextConf) {
		long maximumRunTime = contextConf.getLong("[@maximumRunTime]", -1);
		context.setMaximumRunTime(maximumRunTime);
		
		String maximumRunTimeUnit = contextConf.getString("[@maximumRunTimeUnit]", null);
		if(null != maximumRunTimeUnit && ! "".equals(maximumRunTimeUnit.trim()))
			context.setMaximumRunTimeUnit(TimeUnit.valueOf(maximumRunTimeUnit.toUpperCase()));
		
		String maximumRunTimeMode = contextConf.getString("[@maximumRunTimeMode]", null);
		if(null != maximumRunTimeMode && ! "".equals(maximumRunTimeMode.trim()))
			context.setMaximumRuntimeMode(RuntimeMode.valueOf(maximumRunTimeMode.toUpperCase()));
		
		int maxStackDepth = contextConf.getInteger("[@maximumStackDepth]", -1);
		context.setMaximumStackDepth(maxStackDepth);
	}

	protected void configureFileAccess(SandboxContext context,
			HierarchicalConfiguration rs) {
		for(HierarchicalConfiguration suf : rs.configurationsAt("fileAccess.read.suffixes.entry"))
			context.addFilePermission(FileAccess.READ, AccessType.PERMIT, new FileSuffixPermission(suf.getString("."), suf.getBoolean("[@negate]", false)));
		for(HierarchicalConfiguration prefix : rs.configurationsAt("fileAccess.read.prefixes.entry"))
			context.addFilePermission(FileAccess.READ, AccessType.PERMIT, new FilePrefixPermission(prefix.getString("."), prefix.getBoolean("[@negate]", false)));
		for(HierarchicalConfiguration entry : rs.configurationsAt("fileAccess.read.exactMatches.entry"))
			context.addFilePermission(FileAccess.READ, AccessType.PERMIT, new FileEqualsPermission(entry.getString("."), entry.getBoolean("[@negate]", false)));
		for(HierarchicalConfiguration entry : rs.configurationsAt("fileAccess.read.regex.entry"))
			context.addFilePermission(FileAccess.READ, AccessType.PERMIT, new FileRegexPermission(entry.getString("."), entry.getBoolean("[@negate]", false)));
		
		for(HierarchicalConfiguration suf : rs.configurationsAt("fileAccess.write.suffixes.entry"))
			context.addFilePermission(FileAccess.WRITE, AccessType.PERMIT, new FileSuffixPermission(suf.getString("."), suf.getBoolean("[@negate]", false)));
		for(HierarchicalConfiguration prefix : rs.configurationsAt("fileAccess.write.prefixes.entry"))
			context.addFilePermission(FileAccess.WRITE, AccessType.PERMIT, new FilePrefixPermission(prefix.getString("."), prefix.getBoolean("[@negate]", false)));
		for(HierarchicalConfiguration entry : rs.configurationsAt("fileAccess.write.exactMatches.entry"))
			context.addFilePermission(FileAccess.WRITE, AccessType.PERMIT, new FileEqualsPermission(entry.getString("."), entry.getBoolean("[@negate]", false)));
		for(HierarchicalConfiguration entry : rs.configurationsAt("fileAccess.write.regex.entry"))
			context.addFilePermission(FileAccess.WRITE, AccessType.PERMIT, new FileRegexPermission(entry.getString("."), entry.getBoolean("[@negate]", false)));
		
		for(HierarchicalConfiguration suf : rs.configurationsAt("fileAccess.delete.suffixes.entry"))
			context.addFilePermission(FileAccess.DELETE, AccessType.PERMIT, new FileSuffixPermission(suf.getString("."), suf.getBoolean("[@negate]", false)));
		for(HierarchicalConfiguration prefix : rs.configurationsAt("fileAccess.delete.prefixes.entry"))
			context.addFilePermission(FileAccess.DELETE, AccessType.PERMIT, new FilePrefixPermission(prefix.getString("."), prefix.getBoolean("[@negate]", false)));
		for(HierarchicalConfiguration entry : rs.configurationsAt("fileAccess.delete.exactMatches.entry"))
			context.addFilePermission(FileAccess.DELETE, AccessType.PERMIT, new FileEqualsPermission(entry.getString("."), entry.getBoolean("[@negate]", false)));
		for(HierarchicalConfiguration entry : rs.configurationsAt("fileAccess.delete.regex.entry"))
			context.addFilePermission(FileAccess.DELETE, AccessType.PERMIT, new FileRegexPermission(entry.getString("."), entry.getBoolean("[@negate]", false)));
		
		Boolean cp = rs.getBoolean("fileAccess[@classpath]", false);
		if(cp)
			context.addClasspath();
		
		Boolean home = rs.getBoolean("fileAccess[@home]", false);
		if(home)
			context.addHome();
		
		Boolean temp = rs.getBoolean("fileAccess[@temp]", false);
		if(temp)
			context.addTempDir();
		
		Boolean work = rs.getBoolean("fileAccess[@work]", false);
		if(work)
			context.addWorkDir();
		
		for(HierarchicalConfiguration suf : rs.configurationsAt("fileAccessDeny.read.suffixes.entry"))
			context.addFilePermission(FileAccess.READ, AccessType.DENY, new FileSuffixPermission(suf.getString("."), suf.getBoolean("[@negate]", false)));
		for(HierarchicalConfiguration prefix : rs.configurationsAt("fileAccessDeny.read.prefixes.entry"))
			context.addFilePermission(FileAccess.READ, AccessType.DENY, new FilePrefixPermission(prefix.getString("."), prefix.getBoolean("[@negate]", false)));
		for(HierarchicalConfiguration entry : rs.configurationsAt("fileAccessDeny.read.exactMatches.entry"))
			context.addFilePermission(FileAccess.READ, AccessType.DENY, new FileEqualsPermission(entry.getString("."), entry.getBoolean("[@negate]", false)));
		for(HierarchicalConfiguration entry : rs.configurationsAt("fileAccessDeny.read.regex.entry"))
			context.addFilePermission(FileAccess.READ, AccessType.DENY, new FileRegexPermission(entry.getString("."), entry.getBoolean("[@negate]", false)));
		
		for(HierarchicalConfiguration suf : rs.configurationsAt("fileAccessDeny.write.suffixes.entry"))
			context.addFilePermission(FileAccess.WRITE, AccessType.DENY, new FileSuffixPermission(suf.getString("."), suf.getBoolean("[@negate]", false)));
		for(HierarchicalConfiguration prefix : rs.configurationsAt("fileAccessDeny.write.prefixes.entry"))
			context.addFilePermission(FileAccess.WRITE, AccessType.DENY, new FilePrefixPermission(prefix.getString("."), prefix.getBoolean("[@negate]", false)));
		for(HierarchicalConfiguration entry : rs.configurationsAt("fileAccessDeny.write.exactMatches.entry"))
			context.addFilePermission(FileAccess.WRITE, AccessType.DENY, new FileEqualsPermission(entry.getString("."), entry.getBoolean("[@negate]", false)));
		for(HierarchicalConfiguration entry : rs.configurationsAt("fileAccessDeny.write.regex.entry"))
			context.addFilePermission(FileAccess.WRITE, AccessType.DENY, new FileRegexPermission(entry.getString("."), entry.getBoolean("[@negate]", false)));
		
		for(HierarchicalConfiguration suf : rs.configurationsAt("fileAccessDeny.delete.suffixes.entry"))
			context.addFilePermission(FileAccess.DELETE, AccessType.DENY, new FileSuffixPermission(suf.getString("."), suf.getBoolean("[@negate]", false)));
		for(HierarchicalConfiguration prefix : rs.configurationsAt("fileAccessDeny.delete.prefixes.entry"))
			context.addFilePermission(FileAccess.DELETE, AccessType.DENY, new FilePrefixPermission(prefix.getString("."), prefix.getBoolean("[@negate]", false)));
		for(HierarchicalConfiguration entry : rs.configurationsAt("fileAccessDeny.delete.exactMatches.entry"))
			context.addFilePermission(FileAccess.DELETE, AccessType.DENY, new FileEqualsPermission(entry.getString("."), entry.getBoolean("[@negate]", false)));
		for(HierarchicalConfiguration entry : rs.configurationsAt("fileAccessDeny.delete.regex.entry"))
			context.addFilePermission(FileAccess.DELETE, AccessType.DENY, new FileRegexPermission(entry.getString("."), entry.getBoolean("[@negate]", false)));
	}

	protected void configurePermissions(SandboxContext set,
			HierarchicalConfiguration rs) {
		for(HierarchicalConfiguration perm : rs.configurationsAt("permission.whitelist.entry")){
			String type = perm.getString("type");
			String pName = perm.getString("name");
			String actions = perm.getString("actions");
			
			Collection<StackEntry> entries = new HashSet<StackEntry>();
			
			for(HierarchicalConfiguration stack : perm.configurationsAt("stack.check")){
				StackEntry entry = getStackEntry(stack);
				entries.add(entry);
			}
			
			SecurityPermission wp = new SecurityPermission(type, pName, actions, entries);
			
			set.addSecurityPermission(AccessType.PERMIT, wp);
		}
		
		for(HierarchicalConfiguration perm : rs.configurationsAt("permission.blacklist.entry")){
			String type = perm.getString("type");
			String pName = perm.getString("name", null);
			String actions = perm.getString("actions", null);
			
			Collection<StackEntry> entries = new HashSet<StackEntry>();
			
			for(HierarchicalConfiguration stack : perm.configurationsAt("stack.check")){
				StackEntry entry = getStackEntry(stack);
				entries.add(entry);
			}
			
			SecurityPermission wp = new SecurityPermission(type, 
				null != pName && "".equals(pName.trim()) ? null : pName, 
				null != actions && "".equals(actions.trim()) ? null : actions, 
				entries);
			
			set.addSecurityPermission(AccessType.DENY, wp);
		}
	}

	protected void configurePackages(SandboxContext set,
			HierarchicalConfiguration rs) {
		for(Object e : rs.getList("packages.whitelist.entry"))
			set.addPackagePermission(AccessType.PERMIT, (String) e);
		
		for(Object e : rs.getList("packages.blacklist.entry"))
			set.addPackagePermission(AccessType.DENY, (String) e);
		
		for(HierarchicalConfiguration compl : rs.configurationsAt("packages.whitelist.complex")){
			String cName = compl.getString("[@name]");
			
			Collection<StackEntry> entries = new HashSet<StackEntry>();
			
			for(HierarchicalConfiguration stack : compl.configurationsAt("check"))
				entries.add(getStackEntry(stack));
			
			PackagePermission wpkg = new PackagePermission(cName, entries);
			
			set.addPackagePermission(wpkg);
		}		
	}
	
	protected void configureClasses(SandboxContext set,
			HierarchicalConfiguration rs) throws MalformedURLException {
		for(Object e : rs.getList("classes.whitelist.entry"))
			set.addClassPermission(AccessType.PERMIT,(String) e);
		
		for(Object e : rs.getList("classes.whitelist.jar")){
			set.addJarToWhitelist(new URL((String) e));
		}
		
		for(Object e : rs.getList("classes.blacklist.entry"))
			set.addClassPermission(AccessType.DENY,(String) e);
		
		for(HierarchicalConfiguration compl : rs.configurationsAt("classes.whitelist.complex")){
			String cName = compl.getString("[@name]");
			
			Collection<StackEntry> entries = new HashSet<StackEntry>();
			
			for(HierarchicalConfiguration stack : compl.configurationsAt("check"))
				entries.add(getStackEntry(stack));
			
			ClassPermission wclazz = new ClassPermission(cName, entries);
			
			set.addClassPermission(wclazz);
		}		
	}
	
	protected void configureClassesForApplicationLoader(SandboxContext set,
			HierarchicalConfiguration rs) throws MalformedURLException {
		for(Object e : rs.getList("applicationLoader.class"))
			set.addClassForApplicationLoader((String) e);
		for(Object e : rs.getList("applicationLoader.classPrefix"))
			set.addClassForApplicationLoader((String) e, Mode.PREFIX);
		for(Object e : rs.getList("applicationLoader.jar"))
			set.addJarForApplicationLoader(new URL((String) e));
		
		for(Object e : rs.getList("sandboxLoader.class"))
			set.addClassForSandboxLoader((String) e);
		for(Object e : rs.getList("sandboxLoader.classPrefix"))
			set.addClassForSandboxLoader((String) e, Mode.PREFIX);
	}

	protected StackEntry getStackEntry(HierarchicalConfiguration stack) {
		int pos = stack.getInt("[@pos]");
		String stype = stack.getString("[@type]");
		Boolean prefix = stack.getBoolean("[@prefix]", false);
		
		return new StackEntry(pos, stype, Boolean.TRUE.equals(prefix));
	}

	public Map<String, SandboxContext> getRestrictionSets() {
		return restrictionSets;
	}
	
}
