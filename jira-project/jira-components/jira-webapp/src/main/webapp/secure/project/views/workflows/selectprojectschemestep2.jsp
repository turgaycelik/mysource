<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib prefix="jira" uri="jiratags" %>
<%@ taglib prefix="aui" uri="webwork" %>

<html>
<head>
    <title><ww:if test="/draftMigration == true"><ww:text name="'admin.selectworkflowscheme.migration.publish.draft'"/></ww:if><ww:else><ww:text name="'admin.selectworkflowscheme.select.workflow.scheme'"/></ww:else></title>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflow_schemes"/>
    <jira:web-resource-require modules="jira.webresources:workflow-migration" />
</head>
<body>
    <header>
        <h2>
            <ww:if test="/draftMigration == true">
                <ww:text name="'admin.selectworkflowscheme.migration.publish.draft'"/>
            </ww:if>
            <ww:else>
                <ww:text name="'admin.selectworkflowscheme.associate.scheme.to.project'"/>: <ww:property value="project/string('name')" />
            </ww:else>
        </h2>
    </header>
    <p>
        <ww:if test="/draftMigration == true">
            <ww:text name="'admin.selectworkflowscheme.step1.draft'">
                <ww:param name="'value0'"><strong></ww:param>
                <ww:param name="'value1'"></strong></ww:param>
            </ww:text>
        </ww:if>
        <ww:else>
            <ww:text name="'admin.selectworkflowscheme.step2'">
                <ww:param name="'value0'"><strong></ww:param>
                <ww:param name="'value1'"></strong></ww:param>
            </ww:text>
        </ww:else>
    </p>

    <page:applyDecorator id="workflow-mapping" name="auiform">
        <page:param name="cancelLinkURI"><%= request.getContextPath() %><ww:property value="/returnUrlForCancelLink"/></page:param>
        <ww:if test="currentTask">
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">info</aui:param>
                <aui:param name="'messageHtml'">
                    <ww:text name="'admin.selectworkflowscheme.blocked.by.user'">
                        <ww:param name="'value0'"><jira:formatuser userName="currentTask/user/name" type="'profileLink'" id="'user_profile'"/></ww:param>
                    </ww:text>

                    <ww:text name="'admin.selectworkflowscheme.goto.progressbar'">
                        <ww:param name="'value0'"><a href="<ww:property value="currentTask/progressURL"/>"></ww:param>
                        <ww:param name="'value1'"><ww:text name="'common.words.here'"/></ww:param>
                        <ww:param name="'value2'"></a></ww:param>
                    </ww:text>
                </aui:param>
            </aui:component>
        </ww:if>
        <ww:elseIf test="/invalidInput == false">
            <page:param name="id">workflow-associate</page:param>
            <page:param name="name">workflow-associate</page:param>
            <page:param name="action">SelectProjectWorkflowSchemeStep2.jspa</page:param>
            <page:param name="submitButtonName">Associate</page:param>
            <page:param name="submitButtonText"><ww:text name="'admin.projects.schemes.associate'"/></page:param>

            <ww:if test="/haveIssuesToMigrate == false">
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'id'">workflow-associate-noissues</aui:param>
                    <aui:param name="'messageType'">info</aui:param>
                    <aui:param name="'messageHtml'"><ww:text name="'admin.selectworkflows.no.issues.to.migrate'"/></aui:param>
                </aui:component>
            </ww:if>
            <ww:elseIf test="/migrationHelper/typesNeedingMigration/empty == true">
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">info</aui:param>
                    <aui:param name="'messageHtml'"><ww:text name="'admin.selectworkflowscheme.all.issues.automatic'"/></aui:param>
                </aui:component>
            </ww:elseIf>
            <ww:else>
                <table id="workflow-mapping-table" class="aui">
                    <thead>
                        <tr>
                            <th class="workflow-mapping-issue-type"><ww:text name="'common.concepts.issuetype'"/></th>
                            <th class="workflow-mapping-current-status"><ww:text name="'admin.selectworkflowscheme.current.status'"/></th>
                            <th class="workflow-mapping-arrow"></th>
                            <th><ww:text name="'admin.selectworkflowscheme.target.status'"/></th>
                        </tr>
                    </thead>
                    <ww:iterator value="/migrationHelper/typesNeedingMigration" status="'status'">
                        <tbody>
                            <tr class="workflow-mapping-issue-type-row<ww:if test="/numAffectedIssues(.) == 0"> collapsed</ww:if>">
                                <td>
                                    <ww:component name="'status'" template="constanticon.jsp">
                                        <ww:param name="'contextPath'"><%= request.getContextPath() %></ww:param>
                                        <ww:param name="'iconurl'" value="./string('iconurl')" />
                                        <ww:param name="'alt'"><ww:property value="./string('name')" /></ww:param>
                                        <ww:param name="'title'"><ww:property value="./string('name')" /> - <ww:property value="./string('description')" /></ww:param>
                                    </ww:component>
                                    <ww:property value="./string('name')" />
                                    <span title="<ww:text name="'admin.selectworkflowscheme.num.affected.issues'"><ww:param name="'value0'"><ww:property value ="/numAffectedIssues(.)"/></ww:param><ww:param name="'value1'"><ww:property value ="/totalAffectedIssues(.)"/></ww:param></ww:text>" class="aui-lozenge status-issue-count"><ww:property value="/numAffectedIssues(.)"/></span>
                                <td class="workflow-mapping-current-status"><ww:property value="/existingWorkflow(.)/displayName" /></td>
                                <td></td>
                                <td><ww:property value="/targetWorkflow(.)/displayName" /></td>
                            </tr>
                            <ww:iterator value="/statusesNeedingMigration(.)">
                                <tr>
                                    <td></td>
                                    <td class="workflow-mapping-current-status">
                                        <ww:component name="'status'" template="issuestatus.jsp" theme="'aui'">
                                            <ww:param name="'issueStatus'" value="constantsManager/statusObject(./string('id'))"/>
                                            <ww:param name="'isSubtle'" value="false"/>
                                            <ww:param name="'isCompact'" value="false"/>
                                        </ww:component>
                                    </td>
                                    <td class="workflow-mapping-arrow">&rarr;</td>
                                    <td>
                                        <select name="<ww:property value='/selectListName(.., .)'/>">
                                            <ww:iterator value="/targetStatuses(..)">
                                                <option value="<ww:property value="string('id')"/>"><ww:property value="string('name')"/></option>
                                            </ww:iterator>
                                        </select>
                                    </td>
                                </tr>
                            </ww:iterator>
                        </tbody>
                    </ww:iterator>
                </table>
            </ww:else>

            <ui:component name="'projectId'" template="hidden.jsp" theme="'aui'"/>
            <ui:component name="'schemeId'" template="hidden.jsp" theme="'aui'"/>
            <ui:component name="'draftMigration'" template="hidden.jsp" theme="'aui'"/>
            <ui:component name="'projectIdsParameter'" template="hidden.jsp" theme="'aui'"/>
        </ww:elseIf>
    </page:applyDecorator>
</body>
</html>
