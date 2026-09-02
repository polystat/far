/*
 * SPDX-FileCopyrightText: Copyright (c) 2020-2021 Polystat.org
 * SPDX-License-Identifier: MIT
 */
package org.polystat.far;

import com.jcabi.log.Logger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link FaR}.
 * @since 0.1
 */
final class FaRTest {

    @Test
    void findsBugsInSimpleXml() throws Exception {
        final String xmir = "xmir/03-optimize/org/polystat/far/samples/div-by-zero.xmir";
        Assumptions.assumeTrue(Files.exists(Paths.get("target/test-classes").resolve(xmir)));
        final FaR reverses = new FaR();
        final Collection<String> bugs = reverses.errors(
            new Program(xmir), "\\Phi.test"
        );
        MatcherAssert.assertThat(
            bugs,
            Matchers.iterableWithSize(Matchers.not(Matchers.greaterThan(3)))
        );
        Logger.debug(this, "Bugs found: %s", bugs);
    }
}
