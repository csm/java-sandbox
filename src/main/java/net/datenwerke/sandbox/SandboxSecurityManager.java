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

import java.security.AccessControlException;
import java.security.Permission;
import java.util.Arrays;

import net.datenwerke.sandbox.securitypermissions.SandboxRuntimePermission;

/**
 * The {@link SandboxSecurityManager} is an implementation of Java's {@link SecurityManager}
 * that performs all necessary checks for code currently executed in sandboxes.
 * 
 * @author Arno Mittelbach
 *
 */
final public class SandboxSecurityManager extends SecurityManager {

	private final InheritableThreadLocal<byte[]> restrict = new InheritableThreadLocal<byte[]>();
	private final InheritableThreadLocal<SandboxContext> contextHolder = new InheritableThreadLocal<SandboxContext>();
	
	private final ThreadLocal<Boolean> isInCheck = new ThreadLocal<Boolean>();
	private final ThreadLocal<Boolean> debug = new ThreadLocal<Boolean>();
	private final SandboxService sandboxingService;
	private boolean codesourceSecurityChecks;
	
	private static SandboxSecurityManager INSTANCE;
	
	private SandboxSecurityManager(SandboxService service) {
		super();
		this.sandboxingService = service;
	}	
	
	static SandboxSecurityManager newInstance(SandboxService service){
		if(null != INSTANCE)
			throw new IllegalStateException("SecurityManager cannot be instantiated twice");
		INSTANCE = new SandboxSecurityManager(service);
		return INSTANCE;
	}
	
	public SandboxService getSandboxService() {
		return sandboxingService;
	}

	void restrictAccess(String pw, SandboxContext context){
		if(null != restrict.get())
			throw new IllegalStateException();

		byte[] password = pw.getBytes();
		
		this.contextHolder.set(context);
		restrict.set(password);
		if(context.isDebug())
			debug.set(true);
	}
	
	public boolean isRestricted(){
		return null != restrict.get();
	}
	
	void releaseRestriction(String pw){
		if(isInCheck())
			throw new AccessControlException("cannot release restriction during security checks"); 
			
		byte[] password = pw.getBytes();
		if(Arrays.equals(password, restrict.get())){
			restrict.set(null);
			contextHolder.set(null);
			isInCheck.set(null);
			debug.set(null);
		} else
			throw new AccessControlException("Wrong password");
	}
	
	private boolean isInCheck(){
		return null != isInCheck.get();
	}
	
	private boolean isDebug(){
		return null != debug.get();
	}
	
	private void setInCheck(boolean check){
		if(check)
			isInCheck.set(true);
		else
			isInCheck.set(null);
	}
	
	public void checkPermission(Permission perm) {
		if(codesourceSecurityChecks)
			super.checkPermission(perm);
		
		if(isRestricted() && ! isInCheck()){
			setInCheck(true);
			
			try{
				SandboxContext context = contextHolder.get();
				boolean debug = isDebug();
				if(debug)
					context.debugPermissionCheck(perm);
				
				if(context.isBypassPermissionAccessChecks() || context.isPassAll())
					return;
				
				Class stack[] = getClassContext();
				if(context.checkPermission(perm, stack))
					return;
				
				if(debug)
					context.debugDeniedPermission(perm, getClassContext());

				throw new AccessControlException("Permission not granted: " + perm, perm);
			} finally {
				setInCheck(false);
			}
		}
	}

	public void checkPermission(Permission perm, Object context) {
		if(codesourceSecurityChecks)
			super.checkPermission(perm, context);
		
		if(isRestricted())
			throw new AccessControlException("Nope");
	}
	
	public void checkClassAccess(String clazz) {
		if(isRestricted() && ! isInCheck()){
			setInCheck(true);

			try{
				SandboxContext rs = contextHolder.get();
				boolean debug = isDebug();
				if(debug)
					rs.debugCheckClassAccess(clazz);

				if(rs.isBypassClassAccessChecks() || rs.isPassAll())
					return;
				
				Class stack[] = getClassContext();
				
				if(! rs.checkClassAccess(clazz, stack)){
					if(debug)
						rs.debugDeniedClassAccess(clazz, stack);
					
					throw new AccessControlException("No class access allowed for class: " + clazz);
				}
			} finally {
				setInCheck(false);
			}
		}
	}
	
	public void checkPackageAccess(String pkg) {
		if(codesourceSecurityChecks)
			super.checkPackageAccess(pkg);
		
		if(isRestricted() && ! isInCheck()){
			/* have to allow java.lang for basic datatype */
			if("java.lang".equals(pkg))
				return;
			
			setInCheck(true);

			try{
				SandboxContext rs = contextHolder.get();
				boolean debug = isDebug();
				if(debug)
					contextHolder.get().debugCheckPackageAccess(pkg);
				
				if(rs.isBypassPackageAccessChecks() || rs.isPassAll())
					return;
				
				Class stack[] = getClassContext();
				
				if(! rs.checkPackageAccess(pkg, stack)){
					if(debug)
						rs.debugDeniedPackageAccess(pkg, stack);
					
					throw new AccessControlException("No package access allowed for package: " + pkg);
				}
			} finally {
				setInCheck(false);
			}
		}
	}
	
	Class[] getCurrentClassContext() {
		if(isRestricted())
			throw new AccessControlException("no classContext during sandbox");
		return super.getClassContext();
	}

	public void checkPackageDefinition(String pkg) {
		if(codesourceSecurityChecks)
			super.checkPackageDefinition(pkg);
		
		if(isRestricted())
			throw new AccessControlException("no package definition: " + pkg);
	}

	void setCodesourceSecurityChecks(boolean enable) {
		checkPermission(new SandboxRuntimePermission("enableCodesourceSecurity"));
		codesourceSecurityChecks = enable;
	}
	
	public boolean isCodesourceSecurityChecks() {
		return codesourceSecurityChecks;
	}
	
		
}
