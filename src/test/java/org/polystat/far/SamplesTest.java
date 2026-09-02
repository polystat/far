/*
 * SPDX-FileCopyrightText: Copyright (c) 2020-2021 Polystat.org
 * SPDX-License-Identifier: MIT
 */
package org.polystat.far;

import com.jcabi.xml.XMLDocument;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;
import org.cactoos.io.ResourceOf;
import org.cactoos.text.TextOf;
import org.cactoos.text.UncheckedText;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test case for {@link FaR}.
 * @since 0.4
 */
final class SamplesTest {

    @ParameterizedTest
    @MethodSource("samples")
    void testPacks(final String xmir) throws Exception {
        final String res = String.format(
            "xmir/03-optimize/org/polystat/far/samples/%s",
            xmir
        );
        MatcherAssert.assertThat(
            new FaR().errors(new Program(res), "\\Phi.test").size(),
            Matchers.equalTo(
                Integer.parseInt(
                    new XMLDocument(
                        new TextOf(new ResourceOf(res)).asString()
                    ).xpath("//meta[head='expected']/tail/text()").get(0)
                )
            )
        );
    }

    private static Collection<String> samples() {
        Assumptions.assumeTrue(
            Files.exists(
                Paths.get("").resolve("target/test-classes/xmir/03-optimize")
            ),
            "You must run 'mvn test' beforehand"
        );
        return Arrays.asList(
            Pattern.compile("\\R").split(
                new UncheckedText(
                    new TextOf(
                        new ResourceOf("xmir/03-optimize/org/polystat/far/samples")
                    )
                ).asString()
            )
        );
    }
}
