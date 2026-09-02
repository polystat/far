<?xml version="1.0"?>
<!--
SPDX-FileCopyrightText: Copyright (c) 2020-2021 Polystat.org
SPDX-License-Identifier: MIT
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" id="reverses" version="2.0">
  <!--
  This XSL goes through all free attributes of each abstract
  object and adds <opts> elements to them.

  If this is the input:

  <o name="foo">
  <o name="a"/>
  <o name="@">
  <o base="fun">
  <o base="a"/>
  </o>
  </o>
  </o>

  The output will look like this:

  <o name="foo">
  <o name="a">
  <opts>
  <r f="fun" pos="1" tau="1"/>
  </opts>
  </o>
  <o name="@">
  <o base="fun">
  <o base="a"/>
  </o>
  </o>
  </o>

  There will be as many <opts> elements as many times the attribute
  is seen in the body of the object.
  -->
  <xsl:strip-space elements="*"/>
  <xsl:template name="r">
    <xsl:param name="result" select="''"/>
    <xsl:param name="o" as="node()"/>
    <xsl:param name="attr" as="node()"/>
    <xsl:for-each select="$o/o">
      <xsl:variable name="i">
        <xsl:element name="r">
          <xsl:attribute name="f" select="$o/@base"/>
          <xsl:attribute name="pos" select="position()"/>
          <xsl:attribute name="tau">
            <xsl:value-of select="count($o/preceding::*) + count($o/ancestor::*)"/>
          </xsl:attribute>
          <xsl:copy-of select="$result"/>
        </xsl:element>
      </xsl:variable>
      <xsl:choose>
        <xsl:when test="@base=$attr/@name and @ref=$attr/@line">
          <xsl:element name="opts">
            <xsl:copy-of select="$i"/>
          </xsl:element>
        </xsl:when>
        <xsl:when test="text()=$attr/@data">
          <xsl:element name="opts">
            <xsl:copy-of select="$i"/>
          </xsl:element>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="r">
            <xsl:with-param name="result">
              <xsl:copy-of select="$i"/>
            </xsl:with-param>
            <xsl:with-param name="attr" select="$attr"/>
            <xsl:with-param name="o" select="."/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>
  <xsl:template match="o[not(@base) and not(o) and parent::o[o[@name='@']] and not(parent::o/parent::o)]">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:call-template name="r">
        <xsl:with-param name="attr" select="."/>
        <xsl:with-param name="o" select="parent::o/o[@name='@']"/>
      </xsl:call-template>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="node()|@*">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
