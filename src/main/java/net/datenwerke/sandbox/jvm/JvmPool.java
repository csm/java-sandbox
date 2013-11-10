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

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import net.datenwerke.sandbox.SandboxedCallResult;

/**
 * A pool holding jvms for remote sandboxing. This can be regarded as an
 * analougue of a thread pool containing jvms instead of threads.
 * 
 * @author Arno Mittelbach
 *
 */
public interface JvmPool {

	/**
	 * Shutdown the jvm pool
	 */
	void shutdown();

	/**
	 * Returns true if the pool has been shutdown.
	 * @return
	 */
	boolean isShutdown();

	/**
	 * Adds a task to be remotely executed.
	 * 
	 * @param task
	 * @return
	 */
	Future<SandboxedCallResult> addTask(JvmTask task);

	/**
	 * Blocks to acquire a {@link JvmFreelancer}.
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	JvmFreelancer acquireFreelancer() throws InterruptedException;

	/**
	 * Blocks to acquire a {@link JvmFreelancer}. Throws {@link InterruptedException} if in the
	 * specified time no {@link JvmFreelancer} became available.
	 * 
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws InterruptedException
	 */
	JvmFreelancer acquireFreelancer(long timeout, TimeUnit unit) throws InterruptedException;

	/**
	 * Returns a {@link JvmFreelancer} to the pool
	 * @param freelancer
	 */
	void releaseFreelancer(JvmFreelancer freelancer);

	/**
	 * Restarts the pool
	 */
	void restart();


}
