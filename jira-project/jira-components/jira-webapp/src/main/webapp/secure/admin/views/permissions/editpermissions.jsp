<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.projects.editpermissions.title'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/misc_schemes_section"/>
    <meta name="admin.active.tab" content="permission_schemes"/>
</head>

<body>
<page:applyDecorator name="jirapanel">
    <page:param name="title"><ww:text name="'admin.projects.editpermissions.title'"/> &mdash; <ww:property value="scheme/string('name')"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="helpURL">permissionsHelp</page:param>
    <page:param name="helpURLFragment">#adding_to_permission</page:param>
    <page:param name="postTitle">
        <ui:component theme="'raw'" template="projectshare.jsp" name="'name'" value="'value'" label="'label'">
            <ui:param name="'projects'" value="/usedIn"/>
        </ui:component>
    </page:param>
    <p>
        <ww:text name="'admin.projects.editpermissionschemes.description.of.page'">
            <ww:param name="'value0'">"<ww:property value="scheme/string('name')"/>"</ww:param>
        </ww:text>
    </p>
    <ul class="optionslist">
        <li><ww:text name="'admin.projects.editpermissions.grant.permission'">
            <ww:param name="'value0'"><a href="<ww:url page="AddPermission!default.jspa"><ww:param name="'schemeId'" value="scheme/string('id')"/></ww:url>"><b></ww:param>
            <ww:param name="'value1'"></b></a></ww:param>
        </ww:text></li>
        <li><ww:text name="'admin.projects.editpermissions.view.all.permission.schemes'">
            <ww:param name="'value0'"><a href="ViewPermissionSchemes.jspa"><b></ww:param>
            <ww:param name="'value1'"></b></a></ww:param>
        </ww:text></li>
    </ul>
</page:applyDecorator>

<table class="aui aui-table-rowhover" id="edit_project_permissions" data-schemeid='<ww:property value="scheme/string('id')"/>'>
    <thead>
        <tr>
            <th>
                <ww:text name="'admin.permission.group.project.permissions'"/>
            </th>
            <th width="25%">
                <ww:property value="/i18nUsersGroupsRolesHeader" />
            </th>
            <th width="10%">
                <ww:text name="'common.words.operations'"/>
            </th>
        </tr>
    </thead>
    <tbody>
    <ww:iterator value="projectPermissions" status="'status'">
        <tr>
            <td>
                <b><ww:text name="./nameI18nKey"/></b>
                <div class="description"><ww:text name="./descriptionI18nKey"/></div>
            </td>
            <td>
                <ww:if test="permissions(./key)/empty == false">
                    <ul>
                    <ww:iterator value="permissions(./key)">
                        <li>
	                        <ww:if test="/type(string('type')) != null">
		                        <ww:property value="/type(string('type'))/displayName" />
		                        <ww:property value="/formatSecurityTypeParameter(string('type'), ../../type(string('type'))/argumentDisplay(string('parameter')))" />
			                </ww:if>
			                <ww:else>
			                    <ww:text name="'admin.schemes.issuesecurity.unknown.type'">
			                    	<ww:param name="'value0'"><ww:property value="string('type')"/></ww:param>
			                    </ww:text>
			                </ww:else>
                            (<a id="del_perm_<ww:property value="./string('permissionKey')"/>_<ww:property value="./string('parameter')"/>" href="<ww:url page="DeletePermission!default.jspa"><ww:param name="'id'" value="long('id')"/><ww:param name="'schemeId'" value="schemeId"/></ww:url>"><ww:text name="'common.words.delete'"/></a>)
                        </li>
                    </ww:iterator>
                    </ul>
                </ww:if>
                <ww:else>
                    &nbsp;
                </ww:else>
            </td>
            <td>
                <ul class="operations-list">
                    <li><a id="add_perm_<ww:property value="./key"/>" href="<ww:url page="AddPermission!default.jspa"><ww:param name="'schemeId'" value="../schemeId"/><ww:param name="'permissions'" value="./key"/></ww:url>"><ww:text name="'common.forms.add'"/></a></li>
                </ul>
            </td>
        </tr>
    </ww:iterator>
    </tbody>
</table>

