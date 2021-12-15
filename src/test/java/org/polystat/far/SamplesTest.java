/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020-2021 Polystat.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.polystat.far;

import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import org.cactoos.io.ResourceOf;
import org.cactoos.text.TextOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test case for {@link FaR}.
 *
 * @since 0.4
 */
final class SamplesTest {

    @ParameterizedTest
    @MethodSource("samples")
    public void testPacks(final String xmir) throws Exception {
        final String res = String.format(
            "xmir/03-optimize/org/polystat/far/samples/%s",
            xmir
        );
        final FaR far = new FaR();
        final Collection<String> bugs = far.errors(
            new Program(res), "\\Phi.test"
        );
        final XML xml = new XMLDocument(
            new TextOf(new ResourceOf(res)).asString()
        );
        MatcherAssert.assertThat(
            bugs.size(),
            Matchers.equalTo(
                Integer.parseInt(
                    xml.xpath("//meta[head='expected']/tail/text()").get(0)
                )
            )
        );
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private static Collection<String> samples() throws IOException {
        return Arrays.asList(
            new TextOf(
                new ResourceOf("xmir/03-optimize/org/polystat/far/samples")
            ).asString().split("\n")
        );
    }

}
