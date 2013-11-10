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

import com.google.inject.Inject;

/**
 * A {@link SandboxContext} containing basic permissions to run rhino scripts.
 * 
 * <pre>
 * {@code
 * super();
 * 
 * addHome();
 * 
 * if(null != jarLocation)
 * 	for(URL url : jarLocation)
 * addJarToWhitelist(url);
 * 
 * addClassPermission(AccessType.PERMIT,
 * "java.lang.", "java.util.", "java.io.", "java.security.", "java.text.",
 * "javax.script.", 
 * "net.datenwerke.sandbox.util.VariableAssignment",
 * "sun.reflect."
 * 	);
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
 * addSecurityPermission(AccessType.PERMIT, new SecurityPermission("java.util.PropertyPermission", null, "read"));

 * }
 * </pre>
 * @author Arno Mittelbach
 *
 */
public class RhinoBaseContext extends SandboxContext {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1379403643054213434L;

	@Inject
	public RhinoBaseContext(
		URL... jarLocation
		) {
		super();
		
		addHome();

		if(null != jarLocation)
			for(URL url : jarLocation)
				addJarToWhitelist(url);
		
		addClassPermission(AccessType.PERMIT,
				"java.lang.", "java.util.", "java.io.", "java.security.", "java.text.",
				"javax.script.", 
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
		
		addSecurityPermission(AccessType.PERMIT, new SecurityPermission("java.util.PropertyPermission", null, "read"));

	}

}
