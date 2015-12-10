<?xml version="1.0"?>

<!-- Sample stylesheet which renders JIRA's RSS (XML) output in a HTML format similar to the {jiraissues} macro in Confluence. See navigator-rss.jsp for how to apply this automatically. -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<xsl:param name="url" select="'undefined.jira.url'"/>


	<xsl:variable name="lowercase" select="'abcdefghijklmnopqrstuvwxyz'" />
	<xsl:variable name="uppercase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ '" />

	<xsl:template match="/rss/channel">
		<html>
			<head>
				<link rel="stylesheet" href="http://confluence.atlassian.com/styles/main-action.css" type="text/css"/>
				<title>RSS - <xsl:value-of select="title"/></title>
			</head>
			<body>
                <xsl:for-each select="item">
                    <xsl:value-of disable-output-escaping="yes" select="description"/>
                    <br/>
                </xsl:for-each>
			</body>
		</html>
	</xsl:template>

</xsl:stylesheet>
<!-- vim: set tw=10000:-->
