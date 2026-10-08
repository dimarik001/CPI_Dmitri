<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:multimap="http://sap.com/xi/XI/SplitAndMerge"
    exclude-result-prefixes="multimap">

    <xsl:output method="xml"
                indent="yes"
                omit-xml-declaration="yes"/>

    <xsl:strip-space elements="*"/>

    <!-- Remove multimap wrapper -->
    <xsl:template match="/multimap:Messages">
        <Items>
            <xsl:apply-templates
                select="multimap:Message1/Items/Item"
                mode="item"/>
        </Items>
    </xsl:template>

    <!-- Rebuild every Item without inherited namespaces -->
    <xsl:template match="Item" mode="item">
        <Item>

            <!-- Copy original Item content cleanly -->
            <xsl:apply-templates select="@*|node()" mode="clean"/>

            <!-- Add Message2 content inside Item cleanly -->
            <xsl:apply-templates
                select="/multimap:Messages/multimap:Message2/*"
                mode="clean"/>

        </Item>
    </xsl:template>

    <!-- Recreate elements using only their local name -->
    <xsl:template match="*" mode="clean">
        <xsl:element name="{local-name()}">
            <xsl:apply-templates select="@*|node()" mode="clean"/>
        </xsl:element>
    </xsl:template>

    <!-- Recreate normal attributes without namespaces -->
    <xsl:template match="@*" mode="clean">
        <xsl:attribute name="{local-name()}">
            <xsl:value-of select="."/>
        </xsl:attribute>
    </xsl:template>

    <!-- Copy text, comments and processing instructions -->
    <xsl:template match="text()|comment()|processing-instruction()"
                  mode="clean">
        <xsl:copy/>
    </xsl:template>

</xsl:stylesheet>