/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester
 * 
 * Modifications to the initial code base are copyright of their respective
 * authors, or their employers as appropriate.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.ui.perspectives.myexperiment;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.ui.perspectives.PerspectiveRegistry;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Base64;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.MyExperimentClient;
import net.sf.taverna.t2.workbench.ShutdownSPI;
import net.sf.taverna.t2.workbench.ui.zaria.PerspectiveSPI;

/**
 * @author Sergejs Aleksejevs, Jiten Bhagat
 */

public class MainComponentShutdownHook implements ShutdownSPI {
  private MainComponent pluginMainComponent;
  private MyExperimentClient myExperimentClient;
  private Logger logger;

  public int positionHint() {
	// all custom plugins are suggested to return a value of > 100;
	// this affects when in the termination process will this plugin
	// be shutdown;
	return 100;
  }

  public boolean shutdown() {
	// find instance of main component of the running myExperiment perspective
	MainComponent mainComponent = null;
	for (PerspectiveSPI perspective : PerspectiveRegistry.getInstance().getPerspectives()) {
	  if (perspective.getText().equals(MyExperimentPerspective.PERSPECTIVE_NAME)) {
		mainComponent = ((MyExperimentPerspective) perspective).getMainComponent();
		break;
	  }
	}

	// if myExperiment perspective wasn't initialised, no shutdown operations are required / possible
	if (mainComponent != null) {
	  this.setLinks(mainComponent, mainComponent.getMyExperimentClient(), mainComponent.getLogger());
	  new MyExperimentClientShutdownThread().start();
	}

	// "true" means that shutdown operations are complete and Taverna can terminate
	return true;
  }

  /**
   * Sets up links of this class with the rest of the plugin.
   */
  public void setLinks(MainComponent component, MyExperimentClient client, Logger logger) {
	this.pluginMainComponent = component;
	this.myExperimentClient = client;
	this.logger = logger;
  }

  /**
   * Actual shutdown cleaning up, saving settings and flushing caches.
   */
  // ************** CLEANUP THREAD *****************
  protected class MyExperimentClientShutdownThread extends Thread {
	public void run() {
	  this.setName("myExperiment Plugin shutdown thread");
	  logger.debug("Starting shutdown operations for myExperiment plugin");

	  try {
		myExperimentClient.getSettings().put(MyExperimentClient.INI_FAVOURITE_SEARCHES, Base64.encodeObject(pluginMainComponent.getSearchTab().getSearchFavouritesList()));
		myExperimentClient.getSettings().put(MyExperimentClient.INI_SEARCH_HISTORY, Base64.encodeObject(pluginMainComponent.getSearchTab().getSearchHistory()));
		myExperimentClient.getSettings().put(MyExperimentClient.INI_TAG_SEARCH_HISTORY, Base64.encodeObject(pluginMainComponent.getTagBrowserTab().getTagSearchHistory()));
		myExperimentClient.getSettings().put(MyExperimentClient.INI_PREVIEWED_ITEMS_HISTORY, Base64.encodeObject(pluginMainComponent.getPreviewBrowser().getPreviewHistory()));
		myExperimentClient.getSettings().put(MyExperimentClient.INI_DOWNLOADED_ITEMS_HISTORY, Base64.encodeObject(pluginMainComponent.getHistoryBrowser().getDownloadedItemsHistoryList()));
		myExperimentClient.getSettings().put(MyExperimentClient.INI_OPENED_ITEMS_HISTORY, Base64.encodeObject(pluginMainComponent.getHistoryBrowser().getOpenedItemsHistoryList()));
		myExperimentClient.getSettings().put(MyExperimentClient.INI_COMMENTED_ITEMS_HISTORY, Base64.encodeObject(pluginMainComponent.getHistoryBrowser().getCommentedOnItemsHistoryList()));

		myExperimentClient.storeSettings();
	  } catch (Exception e) {
		logger.error("Failed while serializing myExperiment plugin settings:\n"
			+ e);
		e.printStackTrace();
	  }

	  logger.debug("myExperiment plugin shutdown is completed; terminated...");
	}
  }

}
