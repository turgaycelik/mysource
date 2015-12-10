
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.projects.editpermissions.delete.permission'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/misc_schemes_section"/>
    <meta name="admin.active.tab" content="permission_schemes"/>
</head>

<body>

    <p>
    <table width=100% cellpadding=10 cellspacing=0 border=0>
    <page:applyDecorator name="jiraform">
        <page:param name="action">DeletePermission.jspa</page:param>
        <page:param name="submitId">delete_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.words.delete'"/></page:param>
    	<page:param name="cancelURI"><ww:url page="EditPermissions!default.jspa"><ww:param name="'schemeId'" value="schemeId"/></ww:url></page:param>
        <page:param name="title"><ww:text name="'admin.projects.editpermissions.delete.permission'"/></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="autoSelectFirst">false</page:param>
	    <page:param name="description">
        <input type="hidden" name="schemeId" value="<ww:property value="schemeId" />">
        <input type="hidden" name="id" value="<ww:property value="id" />">
        <input type="hidden" name="confirmed" value="true">
            <ww:text name="'admin.permissions.deleting.confirmation'">
                <ww:param name="'value0'">
                    <b>
                    <ww:if test="permissionDisplayName == 'Group' && permissionParameter == null">
                        <ww:text name="'common.words.anonymous'"/>
                    </ww:if>
                    <ww:else>
                        <ww:property value="permissionDisplayName"/>
                    </ww:else>
                    </b>
                </ww:param>
                <ww:param name="'value1'">
                    <ww:if test="permissionParameter != null">
                        &nbsp;(<ww:property value="permissionParameter"/>)
                    </ww:if>
                    <ww:else>
                        &nbsp;
                    </ww:else>
                </ww:param>
                <ww:param name="'value2'"><b><ww:text name="permissionName"/></b><br></ww:param>
            </ww:text>
        </page:param>
    </page:applyDecorator>
    </table>
    </p>

</body>
</html>
