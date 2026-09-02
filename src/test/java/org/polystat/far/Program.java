/*
 * SPDX-FileCopyrightText: Copyright (c) 2020-2021 Polystat.org
 * SPDX-License-Identifier: MIT
 */
package org.polystat.far;

import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import org.cactoos.Func;
import org.cactoos.io.ResourceOf;
import org.cactoos.text.TextOf;

/**
 * Simulator of a real program from Polystat.
 * @since 0.4
 */
final class Program implements Func<String, XML> {

    /**
     * The location of .xmir file.
     */
    private final String res;

    /**
     * Ctor.
     * @param name Name of resource
     */
    Program(final String name) {
        this.res = name;
    }

    @Override
    public XML apply(final String name) throws Exception {
        final String[] parts = name.split("\\.", -1);
        if (!"\\Phi".equals(parts[0])) {
            throw new IllegalArgumentException(
                String.format(
                    "Can't fetch object '%s', must start with \\Phi",
                    name
                )
            );
        }
        if (parts.length != 2) {
            throw new IllegalArgumentException(
                String.format(
                    "Can't fetch object '%s', must start with \\Phi and end with name",
                    name
                )
            );
        }
        return new XMLDocument(
            new XMLDocument(
                new TextOf(
                    new ResourceOf(this.res)
                ).asString()
            ).nodes(
                String.format("//o[@name='%s']", parts[1])
            ).get(0).deepCopy()
        );
    }
}
