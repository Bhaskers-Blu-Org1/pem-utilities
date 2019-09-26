<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:param name="configurationId" />
	<xsl:param name="testType" />
	<xsl:param name="prodType" />
	<xsl:param name="inbound" />
	<xsl:param name="outbound" />
	<xsl:param name="AuthenticationHost" />
	<xsl:param name="SFGCommunityName" />
	<xsl:param name="PartnerPrefix" />
	<xsl:param name="AuthenticationHostProdVal" />
	<xsl:param name="AuthenticationHostTestVal" />
	<xsl:param name="SFGCommunityNameProdVal" />
	<xsl:param name="SFGCommunityNameTestVal" />
	
	<xsl:param name="ProdPartnerPrefixVal" />
	<xsl:param name="TestPartnerPrefixVal" />

	<xsl:param name="managedSshKeyProfileConfigKey" />
	<xsl:param name="userIdentityKeyProfileConfigKey" />
	<xsl:param name="userCredentialProfileConfigKey" />
	<xsl:param name="sftpInbPullProfileConfigKey" />

	<xsl:param name="managedSshKeyProfileRefName" />
	<xsl:param name="userIdentityKeyProfileRefName" />
	<xsl:param name="userCredentialProfileRefName" />
	<xsl:param name="sftpProfileRefName" />

	<xsl:param name="managedSshKeyProfileRefType" />
	<xsl:param name="userIdentityKeyProfileRefType" />
	<xsl:param name="userCredentialProfileRefType" />
	<xsl:param name="sftpProfileRefType" />
	
	<xsl:param name="sftpOutProfileRefName" />
	<xsl:param name="sftpOutProfileRefType" />
	<xsl:param name="sftpOutbPushProfileConfigKey" />
	
	<xsl:param name="SFGPartnerKey" />
	<xsl:param name="ProdSFGPartnerKey" />
	<xsl:param name="TestSFGPartnerKey" />
	
	<xsl:param name="sshAuthorizedUserKeyProfileConfigKey" />
	<xsl:param name="sftpInbPushProfileConfigKey" />
	<xsl:param name="sftpOutbPullProfileConfigKey" />
	
	<xsl:param name="hostIdentityKeyyProfileConfigKey" />
	<xsl:param name="hostIdentityKeyProfileRefName" />
	<xsl:param name="hostIdentityKeyProfileRefType" />

	<xsl:template match="/">
		<xsl:element name="create">
			<xsl:attribute name="configurationId">
<xsl:value-of select="$configurationId" />
</xsl:attribute>

			<xsl:element name="configurationTypes">
				<xsl:element name="SystemType">
					<xsl:attribute name="type">
<xsl:value-of select="$testType" />
</xsl:attribute>

					<xsl:attribute name="inbound">
<xsl:value-of select="$inbound" />
</xsl:attribute>

					<xsl:attribute name="outbound">
<xsl:value-of select="$outbound" />
</xsl:attribute>

					<xsl:element name="extensions">
					
								<xsl:element name="SystemTypeExtn">


									<xsl:attribute name="extensionName">
<xsl:value-of select="$AuthenticationHost" />
</xsl:attribute>

									<xsl:attribute name="extensionValue">
<xsl:value-of select="$AuthenticationHostTestVal" />
</xsl:attribute>

								</xsl:element>
					
						<xsl:element name="SystemTypeExtn">
							<xsl:attribute name="extensionName">
<xsl:value-of select="$SFGCommunityName" />
</xsl:attribute>

							<xsl:attribute name="extensionValue">
<xsl:value-of select="$SFGCommunityNameTestVal" />
</xsl:attribute>
						</xsl:element>
						<xsl:element name="SystemTypeExtn">
							<xsl:attribute name="extensionName">
<xsl:value-of select="$PartnerPrefix" />
</xsl:attribute>

							<xsl:attribute name="extensionValue">
<xsl:value-of select="$TestPartnerPrefixVal" />
</xsl:attribute>
						</xsl:element>
						
							<xsl:element name="SystemTypeExtn">
							<xsl:attribute name="extensionName">
<xsl:value-of select="$SFGPartnerKey" />
</xsl:attribute>

							<xsl:attribute name="extensionValue">
<xsl:value-of select="$TestSFGPartnerKey" />
</xsl:attribute>
						</xsl:element>
					</xsl:element>


					<xsl:element name="systemProfileRefs">

						<xsl:choose>
							<xsl:when test="$sftpInbPullProfileConfigKey !=null">
								<xsl:element name="SystemProfileRef">
									<xsl:attribute name="profileRefName">
