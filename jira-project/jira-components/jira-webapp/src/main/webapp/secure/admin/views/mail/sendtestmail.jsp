<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head><title><ww:text name="'admin.mailservers.send.test.mail'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section/mail_section"/>
    <meta name="admin.active.tab" content="<ww:property value='./activeTab'/>"/>
</head>
<body>

<page:applyDecorator id="first" name="jiraform">
    <page:param name="action">SendTestMail.jspa</page:param>
    <page:param name="cancelURI"><ww:property value='./cancelURI'/></page:param>
    <page:param name="submitId">send_submit</page:param>
    <page:param name="submitName"><ww:text name="'admin.email.send'"/></page:param>
    <page:param name="autoSelectFirst">false</page:param>
    <page:param name="title"><ww:text name="'admin.email.send.email'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="description"><ww:text name="'admin.mailserver.send.test.email.here'"/></page:param>

    <ui:textfield label="text('admin.email.to')" name="'to'" size="60"></ui:textfield>

    <ui:textfield label="text('admin.email.subject')" name="'subject'" size="60"></ui:textfield>

    <ui:select label="text('admin.email.message.type')" name="'messageType'" list="mimeTypes" listKey="'key'" listValue="'value'"></ui:select>

    <ui:textarea label="text('admin.email.body')" name="'message'" cols="70" rows="8" ></ui:textarea>

    <ui:checkbox label="text('admin.mailserver.smtp.logging')" name="'debug'" fieldValue="'true'">
    <ui:param name="'description'"><ww:text name="'admin.mailserver.smtp.logging.description'"/></ui:param>
</ui:checkbox>
</page:applyDecorator>

<page:applyDecorator name="jiraform">
    <page:param name="width">100%</page:param>
    <page:param name="title"><ww:text name="'admin.mailserver.mail.log'"/></page:param>
    <page:param name="description"> </page:param>
    <page:param name="jiraformId">mail-log-table</page:param>

    <ui:textarea label="text('admin.mailserver.log')" name="'log'" cols="80" rows="15" readonly="true" >
        <ui:param name="'description'"><ww:text name="'admin.mailserver.mail.log.description'"/></ui:param>
    </ui:textarea>
</page:applyDecorator>

</body>
</html>
