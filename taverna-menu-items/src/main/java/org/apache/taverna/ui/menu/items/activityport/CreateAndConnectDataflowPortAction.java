package org.apache.taverna.ui.menu.items.activityport;
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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;

import org.apache.taverna.lang.ui.ValidatingUserInputDialog;
import org.apache.taverna.workbench.design.ui.DataflowInputPortPanel;
import org.apache.taverna.workbench.design.ui.DataflowOutputPortPanel;
import org.apache.taverna.workbench.edits.CompoundEdit;
import org.apache.taverna.workbench.edits.Edit;
import org.apache.taverna.workbench.edits.EditException;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workflow.edits.AddChildEdit;
import org.apache.taverna.workflow.edits.AddDataLinkEdit;
import org.apache.taverna.workflow.edits.AddWorkflowInputPortEdit;
import org.apache.taverna.workflow.edits.AddWorkflowOutputPortEdit;

import org.apache.log4j.Logger;

import org.apache.taverna.scufl2.api.core.DataLink;
import org.apache.taverna.scufl2.api.core.Workflow;
import org.apache.taverna.scufl2.api.port.DepthPort;
import org.apache.taverna.scufl2.api.port.InputPort;
import org.apache.taverna.scufl2.api.port.InputWorkflowPort;
import org.apache.taverna.scufl2.api.port.OutputPort;
import org.apache.taverna.scufl2.api.port.OutputWorkflowPort;
import org.apache.taverna.scufl2.api.port.Port;
import org.apache.taverna.scufl2.api.port.ReceiverPort;
import org.apache.taverna.scufl2.api.port.SenderPort;

/**
 * Action to create a dataflow input/output port and connect it to the specified
 * processor/activity output/input port.
 * <p>
 * The created dataflow port name will be taken from the name of the provided
 * port.
 *
 * @author Stian Soiland-Reyes
 *
 */
@SuppressWarnings("serial")
public class CreateAndConnectDataflowPortAction extends AbstractAction {

	private static final String VALID_PORT_NAME_REGEX = "[\\p{L}\\p{Digit}_.]+";
	private static final Dimension INPUT_PORT_DIALOGUE_SIZE = new Dimension(400, 250);
	private static final Dimension OUTPUT_PORT_DIALOGUE_SIZE = new Dimension(400, 200);

	private static final String INVALID_WORKFLOW_OUTPUT_PORT_NAME = "Invalid workflow output port name.";
	private static final String DUPLICATE_WORKFLOW_OUTPUT_PORT_NAME = "Duplicate workflow output port name.";
	private static final String SET_THE_WORKFLOW_OUTPUT_PORT_NAME = "Set the workflow output port name.";
	private static final String ADD_WORKFLOW_OUTPUT_PORT = "Add workflow output port";
	private static final String SET_THE_INPUT_PORT_LIST_DEPTH = "Set the input port list depth.";
	private static final String SET_THE_INPUT_PORT_TYPE = "Set the input port type.";
	private static final String INVALID_WORKFLOW_INPUT_PORT_NAME = "Invalid workflow input port name.";
	private static final String DUPLICATE_WORKFLOW_INPUT_PORT_NAME = "Duplicate workflow input port name.";
	private static final String SET_THE_WORKFLOW_INPUT_PORT_NAME = "Set the workflow input port name.";
	private static final String ADD_WORKFLOW_INPUT_PORT = "Add workflow input port";
	private static Logger logger = Logger.getLogger(CreateAndConnectDataflowPortAction.class);
	private final Workflow workflow;

	private final Port port;
	private final String suggestedName;
	private final Component parentComponent;
	private final EditManager editManager;

