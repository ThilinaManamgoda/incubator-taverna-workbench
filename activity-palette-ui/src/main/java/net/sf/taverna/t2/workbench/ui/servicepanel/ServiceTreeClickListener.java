/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester
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
package net.sf.taverna.t2.workbench.ui.servicepanel;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.lang.ui.ShadedLabel;
import net.sf.taverna.t2.servicedescriptions.ConfigurableServiceProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.ui.DataflowSelectionManager;
import net.sf.taverna.t2.workbench.ui.servicepanel.actions.ExportServiceDescriptionsAction;
import net.sf.taverna.t2.workbench.ui.servicepanel.actions.ImportServiceDescriptionsFromFileAction;
import net.sf.taverna.t2.workbench.ui.servicepanel.actions.ImportServiceDescriptionsFromURLAction;
import net.sf.taverna.t2.workbench.ui.servicepanel.actions.RemoveDefaultServicesAction;
import net.sf.taverna.t2.workbench.ui.servicepanel.actions.RemoveUserServicesAction;
import net.sf.taverna.t2.workbench.ui.servicepanel.actions.RestoreDefaultServicesAction;
import net.sf.taverna.t2.workbench.ui.servicepanel.tree.FilterTreeNode;
import net.sf.taverna.t2.workbench.ui.servicepanel.tree.FilterTreeSelectionModel;
import net.sf.taverna.t2.workbench.ui.servicepanel.tree.TreePanel;
import net.sf.taverna.t2.workbench.ui.workflowview.WorkflowView;
import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * @author alanrw
 *
 */
public class ServiceTreeClickListener extends MouseAdapter {

	private static Logger logger = Logger.getLogger(ServiceTreeClickListener.class);

	private JTree tree;
	private TreePanel panel;

	private final ServiceDescriptionRegistry serviceDescriptionRegistry;

	private final EditManager editManager;

	private final MenuManager menuManager;

	private final DataflowSelectionManager dataflowSelectionManager;

	public ServiceTreeClickListener(JTree tree, TreePanel panel,
			ServiceDescriptionRegistry serviceDescriptionRegistry, EditManager editManager,
			MenuManager menuManager, DataflowSelectionManager dataflowSelectionManager) {
		this.tree = tree;
		this.panel = panel;
		this.serviceDescriptionRegistry = serviceDescriptionRegistry;
		this.editManager = editManager;
		this.menuManager = menuManager;
		this.dataflowSelectionManager = dataflowSelectionManager;
	}

	private void handleMouseEvent(MouseEvent evt) {

		FilterTreeSelectionModel selectionModel = (FilterTreeSelectionModel) tree
				.getSelectionModel();
		// Discover the tree row that was clicked on
		int selRow = tree.getRowForLocation(evt.getX(), evt.getY());
		if (selRow != -1) {
			// Get the selection path for the row
			TreePath selectionPath = tree.getPathForLocation(evt.getX(), evt.getY());
			if (selectionPath != null) {
				// Get the selected node
				final FilterTreeNode selectedNode = (FilterTreeNode) selectionPath
						.getLastPathComponent();

				selectionModel.clearSelection();
				selectionModel.mySetSelectionPath(selectionPath);

				if (evt.isPopupTrigger()) {
					JPopupMenu menu = new JPopupMenu();
					Object selectedObject = selectedNode.getUserObject();
					logger.info(selectedObject.getClass().getName());
					if (!(selectedObject instanceof ServiceDescription)) {
						menu.add(new ShadedLabel("Tree", ShadedLabel.BLUE));
						menu.add(new JMenuItem(new AbstractAction("Expand all",
								WorkbenchIcons.plusIcon) {
							public void actionPerformed(ActionEvent evt) {
								SwingUtilities.invokeLater(new Runnable() {

									public void run() {
										panel.expandAll(selectedNode, true);
									}
								});
							}
						}));
						menu.add(new JMenuItem(new AbstractAction("Collapse all",
								WorkbenchIcons.minusIcon) {
							public void actionPerformed(ActionEvent evt) {
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										panel.expandAll(selectedNode, false);
									}

								});
							}
						}));
					}

					if (selectedObject instanceof ServiceDescription) {
						final ServiceDescription sd = (ServiceDescription) selectedObject;
						menu.add(new ShadedLabel(sd.getName(), ShadedLabel.ORANGE));
						menu.add(new AbstractAction("Add to workflow") {

							public void actionPerformed(ActionEvent e) {
								Dataflow currentDataflow = (Dataflow) ModelMap.getInstance()
										.getModel(ModelMapConstants.CURRENT_DATAFLOW);
								WorkflowView.importServiceDescription(sd, false, editManager, menuManager, dataflowSelectionManager);

							}

						});
						menu.add(new AbstractAction("Add to workflow with name...") {

							public void actionPerformed(ActionEvent e) {
								Dataflow currentDataflow = (Dataflow) ModelMap.getInstance()
										.getModel(ModelMapConstants.CURRENT_DATAFLOW);
								WorkflowView.importServiceDescription(sd, true, editManager, menuManager, dataflowSelectionManager);

							}

						});
					}

					Set<ServiceDescriptionProvider> providers = new HashSet<ServiceDescriptionProvider>();
					TreeMap<String, ServiceDescriptionProvider> nameMap = new TreeMap<String, ServiceDescriptionProvider>();

					if (selectedNode.isRoot()) {
						providers = serviceDescriptionRegistry.getServiceDescriptionProviders();
					} else {

						for (FilterTreeNode leaf : selectedNode.getLeaves()) {
							if (!leaf.isLeaf()) {
								logger.info("Not a leaf");
							}
							if (!(leaf.getUserObject() instanceof ServiceDescription)) {
								logger.info(leaf.getUserObject().getClass().getCanonicalName());
								logger.info(leaf.getUserObject().toString());
								continue;
							}
							providers.addAll(serviceDescriptionRegistry
									.getServiceDescriptionProviders((ServiceDescription) leaf
											.getUserObject()));
						}
					}
					for (ServiceDescriptionProvider sdp : providers) {
						nameMap.put(sdp.toString(), sdp);
					}
					boolean first = true;
					for (String name : nameMap.keySet()) {
						final ServiceDescriptionProvider sdp = nameMap.get(name);
						if (!(sdp instanceof ConfigurableServiceProvider)) {
							continue;
						}
						if (first) {
							menu.add(new ShadedLabel("Remove individual service provider",
									ShadedLabel.GREEN));
							first = false;
						}
						menu.add(new AbstractAction(name) {

							public void actionPerformed(ActionEvent e) {
								serviceDescriptionRegistry.removeServiceDescriptionProvider(sdp);
							}

						});
					}

					if (selectedNode.isRoot()) { // Root "Available services"
						menu.add(new ShadedLabel("Default and added service providers",
								ShadedLabel.ORANGE));
						menu.add(new RemoveUserServicesAction());
						menu.add(new RemoveDefaultServicesAction());
						menu.add(new RestoreDefaultServicesAction());

						menu.add(new ShadedLabel("Import/export services", ShadedLabel
								.halfShade(Color.RED)));
						menu.add(new ImportServiceDescriptionsFromFileAction());
						menu.add(new ImportServiceDescriptionsFromURLAction());
						menu.add(new ExportServiceDescriptionsAction());
					}

					menu.show(evt.getComponent(), evt.getX(), evt.getY());
				}
			}
		}
	}

	public void mousePressed(MouseEvent evt) {
		handleMouseEvent(evt);
	}

	public void mouseReleased(MouseEvent evt) {
		handleMouseEvent(evt);
	}
}
