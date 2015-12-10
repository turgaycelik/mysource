<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<ww:bean name="'com.atlassian.jira.web.util.HelpUtil'" id="helpUtil" />
<html>
<head>
	<title><ww:text name="'admin.menu.usersandgroups.user.browser'"/></title>
    <meta name="admin.active.section" content="admin_users_menu/users_groups_section"/>
    <meta name="admin.active.tab" content="user_browser"/>
    <jira:web-resource-require modules="jira.webresources:userbrowser,
        com.atlassian.plugins.helptips.jira-help-tips:common" />
</head>
<body>
    <ui:soy moduleKey="'jira.webresources:soy-templates'" template="'JIRA.Templates.Headers.pageHeader'">
        <ui:param name="'mainContent'">
            <h2><ww:text name="'admin.menu.usersandgroups.user.browser'" /></h2>
        </ui:param>
        <ww:property value="/opsbarLinks" >
            <ww:if test="./empty == false">
                <ui:param name="'actionsContent'">
                    <div class="aui-buttons">
                        <ww:iterator value=".">
                            <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.buttons.button'">
                                <ui:param name="'tagName'" value="'a'"/>
                                <ui:param name="'id'"><ww:property value="./id"/></ui:param>
                                <ui:param name="'text'"> <ww:property value="./label" escape="false" /></ui:param><!-- JRADEV-20811 aui.buttons.button escapes, so we shouldn't -->
                                <ui:param name="'extraClasses'">trigger-dialog</ui:param>
                                <ui:param name="'extraAttributes'">href="<ww:property value="./url"/>" data-url="<ww:property value="@helpUtil/helpPath('add.new.users')/url" />" title="<ww:property value="./title"/>"</ui:param>
                                <ww:property value="./params/('iconClass')">
                                    <ww:if test=".">
                                        <ui:param name="'iconType'" value="'custom'"/>
                                        <ui:param name="'iconClass'">icon <ww:property value="." /></ui:param>
                                    </ww:if>
                                </ww:property>
                            </ui:soy>
                        </ww:iterator>
                    </div>
                </ui:param>
            </ww:if>
        </ww:property>
    </ui:soy>
    <page:applyDecorator id="user-filter" name="auiform">
        <page:param name="action">UserBrowser.jspa</page:param>
        <page:param name="cssClass">top-label</page:param>
        <page:param name="submitButtonText"><ww:text name="'navigator.tabs.filter'"/></page:param>
        <page:param name="cancelLinkURI">UserBrowser.jspa?emailFilter=&group=&max=<ww:property value="filter/max"/></page:param>
        <page:param name="cancelLinkText"><ww:text name="'admin.userbrowser.reset.filter'"/></page:param>

        <aui:component template="formSubHeading.jsp" theme="'aui'">
            <aui:param name="'text'"><ww:text name="'admin.userbrowser.filter.users'"/></aui:param>
        </aui:component>
        <ww:property value="filter">
            <div class="aui-group">
                <div class="aui-item">
                    <page:applyDecorator name="auifieldgroup">
                        <aui:textfield label="text('admin.userbrowser.username.contains')" maxlength="255" id="'userNameFilter'" name="'userNameFilter'" theme="'aui'">
                            <aui:param name="'cssClass'">full-width-field</aui:param>
                        </aui:textfield>
                    </page:applyDecorator>
                </div>
                <div class="aui-item">
                    <page:applyDecorator name="auifieldgroup">
                        <aui:textfield label="text('admin.userbrowser.userfullname.contains')" maxlength="255" id="'fullNameFilter'" name="'fullNameFilter'" theme="'aui'">
                            <aui:param name="'cssClass'">full-width-field</aui:param>
                        </aui:textfield>
                    </page:applyDecorator>
                </div>
                <div class="aui-item">
                    <page:applyDecorator name="auifieldgroup">
                        <aui:textfield label="text('admin.userbrowser.email.contains')" maxlength="255" id="'emailFilter'" name="'emailFilter'" theme="'aui'">
                            <aui:param name="'cssClass'">full-width-field</aui:param>
                        </aui:textfield>
                    </page:applyDecorator>
                </div>
                <div class="aui-item">
                    <page:applyDecorator name="auifieldgroup">
                        <aui:select label="text('admin.userbrowser.in.group')" id="'group'" name="'group'" list="/groups" listKey="'name'" listValue="'name'" theme="'aui'">
                            <aui:param name="'cssClass'">full-width-field</aui:param>
                            <aui:param name="'defaultOptionText'" value="text('common.filters.any')" />
                            <aui:param name="'defaultOptionValue'" value="''" />
                        </aui:select>
                    </page:applyDecorator>
                </div>
                <div class="aui-item">
                    <page:applyDecorator name="auifieldgroup">
                        <aui:select label="text('admin.userbrowser.users.per.page')" id="'usersPerPage'" name="'max'" list="/maxValues" listKey="'.'" listValue="'.'" theme="'aui'">
                            <aui:param name="'cssClass'">full-width-field</aui:param>
                            <aui:param name="'defaultOptionText'" value="text('common.words.all')" />
                            <aui:param name="'defaultOptionValue'" value="'1000000'" />
                        </aui:select>
                    </page:applyDecorator>
                </div>
            </div>
        </ww:property>
    </page:applyDecorator>
    <div class="aui-group count-pagination">
        <div class="results-count aui-item">
            <ww:text name="'admin.userbrowser.displaying.users'">
                <ww:param name="'value0'"><span class="results-count-start"><ww:property value="niceStart" /></span></ww:param>
                <ww:param name="'value1'"><span class="results-count-end"><ww:property value="niceEnd" /></span></ww:param>
                <ww:param name="'value2'"><span class="results-count-total"><ww:property value="users/size" /></span></ww:param>
            </ww:text>
        </div>
        <div class="pagination aui-item">
            <ww:if test="filter/start > 0">
                <a class="icon icon-previous" href="<ww:url page="UserBrowser.jspa"><ww:param name="'start'" value="filter/previousStart" /></ww:url>"><span>&lt;&lt; <ww:text name="'common.forms.previous'"/></span></a>
            </ww:if>
            <ww:property value = "pager/pages(/browsableItems)">
                <ww:if test="size > 1">
                    <ww:iterator value="." status="'pagerStatus'">
                        <ww:if test="currentPage == true"><strong><ww:property value="pageNumber" /></strong></ww:if>
                        <ww:else>
                            <a href="<ww:url page="UserBrowser.jspa"><ww:param name="'start'" value="start" /></ww:url>"><ww:property value="pageNumber" /></a>
                        </ww:else>
                    </ww:iterator>
                </ww:if>
            </ww:property>
            <ww:if test="filter/end < users/size">
                <a class="icon icon-next" href="<ww:url page="UserBrowser.jspa"><ww:param name="'start'" value="filter/nextStart" /></ww:url>"><span><ww:text name="'common.forms.next'"/> &gt;&gt;</span></a>
            </ww:if>
        </div>
    </div>
    <table class="aui aui-table-rowhover" id="user_browser_table">
        <thead>
            <tr>
                <th>
                    <ww:text name="'common.words.username'"/>
                </th>
                <th>
                    <ww:text name="'common.words.fullname'"/>
                </th>
                <th class="minNoWrap">
                    <ww:text name="'login.details'"/>
                </th>
                <th>
                    <ww:text name="'common.words.groups'"/>
                </th>
                <th>
                    <ww:text name="'admin.user.directory'"/>
                </th>
                <th class="minNoWrap">
                    <ww:text name="'common.words.operations'"/>
                </th>
            </tr>
        </thead>
        <tbody>
            <ww:iterator value="currentPage" status="'status'">
                <tr class="vcard user-row" data-user="<ww:property value="name"/>">
                    <td data-cell-type="username"><div><a id="<ww:property value="name"/>" rel="<ww:property value="name"/>" class="user-hover user-avatar" style="background-image:url(<ww:property value="/avatarUrl(name)"/>);" href="<ww:url page="ViewUser.jspa"><ww:param name="'name'" value="name"/></ww:url>">
                        <span class="username"><ww:if test="active==false"><del></ww:if><ww:property value="name"/><ww:if test="active==false"></del><br>(<ww:text name="'admin.common.words.inactive'"/>)</ww:if></span>
                    </a></div></td>
                    <td data-cell-type="fullname">
                        <span class="fn"><ww:if test="active==false"><del></ww:if><ww:property value="displayName"/><ww:if test="active==false"></del></ww:if></span><br />
                        <a href="mailto:<ww:property value="emailAddress"/>"><span class="email"><ww:property value="emailAddress"/></span></a>
                    </td>
                    <td data-cell-type="login-details" class="minNoWrap">
                        <ww:if test="/everLoggedIn(.) == true">
                            <strong><ww:text name="'common.concepts.count'"/>:</strong> <ww:property value="/loginCount(.)" /><br />
                            <strong><ww:text name="'common.concepts.last'"/>:</strong> <ww:property value="/lastLogin(.)" /><br />
                            <br />
                        </ww:if>
                        <ww:else>
                            <ww:text name="'login.not.recorded'"/><br />
                        </ww:else>
                        <ww:if test="/elevatedSecurityCheckRequired(.) == true">
                            <strong><i><ww:text name="'login.elevated.security.check.required'"/></i></strong><br />
                            <strong><ww:text name="'login.last.failed.login'"/>:</strong> <span id="lastFailedLogin"><ww:property value="/lastFailedLogin(.)" /></span><br />
                            <strong><ww:text name="'login.current.failed.login.count'"/>:</strong> <span id="currentFailedLoginCount"><ww:property value="/currentFailedLoginCount(.)" /></span><br />
                            <strong><ww:text name="'login.total.failed.login.count'"/>:</strong> <span id="totalFailedLoginCount"><ww:property value="/totalFailedLoginCount(.)" /></span><br />
                            <a data-link-type="reset-login-count" href="<ww:url page="ResetFailedLoginCount.jspa"><ww:param name="'name'" value="name" /><ww:param name="'returnUrl'" value="'/secure/admin/user/UserBrowser.jspa'"/></ww:url>"><ww:text name="'admin.resetfailedlogin.title'"/></a>
                        </ww:if>
                    </td>
                    <td data-cell-type="user-groups">
                        <ul>
                        <ww:iterator value="/groupsForUser(.)">
                            <li><a href="<ww:url page="ViewGroup.jspa"><ww:param name="'name'" value="."/></ww:url>"><ww:property value="."/></a></li>
                        </ww:iterator>
                        </ul>
                    </td>
                    <td data-cell-type="user-directory"><ww:property value="/directoryForUser(.)"/></td>
                    <td data-cell-type="operations">
                        <ul class="operations-list">
                            <ww:if test="/remoteUserPermittedToEditSelectedUsersGroups(.) == true">
                                <li><a class="trigger-dialog editgroups_link" id="editgroups_<ww:property value="name"/>" href="<ww:url page="EditUserGroups!default.jspa"><ww:param name="'name'" value="name" /><ww:param name="'returnUrl'" value="'UserBrowser.jspa'" /></ww:url>"><ww:text name="'common.words.groups'"/></a></li>
                            </ww:if>
                        <li><a id="projectroles_link_<ww:property value="name"/>" href="<ww:url page="ViewUserProjectRoles!default.jspa"><ww:param name="'name'" value="name" /><ww:param name="'returnUrl'" value="'UserBrowser.jspa'" /></ww:url>"><ww:text name="'common.words.project.roles'"/></a></li>
                            <ww:if test="/remoteUserPermittedToEditSelectedUser(.) == true">
                                <li><a class="trigger-dialog" id="edituser_link_<ww:property value="name"/>" href="<ww:url page="EditUser!default.jspa"><ww:param name="'editName'" value="name" /><ww:param name="'returnUrl'" value="'UserBrowser.jspa'" /></ww:url>"><ww:text name="'common.words.edit'"/></a></li>
                                <li><a class="trigger-dialog" id="deleteuser_link_<ww:property value="name"/>" href="<ww:url page="DeleteUser!default.jspa"><ww:param name="'name'" value="name" /><ww:param name="'returnUrl'" value="'UserBrowser.jspa'" /></ww:url>"><ww:text name="'common.words.delete'"/></a></li>
                            </ww:if>
                        </ul>
                    </td>
                </tr>
            </ww:iterator>
        </tbody>
    </table>
    <div class="aui-group count-pagination">
        <div class="pagination aui-item">
            <ww:if test="filter/start > 0">
                <a class="icon icon-previous" href="<ww:url page="UserBrowser.jspa"><ww:param name="'start'" value="filter/previousStart" /></ww:url>"><span>&lt;&lt; <ww:text name="'common.forms.previous'"/></span></a>
            </ww:if>
            <ww:property value = "pager/pages(/browsableItems)">
                <ww:if test="size > 1">
                    <ww:iterator value="." status="'pagerStatus'">
                        <ww:if test="currentPage == true"><strong><ww:property value="pageNumber" /></strong></ww:if>
                        <ww:else>
                            <a href="<ww:url page="UserBrowser.jspa"><ww:param name="'start'" value="start" /></ww:url>"><ww:property value="pageNumber" /></a>
                        </ww:else>
                    </ww:iterator>
                </ww:if>
            </ww:property>
            <ww:if test="filter/end < users/size">
                <a class="icon icon-next" href="<ww:url page="UserBrowser.jspa"><ww:param name="'start'" value="filter/nextStart" /></ww:url>"><span><ww:text name="'common.forms.next'"/> &gt;&gt;</span></a>
            </ww:if>
        </div>
    </div>
</body>
</html>
