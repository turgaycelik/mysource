<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<html>
<head>
    <ww:if test="remoteUser != null">
        <title><ww:text name="'common.concepts.projects'"/></title>
        <meta name="admin.active.section" content="admin_project_menu/project_section"/>
        <meta name="admin.active.tab" content="view_projects"/>
    </ww:if>
    <ww:else>
        <title><ww:text name="'common.words.error'"/></title>
        <meta name="decorator" content="message" />
    </ww:else>
</head>
<body>
<% final String avatarSize = "small"; %>
<ww:if test="remoteUser != null">
    <header class="aui-page-header">
        <div class="aui-page-header-inner">
            <div class="aui-page-header-main">
                <h2><ww:text name="'admin.projects.project.list'"/></h2>
            </div>
            <ww:if test = "/admin == true">
                <div class="aui-page-header-actions">
                    <div class="aui-buttons">
                        <a id="add_project" class="aui-button add-project-trigger" href="<ww:url value="'/secure/admin/AddProject!default.jspa?src=adminprojectslist'" />">
                            <span class="icon jira-icon-add"></span>
                            <ww:text name="'admin.projects.add.project'"/>
                        </a>
                    </div>
                </div>
            </ww:if>
        </div>
    </header>
    <ww:if test="projectObjects/size != 0">
        <table id="project-list" class="aui">
            <thead>
                <tr>
                    <th></th>
                    <th><ww:text name="'common.words.name'"/></th>
                    <th><ww:text name="'issue.field.key'"/></th>
                    <th><ww:text name="'common.concepts.url'"/></th>
                    <th><ww:text name="'common.concepts.projectlead'"/></th>
                    <th><ww:text name="'admin.projects.default.assignee'"/></th>
                    <th><ww:text name="'common.words.operations'"/></th>
                </tr>
            </thead>
            <tbody>
                <ww:iterator value="projectObjects">
                    <tr data-project-key="<ww:property value="./key" />">
                        <td class="cell-type-icon" data-cell-type="avatar">
                            <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.avatar.avatar'">
                                <ui:param name="'tagName'" value="'div'"/>
                                <ui:param name="'isProject'" value="true" />
                                <ui:param name="'size'"><%= avatarSize %></ui:param>
                                <ww:if test="./avatar != null">
                                    <ui:param name="'avatarImageUrl'"><ww:url value="'/secure/projectavatar'" atltoken="false">
                                        <ww:param name="'pid'" value="./id" />
                                        <ww:param name="'avatarId'" value="./avatar/id" />
                                        <ww:param name="'size'"><%= avatarSize %></ww:param>
                                    </ww:url></ui:param>
                                    <ww:if test="./avatar/systemAvatar == true">
                                        <ui:param name="'extraClasses'" value="'jira-system-avatar'"/>
                                    </ww:if>
                                </ww:if>
                                <ww:else>
                                    <ui:param name="'avatarImageUrl'"><%= request.getContextPath() %>/images/16jira.png</ui:param>
                                </ww:else>
                            </ui:soy>
                        </td>
                        <td data-cell-type="name">
                            <a id="view-project-<ww:property value="./id" />" href="<ww:url value="'/plugins/servlet/project-config/' + ./key + '/summary'" atltoken="false"/>"><ww:property value="./name" /></a>
                        </td>
                        <td data-cell-type="key"><ww:property value="./key"/></td>
                        <td class="cell-type-url" data-cell-type="url">
                            <ww:if test="./url == null || ./url == ''">
                                <ww:text name="'browse.projects.no.url'"/>
                            </ww:if>
                            <ww:else>
                                <a href="<ww:property value="./url" />" title="<ww:property value="./url" />"><ww:property value="./url" /></a>
                            </ww:else>
                        </td>
                        <td class="cell-type-user" data-cell-type="lead">
                            <ww:if test="./projectLead != null">
                                <jira:formatuser userKey="./projectLead/key" type="'profileLink'" id="'view_' + ./key + '_projects'"/>
                            </ww:if>
                            <ww:else>
                               <ww:text name="'browse.projects.no.lead'"/>
                            </ww:else>
                        </td>
                        <td class="cell-type-user" data-cell-type="default-assignee">
                            <ww:if test="/defaultAssigneeAssignable(.) == false"><span class="warning" title="<ww:text name="'admin.projects.warning.user.not.assignable'"/>"></ww:if>
                            <ww:text name="/prettyAssigneeType(.)"/>
                            <ww:if test="/defaultAssigneeAssignable(.) == false"></span></ww:if>
                        </td>
                        <td data-cell-type="operations">
                            <ul class="operations-list">
                            <ww:if test="/projectAdmin(.) == true || /admin == true">
                                <li><a class="edit-project" id="edit-project-<ww:property value="./id" />" href="<ww:url value="'/secure/project/EditProject!default.jspa'" atltoken="false"><ww:param name="'pid'" value="./id" /><ww:param name="'returnUrl'" value="'ViewProjects.jspa'" /></ww:url>"><ww:text name="'common.words.edit'"/></a></li>
                            </ww:if>
                            <ww:if test = "/admin == true">
                                <li><a id="delete_project_<ww:property value="./id"/>" href="<ww:url value="'/secure/project/DeleteProject!default.jspa'" atltoken="false"><ww:param name="'pid'" value="./id" /><ww:param name="'returnUrl'" value="'ViewProjects.jspa'" /></ww:url>"><ww:text name="'common.words.delete'"/></a></li>
                            </ww:if>
                            </ul>
                        </td>
                    </tr>
                </ww:iterator>
            </tbody>
        </table>
    </ww:if>
    <ww:else>
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'id'" value="'noprojects'"/>
            <aui:param name="'messageType'">warning</aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:text name="'admin.projects.nopermission'"/></p>
            </aui:param>
        </aui:component>
    </ww:else>
</ww:if>
<ww:else>
    <div class="form-body">
        <header>
            <h1><ww:text name="'login.required.title'" /></h1>
        </header>
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">warning</aui:param>
            <aui:param name="'messageHtml'">
                <p>
                    <ww:if test="/allowSignUp == true">
                        <ww:text name="'admin.projects.login.or.signup'">
                            <ww:param name="'value0'"><jira:loginlink><ww:text name="'admin.common.words.log.in'"/></jira:loginlink></ww:param>
                            <ww:param name="'value1'"><a href="<ww:url value="'/secure/Signup!default.jspa'" atltoken="false"/>"></ww:param>
                            <ww:param name="'value2'"></a></ww:param>
                        </ww:text>
                    </ww:if>
                    <ww:else>
                        <ww:text name="'admin.projects.login'">
                            <ww:param name="'value0'"><jira:loginlink><ww:text name="'admin.common.words.log.in'"/></jira:loginlink></ww:param>
                        </ww:text>
                    </ww:else>
                </p>
            </aui:param>
        </aui:component>
    </div>
</ww:else>
</body>
</html>