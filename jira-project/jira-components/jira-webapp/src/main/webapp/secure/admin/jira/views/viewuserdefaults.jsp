<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.userdefaults.user.defaults'"/></title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_configuration"/>
    <meta name="admin.active.tab" content="user_defaults"/>
</head>
<body>
    <ui:soy moduleKey="'jira.webresources:soy-templates'" template="'JIRA.Templates.Headers.pageHeader'">
        <ui:param name="'mainContent'">
            <h2><ww:text name="'admin.userdefaults.user.default.settings'"/></h2>
        </ui:param>
        <ui:param name="'actionsContent'">
            <div class="aui-buttons">
                <a class="aui-button trigger-dialog" id="user-defaults-edit" href="EditUserDefaultSettings!default.jspa"><ww:text name="'admin.userdefaults.edit.default.values'"/></a>
            </div>
        </ui:param>
    </ui:soy>
    <p><ww:text name="'admin.userdefaults.set.default.values'"/></p>
    <table id="view_user_defaults" class="aui aui-table-rowhover">
        <thead>
            <tr>
                <th class="cell-type-key">
                    <ww:text name="'common.words.name'"/>
                </th>
                <th>
                    <ww:text name="'admin.common.words.value'"/>
                </th>
                <th class="cell-type-collapsed">
                    <ww:text name="'common.words.operations'"/>
                </th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td>
                    <ww:text name="'admin.userdefaults.outgoing.email.format'"/>
                </td>
                <td>
                    <ww:property value="applicationProperties/defaultBackedString('user.notifications.mimetype')"/>
                </td>
                <td>
                    <ul class="operations-list">
                        <li><a href="SetGlobalEmailPreference!default.jspa" title="<ww:text name="'admin.userdefaults.force.user.defaults'"/>"><ww:text name="'admin.common.words.apply'"/></a></li>
                    </ul>
                </td>
            </tr>
            <tr>
                <td>
                    <ww:text name="'admin.userdefaults.number.of.issues'"/>
                </td>
                <td>
                    <span data-defaults-issue-count="<ww:property value="applicationProperties/defaultBackedString('user.issues.per.page')"/>"><ww:property value="applicationProperties/defaultBackedString('user.issues.per.page')"/></span>
                </td>
                <td></td>
            </tr>
            <tr>
                <td>
                    <ww:text name="'admin.userdefaults.default.share'"/>
                </td>
                <td>
                    <ww:if test="applicationProperties/option('user.default.share.private') == false">
                        <ww:text name="'admin.common.words.public'"/>
                    </ww:if>
                    <ww:else>
                        <ww:text name="'admin.common.words.private'"/>
                    </ww:else>
                </td>
                <td></td>
            </tr>
            <tr>
                <td>
                    <ww:text name="'admin.userdefaults.notify.users.of.own.changes'"/>
                </td>
                <td>
                    <ww:if test="applicationProperties/option('user.notify.own.changes') == true">
                        <ww:text name="'common.words.yes'"/>
                    </ww:if>
                    <ww:else>
                        <ww:text name="'common.words.no'"/>
                    </ww:else>
                </td>
                <td></td>
            </tr>
            <tr>
                <td>
                    <ww:text name="'admin.userdefaults.labels.autowatch'"/>
                </td>
                <td>
                    <ww:if test="applicationProperties/option('user.autowatch.disabled') == true">
                        <ww:text name="'common.words.no'"/>
                    </ww:if>
                    <ww:else>
                        <ww:text name="'common.words.yes'"/>
                    </ww:else>
                </td>
                <td></td>
            </tr>
        </tbody>
    </table>
</body>
</html>
