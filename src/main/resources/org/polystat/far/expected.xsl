<?xml version="1.0"?>
<!--
SPDX-FileCopyrightText: Copyright (c) 2020-2021 Polystat.org
SPDX-License-Identifier: MIT
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" id="expected" version="2.0">
  <!--
  This XSL expects a parameter $expected, which it adds
  to the XML element /o/@expected. The value is what the
  object is expected to become with a certain combination
  of its free attributes.
  -->
  <xsl:strip-space elements="*"/>
  <xsl:param name="expected"/>
  <xsl:template match="/o">
    <xsl:copy>
      <xsl:attribute name="expected">
        <xsl:value-of select="$expected"/>
      </xsl:attribute>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="node()|@*">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
