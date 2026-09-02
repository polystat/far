<?xml version="1.0"?>
<!--
SPDX-FileCopyrightText: Copyright (c) 2020-2021 Polystat.org
SPDX-License-Identifier: MIT
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" id="cleanup-outsiders" version="2.0">
  <xsl:strip-space elements="*"/>
  <xsl:template match="opt[exists(parent::opts/parent::o/opts[not(exists(opt[@x = current()/@x or @x='\any' or current()/@x='\any']))])]">
    <!-- remove it -->
  </xsl:template>
  <xsl:template match="node()|@*">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
