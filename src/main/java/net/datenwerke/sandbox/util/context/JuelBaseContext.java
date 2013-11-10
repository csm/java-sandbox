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

import com.google.inject.Inject;

/**
 * A {@link SandboxContext} containing basic permissions to run juel.
 * 
 * <pre>
 * {@code
 * super();
 * 
 * addClasspath();
 * addHome();
 * 
 * addClassForApplicationLoader("javax.el", "sun.reflect.");
 * 
 * if(null != jarLocation)
 * 	for(URL url : jarLocation)
 * addJarToWhitelist(url);
 * 
 * addClassPermission(AccessType.PERMIT, 
 * 	"java.lang.", "java.text.", "java.util.", "java.math.", "java.io.",
 * 	"net.datenwerke.sandbox.util.VariableAssignment",
 * 	"sun.reflect."
 * );
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
 * addSecurityPermission(AccessType.PERMIT, new SecurityPermission("java.net.NetPermission", "specifyStreamHandler"));
 * 
 * addSecurityPermission(AccessType.PERMIT, new SecurityPermission("java.lang.RuntimePermission", "createClassLoader",
 * 	new StackEntry(4, "net.datenwerke.transloader.load.CollectedClassLoader")
 * ));
 * addSecurityPermission(AccessType.PERMIT, new SecurityPermission("java.lang.RuntimePermission", "createClassLoader",
 * new StackEntry(4, "net.datenwerke.transloader.load.BootClassLoader")
 * 	));
 * addSecurityPermission(AccessType.PERMIT, new SecurityPermission("java.lang.RuntimePermission", "createClassLoader",
 * new StackEntry(14, "javax.el.ExpressionFactory")
 * 	));
 * addSecurityPermission(AccessType.PERMIT, new SecurityPermission("java.lang.RuntimePermission", "createClassLoader",
 * new StackEntry(17, "javax.el.ExpressionFactory")
 * 	));
 * addSecurityPermission(AccessType.PERMIT, new SecurityPermission("java.lang.RuntimePermission", "createClassLoader",
 * new StackEntry(-1, "net.datenwerke.transloader.clone.reflect.instantiate.ObjenesisInstantiationStrategy")
 * 	));
 * 
 * addSecurityPermission(AccessType.PERMIT, new SecurityPermission("java.util.PropertyPermission", null, "read"));
 * }
 * </pre>
 * @author Arno Mittelbach
 *
 */
public class JuelBaseContext extends SandboxContext {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3906292911108743974L;

	@Inject
	public JuelBaseContext(
		URL... jarLocation
		) {
		super();
		
		addClasspath();
		addHome();

		addClassForApplicationLoader("javax.el", "sun.reflect.");
		
		if(null != jarLocation)
			for(URL url : jarLocation)
				addJarToWhitelist(url);
		
		addClassPermission(AccessType.PERMIT, 
			"java.lang.", "java.text.", "java.util.", "java.math.", "java.io.",
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
		
		
		addSecurityPermission(AccessType.PERMIT, new SecurityPermission("java.net.NetPermission", "specifyStreamHandler"));

		/* classloader */
		addSecurityPermission(AccessType.PERMIT, new SecurityPermission("java.lang.RuntimePermission", "createClassLoader",
			new StackEntry(4, "net.datenwerke.transloader.load.CollectedClassLoader")
		));
		addSecurityPermission(AccessType.PERMIT, new SecurityPermission("java.lang.RuntimePermission", "createClassLoader",
				new StackEntry(4, "net.datenwerke.transloader.load.BootClassLoader")
			));
		addSecurityPermission(AccessType.PERMIT, new SecurityPermission("java.lang.RuntimePermission", "createClassLoader",
				new StackEntry(14, "javax.el.ExpressionFactory")
			));
		addSecurityPermission(AccessType.PERMIT, new SecurityPermission("java.lang.RuntimePermission", "createClassLoader",
				new StackEntry(17, "javax.el.ExpressionFactory")
			));
		addSecurityPermission(AccessType.PERMIT, new SecurityPermission("java.lang.RuntimePermission", "createClassLoader",
				new StackEntry(-1, "net.datenwerke.transloader.clone.reflect.instantiate.ObjenesisInstantiationStrategy")
			));
		
		/* properties */
		addSecurityPermission(AccessType.PERMIT, new SecurityPermission("java.util.PropertyPermission", null, "read"));
	}

}
