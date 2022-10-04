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

import com.jcabi.xml.ClasspathSources;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.jcabi.xml.XSL;
import com.jcabi.xml.XSLDocument;
import com.yegor256.xsline.Shift;
import com.yegor256.xsline.StEndless;
import com.yegor256.xsline.StLambda;
import com.yegor256.xsline.StRepeated;
import com.yegor256.xsline.TrDefault;
import com.yegor256.xsline.TrLogged;
import com.yegor256.xsline.TrXSL;
import com.yegor256.xsline.Train;
import com.yegor256.xsline.Xsline;
import java.util.Collection;
import java.util.LinkedList;
import javax.xml.transform.stream.StreamSource;
import org.cactoos.Func;
import org.cactoos.io.InputStreamOf;
import org.cactoos.io.ResourceOf;
import org.cactoos.iterable.Mapped;
import org.cactoos.text.TextOf;
import org.cactoos.text.UncheckedText;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Finding bugs via reverses.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class FaR {

    /**
     * Find all errors.
     *
     * @param xmir The program
     * @param locator Name of the object to fetch
     * @return List of errors found
     * @throws Exception If fails
     * @checkstyle NonStaticMethodCheck (10 lines)
     */
    public Collection<String> errors(final Func<String, XML> xmir,
        final String locator) throws Exception {
        final XML obj = xmir.apply(locator);
        final Train<Shift> train = new TrXSL<>(new TrLogged(new TrDefault<>()))
            .with(FaR.xsl("expected.xsl").with("expected", "\\perp"))
            .with(FaR.xsl("data-to-attrs.xsl"))
            .with(FaR.xsl("reverses.xsl"))
            .with(
                new StRepeated(
                    FaR.xsl("calculate.xsl").with(
                        (href, base) -> new StreamSource(
                            new InputStreamOf(
                                new Calc(
                                    new UncheckedText(
                                        new TextOf(
                                            new ResourceOf(
                                                "org/polystat/far/rules.txt"
                                            )
                                        )
                                    ).asString().trim()
                                ).xsl().toString()
                            )
                        )
                    ),
                    after -> !after.nodes("//r").isEmpty()
                )
            )
            .with(FaR.xsl("cleanup-outsiders.xsl"))
            .with(FaR.xsl("taus-to-tree.xsl"))
            .with(FaR.xsl("unmatch-data.xsl").with("never", Expr.NEVER))
            .with(new StEndless(FaR.xsl("cleanup-conflicts.xsl")))
            .with(FaR.xsl("cleanup-perps.xsl"))
            .with(
                new StLambda(
                    (integer, xml) -> new XMLDocument(
                        new Xembler(new Expr(xml).find()).applyQuietly(xml.node())
                    )
                )
            )
            .with(FaR.xsl("opts-to-expressions.xsl"))
            .with(
                new StLambda(
                    (integer, xml) -> {
                        final Directives dirs = new Directives();
                        final StringBuilder expression = new StringBuilder();
                        for (final String var : xml.xpath("/o/input/a/@attr")) {
                            final String path = String.format("/o/input/a[@attr='%s']", var);
                            final String val = xml.xpath(String.format("%s/@x", path)).get(0);
                            final String expr = xml.xpath(
                                String.format("/o/o[@name='%s']/b[@x='%s']/text()", var, val)
                            ).get(0);
                            dirs.xpath(path)
                                .set(expr);
                            if (expression.length() != 0) {
                                expression.append(" and ");
                            }
                            expression.append(expr);
                        }
                        dirs.xpath("/o/input").add("expr").set(expression);
                        return new XMLDocument(
                            new Xembler(dirs).applyQuietly(xml.node())
                        );
                    }
                )
            )
            .with(FaR.xsl("cleanup-expressions.xsl"))
            .back();
        final XML out = new Xsline(train).pass(obj);
        final Collection<String> bugs = new LinkedList<>();
        for (final XML bug : out.nodes("/o/input[@found]")) {
            bugs.add(
                String.format(
                    "\\perp at {%s}",
                    String.join(
                        ", ",
                        new Mapped<>(
                            attr -> String.format(
                                "%s=%s", attr.xpath("@attr").get(0),
                                attr.xpath("@x").get(0)
                            ),
                            bug.nodes("a")
                        )
                    )
                )
            );
        }
        return bugs;
    }

    /**
     * Make XSL.
     *
     * @param name Name of it
     * @return A new XSL
     */
    private static XSL xsl(final String name) {
        final String path = String.format("org/polystat/far/%s", name);
        return new XSLDocument(
            new UncheckedText(
                new TextOf(
                    new ResourceOf(path)
                )
            ).asString(),
            path
        ).with(new ClasspathSources());
    }

}
