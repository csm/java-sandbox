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

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.datenwerke.sandbox.exception.SandboxedTaskKilledException;

/**
 * Daemon thread that is used to monitor running sandboxes.
 * 
 * @author Arno Mittelbach
 *
 */
public class SandboxMonitorDaemon implements Runnable {

	private final Logger logger = Logger.getLogger(getClass().getName());
	
	protected boolean shutdown;
	protected boolean terminated;
	
	private final ThreadMXBean threadBean;
	private final SandboxServiceImpl sandboxService;
	private final ConcurrentLinkedQueue<SandboxMonitoredThread> monitorQueue;

	private long startTime;

	private long checkInterval;

	public SandboxMonitorDaemon(SandboxServiceImpl sandboxService, ConcurrentLinkedQueue<SandboxMonitoredThread> monitorQueue) {
		this.sandboxService = sandboxService;
		this.monitorQueue = monitorQueue;
		this.threadBean = ManagementFactory.getThreadMXBean();
	}
	
	@Override
	public void run() {
		while(!shutdown){
			try{
				Iterator<SandboxMonitoredThread> iterator = monitorQueue.iterator();
				while(iterator.hasNext()){
					SandboxMonitoredThread monitor = iterator.next();
					if(null == monitor)
						continue;
					
					if(! monitor.isAlive()){
						iterator.remove();
						continue;
					}

					testStack(monitor);
					testRuntime(monitor);
				}
				
				try {
					Thread.sleep(checkInterval);
				} catch (InterruptedException e) {
					logger.log(Level.WARNING, "SandboxMonitor was interrupted", e);
				}
				
			} catch(Exception e){
				logger.log(Level.SEVERE, "Exception SandboxMonitorDaemon: ", e);
			}
		}
	}

	protected void testRuntime(SandboxMonitoredThread monitor) {
		SandboxContext context = monitor.getContext();
		if(0 > context.getMaximumRunTime() || null == context.getMaximumRunTimeUnit())
			return;
		if(context.getMaximumRuntimeMode() == SandboxContext.RuntimeMode.CPU_TIME){
			long cpuTime = threadBean.getThreadCpuTime(monitor.getMonitoredThread().getId());
			if(cpuTime > TimeUnit.NANOSECONDS.convert(context.getMaximumRunTime(), context.getMaximumRunTimeUnit()))
				suspend(monitor, new SandboxedTaskKilledException("killed task as maxmimum runtime was exceeded"));
	 	} else {
	 		if(System.currentTimeMillis() - startTime > TimeUnit.MILLISECONDS.convert(context.getMaximumRunTime(), context.getMaximumRunTimeUnit()))
	 			suspend(monitor, new SandboxedTaskKilledException("killed task as maxmimum runtime was exceeded"));
	 	}
	}

	protected void testStack(SandboxMonitoredThread monitor) {
		SandboxContext context = monitor.getContext();
		if(0 > context.getMaximumStackDepth())
			return;
		
		ThreadInfo threadInfo = threadBean.getThreadInfo(monitor.getMonitoredThread().getId(), context.getMaximumStackDepth());
		if(threadInfo.getStackTrace().length == context.getMaximumStackDepth())
			suspend(monitor, new SandboxedTaskKilledException("killed task as stack depth exceeded maximum"));
	}
	
	protected void suspend(SandboxMonitoredThread monitor, SandboxedTaskKilledException exception) {
		monitorQueue.remove(monitor);
		sandboxService.kill(monitor, exception);
	}

	public synchronized void shutdown(){
		shutdown = true;
	}
	
	public boolean isShutdown() {
		return shutdown;
	}

	public boolean isTerminated() {
		return terminated;
	}

	public void setCheckInterval(long checkInterval) {
		this.checkInterval = checkInterval;
	}
	
}
