<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <title><ww:text name="'resetpassword.title'"/></title>
    <meta name="decorator" content="message" />
</head>
<body>
<div class="content">
    <aui:component template="formHeading.jsp" theme="'aui'">
        <aui:param name="'text'"><ww:text name="'resetpassword.title'"/></aui:param>
        <aui:param name="'cssClass'">resetpassword-title</aui:param>
    </aui:component>
    <page:applyDecorator id="reset-password" name="auiform">
        <page:param name="title"><ww:text name="'resetpassword.title'"/></page:param>
        <page:param name="action">ResetPassword.jspa</page:param>

        <ww:if test="/tokenInvalid == false && /tokenTimedOut == false">
            <page:param name="submitButtonName">Reset</page:param>
            <page:param name="submitButtonText"><ww:text name="'admin.common.words.reset'"/></page:param>
            <page:applyDecorator name="auifieldset">
                <page:param name="legend"><ww:text name="'resetpassword.title'"/></page:param>

                <page:applyDecorator name="auifieldgroup">
                    <aui:component id="'user-name'" label="text('common.words.username')" name="'/os_username'" template="formFieldValue.jsp" theme="'aui'" />
                </page:applyDecorator>

                <page:applyDecorator name="auifieldgroup">
                    <aui:password label="text('resetpassword.password')" name="'password'" theme="'aui'"/>
                    <ww:if test="/passwordErrors/size > 0"><ul class="error"><ww:iterator value="/passwordErrors">
                        <li><ww:property value="./snippet" escape="false"/></li>
                    </ww:iterator></ul></ww:if>
                </page:applyDecorator>

                <page:applyDecorator name="auifieldgroup">
                    <aui:password label="text('resetpassword.confirm')" name="'confirm'" theme="'aui'"/>
                </page:applyDecorator>
            </page:applyDecorator>

            <aui:component name="'os_username'" value="/os_username" template="hidden.jsp" theme="'aui'"/>
            <aui:component name="'token'" value="/token" template="hidden.jsp" theme="'aui'"/>
        </ww:if>
        <ww:else>
            <div id="reset-password-get-new-token">
                <ww:text name="'resetpassword.error.get.new.token'">
                    <ww:param name="value0"><a id="reset-password-get-new-token-link" href="ForgotLoginDetails.jspa"></ww:param>
                    <ww:param name="value1"></a></ww:param>
                </ww:text>
            </div>
        </ww:else>
    </page:applyDecorator>
</div>
</body>
</html>
