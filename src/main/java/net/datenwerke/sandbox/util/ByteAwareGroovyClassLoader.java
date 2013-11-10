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

import groovy.lang.GroovyClassLoader;
import groovyjarjarasm.asm.ClassWriter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;

/**
 * 
 * @author Arno Mittelbach
 *
 */
public class ByteAwareGroovyClassLoader extends GroovyClassLoader {

	private Map<String, byte[]> classBytes = new HashMap<String, byte[]>();
    
    public ByteAwareGroovyClassLoader() {
    	super();
    }
    
    public ByteAwareGroovyClassLoader(ClassLoader parent) {
    	super(parent);
	}
    
    public ByteAwareGroovyClassLoader(CompilerConfiguration config) {
    	this(Thread.currentThread().getContextClassLoader(), config);
	}
    
    public ByteAwareGroovyClassLoader(CompilerConfiguration config, boolean useConfigurationClasspath) {
    	this(Thread.currentThread().getContextClassLoader(), config, useConfigurationClasspath);
	}
    
    public ByteAwareGroovyClassLoader(ClassLoader parent, CompilerConfiguration config) {
    	super(parent, config);
	}
    
    public ByteAwareGroovyClassLoader(ClassLoader parent, CompilerConfiguration config, boolean useConfigurationClasspath) {
    	super(parent, config, useConfigurationClasspath);
	}

	@Override
    public InputStream getResourceAsStream(String name) {
        if (classBytes.containsKey(name)) {
            return new ByteArrayInputStream(classBytes.get(name));
        }
        return super.getResourceAsStream(name);
    }

    @Override
    protected ClassCollector createCollector(CompilationUnit unit, SourceUnit su) {
        InnerLoader loader = AccessController.doPrivileged(new PrivilegedAction<InnerLoader>() {
            public InnerLoader run() {
                return new InnerLoader(ByteAwareGroovyClassLoader.this);
            }
        });
        return new ByteAwareGroovyClassCollector(classBytes, loader, unit, su);
    }
    
    public Map<String, byte[]> getClassBytes() {
		return classBytes;
	}
    
    public byte[] getClassBytes(String name) {
  		return classBytes.get(name);
  	}

    public static class ByteAwareGroovyClassCollector extends ClassCollector {
        private final Map<String, byte[]> classBytes;

        public ByteAwareGroovyClassCollector(Map<String, byte[]> classBytes, InnerLoader loader, CompilationUnit unit, SourceUnit su) {
            super(loader, unit, su);
            this.classBytes = classBytes;
        }

        
        @Override
        protected Class onClassNode(ClassWriter classWriter,
        		org.codehaus.groovy.ast.ClassNode classNode) {
        	classBytes.put(classNode.getName(), classWriter.toByteArray());
        	return super.onClassNode(classWriter, classNode);
        }
    }

    /**
     * 
     * @param script
     * @return
     */
	public CompiledScript parseClassToBytes(String script) {
		Class parseClass = parseClass(script);
		CompiledScript compiled = new CompiledScript(classBytes.get(parseClass.getName()), parseClass.getName());
		Collection<CompiledScript> col = new HashSet<CompiledScript>();
		for(String key : classBytes.keySet()){
			if(! parseClass.getName().equals(key))
				col.add(new CompiledScript(classBytes.get(key), key));
		}
		compiled.setSubScripts(col);
		return compiled;
		
	}
    

}
