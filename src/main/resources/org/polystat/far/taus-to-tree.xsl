<?xml version="1.0"?>
<!--
SPDX-FileCopyrightText: Copyright (c) 2020-2021 Polystat.org
SPDX-License-Identifier: MIT
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" id="taus-to-tree" version="2.0">
  <!--
  This XSL simply converts textual representation of options into
  XML-tree format. For example, this XML:

  <o name="foo">
  <o line="3" name="a">
  <opts>
  <opt m="xx" x="\any">{t4:1=1} {t3:2=1}</opt>
  </opts>
  </o>
  </o>

  Will turn into:

  <o name="foo">
  <o line="3" name="a">
  <opts>
  <opt m="xx" x="\any">
  <tau i="4:1">1</tau>
  <tau i="3:2">1</tau>
  </opt>
  </opts>
  </o>
  </o>
  -->
  <xsl:strip-space elements="*"/>
  <xsl:template match="opt[not(empty(text()))]">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:for-each select="tokenize(text(), ' ')">
        <xsl:variable name="parts" select="tokenize(., '=')"/>
        <xsl:element name="tau">
          <xsl:attribute name="i">
            <xsl:value-of select="substring($parts[1], 3)"/>
          </xsl:attribute>
          <xsl:value-of select="substring($parts[2], 1, string-length($parts[2]) - 1)"/>
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
