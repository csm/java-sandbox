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

package net.datenwerke.sandbox.util.context;

import java.net.URL;

import net.datenwerke.sandbox.SandboxContext;
import net.datenwerke.sandbox.permissions.SecurityPermission;
import net.datenwerke.sandbox.permissions.StackEntry;

/**
 * A permission set to run groovy scripts
 * 
 * <pre>
 * {@code
 * super(true);
 * 
 * groovyURLs = urls;
 * if(null == urls)
 * 	throw new IllegalArgumentException("urls cannot be null");
 * for(URL url : urls)
 * 	addJarToWhitelist(url);
 * 
 * addHome();
 * 
 * addClassForApplicationLoader("sun.reflect.");
 * 
 * addClassPermission(AccessType.PERMIT,
 * 	"java.lang.", "java.util.", "java.io.", "java.security.", "java.text.",
 * 	"javax.script.", 
 * 	"javax.swing.",
 * 	"net.datenwerke.sandbox.util.VariableAssignment",
 * 	"sun.reflect."
 * );
 * 
 * 
 * addSecurityPermission(AccessType.PERMIT, 
 * new SecurityPermission("java.lang.RuntimePermission", "reflectionFactoryAccess"));
 * addSecurityPermission(AccessType.PERMIT, 
 * new SecurityPermission("java.lang.RuntimePermission", "accessDeclaredMembers"));
 * addSecurityPermission(AccessType.PERMIT, 
 * new SecurityPermission("java.lang.reflect.ReflectPermission", "suppressAccessChecks"));
 * addSecurityPermission(AccessType.PERMIT, 
 * new SecurityPermission("java.lang.reflect.AccessDeclaredMemberPermission"));
 * addSecurityPermission(AccessType.PERMIT, 
 * new SecurityPermission("java.lang.reflect.SuppressAccessCheckPermission"));
 * 
 * 
 * addSecurityPermission(AccessType.PERMIT, 
 * new SecurityPermission("java.lang.RuntimePermission", "getProtectionDomain"));
 * 
 * addSecurityPermission(AccessType.PERMIT, 
 * new SecurityPermission("java.lang.RuntimePermission", "createClassLoader", new StackEntry(8, "org.codehaus.groovy.util.LazyReference" )));
 * addSecurityPermission(AccessType.PERMIT, 
 * new SecurityPermission("java.lang.RuntimePermission", "createClassLoader", new StackEntry(10, "org.codehaus.groovy.util.LazyReference" )));
 * addSecurityPermission(AccessType.PERMIT, 
 * new SecurityPermission("java.lang.RuntimePermission", "createClassLoader", new StackEntry(12, "org.codehaus.groovy.reflection.CachedMethod" )));
 * addSecurityPermission(AccessType.PERMIT, 
 * new SecurityPermission("java.lang.RuntimePermission", "createClassLoader", new StackEntry(15, "org.codehaus.groovy.reflection.CachedConstructor" )));
 * addSecurityPermission(AccessType.PERMIT, 
 * new SecurityPermission("java.lang.RuntimePermission", "getClassLoader", new StackEntry(8, "org.codehaus.groovy.util.LazyReference" )));
 * 
 * 
 * addSecurityPermission(AccessType.PERMIT, 
 * new SecurityPermission("java.util.PropertyPermission", null, "read"));
 * 
 * addSecurityPermission(AccessType.PERMIT, 
 * new SecurityPermission("java.util.logging.LoggingPermission", "control"));
 * }
 * </pre>
 * 
 * 
 * @author Arno Mittelbach
 *
 */
public class GroovyBaseContext extends SandboxContext {

	public static String BASE_CONTEXT_IDENTIFIER = "GROOVY_BASE_CONTEXT";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8301792633121425441L;
	
	private URL[] groovyURLs;
	
	public GroovyBaseContext(URL... urls) {
		super(true);
		
		groovyURLs = urls;
		if(null == urls)
			throw new IllegalArgumentException("urls cannot be null");
		for(URL url : urls)
			addJarToWhitelist(url);
		
		addHome();
		
		addClassForApplicationLoader("sun.reflect.");
		
		addClassPermission(AccessType.PERMIT,
			"java.lang.", "java.util.", "java.io.", "java.security.", "java.text.",
			"javax.script.", 
			"javax.swing.",
			"net.datenwerke.sandbox.util.VariableAssignment",
			"sun.reflect."
		);
		
		/* allow reflection */

		addSecurityPermission(AccessType.PERMIT, 
				new SecurityPermission("java.lang.RuntimePermission", "reflectionFactoryAccess"));
		addSecurityPermission(AccessType.PERMIT, 
				new SecurityPermission("java.lang.RuntimePermission", "accessDeclaredMembers"));
		addSecurityPermission(AccessType.PERMIT, 
				new SecurityPermission("java.lang.reflect.ReflectPermission", "suppressAccessChecks"));
		addSecurityPermission(AccessType.PERMIT, 
				new SecurityPermission("java.lang.reflect.AccessDeclaredMemberPermission"));
		addSecurityPermission(AccessType.PERMIT, 
				new SecurityPermission("java.lang.reflect.SuppressAccessCheckPermission"));
		
		
		addSecurityPermission(AccessType.PERMIT, 
				new SecurityPermission("java.lang.RuntimePermission", "getProtectionDomain"));
		
		addSecurityPermission(AccessType.PERMIT, 
				new SecurityPermission("java.lang.RuntimePermission", "createClassLoader", new StackEntry(8, "org.codehaus.groovy.util.LazyReference" )));
		addSecurityPermission(AccessType.PERMIT, 
				new SecurityPermission("java.lang.RuntimePermission", "createClassLoader", new StackEntry(10, "org.codehaus.groovy.util.LazyReference" )));
		addSecurityPermission(AccessType.PERMIT, 
				new SecurityPermission("java.lang.RuntimePermission", "createClassLoader", new StackEntry(12, "org.codehaus.groovy.reflection.CachedMethod" )));
		addSecurityPermission(AccessType.PERMIT, 
				new SecurityPermission("java.lang.RuntimePermission", "createClassLoader", new StackEntry(15, "org.codehaus.groovy.reflection.CachedConstructor" )));
		addSecurityPermission(AccessType.PERMIT, 
				new SecurityPermission("java.lang.RuntimePermission", "getClassLoader", new StackEntry(8, "org.codehaus.groovy.util.LazyReference" )));

		
		addSecurityPermission(AccessType.PERMIT, 
				new SecurityPermission("java.util.PropertyPermission", null, "read"));

		addSecurityPermission(AccessType.PERMIT, 
				new SecurityPermission("java.util.logging.LoggingPermission", "control"));
	}
	

	public URL[] getGroovyURLs() {
		return groovyURLs;
	}
}
