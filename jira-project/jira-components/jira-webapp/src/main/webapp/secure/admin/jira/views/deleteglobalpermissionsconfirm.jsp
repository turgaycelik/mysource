
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.globalpermissions.confirm.title'"/></title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_configuration"/>
    <meta name="admin.active.tab" content="global_permissions"/>
</head>

<body>

    <p>
    <table width=100% cellpadding=10 cellspacing=0 border=0>
    <page:applyDecorator name="jiraform">
        <page:param name="action">GlobalPermissions.jspa</page:param>
        <page:param name="submitId">delete_permission_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.words.delete'"/></page:param>
    	<page:param name="cancelURI">GlobalPermissions!default.jspa</page:param>
        <page:param name="title"><ww:text name="'admin.globalpermissions.confirm.title'"/></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="autoSelectFirst">false</page:param>
	    <page:param name="description">
        <input type="hidden" name="action" value="del">
        <input type="hidden" name="globalPermType" value="<ww:property value="globalPermType" />">
        <input type="hidden" name="groupName" value="<ww:property value="groupName" />">

        <ww:text name="'admin.globalpermissions.confirm.question'">
            <ww:param name="'value0'"><ww:if test="groupName == null"><ww:text name="'admin.common.words.anyone'"/></ww:if> <ww:property value="groupName" /></ww:param>
            <ww:param name="'value1'"><ww:property value="permTypeName" /></ww:param>
        </ww:text>
        </page:param>
    </page:applyDecorator>
    </table>
    </p>

</body>
</html>
