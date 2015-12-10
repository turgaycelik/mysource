<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.deleteuserproperty.delete.property'"><ww:param name="'value0'"><ww:property value="/key"/></ww:param></ww:text></title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_section"/>
    <meta name="admin.active.tab" content="user_browser"/>
</head>
<body>
    <page:applyDecorator id="user-properties-delete" name="auiform">
        <page:param name="action">DeleteUserProperty.jspa</page:param>
        <page:param name="submitButtonText"><ww:text name="'common.words.delete'"/></page:param>
        <page:param name="submitButtonName">Delete</page:param>
        <page:param name="cancelLinkURI"><ww:url page="EditUserProperties.jspa"><ww:param name="'name'" value="name"/></ww:url></page:param>
        <page:param name="cssClass">top-label</page:param>

        <aui:component template="formHeading.jsp" theme="'aui'">
            <aui:param name="'text'"><ww:text name="'admin.deleteuserproperty.delete.property'"><ww:param name="'value0'"><ww:property value="/key"/></ww:param></ww:text></aui:param>
        </aui:component>

        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">warning</aui:param>
            <aui:param name="'messageHtml'">
                <p>
                    <ww:text name="'admin.deleteuserproperty.description'">
                        <ww:param name="'value0'"><b><ww:property value="/key"/></b></ww:param>
                        <ww:param name="'value0'"><b><ww:property value="/user/displayName"/></b></ww:param>
                    </ww:text>
                </p>
            </aui:param>
        </aui:component>

        <aui:component name="'name'" template="hidden.jsp" theme="'aui'" />
        <aui:component name="'key'" template="hidden.jsp" theme="'aui'" />
        <aui:component name="'confirm'" value="'true'" template="hidden.jsp" theme="'aui'"  />
    </page:applyDecorator>
</body>
</html>
