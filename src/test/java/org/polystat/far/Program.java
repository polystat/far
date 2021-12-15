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
import org.cactoos.Func;
import org.cactoos.io.ResourceOf;
import org.cactoos.text.TextOf;

/**
 * Simulator of a real program from Polystat.
 *
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
        final String[] parts = name.split("\\.");
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
        final XML xml = new XMLDocument(
            new TextOf(
                new ResourceOf(this.res)
            ).asString()
        );
        return xml.nodes(String.format("//o[@name='%s']", parts[1])).get(0);
    }

}
