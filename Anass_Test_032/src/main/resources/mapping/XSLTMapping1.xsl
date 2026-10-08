<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>

    <xsl:template match="/">
        <xsl:text>[&#10;</xsl:text>
        <xsl:apply-templates select="Products/Product"/>
        <xsl:text>&#10;]</xsl:text>
    </xsl:template>

    <xsl:template match="Product">
        <xsl:text>  {&#10;</xsl:text>
        <xsl:apply-templates select="*" />
        <xsl:text>  },&#10;</xsl:text>
    </xsl:template>

    <xsl:template match="*">
        <xsl:text>    "</xsl:text>
        <xsl:value-of select="name()"/>
        <xsl:text>": "</xsl:text>
        <xsl:choose>
            <xsl:when test="normalize-space(.)=''">""</xsl:when>
            <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
        </xsl:choose>
        <xsl:text>",&#10;</xsl:text>
    </xsl:template>
</xsl:stylesheet>
