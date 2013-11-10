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

package net.datenwerke.sandbox.jvm;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.datenwerke.sandbox.SandboxedCallResult;

/**
 * 
 * @author Arno Mittelbach
 *
 */
public class JvmFuture implements Future<SandboxedCallResult> {

	private boolean cancel = false;
	private boolean done = false;
	
	private final JvmTask task;
	
	private SandboxedCallResult result = null;
	private Exception exception = null;
	
	public JvmFuture(JvmTask task) {
		super();
		this.task = task;
	}

	JvmTask getTask() {
		return task;
	}
	
	synchronized void setResult(SandboxedCallResult result) {
		if(done)
			throw new IllegalStateException("result already set");
		done = true;
		this.result = result;
		notifyAll();
	}

	synchronized void setException(Exception e) {
		if(done)
			throw new IllegalStateException("result already set");
		done = true;
		this.exception = e;
		notifyAll();
	}
	
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return false;
	}

	@Override
	public boolean isCancelled() {
		return cancel;
	}

	@Override
	public boolean isDone() {
		return done;
	}

	@Override
	public SandboxedCallResult get() throws InterruptedException, ExecutionException {
		synchronized (this) {
			if(isDone()){
				if(null != exception)
					throw new ExecutionException(exception);
				return result;
			}
			
			try{
				wait();
			} catch(InterruptedException ignore){
			}
		}
		
		return get();
	}

	@Override
	public SandboxedCallResult get(long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		synchronized (this) {
			if(isDone()){
				if(null != exception)
					throw new ExecutionException(exception);
				return result;
			}
			
			unit.timedWait(this, timeout);
		}
		
		return get();
	}

}
