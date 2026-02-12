package org.openmarkov.io.amua;

import org.openmarkov.core.exception.WriterException;
import org.openmarkov.core.io.ProbNetWriter;
import org.openmarkov.core.io.format.annotation.FormatType;
import org.openmarkov.core.model.decisiontree.DecisionTreeNode;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author Manuel Arias
 * @version 0.5
 */
@FormatType(name = "AmuaWriter0_5",  version = "1.0", extension = "amua", description = "OpenMarkov.1.0", role = "Writer")
public class AmuaDecisionTreeWriter  implements ProbNetWriter {
    public AmuaDecisionTreeWriter(DecisionTreeNode<?> rootNode, Map<DecisionTreeNode<?>, Integer> nodeIndexMap, Map<DecisionTreeNode<?>, List<DecisionTreeNode<?>>> childrenByParentMap, List<Criterion> criteria, AmuaDTType amuaDTType, File outputFile) {
    }

    @Override
    public void writeProbNet(String netName, ProbNet probNet) throws WriterException {

    }

    @Override
    public void writeProbNet(String netName, ProbNet probNet, List<EvidenceCase> evidence) throws WriterException {

    }

    public void write() {
    }
}