<table class="aui aui-table-rowhover" id="edit_issue_permissions">
    <thead>
        <tr>
            <th>
                <ww:text name="'admin.permission.group.issue.permissions'"/>
            </th>
            <th width="25%">
                <ww:property value="/i18nUsersGroupsRolesHeader" />
            </th>
            <th width="10%">
                <ww:text name="'common.words.operations'"/>
            </th>
        </tr>
    </thead>
    <tbody>
    <ww:iterator value="issuePermissions" status="'status'">
        <tr>
            <td>
                <b><ww:text name="./nameI18nKey"/></b>
                <div class="description"><ww:text name="./descriptionI18nKey"/></div>
            </td>
            <td>
            <ww:if test="permissions(./key)/empty == false">
                <ul>
                <ww:iterator value="permissions(./key)">
                    <li>
                        <ww:if test="/type(string('type')) != null">
	                        <ww:property value="/type(string('type'))/displayName" />
	                        <ww:property value="/formatSecurityTypeParameter(string('type'), ../../type(string('type'))/argumentDisplay(string('parameter')))" />
		                </ww:if>
		                <ww:else>
		                    <ww:text name="'admin.schemes.issuesecurity.unknown.type'">
		                    	<ww:param name="'value0'"><ww:property value="string('type')"/></ww:param>
		                    </ww:text>
		                </ww:else>
                        (<a id="del_perm_<ww:property value="./string('permissionKey')"/>_<ww:property value="./string('parameter')"/>" href="<ww:url page="DeletePermission!default.jspa"><ww:param name="'id'" value="long('id')"/><ww:param name="'schemeId'" value="schemeId"/></ww:url>"><ww:text name="'common.words.delete'"/></a>)
                    </li>
                </ww:iterator>
                </ul>
            </ww:if>
            <ww:else>
                &nbsp;
            </ww:else>
            </td>
            <td>
            	<ul class="operations-list">
                    <li><a id="add_perm_<ww:property value="./key"/>" href="<ww:url page="AddPermission!default.jspa"><ww:param name="'schemeId'" value="../schemeId"/><ww:param name="'permissions'" value="./key"/></ww:url>"><ww:text name="'common.forms.add'"/></a></li>
                </ul>
            </td>
        </tr>
    </ww:iterator>
    </tbody>
</table>

<table class="aui aui-table-rowhover" id="edit_votersandwatchers_permissions">
    <thead>
        <tr>
            <th>
                <ww:text name="'admin.permission.group.voters.and.watchers.permissions'"/>
            </th>
            <th width="25%">
                <ww:property value="/i18nUsersGroupsRolesHeader" />
            </th>
            <th width="10%">
                <ww:text name="'common.words.operations'"/>
            </th>
        </tr>
    </thead>
    <tbody>
        <ww:iterator value="votersAndWatchersPermissions" status="'status'">
        <tr>
            <td>
                <b><ww:text name="./nameI18nKey"/></b>
                <div class="description"><ww:text name="./descriptionI18nKey"/></div>
            </td>
            <td>
            <ww:if test="permissions(./key)/empty == false">
                <ul>
                <ww:iterator value="permissions(./key)">
                    <li>
                        <ww:if test="/type(string('type')) != null">
	                        <ww:property value="/type(string('type'))/displayName" />
	                        <ww:property value="/formatSecurityTypeParameter(string('type'), ../../type(string('type'))/argumentDisplay(string('parameter')))" />
		                </ww:if>
		                <ww:else>
		                    <ww:text name="'admin.schemes.issuesecurity.unknown.type'">
		                    	<ww:param name="'value0'"><ww:property value="string('type')"/></ww:param>
		                    </ww:text>
		                </ww:else>
                        (<a id="del_perm_<ww:property value="./string('permissionKey')"/>_<ww:property value="./string('parameter')"/>" href="<ww:url page="DeletePermission!default.jspa"><ww:param name="'id'" value="long('id')"/><ww:param name="'schemeId'" value="schemeId"/></ww:url>"><ww:text name="'common.words.delete'"/></a>)
                    </li>
                </ww:iterator>
                </ul>
            </ww:if>
            <ww:else>
                &nbsp;
            </ww:else>
            </td>
            <td>
            	<ul class="operations-list">
                    <li><a id="add_perm_<ww:property value="./key"/>" href="<ww:url page="AddPermission!default.jspa"><ww:param name="'schemeId'" value="../schemeId"/><ww:param name="'permissions'" value="./key"/></ww:url>"><ww:text name="'common.forms.add'"/></a></li>
                </ul>
            </td>
        </tr>
        </ww:iterator>
    </tbody>
</table>

