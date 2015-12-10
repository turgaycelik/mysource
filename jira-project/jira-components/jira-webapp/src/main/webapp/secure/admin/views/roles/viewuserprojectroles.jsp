<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <title><ww:text name="'admin.view.user.projectroles.title'" /></title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_section"/>
    <meta name="admin.active.tab" content="user_browser"/>
</head>
<body>
    <ui:soy moduleKey="'jira.webresources:soy-templates'" template="'JIRA.Templates.Headers.pageHeader'">
        <ui:param name="'mainContent'">
            <ul class="aui-nav aui-nav-breadcrumbs">
                <li><a href="<ww:url page="/secure/admin/user/UserBrowser.jspa" atltoken="false"/>"><ww:text name="'admin.menu.usersandgroups.user.browser'" /></a></li>
                <li><a id="return_link" href="<ww:url value="returnUrl"><ww:param name="'name'" value="name"/></ww:url>"><ww:property value="/projectRoleEditUser/displayName" /></a></li>
            </ul>
            <h2><ww:text name="'admin.view.user.projectroles.title'" /></h2>
        </ui:param>
        <ui:param name="'actionsContent'">
            <div class="aui-buttons">
                <a class="aui-button" href="<ww:url page="EditUserProjectRoles!default.jspa"><ww:param name="'name'" value="name" /></ww:url>"><ww:text name="'admin.viewuser.edit.project.roles'"/></a>
            </div>
        </ui:param>
        <ui:param name="'helpContent'">
            <aui:component name="'users'" template="help.jsp" theme="'aui'">
                <aui:param name="'helpURLFragment'">#Assigning+a+User+to+a+Project+Role</aui:param>
            </aui:component>
        </ui:param>
    </ui:soy>

    <ww:if test="/visibleProjectsByCategory/size != 0">
        <ww:iterator value="/visibleProjectsByCategory">
            <div class="module twixi-block">
                <div class="mod-header">
                    <h3 class="twixi-trigger toggle-title">
                        <ww:if test="key != null">
                            <ww:property value="key/string('name')"/>
                        </ww:if>
                        <ww:else>
                            <ww:text name="'admin.view.user.projectroles.project.category.uncategorised'"/>
                        </ww:else>
                    </h3>
                </div>
                <div class="mod-content">
                    <table class="aui aui-table-rowhover role-access">
                        <thead>
                            <tr>
                                <th class="cell-type-key"><ww:text name="'common.words.project'"/></th>
                                <ww:iterator value="allProjectRoles">
                                    <th class="cell-type-centered" width="<ww:property value="/projectRoleColumnWidth"/>%"><ww:property value="./name"/></th>
                                </ww:iterator>
                            </tr>
                        </thead>
                        <tbody>
                            <ww:iterator value="value">
                                <tr>
                                    <td class="cell-type-key"><ww:property value="./name"/></td>
                                    <ww:iterator value="allProjectRoles">
                                        <ww:if test="/roleForProjectSelected(., ..) == true && /userInProjectRoleOtherType(., ..) != null">
                                            <!-- Explicitly granted access AND inheriting from a group they're in -->
                                            <td class="role-member cell-type-centered">
                                                <span class="aui-icon icon-role-member" id="<ww:property value="../id"/>_<ww:property value="./id"/>_direct_and_group"
                                                      title="<ww:text name="'admin.view.user.projectroles.group.inherited'"><ww:param><ww:property value="/userInProjectRoleOtherType(., ..)"/></ww:param></ww:text>"
                                                >
                                                    <ww:text name="'admin.view.user.projectroles.user.direct.and.group.member'"/>
                                                    <ww:text name="'admin.view.user.projectroles.group.inherited'"><ww:param><ww:property value="/userInProjectRoleOtherType(., ..)"/></ww:param></ww:text>
                                                </span>
                                            </td>
                                        </ww:if>
                                        <ww:elseIf test="/roleForProjectSelected(., ..) == true">
                                            <!-- Explicitly granted access only -->
                                            <td class="role-member cell-type-centered">
                                                <span class="aui-icon icon-role-member" id="<ww:property value="../id"/>_<ww:property value="./id"/>_direct">
                                                    <ww:text name="'admin.view.user.projectroles.user.direct.member'"/>
                                                </span>
                                            </td>
                                        </ww:elseIf>
                                        <ww:elseIf test="/userInProjectRoleOtherType(., ..) != null">
                                            <!-- Inheriting access from a group they're in -->
                                            <td class="role-member cell-type-centered">
                                                <span class="aui-icon icon-role-member" id="<ww:property value="../id"/>_<ww:property value="./id"/>_group"
                                                      title="<ww:text name="'admin.view.user.projectroles.group.inherited'"><ww:param><ww:property value="/userInProjectRoleOtherType(., ..)"/></ww:param></ww:text>"
                                                >
                                                    <ww:text name="'admin.view.user.projectroles.user.group.member'"/>
                                                    <ww:text name="'admin.view.user.projectroles.group.inherited'"><ww:param><ww:property value="/userInProjectRoleOtherType(., ..)"/></ww:param></ww:text>
                                                </span>
                                            </td>
                                        </ww:elseIf>
                                        <ww:else>
                                            <!-- No access to the role -->
                                            <td class="role-not-member cell-type-centered">
                                                <span class="aui-icon icon-role-not-member" id="<ww:property value="../id"/>_<ww:property value="./id"/>_none">
                                                    <ww:text name="'admin.view.user.projectroles.user.not.member'"/>
                                                </span>
                                            </td>
                                        </ww:else>
                                    </ww:iterator>
                                </tr>
                            </ww:iterator>
                        </tbody>
                    </table>
                </div>
            </div>
        </ww:iterator>
    </ww:if>
    <ww:else>
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">info</aui:param>
            <aui:param name="'messageHtml'"><ww:text name="'admin.view.user.projectroles.noprojects.found'"/></aui:param>
        </aui:component>
    </ww:else>
</body>
</html>
