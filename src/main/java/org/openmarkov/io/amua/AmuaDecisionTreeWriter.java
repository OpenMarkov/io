package org.openmarkov.io.amua;

import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.Criterion.CECriterion;
import org.jdom2.*;
import org.jdom2.output.*;
import org.openmarkov.core.model.decisiontree.*;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.core.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.text.DecimalFormat;

/**
 * @author Hugo Manuel
 * @version 2.0
 */

public class AmuaDecisionTreeWriter {
    private final DecisionTreeNode<?> rootNode;
    private final File outputFile;
    private final List<Criterion> criteria;
    private final Map<DecisionTreeNode<?>, Integer> nodeIndex;
    private final Map<DecisionTreeNode<?>, List<DecisionTreeNode<?>>> childrenByParent;
    private final AmuaDTType amuaDTType;

    private Integer costDim = null;
    private Integer effectDim = null;


    /**
     * Constructs a new AMUA Decision Tree Writer.
     *
     * @param rootNode Root node of the decision tree.
     * @param nodeIndex Map assigning a unique integer index to each node.
     * @param childrenByParent Map storing the list of child nodes for each parent node.
     * @param criteria List of criteria for the analysis.
     * @param amuaDTType Type of AMUA decision tree (COST_EFFECTIVENESS or UNICRITERIA).
     * @param outputFile File to which the XML representation will be written.
     */
    public AmuaDecisionTreeWriter(DecisionTreeNode<?> rootNode, Map<DecisionTreeNode<?>, Integer> nodeIndex, Map<DecisionTreeNode<?>, List<DecisionTreeNode<?>>> childrenByParent, List<Criterion> criteria, AmuaDTType amuaDTType, File outputFile) {
        this.rootNode = rootNode;
        this.nodeIndex = nodeIndex;
        this.childrenByParent = childrenByParent;
        this.criteria = criteria;
        this.amuaDTType = amuaDTType;
        this.outputFile = outputFile;
    }


