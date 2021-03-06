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

package org.apache.taverna.workbench.ui.zaria;

import javax.swing.ImageIcon;

/**
 * Interface for any UI component to be used within the workbench
 * @author Tom Oinn
 */
public interface UIComponentSPI {

	/**
	 * Get the preferred name of this component, for titles in windows etc.
	 */
	public String getName();

	/**
	 * Get an icon to be used in window decorations for this component.
	 */
	public ImageIcon getIcon();

	/**
	 * Called when the component is displayed in the UI
	 */
	public void onDisplay();
	
	/**
	 * Called after the component has been removed from the UI
	 */
	public void onDispose();
	
}
