package org.openmarkov.io.amua;

import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.decisiontree.*;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.core.EvaluationDecisionTreeNode;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.core.CEADecisionTreeNode;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.EnumSet;

/**
 * @author Hugo Manuel
 * @version 2.0
 */

public class AmuaWriter{

    private final DecisionTreeNode<?> rootNode;
    private final List<Criterion> criteria;

    private final Map<DecisionTreeNode<?>, Integer> nodeIndexMap = new HashMap<>();
    private final Map<DecisionTreeNode<?>, List<DecisionTreeNode<?>>> childrenByParentMap = new HashMap<>();

    private int currentIndex, decisionNodeCount;

    private AmuaDTType amuaDTType;


    /**
     * Constructs an AmuaWriter for the given decision tree.
     *
     * @param rootNode Root node of the decision tree
     * @throws IllegalArgumentException if the root node or its network is null
     */
    public AmuaWriter(DecisionTreeNode<?> rootNode) {
        if (rootNode == null) {
            throw new IllegalArgumentException("The decision tree root node cannot be null.");
        }

        this.rootNode = rootNode;

        ProbNet probNet = rootNode.getNetwork();
        if (probNet == null) {
            throw new IllegalArgumentException("The decision tree has no associated ProbNet.");
        }

        this.criteria = probNet.getDecisionCriteria();
    }


    /**
     * Writes the decision tree to a file in Amuaa format.
     *
     * @param outputFile Destination file
     * @throws Exception if validation fails or writing fails
     */
    public void writeAmuaDT(File outputFile) throws Exception {

        if (amuaDTType == null){
            if (!isValidDTForAmua()) {
                throw new IllegalStateException("The decision tree is not valid for export to Amua.");
            }
        }

        AmuaDecisionTreeWriter writer = new AmuaDecisionTreeWriter(rootNode, nodeIndexMap, childrenByParentMap, criteria, amuaDTType, outputFile);
        writer.write();
    }


    /**
     * Checks whether the current decision tree is valid for Amua export.
     *
     * @return true if valid, false otherwise
     */
    public boolean isValidDTForAmua() {
        try {
            getAmuaDTType(); // If no exception is thrown is valid
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }



    // -----------------------------------------------
    // PRIVATE METHODS
    // ----------------------------------------------



    /**
     * Determines the AMUA decision tree type.
     *
     * @throws IllegalStateException if the decision tree type is not supported
     */
    private void getAmuaDTType() {
        if (criteria == null || criteria.isEmpty()) {
            throw new IllegalStateException("The tree has no defined criteria.");
        }

        boolean hasCECriteria = hasCECriteria();

        // COST_EFFECTIVENESS
        if (hasCECriteria && validateDTStructure(true)) {
            amuaDTType = AmuaDTType.COST_EFFECTIVENESS;
        }

        // UNICRITERIA
        // An inferred unicriteria tree may internally include cost-effectiveness criteria
        // (hasCECriteria == true), even if they are not explicitly displayed.
        if ((criteria.size() == 1 || hasCECriteria) && validateDTStructure(false)) {
            amuaDTType = AmuaDTType.UNICRITERIA;
        }

        throw new IllegalStateException("Tree type not supported by Amua.");
    }


    /**
     * Checks whether the criteria correspond to "cost" and "effectiveness"
     *
     * @return true if criteria contain both Cost and Effectiveness
     */
    private boolean hasCECriteria(){
        if (criteria.size() != 2) {
            return false;
        }

        EnumSet<Criterion.CECriterion> types = EnumSet.noneOf(Criterion.CECriterion.class);

        for (Criterion criterion : criteria) {
            types.add(criterion.getCECriterion());
        }

        return types.contains(Criterion.CECriterion.Cost) && types.contains(Criterion.CECriterion.Effectiveness);
    }


    /**
     * Validates the decision tree structure and indexes its nodes.
     *
     * @param isCE Whether CE structure is expected
     * @return true if valid, false otherwise
     */
    private boolean validateDTStructure(boolean isCE) {
        resetValidationState();
        return validateAndIndexTreeDFS(rootNode, 1, isCE);
    }


    /**
     * Resets indexing and validation counters.
     */
    private void resetValidationState(){
        nodeIndexMap.clear();
        childrenByParentMap.clear();
        currentIndex = 0;
        decisionNodeCount = 0;
    }


    /**
     * Performs DFS traversal to validate and index the tree.
     *
     * @param node Current node
     * @param maxDecisionNodes Maximum allowed decision nodes
     * @param isCE Whether CE structure is expected
     * @return true if subtree is valid
     */
    private boolean validateAndIndexTreeDFS(DecisionTreeNode<?> node, int maxDecisionNodes, boolean isCE) {

        if (!isValidNode(node, maxDecisionNodes, isCE)) return false;

        nodeIndexMap.put(node, currentIndex++);

        List<DecisionTreeNode<?>> childNodeList = new ArrayList<>();

        for (DecisionTreeElement child : node.getChildren()) {

            if (child instanceof DecisionTreeBranch branch) {
                DecisionTreeNode<?> childNode = branch.getChild();

                if (!validateAndIndexTreeDFS(childNode, maxDecisionNodes, isCE)) {
                    return false;
                }
                childNodeList.add(childNode);
            }
        }
        childrenByParentMap.put(node, childNodeList);

        return true;
    }


    /**
     * Validates a single decision tree node.
     *
     * @param node Node to validate
     * @param maxDecisionNodes Maximum allowed decision nodes
     * @param isCE Whether CE structure is expected
     * @return true if valid
     */
    private boolean isValidNode(DecisionTreeNode<?> node, int maxDecisionNodes, boolean isCE){
        // Validate node class type
        if (isCE && !(node instanceof CEADecisionTreeNode)) return false;
        if (!isCE && !(node instanceof EvaluationDecisionTreeNode)) return false;

        // Validate node type
        NodeType type = node.getNodeType();
        if (type != NodeType.UTILITY && type != NodeType.CHANCE && type != NodeType.DECISION) {
            return false;
        }

        // Validate decision node constraints
        if (type == NodeType.DECISION) {
            decisionNodeCount++;
            return decisionNodeCount <= maxDecisionNodes;
        }

        return true;
    }
}
