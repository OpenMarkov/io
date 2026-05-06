/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.io;

import org.openmarkov.core.exception.ParserException;
import org.openmarkov.core.model.network.ProbNet;

import java.io.IOException;
import java.io.InputStream;

public interface ProbNetReader {
    
    /**
     * @param file    File
     * @param netName = path + network name + extension. {@code String}
     *
     * @return A {@code ProbNetInfo} or {@code null}
     *
     * @throws ParserException ParserException
     */
    ProbNetInfo loadProbNetInfo(String netName, InputStream file) throws IOException, ParserException;
    
    
    /**
     * @param file    File
     * @param netName = path + network name + extension. {@code String}
     *
     * @return A {@code ProbNet} or {@code null}
     *
     * @throws ParserException ParserException
     */
    ProbNet loadProbNet(String netName, InputStream file) throws IOException, ParserException;
    
    
}