<table class="aui aui-table-rowhover" id="edit_comments_permissions">
    <thead>
        <tr>
            <th>
                <ww:text name="'admin.permission.group.comments.permissions'"/>
            </th>
            <th width="25%">
                <ww:property value="/i18nUsersGroupsRolesHeader" />
            </th>
            <th width="10%">
                <ww:text name="'common.words.operations'"/>
            </th>
        </tr>
    </thead>
    <tbody>
    <ww:iterator value="commentsPermissions" status="'status'">
        <tr>
            <td>
                <b><ww:text name="./nameI18nKey"/></b>
                <div class="description"><ww:text name="./descriptionI18nKey"/></div>
            </td>
            <td>
            <ww:if test="permissions(./key)/empty == false">
                <ul>
                <ww:iterator value="permissions(./key)">
                    <li>
                        <ww:if test="/type(string('type')) != null">
	                        <ww:property value="/type(string('type'))/displayName" />
	                        <ww:property value="/formatSecurityTypeParameter(string('type'), ../../type(string('type'))/argumentDisplay(string('parameter')))" />
		                </ww:if>
		                <ww:else>
		                    <ww:text name="'admin.schemes.issuesecurity.unknown.type'">
		                    	<ww:param name="'value0'"><ww:property value="string('type')"/></ww:param>
		                    </ww:text>
		                </ww:else>
                        (<a id="del_perm_<ww:property value="./string('permissionKey')"/>_<ww:property value="./string('parameter')"/>" href="<ww:url page="DeletePermission!default.jspa"><ww:param name="'id'" value="long('id')"/><ww:param name="'schemeId'" value="schemeId"/></ww:url>"><ww:text name="'common.words.delete'"/></a>)
                    </li>
                </ww:iterator>
                </ul>
            </ww:if>
            <ww:else>
                &nbsp;
            </ww:else>
            </td>
            <td>
            	<ul class="operations-list">
                    <li><a id="add_perm_<ww:property value="./key"/>" href="<ww:url page="AddPermission!default.jspa"><ww:param name="'schemeId'" value="../schemeId"/><ww:param name="'permissions'" value="./key"/></ww:url>"><ww:text name="'common.forms.add'"/></a></li>
                </ul>
            </td>
        </tr>
    </ww:iterator>
    </tbody>
</table>

<table class="aui aui-table-rowhover" id="edit_attachments_permissions">
    <thead>
        <tr>
            <th>
                <ww:text name="'admin.permission.group.attachments.permissions'"/>
            </th>
            <th width="25%">
                <ww:property value="/i18nUsersGroupsRolesHeader" />
            </th>
            <th width="10%">
                <ww:text name="'common.words.operations'"/>
            </th>
        </tr>
    </thead>
    <tbody>
    <ww:iterator value="attachmentsPermissions" status="'status'">
        <tr>
            <td>
                <b><ww:text name="./nameI18nKey"/></b>
                <div class="description"><ww:text name="./descriptionI18nKey"/></div>
            </td>
            <td>
            <ww:if test="permissions(./key)/empty == false">
                <ul>
                <ww:iterator value="permissions(./key)">
                    <li>
                        <ww:if test="/type(string('type')) != null">
	                        <ww:property value="/type(string('type'))/displayName" />
	                        <ww:property value="/formatSecurityTypeParameter(string('type'), ../../type(string('type'))/argumentDisplay(string('parameter')))" />
		                </ww:if>
		                <ww:else>
		                    <ww:text name="'admin.schemes.issuesecurity.unknown.type'">
		                    	<ww:param name="'value0'"><ww:property value="string('type')"/></ww:param>
		                    </ww:text>
		                </ww:else>
                        (<a id="del_perm_<ww:property value="./string('permissionKey')"/>_<ww:property value="./string('parameter')"/>" href="<ww:url page="DeletePermission!default.jspa"><ww:param name="'id'" value="long('id')"/><ww:param name="'schemeId'" value="schemeId"/></ww:url>"><ww:text name="'common.words.delete'"/></a>)
                    </li>
                </ww:iterator>
                </ul>
            </ww:if>
            <ww:else>
                &nbsp;
            </ww:else>
            </td>
            <td>
            	<ul class="operations-list">
                    <li><a id="add_perm_<ww:property value="./key"/>" href="<ww:url page="AddPermission!default.jspa"><ww:param name="'schemeId'" value="../schemeId"/><ww:param name="'permissions'" value="./key"/></ww:url>"><ww:text name="'common.forms.add'"/></a></li>
                </ul>
            </td>
        </tr>
    </ww:iterator>
    </tbody>
</table>

