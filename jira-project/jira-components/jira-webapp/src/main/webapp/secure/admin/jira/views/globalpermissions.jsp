<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="webwork" prefix="ww" %>
<html>
<head>
	<title><ww:text name="'admin.globalpermissions.global.permissions'"/></title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_configuration"/>
    <meta name="admin.active.tab" content="global_permissions"/>
</head>
<body>
<page:applyDecorator name="jirapanel">
    <page:param name="title"><ww:text name="'admin.globalpermissions.global.permissions'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="helpURL">global_permissions</page:param>
    <p>
    <ww:text name="'admin.globalpermissions.description'"/>
    <ww:text name="'admin.globalpermissions.instruction'">
        <ww:param name="'value0'"><a href="<%= request.getContextPath() %>/secure/admin/ViewPermissionSchemes.jspa"></ww:param>
        <ww:param name="'value1'"></a></ww:param>
    </ww:text>
    </p>
</page:applyDecorator>
<jsp:include page="/includes/panels/permissionslist.jsp" />
</body>
</html>

