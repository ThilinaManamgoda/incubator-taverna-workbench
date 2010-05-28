/*******************************************************************************
 * Copyright (C) 2009-2010 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.workbench.run.cleanup;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import net.sf.taverna.t2.workbench.ShutdownSPI;

/**
 * Shutdown hook that checks and waits until the current workflow run deletion
 * is completed before shutting down. (Anything remaining in the queue will be
 * scheduled for deletion next time Taverna starts by
 * {@link StoreRunIdsToDeleteLaterShutdownHook})
 * 
 * @author Alex Nenadic
 * @author Stian Soiland-Reyes
 * 
 */
public class RemoveDataflowRunsShutdownHook implements ShutdownSPI{

	public int positionHint() {
		// We need to finish before Reference Service is shutdown.
		return 300;
	}

	public boolean shutdown() {
		boolean shutdown = true;
		final DatabaseCleanup databaseCleanup = DatabaseCleanup.getInstance();

		// Stop please, we can delete the rest on startup
		databaseCleanup.requestStopDeletionThread();				
		
		// But if is stil active we should wait for it 
		if (databaseCleanup.isDeleteThreadAlive() && ! databaseCleanup.deletionQueue.isEmpty()) {
			final RemoveDataflowRunsShutdownDialog dialog = new RemoveDataflowRunsShutdownDialog();
			dialog.setInitialQueueSize(databaseCleanup.deletionQueue.size());

			Timer timer = new Timer(500, new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dialog.setCurrentQueueSize(databaseCleanup.deletionQueue
							.size());
					if (!databaseCleanup.isDeleteThreadAlive()
							|| databaseCleanup.deletionQueue.isEmpty()) {
						// thread has finished
						dialog.setVisible(false);
					}
				}
			});
			timer.start();
			dialog.setVisible(true);

			timer.stop();
			shutdown = dialog.confirmShutdown();
			dialog.dispose();
		}
		return shutdown;
		
	}

}