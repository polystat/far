/*
 * SPDX-FileCopyrightText: Copyright (c) 2020-2021 Polystat.org
 * SPDX-License-Identifier: MIT
 */

package org.polystat.far;

import com.jcabi.xml.XMLDocument;
import com.jcabi.xml.XSL;
import com.jcabi.xml.XSLDocument;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.cactoos.io.ResourceOf;
import org.cactoos.iterable.IterableOf;
import org.cactoos.iterable.Joined;
import org.cactoos.iterable.Mapped;
import org.cactoos.text.TextOf;
import org.cactoos.text.UncheckedText;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Make XSL from rules.txt.
 * @since 1.0
 */
public final class Calc {

    /**
     * Left part of a rule.
     */
    private static final Pattern LEFT = Pattern.compile(
        "^([a-z.]+)\\(([^)]+)\\)$"
    );

    /**
     * Right part of a rule.
     */
    private static final Pattern RIGHT = Pattern.compile(
        "\\{([^}^{]+)}"
    );

    /**
     * Line break between rules.
     */
    private static final Pattern EOL = Pattern.compile("\\R");

    /**
     * Arrow between the left and the right parts of a rule.
     */
    private static final Pattern ARROW = Pattern.compile(" -> ");

    /**
     * Text rules.
     */
    private final String rules;

    /**
     * Ctor.
     * @param rls Rules
     */
    public Calc(final String rls) {
        this.rules = rls;
    }

    /**
     * Make it.
     * @return The XSL
     */
    public XSL xsl() {
        return new XSLDocument(
            new XSLDocument(
                new UncheckedText(
                    new TextOf(
                        new ResourceOf(
                            "org/polystat/far/build-calc-function.xsl"
                        )
                    )
                ).asString(),
                "build-calc-function.xsl"
            ).applyTo(
                new XMLDocument(
                    new Xembler(
                        new Directives().add("rules").append(
                            new Joined<>(
                                new Mapped<>(
                                    Calc::toDirs,
                                    new IterableOf<>(Calc.EOL.split(this.rules))
                                )
                            )
                        )
                    ).domQuietly()
                )
            ),
            "calc-function.xsl"
        );
    }

    private static Directives toDirs(final String rule) {
        final String[] parts = Calc.ARROW.split(rule, -1);
        final Matcher left = Calc.LEFT.matcher(parts[0]);
        if (!left.matches()) {
            throw new IllegalStateException(
                String.format("Wrong left part in line '%s'", parts[0])
            );
        }
        final Matcher right = Calc.RIGHT.matcher(parts[1]);
        final Directives dirs = new Directives().add("rule")
            .add("f").set(left.group(1)).up()
            .add("y").set(left.group(2)).up()
            .add("inputs");
        while (right.find()) {
            dirs.add("input");
            final String[] inputs = right.group(1).split(" ", -1);
            for (final String input : inputs) {
                dirs.add("x").set(input).up();
            }
            dirs.up();
        }
        return dirs.up().up();
    }
}
