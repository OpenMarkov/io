/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.probmodel.reader;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;
import org.openmarkov.core.io.ProbNetReader;
import org.openmarkov.core.io.format.annotation.FormatType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.*;
import org.openmarkov.core.model.network.potential.plugin.PotentialManager;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;
import org.openmarkov.io.probmodel.exception.PGMXParserException;
import org.openmarkov.io.probmodel.strings.XMLAttributes;
import org.openmarkov.io.probmodel.strings.XMLTags;

/**
 * @author Manuel Arias
 */
@FormatType(name = "PGMXReader", version = "1.0", extension = "pgmx", description = "OpenMarkov.1.0", role = "Reader")
public class PGMXReader_1_0 extends PGMXReader_0_2 implements ProbNetReader {

	/**
	 * @param probNet <code>ProbNet</code>
	 * @param xmlPotential <code>Element</code>
	 * @return <code>Potential</code> read from the XML element
	 * @throws PGMXParserException if the potential type is not supported
	 */
	@Override protected Potential getPotential(Element xmlPotential, ProbNet probNet) throws PGMXParserException {

		return getPotential(xmlPotential, probNet, PotentialRole.CONDITIONAL_PROBABILITY);
	}

	/**
	 * @param probNet <code>ProbNet</code>
	 * @param eXMLPotential <code>Element</code>
	 * @return <code>Potential</code> read from the XML element
	 * @throws PGMXParserException if the potential type is not supported
	 */
	@Override protected Potential getPotential(Element eXMLPotential, ProbNet probNet, PotentialRole potentialRole)
			throws PGMXParserException {

		Potential potential;
		// get type and role of potential
		String sXMLPotentialType = getStringXMLPotentialType(eXMLPotential);
		List<Variable> variables = getReferencedVariables(eXMLPotential, probNet);
		if (sXMLPotentialType.equalsIgnoreCase(PotentialManager.getPotentialName(UniformPotential.class))) {
			potential = getUniformPotential(eXMLPotential, probNet, potentialRole, variables);
		//} else if (sXMLPotentialType.equalsIgnoreCase(PotentialManager.getPotentialName(TablePotential.class))) {
		} else if (sXMLPotentialType.equalsIgnoreCase("ProbTable")) {
			//TODO Manolo> Until it is clarified the design of tables, I leave commented a part of this code and use the same function 
			//of PGMXReader_0_2 for reading probability tables, as it is more stable and this is a critical part of OpenMarkov
			/*
			 * try { Element child =
			 * eXMLPotential.getChild(XMLTags.UNCERTAIN_VALUES.toString()); if (child !=
			 * null) { List<Element> uncertainParametersList =
			 * eXMLPotential.getChild(XMLTags.UNCERTAIN_VALUES.toString())
			 * .getChildren(XMLTags.UNCERT_PARAM.toString()); if (uncertainParametersList !=
			 * null) { for (Element uncertainParameter : uncertainParametersList) { if
			 * ((uncertainParameter.getAttributeValue(XMLAttributes.TYPE.toString()))
			 * .equals("Function")) { potential = getAugmentedTablePotential(eXMLPotential,
			 * xmlRole, variables); break; } } } } } catch (Exception e) { throw new
			 * PGMXParserException("Exception en getPotential", eXMLPotential); } finally {
			 * if (potential == null) { potential = getTablePotential(eXMLPotential,
			 * probNet, xmlRole, variables); } }
			 */
			potential = getTablePotential( eXMLPotential, probNet, potentialRole, variables );
		} else if (sXMLPotentialType.equalsIgnoreCase(PotentialManager.getPotentialName(TreeADDPotential.class))) {
			potential = getTreeADDPotential(eXMLPotential, probNet, potentialRole, variables);
		} else if (sXMLPotentialType.equalsIgnoreCase(PotentialManager.getPotentialName(CycleLengthShift.class))) {
			potential = getCycleLengthShiftPotential(eXMLPotential, probNet, potentialRole, variables);
		} else if (sXMLPotentialType.equalsIgnoreCase(PotentialManager.getPotentialName(SameAsPrevious.class))) {
			potential = getSameAsPrevious(eXMLPotential, variables);
		} else if (sXMLPotentialType.equalsIgnoreCase(PotentialManager.getPotentialName(SumPotential.class))) {
			potential = getSumPotential(PotentialRole.CONDITIONAL_PROBABILITY, variables);
		} else if (sXMLPotentialType.equalsIgnoreCase(PotentialManager.getPotentialName(ProductPotential.class))) {
			potential = getProductPotential(eXMLPotential, probNet, PotentialRole.CONDITIONAL_PROBABILITY, variables);
		} else if (sXMLPotentialType.equalsIgnoreCase("ICIModel")) {
			potential = getICIPotential(eXMLPotential, probNet, potentialRole, variables);
		} else if (sXMLPotentialType
				.equalsIgnoreCase(PotentialManager.getPotentialName(WeibullHazardPotential.class))) {
			potential = getWeibullPotential(eXMLPotential, probNet, potentialRole, variables);
		} else if (sXMLPotentialType
				.equalsIgnoreCase(PotentialManager.getPotentialName(ExponentialHazardPotential.class))) {
			potential = getExponentialHazardPotential(eXMLPotential, probNet, potentialRole, variables);
		} else if (sXMLPotentialType
				.equalsIgnoreCase(PotentialManager.getPotentialName(LinearCombinationPotential.class))
				|| PotentialManager.getAlternativeNames(LinearCombinationPotential.class).contains(sXMLPotentialType)) {
			potential = getLinearRegressionPotential(eXMLPotential, probNet, potentialRole, variables);
		} else if (sXMLPotentialType.equalsIgnoreCase(PotentialManager.getPotentialName(FunctionPotential.class))) {
			potential = getFunctionPotential(eXMLPotential, probNet, potentialRole, variables);
		}
		else if (sXMLPotentialType.equalsIgnoreCase(PotentialManager.getPotentialName(DeltaPotential.class))) {
			potential = getDeltaPotential(eXMLPotential, probNet, potentialRole, variables);
		} else if (sXMLPotentialType.equalsIgnoreCase(PotentialManager.getPotentialName(ExponentialPotential.class))) {
			potential = getExponentialPotential(eXMLPotential, probNet, potentialRole, variables);
		} else if (sXMLPotentialType.equalsIgnoreCase(PotentialManager.getPotentialName(BinomialPotential.class))) {
			potential = getBinomialPotential(eXMLPotential, probNet, potentialRole, variables);
		} else if (sXMLPotentialType.equalsIgnoreCase(PotentialManager.getPotentialName(ExactDistrPotential.class))
				|| sXMLPotentialType
						.equalsIgnoreCase(PotentialManager.getPotentialName(UnivariateDistrPotential.class))) {
			potential = getExactDistrPotential(eXMLPotential, probNet, potentialRole, variables);

		} else {
			throw new PGMXParserException("Potential type " + sXMLPotentialType + " not supported", eXMLPotential);
		}

		Element xmlComment = eXMLPotential.getChild(XMLTags.COMMENT.toString());
		if (xmlComment != null) {
			potential.setComment(xmlComment.getText());
		}
		return potential;
	}

