
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.project.categories.delete.title'">
	    <ww:param name="'value0'"><ww:property value="projectManager/projectCategory(id)/string('name')" /></ww:param>
	</ww:text></title>
    
    <meta name="admin.active.section" content="admin_project_menu/project_section"/>
    <meta name="admin.active.tab" content="view_categories"/>
</head>

<body>

<page:applyDecorator name="jiraform">
    <page:param name="title"><ww:text name="'admin.project.categories.delete.title'">
	    <ww:param name="'value0'"><ww:property value="projectManager/projectCategory(id)/string('name')" /></ww:param>
	</ww:text></page:param>
	<page:param name="description">
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">warning</aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:text name="'admin.project.categories.delete.confirmation'"/></p>
            </aui:param>
        </aui:component>
	</page:param>
	<page:param name="width">100%</page:param>
	<page:param name="action">DeleteProjectCategory.jspa</page:param>
	<page:param name="submitId">delete_submit</page:param>
	<page:param name="submitName"><ww:text name="'common.words.delete'"/></page:param>
	<page:param name="cancelURI">ViewProjectCategories!default.jspa</page:param>
	<page:param name="autoSelectFirst">false</page:param>

	<ui:component name="'id'" template="hidden.jsp" theme="'single'"  />
	<ui:component name="'confirm'" value="'true'" template="hidden.jsp" theme="'single'"  />

</page:applyDecorator>

</body>
</html>
