<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title><ww:text name="'admin.edituserproperties.edit.property'"><ww:param name="'value0'"><ww:property value="/key"/></ww:param></ww:text></title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_section"/>
    <meta name="admin.active.tab" content="user_browser"/>
</head>
<body>

    <page:applyDecorator id="user-properties-edit" name="auiform">
        <page:param name="action">EditUserProperty!update.jspa</page:param>
        <page:param name="submitButtonText"><ww:text name="'common.forms.update'"/></page:param>
        <page:param name="submitButtonName">Update</page:param>
        <page:param name="cancelLinkURI"><ww:url page="EditUserProperties.jspa"><ww:param name="'name'" value="name"/></ww:url></page:param>

        <aui:component template="formHeading.jsp" theme="'aui'">
            <aui:param name="'text'"><ww:text name="'admin.edituserproperties.edit.property'"><ww:param name="'value0'"><ww:property value="/key"/></ww:param></ww:text></aui:param>
        </aui:component>

        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">info</aui:param>
            <aui:param name="'messageHtml'">
                <p>
                    <ww:text name="'admin.edituserproperty.description'">
                        <ww:param name="'value0'"><b><ww:property value="/key"/></b></ww:param>
                        <ww:param name="'value0'"><b><ww:property value="/user/displayName"/></b></ww:param>
                    </ww:text>
                </p>
            </aui:param>
        </aui:component>

        <page:applyDecorator name="auifieldgroup">
            <aui:textfield label="text('common.words.value')" name="'value'" size="20" theme="'aui'" />
        </page:applyDecorator>

        <aui:component name="'name'" template="hidden.jsp" theme="'aui'" />
        <aui:component name="'key'" template="hidden.jsp" theme="'aui'" />

    </page:applyDecorator>
</body>
</html>
