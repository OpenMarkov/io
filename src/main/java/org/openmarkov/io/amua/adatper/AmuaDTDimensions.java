package org.openmarkov.io.amua.adatper;

import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.Criterion.CECriterion;
import org.openmarkov.io.amua.model.AmuaDTCENode;
import org.openmarkov.io.amua.model.AmuaDTNode;
import org.openmarkov.io.amua.model.AmuaDTType;
import org.openmarkov.io.amua.model.AmuaDimensionInfo;
import static org.openmarkov.io.amua.model.AmuaConstants.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the dimensional and analysis information for an AMUA decision tree.
 *
 * @author Hugo Manuel
 * @version 1.0
 */

public class AmuaDTDimensions {

    private final List<AmuaDimensionInfo> dimensions; // name, symbols, decimals
    private final int analysisType;
    private final int objective;
    private final int objectiveDim;
    private final int costDim;
    private final int effectDim;
    private final String baseScenario;
    private final double WTP;
    private final int extendedDim;

    /**
     * Private constructor. Instances are created via the {@link #assignDimensions} factory method.
     */
    private AmuaDTDimensions(List<AmuaDimensionInfo> dimensions,
                          int analysisType,
                          int objective,
                          int objectiveDim,
                          int costDim,
                          int effectDim,
                          String baseScenario,
                          double WTP,
                          int extendedDim) {
        this.dimensions = dimensions;
        this.analysisType = analysisType;
        this.objective = objective;
        this.objectiveDim = objectiveDim;
        this.costDim = costDim;
        this.effectDim = effectDim;
        this.baseScenario = baseScenario;
        this.WTP = WTP;
        this.extendedDim = extendedDim;
    }

    // getters...
    public List<AmuaDimensionInfo> getDimensions() { return dimensions; }
    public int getAnalysisType() { return analysisType; }
    public int getObjective() { return objective; }
    public int getObjectiveDim() { return objectiveDim; }
    public int getCostDim() { return costDim; }
    public int getEffectDim() { return effectDim; }
    public String getBaseScenario() { return baseScenario; }
    public double getWTP() { return WTP; }
    public int getExtendedDim() { return extendedDim; }



    /**
     * Creates an AmuaDTDimensions instance based on the provided criteria, tree type, and root node.
     *
     * @param criteria list of decision criteria associated with the tree
     * @param amuaDTType type of AMUA tree (UNICRITERIA or COST_EFFECTIVENESS)
     * @param tree root node of the decision tree
     * @return fully populated AmuaDTDimensions instance
     * @throws IllegalStateException if required criteria are missing or type is unsupported
     */
    public static AmuaDTDimensions assignDimensions(List<Criterion> criteria, AmuaDTType amuaDTType, AmuaDTNode<?> tree) {

        List<AmuaDimensionInfo> dimensions = new ArrayList<>();
        int analysisType;
        int objective;
        int costDim = -1;
        int effectDim = -1;
        int objectiveDim = 0;
        int extendedDim = 0;

        switch (amuaDTType) {

            case COST_EFFECTIVENESS:
                for (int i = 0; i < criteria.size(); i++) {
                    Criterion c = criteria.get(i);
                    dimensions.add(new AmuaDimensionInfo(c.getCriterionName(), c.getCriterionUnit(), DEFAULT_DECIMALS));

                    if (c.getCECriterion() == CECriterion.Cost) {
                        costDim = i;
                    } else if (c.getCECriterion() == CECriterion.Effectiveness) {
                        effectDim = i;
                    }
                }

                if (costDim == -1 || effectDim == -1) {
                    throw new IllegalStateException("CEA requires one Cost and one Effectiveness criterion.");
                }

                analysisType = ANALYSIS_TYPE_CEA;
                objective = OBJECTIVE_MINIMIZE;
                break;

            case UNICRITERIA:
                dimensions.add(new AmuaDimensionInfo("Utility", "u", DEFAULT_DECIMALS));
                analysisType = ANALYSIS_TYPE_EV;
                objective = OBJECTIVE_MAXIMIZE;
                costDim = 0;
                effectDim = 0;
                break;

            default:
                throw new IllegalStateException("Unsupported AmuaDTType: " + amuaDTType);
        }

        String baseScenario = (amuaDTType == AmuaDTType.COST_EFFECTIVENESS) ? calcBaseScenario(amuaDTType, tree) : null;
        double WTP = calcWTP(amuaDTType, criteria);

        return new AmuaDTDimensions(
                dimensions,
                analysisType,
                objective,
                objectiveDim,
                costDim,
                effectDim,
                baseScenario,
                WTP,
                extendedDim
        );
    }


    /**
     * Retrieves the Willingness-To-Pay (WTP) associated with
     * the current decision tree configuration.
     *
     * @return the WTP value if defined for COST_EFFECTIVENESS; otherwise 0.
     *
     * @throws IllegalStateException if the AmuaDTType is unsupported.
     */
    private static double calcWTP(AmuaDTType amuaDTType, List<Criterion> criteria) {
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
     *
     * @return the name of the base scenario.
     * @throws IllegalStateException if no decision node is found or if the AmuaDTType is unsupported.
     */
    private static String calcBaseScenario(AmuaDTType amuaDTType, AmuaDTNode<?> tree) {
        switch(amuaDTType) { // only works with Cost-Effectiveness Tree
            case COST_EFFECTIVENESS:
                double bestCost = Double.POSITIVE_INFINITY;
                double bestEffectiveness = Double.NEGATIVE_INFINITY;
                AmuaDTCENode decisionNode = (AmuaDTCENode) getDecisionNode(tree);
                AmuaDTCENode bestScenario = null;

                // iterate over the children of the decision node
                for (AmuaDTNode<?> amuaChildNode : decisionNode.getChildNodes()){
                    AmuaDTCENode ceNode = (AmuaDTCENode) amuaChildNode;
                    double cost = ceNode.getPartialUtility().getCost();
                    double effectiveness = ceNode.getPartialUtility().getEffectiveness();
                    // lowest cost, and in case of a tie, highest effectiveness.
                    if (cost < bestCost || (cost == bestCost && effectiveness > bestEffectiveness)) {
                        bestScenario  = ceNode;
                        bestCost = cost;
                        bestEffectiveness = effectiveness;
                    }
                }

                if (bestScenario == null) {throw new IllegalArgumentException("Base Scenario was not found");}
                return bestScenario.getName(); // return baseScenario

            case UNICRITERIA:
                return null;

            default:
                throw new IllegalStateException("Unsupported AmuaDTType: " + amuaDTType);
        }
    }


    /**
     * Get the decision node (type 0) from the tree whose root is {@code amuaRootNode}.
     *
     * @return the decision node found
     * @throws IllegalStateException if no decision node is found in the tree
     */
    private static AmuaDTNode<?> getDecisionNode(AmuaDTNode<?> node){
        AmuaDTNode<?> decisionNode = null;
        if (node.getType()==0){
            decisionNode = node;
        } else {
            for (AmuaDTNode<?> childNode : node.getChildNodes()) {
                if (childNode.getType() == 0) {
                    decisionNode = childNode;
                    break;
                }
            }
        }
        if (decisionNode == null) {throw new IllegalStateException("Decision node not found");}
        return decisionNode;
    }
}