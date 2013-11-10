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

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.datenwerke.sandbox.SandboxedCallResult;
import net.datenwerke.sandbox.SandboxedCallResultImpl;
import net.datenwerke.sandbox.jvm.exceptions.JvmKilledUnsafeThreadException;
import net.datenwerke.sandbox.jvm.exceptions.JvmPoolInstantiationException;
import net.datenwerke.sandbox.jvm.exceptions.JvmServerDeadException;

/**
 * 
 * @author Arno Mittelbach
 *
 */
public class JvmPoolImpl implements JvmPool{

	private final Logger logger = Logger.getLogger(getClass().getName());
	
	private final int poolsize;
	private final int freelancerSize;
	private final BlockingDeque<JvmFuture> workQueue;
	private final JvmWorker[] jvms;
	
	private final JvmFreelancer[] freelancers;
	private final BlockingQueue<JvmFreelancer> freelancerQueue;
	
	private final JvmPoolConfig jvmConfig;
	private final String name;
	
	private boolean shutdown = false;

	private static int poolNumber = 1;
	
	public JvmPoolImpl(JvmPoolConfig jvmConfig){
		this.poolsize = jvmConfig.getPoolSize();
		this.freelancerSize = jvmConfig.getFreelancerSize();
		this.jvmConfig = jvmConfig;
		this.workQueue = new LinkedBlockingDeque<JvmFuture>();
		this.freelancerQueue = new LinkedBlockingQueue<JvmFreelancer>();
		this.name = initPoolName();
		
		/* install pool and freelancers */
		this.jvms = new JvmWorker[poolsize];
		this.freelancers = new JvmFreelancer[freelancerSize];
		
		init();
		
	}
	
	protected void init() {
		try{
			/* install pool */
			for(int i = 0; i < poolsize; i++){
				JvmWorker worker = new JvmWorker(jvmConfig);
				worker.setName(name + "-" + (i+1));
				worker.setDaemon(true);
				worker.start();
				jvms[i] = worker;
			}
			
			/* install freelancers */
			for(int i = 0; i < freelancerSize; i++){
				JvmFreelancer freelancer = new JvmFreelancer(jvmConfig);
				freelancers[i] = freelancer;
				freelancerQueue.add(freelancer);
			}
		} catch(Exception e){
			shutdown();
			throw new JvmPoolInstantiationException(e);
		}	
	}

	synchronized static String initPoolName(){
		int n = poolNumber++;
		return "JvmPool-" + n;
	}
	
	@Override
	public synchronized void shutdown(){
		shutdown = true;
		for(int i = 0; i < poolsize; i++){
			JvmWorker worker = jvms[i];
			if(worker == null)
				continue;
			try{
				worker.shutdown();
				worker.interrupt();
			} catch(Exception ignore){}
		}
		
		for(int i = 0; i < freelancerSize; i++){
			JvmFreelancer freeLancer = freelancers[i];
			if(freeLancer == null)
				continue;
			try{
				freeLancer.shutdown();
			} catch(Exception ignore){}
		}
		
		freelancerQueue.clear();
		
	}
	
	@Override
	public synchronized boolean isShutdown(){
		return shutdown;
	}
	
	@Override
	public void restart() {
		shutdown();
		shutdown = false;
		init();
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public JvmFreelancer acquireFreelancer() throws InterruptedException{
		return freelancerQueue.take();
	}
	
	@Override
	public JvmFreelancer acquireFreelancer(long timeout, TimeUnit unit) throws InterruptedException{
		return freelancerQueue.poll(timeout, unit);
	}
	
	@Override
	public void releaseFreelancer(JvmFreelancer freelancer){
		freelancer.released();
		freelancerQueue.add(freelancer);
	}
	
	@Override
	public Future<SandboxedCallResult> addTask(JvmTask task) {
		JvmFuture future = new JvmFuture(task);
		workQueue.addLast(future);
        
        return future;
    }
	
	protected void addTaskFirst(JvmFuture task) {
    	workQueue.addFirst(task);
    }
	
	/**
	 * 
	 * @author Arno Mittelbach
	 *
	 */
	private class JvmWorker extends Thread {

		private final JvmPoolConfig jvmConfig;
		private Jvm jvm; 
		private boolean shutdown = false;

		public JvmWorker(JvmPoolConfig jvmConfig) {
			this.jvmConfig = jvmConfig;
			
			jvm = jvmConfig.getInstantiator().spawnJvm();
		}

		public void shutdown() {
			shutdown = true;
			if(! isAlive())
				jvm.destroy();
		}
		
		public boolean isShutdown() {
			return shutdown;
		}

		public void restartJvm() {
			/* kill jvm */
			jvm.destroy();
			
			/* create new jvm */
			jvm = jvmConfig.getInstantiator().spawnJvm();
		}
		
		@Override
		public void run() {
			while(! shutdown){
				try {
					JvmFuture future = workQueue.take();
					
					if(null != future){
						try{
							JvmTask task = future.getTask();
							SandboxedCallResult result;
							if(null == task)
								result = new SandboxedCallResultImpl(null);
							else 
								result = jvm.execute(task);
							if(null == result)
								 result = new SandboxedCallResultImpl(null);
							
							future.setResult(result);
						} catch(JvmServerDeadException e){
							/* reinsert task */
							addTaskFirst(future);
							
							/* restart jvm */
							restartJvm();
						} catch(JvmKilledUnsafeThreadException e){
							logger.log(Level.WARNING, "kill jvm as unsafe thread was stopped.");
							
							future.setException(e);
							
							/* restart jvm */
							restartJvm();
						} catch(Exception e){
							future.setException(e);
						}
					}
				} catch (RuntimeException e) {
					logger.log(Level.SEVERE, "could not execute task", e);
				} catch (InterruptedException ignore) {
				}
			}
			
			/* kill jvm */
			jvm.destroy();
		}
	}

}
