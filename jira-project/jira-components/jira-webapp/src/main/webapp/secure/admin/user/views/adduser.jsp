<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.adduser.add.new.user'"/></title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_section"/>
    <meta name="admin.active.tab" content="user_browser"/>
    <meta name="decorator" content="panel-admin"/>
</head>
<body>
    <page:applyDecorator id="user-create" name="auiform">
        <page:param name="action">AddUser.jspa</page:param>
        <page:param name="method">post</page:param>
        <page:param name="submitButtonText"><ww:text name="'common.forms.create'"/></page:param>
        <page:param name="submitButtonName">Create</page:param>
        <page:param name="cancelLinkURI">UserBrowser.jspa</page:param>

        <aui:component template="formHeading.jsp" theme="'aui'">
            <aui:param name="'text'"><ww:text name="'admin.adduser.add.new.user'"/></aui:param>
        </aui:component>

        <ww:property value="/userCountWebPanelHtml" escape="false"/>

        <page:applyDecorator name="auifieldgroup">
            <aui:textfield label="text('common.words.username')" mandatory="true" maxlength="255" id="'username'" name="'username'" theme="'aui'" />
        </page:applyDecorator>
        <ww:if test="/directories/size > 1">
            <page:applyDecorator name="auifieldgroup">
                <aui:select label="text('admin.user.directory')" id="'directoryId'" name="'directoryId'" list="/directories" listKey="'id'" listValue="'name'" theme="'aui'" />
            </page:applyDecorator>
        </ww:if>

        <!-- Hide the password fields if the user is being created in a user directory which cannot set a password -->
        <ww:if test="/hasPasswordWritableDirectory == true">
            <page:applyDecorator name="auifieldgroup">
                <page:param name="description"><ww:text name="'admin.adduser.if.you.do.not.enter.a.password'"/></page:param>
                <aui:password label="text('common.words.password')" id="'password'" name="'password'" theme="'aui'">
                    <aui:param name="'autocomplete'" value="'off'"/>
                </aui:password>
                <ww:if test="/passwordErrors/size > 0"><ul class="error"><ww:iterator value="/passwordErrors">
                    <li><ww:property value="./snippet" escape="false"/></li>
                </ww:iterator></ul></ww:if>
            </page:applyDecorator>
            <page:applyDecorator name="auifieldgroup">
                <aui:password label="text('common.forms.confirm')" id="'confirm'" name="'confirm'" theme="'aui'">
                    <aui:param name="'autocomplete'" value="'off'"/>
                </aui:password>
            </page:applyDecorator>
        </ww:if>

        <page:applyDecorator name="auifieldgroup">
            <aui:textfield label="text('common.words.fullname')" id="'fullname'" mandatory="true" maxlength="255" name="'fullname'" theme="'aui'" />
        </page:applyDecorator>
        <page:applyDecorator name="auifieldgroup">
            <aui:textfield label="text('common.words.email')" id="'email'" mandatory="true" maxlength="255" name="'email'" theme="'aui'" />
        </page:applyDecorator>
        <page:applyDecorator name="auifieldset">
            <page:param name="type">group</page:param>
            <page:applyDecorator name="auifieldgroup">
                <page:param name="type">checkbox</page:param>
                <page:param name="description"><ww:text name="'admin.adduser.send.password.email.description'"/></page:param>
                <aui:checkbox label="text('admin.adduser.send.password.email')"  id="'sendEmail'" name="'sendEmail'" fieldValue="'true'" theme="'aui'">
                    <aui:param name="'labelBefore'">false</aui:param>
                </aui:checkbox>
            </page:applyDecorator>
        </page:applyDecorator>

        <ww:property value="/webPanelHtml" escape="false"/>

    </page:applyDecorator>
    <ww:if test="/directories/size > 1">
    <script>

        var canDirectoryUpdatePasswordMap = {};

        <ww:iterator value="/canDirectoryUpdatePasswordMap/entrySet">
            canDirectoryUpdatePasswordMap['<ww:property value="./key"/>'] = <ww:property value="./value"/>;
        </ww:iterator>
    
    // Disable the password fields depending on the Directory option selected
    function directoryChanged() {
        var directorySelect = AJS.$('#user-create-directoryId');
        if (directorySelect)
        {
            var passwordField = AJS.$("#user-create-password");
            var confirmField = AJS.$("#user-create-confirm");

            var directoryId = directorySelect.val();
            var passwordEnabled = canDirectoryUpdatePasswordMap[directoryId];

            passwordField.attr("disabled", !passwordEnabled);
            confirmField.attr("disabled", !passwordEnabled);
        }
    }

    AJS.$('#user-create-directoryId').change(directoryChanged).change();

</script>
</ww:if>
</body>
</html>
