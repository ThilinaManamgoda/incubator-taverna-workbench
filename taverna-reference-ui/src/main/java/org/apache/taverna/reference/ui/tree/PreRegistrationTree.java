package org.apache.taverna.reference.ui.tree;
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

import static javax.swing.SwingUtilities.invokeLater;
import static javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION;

import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.Autoscroll;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

/**
 * A wrapper around JTree that installs the set of models, renderers and
 * listeners used by the pre-registration tree control. Implements autoscroll
 * and zooms to any new nodes when added. Handles drop of URLs (from e.g.
 * FireFox), File structures and plain text by creating corresponding POJOs.
 * 
 * @author Tom Oinn
 */
public class PreRegistrationTree extends JTree implements Autoscroll {
	private static final long serialVersionUID = -8357524058131749686L;
	private static Logger logger = Logger.getLogger(PreRegistrationTree.class);

	private PreRegistrationTreeModel model;
	private int margin = 15;

	/**
	 * Get the PreRegistrationTreeModel for this tree. Used to get the contents
	 * of the tree as a POJO which can then be registered with the
	 * ReferenceService
	 * 
	 * @return a POJO containing the contents of the tree.
	 */
	public PreRegistrationTreeModel getPreRegistrationTreeModel() {
		return this.model;
	}

	/**
	 * Override this to be informed of status messages from the tree
	 */
	public void setStatusMessage(String message, boolean isError) {
		//
	}

	/**
	 * Construct with the depth of the collection to be assembled. This will
	 * instantiate an appropriate internal model and set all the drag and drop
	 * handlers, renderers and cell editing components.
	 * 
	 * @param depth
	 *            the collection depth to use, 0 for single items, 1 for lists
	 *            and so on.
	 */
	public PreRegistrationTree(int depth) {
		this(depth, null);
	}

	/**
	 * Construct with the depth of the collection to be assembled. This will
	 * instantiate an appropriate internal model and set all the drag and drop
	 * handlers, renderers and cell editing components.
	 *
	 * @param depth
	 *            the collection depth to use, 0 for single items, 1 for lists
	 *            and so on.
	 * @param name Name of the top root of the tree (typically the port name)
	 */
	public PreRegistrationTree(int depth, String name) {
		if (name == null)
			model = new PreRegistrationTreeModel(depth);
		else
			model = new PreRegistrationTreeModel(depth, name);
		setModel(model);
		setInvokesStopCellEditing(true);

		getSelectionModel().setSelectionMode(SINGLE_TREE_SELECTION);
		DefaultTreeCellRenderer renderer = new PreRegistrationTreeCellRenderer();
		setRowHeight(0);
		setCellRenderer(renderer);

		new PreRegistrationTreeDnDHandler(this) {
			@Override
			public void handleNodeMove(MutableTreeNode source,
					MutableTreeNode target) {
				model.moveNode(source, target);
			}

			@Override
			public void handleFileDrop(MutableTreeNode target,
					List<File> fileList) {
				for (File f : fileList) {
					if (f.isDirectory() == false) {
						model.addPojoStructure(target, target, f, 0);
						continue;
					}

					if (model.getDepth() < 1) {
						setStatusMessage(
								"Can't add directory to single item input",
								true);
						return;
					}

					/*
					 * Try to handle directories as flat lists, don't nest any
					 * deeper for now.
					 */
					List<File> children = new ArrayList<>();
					for (File child : f.listFiles())
						if (child.isFile())
							children.add(child);
					model.addPojoStructure(target, target, children, 1);
				}
			}

			@Override
			public void handleUrlDrop(MutableTreeNode target, URL url) {
				if (url.getProtocol().equalsIgnoreCase("http")) {
					model.addPojoStructure(target, target, url, 0);
					setStatusMessage("Added URL : " + url.toExternalForm(),
							false);
				} else
					setStatusMessage("Only http URLs are supported for now.",
							true);
			}

			@Override
			public void handleStringDrop(MutableTreeNode target, String string) {
				model.addPojoStructure(target, target, string, 0);
				setStatusMessage("Added string from drag and drop", false);
			}
		};
	}

	@Override
	public void setModel(TreeModel model) {
		if (treeModel == model)
			return;
		if (treeModelListener == null)
			treeModelListener = new TreeModelHandler() {
				@Override
				public void treeNodesInserted(final TreeModelEvent ev) {
					invokeLater(new Runnable() {
						@Override
						public void run() {
							TreePath path = ev.getTreePath();
							setExpandedState(path, true);
							fireTreeExpanded(path);
						}
					});
				}
			};
		if (model != null)
			model.addTreeModelListener(treeModelListener);
		TreeModel oldValue = treeModel;
		treeModel = model;
		firePropertyChange(TREE_MODEL_PROPERTY, oldValue, model);
	}

	@Override
	public void autoscroll(Point p) {
		int realrow = getRowForLocation(p.x, p.y);
		Rectangle outer = getBounds();
		realrow = (p.y + outer.y <= margin ? realrow < 1 ? 0 : realrow - 1
				: realrow < getRowCount() - 1 ? realrow + 1 : realrow);
		scrollRowToVisible(realrow);
	}

	@Override
	public Insets getAutoscrollInsets() {
		Rectangle outer = getBounds();
		Rectangle inner = getParent().getBounds();
		return new Insets(inner.y - outer.y + margin, inner.x - outer.x
				+ margin, outer.height - inner.height - inner.y + outer.y
				+ margin, outer.width - inner.width - inner.x + outer.x
				+ margin);
	}

	@Override
	public int getRowCount() {
		int result = super.getRowCount();
		logger.info("Row count is " + result);
		return result;
	}
}