    /**
     * Generates the complete AMUA XML document and writes it to the output file.
     *
     * @throws IOException if an error occurs while writing the file.
     */
    public void write() throws IOException {

        // Create the root XML element <Model>...</Model>
        Element model = new Element("Model");

        // Add the model name
        model.addContent(writeAmuaName());

        // <Metadata>...</Metadata> (empty, required by Amua)
        model.addContent(new Element("Metadata"));

        // Add the dimension information
        model.addContent(writeAmuaDimInfo());


        // PARAMETER (cannot be included until they are defined separately in OpenMarkov): pending task

        // INFORMATION ABOUT THE SIMULATION: pending task


        // Add the DecisionTree structure
        model.addContent(writeAmuaTree());

        // Build the complete XML
        Document doc = new Document(model);
        XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            out.output(doc, fos);
        }
    }



    // -----------------------------------------------
    // PRIVATE METHODS
    // ----------------------------------------------



    /**
     * This method extracts the base name of the file (without extension)
     * and returns it as an XML element with the tag "name".
     *
     * @return the XML element representing the name
     */
    private Element writeAmuaName() {

        String fileName = outputFile.getName(); // retrieve the file name
        int dot = fileName.lastIndexOf('.'); // locate the last '.'

        // extract the base name (remove extension if present)
        String treeName = (dot > 0) ? fileName.substring(0, dot) : fileName;

        //<name>...</name>
        return new Element("name").setText(treeName);
    }


    /**
     * Writes the Amua representation of DimInfo
     *
     * @return an XML element representing DimInfo (Amua format)
     */
    private Element writeAmuaDimInfo() {
        // <dimInfo>...</dimInfo>
        Element dimInfo = new Element("DimInfo");

        List<String> dimNamesList = new ArrayList<>();
        List<String> dimSymbolsList = new ArrayList<>();
        List<Integer> decimalsList = new ArrayList<>();

        int analysisType, objective, objectiveDim;
        int extendedDim = 0;

        double wtp = getWTP();
        String baseScenario = getBaseScenario();

        // Dimension info depending on the Amua decision tree type
        switch(amuaDTType) {
            case COST_EFFECTIVENESS:
                for (int i = 0; i < criteria.size(); i++) {
                    Criterion c = criteria.get(i);

                    // set name, symbol and decimals
                    dimNamesList.add(c.getCriterionName());
                    dimSymbolsList.add(c.getCriterionUnit());
                    decimalsList.add(4);

                    // identify cost and effectiveness dimensions
                    if (c.getCECriterion() == CECriterion.Cost) {
                        costDim = i;
                    } else if (c.getCECriterion() == CECriterion.Effectiveness) {
                        effectDim = i;
                    }
                }

                // set values for cost-effectiveness analysis
                analysisType = 1; // 1 => Cost-Effectiveness Analysis (CEA)
                objective = 1; // 1 => minimize
                objectiveDim = 0;

                break;

            case UNICRITERIA:
                // set default name, symbol and decimals
                dimNamesList.add("Utility");
                dimSymbolsList.add("u");
                decimalsList.add(4);

                // set values for unicriteria analysis
                analysisType = 0; // 0 => Expected Value (EV)
                objective = 0; // 0 => maximize
                objectiveDim = 0;
                costDim = 0;
                effectDim = 0;

                break;

            default:
                throw new IllegalStateException("Unsupported AmuaDTType: " + amuaDTType);
        }

        // <dimNames>...</dimNames>
        for (String name : dimNamesList) {
            dimInfo.addContent(new Element("dimNames").setText(name));
        }
        // <dimSymbols>...</dimSymbols>
        for (String symbols : dimSymbolsList) {
            dimInfo.addContent(new Element("dimSymbols").setText(symbols));
        }
        // <decimals>...</decimals>
        for (Integer decimals : decimalsList) {
            dimInfo.addContent(new Element("decimals").setText(String.valueOf(decimals)));
        }

        dimInfo.addContent(new Element("analysisType").setText(String.valueOf(analysisType)));
        dimInfo.addContent(new Element("objective").setText(String.valueOf(objective)));
        dimInfo.addContent(new Element("objectiveDim").setText(String.valueOf(objectiveDim)));
        dimInfo.addContent(new Element("costDim").setText(String.valueOf(costDim)));
        dimInfo.addContent(new Element("effectDim").setText(String.valueOf(effectDim)));

        // <baseScenario>...</baseScenario>
        if(amuaDTType == AmuaDTType.COST_EFFECTIVENESS){ // required by CEA
            dimInfo.addContent(new Element("baseScenario")).setText(baseScenario);
        }

        // <WTP>...</WTP>
        dimInfo.addContent(new Element("WTP")).setText(String.valueOf(wtp));
        // <extendedDim>...</extendedDim>
        dimInfo.addContent(new Element("extendedDim")).setText(String.valueOf(extendedDim));

        return dimInfo;
    }


    /**
     * Writes the Amua representation of a tree to an XML element.
     *
     * @return an XML element <tree> containing all nodes of the decision tree.
     */
    private Element writeAmuaTree() {
        // <tree>...</tree>
        Element treeElem = new Element("tree");

        // a list to store all the nodes
        List<Element> allNodes = new ArrayList<>();

        // explore the nodes (first call)
        writeAmuaNode(rootNode, null, 0, allNodes);

        // add the content of each node
        for (Element nodeElem : allNodes) {
            treeElem.addContent(nodeElem);
        }

        return treeElem;
    }


    /**
     * Writes the Amua representation of a tree node to an XML element.
     *
     * @param node the node from which the properties will be retrieved.
     * @param branch the branch connecting the parent node to the child node.
     * @param level the  number of ancestor nodes separating this node from the root node.
     * @param nodeList a list of all the nodes in the tree.
     *
     */
    private void writeAmuaNode(DecisionTreeNode<?> node, DecisionTreeBranch branch, int level, List<Element> nodeList) {

        DecisionTreeNode<?> parent = null;
        Integer parentType = null;

        if(branch != null){
            parent = branch.getParent();
            parentType = typeNode(parent.getNodeType());
        }

        DecimalFormat df = new DecimalFormat("#.####");

        // <Node>...</Node>
        Element nodeElem = new Element("Node");

        // <type>...</type>
        int type = typeNode(node.getNodeType());
        nodeElem.addContent(new Element("type").setText(String.valueOf(type)));

        // <name>...</name>
        if(branch != null){
            nodeElem.addContent(new Element("name").setText(branch.getBranchState().getName()));
        } else{
            nodeElem.addContent(new Element("name").setText("Root"));
        }

        //xPos, yPos, width, height, parentX, parentY: pending task

        // <parentType>...</parentType>
        if (parent != null) {
            parentType = typeNode(parent.getNodeType());
            nodeElem.addContent(new Element("parentType").setText(String.valueOf(parentType)));
        }

        // <childIndices>...</childIndices>
        List<DecisionTreeNode<?>> childNodeList = childrenByParent.get(node);
        if (childNodeList != null && !childNodeList.isEmpty()) {
            for (DecisionTreeNode<?> childNode: childNodeList) {
                nodeElem.addContent(new Element("childIndices").setText(String.valueOf(nodeIndex.get(childNode))));
            }
        }

        // <level>...</level>
        nodeElem.addContent(new Element("level").setText(String.valueOf(level)));

        // <prob>...</prob>
        if (parentType != null && parentType == 1){
            nodeElem.addContent(new Element("prob").setText(String.valueOf(branch.getBranchProbability())));
        }

        // <hasCost>...</hasCost>
        nodeElem.addContent(new Element("hasCost").setText(String.valueOf(hasCost(node))));
        // <hasVarUpdates>...</hasVarUpdates>
        nodeElem.addContent(new Element("hasVarUpdates").setText("false"));
        // <visible>...</visible>
        nodeElem.addContent(new Element("visible").setText("true"));
        // <collapsed>...</collapsed>
        nodeElem.addContent(new Element("collapsed").setText("false"));


        // <cost>...</cost>: pending task (use of hasCost??)


        // <payoff>...</payoff>
        writeNodePayoff(node, nodeElem, type, df);

        nodeList.add(nodeElem); // adds the node to the node list

        // recursive call
        for (DecisionTreeElement child : node.getChildren()) {
            if (child instanceof DecisionTreeBranch childBranch) {
                DecisionTreeNode<?> childNode = childBranch.getChild();
                writeAmuaNode(childNode, childBranch, level + 1, nodeList);
            }
        }
    }

    /**
     * Writes <payoff> elements to the given node according to the AMUA decision tree type.
     *
     * @param node The decision tree node being serialized.
     * @param nodeElem The XML element representing this node in AMUA format.
     * @param type The Amua node type.
     * @param df DecimalFormat used to format numeric payoff values.
     * @throws IllegalStateException If the node instance does not match the expected type for the Amua analysis.
     */
    private void writeNodePayoff(DecisionTreeNode<?> node, Element nodeElem, int type, DecimalFormat df){
        switch(amuaDTType) {
            case COST_EFFECTIVENESS:
                for (int i = 0; i < criteria.size(); i++) {
                    if (type==2) { // Amua assigns the payoff value to leaf nodes (type == 2)
                        if (costDim != null && costDim == i) {
                            nodeElem.addContent(new Element("payoff").setText(df.format(getCEACost(node))));
                        } else if (effectDim != null && effectDim == i) {
                            nodeElem.addContent(new Element("payoff").setText(df.format(getCEAEffectiveness(node))));
                        }
                    }
                    else{
                        nodeElem.addContent(new Element("payoff"));
                    }
                }
                break;

            case UNICRITERIA:
                EvaluationDecisionTreeNode edtNode = (EvaluationDecisionTreeNode) node;
                if (type==2) {
                    nodeElem.addContent(new Element("payoff").setText(df.format(edtNode.getUtility())));
                } else{
                    nodeElem.addContent(new Element("payoff"));
                }
                break;

            default:
                throw new IllegalStateException("Unsupported AmuaDTType: " + amuaDTType);
        }
    }


    /**
     * Returns the type of the node in Amua format (int)
     *
     * @param nodeType the type of node in OpenMarkov format. It is transformed to Amua format
     * @return the integer representation of the node type (Amua format)
     * @throws IllegalArgumentException if the node type is not recognized by Amua.
     */
    private int typeNode(NodeType nodeType) {
        return switch (nodeType) {
            case DECISION -> 0;
            case CHANCE -> 1;
            case UTILITY -> 2;
            default -> throw new IllegalArgumentException("Node type not recognized by Amua: " + nodeType);
            // NOT INCLUDED: SV_SUM, SV_PRODUCT (Amua does not work with them)
        };
    }


    /**
     * Retrieves the Willingness-To-Pay (WTP) associated with
     * the current decision tree configuration.
     *
     * @return the WTP value if defined for COST_EFFECTIVENESS; otherwise 0.
     *
     * @throws IllegalStateException if the AmuaDTType is unsupported.
     */
    private double getWTP(){
        switch (amuaDTType) { // only works with Cost-Effectiveness Tree
            case COST_EFFECTIVENESS:
                for (Criterion criterion : criteria) {
                    if (criterion.getCECriterion() == CECriterion.Cost) {
                        return criterion.getUnicriterizationScale();
                    }
                }

            case UNICRITERIA:
                return 0;

            default:
                throw new IllegalStateException("Unsupported AmuaDTType: " + amuaDTType);
        }
    }


    /**
     * Selects the base scenario for which the analysis will be performed
     * together with the associated WTP.
     *
     * @return the name of the branch state representing the base scenario.
     *
     * @throws IllegalStateException if no decision node is found or if the AmuaDTType is unsupported.
     * @throws IllegalArgumentException if no valid branch is found.
     */
    private String getBaseScenario() {
        switch(amuaDTType) { // only works with Cost-Effectiveness Tree
            case COST_EFFECTIVENESS:
                DecisionTreeNode<?> decisionNode = null;
                //search for the decision node
                for (DecisionTreeNode<?> node : nodeIndex.keySet()){
                    if(typeNode(node.getNodeType())==0){
                        decisionNode = node;
                        break;
                    }
                }

                if (decisionNode == null) {throw new IllegalStateException("Decision node not found");}

                DecisionTreeBranch bestBranch = null;
                double bestCost = Double.POSITIVE_INFINITY;
                double bestEffectiveness = Double.NEGATIVE_INFINITY;

                // iterate over the children of the decision node
                for (DecisionTreeElement child : decisionNode.getChildren()){
                    if(child instanceof DecisionTreeBranch branch){
                        // get the child node reached by this branch
                        DecisionTreeNode<?> childNode = branch.getChild(); //

                        // get the cost and effectiveness of childNode
                        double cost = getCEACost(childNode);
                        double effectiveness = getCEAEffectiveness(childNode);

                        // lowest cost, and in case of a tie, highest effectiveness.
                        if (cost < bestCost || (cost == bestCost && effectiveness > bestEffectiveness)) {
                            bestBranch  = branch;
                            bestCost = cost;
                            bestEffectiveness = effectiveness;
                        }
                    }
                }

                if (bestBranch == null) {throw new IllegalArgumentException("Base Scenario was not found");}

                return bestBranch.getBranchState().getName(); // return baseScenario

            case UNICRITERIA:
                return null;

            default:
                throw new IllegalStateException("Unsupported AmuaDTType: " + amuaDTType);
        }
    }


    /**
     * Retrieves the cost-effectivenes utility (CEP) associated with a given node
     * of a Cost-Effectiveness decision tree
     *
     * @param node the decision tree node whose utility will be obtained.
     * @return the CEP object containing cost and effectiveness values.
     */
    private CEP getCEAUtility(DecisionTreeNode<?> node) {
        if (amuaDTType != AmuaDTType.COST_EFFECTIVENESS) { // only works with Cost-Effectiveness Tree
            throw new IllegalArgumentException("Unsupported AmuaDTType: " + amuaDTType);
        }

        CEADecisionTreeNode ceaNode = (CEADecisionTreeNode) node;
        return ceaNode.getUtility();
    }


    /**
     * Retrieves the cost component of a Cost-Effectiveness decision tree
     *
     * @param node the decision tree node whose cost will be obtained.
     * @return the cost value associated with the node
     */
    private double getCEACost(DecisionTreeNode<?> node) {
        return getCEAUtility(node).getCost(0);
    }


    /**
     * Retrieves the effectiveness component of a Cost-Effectiveness decision tree
     *
     * @param node the decision tree node whose effectiveness will be obtained.
     * @return the effectiveness value associated with the node
     */
    private double getCEAEffectiveness(DecisionTreeNode<?> node) {
        return getCEAUtility(node).getEffectiveness(0);
    }


    /**
     * Determines whether a decision tree node has an associated cost.
     *
     * @param node the decision tree node to check.
     * @return true if the node has cost in a cost-effectiveness tree, false otherwise.
     */
    private boolean hasCost(DecisionTreeNode<?> node){
        if (amuaDTType != AmuaDTType.COST_EFFECTIVENESS){
            return false;
        } else{
            return getCEACost(node) > 0;
        }
    }

}