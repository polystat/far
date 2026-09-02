/*
 * SPDX-FileCopyrightText: Copyright (c) 2020-2021 Polystat.org
 * SPDX-License-Identifier: MIT
 */
package org.polystat.far;

import com.jcabi.matchers.XhtmlMatchers;
import org.cactoos.io.ResourceOf;
import org.cactoos.text.TextOf;
import org.cactoos.text.UncheckedText;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link Calc}.
 * @since 0.1
 */
final class CalcTest {

    @Test
    void buildsSimpleRulesXsl() {
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(new Calc("plus(y) -> {{y 0}}").xsl()),
            Matchers.allOf(
                XhtmlMatchers.hasXPath("//xsl:stylesheet"),
                XhtmlMatchers.hasXPath("//xsl:function[@name='ps:calc']"),
                XhtmlMatchers.hasXPath("//xsl:when[@test=\"$y = '\\any'\"]"),
                XhtmlMatchers.hasXPath("//xsl:when[@test=\"$func = 'plus'\"]")
            )
        );
    }

    @Test
    void buildsRealRulesXsl() {
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new Calc(
                    new UncheckedText(
                        new TextOf(
                            new ResourceOf(
                                "org/polystat/far/rules.txt"
                            )
                        )
                    ).asString().trim()
                ).xsl()
            ),
            XhtmlMatchers.hasXPath("//xsl:choose[count(xsl:when)=11]")
        );
    }
}
