package org.apache.taverna.workbench.models.graph.svg.event;
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

import org.apache.taverna.workbench.models.graph.GraphElement;

import org.apache.batik.dom.svg.SVGOMPoint;
import org.w3c.dom.events.MouseEvent;

/**
 * SVG event listener for handling mouse click events.
 * 
 * @author David Withers
 */
public class SVGMouseClickEventListener extends SVGEventListener {
	public SVGMouseClickEventListener(GraphElement graphElement) {
		super(graphElement);
	}

	@Override
	protected void event(SVGOMPoint point, MouseEvent evt) {
		graphElement.getEventManager().mouseClicked(graphElement,
				evt.getButton(), evt.getAltKey(), evt.getCtrlKey(),
				evt.getMetaKey(), (int) point.getX(), (int) point.getY(),
				evt.getScreenX(), evt.getScreenY());
	}
}
