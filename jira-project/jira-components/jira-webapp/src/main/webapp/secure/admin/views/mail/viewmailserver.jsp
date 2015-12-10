<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.mailservers.mail.servers'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section/mail_section"/>
    <meta name="admin.active.tab" content="<ww:property value='./activeTab'/>"/>
</head>

<body>

<ww:if test="canManageSmtpMailServers == true">
    <div id="smtp-mail-servers-panel">
        <page:applyDecorator name="jirapanel">
            <page:param name="title"><ww:text name="'admin.mailservers.smtp.mail.servers'"/></page:param>
            <page:param name="width">100%</page:param>
            <p><ww:text name="'admin.mailservers.the.table.below.smtp'"/></p>
        </page:applyDecorator>

    <ww:if test="smtpMailServers/size == 0">
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">info</aui:param>
            <aui:param name="'messageHtml'"><ww:text name="'admin.mailservers.no.smtp.servers.configured'"/></aui:param>
        </aui:component>
        <div class="buttons-container aui-toolbar form-buttons noprint">
            <div class="toolbar-group">
                <span class="toolbar-item">
                    <a class="toolbar-trigger" id="add-new-smtp-server" href="AddSmtpMailServer!default.jspa"><ww:text name="'admin.mailservers.configure.new.smtp.mail.server'"/></a>
                </span>
            </div>
        </div>
    </ww:if>
    <ww:else>
        <table class="aui aui-table-rowhover">
            <thead>
                <tr>
                    <th width="30%">
                        <ww:text name="'common.words.name'"/>
                    </th>
                    <th>
                        <ww:text name="'admin.common.words.details'"/>
                    </th>
                    <th width="10%">
                        <ww:text name="'common.words.operations'"/>
                    </th>
                </tr>
            </thead>
            <tbody>
            <ww:iterator value="smtpMailServers" status="'status'">
                <tr>
                    <td>
                        <strong><ww:property value="name"/></strong>
                        <ww:if test="description">
                            <div class="description"><ww:property value="description"/></div>
                        </ww:if>
                    </td>
                    <td>
                        <ul class="item-details">
                            <li>
                                <dl>
                                    <dt><ww:text name="'admin.mailservers.from'"/>:</dt>
                                    <dd><ww:property value="defaultFrom" /></dd>
                                </dl>
                                <dl>
                                    <dt><ww:text name="'admin.mailservers.prefix'"/>:</dt>
                                    <dd><ww:property value="prefix" /></dd>
                                </dl>
                            </li>
                        <ww:if test="sessionServer == true">
                            <li>
                                <dl>
                                    <dt><ww:text name="'admin.mailservers.jndi.location'"/>:</dt>
                                    <dd><ww:property value="jndiLocation" /></dd>
                                </dl>
                            </li>
                        </ww:if>
                        <ww:else>
                            <li>
                                <dl>
                                    <dt><ww:text name="'admin.mailservers.host'"/>:</dt>
                                    <dd><ww:property value="hostname" /></dd>
                                </dl>
                            <ww:if test="port">
                                <dl>
                                    <dt><ww:text name="'admin.mailservers.smtp.port'"/>:</dt>
                                    <dd><ww:property value="port" /></dd>
                                </dl>
                            </ww:if>
                            <ww:if test="username">
                                <dl>
                                    <dt><ww:text name="'common.words.username'"/>:</dt>
                                    <dd><ww:property value="username" /></dd>
                                </dl>
                            </ww:if>
                            </li>
                        </ww:else>
                        </ul>
                    </td>
                    <td>
                        <ul class="operations-list">
                            <li><a id="<ww:property value="'edit_' + id"/>" href="UpdateSmtpMailServer!default.jspa?id=<ww:property value="id"/>"><ww:text name="'common.words.edit'"/></a></li>
                            <li><a id="deleteSMTP" href="DeleteMailServer!default.jspa?id=<ww:property value="id"/>"><ww:text name="'common.words.delete'"/></a></li>
                            <li><a href="SendTestMail!default.jspa"><ww:text name="'admin.mailservers.send.a.test.email'"/></a></li>
                        </ul>
                    </td>
                </tr>
            </ww:iterator>
            </tbody>
        </table>
    </ww:else>
    </div>
</ww:if>

<ww:if test="canManagePopMailServers == true">
    <div id="pop-mail-servers-panel">
    <page:applyDecorator name="jirapanel">
        <page:param name="title"><ww:text name="'admin.mailservers.pop.imap.servers'"/></page:param>
        <page:param name="width">100%</page:param>
        <p><ww:text name="'admin.mailservers.the.table.below.pop'"/></p>
        <ww:if test="/validMailParameters == false && popMailServers/empty == false">
            <%@include file="/includes/admin/email/badmailprops.jsp"%>
        </ww:if>
    </page:applyDecorator>

    <ww:if test="popMailServers/size == 0">
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">info</aui:param>
            <aui:param name="'messageHtml'"><ww:text name="'admin.mailservers.no.pop.imap.servers.configured'"/></aui:param>
        </aui:component>
    </ww:if>
    <ww:else>
        <table id="pop-mail-servers-table" class="aui aui-table-rowhover">
            <thead>
                <tr>
                    <th width="30%">
                        <ww:text name="'common.words.name'"/>
                    </th>
                    <th>
                        <ww:text name="'admin.common.words.details'"/>
                    </th>
                    <th width="10%">
                        <ww:text name="'common.words.operations'"/>
                    </th>
                </tr>
            </thead>
            <tbody>
            <ww:iterator value="popMailServers" status="'status'">
                <tr>
                    <td>
                        <span class="mail-server-name">
                            <strong><ww:property value="name"/></strong>
                        </span>
                        <ww:if test="description">
                            <div class="description"><ww:property value="description"/></div>
                        </ww:if>
                    </td>
                    <td>
                        <ul class="item-details">
                            <li>
                                <dl>
                                    <dt><ww:text name="'admin.mailservers.host'"/>:</dt>
                                    <dd><span class="mail-server-host"><ww:property value="hostname" /></span></dd>
                                </dl>
                                <dl>
                                    <dt><ww:text name="'common.words.username'"/>:</dt>
                                    <dd><span class="mail-server-username"><ww:property value="username" /></span></dd>
                                </dl>
                            </li>
                        </ul>
                    </td>
                    <td>
                        <ul class="operations-list">
                            <li><a id="edit-pop-<ww:property value="id"/>" href="UpdatePopMailServer!default.jspa?id=<ww:property value="id"/>"><ww:text name="'common.words.edit'"/></a></li>
                            <li><a id="delete-pop-<ww:property value="id"/>" href="DeleteMailServer!default.jspa?id=<ww:property value="id"/>"><ww:text name="'common.words.delete'"/></a></li>
                        </ul>
                    </td>
                </tr>
            </ww:iterator>
            </tbody>
        </table>
    </ww:else>
        <div class="buttons-container aui-toolbar form-buttons noprint">
            <div class="toolbar-group">
                <span class="toolbar-item">
                    <a class="toolbar-trigger" id="add-pop-mail-server" href="AddPopMailServer!default.jspa"><ww:text name="'admin.mailservers.configure.new.pop.imap.mail.server'"/></a>
                </span>
            </div>
        </div>
    </div>
</ww:if>
</body>
</html>
