
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.mailservers.update.pop.imap.mail.server'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section/mail_section"/>
    <meta name="admin.active.tab" content="<ww:property value='./activeTab'/>"/>
</head>

<body>
<page:applyDecorator name="jiraform">
    <page:param name="action">UpdatePopMailServer.jspa</page:param>
    <page:param name="submitId">update_submit</page:param>
    <page:param name="submitName"><ww:text name="'common.forms.update'"/></page:param>
    <page:param name="cancelURI"><ww:property value='./cancelURI'/></page:param>
    <page:param name="leftButtons">
        <input class="aui-button" type="button" id="test_connection" value="Test Connection" onclick="JIRA.app.admin.email.verifyServerConnection(event,'VerifyPopServerConnection!update.jspa')" />
    </page:param>
    <page:param name="title"><ww:text name="'admin.mailservers.update.pop.imap.mail.server'"/></page:param>
    <page:param name="description"><ww:text name="'admin.mailservers.update.pop.imap.mail.server.description'"/></page:param>
    <page:param name="width">100%</page:param>
    <%@include file="verifymailserver.jsp" %>
    <ww:if test="/actionName == 'VerifyPopServerConnection'">
       <page:param name="enableFormErrors"><ww:property value="/hasErrors"/></page:param>
    </ww:if>

    <page:param name="instructions">
        <ww:if test="/validMailParameters == false">
            <%@include file="/includes/admin/email/badmailprops.jsp"%>
        </ww:if>
    </page:param>

    <ui:textfield label="text('common.words.name')" name="'name'" size="'30'">
        <ui:param name="'description'"><ww:text name="'admin.mailservers.pop.name.description'"/></ui:param>
        <ui:param name="'mandatory'">true</ui:param>
    </ui:textfield>

    <ui:textfield label="text('common.words.description')" name="'description'" size="'60'" />

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
    <input type="hidden" id="originalUsername" value="<ww:property value="/username"/>"/>

    <ui:checkbox label="text('common.concepts.changepassword')" name="'changePassword'" fieldValue="true" id="'changePassword'" />

    <ui:component label="text('common.words.password')" name="'password'" value="/passwordSet" template="passwordwithvalue.jsp">
        <ui:param name="'description'"><ww:text name="'admin.mailservers.pop.password.description'"/></ui:param>
        <ui:param name="'mandatory'">true</ui:param>
    </ui:component>

    <ui:component name="'id'" template="hidden.jsp" theme="'single'"  />
    <ui:component name="'type'" template="hidden.jsp" theme="'single'" />
</page:applyDecorator>
</body>
</html>
