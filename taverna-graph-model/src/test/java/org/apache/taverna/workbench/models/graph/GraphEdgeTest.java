package org.apache.taverna.workbench.models.graph;
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

import org.apache.taverna.workbench.models.graph.GraphNode;
import org.apache.taverna.workbench.models.graph.GraphEdge;
import org.apache.taverna.workbench.models.graph.GraphController;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.apache.taverna.workbench.models.graph.GraphEdge.ArrowStyle;

import org.junit.Before;
import org.junit.Test;

public class GraphEdgeTest {

	private GraphEdge edge;

	private GraphNode source;

	private GraphNode sink;

	private ArrowStyle arrowHeadStyle;

	private ArrowStyle arrowTailStyle;

	private GraphController graphController;

	@Before
	public void setUp() throws Exception {
		source = new GraphNode(graphController);
		sink = new GraphNode(graphController);
		arrowHeadStyle = ArrowStyle.DOT;
		arrowTailStyle = ArrowStyle.NORMAL;
		edge = new GraphEdge(graphController);
		edge.setArrowHeadStyle(arrowHeadStyle);
		edge.setArrowTailStyle(arrowTailStyle);
		edge.setSink(sink);
		edge.setSource(source);
	}

	@Test
	public void testEdge() {
		edge = new GraphEdge(graphController);
		assertNull(edge.getSource());
		assertNull(edge.getSink());
		assertNull(edge.getLabel());
	}

	@Test
	public void testEdgeNodeNode() {
		edge = new GraphEdge(graphController);
		edge.setSource(source);
		edge.setSink(sink);
		assertEquals(source, edge.getSource());
		assertEquals(sink, edge.getSink());
		assertNull(edge.getLabel());
	}

	@Test
	public void testGetSource() {
		assertEquals(source, edge.getSource());
	}

	@Test
	public void testSetSource() {
		GraphNode node = new GraphNode(graphController);
		edge.setSource(node);
		assertEquals(node, edge.getSource());
		edge.setSource(null);
		assertNull(edge.getSource());
	}

	@Test
	public void testGetSink() {
		assertEquals(sink, edge.getSink());
	}

	@Test
	public void testSetSink() {
		GraphNode node = new GraphNode(graphController);
		edge.setSink(node);
		assertEquals(node, edge.getSink());
		edge.setSink(null);
		assertNull(edge.getSink());
	}

	@Test
	public void testGetArrowHeadStyle() {
		assertEquals(arrowHeadStyle, edge.getArrowHeadStyle());
	}

	@Test
	public void testSetArrowHeadStyle() {
		edge.setArrowHeadStyle(ArrowStyle.DOT);
		assertEquals(ArrowStyle.DOT, edge.getArrowHeadStyle());
		edge.setArrowHeadStyle(null);
		assertNull(edge.getArrowHeadStyle());
	}

	@Test
	public void testGetArrowTailStyle() {
		assertEquals(arrowTailStyle, edge.getArrowTailStyle());
	}

	@Test
	public void testSetArrowTailStyle() {
		edge.setArrowTailStyle(ArrowStyle.NORMAL);
		assertEquals(ArrowStyle.NORMAL, edge.getArrowTailStyle());
		edge.setArrowTailStyle(null);
		assertNull(edge.getArrowTailStyle());
	}

}
