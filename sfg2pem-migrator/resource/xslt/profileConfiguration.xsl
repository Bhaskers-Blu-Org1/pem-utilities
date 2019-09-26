<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:param name="name" />
	<xsl:param name="partner" />
	<xsl:param name="resourceType" />
	<xsl:param name="subResourceType" />
	<xsl:param name="serverType" />
	<xsl:param name="sponsorDivisionKey" />
	<xsl:param name="parentProfileKey"/>

	<xsl:template match="/">
		<xsl:element name="create">
			<xsl:attribute name="name">
<xsl:value-of select="$name" />
</xsl:attribute>
		<xsl:if test="$partner != ''">
			<xsl:attribute name="partner">
				<xsl:value-of select="$partner" />
			</xsl:attribute>
        </xsl:if>
        
		<xsl:if test="$parentProfileKey != ''">
			<xsl:attribute name="parentProfileKey">
				<xsl:value-of select="$parentProfileKey" />
			</xsl:attribute>
        </xsl:if>
			<xsl:attribute name="resourceType">
<xsl:value-of select="$resourceType" />
</xsl:attribute>

			<xsl:attribute name="serverType">
<xsl:value-of select="$serverType" />
</xsl:attribute>

			<xsl:attribute name="subResourceType">
<xsl:value-of select="$subResourceType" />
</xsl:attribute>

			<xsl:element name="owningDivisions">
				<xsl:element name="SubResourceDivisionRef">
					<xsl:attribute name="sponsorDivisionKey">
<xsl:value-of select="$sponsorDivisionKey" />
</xsl:attribute>
				</xsl:element>
			</xsl:element>
		</xsl:element>
	</xsl:template>
</xsl:stylesheet> 