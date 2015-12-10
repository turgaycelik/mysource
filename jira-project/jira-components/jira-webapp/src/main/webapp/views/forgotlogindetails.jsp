<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib prefix="page" uri="sitemesh-page" %>
<html>
<head>
    <title><ww:text name="'common.concepts.forgotpassword'"/></title>
    <meta name="decorator" content="message" />
</head>
<body>
    <header>
        <h1><ww:text name="'common.concepts.forgotpassword'"/></h1>
    </header>
    <page:applyDecorator id="forgot-login" name="auiform">
        <page:param name="action">ForgotLoginDetails.jspa</page:param>
        <page:param name="submitButtonName">Send</page:param>
        <page:param name="submitButtonText"><ww:text name="'forgotpassword.submit'"/></page:param>
        <page:param name="cancelLinkURI"><ww:url value="'/secure/MyJiraHome.jspa'" atltoken="false"/></page:param>

        <aui:component template="formDescriptionBlock.jsp" theme="'aui'">
            <aui:param name="'messageHtml'">
                <p><ww:text name="'forgotlogindetails.description'"/></p>
            </aui:param>
        </aui:component>

        <page:applyDecorator name="auifieldset">
            <page:param name="legend"><ww:text name="'forgotlogindetails.fieldset.legend.which'"/></page:param>
            <page:param name="type">group</page:param>

            <page:applyDecorator name="auifieldgroup">
                <page:param name="type">matrix</page:param>

                <page:applyDecorator name="auifieldgroup">
                    <page:param name="type">radio</page:param>
                    <ww:if test="/checked('forgotPassword') == true">
                        <ww:property id="forgot-password-checked" value="'true'"/>
                    </ww:if>
                    <aui:radio checked="@forgot-password-checked" id="'rb-forgot-password'"
                               label="text('forgotlogindetails.my.password')" list="null" name="'forgotten'"
                               theme="'aui'" value="'forgotPassword'"/>
                </page:applyDecorator>

                <page:applyDecorator name="auifieldgroup">
                    <page:param name="type">radio</page:param>
                    <ww:if test="/checked('forgotUserName') == true">
                        <ww:property id="forgot-username-checked" value="'true'"/>
                    </ww:if>
                    <aui:radio checked="@forgot-username-checked" id="'rb-forgot-username'"
                               label="text('forgotlogindetails.my.username')" list="null" name="'forgotten'"
                               theme="'aui'" value="'forgotUserName'"/>
                </page:applyDecorator>
            </page:applyDecorator>
        </page:applyDecorator>

        <page:applyDecorator id="password" name="auifieldgroup">
            <page:param name="cssClass">hidden</page:param>
            <aui:textfield id="'username'" label="text('forgotlogindetails.fields.username')" mandatory="'true'"
                           name="'username'" theme="'aui'"/>
        </page:applyDecorator>

        <page:applyDecorator id="username" name="auifieldgroup">
            <page:param name="cssClass">hidden</page:param>
            <aui:textfield id="'email'" label="text('forgotlogindetails.fields.email')" mandatory="'true'" name="'email'"
                           theme="'aui'"/>
        </page:applyDecorator>
    </page:applyDecorator>
</body>
</html>
