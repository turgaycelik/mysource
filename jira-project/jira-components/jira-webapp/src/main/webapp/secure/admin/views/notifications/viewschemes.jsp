
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.schemes.notifications.notification.schemes'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/misc_schemes_section"/>
    <meta name="admin.active.tab" content="notification_schemes"/>
</head>

<body>
<page:applyDecorator name="jirapanel">
    <page:param name="title"><ww:text name="'admin.schemes.notifications.notification.schemes'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="helpURL">notification_schemes</page:param>
        <p><ww:text name="'admin.schemes.notifications.table.below.shows'"/></p>

        <ww:if test="/schemeManager/hasMailServer == false">
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">warning</aui:param>
                <aui:param name="'messageHtml'">
                    <p id="no-mail-server-setup-warning"><ww:text name="'admin.common.words.warning'"/>:
                    <ww:if test="/systemAdministrator == true">
                        <ww:text name="'admin.mailqueue.no.default.mail.server'">
                            <ww:param name="'value0'"><a href="<%=request.getContextPath()%>/secure/admin/OutgoingMailServers.jspa"></ww:param>
                            <ww:param name="'value1'"></a></ww:param>
                        </ww:text>
                    </ww:if>
                    <ww:else>
                        <ww:text name="'admin.mailqueue.no.default.mail.server.admin'">
                            <ww:param name="'value0'"> </ww:param>
                            <ww:param name="'value1'"> </ww:param>
                        </ww:text>
                    </ww:else>
                    </p>
                </aui:param>
            </aui:component>
        </ww:if>
</page:applyDecorator>

<ww:if test="schemes/size == 0">
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">info</aui:param>
        <aui:param name="'messageHtml'">
            <ww:text name="'admin.schemes.notifications.no.notification.schemes.configured'"/>
        </aui:param>
    </aui:component>
</ww:if>
<ww:else>
<table id="notification_schemes" class="aui aui-table-rowhover">
    <thead>
        <tr>
            <th>
                <ww:text name="'common.words.name'"/>
            </th>
            <th>
                <ww:text name="'common.concepts.projects'"/>
            </th>
            <th width="10%">
                <ww:text name="'common.words.operations'"/>
            </th>
        </tr>
    </thead>
    <tbody>
    <ww:iterator value="schemes" status="'status'">
        <tr>
            <td>
                <b><a href="<ww:url page="EditNotifications!default.jspa"><ww:param name="'schemeId'" value="long('id')"/></ww:url>"><ww:property value="string('name')"/></a></b>
                <div class="description"><ww:property value="string('description')"/></div>
            </td>
            <td>
                <ww:if test="/projects(.)/empty == false">
                    <ul>
                    <ww:iterator value="projects(.)">
                        <li><a href="<%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="./string('key')"/>/summary"><ww:property value="string('name')" /></a></li>
                    </ww:iterator>
                    </ul>
                </ww:if>
                <ww:else>
                    &nbsp;
                </ww:else>
            </td>
            <td>
                <ul class="operations-list">
                    <li><a id="<ww:property value="long('id')"/>_edit" href="<ww:url page="EditNotifications!default.jspa"><ww:param name="'schemeId'" value="long('id')"/></ww:url>"><ww:text name="'admin.schemes.notifications.notifications'"/></a></li>
                    <li><a id="<ww:property value="long('id')"/>_copy" href="<ww:url page="CopyNotificationScheme.jspa"><ww:param name="'schemeId'" value="long('id')"/></ww:url>" title="<ww:text name="'admin.schemes.notifications.create.copy.of.this.scheme'"/>"><ww:text name="'common.words.copy'"/></a></li>
                    <li><a id="<ww:property value="long('id')"/>_rename" href="<ww:url page="EditNotificationScheme!default.jspa"><ww:param name="'schemeId'" value="long('id')"/></ww:url>"><ww:text name="'common.words.edit'"/></a></li>
                    <li><a id="<ww:property value="long('id')"/>_del" href="<ww:url page="DeleteNotificationScheme!default.jspa"><ww:param name="'schemeId'" value="long('id')"/></ww:url>"><ww:text name="'common.words.delete'"/></a></li>
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
            <a class="toolbar-trigger" id="add-notification-scheme" href="<ww:url page="AddNotificationScheme!default.jspa"/>"><ww:text name="'admin.schemes.notifications.add.notification.scheme'"/></a>
        </span>
    </div>
</div>

</body>
</html>