	/**
	 * Action for creating a Workflow input/output port and linking it to the
	 * specified port.
	 * <p>
	 * If the provided port is an InputPort then a
	 * Workflow OutputPort will be created and linked. Vice versa, if the
	 * provided port is an OutputPort, a Workflow InputPort will be created.
	 *
	 * @param workflow
	 *            Workflow where to create the Workflow input/output port
	 * @param port
	 *            Existing Processor port to connect to
	 * @param suggestedName
	 *            suggested port name
	 * @param parentComponent
	 *            Component to be parent of any pop-ups
	 */
	public CreateAndConnectDataflowPortAction(Workflow workflow, Port port,
			String suggestedName, Component parentComponent, EditManager editManager) {
		super("Connect to new workflow port");
		this.workflow = workflow;
		this.port = port;
		this.suggestedName = suggestedName;
		this.parentComponent = parentComponent;
		this.editManager = editManager;
		if (!(port instanceof InputPort || port instanceof OutputPort)) {
			throw new IllegalArgumentException("Port " + port
					+ " must be either an InputPort or OutputPort");
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (port instanceof ReceiverPort) {
			InputWorkflowPort inputWorkflowPort = new InputWorkflowPort();
			inputWorkflowPort.setName(suggestedName);
			workflow.getInputPorts().addWithUniqueName(inputWorkflowPort);
			workflow.getInputPorts().remove(inputWorkflowPort);
			if (port instanceof DepthPort) {
				inputWorkflowPort.setDepth(((DepthPort) port).getDepth());
			} else {
				inputWorkflowPort.setDepth(0);
			}
			showDialogue(inputWorkflowPort);

		} else if (port instanceof SenderPort) {
			OutputWorkflowPort outputWorkflowPort = new OutputWorkflowPort();
			outputWorkflowPort.setName(suggestedName);
			workflow.getOutputPorts().addWithUniqueName(outputWorkflowPort);
			workflow.getOutputPorts().remove(outputWorkflowPort);
			showDialogue(outputWorkflowPort);
		} else {
			throw new IllegalStateException("Port " + port
					+ " must be either an InputPort or OutputPort");
		}

	}

	protected void showDialogue(InputWorkflowPort portTemplate) {
		Set<String> usedInputPorts = new HashSet<String>();
		for (InputWorkflowPort usedInputPort : workflow.getInputPorts()) {
			usedInputPorts.add(usedInputPort.getName());
		}
		DataflowInputPortPanel inputPanel = new DataflowInputPortPanel();

		ValidatingUserInputDialog vuid = new ValidatingUserInputDialog(
				ADD_WORKFLOW_INPUT_PORT, inputPanel);
		vuid.addTextComponentValidation(inputPanel.getPortNameField(),
				SET_THE_WORKFLOW_INPUT_PORT_NAME, usedInputPorts,
				DUPLICATE_WORKFLOW_INPUT_PORT_NAME, VALID_PORT_NAME_REGEX,
				INVALID_WORKFLOW_INPUT_PORT_NAME);
		vuid.addMessageComponent(inputPanel.getSingleValueButton(),
				SET_THE_INPUT_PORT_TYPE);
		vuid.addMessageComponent(inputPanel.getListValueButton(),
				SET_THE_INPUT_PORT_LIST_DEPTH);
		vuid.setSize(INPUT_PORT_DIALOGUE_SIZE);

		inputPanel.setPortName(portTemplate.getName());
		inputPanel.setPortDepth(portTemplate.getDepth());

		if (vuid.show(parentComponent)) {
			InputWorkflowPort inputWorkflowPort = new InputWorkflowPort();
			inputWorkflowPort.setName(inputPanel.getPortName());
			inputWorkflowPort.setDepth(inputPanel.getPortDepth());
			List<Edit<?>> editList = new ArrayList<Edit<?>>();
			editList.add(new AddWorkflowInputPortEdit(workflow, inputWorkflowPort));
			DataLink dataLink = new DataLink();
			dataLink.setReceivesFrom(inputWorkflowPort);
			dataLink.setSendsTo((ReceiverPort) port);
			editList.add(new AddDataLinkEdit(workflow, dataLink));
			try {
				CompoundEdit compoundEdit = new CompoundEdit(editList);
				editManager.doDataflowEdit(workflow.getParent(), compoundEdit);
			} catch (EditException ex) {
				logger.warn("Can't create or connect new input port", ex);
			}

		}
	}

	protected void showDialogue(OutputWorkflowPort portTemplate) {
		Set<String> usedOutputPorts = new HashSet<String>();
		for (OutputWorkflowPort usedInputPort : workflow.getOutputPorts()) {
			usedOutputPorts.add(usedInputPort.getName());
		}
		DataflowOutputPortPanel outputPanel = new DataflowOutputPortPanel();

		ValidatingUserInputDialog vuid = new ValidatingUserInputDialog(
				ADD_WORKFLOW_OUTPUT_PORT, outputPanel);
		vuid.addTextComponentValidation(outputPanel.getPortNameField(),
				SET_THE_WORKFLOW_OUTPUT_PORT_NAME, usedOutputPorts,
				DUPLICATE_WORKFLOW_OUTPUT_PORT_NAME,
				VALID_PORT_NAME_REGEX, INVALID_WORKFLOW_OUTPUT_PORT_NAME);
		vuid.setSize(OUTPUT_PORT_DIALOGUE_SIZE);
		outputPanel.setPortName(portTemplate.getName());

		if (vuid.show(parentComponent)) {
			OutputWorkflowPort outputWorkflowPort = new OutputWorkflowPort();
			outputWorkflowPort.setName(outputPanel.getPortName());
			List<Edit<?>> editList = new ArrayList<Edit<?>>();
			editList.add(new AddWorkflowOutputPortEdit(workflow, outputWorkflowPort));
			DataLink dataLink = new DataLink();
			dataLink.setReceivesFrom((SenderPort) port);
			dataLink.setSendsTo(outputWorkflowPort);
			editList.add(new AddDataLinkEdit(workflow, dataLink));
			try {
				CompoundEdit compoundEdit = new CompoundEdit(editList);
				editManager.doDataflowEdit(workflow.getParent(), compoundEdit);
			} catch (EditException ex) {
				logger.warn("Can't create or connect new workflow output port", ex);
			}
		}
	}

}
