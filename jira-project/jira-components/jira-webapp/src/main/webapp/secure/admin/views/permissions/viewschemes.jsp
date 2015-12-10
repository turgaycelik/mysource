<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.schemes.permissions.permission.schemes'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/misc_schemes_section"/>
    <meta name="admin.active.tab" content="permission_schemes"/>
</head>

<body>
<page:applyDecorator name="jirapanel">
    <page:param name="title"><ww:text name="'admin.schemes.permissions.permission.schemes'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="helpURL">permissionsHelp</page:param>

    <p>
    <ww:text name="'admin.schemes.permissions.description'"/>
    <p>
    <ww:text name="'admin.schemes.permissions.description2'"/>
    </p>
    <ww:text name="'admin.schemes.permissions.table.below'">
        <ww:param name="'value0'"><a href="<%= request.getContextPath() %>/secure/admin/jira/GlobalPermissions!default.jspa"></ww:param>
        <ww:param name="'value1'"></a></ww:param>
    </ww:text>
    <p>
</page:applyDecorator>

<ww:if test="schemes/size == 0">
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">info</aui:param>
        <aui:param name="'messageHtml'"><ww:text name="'admin.schemes.permissions.no.schemes.configured'"/></aui:param>
    </aui:component>
</ww:if>
<ww:else>
<table id="permission_schemes_table" class="aui aui-table-rowhover">
    <thead>
        <tr>
            <th>
                <ww:text name="'common.words.name'"/>
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
    <ww:iterator value="schemes" status="'status'">
        <tr>
            <td>
                <b><a href="EditPermissions!default.jspa?schemeId=<ww:property value="long('id')"/>"><ww:property value="string('name')"/></a></b>
                <div class="description"><ww:property value="string('description')"/></div>
            </td>
            <td>
                <ww:if test="/projects(.)/empty == false">
                    <ul>
                    <ww:iterator value="/projects(.)">
                        <li><a href="<%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="./string('key')"/>/summary"><ww:property value="string('name')" /></a></li>
                    </ww:iterator>
                    </ul>
                </ww:if>
                <ww:else>
                    &nbsp;
                </ww:else>
            </td>
            <td>
                <ul class="operations-list">
                    <li><a id="<ww:property value="long('id')"/>_edit" href="EditPermissions!default.jspa?schemeId=<ww:property value="long('id')"/>" title="<ww:text name="'admin.schemes.permissions.change.permissions.for.scheme'"/>"><ww:text name="'admin.common.words.permissions'"/></a></li>
                    <li><a id="<ww:property value="long('id')"/>_copy" href="<ww:url page="CopyPermissionScheme.jspa"><ww:param name="'schemeId'" value="long('id')"/></ww:url>" title="<ww:text name="'admin.schemes.permissions.make.a.copy.of.scheme'"/>"><ww:text name="'common.words.copy'"/></a></li>
                    <li><a id="<ww:property value="long('id')"/>_edit_details" href="EditPermissionScheme!default.jspa?schemeId=<ww:property value="long('id')"/>" title="<ww:text name="'admin.schemes.permissions.edit.name.and.description.of.scheme'"/>"><ww:text name="'common.words.edit'"/></a></li>
                <%-- You cannot delete the default permission scheme (0) --%>
                <ww:if test="long('id') != 0">
                    <li><a id="del_<ww:property value="string('name')"/>" href="DeletePermissionScheme!default.jspa?schemeId=<ww:property value="long('id')"/>" title="<ww:text name="'admin.schemes.permissions.delete.this.scheme'"/>"><ww:text name="'common.words.delete'"/></a></li>
                </ww:if>
                </ul>
            </td>
        </tr>
    </ww:iterator>
    </tbody>
</table>
</ww:else>
<div class="buttons-container aui-toolbar form-buttons noprint">
    <div class="toolbar-group">
        <span class="toolbar-item">
            <a class="toolbar-trigger" href="AddPermissionScheme!default.jspa"><ww:text name="'admin.schemes.permissions.add.permission.schemes'"/></a>
        </span>
    </div>
</div>
</body>
</html>
