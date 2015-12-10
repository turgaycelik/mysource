<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.roles.view.roles'"/></title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_section"/>
    <meta name="admin.active.tab" content="project_role_browser"/>
</head>

<body>

<page:applyDecorator name="jirapanel">
    <page:param name="helpURL">project_roles</page:param>
    <page:param name="helpURLFragment">#Specifying+%27default+members%27+for+a+project+role</page:param>
    <page:param name="title">
        <ww:text name="'admin.roles.default.project.roles.title'">
            <ww:param name="'value0'">
                <ww:property value="/projectRole/name"/>
            </ww:param>
        </ww:text>
    </page:param>
    <page:param name="width">100%</page:param>
    <p>
        <ww:text name="'admin.roles.default.project.roles.desc.1'"/>
    </p>
    <p>
        <ww:text name="'admin.roles.default.project.roles.desc.2'">
           <ww:param name="'value0'"><span class="note"></ww:param>
           <ww:param name="'value1'"></span></ww:param>
           <ww:param name="'value2'"><ww:property value="/projectRole/name"/></ww:param>
        </ww:text>
    </p>
    <ul class="optionslist">
        <li><a id="return_link" href="ViewProjectRoles.jspa"><ww:text name="'admin.projectroles.returnlink'"/></a></li>
    </ul>
</page:applyDecorator>
<table class="aui aui-table-rowhover" id="role_actors">
    <thead>
        <tr>
            <ww:iterator value="/roleActorTypes">
                <th width="<ww:property value="/tableWidthForRoleActorTypes(80)" />%">
                    <ww:text name="'admin.common.words.default'"/> <ww:property value="./prettyName" />
                </th>
            </ww:iterator>
        </tr>
    </thead>
    <tbody>
    <ww:property value="/projectRole" id="role">
    <tr>
        <jsp:include page="displayroleactors.jsp"/>
    </tr>
    </ww:property>
    </tbody>
</table>
</body>
</html>