<xsl:value-of select="$sftpProfileRefName" />
</xsl:attribute>
									<xsl:attribute name="profileRefType">
<xsl:value-of select="$sftpProfileRefType" />
</xsl:attribute>
									<xsl:attribute name="profileRef">
<xsl:value-of select="$sftpInbPullProfileConfigKey" />
</xsl:attribute>
								</xsl:element>

							</xsl:when>

						</xsl:choose>
						
						<xsl:choose>
							<xsl:when test="$sftpInbPushProfileConfigKey !=null">
								<xsl:element name="SystemProfileRef">
									<xsl:attribute name="profileRefName">
<xsl:value-of select="$sftpProfileRefName" />
</xsl:attribute>
									<xsl:attribute name="profileRefType">
<xsl:value-of select="$sftpProfileRefType" />
</xsl:attribute>
									<xsl:attribute name="profileRef">
<xsl:value-of select="$sftpInbPushProfileConfigKey" />
</xsl:attribute>
								</xsl:element>

							</xsl:when>

						</xsl:choose>
						
						<xsl:choose>
							<xsl:when test="$sftpOutbPullProfileConfigKey !=null">
								<xsl:element name="SystemProfileRef">
									<xsl:attribute name="profileRefName">
<xsl:value-of select="$sftpOutProfileRefName" />
</xsl:attribute>
									<xsl:attribute name="profileRefType">
<xsl:value-of select="$sftpOutProfileRefType" />
</xsl:attribute>
									<xsl:attribute name="profileRef">
<xsl:value-of select="$sftpOutbPullProfileConfigKey" />
</xsl:attribute>
								</xsl:element>

							</xsl:when>

						</xsl:choose>
						
						<xsl:choose>
							<xsl:when test="$sftpOutbPushProfileConfigKey !=null">
								<xsl:element name="SystemProfileRef">
									<xsl:attribute name="profileRefName">
<xsl:value-of select="$sftpOutProfileRefName" />
</xsl:attribute>
									<xsl:attribute name="profileRefType">
<xsl:value-of select="$sftpOutProfileRefType" />
</xsl:attribute>
									<xsl:attribute name="profileRef">
<xsl:value-of select="$sftpOutbPushProfileConfigKey" />
</xsl:attribute>
								</xsl:element>

							</xsl:when>

						</xsl:choose>
						<xsl:choose>
							<xsl:when test="$managedSshKeyProfileConfigKey !=null">
								<xsl:element name="SystemProfileRef">
									<xsl:attribute name="profileRefName">
<xsl:value-of select="$managedSshKeyProfileRefName" />
</xsl:attribute>
									<xsl:attribute name="profileRefType">
<xsl:value-of select="$managedSshKeyProfileRefType" />
</xsl:attribute>
									<xsl:attribute name="profileRef">
<xsl:value-of select="$managedSshKeyProfileConfigKey" />
</xsl:attribute>
								</xsl:element>

							</xsl:when>

						</xsl:choose>
						
						<xsl:choose>
							<xsl:when test="$sshAuthorizedUserKeyProfileConfigKey !=null">
								<xsl:element name="SystemProfileRef">
									<xsl:attribute name="profileRefName">
<xsl:value-of select="$managedSshKeyProfileRefName" />
</xsl:attribute>
									<xsl:attribute name="profileRefType">
<xsl:value-of select="$managedSshKeyProfileRefType" />
</xsl:attribute>
									<xsl:attribute name="profileRef">
<xsl:value-of select="$sshAuthorizedUserKeyProfileConfigKey" />
</xsl:attribute>
								</xsl:element>

							</xsl:when>

						</xsl:choose>
						
						<xsl:choose>
							<xsl:when test="$hostIdentityKeyyProfileConfigKey !=null">
								<xsl:element name="SystemProfileRef">
									<xsl:attribute name="profileRefName">
<xsl:value-of select="$hostIdentityKeyProfileRefName" />
</xsl:attribute>
									<xsl:attribute name="profileRefType">
<xsl:value-of select="$hostIdentityKeyProfileRefType" />
</xsl:attribute>
									<xsl:attribute name="profileRef">
<xsl:value-of select="$hostIdentityKeyyProfileConfigKey" />
</xsl:attribute>
								</xsl:element>

							</xsl:when>

						</xsl:choose>

						<xsl:choose>
							<xsl:when test="$userIdentityKeyProfileConfigKey !=null">
								<xsl:element name="SystemProfileRef">
									<xsl:attribute name="profileRefName">
