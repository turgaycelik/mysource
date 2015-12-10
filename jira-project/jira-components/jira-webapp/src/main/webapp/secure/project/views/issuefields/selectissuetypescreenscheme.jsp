<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.itss.select.issue.type.screen.scheme'"/></title>
    <meta name="admin.active.section" content="atl.jira.proj.config"/>
</head>

<body>

<page:applyDecorator name="jiraform">
    <page:param name="title"><ww:text name="'admin.itss.issue.type.screen.scheme.association'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="cancelURI"><%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="/project/string('key')"/>/screens</page:param>
    <page:param name="action">SelectIssueTypeScreenScheme.jspa</page:param>
    <page:param name="submitId">associate_submit</page:param>
    <page:param name="submitName"><ww:text name="'admin.projects.schemes.associate'"/></page:param>
    <page:param name="description">
        <ww:text name="'admin.itss.page.purpose'">
            <ww:param name="'value0'"><a href="<%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="/project/string('key')"/>/screens"><b><ww:property value="/project/string('name')"/></b></a></ww:param>
        </ww:text>
    </page:param>

    <ui:select label="'Scheme'" name="'schemeId'" list="/issueTypeScreenSchemes" listKey="'./id'" listValue="'./name'" />

    <ui:component name="'projectId'" template="hidden.jsp"/>
</page:applyDecorator>

</body>
</html>
