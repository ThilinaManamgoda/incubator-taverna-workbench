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

import static org.apache.taverna.workbench.models.graph.svg.SVGUtil.screenToDocument;
import org.apache.taverna.workbench.models.graph.GraphElement;

import org.apache.batik.dom.svg.SVGOMPoint;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.MouseEvent;
import org.w3c.dom.svg.SVGLocatable;

/**
 * Abstract superclass for SVG event listeners.
 * 
 * @author David Withers
 */
public abstract class SVGEventListener implements EventListener {
	protected GraphElement graphElement;

	public SVGEventListener(GraphElement graphElement) {
		this.graphElement = graphElement;
	}

	protected abstract void event(SVGOMPoint point, MouseEvent evt);

	@Override
	public final void handleEvent(Event evt) {
		if (evt instanceof MouseEvent) {
			MouseEvent me = (MouseEvent) evt;
			SVGOMPoint point = screenToDocument((SVGLocatable) me.getTarget(),
					new SVGOMPoint(me.getClientX(), me.getClientY()));
			event(point, me);
			evt.stopPropagation();
		}
	}
}