<xsl:value-of select="$userIdentityKeyProfileRefName" />
</xsl:attribute>
									<xsl:attribute name="profileRefType">
<xsl:value-of select="$userIdentityKeyProfileRefType" />
</xsl:attribute>
									<xsl:attribute name="profileRef">
<xsl:value-of select="$userIdentityKeyProfileConfigKey" />
</xsl:attribute>
								</xsl:element>

							</xsl:when>

						</xsl:choose>

						<xsl:choose>
							<xsl:when test="$userCredentialProfileConfigKey !=null">
								<xsl:element name="SystemProfileRef">
									<xsl:attribute name="profileRefName">
<xsl:value-of select="$userCredentialProfileRefName" />
</xsl:attribute>
									<xsl:attribute name="profileRefType">
<xsl:value-of select="$userCredentialProfileRefType" />
</xsl:attribute>
									<xsl:attribute name="profileRef">
<xsl:value-of select="$userCredentialProfileConfigKey" />
</xsl:attribute>
								</xsl:element>

							</xsl:when>

						</xsl:choose>
					</xsl:element>

				</xsl:element>
				
				
				<xsl:element name="SystemType">
					<xsl:attribute name="type">
<xsl:value-of select="$prodType" />
</xsl:attribute>

					<xsl:attribute name="inbound">
<xsl:value-of select="$inbound" />
</xsl:attribute>

					<xsl:attribute name="outbound">
<xsl:value-of select="$outbound" />
</xsl:attribute>

					<xsl:element name="extensions">
					
								<xsl:element name="SystemTypeExtn">


									<xsl:attribute name="extensionName">
<xsl:value-of select="$AuthenticationHost" />
</xsl:attribute>

									<xsl:attribute name="extensionValue">
<xsl:value-of select="$AuthenticationHostProdVal" />
</xsl:attribute>

								</xsl:element>
						
						<xsl:element name="SystemTypeExtn">
							<xsl:attribute name="extensionName">
<xsl:value-of select="$SFGCommunityName" />
</xsl:attribute>

							<xsl:attribute name="extensionValue">
<xsl:value-of select="$SFGCommunityNameProdVal" />
</xsl:attribute>
						</xsl:element>
						<xsl:element name="SystemTypeExtn">
							<xsl:attribute name="extensionName">
<xsl:value-of select="$PartnerPrefix" />
</xsl:attribute>

							<xsl:attribute name="extensionValue">
<xsl:value-of select="$ProdPartnerPrefixVal" />
</xsl:attribute>
						</xsl:element>
						
							<xsl:element name="SystemTypeExtn">
							<xsl:attribute name="extensionName">
<xsl:value-of select="$SFGPartnerKey" />
</xsl:attribute>

							<xsl:attribute name="extensionValue">
<xsl:value-of select="$ProdSFGPartnerKey" />
</xsl:attribute>
						</xsl:element>
					</xsl:element>


					<xsl:element name="systemProfileRefs">

						<xsl:choose>
							<xsl:when test="$sftpInbPullProfileConfigKey !=null">
								<xsl:element name="SystemProfileRef">
									<xsl:attribute name="profileRefName">
<xsl:value-of select="$sftpProfileRefName" />
</xsl:attribute>
									<xsl:attribute name="profileRefType">
<xsl:value-of select="$sftpProfileRefType" />
</xsl:attribute>
									<xsl:attribute name="profileRef">
<xsl:value-of select="$sftpInbPullProfileConfigKey" />
</xsl:attribute>
								</xsl:element>

							</xsl:when>

						</xsl:choose>
						
							<xsl:choose>
							<xsl:when test="$sftpOutbPushProfileConfigKey !=null">
								<xsl:element name="SystemProfileRef">
									<xsl:attribute name="profileRefName">
<xsl:value-of select="$sftpOutProfileRefName" />
</xsl:attribute>
									<xsl:attribute name="profileRefType">
<xsl:value-of select="$sftpOutProfileRefType" />
</xsl:attribute>
									<xsl:attribute name="profileRef">
<xsl:value-of select="$sftpOutbPushProfileConfigKey" />
</xsl:attribute>
								</xsl:element>

							</xsl:when>

						</xsl:choose>
						
						<xsl:choose>
							<xsl:when test="$sftpInbPushProfileConfigKey !=null">
								<xsl:element name="SystemProfileRef">
									<xsl:attribute name="profileRefName">
