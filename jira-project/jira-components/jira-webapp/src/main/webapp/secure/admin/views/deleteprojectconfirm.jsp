
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.deleteproject.delete.project'"/>: <ww:property value="project/string('name')" /></title>
    <meta name="admin.active.section" content="admin_project_menu/project_section"/>
    <meta name="admin.active.tab" content="view_projects"/>
</head>

<body>

<page:applyDecorator name="jiraform">
    <page:param name="title"><ww:text name="'admin.deleteproject.delete.project'"/>: <ww:property value="project/string('name')" /></page:param>
	<page:param name="description">
		<p><ww:text name="'admin.deleteproject.confirmation'"/></p>
		<p><font color=#cc0000>
            <ww:if test="/systemAdministrator == true">
                <ww:text name="'admin.deleteproject.warning'">
                    <ww:param name="'value0'"><a href="XmlBackup!default.jspa"></ww:param>
                    <ww:param name="'value1'"></a></ww:param>
                </ww:text>
            </ww:if>
            <ww:else>
                <ww:text name="'admin.deleteproject.warning.admin'">
                    <ww:param name="'value0'"> </ww:param>
                    <ww:param name="'value1'"> </ww:param>
                </ww:text>
            </ww:else>
        </font></p>
	</page:param>

	<page:param name="width">100%</page:param>
	<page:param name="action">DeleteProject.jspa</page:param>
	<page:param name="submitId">delete_submit</page:param>
	<page:param name="submitName"><ww:text name="'common.words.delete'"/></page:param>
	<page:param name="cancelURI"><%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="/project/string('key')"/>/summary</page:param>
	<page:param name="autoSelectFirst">false</page:param>

	<ui:component name="'pid'" template="hidden.jsp" />
	<input type="hidden" name="confirm" value="true">
</page:applyDecorator>

</body>
</html>
