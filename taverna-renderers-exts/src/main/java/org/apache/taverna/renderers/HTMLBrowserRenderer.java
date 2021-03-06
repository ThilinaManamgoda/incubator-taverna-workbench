package org.apache.taverna.renderers;
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

import org.apache.taverna.renderers.Renderer;
import org.apache.taverna.renderers.RendererException;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JEditorPane;

import org.apache.log4j.Logger;

import org.apache.taverna.databundle.DataBundles;

/**
 * Web browser renderer for MIME type text/html.
 *
 * @author Peter Li
 */
public class HTMLBrowserRenderer implements Renderer {
	private Logger logger = Logger.getLogger(HTMLBrowserRenderer.class);
	private Pattern pattern;

	public HTMLBrowserRenderer() {
		pattern = Pattern.compile(".*text/html.*");
	}

	@Override
	public boolean canHandle(String mimeType) {
		return pattern.matcher(mimeType).matches();
	}

	public boolean isTerminal() {
		return true;
	}

	@Override
	public String getType() {
		return "HTML (in Web browser)";
	}

	@Override
	public JComponent getComponent(Path path) throws RendererException {
		URI uri = null;
		try {
			if (DataBundles.isValue(path)) {
				Path tempFile = Files.createTempFile(null, ".html");
				Files.copy(path, tempFile, StandardCopyOption.REPLACE_EXISTING);
				uri = tempFile.toUri();
			} else if (DataBundles.isReference(path)) {
				uri = DataBundles.getReference(path);
			}
		} catch (IOException e) {
			logger.error("Error fetching data value", e);
			return new JEditorPane("text/html", "Error fetching data value\n"
					+ e.toString());
		}

		// Start Web browser
		try {
			Desktop.getDesktop().browse(uri);
		} catch (IOException e) {
			logger.error("Error attempting to launch Web browser", e);
			return new JEditorPane("text/html", "Error attempting to launch Web browser\n"
					+ e.toString());

		}

		return new JEditorPane("text/plain", "Launching a Web browser ...");
	}

}
