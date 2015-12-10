<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <ww:if test="user != null">
        <title><ww:text name="'common.words.user'"/>: <ww:property value="user/displayName" /></title>
    </ww:if>
    <ww:else>
        <title><ww:text name="'admin.viewuser.user.does.not.exist.title'"/></title>
        <meta name="decorator" content="error" />
    </ww:else>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_section"/>
    <meta name="admin.active.tab" content="user_browser"/>
</head>
<body>
    <ww:if test="user != null">
        <ui:soy moduleKey="'jira.webresources:soy-templates'" template="'JIRA.Templates.Headers.pageHeader'">
            <ui:param name="'mainContent'">
                <ul class="aui-nav aui-nav-breadcrumbs">
                    <li><a href="<ww:url page="/secure/admin/user/UserBrowser.jspa" atltoken="false"/>"><ww:text name="'admin.menu.usersandgroups.user.browser'" /></a></li>
                </ul>
                <h2><ww:property value="user/displayName" /> <ww:if test="user/active == false">(<ww:text name="'admin.common.words.inactive'"/>)</ww:if></h2>
            </ui:param>
            <ui:param name="'actionsContent'">
                <div class="aui-buttons">
                    <a class="aui-button" href="<ww:url page="/secure/ViewProfile.jspa"><ww:param name="'name'" value="user/name"/></ww:url>"><ww:text name="'admin.viewuser.view.public.profile'"/></a>
                    <a class="aui-button" id="viewprojectroles_link" href="<ww:url page="ViewUserProjectRoles!default.jspa"><ww:param name="'name'" value="user/name" /><ww:param name="'returnUrl'" value="'ViewUser.jspa'"/></ww:url>"><ww:text name="'admin.viewuser.view.project.roles'"/></a>
                </div>
                <div class="aui-buttons">
                    <a href="#" class="aui-dropdown2-trigger aui-button" aria-owns="user-edit-options" aria-haspopup="true" aria-controls="user-edit-options"><ww:text name="'common.words.actions'" /></a>
                </div>
                <span></span> <%-- JRADEV-20703 --%>
                <div class="aui-dropdown2 aui-style-default" id="user-edit-options">
                    <ww:if test="/selectedUserEditable == true">
                        <div class="aui-dropdown2-section">
                            <ul>
                                <li><a class="trigger-dialog" href="<ww:url page="EditUser!default.jspa"><ww:param name="'editName'" value="user/name" /></ww:url>"><ww:text name="'admin.viewuser.edit.details'"/></a></li>
                                <ww:if test="/canUpdateUserPassword == true">
                                    <li><a class="trigger-dialog" href="<ww:url page="SetPassword!default.jspa"><ww:param name="'name'" value="user/name" /></ww:url>"><ww:text name="'admin.viewuser.set.password'"/></a></li>
                                </ww:if>
                                <ww:if test="/onDemand == false">
                                    <li><a class="trigger-dialog" id="rememberme_link" href="<ww:url page="UserRememberMeCookies!default.jspa"><ww:param name="'name'" value="user/name" /></ww:url>"><ww:text name="'admin.viewuser.rememberme.user'"/></a></li>
                                </ww:if>
                                <li><a class="trigger-dialog" id="deleteuser_link" href="<ww:url page="DeleteUser!default.jspa"><ww:param name="'name'" value="user/name" /></ww:url>"><ww:text name="'admin.viewuser.delete.user'"/></a></li>
                            </ul>
                        </div>
                    </ww:if>
                    <div class="aui-dropdown2-section">
                        <ul>
                            <ww:if test="/selectedUsersGroupsEditable == true">
                                <li><a class="trigger-dialog" id="editgroups_link" href="<ww:url page="EditUserGroups!default.jspa"><ww:param name="'name'" value="user/name" /></ww:url>"><ww:text name="'admin.viewuser.edit.groups'"/></a></li>
                            </ww:if>
                            <li><a href="<ww:url page="EditUserProperties.jspa"><ww:param name="'name'" value="user/name" /></ww:url>"><ww:text name="'admin.viewuser.edit.properties'"/></a></li>
                        </ul>
                    </div>
                </div>
            </ui:param>
        </ui:soy>

        <ww:if test="/remoteUserPermittedToEditSelectedUser == false">
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">warning</aui:param>
                <aui:param name="'messageHtml'"><p><ww:text name="'admin.viewuser.user.is.sysadmin.and.you.are.admin'"/></p></aui:param>
            </aui:component>
        </ww:if>

        <ww:if test="/showPasswordUpdateMsg == true">
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">success</aui:param>
                <aui:param name="'messageHtml'">
                    <p><ww:text name="'admin.setpassword.success.desc'"><ww:param name="param1"><ww:property value="user/name"/></ww:param></ww:text></p>
                </aui:param>
            </aui:component>
        </ww:if>
        <div class="module vcard" id="viewUserDetails"><%-- hCard microformat --%>
            <div class="mod-header">
                <h3><ww:text name="'admin.viewuser.account.information'" /></h3>
            </div>
            <div class="mod-content">
                <div class="aui-group">
                    <div class="aui-item">
                        <ul class="item-details">
                            <li data-userdata-group="user-details">
                                <dl data-userdata-row="username">
                                    <dt><ww:text name="'common.words.username'"/>:</dt>
                                    <dd id="username"><ww:property value="user/name" /></dd>
                                </dl>
                                <dl data-userdata-row="fullname">
                                    <dt><ww:text name="'common.words.fullname'"/>:</dt>
                                    <dd id="displayName" class="fn"><ww:property value="user/displayName" /></dd>
                                </dl>
                                <dl data-userdata-row="email">
                                    <dt><ww:text name="'common.words.email'"/>:</dt>
                                    <dd><a class="email" href="mailto:<ww:property value="user/emailAddress" />"><ww:property value="user/emailAddress" /></a></dd>
                                </dl>
                            </li>
                            <li data-userdata-row="user-directory">
                                <dl data-userdata-row="directory">
                                    <dt><ww:text name="'admin.user.directory'"/>:</dt>
                                    <dd id="directory"><ww:property value="/directoryName" /></dd>
                                </dl>
                                <dl data-userdata-row="groups">
                                    <dt><ww:text name="'common.words.groups'"/>:</dt>
                                    <dd id="groups" class="user-group-info">
                                        <ww:if test="userGroups/empty==true">
                                            <aui:component template="auimessage.jsp" theme="'aui'">
                                                <aui:param name="'messageType'">warning</aui:param>
                                                <aui:param name="'messageHtml'">
                                                    <p>
                                                        <ww:text name="'admin.viewuser.user.not.in.group'">
                                                            <ww:param name="param0"><ww:property value="user/displayName" /></ww:param>
                                                        </ww:text>
                                                    </p>
                                                </aui:param>
                                            </aui:component>
                                        </ww:if>
                                        <ww:else>
                                            <ul class="aui-nav">
                                                <ww:iterator value="userGroups">
                                                    <li><ww:property value="." /></li>
                                                </ww:iterator>
                                            </ul>
                                        </ww:else>
                                    </dd>
                                </dl>
                            </li>
                        </ul>
                    </div>
                    <div class="aui-item">
                        <ul class="item-details">
                            <li data-userdata-group="login-details">
                                <dl data-userdata-row="login-count">
                                    <dt><ww:text name="'login.count'"/>:</dt>
                                    <dd id="loginCount"><ww:property value="/loginCount(user)" /></dd>
                                </dl>
                                <dl data-userdata-row="last-login">
                                    <dt><ww:text name="'login.last.login'"/>:</dt>
                                    <dd id="lastLogin"><ww:property value="/lastLogin(user)" /></dd>
                                </dl>
                                <dl data-userdata-row="previous-login">
                                    <dt><ww:text name="'login.prev.login'"/>:</dt>
                                    <dd id="previousLogin"><ww:property value="/previousLogin(user)" /></dd>
                                </dl>
                                <ww:if test="/elevatedSecurityCheckRequired(user) == true">
                                    <dl data-userdata-row="elevated-security-check-required">
                                        <dt>&nbsp;</dt>
                                        <dd><em><ww:text name="'login.elevated.security.check.required'"/></em> <a href="<ww:url page="ResetFailedLoginCount.jspa"><ww:param name="'name'" value="user/name" /><ww:param name="'returnUrl'" value="'/secure/admin/user/ViewUser.jspa'"/></ww:url>"><ww:text name="'admin.resetfailedlogin.title'"/></a></dd>
                                    </dl>
                                </ww:if>
                                <dl data-userdata-row="last-failed-login">
                                    <dt><ww:text name="'login.last.failed.login'"/>:</dt>
                                    <dd id="lastFailedLogin"><ww:property value="/lastFailedLogin(user)" /></dd>
                                </dl>
                                <dl data-userdata-row="current-failed-login-count">
                                    <dt><ww:text name="'login.current.failed.login.count'"/>:</dt>
                                    <dd id="currentFailedLoginCount"><ww:property value="/currentFailedLoginCount(user)" /></dd>
                                </dl>
                                <dl data-userdata-row="total-failed-login-count">
                                    <dt><ww:text name="'login.total.failed.login.count'"/>:</dt>
                                    <dd id="totalFailedLoginCount"><ww:property value="/totalFailedLoginCount(user)" /></dd>
                                </dl>
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
        <ww:if test="/userProperties != null && /userProperties/empty == false">
            <div class="module" data-userdata-group="user-properties">
                <div class="mod-header">
                    <h3><ww:text name="'common.words.properties'"/></h3>
                </div>
                <div class="mod-content">
                    <table class="aui">
                        <thead>
                            <tr>
                                <th><ww:text name="'common.concepts.key'" /></th>
                                <th><ww:text name="'common.concepts.value'" /></th>
                            </tr>
                        </thead>
                        <tbody>
                            <ww:iterator value="/userProperties">
                                <tr>
                                    <td class="cell-type-key"><ww:property value="key" /></td>
                                    <td><ww:property value="value" /></td>
                                </tr>
                            </ww:iterator>
                        </tbody>
                    </table>
                </div>
            </div>
        </ww:if>
    </ww:if>
    <ww:else>
        <h1><ww:text name="'admin.viewuser.user.does.not.exist.title'" /></h1>
    </ww:else>
</body>
</html>