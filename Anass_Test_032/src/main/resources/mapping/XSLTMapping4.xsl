<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:multimap="http://sap.com/xi/XI/SplitAndMerge"
    exclude-result-prefixes="multimap">

    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>

    <!-- ENTRY: remove multimap wrapper -->
    <xsl:template match="/multimap:Messages">
        <xsl:apply-templates select="multimap:Message1/MBGMCR04"/>
    </xsl:template>

    <!-- COPY MATMAS05 -->
    <xsl:template match="MBGMCR04">
        <MBGMCR04>
            <xsl:apply-templates select="@*|node()"/>

            <!-- Insert Message2 content CLEAN (without namespaces) -->
            <xsl:apply-templates select="/multimap:Messages/multimap:Message2/*" mode="clean"/>
        </MBGMCR04>
    </xsl:template>

    <!-- DEFAULT COPY -->
    <xsl:template match="@*|node()" >
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- CLEAN COPY MODE (removes multimap namespace) -->
    <xsl:template match="*" mode="clean">
        <xsl:element name="{local-name()}">
            <xsl:apply-templates select="@*|node()" mode="clean"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="@*" mode="clean">
        <xsl:attribute name="{local-name()}"><xsl:value-of select="."/></xsl:attribute>
    </xsl:template>

</xsl:stylesheet>
