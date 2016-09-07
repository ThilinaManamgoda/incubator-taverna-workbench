/*
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 * Source code information
 * -----------------------
 * Filename           $RCSfile: UpdatesAvailableIcon.java,v $
 * Revision           $Revision: 1.5 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2008/12/01 12:32:40 $
 *               by   $Author: alaninmcr $
 * Created on 12 Dec 2006
package org.apache.taverna.raven.plugins.ui;
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;


import org.apache.log4j.Logger;

/**
 * A JLabel that periodically checks for updates, running on a daemon thread. If
 * updates are available it makes itself visible and responds to click events to
 * display the appropriate update response.
 *
 * Also acts as a pluginmanager listener to refresh itself whenever a new plugin
 * is added.
 *
 * @author Stuart Owen
 *
 */

@SuppressWarnings("serial")
public class UpdatesAvailableIcon extends JLabel implements PluginManagerListener {

	private UpdatePluginsMouseAdaptor updatePluginMouseAdaptor = new UpdatePluginsMouseAdaptor();
	private static Logger logger = Logger.getLogger(UpdatesAvailableIcon.class);

	private final int CHECK_INTERVAL = 1800000; // every 30 minutes

	public static final Icon updateIcon = new ImageIcon(
			UpdatesAvailableIcon.class.getResource("update.png"));
	public static final Icon updateRecommendedIcon = new ImageIcon(
			UpdatesAvailableIcon.class.getResource("updateRecommended.png"));

	public UpdatesAvailableIcon() {
		super();
		setVisible(false);

		startCheckThread();
		PluginManager.addPluginManagerListener(this);
	}

	public void pluginAdded(PluginManagerEvent event) {
		logger.info("Plugin Added");
		if (!isVisible())
			checkForUpdates();
	}

	public void pluginStateChanged(PluginManagerEvent event) {

	}

	public void pluginUpdated(PluginManagerEvent event) {
		logger.info("Plugin Updated");
	}

	public void pluginRemoved(PluginManagerEvent event) {
		logger.info("Plugin Removed");
		if (isVisible())
			checkForUpdates();
	}

	public void pluginIncompatible(PluginManagerEvent event) {
		logger
				.warn("Plugin found to be incompatible with the current version of Taverna: "
						+ event.getPlugin());
	}

	private void startCheckThread() {
		Thread checkThread = new Thread("Check for updates thread") {

			@Override
			public void run() {
				while (true) {
					try {
						checkForUpdates();
						Thread.sleep(CHECK_INTERVAL);
					} catch (InterruptedException e) {
						logger.warn("Interruption exception in checking for updates thread",
										e);
					}
				}
			}
		};
		checkThread.setDaemon(true); // daemon so that taverna will stop the
										// thread and close on exit.
		checkThread.start();
	}

	private Object updateLock = new Object();

	private void checkForUpdates() {
		synchronized (updateLock) {
			if (pluginUpdateAvailable()) {
				logger.info("Plugin update available");
				try {
					SwingUtilities.invokeAndWait(new Runnable() {
						public void run() {
							// TODO Auto-generated method stub
							setToolTipText("Plugin updates are available");

							setVisible(true);
							setIcon(updateIcon);
							if (!Arrays.asList(getMouseListeners()).contains(
									updatePluginMouseAdaptor)) {
								addMouseListener(updatePluginMouseAdaptor);
							}

						}
					});
				} catch (InterruptedException e) {
					logger.error("Could not check for updates", e);
				} catch (InvocationTargetException e) {
					logger.error("Could not check for updates", e);
				}
			} else {
				setToolTipText("");
				setVisible(false);

			}
		}

	}

	private boolean pluginUpdateAvailable() {
		return PluginManager.getInstance().checkForUpdates();
	}

	private final class UpdatePluginsMouseAdaptor extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
			// FIXME: this assumes the button is on the toolbar.
			Component parent = UpdatesAvailableIcon.this.getParent()
					.getParent();

			final PluginManagerFrame pluginManagerUI = new PluginManagerFrame(
					PluginManager.getInstance());
			pluginManagerUI.setLocationRelativeTo(parent);
			pluginManagerUI.setVisible(true);
		}

	}

}