<table class="aui aui-table-rowhover" id="edit_timetracking_permissions">
    <thead>
        <tr>
            <th>
                <ww:text name="'admin.permission.group.time.tracking.permissions'"/>
            </th>
            <th width="25%">
                <ww:property value="/i18nUsersGroupsRolesHeader" />
            </th>
            <th width="10%">
                <ww:text name="'common.words.operations'"/>
            </th>
        </tr>
    </thead>
    <tbody>
    <ww:iterator value="timeTrackingPermissions" status="'status'">
        <tr>
            <td>
                <b><ww:text name="./nameI18nKey"/></b>
                <div class="description"><ww:text name="./descriptionI18nKey"/></div>
            </td>
            <td>
            <ww:if test="permissions(./key)/empty == false">
                <ul>
                <ww:iterator value="permissions(./key)">
                    <li>
                        <ww:if test="/type(string('type')) != null">
	                        <ww:property value="/type(string('type'))/displayName" />
	                        <ww:property value="/formatSecurityTypeParameter(string('type'), ../../type(string('type'))/argumentDisplay(string('parameter')))" />
		                </ww:if>
		                <ww:else>
		                    <ww:text name="'admin.schemes.issuesecurity.unknown.type'">
		                    	<ww:param name="'value0'"><ww:property value="string('type')"/></ww:param>
		                    </ww:text>
		                </ww:else>
                        (<a id="del_perm_<ww:property value="./string('permissionKey')"/>_<ww:property value="./string('parameter')"/>" href="<ww:url page="DeletePermission!default.jspa"><ww:param name="'id'" value="long('id')"/><ww:param name="'schemeId'" value="schemeId"/></ww:url>"><ww:text name="'common.words.delete'"/></a>)
                    </li>
                </ww:iterator>
                </ul>
            </ww:if>
            <ww:else>
                &nbsp;
            </ww:else>
            </td>
            <td>
            	<ul class="operations-list">
                    <li><a id="add_perm_<ww:property value="./key"/>" href="<ww:url page="AddPermission!default.jspa"><ww:param name="'schemeId'" value="../schemeId"/><ww:param name="'permissions'" value="./key"/></ww:url>"><ww:text name="'common.forms.add'"/></a></li>
                </ul>
            </td>
        </tr>
    </ww:iterator>
    </tbody>
</table>

<ww:if test="otherPermissions/empty == false">
<table class="aui aui-table-rowhover" id="edit_other_permissions">
    <thead>
    <tr>
        <th>
            <ww:text name="'admin.permission.group.other.permissions'"/>
        </th>
        <th width="25%">
            <ww:property value="/i18nUsersGroupsRolesHeader" />
        </th>
        <th width="10%">
            <ww:text name="'common.words.operations'"/>
        </th>
    </tr>
    </thead>
    <tbody>
    <ww:iterator value="otherPermissions" status="'status'">
        <tr>
            <td>
                <b><ww:text name="./nameI18nKey"/></b>
                <div class="description"><ww:text name="./descriptionI18nKey"/></div>
            </td>
            <td>
                <ww:if test="permissions(./key)/empty == false">
                    <ul>
                        <ww:iterator value="permissions(./key)">
                            <li>
                                <ww:if test="/type(string('type')) != null">
                                    <ww:property value="/type(string('type'))/displayName" />
                                    <ww:property value="/formatSecurityTypeParameter(string('type'), ../../type(string('type'))/argumentDisplay(string('parameter')))" />
                                </ww:if>
                                <ww:else>
                                    <ww:text name="'admin.schemes.issuesecurity.unknown.type'">
                                        <ww:param name="'value0'"><ww:property value="string('type')"/></ww:param>
                                    </ww:text>
                                </ww:else>
                                (<a id="del_perm_<ww:property value="./string('permissionKey')"/>_<ww:property value="./string('parameter')"/>" href="<ww:url page="DeletePermission!default.jspa"><ww:param name="'id'" value="long('id')"/><ww:param name="'schemeId'" value="schemeId"/></ww:url>"><ww:text name="'common.words.delete'"/></a>)
                            </li>
                        </ww:iterator>
                    </ul>
                </ww:if>
                <ww:else>
                    &nbsp;
                </ww:else>
            </td>
            <td>
                <ul class="operations-list">
                    <li><a id="add_perm_<ww:property value="./key"/>" href="<ww:url page="AddPermission!default.jspa"><ww:param name="'schemeId'" value="../schemeId"/><ww:param name="'permissions'" value="./key"/></ww:url>"><ww:text name="'common.forms.add'"/></a></li>
                </ul>
            </td>
        </tr>
    </ww:iterator>
    </tbody>
</table>
</ww:if>

<ui:component theme="'raw'" template="projectsharedialog.jsp" name="'name'" value="'value'" label="'label'">
    <ui:param name="'projects'" value="/usedIn"/>
    <ui:param name="'title'"><ww:text name="'admin.project.shared.list.heading.scheme'"/></ui:param>
</ui:component>

</body>
</html>
