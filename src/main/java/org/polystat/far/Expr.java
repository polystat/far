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
import com.microsoft.z3.*;
import org.xembly.Directives;

import java.util.*;

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
        RULES.put("model", "true");
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
    public Directives find()  {
        final Solver slv = CTX.mkSolver();
        final List<BoolExpr> list = new ArrayList<>(0);
        for (final XML obj : this.xml.nodes("o/o")) {
            final BoolExpr cur = opts(obj);
            if (cur != null) {
                list.add(cur);
            }
        }
        list.add(this.mkVariables());
        slv.add(list.toArray(new BoolExpr[0]));
        final Directives dirs = new Directives();
        if (slv.check() == Status.SATISFIABLE) {
            final Model model = slv.getModel();
            dirs.xpath("o").add("input").attr("found", this.found(model));
            for (final String var : this.xml.xpath("/o/o/@name")) {
                String val = model.evaluate(CTX.mkConst(var, CTX.getStringSort()), true).toString();
                val = val.substring(1, val.length() - 1);
                if (!val.isEmpty()) {
                    dirs.xpath("/o/input").add("a")
                        .attr("attr", var)
                        .attr("x", val);
                }
            }
        }
        return dirs;
    }

    /**
     * Parse opts tags.
     * @param xml Current XML block
     * @return BoolExpr of current block
     */
    private static BoolExpr opts(final XML xml) {
        BoolExpr result = null;
        final String name = xml.xpath("@name").get(0);
        final List<Map<String, BoolExpr>> obj = new ArrayList<>(0);
        for (final XML opts : xml.nodes("opts")) {
            obj.add(opt(opts));
        }
        final Set<String> values = values(obj);
        for (final String val : values) {
            if (!possible(obj, val)) {
                continue;
            }
            final BoolExpr cur = CTX.mkAnd(
                CTX.mkEq(
                    CTX.mkConst(name, CTX.getStringSort()),
                    CTX.mkString(val)
                ),
                mkNegations(obj, val)
            );
            if (result == null) {
                result = cur;
            } else {
                result = CTX.mkOr(result, cur);
            }
        }
        return result;
    }

    /**
     * Parse opt tags of current opts block.
     * @param xml Current XML block
     * @return Set of all value of opt and its BoolExpr
     */
    private static Map<String, BoolExpr> opt(final XML xml) {
        final Map<String, BoolExpr> result = new HashMap<>();
        for (final XML opt : xml.nodes("opt")) {
            final String val = opt.xpath("@x").get(0);
            final BoolExpr old = result.getOrDefault(val, null);
            BoolExpr cur = taus(opt);
            if (old != null) {
                cur = CTX.mkOr(cur, old);
            }
            result.put(val, cur);
        }
        return result;
    }

    /**
     * Parse tau tags of current opt block.
     * @param xml Current XML block
     * @return BoolExpr of this opt block
     */
    private static BoolExpr taus(final XML xml) {
        BoolExpr result = null;
        for (final XML tau : xml.nodes("tau")) {
            final String var = tau.xpath("@i").get(0).split(":")[0];
            final String val = tau.xpath("text()").get(0);
            final BoolExpr cur = CTX.mkEq(
                CTX.mkConst(var, CTX.mkStringSort()),
                CTX.mkString(val)
            );
            if (result == null) {
                result = cur;
            } else {
                result = CTX.mkAnd(result, cur);
            }
        }
        return result;
    }

    /**
     * Solution of Boolean Expressions consisting all tau variables.
     * @param model Model
     * @return Representation of an expression in a string
     */
    private String found(final Model model) {
        final Set<String> vars = new HashSet<>();
        final StringBuilder result = new StringBuilder();
        for (final String var : this.xml.xpath(Expr.TAU_PATH)) {
            vars.add(var.split(":")[0]);
        }
        for (final String var : vars) {
            String val = model.evaluate(CTX.mkConst(var, CTX.getStringSort()), true).toString();
            val = val.substring(1, val.length() - 1);
            result.append("\uD835\uDF0F")
                .append(var)
                .append('=')
                .append(val)
                .append(' ');
        }
        result.setLength(result.length() - 1);
        return result.toString();
    }

    /**
     * Makes a BoolExpr of all possible values of tau.
     * @return BoolExpr
     */
    private BoolExpr mkVariables() {
        final List<String> vars = this.xml.xpath(Expr.TAU_PATH);
        final Map<String, Set<String>> variables = new HashMap<>();
        for (final String var : vars) {
            final String name = var.split(":")[0];
            variables.putIfAbsent(name, new HashSet<>());
            final String path = String.format("/o/o/opts/opt/tau[@i='%s']/text()", var);
            variables.get(name).addAll(this.xml.xpath(path));
        }
        BoolExpr result = null;
        for (final String var : variables.keySet()) {
            BoolExpr cur = null;
            for (final String val : variables.get(var)) {
                final BoolExpr expr = CTX.mkEq(
                    CTX.mkConst(var, CTX.getStringSort()),
                    CTX.mkString(val)
                );
                if (cur == null) {
                    cur = expr;
                } else {
                    cur = CTX.mkOr(cur, expr);
                }
            }
            if (result == null) {
                result = cur;
            } else {
                result = CTX.mkAnd(result, cur);
            }
        }
        return result;
    }

    /**
     * Makes a BoolExpr of current o-tag with negations of all values, that do not match to val.
     * @param obj Parsed o-tag
     * @param val Value of o-tag
     * @return BoolExpr
     */
    private static BoolExpr mkNegations(final List<Map<String, BoolExpr>> obj, final String val) {
        BoolExpr result = null;
        for (final Map<String, BoolExpr> opt : obj) {
            BoolExpr negations = null;
            BoolExpr cur = null;
            for (final String oth : opt.keySet()) {
                final BoolExpr expr = opt.get(oth);
                if (match(val, oth)) {
                    if (cur == null) {
                        cur = opt.get(oth);
                    } else {
                        cur = CTX.mkOr(cur, opt.get(oth));
                    }
                } else {
                    if (negations == null) {
                        negations = expr;
                    } else {
                        negations = CTX.mkOr(negations, expr);
                    }
                }
            }
            if (negations != null) {
                cur = CTX.mkAnd(cur, CTX.mkNot(negations));
            }
            if (result == null) {
                result = cur;
            } else {
                result = CTX.mkAnd(result, cur);
            }
        }
        return result;
    }

    /**
     * Is it possible to make BoolExpr with this value?
     * @param obj Parsed o-tag block
     * @param val Value of o-tag block
     * @return True if YES, false if NO
     */
    private static boolean possible(final List<Map<String, BoolExpr>> obj, final String val) {
        boolean result = true;
        for (final Map<String, BoolExpr> opt : obj) {
            boolean cur = false;
            for (final String oth : opt.keySet()) {
                if (match(val, oth)) {
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

    /**
     * Find all possible values of current variable.
     * @param obj Parsed o-tag block
     * @return All possible values of current variable
     */
    private static Set<String> values(final List<Map<String, BoolExpr>> obj) {
        final Set<String> result = new HashSet<>();
        for (final Map<String, BoolExpr> opt : obj) {
            result.addAll(opt.keySet());
        }
        return result;
    }

    /**
     * Check that second string matches to first string.
     * @param first First string
     * @param second Second string
     * @return True if second string matches to first, false if it does not
     */
    private static boolean match(final String first, final String second) {
        return first.equals(second) || second.equals("\\any");
    }
}
