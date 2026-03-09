package org.openmarkov.io.amua;

import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.decisiontree.*;
import org.openmarkov.io.amua.adatper.AmuaDTConverter;
import org.openmarkov.io.amua.adatper.AmuaDTDimensions;
import org.openmarkov.io.amua.adatper.AmuaDTValidator;
import org.openmarkov.io.amua.model.AmuaDTNode;
import org.openmarkov.io.amua.model.AmuaDTType;
import org.openmarkov.io.amua.writer.AmuaDecisionTreeWriter;

import java.io.File;
import java.util.List;

import static org.openmarkov.io.amua.adatper.AmuaDTDimensions.assignDimensions;

/**
 * @author Hugo Manuel
 * @version 3.0
 */

public class AmuaExporter {

    private final DecisionTreeNode<?> treeNode;
    private final List<Criterion> criteria;
    private AmuaDTType amuaDTType;
    private AmuaDTNode<?> amuaTreeNode;

    private boolean isValidDT;
    private boolean hasBeenValidatedDT;


    /**
     * Constructs an AmuaWriter for the given decision tree.
     *
     * @param treeNode root node of the decision tree in OpenMarkov format.
     * @throws IllegalArgumentException if the root node or its network is null.
     */
    public AmuaExporter(DecisionTreeNode<?> treeNode) {

        if (treeNode == null) {
            throw new IllegalArgumentException("The decision tree root node cannot be null.");
        }

        ProbNet probNet = treeNode.getNetwork();
        if (probNet == null) {
            throw new IllegalArgumentException("The decision tree has no associated ProbNet.");
        }

        this.treeNode = treeNode;
        this.criteria = probNet.getDecisionCriteria();

    }


    /**
     * Writes the decision tree to a file in Amua format.
     * Automatically validates if the tree is compatible before writing it
     *
     * @param outputFile destination file.
     * @throws Exception if validation fails or writing fails.
     */
    public void writeAmuaDT(File outputFile) throws Exception {
        if (!hasBeenValidatedDT) {
            isValidDT = isValidDTForAmua();
        }

        if (!isValidDT) {
            throw new IllegalStateException("The decision tree is not valid for export to Amua.");
        }

        AmuaDTDimensions amuaDimInfo = assignDimensions(criteria, amuaDTType, amuaTreeNode);

        AmuaDecisionTreeWriter writer = new AmuaDecisionTreeWriter(amuaTreeNode, amuaDimInfo, amuaDTType, outputFile);
        writer.writeDT();
    }


    /**
     * Checks whether the current decision tree is valid for Amua export.
     * If is valid, initializes internal attributes for export.
     *
     * @return true if valid, false otherwise
     */
    private boolean isValidDTForAmua() {
        try { // if no exception is thrown is valid
            AmuaDTValidator validator = new AmuaDTValidator(criteria);
            amuaDTType = validator.determineAmuaDTType(treeNode);

            AmuaDTConverter converter = new AmuaDTConverter(amuaDTType);
            amuaTreeNode = converter.convertToAmuaTree(treeNode);

            hasBeenValidatedDT = true;
            isValidDT = true;
            return true;
        } catch (IllegalStateException e) {
            hasBeenValidatedDT = true;
            isValidDT = false;
            return false;
        }
    }
}