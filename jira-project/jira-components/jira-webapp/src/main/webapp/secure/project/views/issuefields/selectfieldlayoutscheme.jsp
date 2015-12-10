<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.projects.fieldconfigscheme.select.field.config.scheme'"/></title>
    <meta name="admin.active.section" content="atl.jira.proj.config"/>
</head>

<body>

<ww:if test="/fieldLayoutSchemes == null || /fieldLayoutSchemes/empty == true">
    <page:applyDecorator name="jirapanel">
        <page:param name="title"><ww:text name="'admin.projects.fieldconfigscheme.field.layout.config.association'"/></page:param>
        <page:param name="width">100%</page:param>
        <p>
        <ww:text name="'admin.projects.fieldconfigscheme.none.set.up'"/>
        </p>
        <p>
        <ww:text name="'admin.projects.fieldconfigscheme.add'">
            <ww:param name="'value0'"><a id="add-new-scheme" href="AddFieldConfigurationScheme!default.jspa"></ww:param>
            <ww:param name="'value1'"></a></ww:param>
        </ww:text>
        </p>
    </page:applyDecorator>
</ww:if>
<ww:else>
	<page:applyDecorator name="jiraform">
		<page:param name="title"><ww:text name="'admin.projects.fieldconfigscheme.field.layout.config.association'"/></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="cancelURI"><%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="/project/key"/>/fields</page:param>
        <page:param name="action">SelectFieldLayoutScheme.jspa</page:param>
        <page:param name="submitId">associate_submit</page:param>
        <page:param name="submitName"><ww:text name="'admin.projects.schemes.associate'"/></page:param>
        <page:param name="description">
            <ww:text name="'admin.projects.fieldconfigscheme.page.description'">
                <ww:param name="'value0'"><a href="<%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="/project/key"/>/fields"><b><ww:property value="/project/name"/></b></a></ww:param>
            </ww:text>
        </page:param>

        <ui:select label="text('admin.common.words.scheme')" name="'schemeId'" list="/fieldLayoutSchemes" listKey="'./id'" listValue="'./name'">
            <ui:param name="'headerrow'" value="text('admin.projects.system.default.field.config')" />
            <ui:param name="'headervalue'" value="''" />
        </ui:select>

        <ui:component name="'projectId'" template="hidden.jsp"/>
	</page:applyDecorator>
</ww:else>

</body>
</html>
