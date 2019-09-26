<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:param name="configurationId" />
	<xsl:param name="prodType" />
	<xsl:param name="testType" />
	<xsl:param name="userCredential" />
	<xsl:param name="userIdentityKey" />
	<xsl:param name="authorizedUserKey" />
	<xsl:param name="sshKey" />
	<xsl:param name="keyType" />
	<xsl:param name="subResourceType" />
	<xsl:param name="testInputXml" select="/"/>
	<xsl:param name="prodInputXml" select="/"/>

	<xsl:template match="/SSHRemoteProfile">

		<xsl:element name="create">
			<xsl:attribute name="configurationId">
<xsl:value-of select="$configurationId" />
</xsl:attribute>

			<xsl:element name="configurationTypes">
			
			
				<xsl:element name="SftpInboundPullType">
					<xsl:attribute name="type">
<xsl:value-of select="$prodType" />
</xsl:attribute>
					<xsl:attribute name="characterEncoding">
<xsl:value-of select="$prodInputXml/@characterEncoding" />
</xsl:attribute>
					<xsl:attribute name="compression">
<xsl:value-of select="$prodInputXml/compression/@display" />
</xsl:attribute>
					<xsl:attribute name="directory">
<xsl:value-of select="$prodInputXml/@directory" />
</xsl:attribute>

					<xsl:attribute name="host">
<xsl:value-of select="$prodInputXml/@remoteHost" />
</xsl:attribute>

					<xsl:attribute name="localPortRange">
<xsl:value-of select="$prodInputXml/@localPortRange" />
</xsl:attribute>

					<xsl:attribute name="noOfRetries">
<xsl:value-of select="$prodInputXml/@connectionRetryCount" />
</xsl:attribute>

					<xsl:attribute name="remotePort">
<xsl:value-of select="$prodInputXml/@remotePort" />
</xsl:attribute>

					<xsl:attribute name="preferredAuthenticationType">
<xsl:value-of
						select="$prodInputXml/preferredAuthenticationType/@display" />
</xsl:attribute>

					<xsl:attribute name="preferredCypher">
<xsl:value-of
						select="$testInputXml/preferredCipher/@display" />
</xsl:attribute>

					<xsl:attribute name="preferredMac">
<xsl:value-of
						select="$testInputXml/preferredMacAlgorithm/@display" />
</xsl:attribute>

					<xsl:attribute name="profileName">
<xsl:value-of select="$prodInputXml/@profileName" />
</xsl:attribute>

					<xsl:attribute name="responseTimeout">
<xsl:value-of select="$prodInputXml/@responseTimeout" />
</xsl:attribute>

					<xsl:attribute name="retryInterval">
<xsl:value-of select="$prodInputXml/@retryDelay" />
</xsl:attribute>

	<xsl:choose>
						<xsl:when test="$userIdentityKey !=null">
					<xsl:attribute name="userIdentityKey">
<xsl:value-of select="$userIdentityKey" />
</xsl:attribute>
</xsl:when>

					</xsl:choose>
					
	<xsl:choose>
						<xsl:when test="$authorizedUserKey !=null">
					<xsl:attribute name="authorizedUserKey">
<xsl:value-of select="$authorizedUserKey" />
</xsl:attribute>
</xsl:when>

					</xsl:choose>				


	<xsl:choose>
						<xsl:when test="$userCredential !=null">
					<xsl:attribute name="userCredential">
<xsl:value-of select="$userCredential" />
</xsl:attribute>

	</xsl:when>

					</xsl:choose>


					<xsl:choose>
						<xsl:when test="$sshKey !=null">
							<xsl:element name="sshKeys">
								<xsl:element name="SftpInboundPullTypeKey">
									<xsl:attribute name="keyType">
<xsl:value-of select="$keyType" />
</xsl:attribute>
									<xsl:attribute name="sshKey">
<xsl:value-of select="$sshKey" />
</xsl:attribute>

								</xsl:element>

							</xsl:element>
						</xsl:when>

					</xsl:choose>
				</xsl:element>
		
		
				
				<xsl:element name="SftpInboundPullType">
					<xsl:attribute name="type">
<xsl:value-of select="$testType" />
</xsl:attribute>
					<xsl:attribute name="characterEncoding">
<xsl:value-of select="$testInputXml/@characterEncoding" />
</xsl:attribute>
					<xsl:attribute name="compression">
<xsl:value-of select="$testInputXml/compression/@display" />
</xsl:attribute>
					<xsl:attribute name="directory">
<xsl:value-of select="$testInputXml/@directory" />
</xsl:attribute>

					<xsl:attribute name="host">
<xsl:value-of select="$testInputXml/@remoteHost" />
</xsl:attribute>

					<xsl:attribute name="localPortRange">
<xsl:value-of select="$testInputXml/@localPortRange" />
</xsl:attribute>

					<xsl:attribute name="noOfRetries">
<xsl:value-of select="$testInputXml/@connectionRetryCount" />
</xsl:attribute>

					<xsl:attribute name="remotePort">
<xsl:value-of select="$testInputXml/@remotePort" />
</xsl:attribute>

					<xsl:attribute name="preferredAuthenticationType">
<xsl:value-of
						select="$testInputXml/preferredAuthenticationType/@display" />
</xsl:attribute>

					<xsl:attribute name="preferredCypher">
<xsl:value-of
						select="$testInputXml/preferredCipher/@display" />
</xsl:attribute>

					<xsl:attribute name="preferredMac">
<xsl:value-of
						select="$testInputXml/preferredMacAlgorithm/@display" />
</xsl:attribute>

					<xsl:attribute name="profileName">
<xsl:value-of select="$testInputXml/@profileName" />
</xsl:attribute>

					<xsl:attribute name="responseTimeout">
<xsl:value-of select="$testInputXml/@responseTimeout" />
</xsl:attribute>

					<xsl:attribute name="retryInterval">
<xsl:value-of select="$testInputXml/@retryDelay" />
</xsl:attribute>

	<xsl:choose>
						<xsl:when test="$userIdentityKey !=null">
					<xsl:attribute name="userIdentityKey">
<xsl:value-of select="$userIdentityKey" />
</xsl:attribute>
</xsl:when>

					</xsl:choose>

<xsl:choose>
						<xsl:when test="$authorizedUserKey !=null">
					<xsl:attribute name="authorizedUserKey">
<xsl:value-of select="$authorizedUserKey" />
</xsl:attribute>
</xsl:when>

					</xsl:choose>	
	<xsl:choose>
						<xsl:when test="$userCredential !=null">
					<xsl:attribute name="userCredential">
<xsl:value-of select="$userCredential" />
</xsl:attribute>

	</xsl:when>

					</xsl:choose>


					<xsl:choose>
						<xsl:when test="$sshKey !=null">
							<xsl:element name="sshKeys">
								<xsl:element name="SftpInboundPullTypeKey">
									<xsl:attribute name="keyType">
<xsl:value-of select="$keyType" />
</xsl:attribute>
									<xsl:attribute name="sshKey">
<xsl:value-of select="$sshKey" />
</xsl:attribute>

								</xsl:element>

							</xsl:element>
						</xsl:when>

					</xsl:choose>



				</xsl:element>

			</xsl:element>
		</xsl:element>
	</xsl:template>
</xsl:stylesheet>
