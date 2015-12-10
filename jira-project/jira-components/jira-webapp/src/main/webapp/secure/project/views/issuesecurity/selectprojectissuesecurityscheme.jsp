<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.iss.security.scheme'"/></title>
    <meta name="admin.active.section" content="atl.jira.proj.config"/>
</head>
<body>
    <ww:if test="schemes == null || schemes/size == 0">
        <page:applyDecorator name="jirapanel">
            <page:param name="title"><ww:text name="'admin.iss.associate.security.scheme.to.project'"/></page:param>
            <page:param name="width">100%</page:param>
            <p>
            <ww:text name="'admin.iss.no.schemes.set.up'"/>
            </p>
            <p><ww:text name="'admin.iss.add.new.issue.security.scheme'">
                <ww:param name="'value0'"><a href="<%= request.getContextPath() %>/secure/admin/ViewIssueSecuritySchemes.jspa"></ww:param>
                <ww:param name="'value1'"></a></ww:param>
            </ww:text>
            </p>
        </page:applyDecorator>
    </ww:if>
    <ww:else>
        <page:applyDecorator name="jiraform">
            <page:param name="title"><ww:text name="'admin.iss.associate.security.scheme.to.project'"/></page:param>
            <page:param name="description">
                <p><ww:text name="'admin.iss.step1'">
                    <ww:param name="'value0'"><b></ww:param>
                    <ww:param name="'value1'"></b></ww:param>
                </ww:text></p>
    <%--            page allows you to associate a Issue Security scheme with this project.--%>
            </page:param>
            <page:param name="width">100%</page:param>
            <page:param name="cancelURI"><%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="/project/string('key')"/>/issuesecurity</page:param>
            <page:param name="action">SelectProjectSecuritySchemeStep2!default.jspa</page:param>
            <page:param name="submitId">next_submit</page:param>
            <page:param name="submitName">Next >></page:param>

            <ui:select label="'Scheme'" name="'newSchemeId'" value="schemeIds[0]" list="schemes" listKey="'string('id')'" listValue="'string('name')'">
                <ui:param name="'headerrow'" value="'None'" />
                <ui:param name="'headervalue'" value="-1" />
            </ui:select>
            <ui:component name="'projectId'" template="hidden.jsp"/>

            <input type="hidden" name="origSchemeId" value="<ww:property value="schemeId" />">
        </page:applyDecorator>
    </ww:else>
</body>
</html>
