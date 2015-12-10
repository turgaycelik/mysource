<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.roles.view.roles'"/></title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_section"/>
    <meta name="admin.active.tab" content="project_role_browser"/>
</head>
<body>
    <page:applyDecorator name="jirapanel">
        <page:param name="title"><ww:text name="'admin.roles.view.roles'"/></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="helpURL">project_roles</page:param>
        <p><ww:text name="'admin.roles.the.table.below'"/></p>
    </page:applyDecorator>
    <ww:if test="/roles/size() > 0">
        <table class="aui aui-table-rowhover" id="project_roles">
            <thead>
                <tr>
                    <th>
                        <ww:text name="'admin.projectroles.view.project.role.name'"/>
                    </th>
                    <th>
                        <ww:text name="'common.words.description'"/>
                    </th>
                    <th>
                        <ww:text name="'common.words.operations'"/>
                    </th>
                </tr>
            </thead>
            <tbody>
                <ww:iterator value="/roles" status="'status'">
                <tr id="project-role-<ww:property value="name"/>" class="project-role-row" data-row-for="<ww:property value="name"/>" data-project-role-id="<ww:property value="id" />" >
                    <td valign=top width="20%">
                        <ww:property value="name"/>
                    </td>
                    <td>
                        <ww:property value="description"/>
                    </td>
                    <td>
                        <ul class="operations-list">
                            <li><a href="ViewProjectRoleUsage.jspa?id=<ww:property value="id"/>" id="view_<ww:property value="name"/>"><ww:text name="'admin.roles.view.usages'"/></a></li>
                            <li><a href="ViewDefaultProjectRoleActors.jspa?id=<ww:property value="id"/>" id="manage_<ww:property value="name"/>"><ww:text name="'admin.roles.manage.default.members'"/></a></li>
                            <li><a href="EditProjectRole!default.jspa?id=<ww:property value="id"/>" id="edit_<ww:property value="name"/>"><ww:text name="'common.words.edit'"/></a></li>
                            <li><a href="DeleteProjectRole!default.jspa?id=<ww:property value="id"/>" id="delete_<ww:property value="name"/>"><ww:text name="'common.words.delete'"/></a></li>
                        </ul>
                    </td>
                </tr>
                </ww:iterator>
            </tbody>
        </table>
    </ww:if>

    <aui:component template="module.jsp" theme="'aui'">
        <aui:param name="'contentHtml'">
            <page:applyDecorator name="jiraform">
                <page:param name="action">AddProjectRole.jspa</page:param>
                <page:param name="width">100%</page:param>
                <page:param name="submitId">role_submit</page:param>
                <page:param name="submitName"><ww:text name="'admin.roles.add.new.role'"/></page:param>
                <page:param name="title"><ww:text name="'admin.roles.add.new.role'"/></page:param>
                <ui:textfield label="text('common.words.name')" name="'name'" size="'30'" />
                <ui:textfield label="text('common.words.description')" name="'description'" size="'60'" />
            </page:applyDecorator>
        </aui:param>
    </aui:component>
</body>
</html>
