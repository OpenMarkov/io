/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.probmodel;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.openmarkov.core.exception.ParserException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.StringWithProperties;
import org.openmarkov.core.test.TestSpeed;
import org.openmarkov.io.probmodel.reader.PGMXReader_0_2;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class AgentsTest {

    private static String rootPath;
    
	private static String probNetManualName = "test-decpomdp-manual.pgmx";
	
	private static ProbNet manualProbNet;
	
	/**
	 */
	@BeforeEach
	public void setUp() {
		URL url = getClass().getClassLoader ().getResource (probNetManualName);
		File file = new File(url.getPath());
		String absolutePath = file.getAbsolutePath();
		rootPath = absolutePath.substring(0, absolutePath.length() - probNetManualName.length());
	}
	
	@Tag(TestSpeed.MEDIUM)
	@Test
	public void testAgentsNumber() throws ParserException {
		manualProbNet = new PGMXReader_0_2().loadProbNet(rootPath + probNetManualName);
		List<StringWithProperties> agents = manualProbNet.getAgents();
		assertEquals(2, agents.size());
		StringWithProperties agent1 = agents.get(0);
		assertTrue(agent1.string.contentEquals("Agent 1"));
		StringWithProperties agent2 = agents.get(1);
		assertTrue(agent2.string.contentEquals("Agent 2"));
	}


}
