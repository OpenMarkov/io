/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.probmodel.writer;

import org.openmarkov.core.exception.WriterException;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.io.format.annotation.FormatType;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.potential.*;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;
import org.openmarkov.core.model.network.potential.plugin.PotentialUtils;
import org.openmarkov.io.probmodel.strings.XMLAttributes;
import org.openmarkov.io.probmodel.strings.XMLTags;

import java.util.List;

import org.jdom2.Element;

/**
 * @author Manuel Arias
 * @version 0.5
 */
@FormatType(name = "PGMXWriter0_5",  version = "1.0", extension = "pgmx", description = "OpenMarkov.1.0")
public class PGMXWriter_1_0 extends PGMXWriter_0_2 {

	// Methods
	/**
	 * @param netName = path + network name + extension
	 * @param probNet
	 */
	@Override public void writeProbNet(String netName, ProbNet probNet) throws WriterException.CannotCreateFile, WriterException.TryingToWriteAProbNetWithoutName, WriterException.TryingToWriteANullProbNet {
		formatVersion= "1.0.0";
		super.writeProbNet(netName, probNet);
	}

	/**
	 * @param netName = path + network name + extension
	 * @param probNet
	 * @param evidences list of evidence cases
	 */
	@Override public void writeProbNet(String netName, ProbNet probNet, List<EvidenceCase> evidences) throws WriterException.CannotCreateFile, WriterException.TryingToWriteAProbNetWithoutName, WriterException.TryingToWriteANullProbNet {
		formatVersion= "1.0.0";
		super.writeProbNet(netName, probNet, evidences);
	}
	
	/**
	 * @param probNet
     *            . {@code ProbNet}
	 * @param probNetElement
     *            . {@code Element}
	 */
	@Override protected void getProbNetChildren(ProbNet probNet, Element probNetElement) {
		getAdditionalConstraints(probNet, probNetElement, new Element(XMLTags.ADDITIONAL_CONSTRAINTS.toString()));
		getProbNetComment(probNet, probNetElement, new Element(XMLTags.COMMENT.toString()));
		getDecisionCriteria(probNet, probNetElement, new Element(XMLTags.DECISION_CRITERIA.toString()));
		getAgents(probNet, probNetElement, new Element(XMLTags.AGENTS.toString()));
		getLanguage(probNet, probNetElement, new Element(XMLTags.LANGUAGE.toString()));
		getTemporaUnit(probNet, probNetElement);
		getAdditionalProperties(probNet, probNetElement);
		getVariables(probNet, probNetElement, new Element(XMLTags.VARIABLES.toString()));
		getLinks(probNet, probNetElement, new Element(XMLTags.LINKS.toString()));
		getPotentials(probNet, probNetElement, new Element(XMLTags.POTENTIALS.toString()));
	}

	/**
	 * @param probNet
     *            . {@code ProbNet}
	 * @param probNetElement
     *            . {@code Element}
	 */


    
	/**
	 * @param properties
	 * @return Element
	 */
	@Override protected Element getPropertiesElement(Properties properties) {
		Element additionalPropertiesElement = new Element(XMLTags.ADDITIONAL_PROPERTIES.toString());
		for (String propertyName : properties.getKeySet()) {
			Element propertyElement = new Element(XMLTags.PROPERTY.toString());
			propertyElement.setAttribute(XMLAttributes.NAME.toString(), propertyName);
			propertyElement.setAttribute(XMLAttributes.VALUE.toString(), properties.get(propertyName).toString());
			additionalPropertiesElement.addContent(propertyElement);
		}
		return additionalPropertiesElement;
	}



	/**
	 * Writes the link restriction
	 * 
	 * @param link
	 * @param linkElement
	 */
	@Override protected void getLinkRestriction(Link<Node> link, Element linkElement) {
		double[] table = ((TablePotential) link.getRestrictionsPotential()).values;

		boolean hasRestriction = false;
		for (int i = 0; i < table.length; i++) {
            if (table[i] == 0.0) {
                hasRestriction = true;
                break;
            }
		}
		if (hasRestriction) {
			Potential potential = link.getRestrictionsPotential();
			Element restrictionPotential = new Element(XMLTags.POTENTIAL.toString());
            String potentialType = PotentialUtils.getPotentialName(potential.getClass());
			restrictionPotential.setAttribute(XMLAttributes.TYPE.toString(), potentialType);
			Element variables = new Element(XMLTags.VARIABLES.toString());
			Element variable1 = new Element(XMLTags.VARIABLE.toString());
			variable1.setAttribute(XMLAttributes.NAME.toString(), potential.getVariable(0).getName());
			Element variable2 = new Element(XMLTags.VARIABLE.toString());
			variable2.setAttribute(XMLAttributes.NAME.toString(), potential.getVariable(1).getName());
			variables.addContent(variable1);
			variables.addContent(variable2);
			Element valuesElement = new Element(XMLTags.VALUES.toString())
					.setText(getValuesInAString(((TablePotential) link.getRestrictionsPotential()).values));
			restrictionPotential.addContent(variables);
			restrictionPotential.addContent(valuesElement);
			linkElement.addContent(restrictionPotential);
		}
	}

