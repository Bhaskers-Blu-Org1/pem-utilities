<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:param name="configurationId" />
	<xsl:param name="prodType" />
	<xsl:param name="testType" />
	<xsl:param name="prodContent" />
	<xsl:param name="testContent" />
	<xsl:param name="keyType" />
	
	<xsl:template match="/">
		<xsl:element name="create">
			<xsl:attribute name="configurationId">
<xsl:value-of select="$configurationId" />
</xsl:attribute>

			<xsl:element name="configurationTypes">
				<xsl:element name="ManagedSshKeyType">
					<xsl:attribute name="type">
<xsl:value-of select="$prodType" />
</xsl:attribute>

<xsl:attribute name="content">
<xsl:value-of select="$prodContent" />
</xsl:attribute>

 <xsl:attribute name="keyType">
<xsl:value-of select="$keyType" />
</xsl:attribute>
				</xsl:element>
				
				<xsl:element name="ManagedSshKeyType">
					<xsl:attribute name="type">
<xsl:value-of select="$testType" />
</xsl:attribute>

<xsl:attribute name="content">
<xsl:value-of select="$testContent" />
</xsl:attribute>

 <xsl:attribute name="keyType">
<xsl:value-of select="$keyType" />
</xsl:attribute>
				</xsl:element>
			</xsl:element>
		</xsl:element>
	</xsl:template>
</xsl:stylesheet> 