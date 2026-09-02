<?xml version="1.0"?>
<!--
SPDX-FileCopyrightText: Copyright (c) 2020-2021 Polystat.org
SPDX-License-Identifier: MIT
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" id="data-to-attrs" version="2.0">
  <!--
  This XSL takes all constants (data) anywhere in the object
  and turns them into bound attributes. Each new attributes
  gets a name that starts with a &#x3BA; and ends with a
  unique number.

  We need this because we want to turn the object into
  a functional aggregate, which be definition consists of functions
  and variables (no data, no constants).
  -->
  <xsl:strip-space elements="*"/>
  <xsl:template match="o[o[@name='@']]">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:for-each select=".//o[@base and @data and not(o)]">
        <xsl:element name="o">
          <xsl:attribute name="name">
            <xsl:text>&#x3BA;</xsl:text>
            <xsl:text>-</xsl:text>
            <xsl:value-of select="count(./preceding::*) + count(ancestor::*) + 1"/>
            <xsl:text>-</xsl:text>
            <xsl:value-of select="@line"/>
          </xsl:attribute>
          <xsl:attribute name="line">
            <xsl:value-of select="@line"/>
          </xsl:attribute>
          <xsl:attribute name="data">
            <xsl:value-of select="text()"/>
          </xsl:attribute>
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
