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
/*

package org.apache.taverna.ui.perspectives.results;

import java.awt.Component;
import java.text.SimpleDateFormat;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.apache.taverna.platform.report.WorkflowReport;
import org.apache.taverna.platform.run.api.InvalidRunIdException;
import org.apache.taverna.platform.run.api.RunService;

/**
 * @author David Withers
 */
public class WorkflowRunListCellRenderer extends JLabel implements ListCellRenderer<String> {
	private static final long serialVersionUID = -4954451814016664554L;
	private static final SimpleDateFormat ISO_8601 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private final RunService runService;

	public WorkflowRunListCellRenderer(RunService runService) {
		this.runService = runService;
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends String> list, String value,
			int index, boolean isSelected, boolean cellHasFocus) {
		try {
			WorkflowReport workflowReport = runService.getWorkflowReport(value);
			setText(workflowReport.getSubject().getName() + " "
					+ ISO_8601.format(workflowReport.getCreatedDate()));
		} catch (InvalidRunIdException e) {
			setText(value);
		}
		return this;
	}
}
