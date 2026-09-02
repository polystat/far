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
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Model;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.xembly.Directives;

/**
 * Boolean Expression Solver.
 * @since 1.1
 */
public final class Expr {

    /**
     * NONE.
     */
    public static final String NEVER = "N";

    /**
     * Rules of SMT Solver.
     */
    private static final Map<String, String> RULES = new HashMap<>();

    static {
        Expr.RULES.put("model", "true");
    }

    /**
     * SMT Solver.
     */
    private static final Context CTX = new Context(Expr.RULES);

    /**
     * Path of tau tags in XML.
     */
    private static final String TAU_PATH = "/o/o/opts/opt/tau/@i";

    /**
     * XML to parse.
     */
    private final XML xml;

    /**
     * Ctor.
     * @param xml XML
     */
    public Expr(final XML xml) {
        this.xml = xml;
    }

    /**
     * Make it.
     * @return Directives
     */
    public Directives find() {
        final Solver slv = Expr.CTX.mkSolver();
        final List<BoolExpr> list = new ArrayList<>(0);
        for (final XML obj : this.xml.nodes("o/o")) {
            final String name = obj.xpath("@name").get(0);
            BoolExpr equation = Expr.CTX.mkNot(
                Expr.CTX.mkEq(
                    Expr.CTX.mkConst(name, Expr.CTX.getStringSort()),
                    Expr.CTX.mkString("NONE")
                )
            );
            final BoolExpr cur = Expr.opts(obj, name);
            if (!cur.isFalse()) {
                equation = Expr.CTX.mkAnd(cur, equation);
            }
            list.add(equation);
        }
        list.add(this.mkVariables());
        slv.add(list.toArray(new BoolExpr[0]));
        final Directives dirs = new Directives();
        if (slv.check() == Status.SATISFIABLE) {
            final Model model = slv.getModel();
            dirs.xpath("o").add("input").attr("found", this.found(model));
            for (final String attr : this.xml.xpath("/o/o/@name")) {
                final String val = Expr.evaluated(model, attr);
                if (!val.isEmpty()) {
                    dirs.xpath("/o/input").add("a")
                        .attr("attr", attr)
                        .attr("x", val);
                }
            }
        }
        return dirs;
    }

    private static BoolExpr opts(final XML xml, final String name) {
        BoolExpr result = Expr.CTX.mkFalse();
        final List<Map<String, BoolExpr>> obj = new ArrayList<>(0);
        for (final XML opts : xml.nodes("opts")) {
            obj.add(Expr.opt(opts));
        }
        final Set<String> values = Expr.values(obj);
        for (final String val : values) {
            if (!Expr.possible(obj, val)) {
                continue;
            }
            final BoolExpr cur = Expr.CTX.mkAnd(
                Expr.CTX.mkEq(
                    Expr.CTX.mkConst(name, Expr.CTX.getStringSort()),
                    Expr.CTX.mkString(val)
                ),
                Expr.mkNegations(obj, val)
            );
            result = Expr.CTX.mkOr(result, cur);
        }
        return result;
    }

    private static Map<String, BoolExpr> opt(final XML xml) {
        final Map<String, BoolExpr> result = new HashMap<>();
        for (final XML opt : xml.nodes("opt")) {
            final String val = opt.xpath("@x").get(0);
            final BoolExpr old = result.getOrDefault(val, Expr.CTX.mkFalse());
            result.put(val, Expr.CTX.mkOr(Expr.taus(opt), old));
        }
        return result;
    }

    private static BoolExpr taus(final XML xml) {
        BoolExpr result = Expr.CTX.mkTrue();
        for (final XML tau : xml.nodes("tau")) {
            final String name = tau.xpath("@i").get(0).split(":", -1)[0];
            final String val = tau.xpath("text()").get(0);
            final BoolExpr cur = Expr.CTX.mkEq(
                Expr.CTX.mkConst(name, Expr.CTX.mkStringSort()),
                Expr.CTX.mkString(val)
            );
            result = Expr.CTX.mkAnd(result, cur);
        }
        return result;
    }

    private String found(final Model model) {
        final Set<String> names = new HashSet<>();
        final StringBuilder result = new StringBuilder();
        for (final String tau : this.xml.xpath(Expr.TAU_PATH)) {
            names.add(tau.split(":", -1)[0]);
        }
        for (final String name : names) {
            result.append("𝜏")
                .append(name)
                .append('=')
                .append(Expr.evaluated(model, name))
                .append(' ');
        }
        result.setLength(result.length() - 1);
        return result.toString();
    }

    private BoolExpr mkVariables() {
        final Map<String, Set<String>> variables = new HashMap<>();
        for (final String tau : this.xml.xpath(Expr.TAU_PATH)) {
            final String name = tau.split(":", -1)[0];
            variables.putIfAbsent(name, new HashSet<>());
            variables.get(name).addAll(
                this.xml.xpath(
                    String.format("/o/o/opts/opt/tau[@i='%s']/text()", tau)
                )
            );
        }
        BoolExpr result = Expr.CTX.mkTrue();
        for (final String name : variables.keySet()) {
            BoolExpr cur = Expr.CTX.mkFalse();
            for (final String val : variables.get(name)) {
                final BoolExpr expr = Expr.CTX.mkEq(
                    Expr.CTX.mkConst(name, Expr.CTX.getStringSort()),
                    Expr.CTX.mkString(val)
                );
                cur = Expr.CTX.mkOr(cur, expr);
            }
            result = Expr.CTX.mkAnd(result, cur);
        }
        return result;
    }

    private static BoolExpr mkNegations(final List<Map<String, BoolExpr>> obj, final String val) {
        BoolExpr result = Expr.CTX.mkTrue();
        for (final Map<String, BoolExpr> opt : obj) {
            BoolExpr negations = Expr.CTX.mkFalse();
            BoolExpr cur = Expr.CTX.mkFalse();
            for (final String oth : opt.keySet()) {
                final BoolExpr expr = opt.get(oth);
                if (Expr.match(val, oth)) {
                    cur = Expr.CTX.mkOr(cur, expr);
                } else {
                    negations = Expr.CTX.mkOr(negations, expr);
                }
            }
            cur = Expr.CTX.mkAnd(cur, Expr.CTX.mkNot(negations));
            result = Expr.CTX.mkAnd(result, cur);
        }
        return result;
    }

    private static boolean possible(final List<Map<String, BoolExpr>> obj, final String val) {
        boolean result = true;
        for (final Map<String, BoolExpr> opt : obj) {
            boolean cur = false;
            for (final String oth : opt.keySet()) {
                if (Expr.match(val, oth)) {
                    cur = true;
                    break;
                }
            }
            if (!cur) {
                result = false;
                break;
            }
        }
        return result;
    }

    private static Set<String> values(final List<Map<String, BoolExpr>> obj) {
        final Set<String> result = new HashSet<>();
        for (final Map<String, BoolExpr> opt : obj) {
            result.addAll(opt.keySet());
        }
        return result;
    }

    private static String evaluated(final Model model, final String name) {
        final String val = model.evaluate(
            Expr.CTX.mkConst(name, Expr.CTX.getStringSort()), true
        ).toString();
        return val.substring(1, val.length() - 1);
    }

    private static boolean match(final String first, final String second) {
        return first.equals(second) || "\\any".equals(second);
    }
}