	/**
	 * @param probNet
	 * @param probNetElement
	 * @param potentialsElement
	 */
	@Override protected void getPotentials(ProbNet probNet, Element probNetElement, Element potentialsElement) {
		// HashMap of declared TablePotentials
		List<Potential> potentials = probNet.getPotentials();
		for (Potential potential : potentials) {
			Variable potentialVariable = potential.getVariable(0);
			// Do not write here policies
			if ((probNet.getNode(potentialVariable).getNodeType() != NodeType.DECISION)
					&& (potential.getPotentialRole() != PotentialRole.POLICY)) {
				Element potentialElement = new Element(XMLTags.POTENTIAL.toString());
				getPotential(probNet, potential, potentialElement);
				potentialsElement.addContent(potentialElement);
			}
		}
		probNetElement.addContent(potentialsElement);
	}

	/**
	 * @param potential
	 * @param potentialElement
	 * @param potentialElement
	 */
	@Override protected void getPotential(ProbNet probNet, Potential potential, Element potentialElement) {
		getPotentialAttributesAndVariables(potential, potentialElement);
		getPotentialBody(probNet, potential, potentialElement);
	}

	/**
	 *
	 * @param potential
	 * @param potentialElement
	 */
    protected static void getPotentialAttributesAndVariables(Potential potential, Element potentialElement) {
		/*
         * TablePotential tablePotential = null;
		 * if (potential instanceof ExactDistrPotential) { ExactDistrPotential
		 * exactDistrPotential = (ExactDistrPotential) potential; tablePotential =
		 * exactDistrPotential.getTablePotential(); } Potential wrapped = tablePotential
		 * == null ? potential : tablePotential;
		 */
        
        String potentialType = PotentialUtils.getPotentialName(potential.getClass());
        if (potential.getClass() == ExactDistrPotential.class) {
			potentialType = "UnivariateDistr";
			String distribution = "Exact";
			potentialElement.setAttribute(XMLAttributes.TYPE.toString(), potentialType);
			potentialElement.setAttribute(XMLAttributes.DISTRIBUTION.toString(), distribution);
		} else {
			potentialElement.setAttribute(XMLAttributes.TYPE.toString(), potentialType);
		}

		// TODO add function attribute

		getPotentialComment(potential, potentialElement);

		// TODO add aditionalProperties child

		getPotentialVariables(potential, potentialElement);
	}

	/**
	 *
	 * @param potential
	 * @param potentialElement
	 */
    protected static void getPotentialVariables(Potential potential, Element potentialElement) {
		List<Variable> potentialVariables = potential.getVariables();
		if (!potentialVariables.isEmpty()) {
			writePotentialVariables(potentialVariables, potentialElement);
		}
	}

	/**
	 *
	 * @param probNet
	 * @param potential
	 * @param potentialElement
	 */
	@Override protected void getPotentialBody(ProbNet probNet, Potential potential, Element potentialElement) {
		super.getPotentialBody(probNet, potential, potentialElement);
		if (potential instanceof UnivariateDistrPotential) { // New from the previous version of the writer
			getUnivariateDistrPotential(potentialElement, (UnivariateDistrPotential) potential);
		}
	}

	/**
	 * Adds an UnivariateDistrPotential to the XML structure
	 * @param xmlElement 
	 * @param potential
	 */
    protected void getUnivariateDistrPotential(Element xmlElement, UnivariateDistrPotential potential) {
		xmlElement.setAttribute(XMLAttributes.DISTRIBUTION.toString(), potential.getProbDensFunctionUnivariateName());
		xmlElement.setAttribute(XMLAttributes.PARAMETRIZATION.toString(), potential.getProbDensFunctionParametrizationName());
		Element parametersElement = new Element(XMLTags.PARAMETERS.toString());
		parametersElement.setText(getValuesInAString(potential.getDistributionTable().getValues()));
		getAugmentedProbTablePotential(xmlElement, potential.getAugmentedProbTable());
		// Write table values to the XML file
		xmlElement.addContent(parametersElement);		
	}

	/**
	 *
	 * @param xmlElement
	 * @param AugmentedProbTable
	 */
	@Override protected void getAugmentedProbTablePotential(Element xmlElement, AugmentedProbTable AugmentedProbTable) {
		Element parametersElement = new Element(XMLTags.FUNCTIONS.toString());
        
        VariableExpression[] functionValues = AugmentedProbTable.getFunctionValues();
        for (VariableExpression function : functionValues) {
            parametersElement.addContent("\"" + function.asStringExpression() + "\" ");
		}
		// Write table values to the XML file
		xmlElement.addContent(parametersElement);
	}

}