<?xml version="1.0"?>
<!--
SPDX-FileCopyrightText: Copyright (c) 2020-2021 Polystat.org
SPDX-License-Identifier: MIT
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" id="cleanup-perps" version="2.0">
  <!--
  This XSL deletes all <opt> elements, which variable is \perp value. It's obvious, that such a
  situation is impossible: \perp can't come in as a value.
  -->
  <xsl:strip-space elements="*"/>
  <xsl:template match="opt[@x = '\perp']">
    <!-- just delete it -->
  </xsl:template>
  <xsl:template match="node()|@*">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
