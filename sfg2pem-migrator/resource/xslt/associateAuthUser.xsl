<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:param name="authorizedUserProfileConfigKey" />
	
	<xsl:template match="/">
		<xsl:element name="create">
			<xsl:attribute name="managedSshKeyConfigId">
<xsl:value-of select="$authorizedUserProfileConfigKey" />
</xsl:attribute>

		</xsl:element>
	</xsl:template>
</xsl:stylesheet> 