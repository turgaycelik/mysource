<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.project.categories.view.project.categories'"/></title>
    <meta name="admin.active.section" content="admin_project_menu/project_section"/>
    <meta name="admin.active.tab" content="view_categories"/>
</head>

<body>

<page:applyDecorator name="jirapanel">
    <page:param name="title"><ww:text name="'admin.project.categories.view.project.categories'"/></page:param>
    <page:param name="width">100%</page:param>

    <p>
    <ww:text name="'admin.project.categories.description.of.table'"/>
    </p>
</page:applyDecorator>


<table class="aui aui-table-rowhover">
    <thead>
        <tr>
            <th>
                <ww:text name="'common.words.name'"/>
            </th>
            <th>
                <ww:text name="'common.words.description'"/>
            </th>
            <th>
                <ww:text name="'common.concepts.projects'"/>
            </th>
            <th>
                <ww:text name="'common.words.operations'"/>
            </th>
        </tr>
    </thead>
    <tbody>
    <ww:iterator value="/projectManager/projectCategories" status="'status'">
        <tr>
            <td><b><ww:property value="string('name')"/></b></td>
            <td><ww:property value="string('description')"/></td>
            <td>
            <ww:iterator value="/projectManager/projectsFromProjectCategory(.)">
                <a href="<%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="./string('key')"/>/summary"><ww:property value="string('name')" /></a><br>
            </ww:iterator>
            </td>
            <td>
                <ul class="operations-list">
                    <li><a href="EditProjectCategory!default.jspa?id=<ww:property value="string('id')"/>"><ww:text name="'common.words.edit'"/></a></li>
                    <li><a id="del_<ww:property value="string('name')"/>" href="DeleteProjectCategory!default.jspa?id=<ww:property value="string('id')"/>"><ww:text name="'common.words.delete'"/></a></li>
                </ul>
            </td>
        </tr>
    </ww:iterator>
    </tbody>
</table>

<aui:component template="module.jsp" theme="'aui'">
    <aui:param name="'contentHtml'">
        <page:applyDecorator name="jiraform">
            <page:param name="action">AddProjectCategory.jspa</page:param>
            <page:param name="width">100%</page:param>
            <page:param name="submitId">add_submit</page:param>
            <page:param name="submitName"><ww:text name="'common.forms.add'"/></page:param>
            <page:param name="title"><ww:text name="'admin.project.categories.add.new.project.category'"/></page:param>

            <ui:textfield label="text('common.words.name')" name="'name'" size="'30'" />

            <ui:textfield label="text('common.words.description')" name="'description'" size="'60'" />
        </page:applyDecorator>
    </aui:param>
</aui:component>

</body>
</html>
