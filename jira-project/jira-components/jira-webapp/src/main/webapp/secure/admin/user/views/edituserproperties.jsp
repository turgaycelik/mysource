<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title><ww:text name="'admin.edituserproperties.edit.properties'"/></title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_section"/>
    <meta name="admin.active.tab" content="user_browser"/>
</head>
<body>
<%--TODO: Restful table candidate - add/edit/delete user properties --%>
    <ui:soy moduleKey="'jira.webresources:soy-templates'" template="'JIRA.Templates.Headers.pageHeader'">
        <ui:param name="'mainContent'">
            <ul class="aui-nav aui-nav-breadcrumbs">
                <li><a href="<ww:url page="/secure/admin/user/UserBrowser.jspa" atltoken="false"/>"><ww:text name="'admin.menu.usersandgroups.user.browser'" /></a></li>
                <li><a id="view_user" href="<ww:url page="ViewUser.jspa"><ww:param name="'name'" value="name"/></ww:url>"><ww:property value="/user/displayName"/></a></li>
            </ul>
            <h2><ww:text name="'admin.edituserproperties.edit.properties'"/></h2>
        </ui:param>
    </ui:soy>

    <ww:if test="userProperties != null && userProperties/size > 0">
        <p>
            <ww:text name="'admin.edituserproperties.page.description'">
                <ww:param name="'value0'"><b><ww:property value="/user/displayName"/></b></ww:param>
            </ww:text>
        </p>
        <p><ww:text name="'admin.edituserproperties.available.properties.description'"/></p>
        <table class="aui">
            <thead>
                <tr>
                    <th><ww:text name="'common.words.key'"/></th>
                    <th><ww:text name="'common.words.value'"/></th>
                    <th><ww:text name="'common.words.operations'"/></th>
                </tr>
            </thead>
            <tbody>
                <ww:iterator value="userProperties">
                    <tr>
                        <td><ww:property value="key" /></td>
                        <td><ww:property value="value" /></td>
                        <td>
                            <ul class="operations-list">
                                <li><a class="trigger-dialog" id="edit_<ww:property value="key"/>" href="<ww:url page="EditUserProperty.jspa"><ww:param name="'name'" value="user/name" /><ww:param name="'key'" value="key" /></ww:url>"><ww:text name="'admin.common.words.edit'"/></a></li>
                                <li><a class="trigger-dialog" href="<ww:url page="DeleteUserProperty!default.jspa"><ww:param name="'name'" value="user/name" /><ww:param name="'key'" value="key"/></ww:url>" id="delete_<ww:property value="key"/>"><ww:text name="'admin.common.words.delete'"/></a></li>
                            </ul>
                        </td>
                    </tr>
                </ww:iterator>
            </tbody>
        </table>
    </ww:if>
    <ww:else>
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">info</aui:param>
            <aui:param name="'messageHtml'">
                <p>
                    <ww:text name="'admin.edituserproperties.user.has.no.properties'">
                        <ww:param name="'value0'"><b><ww:property value="/user/displayName"/></b></ww:param>
                    </ww:text>
                </p>
            </aui:param>
        </aui:component>
    </ww:else>

    <page:applyDecorator id="user-properties-add" name="auiform">
        <page:param name="action">EditUserProperties!add.jspa</page:param>
        <page:param name="submitButtonText"><ww:text name="'common.forms.add'"/></page:param>
        <page:param name="submitButtonName">Add</page:param>

        <aui:component template="formSubHeading.jsp" theme="'aui'">
            <aui:param name="'text'"><ww:text name="'admin.edituserproperties.add.property'"/></aui:param>
        </aui:component>

        <p><ww:text name="'admin.edituserproperties.example'"/></p>

        <page:applyDecorator name="auifieldgroup">
            <aui:textfield label="text('common.words.key')" id="'property-key'" name="'key'" size="20" theme="'aui'" />
        </page:applyDecorator>
        <page:applyDecorator name="auifieldgroup">
            <aui:textfield label="text('common.words.value')" id="'property-value'" name="'value'" size="20" theme="'aui'" />
        </page:applyDecorator>
        <page:applyDecorator name="auifieldgroup">
            <aui:component name="'name'" template="hidden.jsp" theme="'aui'" />
        </page:applyDecorator>

    </page:applyDecorator>
</body>
</html>
