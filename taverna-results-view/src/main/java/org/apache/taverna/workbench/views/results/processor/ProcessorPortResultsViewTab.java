package org.apache.taverna.workbench.views.results.processor;
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

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;
import static java.awt.BorderLayout.WEST;
import static org.apache.taverna.workbench.views.results.processor.ProcessorResultTreeNode.ProcessorResultTreeNodeState.RESULT_REFERENCE;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.taverna.renderers.RendererRegistry;
import org.apache.taverna.workbench.views.results.processor.FilteredProcessorValueTreeModel.FilterType;
import org.apache.taverna.workbench.views.results.saveactions.SaveIndividualResultSPI;

/**
 * A tab containing result tree for an input or output port of a processor
 * and a panel with rendered result
 * of the currently selected node in the tree.
 *
 * @author Alex Nenadic
 *
 */
@SuppressWarnings("serial")
public class ProcessorPortResultsViewTab extends JPanel{
	/** Rendered result component */
	private RenderedProcessorResultComponent renderedResultComponent;
	private boolean isOutputPortTab = true;
	private JTree resultsTree;
	private String portName;
	/** Panel holding the results tree*/
	private JPanel treePanel;
	/**
	 * Split pane holding the result tree panel on the left and rendering
	 * component on the right
	 */
	private JSplitPane splitPanel;
    private JComboBox<FilterType> filterChoiceBox;
	private FilteredProcessorValueTreeModel filteredTreeModel;

	private final RendererRegistry rendererRegistry;
	private final List<SaveIndividualResultSPI> saveActions;

	public ProcessorPortResultsViewTab(String portName, RendererRegistry rendererRegistry, List<SaveIndividualResultSPI> saveActions) {
		this.portName = portName;
		this.rendererRegistry = rendererRegistry;
		this.saveActions = saveActions;
		initComponents();
	}

	private void initComponents() {
		setLayout(new BorderLayout());

		/*
		 * Split pane containing a tree with results from an output port for the
		 * selected enactment and rendered result component for rendering an
		 * individual result currently selected from the results tree.
		 */
		splitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

		/*
		 * Results tree (containing T2References to all individual results for
		 * this port)
		 */
		//resultsTree = new JTree(); // initially tree is empty - will be set when user selects a particular enactment

		// Component for rendering individual results
		renderedResultComponent = new RenderedProcessorResultComponent(
				rendererRegistry, saveActions);

		treePanel = new JPanel();
		treePanel.setLayout(new BorderLayout());
		treePanel.add(new JLabel("Click to view values"), NORTH);
		treePanel.add(new JScrollPane(resultsTree), CENTER);
		splitPanel.setTopComponent(treePanel);
		splitPanel.setBottomComponent(renderedResultComponent);
		splitPanel.setDividerLocation(400);

		// Add all to main panel
		add(splitPanel, CENTER);
	}

	public void setIsOutputPortTab(boolean isOutputPortTab) {
		this.isOutputPortTab  = isOutputPortTab;
	}

	public boolean getIsOutputPortTab() {
		return this.isOutputPortTab;
	}

    private List<TreePath> expandedPaths = new ArrayList<TreePath>();
    private TreePath selectionPath = null;

	private void rememberPaths() {
		expandedPaths.clear();
		for (Enumeration<?> e = resultsTree
				.getExpandedDescendants(new TreePath(filteredTreeModel
						.getRoot())); (e != null) && e.hasMoreElements();)
			expandedPaths.add((TreePath) e.nextElement());
		selectionPath = resultsTree.getSelectionPath();
	}

	private void reinstatePaths() {
		for (TreePath path : expandedPaths)
			if (filteredTreeModel.isShown((DefaultMutableTreeNode) path
					.getLastPathComponent()))
				resultsTree.expandPath(path);
		if (selectionPath != null) {
			if (filteredTreeModel
					.isShown((DefaultMutableTreeNode) selectionPath
							.getLastPathComponent()))
				resultsTree.setSelectionPath(selectionPath);
			else {
				resultsTree.clearSelection();
				renderedResultComponent.clearResult();
			}
		}
	}

	private void updateTree() {
		filteredTreeModel = (FilteredProcessorValueTreeModel) resultsTree
				.getModel();
		filteredTreeModel.setFilter((FilterType) filterChoiceBox.getSelectedItem());
		rememberPaths();
		filteredTreeModel.reload();
		resultsTree.setModel(filteredTreeModel);
		reinstatePaths();
	}

	public void setResultsTree(JTree tree) {
		resultsTree = tree;

		treePanel.removeAll();
		treePanel = new JPanel(new BorderLayout());
		if (tree == null) {
			splitPanel.setVisible(false);
			revalidate();
			return;
		}

		TreeModel treeModel = tree.getModel();
		int childCount = treeModel.getChildCount(treeModel.getRoot());
		if (childCount == 0) {
		    splitPanel.setVisible(false);
		    revalidate();
		    return;
		}

		splitPanel.setTopComponent(treePanel);
		splitPanel.setBottomComponent(renderedResultComponent);
		splitPanel.setVisible(true);

		JPanel treeSubPanel = new JPanel();
		treeSubPanel.setLayout(new BorderLayout());
		treeSubPanel.add(new JLabel("Click in tree to"), WEST);
		filterChoiceBox = new JComboBox<>(new FilterType[] { FilterType.ALL,
				FilterType.RESULTS, FilterType.ERRORS });
		filterChoiceBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateTree();
			}
		});
		treeSubPanel.add(filterChoiceBox);
		treePanel.add(treeSubPanel, NORTH);
		treePanel.add(new JScrollPane(resultsTree), CENTER);
		splitPanel.setTopComponent(treePanel);

		if (childCount == 1) {
			Object child = treeModel.getChild(treeModel.getRoot(), 0);
			ProcessorResultTreeNode node = (ProcessorResultTreeNode) child;
			if (node.getState() == RESULT_REFERENCE)
				if (treeModel.getChildCount(child) == 0) {
					TreePath path = new TreePath(new Object[] {
							treeModel.getRoot(), child });
					tree.setSelectionPath(path);
					splitPanel.setTopComponent(new JPanel());
					splitPanel.setDividerLocation(0);
				}
		}

		revalidate();
	}

	public void setPortName(String portName) {
		this.portName = portName;
	}

	public String getPortName() {
		return portName;
	}

	public RenderedProcessorResultComponent getRenderedResultComponent() {
		return renderedResultComponent;
	}
}
