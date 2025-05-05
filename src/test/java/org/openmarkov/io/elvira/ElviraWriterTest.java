/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.elvira;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.junit.jupiter.api.*;

import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
//import org.openmarkov.core.model.network.ProbNetTest;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;

/**
 * @author marias
 * @vesion 1.0
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class ElviraWriterTest {

	// Attributes
	private ProbNet small;

	private String tatmanTestFile = "ejemplo_Tatman_y_Shachter.elv";
	private String smallTestFile = "small.elv";

	private String rootPath = "";

	// Methods
	@BeforeEach
	/** Creates a small ProbNet */ public void setUp() throws IOException {
		//probNet small					
		//Variables
		String a = new String("A");
		String b = new String("B");
		String c = new String("C");

		//finite States variables
		Variable variableA = new Variable(a, 2);
		Variable variableB = new Variable(b, 2);
		Variable variableC = new Variable(c, 2);

		//additional properties
		String relevance = new String("Relevance");
		String value = new String("7.0");

		variableA.setAdditionalProperty(relevance, value);
		variableB.setAdditionalProperty(relevance, value);
		variableC.setAdditionalProperty(relevance, value);

		//Setting variable states
		State absent = new State("ausente");
		State present = new State("presente");
		State[] states = { absent, present };

		variableA.setStates(states);
		variableB.setStates(states);
		variableC.setStates(states);

		//Potentials
		//PotentialType type = PotentialType.TABLE;
		PotentialRole role = PotentialRole.CONDITIONAL_PROBABILITY;

		//Potential A
		double[] tableA = { 0.2, 0.8 };

		ArrayList<Variable> variablesA = new ArrayList<Variable>();
		variablesA.add(variableA);

		TablePotential potentialvaluesA = new TablePotential(variablesA, role, tableA);

		//Potential BA
		double[] tableBA = { 0.7, 0.3, 0.9, 0.1 };

		ArrayList<Variable> variablesBA = new ArrayList<Variable>();
		variablesBA.add(variableB);
		variablesBA.add(variableA);

		TablePotential potentialvaluesBA = new TablePotential(variablesBA, role, tableBA);

		//potencial CAB
		double[] tableCAB = { 0.15, 0.29, 0.84, 0.98, 0.85, 0.71, 0.16, 0.02 };

		ArrayList<Variable> variablesCAB = new ArrayList<Variable>();
		variablesCAB.add(variableC);
		variablesCAB.add(variableA);
		variablesCAB.add(variableB);

		TablePotential potentialvaluesCAB = new TablePotential(variablesCAB, role, tableCAB);

		small = new ProbNet();

		NodeType nodeType = NodeType.CHANCE;

		small.addNode(variableA, nodeType);
		small.addNode(variableB, nodeType);
		small.addNode(variableC, nodeType);

		//Links throws NodeNotFoundException
		try {
			small.addLink(variableA, variableB, true);
		} catch (NodeNotFoundException e) {
			fail("Can not create small network adding link A->B.");
		}
		try {
			small.addLink(variableA, variableC, true);
		} catch (NodeNotFoundException e) {
			fail("Can not create small network adding link A->C.");
		}
		try {
			small.addLink(variableB, variableC, true);
		} catch (NodeNotFoundException e) {
			fail("Can not create small network adding link B->C.");
		}

		small.addPotential((Potential) potentialvaluesA);
		small.addPotential((Potential) potentialvaluesBA);
		small.addPotential((Potential) potentialvaluesCAB);

		tatmanTestFile = getClass().getClassLoader().getResource(tatmanTestFile).getFile();

		URL url = this.getClass().getClassLoader().getResource("trivial3jensen.elv");
		rootPath = url.getPath();
		File file = new File(rootPath);
		String absolutePath = file.getAbsolutePath();
		String name = file.getName();
		rootPath = absolutePath.substring(0, absolutePath.length() - name.length());

		smallTestFile = rootPath + smallTestFile;
	}

	//TODO fix this
	/*
	@Disabled("Temporarily disabled as it uses tests classes from openmarkov.core (ProbNetTest)")
	@Test public void testBNTwoNodes() {
		try {
			new ElviraWriter().writeProbNet(smallTestFile, small);
		} catch (WriterException e) {
			System.err.println(e.getMessage());
			fail("Exception writing network: " + smallTestFile);
		}
		ProbNet readNetwork = null;
		try {
			readNetwork = new ElviraParser().
					loadProbNet(smallTestFile);
		} catch (FileNotFoundException e) {
			System.err.println(e.getMessage());
			fail("Not exists file: " + smallTestFile);
		} catch (ParserException e) {
			System.err.println(e.getMessage());
			fail("Parser error in file: " + smallTestFile);
		}
		ProbNetTest.compareNetworks(small, readNetwork);
	}*/

}
