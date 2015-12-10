<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.mailservers.add.smtp.mail.server'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section/mail_section"/>
    <meta name="admin.active.tab" content="<ww:property value='./activeTab'/>"/>
</head>
<body>
    <page:applyDecorator name="jiraform">
        <page:param name="action">AddSmtpMailServer.jspa</page:param>
        <page:param name="submitId">add_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.forms.add'"/></page:param>
    	<page:param name="cancelURI"><ww:property value='./cancelURI'/></page:param>
        <page:param name="leftButtons">
            <input type="button" class="aui-button" id="test_connection" value="Test Connection" onclick="JIRA.app.admin.email.verifyServerConnection(event,'VerifySmtpServerConnection!add.jspa')" />&nbsp;
        </page:param>
        <page:param name="title"><ww:text name="'admin.mailservers.add.smtp.mail.server'"/></page:param>
        <page:param name="description"><ww:text name="'admin.mailservers.add.smtp.instruction'"/></page:param>
        <page:param name="helpURL">smtpconfig</page:param>
        <page:param name="width">100%</page:param>
        <%@include file="verifymailserver.jsp" %>
        <ww:if test="/actionName == 'VerifySmtpServerConnection'">
           <page:param name="enableFormErrors"><ww:property value="/hasErrors"/></page:param>
        </ww:if>

        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">warning</aui:param>
            <aui:param name="'messageHtml'">
                <ww:text name="'admin.mailservers.automatically.sent'">
                    <ww:param name="'value0'"><ww:if test="projects/empty == false"><a href="#projects-with-notification-schemes" class="projects-list-trigger"></ww:if><ww:else><span></ww:else></ww:param>
                    <ww:param name="'value1'"><ww:if test="projects/empty == false"></a></ww:if><ww:else></span></ww:else></ww:param>
                </ww:text>
            </aui:param>
        </aui:component>

        <div id="projects-with-notification-schemes" class="shared-item-target">
            <div class="shared-item-content">
                <ul class="shared-project-list">
                    <ww:iterator value="projects">
                        <li>
                            <a class="shared-project-name" href="<%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="key"/>">
                                <img class="shared-project-icon" width="16" height="16" alt="" src="<%= request.getContextPath() %>/secure/projectavatar?size=small&amp;pid=<ww:property value="id"/>" /> <ww:property value="name"/>
                            </a>
                        </li>
                    </ww:iterator>
                </ul>
            </div>
        </div>

        <ui:textfield label="text('common.words.name')" name="'name'" size="'30'">
            <ui:param name="'description'"><ww:text name="'admin.mailservers.name.description'"/></ui:param>
            <ui:param name="'mandatory'">true</ui:param>
        </ui:textfield>
        <ui:textfield label="text('common.words.description')" name="'description'" size="'60'" />

        <ui:textfield label="text('admin.mailservers.from.address')" name="'from'">
            <ui:param name="'description'"><ww:text name="'admin.mailservers.from.address.description'"/></ui:param>
            <ui:param name="'mandatory'">true</ui:param>
        </ui:textfield>
        <ui:textfield label="text('admin.mailservers.email.prefix')" name="'prefix'">
            <ui:param name="'description'"><ww:text name="'admin.mailservers.email.prefix.description'"/></ui:param>
            <ui:param name="'mandatory'">true</ui:param>
        </ui:textfield>

        <tr>
            <td colspan="2">
                <b><ww:text name="'admin.mailservers.server.details'"/></b>
                <div class="description">
                    <ww:text name="'admin.mailservers.server.details.description'">
                        <ww:param name="'value0'"><i></ww:param>
                        <ww:param name="'value1'"></i></ww:param>
                        <ww:param name="'value2'"><i></ww:param>
                        <ww:param name="'value3'"></i></ww:param>
                    </ww:text>
                </div>
            </td>
        </tr>

        <tr>
            <td colspan="2"><b><ww:text name="'admin.mailservers.smtp.host'"/></b></td>
        </tr>

        <ui:select label="text('admin.mailservers.service.provider')" name="'serviceProvider'" list="/supportedServiceProviders" listKey="'key'" listValue="'text(value)'">
        </ui:select>

        <ui:select label="text('admin.mailservers.protocol')" name="'protocol'" list="/supportedClientProtocols(types[1])" listKey="'protocol'" listValue="'.'">
            <ui:param name="description"><ww:text name="'admin.mailservers.protocol.description'"/></ui:param>
        </ui:select>

        <ui:textfield label="text('admin.mailservers.host.name')" name="'serverName'">
            <ui:param name="'description'"><ww:text name="'admin.mailservers.smtp.host.name.description'"/></ui:param>
            <ui:param name="'mandatory'">true</ui:param>
        </ui:textfield>

        <ui:textfield label="text('admin.mailservers.smtp.port')" name="'port'">
            <ui:param name="'description'"><ww:text name="'admin.mailservers.smtp.port.description'"/></ui:param>
        </ui:textfield>

        <ui:textfield label="text('admin.mailservers.host.timeout')" name="'timeout'">
            <ui:param name="'description'"><ww:text name="'admin.mailservers.host.timeout.description'"/></ui:param>
            <ui:param name="'mandatory'">false</ui:param>
        </ui:textfield>

        <ui:checkbox label="text('admin.mailservers.smtp.tls.required')" name="'tlsRequired'" fieldValue="true">
              <ui:param name="'description'"><ww:text name="'admin.mailservers.smtp.tls.required.description'"/></ui:param>
        </ui:checkbox>

        <ui:textfield label="text('common.words.username')" name="'username'">
            <ui:param name="'description'"><ww:text name="'admin.mailservers.username.description'"/></ui:param>
        </ui:textfield>

        <input type="hidden" name="changePassword" value="true"/>

        <ui:component label="text('common.words.password')" name="'password'" template="passwordwithvalue.jsp">
            <ui:param name="'description'"><ww:text name="'admin.mailservers.password.description'"/></ui:param>
        </ui:component>

        <tr>
            <td><b><ww:text name="'common.words.or'"/></b></td>
            <td>&nbsp;</td>
        </tr>

        <tr>
            <td colspan="2"><b><ww:text name="'admin.mailservers.jndi.location'"/></b></td>
        </tr>

        <ui:textfield label="text('admin.mailservers.jndi.location')" name="'jndiLocation'">
            <ui:param name="'description'"><ww:text name="'admin.mailservers.jndi.location.description'"/></ui:param>
        </ui:textfield>

        <ui:component name="'type'" value="types[1]" template="hidden.jsp" theme="'single'"  />

    </page:applyDecorator>

</body>
</html>
