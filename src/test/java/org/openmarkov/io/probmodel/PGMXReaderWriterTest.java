/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.probmodel;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.openmarkov.core.exception.ParserException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
//import org.openmarkov.core.model.network.ProbNetTest;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.canonical.MaxPotential;
import org.openmarkov.core.model.network.potential.canonical.TuningPotential;
import org.openmarkov.core.model.network.type.DecisionAnalysisNetworkType;
import org.openmarkov.io.probmodel.reader.PGMXReader_0_2;
import org.openmarkov.io.probmodel.writer.PGMXWriter_0_2;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class PGMXReaderWriterTest {

    private String rootPath;
    private PGMXWriter_0_2 writer;
    private PGMXReader_0_2 reader;
    
	private String networkTestName = "HPV-model-0.2.0.pgmx";

	@BeforeEach
    public void setUp() throws Exception {
    	URL url = getClass().getClassLoader ().getResource (networkTestName);
		File file = new File(url.getPath());
		String absolutePath = file.getAbsolutePath();
		rootPath = absolutePath.substring(0, absolutePath.length() - networkTestName.length());
		writer = new PGMXWriter_0_2();
		reader = new PGMXReader_0_2();
    }

	//TODO fix this
	/*
	@Disabled("Temporarily disabled as it uses tests classes from openmarkov.core (ProbNetTest)")
	@Test
	public void readWriteNetwork() {
		ProbNet trivialNet = Util.createTrivialID();
		
		// Write test network
		String testNetworkName =  rootPath +  "test.pgmx";
		try {
			writer.writeProbNet(testNetworkName, trivialNet);
		} catch (WriterException e) {
			System.err.println(e.getMessage());
			System.err.println(e.getStackTrace());
			fail("Can not write network.");
		}
		
		// Read the preceding network
		ProbNet trivialNetRead = null;
		try {
			trivialNetRead = reader.loadProbNet(testNetworkName);
		} catch (ParserException e) {
			System.err.println(e.getMessage());
			System.err.println(e.getStackTrace());
			fail("Can not read network:" + e.getLocalizedMessage ());
		}
		
		// Compare both networks
		ProbNetTest.compareNetworks(trivialNet, trivialNetRead);
	}*/

	//TODO fix this
	/*
	@Disabled("Ignored because a deprecated network")
	@Test
	public void readHPVNetwork() { 
		ProbNet probNet1;
		try {
			String pathAndName = rootPath + networkTestName;
			probNet1 = reader.loadProbNet(pathAndName);
			assertNotNull(probNet1);
			String testNetwork = "/test-" + networkTestName;
			writer.writeProbNet(rootPath +  testNetwork, probNet1);
			ProbNet probNet2 = reader.loadProbNet(rootPath +  testNetwork);
			ProbNetTest.compareNetworks(probNet1, probNet2);
		} catch (ParserException e) {
			fail("Parser exception: " + e.getLocalizedMessage());
		} catch (WriterException e) {
			fail("Writer exception: " + e.getLocalizedMessage());
		}
	}*/
	
	@Test
    public void iciPotentialsReadingTest() throws NodeNotFoundException {
        
        // Read test network
        String testNetworkName = rootPath + "/test-ici-reading.pgmx";
        
        ProbNet readProbNet = null;
        try {
            readProbNet = reader.loadProbNet(testNetworkName);
        } catch (ParserException e) {
			fail("Can not read network:" + e.getLocalizedMessage ());
        }
        
        Assertions.assertNotNull(readProbNet);
        Assertions.assertEquals(9, readProbNet.getNumNodes());
        Assertions.assertEquals(1, readProbNet.getNode("I").getNumPotentials());
        Assertions.assertTrue(readProbNet.getNode("I").getPotentials().get(0) instanceof TuningPotential);
        Assertions.assertEquals(1, readProbNet.getNode("H").getNumPotentials());
        Assertions.assertTrue(readProbNet.getNode("H").getPotentials().get(0) instanceof MaxPotential);
    }

	//TODO fix this
	/*
	@Disabled("Temporarily disabled as it uses tests classes from openmarkov.core (ProbNetTest)")
	@Test
    public void iciPotentialsWritingTest() {
        ProbNet probNet = Util.buildNetWithICI ();
        
        // Write test network
        String testNetworkName = rootPath +  "/test-ici.pgmx";
        try {
            writer.writeProbNet(testNetworkName, probNet);
        } catch (WriterException e) {
			System.err.println(e.getMessage());
            fail("Can not write network.");
        }
        
        // Read the preceding network
        ProbNet readProbNet = null;
        try {
            readProbNet = reader.loadProbNet(testNetworkName);
        } catch (ParserException e) {
			fail("Can not read network:" + e.getLocalizedMessage ());
        }
        
        // Compare both networks
        ProbNetTest.compareNetworks(probNet, readProbNet);        
    }*/

	//TODO fix this
	/*
	@Disabled("Temporarily disabled as it uses tests classes from openmarkov.core (ProbNetTest)")
	@Test
	public void decisionAnalysisNetTest() {
		ProbNet probNet = createDecisionAnalysisNet();

		// Write test network
		String testNetworkName = rootPath +  "/test-dan.pgmx";
		try {
			writer.writeProbNet(testNetworkName, probNet);
		} catch (WriterException e) {
			System.err.println(e.getMessage());
			fail("Can not write network.");
		}

		// Read the preceding network
		ProbNet readProbNet = null;
		try {
			readProbNet = reader.loadProbNet(testNetworkName);
		} catch (ParserException e) {
			System.err.println(e.getMessage());
			fail("Can not read network:" + e.getLocalizedMessage());
		}

		// Compare both networks
		ProbNetTest.compareNetworks(probNet, readProbNet);
	}*/

	private ProbNet createDecisionAnalysisNet() {
		ProbNet probNet = new ProbNet(
				DecisionAnalysisNetworkType.getUniqueInstance());
		probNet.makeLinksExplicit(true);

		State[] stateA = new State[] { new State("A1"), new State("A2"),
				new State("A3") };
		State[] stateB = new State[] { new State("B1"), new State("B2") };
		State[] stateC = new State[] { new State("C1"), new State("C2") };
		Variable varA = new Variable("A", stateA);
		Variable varB = new Variable("B", stateB);
		Variable varC = new Variable("C", stateC);
		List<Variable> variables = new ArrayList<Variable>();
		variables.add(varB);
		variables.add(varA);
		variables.add(varC);
		Node nodeA = probNet.addNode(varA, NodeType.CHANCE);
		nodeA.setAlwaysObserved (true);
		probNet.addNode(varB, NodeType.CHANCE);
		probNet.addNode(varC, NodeType.CHANCE);
		Node nodeB = new Node(probNet, varB, NodeType.CHANCE);
		List<Link<Node>> links = probNet.getLinks();
		TablePotential potential = new TablePotential(variables,
				PotentialRole.CONDITIONAL_PROBABILITY);
		nodeB.addPotential(potential);
		probNet.addPotential(potential);
		for (Link<Node> link : links) {
			if (link.getNode1().getVariable().equals(varA)) {
				link.initializesRestrictionsPotential();
				link.setCompatibilityValue(stateA[0], stateB[0], 0);
				link.addRevealingState(stateA[0]);

			}
		}

		return probNet;
	}

	@Test
	public void writeOrphanUtility() {
		ProbNet probNet = Util.createTrivialID();
		try {
			probNet.removeNode(probNet.getNode("A"));
			probNet.removeNode(probNet.getNode("B"));
			probNet.removeNode(probNet.getNode("D"));
		} catch (NodeNotFoundException e) {
			e.printStackTrace();
			fail("This can not happen.");
		}
	}

}
