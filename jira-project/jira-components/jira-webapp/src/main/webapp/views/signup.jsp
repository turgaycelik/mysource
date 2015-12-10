<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="ui" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib prefix="page" uri="sitemesh-page" %>
<%
    WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
    webResourceManager.requireResource("jira.webresources:captcha");
%>
<html>
<head>
    <title><ww:text name="'signup.title'"/></title>
</head>
<body class="aui-page-focused aui-page-focused-medium">
    <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanel'">
        <ui:param name="'content'">
            <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanelContent'">
                <ui:param name="'content'">
                    <h1><ww:text name="'signup.heading'"/></h1>
                    <page:applyDecorator id="signup" name="auiform">
                        <page:param name="action">Signup.jspa</page:param>
                        <page:param name="submitButtonName">Signup</page:param>
                        <page:param name="submitButtonText"><ww:text name="'signup.heading'"/></page:param>
                        <page:param name="cancelLinkURI"><ww:url value="'default.jsp'" atltoken="false"/></page:param>


                        <page:applyDecorator name="auifieldgroup">
                            <aui:textfield id="'fullname'" label="text('common.words.fullname')" mandatory="'true'" maxlength="'255'" name="'fullname'" theme="'aui'"/>
                        </page:applyDecorator>
                        <page:applyDecorator name="auifieldgroup">
                            <aui:textfield id="'email'" label="text('common.words.email')" mandatory="'true'" maxlength="'255'" name="'email'" theme="'aui'"/>
                        </page:applyDecorator>
                        <page:applyDecorator name="auifieldgroup">
                            <aui:textfield id="'username'" label="text('common.words.username')" mandatory="'true'" maxlength="'255'" name="'username'" theme="'aui'"/>
                        </page:applyDecorator>
                        <page:applyDecorator name="auifieldgroup">
                            <aui:password id="'password'" label="text('common.words.password')" mandatory="'true'" maxlength="'255'" name="'password'" theme="'aui'"/>
                            <ww:if test="/passwordErrors/size > 0"><ul class="error"><ww:iterator value="/passwordErrors">
                                <li><ww:property value="./snippet" escape="false"/></li>
                            </ww:iterator></ul></ww:if>
                        </page:applyDecorator>
                        <page:applyDecorator name="auifieldgroup">
                            <aui:password id="'confirm'" label="text('signup.confirmPassword')" mandatory="'true'" maxlength="'255'" name="'confirm'" theme="'aui'"/>
                        </page:applyDecorator>

                        <ww:if test="applicationProperties/option('jira.option.captcha.on.signup') == true">
                            <page:applyDecorator id="'captcha'" name="auifieldgroup">
                                <aui:component label="text('signup.captcha.text')" id="'os_captcha'" name="'captcha'" template="captcha.jsp" theme="'aui'">
                                    <aui:param name="'captchaURI'"><%= request.getContextPath() %>/captcha</aui:param>
                                    <aui:param name="'iconText'"><ww:text name="'admin.common.words.refresh'"/></aui:param>
                                    <aui:param name="'iconCssClass'">icon-reload reload</aui:param>
                                </aui:component>
                            </page:applyDecorator>
                        </ww:if>

                    </page:applyDecorator>
                </ui:param>
            </ui:soy>
        </ui:param>
    </ui:soy>
</body>
</html>
