<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib prefix="jira" uri="jiratags" %>

<%
// don't show ANYTHING to the user if they come here looking for trouble
if (com.atlassian.jira.util.JiraUtils.isSetup()) {
%>
<%--
Leave this as a raw HTML. Do not use response.getWriter() or response.getOutputStream() here as this will fail
on Orion. Let the application server figure out how it want to output this text.
--%>
JIRA has already been set up.
<%
} else {
%>
<html>
<head>
	<title><ww:text name="'setup.title'" /></title>
    <meta name="ajs-server-id" content="<ww:property value='/serverId'/>" />
</head>

<body class="jira-setup-page-mail-notifications">

<ww:if test="/actionName == 'VerifySmtpServerConnection' || /actionName == 'VerifyPopServerConnection'" >
    <%--Some other JS caused this to be hidden but once we have tested we need it back--%>
    <script type="text/javascript">
        AJS.$(function() {
            AJS.$('#test-connection-messages').show();
        })
    </script>
    <div id="test-connection-messages">
        <ww:if test="/hasAnyErrors == false">
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">success</aui:param>
                <aui:param name="'messageHtml'">
                    <p><ww:text name="'admin.mailserver.verify.success'"/></p>
                </aui:param>
            </aui:component>
            <ww:if test="/anonymous == true">
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">warning</aui:param>
                    <aui:param name="'messageHtml'">
                        <p><ww:text name="'admin.mailserver.verify.anonymous'"><ww:param name="'value0'"><ww:property value="/serverName"/></ww:param></ww:text></p>
                    </aui:param>
                </aui:component>
            </ww:if>
        </ww:if>
        <ww:else>
            <ww:if test="/hasErrorMessages == true && /hasErrors==false">
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">error</aui:param>
                    <aui:param name="'messageHtml'">
                        <p><ww:text name="'admin.mailserver.verify.failure.connection'"/></p>
                        <ul>
                            <ww:iterator id="error" value="/errorMessages">
                                <li><ww:property value="."/></li>
                            </ww:iterator>
                        </ul>
                    </aui:param>
                </aui:component>
            </ww:if>
        </ww:else>
    </div>
</ww:if>

<page:applyDecorator id="jira-setupwizard" name="auiform">
    <page:param name="action">SetupMailNotifications.jspa</page:param>
    <page:param name="useCustomButtons">true</page:param>
    <ww:if test="/actionName == 'VerifySmtpServerConnection'">
       <page:param name="enableFormErrors"><ww:property value="/hasErrors"/></page:param>
    </ww:if>

    <aui:component template="formHeading.jsp" theme="'aui'">
        <aui:param name="'text'"><ww:text name="'setup3.title'"/></aui:param>
    </aui:component>

    <img class="setup-banner" src="<%=request.getContextPath()%>/images/setup/setup-email-banner.png"/>

    <div>
        <p><ww:text name="'setup3.desc'"/></p>
    </div>

    <aui:component name="'analytics-enabled'" value="analyticsEnabled" template="hidden.jsp" theme="'aui'" />

    <page:applyDecorator name="auifieldset">
        <page:param name="type">group</page:param>
        <page:param name="legend"><ww:text name="'setup3.email.noficiations'" /></page:param>
        <page:param name="cssClass">inline-radio-options mail-notifications-radio-options</page:param>

        <page:applyDecorator name="auifieldgroup">
            <page:param name="type">radio</page:param>
            <aui:radio id="'email-notifications-disabled'" label="text('setup3.email.notifications.no')" list="null" name="'noemail'" theme="'aui'">
                <aui:param name="'customValue'">true</aui:param>
                <ww:if test="noemail == true">
                    <aui:param name="'checked'">true</aui:param>
                </ww:if>
            </aui:radio>
        </page:applyDecorator>

        <page:applyDecorator name="auifieldgroup">
            <page:param name="type">radio</page:param>
            <aui:radio id="'email-notifications-enabled'" label="text('setup3.email.notifications.yes')" list="null" name="'noemail'" theme="'aui'">
                <aui:param name="'customValue'">false</aui:param>
                <ww:if test="noemail == false">
                    <aui:param name="'checked'">true</aui:param>
                </ww:if>
            </aui:radio>
        </page:applyDecorator>
    </page:applyDecorator>

    <div id="setup-notification-fields" class="hidden">

        <page:applyDecorator name="auifieldgroup">
            <aui:textfield label="text('setup3.name.label')" name="'name'" theme="'aui'" />
            <page:param name="description"><ww:text name="'setup3.name.desc'"/></page:param>
        </page:applyDecorator>

        <page:applyDecorator name="auifieldgroup">
            <ui:textfield label="text('setup3.fromaddress.label')" name="'from'" theme="'aui'" />
            <page:param name="description"><ww:text name="'setup3.fromaddress.desc'"/></page:param>
        </page:applyDecorator>

        <page:applyDecorator name="auifieldgroup">
            <ui:textfield label="text('setup3.emailprefix.label')" name="'prefix'" theme="'aui'" />
            <page:param name="description"><ww:text name="'setup3.emailprefix.desc'"/></page:param>
        </page:applyDecorator>

        <page:applyDecorator name="auifieldset">
            <page:param name="type">group</page:param>
            <page:param name="legend"><ww:text name="'setup3.server.type'" /></page:param>
            <page:param name="cssClass">inline-radio-options</page:param>

            <page:applyDecorator name="auifieldgroup">
                <page:param name="type">radio</page:param>
                <aui:radio id="'email-notifications-smtp'" label="text('setup3.smtphost')" list="null" name="'mailservertype'" theme="'aui'">
                    <aui:param name="'customValue'">smtp</aui:param>
                    <ww:if test="mailservertype == 'smtp'">
                        <aui:param name="'checked'">true</aui:param>
                    </ww:if>
                </aui:radio>
            </page:applyDecorator>

            <page:applyDecorator name="auifieldgroup">
                <page:param name="type">radio</page:param>
                <aui:radio id="'email-notifications-jndi'" label="text('setup3.jndilocation')" list="null" name="'mailservertype'" theme="'aui'">
                    <aui:param name="'customValue'">jndi</aui:param>
                    <ww:if test="mailservertype == 'jndi'">
                        <aui:param name="'checked'">true</aui:param>
                    </ww:if>
                </aui:radio>
            </page:applyDecorator>
        </page:applyDecorator>

        <div id="email-notifications-smtp-fields" class="setup-fields">

            <page:applyDecorator name="auifieldgroup" id="serviceProvider-container">
                <aui:select label="text('admin.mailservers.service.provider')" name="'serviceProvider'" list="./supportedServiceProviders" listKey="'key'" listValue="'text(value)'" size="'medium'" theme="'aui'"/>
            </page:applyDecorator>

            <page:applyDecorator name="auifieldgroup">
                <page:param name="description"><ww:text name="'setup3.hostname.desc'"/></page:param>
                <aui:textfield label="text('setup3.hostname.label')" name="'serverName'" theme="'aui'" />
            </page:applyDecorator>

            <page:applyDecorator name="auifieldgroup" id="protocol-container">
                <page:param name="description"><ww:text name="'admin.mailservers.protocol.description'"/></page:param>
                <aui:select label="text('admin.mailservers.protocol')" name="'protocol'" list="./supportedClientProtocols(types[1])" listKey="'protocol'" listValue="'.'" size="'medium'" theme="'aui'" />
            </page:applyDecorator>

            <page:applyDecorator name="auifieldgroup">
                <page:param name="description"><ww:text name="'setup3.smtpport.desc'"/></page:param>
                <aui:textfield label="text('setup3.smtpport.label')" name="'port'" size="'short'" theme="'aui'" />
            </page:applyDecorator>

            <page:applyDecorator name="auifieldgroup">
                <page:param name="description"><ww:text name="'admin.mailservers.host.timeout.description'"/></page:param>
                <aui:textfield label="text('admin.mailservers.host.timeout')" name="'timeout'" size="'short'" theme="'aui'" />
            </page:applyDecorator>

            <div id="tls-option">
            <page:applyDecorator name="auifieldgroup">
                <page:param name="cssClass">tls-option2</page:param>
                <page:param name="description"><ww:text name="'admin.mailservers.smtp.tls.required.description'"/></page:param>
                <aui:checkbox label="text('admin.mailservers.smtp.tls.required')" name="'tlsRequired'" fieldValue="true" theme="'aui'" />
            </page:applyDecorator>
            </div>

            <page:applyDecorator name="auifieldgroup">
                <page:param name="description"><ww:text name="'setup3.username.desc'"/></page:param>
                <aui:textfield label="text('common.words.username')" name="'username'" theme="'aui'" />
            </page:applyDecorator>

            <page:applyDecorator name="auifieldgroup">
                <page:param name="description"><ww:text name="'setup3.password.desc'"/></page:param>
                <aui:password label="text('common.words.password')" name="'password'" theme="'aui'" />
            </page:applyDecorator>

        </div>

        <div id="email-notifications-jndi-fields" class="setup-fields hidden">

            <page:applyDecorator name="auifieldgroup">
                <page:param name="description"><ww:text name="'setup3.jndilocation.desc'"/></page:param>
                <aui:textfield label="text('setup3.jndilocation')" name="'jndiLocation'" theme="'aui'" />
            </page:applyDecorator>

            <aui:component name="'type'" value="types[1]" template="hidden.jsp" theme="'aui'" />

        </div>

    </div>

    <aui:component name="'testingMailConnection'" value="'false'" template="hidden.jsp" theme="'aui'" />

    <page:applyDecorator name="auifieldgroup">
        <page:param name="type">buttons-container</page:param>
        <page:applyDecorator name="auifieldgroup">
            <page:param name="type">buttons</page:param>
            <aui:component name="finishSetup" theme="'aui'" template="formSubmit.jsp">
                <aui:param name="'id'">jira-setupwizard-submit</aui:param>
                <aui:param name="'submitButtonName'">finish</aui:param>
                <aui:param name="'submitButtonText'"><ww:text name="'common.forms.finish'"/></aui:param>
                <aui:param name="'submitButtonCssClass'">aui-button-primary</aui:param>
            </aui:component>
            <aui:component name="'testConnection'" template="formButton.jsp" theme="'aui'">
                <aui:param name="'id'">test-mailserver-connection</aui:param>
                <aui:param name="'cssClass'">hidden</aui:param>
                <aui:param name="'text'"><ww:text name="'setupdb.testconnection'" /></aui:param>
            </aui:component>
            <div class="hidden throbber-message">
                <span id="test-connection-throbber" class="hidden">
                    <span class="aui-icon aui-icon-wait"></span>
                    <ww:text name="'admin.mailserver.verify.testing'" />
                </span>
                <span id="submit-throbber" class="hidden">
                    <span class="aui-icon aui-icon-wait"></span>
                    <ww:if test="/instantSetup == true">
                        <ww:text name="'setup3.spinner.message.instant'" />
                    </ww:if>
                    <ww:else>
                        <ww:text name="'setup3.spinner.message'" />
                    </ww:else>
                </span>
            </div>

        </page:applyDecorator>
    </page:applyDecorator>

</page:applyDecorator>

</body>
</html>
<% } %>
