/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.probmodel.reader;

import org.openmarkov.core.exception.ParserException;

import java.util.Arrays;

/**
 * Create readers given a version
 */
public class ReaderFactory {
    
    /**
     * @param strVersion, read from the PGXML file. {@code String}
     * @return PGMXReader 0_2 or newer
     * @throws ParserException when the version is missing.
     */
    public static PGMXReader_0_2 getReader(String strVersion) throws ParserException.WrongVersion {
        Version version = Arrays.stream(Version.values())
                                .filter(
                                        iteratorVersion -> strVersion.startsWith(iteratorVersion.toString()))
                                .findFirst()
                                .orElse(Version.V02); // Default value
        // This means that, for example, 0.5.0 will be considered equal to 0.5.1,
        // because last digit is used to correct bugs. As consequence, both will use the same Reader.
        return switch (version) {
            case V02 -> new PGMXReader_0_2();
            case V10 -> new PGMXReader_1_0();
            // Extension point in future parser versions.
            default -> throw new ParserException.WrongVersion(strVersion);
        };
    }
    
}
