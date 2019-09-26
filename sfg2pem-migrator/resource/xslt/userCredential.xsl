<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:param name="configurationId" />
	<xsl:param name="testType" />
	<xsl:param name="prodType" />
	<xsl:param name="prodUsername" />
	<xsl:param name="testUsername" />
	<xsl:param name="passphrase" />

	<xsl:template match="/">
		<xsl:element name="create">
			<xsl:attribute name="configurationId">
<xsl:value-of select="$configurationId" />
</xsl:attribute>
			<xsl:element name="configurationTypes">
				<xsl:element name="ManagedUserCredentialType">
					<xsl:attribute name="type">
<xsl:value-of select="$testType" />
</xsl:attribute>

					<xsl:attribute name="username">
<xsl:value-of select="$testUsername" />
</xsl:attribute>

					<xsl:attribute name="passphrase">
<xsl:value-of select="$passphrase" />
</xsl:attribute>
				</xsl:element>
				
				<xsl:element name="ManagedUserCredentialType">
					<xsl:attribute name="type">
<xsl:value-of select="$prodType" />
</xsl:attribute>

					<xsl:attribute name="username">
<xsl:value-of select="$prodUsername" />
</xsl:attribute>

					<xsl:attribute name="passphrase">
<xsl:value-of select="$passphrase" />
</xsl:attribute>
				</xsl:element>
			</xsl:element>
		</xsl:element>
	</xsl:template>
</xsl:stylesheet> 