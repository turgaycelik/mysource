<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.schemes.notifications.edit.notifications'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/misc_schemes_section"/>
    <meta name="admin.active.tab" content="notification_schemes"/>
</head>
<body>
<page:applyDecorator name="jirapanel">
    <page:param name="title"><ww:text name="'admin.schemes.notifications.edit.notifications'"/> &mdash; <ww:property value="scheme/string('name')"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="helpURL">notification_schemes</page:param>
    <page:param name="postTitle">
        <ui:component theme="'raw'" template="projectshare.jsp" name="'name'" value="'value'" label="'label'">
            <ui:param name="'projects'" value="/usedIn"/>
        </ui:component>
    </page:param>
    <p>
    <ww:text name="'admin.schemes.notifications.edit.notifications.description'">
        <ww:param name="'value0'"><ww:property value="scheme/string('name')"/></ww:param>
    </ww:text>
    </p>

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

    <ul class="optionslist">
        <li><ww:text name="'admin.schemes.notifications.add.notification'">
            <ww:param name="'value0'"><a href="<ww:url page="AddNotification!default.jspa"><ww:param name="'schemeId'" value="scheme/string('id')"/></ww:url>"><b></ww:param>
            <ww:param name="'value1'"></b></a></ww:param>
        </ww:text></li>
        <li><ww:text name="'admin.schemes.notifications.view.all.notification.schemes'">
            <ww:param name="'value0'"><a href="ViewNotificationSchemes.jspa"><b></ww:param>
            <ww:param name="'value1'"></b></a></ww:param>
        </ww:text></li>
    </ul>

</page:applyDecorator>

<table class="aui aui-table-rowhover" id="notificationSchemeTable" data-schemeid='<ww:property value="scheme/string('id')"/>'>
    <thead>
        <tr>
            <th>
                <ww:text name="'admin.schemes.notifications.event'"/>
            </th>
            <th>
                <ww:text name="'admin.schemes.notifications.notifications'"/>
            </th>
            <th width="10%">
                <ww:text name="'common.words.operations'"/>
            </th>
        </tr>
    </thead>
    <tbody>
    <ww:iterator value="events/keySet" status="'status'">
        <tr>
            <!-- Event Type Name -->
            <ww:property value="events/(.)">
            <td>
                <b><ww:property value="./translatedName(/remoteUser)"/></b>
                <ww:if test="./systemEventType == true">
                    <span class="secondary-text">(<ww:text name="'admin.schemes.notifications.system.event.type'" />)</span>
                </ww:if>
            </td>
            </ww:property>
            <!-- Notifications -->
            <td>
                <ww:if test="/notifications(.)/empty == false">
                    <ul>
                    <ww:iterator value="notifications(.)">
                        <li>
                            <span title="<ww:text name="'admin.schemes.notifications.notification'" />">
                                <ww:property value="../../type(string('type'))/displayName" />
                                <ww:if test="string('parameter')">(<ww:property value="../../type(string('type'))/argumentDisplay(string('parameter'))" />)</ww:if>
                            </span>
                            (<a id="del_<ww:property value="long('id')" />" title="<ww:text name="'admin.schemes.notifications.delete.notification'" />" href="<ww:url page="DeleteNotification!default.jspa"><ww:param name="'id'" value="long('id')"/><ww:param name="'schemeId'" value="schemeId"/></ww:url>"><ww:text name="'common.words.delete'"/></a>)
                        </li>
                    </ww:iterator>
                    </ul>
                </ww:if>
                <ww:else>
                    &nbsp;
                </ww:else>
            </td>
            <!-- Operations -->
            <td>
                <ul class="operations-list">
                    <li><a id="add_<ww:property value="." />" href="<ww:url page="AddNotification!default.jspa"><ww:param name="'schemeId'" value="../schemeId"/><ww:param name="'eventTypeIds'" value="."/></ww:url>"><ww:text name="'common.forms.add'"/></a></li>
                </ul>
            </td>
        </tr>
    </ww:iterator>
    </tbody>
</table>

<fieldset class="hidden parameters">
    <input type="hidden" id="notification-scheme-id" value="<ww:property value="/schemeId" />">
</fieldset>
<ui:component theme="'raw'" template="projectsharedialog.jsp" name="'name'" value="'value'" label="'label'">
    <ui:param name="'projects'" value="/usedIn"/>
    <ui:param name="'title'"><ww:text name="'admin.project.shared.list.heading.fields'"/></ui:param>
</ui:component>
</body>
</html>
