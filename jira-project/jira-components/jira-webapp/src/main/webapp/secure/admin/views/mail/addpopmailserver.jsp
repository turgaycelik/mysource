<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title><ww:text name="'admin.mailservers.add.pop.imap.mail.server'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section/mail_section"/>
    <meta name="admin.active.tab" content="<ww:property value='./activeTab'/>"/>
</head>
<body>
    <page:applyDecorator name="jiraform">
        <page:param name="action">AddPopMailServer.jspa</page:param>
        <page:param name="submitId">add_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.forms.add'"/></page:param>
    	<page:param name="cancelURI"><ww:property value='./cancelURI'/></page:param>
        <page:param name="leftButtons">
            <input class="aui-button" type="button" id="test_connection" value="Test Connection" onclick="JIRA.app.admin.email.verifyServerConnection(event,'VerifyPopServerConnection!add.jspa');" />
        </page:param>
        <page:param name="title"><ww:text name="'admin.mailservers.add.pop.imap.mail.server'"/></page:param>
        <page:param name="description">
            <p><ww:text name="'admin.mailservers.add.pop.server.description'"/></p>
        </page:param>
        <page:param name="width">100%</page:param>
        <page:param name="instructions">
            <ww:if test="/validMailParameters == false">
                <%@include file="/includes/admin/email/badmailprops.jsp"%>
            </ww:if>
        </page:param>
        <%@include file="verifymailserver.jsp" %>
        <ww:if test="/actionName == 'VerifyPopServerConnection'">
            <page:param name="enableFormErrors"><ww:property value="/hasErrors"/></page:param>
        </ww:if>

        <ui:textfield label="text('common.words.name')" name="'name'" size="'30'">
            <ui:param name="'description'"><ww:text name="'admin.mailservers.pop.name.description'"/></ui:param>
            <ui:param name="'mandatory'">true</ui:param>
        </ui:textfield>

        <ui:textfield label="text('common.words.description')" name="'description'" size="'60'" />

        <ui:select label="text('admin.mailservers.service.provider')" name="'serviceProvider'" list="/supportedServiceProviders" listKey="'key'" listValue="'text(value)'">
        </ui:select>

        <ui:select label="text('admin.mailservers.protocol')" name="'protocol'" list="/supportedClientProtocols(types[0])" listKey="'protocol'" listValue="'.'">
            <ui:param name="description"><ww:text name="'admin.mailservers.protocol.description'"/></ui:param>
        </ui:select>

        <ui:textfield label="text('admin.mailservers.host.name')" name="'serverName'">
            <ui:param name="'description'"><ww:text name="'admin.mailservers.pop.host.name.description'"/></ui:param>
            <ui:param name="'mandatory'">true</ui:param>
        </ui:textfield>

        <ui:textfield label="text('admin.mailservers.pop.port')"  name="'port'" size="'5'">
            <ui:param name="'description'"><ww:text name="'admin.mailservers.pop.port.description'"/></ui:param>
        </ui:textfield>

        <ui:textfield label="text('admin.mailservers.host.timeout')" name="'timeout'">
            <ui:param name="'description'"><ww:text name="'admin.mailservers.host.timeout.description'"/></ui:param>
            <ui:param name="'mandatory'">false</ui:param>
        </ui:textfield>

        <ui:textfield label="text('common.words.username')" name="'username'">
            <ui:param name="'description'"><ww:text name="'admin.mailservers.pop.username.description'"/></ui:param>
            <ui:param name="'mandatory'">true</ui:param>
        </ui:textfield>

        <input type="hidden" name="changePassword" value="true"/>

        <ui:component label="text('common.words.password')" name="'password'" template="passwordwithvalue.jsp">
            <ui:param name="'description'"><ww:text name="'admin.mailservers.pop.password.description'"/></ui:param>
            <ui:param name="'mandatory'">true</ui:param>
        </ui:component>

        <ui:component name="'type'" value="types[0]" template="hidden.jsp" theme="'single'"  />
    </page:applyDecorator>
</body>
</html>