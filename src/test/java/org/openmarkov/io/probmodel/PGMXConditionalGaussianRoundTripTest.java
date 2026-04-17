/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.probmodel;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.potential.ConditionalGaussianPotential;
import org.openmarkov.core.model.network.potential.DiscretizedCauchyPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.io.probmodel.reader.PGMXReader_1_0;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Round-trip fixtures for the ConditionalGaussian and DiscretizedCauchy parsers.
 * Both tests stay {@link Disabled} until refactors A.1 and A.2 implement the
 * corresponding entries in {@code PGMXPotentialParsers}.
 */
class PGMXConditionalGaussianRoundTripTest {

    @Test
    void conditionalGaussianNetworkLoadsIntoExpectedPotential() throws Exception {
        ProbNet probNet = loadFixture("BN-conditional-gaussian.pgmx");
        Potential potential = probNet.getNode("Height").getPotentials().getFirst();
        assertThat(potential).isInstanceOf(ConditionalGaussianPotential.class);
    }

    @Disabled("Pending A.2: getDiscretizedCauchyPotential implementation")
    @Test
    void discretizedCauchyNetworkLoadsIntoExpectedPotential() throws Exception {
        ProbNet probNet = loadFixture("BN-discretized-cauchy.pgmx");
        Potential potential = probNet.getNode("Rainfall").getPotentials().getFirst();
        assertThat(potential).isInstanceOf(DiscretizedCauchyPotential.class);
    }

    private ProbNet loadFixture(String fileName) throws Exception {
        URL url = getClass().getClassLoader().getResource(fileName);
        assertThat(url).as("fixture %s on test classpath", fileName).isNotNull();
        File file = new File(url.toURI());
        try (FileInputStream in = new FileInputStream(file)) {
            return new PGMXReader_1_0().loadProbNet(file.getAbsolutePath(), in);
        }
    }
}