<xsl:value-of select="$sftpProfileRefName" />
</xsl:attribute>
									<xsl:attribute name="profileRefType">
<xsl:value-of select="$sftpProfileRefType" />
</xsl:attribute>
									<xsl:attribute name="profileRef">
<xsl:value-of select="$sftpInbPushProfileConfigKey" />
</xsl:attribute>
								</xsl:element>

							</xsl:when>

						</xsl:choose>
						
						<xsl:choose>
							<xsl:when test="$sftpOutbPullProfileConfigKey !=null">
								<xsl:element name="SystemProfileRef">
									<xsl:attribute name="profileRefName">
<xsl:value-of select="$sftpOutProfileRefName" />
</xsl:attribute>
									<xsl:attribute name="profileRefType">
<xsl:value-of select="$sftpOutProfileRefType" />
</xsl:attribute>
									<xsl:attribute name="profileRef">
<xsl:value-of select="$sftpOutbPullProfileConfigKey" />
</xsl:attribute>
								</xsl:element>

							</xsl:when>

						</xsl:choose>
						
						<xsl:choose>
							<xsl:when test="$sshAuthorizedUserKeyProfileConfigKey !=null">
								<xsl:element name="SystemProfileRef">
									<xsl:attribute name="profileRefName">
<xsl:value-of select="$managedSshKeyProfileRefName" />
</xsl:attribute>
									<xsl:attribute name="profileRefType">
<xsl:value-of select="$managedSshKeyProfileRefType" />
</xsl:attribute>
									<xsl:attribute name="profileRef">
<xsl:value-of select="$sshAuthorizedUserKeyProfileConfigKey" />
</xsl:attribute>
								</xsl:element>

							</xsl:when>

						</xsl:choose>
						<xsl:choose>
							<xsl:when test="$managedSshKeyProfileConfigKey !=null">
								<xsl:element name="SystemProfileRef">
									<xsl:attribute name="profileRefName">
<xsl:value-of select="$managedSshKeyProfileRefName" />
</xsl:attribute>
									<xsl:attribute name="profileRefType">
<xsl:value-of select="$managedSshKeyProfileRefType" />
</xsl:attribute>
									<xsl:attribute name="profileRef">
<xsl:value-of select="$managedSshKeyProfileConfigKey" />
</xsl:attribute>
								</xsl:element>

							</xsl:when>

						</xsl:choose>
						
						<xsl:choose>
							<xsl:when test="$hostIdentityKeyyProfileConfigKey !=null">
								<xsl:element name="SystemProfileRef">
									<xsl:attribute name="profileRefName">
<xsl:value-of select="$hostIdentityKeyProfileRefName" />
</xsl:attribute>
									<xsl:attribute name="profileRefType">
<xsl:value-of select="$hostIdentityKeyProfileRefType" />
</xsl:attribute>
									<xsl:attribute name="profileRef">
<xsl:value-of select="$hostIdentityKeyyProfileConfigKey" />
</xsl:attribute>
								</xsl:element>

							</xsl:when>

						</xsl:choose>

						<xsl:choose>
							<xsl:when test="$userIdentityKeyProfileConfigKey !=null">
								<xsl:element name="SystemProfileRef">
									<xsl:attribute name="profileRefName">
<xsl:value-of select="$userIdentityKeyProfileRefName" />
</xsl:attribute>
									<xsl:attribute name="profileRefType">
<xsl:value-of select="$userIdentityKeyProfileRefType" />
</xsl:attribute>
									<xsl:attribute name="profileRef">
<xsl:value-of select="$userIdentityKeyProfileConfigKey" />
</xsl:attribute>
								</xsl:element>

							</xsl:when>

						</xsl:choose>

						<xsl:choose>
							<xsl:when test="$userCredentialProfileConfigKey !=null">
								<xsl:element name="SystemProfileRef">
									<xsl:attribute name="profileRefName">
<xsl:value-of select="$userCredentialProfileRefName" />
</xsl:attribute>
									<xsl:attribute name="profileRefType">
<xsl:value-of select="$userCredentialProfileRefType" />
</xsl:attribute>
									<xsl:attribute name="profileRef">
<xsl:value-of select="$userCredentialProfileConfigKey" />
</xsl:attribute>
								</xsl:element>

							</xsl:when>

						</xsl:choose>
					</xsl:element>

				</xsl:element>


			</xsl:element>
		</xsl:element>

	</xsl:template>
</xsl:stylesheet> 