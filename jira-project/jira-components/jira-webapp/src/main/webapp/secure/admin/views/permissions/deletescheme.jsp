<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.schemes.permissions.delete.permission.scheme'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/misc_schemes_section"/>
    <meta name="admin.active.tab" content="permission_schemes"/>
</head>

<body>

    <p>
    <table width=100% cellpadding=10 cellspacing=0 border=0>
    <page:applyDecorator name="jiraform">
        <page:param name="action">DeletePermissionScheme.jspa</page:param>
        <page:param name="submitId">delete_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.words.delete'"/></page:param>
    	<page:param name="cancelURI">ViewPermissionSchemes.jspa</page:param>
        <page:param name="title"><ww:text name="'admin.schemes.permissions.delete.permission.scheme'"/></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="autoSelectFirst">false</page:param>
	    <page:param name="description">
        <input type="hidden" name="schemeId" value="<ww:property value="schemeId" />">
        <input type="hidden" name="confirmed" value="true">
        <ww:if test="errorMessages/size == 0" >
            <p>
            <ww:text name="'admin.schemes.delete.confirmation'">
                <ww:param name="'value0'"><u><ww:property value="name" /></u></ww:param>
            </ww:text><br>
            <ww:if test="description" >
                "<ww:property value="description" />"
            </ww:if>
            </p>

            <ww:if test="active == true">
                <p><ww:text name="'admin.schemes.permissions.note'">
                    <ww:param name="'value0'"><ww:property value="name" /></ww:param>
                </ww:text>
                <ww:iterator value="projects(schemeObject)" status="'liststatus'">
                    <a href="<%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="key"/>/summary">
                <ww:property value="name" /></a><ww:if test="@liststatus/last == false">, </ww:if><ww:else>.</ww:else>
                </ww:iterator><br> <br>
                <ww:text name="'admin.schemes.permissions.if.you.delete.this.scheme'"/></p>
            </ww:if>
        </ww:if>
        </page:param>
    </page:applyDecorator>
    </table>
    </p>

</body>
</html>
