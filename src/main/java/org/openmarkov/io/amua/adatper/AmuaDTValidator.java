package org.openmarkov.io.amua.adatper;

import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.decisiontree.*;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.core.EvaluationDecisionTreeNode;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.core.CEADecisionTreeNode;
import org.openmarkov.io.amua.model.AmuaDTType;

import java.util.EnumSet;
import java.util.List;

/**
 * Validates an OpenMarkov decision tree for AMUA export.
 *
 * @author Hugo Manuel
 * @version 1.0
 */

public class AmuaDTValidator {

    private final List<Criterion> criteria;
    private int decisionNodeCount = 0;

    public AmuaDTValidator(List<Criterion> criteria) {
        this.criteria = criteria;
    }

    /**
     * Determines the AMUA decision tree type.
     *
     * @throws IllegalStateException if the decision tree type is not supported
     */
    public AmuaDTType determineAmuaDTType(DecisionTreeNode<?> treeNode) {
        if (criteria == null || criteria.isEmpty()) {
            throw new IllegalStateException("The tree has no defined criteria.");
        }

        boolean hasCE = hasCECriteria();

        // COST_EFFECTIVENESS
        if (hasCE && hasValidStructure(treeNode, 1, true)) {
            return AmuaDTType.COST_EFFECTIVENESS;
        }

        // UNICRITERIA
        // An inferred unicriteria tree may internally include cost-effectiveness criteria
        // (hasCECriteria == true), even if they are not explicitly displayed.
        if ((criteria.size() == 1 || hasCE) && hasValidStructure(treeNode, 1, false)) {
            return AmuaDTType.UNICRITERIA;
        }

        throw new IllegalStateException("Tree type not supported by Amua.");
    }


    /**
     * Performs DFS traversal to validate the tree structure.
     *
     * @param node current node.
     * @param maxDecisionNodes maximum allowed decision nodes.
     * @param isCE whether CE structure is expected.
     * @return true if subtree is valid.
     */
    private boolean hasValidStructure(DecisionTreeNode<?> node, int maxDecisionNodes, boolean isCE){
        if (!isValidNode(node, maxDecisionNodes, isCE)) return false;

        for (DecisionTreeElement child : node.getChildren()) {
            if (child instanceof DecisionTreeBranch branch) {
                if (!hasValidStructure(branch.getChild(), maxDecisionNodes, isCE)) {
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * Checks if a single node is valid in Amua.
     *
     * @param node current node.
     * @param maxDecisionNodes maximum allowed decision nodes.
     * @param isCE whether CE structure is expected.
     * @return true if node is valid.
     */
    private boolean isValidNode(DecisionTreeNode<?> node, int maxDecisionNodes, boolean isCE){
        // validate node class type
        if (isCE && !(node instanceof CEADecisionTreeNode)) return false;
        if (!isCE && !(node instanceof EvaluationDecisionTreeNode)) return false;

        NodeType type = node.getNodeType();
        if (type != NodeType.UTILITY && type != NodeType.CHANCE && type != NodeType.DECISION) {
            return false;
        }

        if (type == NodeType.DECISION) {
            decisionNodeCount++;
            return decisionNodeCount <= maxDecisionNodes;
        }

        return true;
    }


    /**
     * Checks whether the criteria correspond to a Cost-Effectiveness (CEA) analysis.
     *
     * @return true if there are exactly two criteria: Cost and Effectiveness
     */
    private boolean hasCECriteria(){
        if (criteria.size() != 2) return false;
        EnumSet<Criterion.CECriterion> types = EnumSet.noneOf(Criterion.CECriterion.class);
        for (Criterion c : criteria) types.add(c.getCECriterion());
        return types.contains(Criterion.CECriterion.Cost) && types.contains(Criterion.CECriterion.Effectiveness);
    }

}
