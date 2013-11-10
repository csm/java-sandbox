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

import java.util.logging.Level;
import java.util.logging.Logger;



/**
 * 
 * @author Arno Mittelbach
 *
 */
public class SandboxMonitorWatchdog implements Runnable{

	private final Logger logger = Logger.getLogger(getClass().getName());

	private boolean shutdown = false;

	private SandboxServiceImpl sandboxService;
	
	protected long checkInterval = 1000*60;

	public SandboxMonitorWatchdog(SandboxServiceImpl sandboxService) {
		this.sandboxService = sandboxService;
	}
	
	@Override
	public void run() {
		while(! shutdown){
			checkDaemonState();
			
			try {
				Thread.sleep(checkInterval);
			} catch (InterruptedException e) {
			}
		}
	}

	private void checkDaemonState() {
		try{
			if(! sandboxService.isMonitorDaemonActive()){
				sandboxService.startMonitorDaemon();
				
				logger.log(Level.WARNING, "Restarted SandboxMonitorWatchdog.");
			}
		}catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	public void shutdown(){
		synchronized (SandboxMonitorWatchdog.class) {
			shutdown = true;			
		}
	}

	public boolean isShutdown() {
		synchronized (SandboxMonitorWatchdog.class) {
			return false;
		}
	}

	public void setCheckInterval(long checkInterval) {
		this.checkInterval = checkInterval;
	}

}
