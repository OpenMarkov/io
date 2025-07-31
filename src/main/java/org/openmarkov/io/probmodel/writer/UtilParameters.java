/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.probmodel.writer;

import org.openmarkov.core.exception.WriterException;
import org.openmarkov.core.model.network.ProbNet;

/** Manage the error conditions in PGMXWriter0_2 and newer classes */
public class UtilParameters {

    public static void manageParametersWriter(String netName, ProbNet probNet) throws WriterException.TryingToWriteANullProbNet, WriterException.TryingToWriteAProbNetWithoutName {
        if (probNet == null || netName == null) {
            String msg;
            if (probNet == null) {
                throw new WriterException.TryingToWriteANullProbNet();
            };
            if(netName==null) {
                throw new WriterException.TryingToWriteAProbNetWithoutName(probNet);
            }
        }

    }
}
