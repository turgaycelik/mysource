<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.projectroles.delete.name'"/>: <ww:property value="/role/name" /></title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_section"/>
    <meta name="admin.active.tab" content="project_role_browser"/>
</head>
<body>
    <page:applyDecorator id="user-role-delete" name="auiform">
        <page:param name="action">DeleteProjectRole.jspa</page:param>
        <page:param name="submitButtonText"><ww:text name="'common.words.delete'"/></page:param>
        <page:param name="submitButtonName">Delete</page:param>
        <page:param name="cancelLinkURI">ViewProjectRoles.jspa</page:param>

        <aui:component template="formHeading.jsp" theme="'aui'">
            <aui:param name="'text'"><ww:text name="'admin.projectroles.delete.name'"/>: <ww:property value="/role/name" /></aui:param>
            <aui:param name="'escape'">false</aui:param>
        </aui:component>

        <p>
            <ww:text name="'admin.projectroles.delete.confirm'">
               <ww:param name="'value0'"><ww:property value="/role/name"/></ww:param>
            </ww:text>
        </p>
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">warning</aui:param>
            <aui:param name="'messageHtml'"><ww:text name="'admin.projectroles.delete.warning'"/></aui:param>
        </aui:component>

        <jsp:include page="associatedschemestables.jsp"/>

        <input type="hidden" value="<ww:property value="/role/id"/>" name="id" />
    </page:applyDecorator>
</body>
</html>
