<?xml version="1.0"?>
<!--
SPDX-FileCopyrightText: Copyright (c) 2020-2021 Polystat.org
SPDX-License-Identifier: MIT
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" id="opts-to-expressions" version="2.0">
  <!--
  This XSL will fold all <opts> elements into more compact
  textual boolean expressions. For example, this XML:

  <o name="foo">
  <o line="3" name="a">
  <opts>
  <opt m="xxx" x="\any">
  <tau i="4:1">1</tau>
  <tau i="3:2">1</tau>
  </opt>
  </opts>
  </o>
  </o>

  Will turn into:

  <o name="foo">
  <o line="3" name="a">
  <b x="\any">((t4=1 ∧ t3=1))</b>
  </o>
  </o>
  -->
  <xsl:strip-space elements="*"/>
  <xsl:template match="o[opts]">
    <xsl:variable name="o" select="."/>
    <xsl:copy>
      <xsl:apply-templates select="@*|node() except opts"/>
      <xsl:for-each select="distinct-values(opts/opt/@x)">
        <xsl:variable name="x" select="."/>
        <xsl:element name="b">
          <xsl:attribute name="x">
            <xsl:value-of select="$x"/>
          </xsl:attribute>
          <xsl:for-each select="$o/opts[opt[@x=$x or @x='\any']]">
            <xsl:if test="position() &gt; 1">
              <xsl:text> and </xsl:text>
            </xsl:if>
            <xsl:text>(</xsl:text>
            <xsl:for-each select="opt[@x=$x or @x='\any' and tau]">
              <xsl:if test="position() &gt; 1">
                <xsl:text> or </xsl:text>
              </xsl:if>
              <xsl:text>(</xsl:text>
              <xsl:for-each select="tau">
                <xsl:if test="position() &gt; 1">
                  <xsl:text> &#x2227; </xsl:text>
                </xsl:if>
                <xsl:text>&#x1D70F;</xsl:text>
                <xsl:variable name="parts" select="tokenize(@i, ':')"/>
                <xsl:value-of select="$parts[1]"/>
                <xsl:text>=</xsl:text>
                <xsl:value-of select="text()"/>
              </xsl:for-each>
              <xsl:text>)</xsl:text>
            </xsl:for-each>
            <xsl:text>)</xsl:text>
          </xsl:for-each>
        </xsl:element>
      </xsl:for-each>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="node()|@*">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