	/**
	 * @param xmlPotential <code>Element</code>
	 * @return PotentialRole read from the XML element
	 */
	@Override protected PotentialRole getPotentialRole(Element xmlPotential) {
		String xmlPotentialRole = xmlPotential.getAttributeValue(XMLAttributes.ROLE.toString());
		PotentialRole xmlRole;
		if (xmlPotentialRole.equalsIgnoreCase("utility")) {
			xmlRole = PotentialRole.UNSPECIFIED;
		} else {
			xmlRole = PotentialRole.getEnumMember(xmlPotentialRole);
		}
		return xmlRole;
	}

	/**
	 * Reads an UnivariateDistrPotential from the Element xmlPotential
	 * 
	 * @param xmlPotential the Element to read
	 * @param xmlRole the role of the potential
	 * @param variables the variables of the potential
	 * @return the UnivariateDistrPotential read
	 * @throws PGMXParserException if there is an error reading the potential
	 */
	protected Potential getUnivariateDistrPotential(Element xmlPotential, PotentialRole xmlRole,
			List<Variable> variables) throws PGMXParserException {
		String univariateName = xmlPotential.getAttributeValue(XMLAttributes.DISTRIBUTION.toString());
		String parametrization = xmlPotential.getAttributeValue(XMLAttributes.PARAMETRIZATION.toString());
		Element xmlRootTable = xmlPotential.getChild(XMLTags.PARAMETERS.toString());
		double[] table = parseDoubles(xmlRootTable.getTextNormalize());

		UnivariateDistrPotential potential;
		try {
			potential = new UnivariateDistrPotential(variables, univariateName, parametrization, xmlRole);
			List<Variable> parameterVariables = potential.getParameterVariables();

			List<Variable> vDistributionTable = new ArrayList<>(potential.getFiniteStatesVariables());
			vDistributionTable.add(0, potential.getPseudoVariableDistribution());
			potential.getAugmentedTable().setValues(table);
			potential.setDistributionTable(
					getAugmentedTable(xmlPotential, xmlRole, vDistributionTable, parameterVariables));
		} catch (Exception e) {
			throw new PGMXParserException("Exception in getUnivariateDistrPotential.", xmlPotential);
		}
		return potential;

	}

	/**
	 * @param xmlPotential <code>Element</code>
	 * @param xmlRole <code>PotentialRole</code>
	 * @param variables <code>List</code> of <code>Variable</code> of the potential
	 * @return Potential
	 * @throws PGMXParserException if there is an error reading the potential
	 */
	// TODO Remove?
	protected Potential getAugmentedTablePotential(Element xmlPotential, PotentialRole xmlRole,
			List<Variable> variables) throws PGMXParserException {

		List<Variable> finiteStatesVariables;
		List<Variable> parameterVariables;
		AugmentedTablePotential potential;

		try {
			potential = new AugmentedTablePotential(variables, xmlRole);
			parameterVariables = potential.getParameterVariables();
			finiteStatesVariables = potential.getFiniteStatesVariables();
		} catch (Exception e) {
			throw new PGMXParserException("Exception in getAugmentedTablePotential", xmlPotential);
		}
		potential.setAugmentedTable(getAugmentedTable(xmlPotential, xmlRole, finiteStatesVariables, parameterVariables));
		return potential;
	}

	/**
	 * @param xmlPotential <code>Element</code>
	 * @param xmlRole <code>PotentialRole</code>
	 * @param finiteStatesVariables <code>List</code> of <code>Variable</code> of the potential
	 * @param parameterVariables <code>List</code> of <code>Variable</code>
	 * @return AugmentedTable
	 */
	// TODO parameterVariables is not used. Remove or use it
	protected AugmentedTable getAugmentedTable(Element xmlPotential, PotentialRole xmlRole,
			List<Variable> finiteStatesVariables, List<Variable> parameterVariables) {

		List<Element> uncertainParametersList = xmlPotential.getChild(XMLTags.UNCERTAIN_VALUES.toString())
				.getChildren(XMLTags.UNCERT_PARAM.toString());
		String[] functionValues = new String[uncertainParametersList.size()];
		int i = 0;
		for (Element uncertainParameter : uncertainParametersList) {
			functionValues[i++] = uncertainParameter.getText();
		}
		return new AugmentedTable(finiteStatesVariables, xmlRole, functionValues);
	}

}
