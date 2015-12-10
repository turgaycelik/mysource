
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.projects.notificationschemes.title'"/></title>
    <meta name="admin.active.section" content="atl.jira.proj.config"/>
</head>

<body>

<ww:if test="schemes == null || schemes/size == 0">
    <page:applyDecorator name="jirapanel">
        <page:param name="title"><ww:text name="'admin.projects.notificationschemes.shortheading'"/></page:param>
        <page:param name="width">100%</page:param>

        <p>
        <ww:text name="'admin.projects.notificationschemes.noschemes'"/>
        </p>
        <p>
            <ww:text name="'admin.projects.notificationschemes.add.new.scheme'">
                <ww:param name="value0"><a href="<%= request.getContextPath() %>/secure/admin/ViewNotificationSchemes.jspa"></ww:param>
                <ww:param name="value1"></a></ww:param>
            </ww:text>
        </p>
    </page:applyDecorator>
</ww:if>
<ww:else>
	<page:applyDecorator name="jiraform">
		<page:param name="title"><ww:text name="'admin.projects.notificationschemes.shortheading'"/></page:param>
        <page:param name="description">
            <ww:text name="'admin.projects.notificationschemes.longheading'"/>
        </page:param>
        <page:param name="width">100%</page:param>
        <page:param name="cancelURI"><%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="/project/string('key')"/>/notifications</page:param>
        <page:param name="action">SelectProjectScheme.jspa</page:param>
        <page:param name="submitId">associate_submit</page:param>
        <page:param name="submitName"><ww:text name="'admin.projects.schemes.associate'"/></page:param>

        <ui:select label="/text('admin.projects.schemes.scheme')" name="'schemeIds'" value="schemeIds[0]" list="schemes" listKey="'string('id')'" listValue="'string('name')'" template="selectmap.jsp">
            <ui:param name="'headerrow'" value="/text('common.words.none')" />
            <ui:param name="'headervalue'" value="''" />
        </ui:select>
        <ui:component name="'projectId'" template="hidden.jsp"/>
	</page:applyDecorator>
</ww:else>

</body>
</html>
