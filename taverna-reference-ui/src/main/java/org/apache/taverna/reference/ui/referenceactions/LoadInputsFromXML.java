package org.apache.taverna.reference.ui.referenceactions;
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

import static javax.swing.JFileChooser.APPROVE_OPTION;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.xmlNodeIcon;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import org.apache.taverna.lang.ui.ExtensionFileFilter;
import org.apache.taverna.reference.ui.RegistrationPanel;

import org.jdom.Document;
import org.jdom.input.SAXBuilder;

/**
 * Loads a set of input values from an XML document
 */
public class LoadInputsFromXML extends AbstractAction implements
		ReferenceActionSPI {
	private static final long serialVersionUID = -5031867688853589341L;
	private static final String INPUT_DATA_DIR_PROPERTY = "inputDataValuesDir";

	private Map<String, RegistrationPanel> inputPanelMap;

	public LoadInputsFromXML() {
		super();
		putValue(NAME, "Load previous values");
		putValue(SMALL_ICON, xmlNodeIcon);
	}

	@Override
	public AbstractAction getAction() {
		return new LoadInputsFromXML();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Preferences prefs = Preferences.userNodeForPackage(getClass());
		String curDir = prefs.get(INPUT_DATA_DIR_PROPERTY, System.getProperty("user.home"));

		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Select file to load input values from");

		chooser.resetChoosableFileFilters();
		chooser.setFileFilter(new ExtensionFileFilter(new String[]{"xml"}));
		chooser.setCurrentDirectory(new File(curDir));

		if (chooser.showOpenDialog(null) != APPROVE_OPTION)
			return;
		prefs.put(INPUT_DATA_DIR_PROPERTY, chooser.getCurrentDirectory()
				.toString());
		try {
			File file = chooser.getSelectedFile();
			InputStreamReader stream;
			stream = new InputStreamReader(new FileInputStream(file),
					Charset.forName("UTF-8"));
			Document inputDoc = new SAXBuilder(false).build(stream);
			Map<String, DataThing> inputMap = DataThingXMLFactory.parseDataDocument(inputDoc);
			for (String portName : inputMap.keySet()) {
				RegistrationPanel panel = inputPanelMap.get(portName);
				Object o = inputMap.get(portName).getDataObject();
				if (o != null) {
					int objectDepth = getObjectDepth(o);
					if ((panel != null) && (objectDepth <= panel.getDepth()))
						panel.setValue(o, objectDepth);
				}
			}
		} catch (Exception ex) {
			// Nothing
		}
	}

	@Override
	public void setInputPanelMap(Map<String, RegistrationPanel> inputPanelMap) {
		this.inputPanelMap = inputPanelMap;
	}

	private int getObjectDepth(Object o) {
		int result = 0;
		if (o instanceof Iterable) {
			result++;
			@SuppressWarnings("unchecked")
			Iterator<Object> i = ((Iterable<Object>) o).iterator();
			if (i.hasNext())
				result = result + getObjectDepth(i.next());
		}
		return result;
	}
}
