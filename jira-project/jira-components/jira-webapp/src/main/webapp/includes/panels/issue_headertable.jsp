<%@ page import="com.atlassian.jira.issue.Issue" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="webwork.util.TextUtil" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<ww:bean id="fieldVisibility" name="'com.atlassian.jira.web.bean.FieldVisibilityBean'"/>
<ww:bean id="permissionCheck" name="'com.atlassian.jira.web.bean.PermissionCheckBean'"/>
<ww:bean id="projectDescriptionRenderer" name="'com.atlassian.jira.web.bean.ProjectDescriptionRendererBean'"/>

<ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pageHeader'">
    <ui:param name="'content'">
        <ww:property value="/issueObject/projectObject">
            <ww:if test="./avatar != null">
                <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pageHeaderImage'">
                    <ui:param name="'content'">
                        <ui:soy moduleKey="'jira.webresources:soy-templates'" template="'JIRA.Templates.Headers.projectAvatar'">
                            <ui:param name="'id'" value="'heading-avatar'"/>
                            <ui:param name="'projectName'" value="/project/name"/>
                            <ui:param name="'projectKey'" value="/project/key"/>
                            <ui:param name="'avatarAlt'"><ww:property value="./name"/></ui:param>
                            <ui:param name="'avatarUrl'"><ww:url value="'/secure/projectavatar'" atltoken="false"><ww:param name="'pid'" value="./id" /><ww:param name="'avatarId'" value="./avatar/id" /><ww:param name="'size'" value="'large'" /></ww:url></ui:param>
                            <ui:param name="'isSystemAvatar'" value="/project/avatar/systemAvatar"/>
                            <jira:feature-check featureKey="rotp.project.shortcuts">
                                <ui:param name="'hasProjectShortcut'" value="true"/>
                            </jira:feature-check>
                        </ui:soy>
<%--
            <jira:feature-check featureKey="rotp.project.shortcuts">
                <div class="project-shortcut-dialog-trigger" data-key="<ww:property value="./key"/>" data-name="<ww:property value="./name"/>" data-entity-type="jira.project">
            </jira:feature-check>
                    <img id="project-avatar" alt="<ww:property value="./name"/>" class="project-avatar-48" height="48" src="<ww:url value="'/secure/projectavatar'" atltoken="false"><ww:param name="'pid'" value="./id" /><ww:param name="'avatarId'" value="./avatar/id" /><ww:param name="'size'" value="'large'" /></ww:url>" width="48" />
            <jira:feature-check featureKey="rotp.project.shortcuts">
                </div>
            </jira:feature-check>
--%>
                    </ui:param>
                </ui:soy>
            </ww:if>
        </ww:property>
        <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pageHeaderMain'">
            <ui:param name="'content'">
                <ol class="aui-nav aui-nav-breadcrumbs">
                    <li><a id="project-name-val" href="<ww:url value="'/browse/' + /project/string('key')" atltoken="false" />"><ww:property value="/project/string('name')"/></a></li>
                    <ww:if test="/subTask == true">
                        <ww:property value="/parentIssueObject" id="parentIssueObject">
                            <ww:if test="@permissionCheck/issueVisible(.) == true">
                                <li><a class="issue-link"
                                       data-issue-key="<ww:property value="./key" escape="true" />"
                                       href="<ww:url value="'/browse/' + ./key" atltoken="false" />"
                                       id="parent_issue_summary"
                                       title="<ww:property value="./summary" escape="true"/>"><ww:property value="./key"/> <%= TextUtil.escapeHTML(StringUtils.abbreviate(((Issue)pageContext.getAttribute("parentIssueObject")).getSummary(), 40)) %></a></li>
                            </ww:if>
                            <ww:else>
                                <li><ww:property value="./key"/></li>
                            </ww:else>
                        </ww:property>
                    </ww:if>
                    <li><a class="issue-link"
                           data-issue-key="<ww:property value="string('key')" />"
                           href="<ww:url value="'/browse/' + string('key')" atltoken="false" />"
                           id="key-val"
                           rel="<ww:property value="string('id')" />"><ww:property value="string('key')"/></a></li>
                </ol>
                <h1 id="summary-val">
                    <ww:if test="/useKickAss() == true"><ww:property value="string('summary')"/></ww:if>
                    <ww:else><a href="<ww:url value="'/browse/' + string('key')" atltoken="false" />"><ww:property value="string('summary')"/></a></ww:else>
                </h1>
            </ui:param>
        </ui:soy>
        <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pageHeaderActions'">
            <ui:param name="'content'">
                <ww:if test="/searchRequest != null">
                    <ww:property value="/nextPreviousPager">
                        <ww:if test="/nextPreviousPager/hasCurrentKey == true">
                            <div id="issue-header-pager">
                                <ul class="ops page-navigation">
                                    <ww:if test="/nextPreviousPager/previousKey != null">
                                        <li class="previous">
                                            <a id="previous-issue" href="<ww:url value="'/browse/' + ./previousKey" atltoken="false" />" title="<ww:text name="'navigator.previous.title'"/> '<ww:property value="./previousKey" />'">
                                                <span class="icon icon-page-prev"><span><ww:text name="'navigator.previous.title'"/> '<ww:property value="./previousKey" />'</span></span>
                                            </a>
                                        </li>
                                    </ww:if>
                                    <ww:else>
                                        <li class="previous">
                                            <span class="icon icon-page-prev-deactivated" title="<ww:text name="'pager.results.firstpage'"/>"></span>
                                        </li>
                                    </ww:else>
                                    <li class="showing">
                                        <ww:text name="'pager.results.displayissues.short'">
                                            <ww:param name="'value0'"><ww:property value="./currentPosition"/></ww:param>
                                            <ww:param name="'value1'"><ww:property value="./currentSize"/>
                                                <a id="return-to-search" href="<ww:url value="'/secure/IssueNavigator.jspa'" atltoken="false" />" title="<ww:text name="'navigator.return.search'"/>"><ww:text name="'navigator.return.search'"/></a>
                                            </ww:param>
                                        </ww:text>
                                    </li>
                                    <ww:if test="/nextPreviousPager/nextKey != null">
                                        <li class="next">
                                            <a id="next-issue" rel="<ww:property value="./nextKey" />" href="<ww:url value="'/browse/' + ./nextKey" atltoken="false" />" title="<ww:text name="'navigator.next.title'"/> '<ww:property value="./nextKey" />'">
                                                <span class="icon icon-page-next"><span><ww:text name="'navigator.next.title'"/> '<ww:property value="./nextKey" />'</span></span>
                                            </a>
                                        </li>
                                    </ww:if>
                                    <ww:else>
                                        <li class="next">
                                            <span class="icon icon-page-next-deactivated" title="<ww:text name="'pager.results.lastpage'"/>"></span>
                                        </li>
                                    </ww:else>
                                </ul>
                            </div>
                        </ww:if>
                    </ww:property>
                </ww:if>
            </ui:param>
        </ui:soy>
    </ui:param>
</ui:soy>
