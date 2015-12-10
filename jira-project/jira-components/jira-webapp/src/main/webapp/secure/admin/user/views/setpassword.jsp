<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.setpassword.set.password'"/>: <ww:property value="user/displayName" /></title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_section"/>
    <meta name="admin.active.tab" content="user_browser"/>
</head>
<body>
    <page:applyDecorator id="user-edit-password" name="auiform">
        <page:param name="action">SetPassword.jspa</page:param>
        <page:param name="submitButtonText"><ww:text name="'common.forms.update'"/></page:param>
        <page:param name="submitButtonName">Update</page:param>
        <page:param name="cancelLinkURI"><ww:url value="'ViewUser.jspa'"><ww:param name="'name'" value="user/name" /></ww:url></page:param>

        <aui:component template="formHeading.jsp" theme="'aui'">
            <aui:param name="'text'"><ww:text name="'admin.setpassword.set.password'"/>: <ww:property value="user/displayName" escape="false" /></aui:param>
        </aui:component>

        <page:applyDecorator name="auifieldgroup">
            <aui:password label="text('common.words.password')" name="'password'" theme="'aui'">
                <aui:param name="'autocomplete'" value="'off'"/>
            </aui:password>
            <ww:if test="/passwordErrors/size > 0"><ul class="error"><ww:iterator value="/passwordErrors">
                <li><ww:property value="./snippet" escape="false"/></li>
            </ww:iterator></ul></ww:if>
        </page:applyDecorator>

        <page:applyDecorator name="auifieldgroup">
            <aui:password label="text('common.forms.confirm')" name="'confirm'" theme="'aui'">
                <aui:param name="'autocomplete'" value="'off'"/>
            </aui:password>
        </page:applyDecorator>

        <aui:component name="'name'" template="hidden.jsp" theme="'aui'" />
    </page:applyDecorator>
</body>
</html>
