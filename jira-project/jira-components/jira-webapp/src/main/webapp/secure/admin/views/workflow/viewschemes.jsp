<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib prefix="jira" uri="jiratags" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflow_schemes"/>
    <jira:web-resource-require modules="jira.webresources:workflow-administration"/>
    <title><ww:text name="'admin.schemes.workflow.workflow.schemes'"/></title>
</head>
<body>
    <ui:soy moduleKey="'jira.webresources:soy-templates'" template="'JIRA.Templates.Headers.pageHeader'">
        <ui:param name="'mainContent'">
            <h2><ww:text name="'admin.schemes.workflow.workflow.schemes'"/></h2>
        </ui:param>
        <ui:param name="'actionsContent'">
            <div class="aui-buttons">
                <a id="add_workflowscheme" class="aui-button trigger-dialog" href="<ww:url atltoken="false" page="AddWorkflowScheme!default.jspa"/>">
                    <span class="icon jira-icon-add"></span>
                    <ww:text name="'admin.schemes.workflow.add.a.new.workflow.scheme'"/>
                </a>
            </div>
        </ui:param>
        <ui:param name="'helpContent'">
            <aui:component name="'workflow'" template="help.jsp" theme="'aui'"/>
        </ui:param>
    </ui:soy>

    <aui:component template="module.jsp" theme="'aui'">
        <aui:param name="'id'">WorkflowSchemes</aui:param>
        <aui:param name="'contentHtml'">
            <ww:if test="/hasScheme == false">
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">info</aui:param>
                    <aui:param name="'messageHtml'">
                        <ww:text name="'admin.schemes.workflow.no.workflow.schemes.configured'"/>
                    </aui:param>
                </aui:component>
            </ww:if>
            <p><ww:text name="'admin.schemes.workflow.description'"/></p>
            <ww:if test="/hasScheme == true">
                <div class="module toggle-wrap">
                    <div class="mod-header">
                        <h3 class="toggle-title"><ww:text name="'admin.common.words.active'"/></h3>
                    </div>
                    <div class="mod-content">
                        <ww:if test="activeWorkflowSchemes/empty == true">
                            <p><ww:text name="'admin.schemes.workflow.no.active'"/></p>
                        </ww:if>
                        <ww:else>
                            <table class="aui aui-table-rowhover list-workflow-table">
                                <thead>
                                    <tr>
                                        <th class="workflow-scheme-name">
                                            <ww:text name="'common.words.name'"/>
                                        </th>
                                        <th class="workflow-scheme-projects">
                                            <ww:text name="'common.concepts.projects'"/>
                                        </th>
                                        <th class="workflow-scheme-details">
                                            <ul class="item-details">
                                                <li>
                                                    <dl>
                                                        <dt>
                                                            <ww:text name="'admin.issue.constant.issuetype'"/>
                                                        </dt>
                                                        <dd class="rarr"></dd>
                                                        <dd>
                                                            <ww:text name="'admin.common.words.workflow'"/>
                                                        </dd>
                                                    </dl>
                                                </li>
                                            </ul>
                                        </th>
                                        <th class="workflow-scheme-operations">
                                            <ww:text name="'common.words.operations'"/>
                                        </th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <ww:iterator value="activeWorkflowSchemes">
                                        <tr>
                                            <td class="workflow-scheme-name cell-type-value">
                                                <strong><ww:property value="name"/></strong>
                                                <ww:property value="/draftFor(.)">
                                                    <ww:if test=".">
                                                        <span class="aui-icon aui-icon-small aui-iconfont-info icon-draft"
                                                             title="<ww:text name="'admin.schemes.workflow.draft.information.tooltip'"><ww:param name="'value0'"><ww:property value="lastModifiedUser/displayName"/></ww:param><ww:param name="'value1'"><ww:property value="lastModifiedDate"/></ww:param></ww:text>"></span>
                                                    </ww:if>
                                                </ww:property>
                                                <div class="secondary-text"><ww:property value="description"/></div>
                                            </td>
                                            <td class="workflow-scheme-projects cell-type-value">
                                                <ww:property value="/projects(.)">
                                                    <ww:if test="empty == false">
                                                        <ul>
                                                            <ww:iterator value=".">
                                                                <li><a href="<%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="key"/>/summary"><ww:property value="name" /></a></li>
                                                            </ww:iterator>
                                                        </ul>
                                                    </ww:if>
                                                </ww:property>
                                                <ww:else>
                                                    &nbsp;
                                                </ww:else>
                                            </td>
                                            <td class="workflow-scheme-details cell-type-value">
                                                <ul class="item-details">
                                                    <li>
                                                        <dl>
                                                            <dt>
                                                                <img src="<%= request.getContextPath() %>/images/icons/issuetypes/all_unassigned.png" height="16" width="16" alt="<ww:text name="'admin.schemes.issuesecurity.unassigned.types'"/>"/> <ww:text name="'admin.schemes.issuesecurity.unassigned.types'"/>
                                                            </dt>
                                                            <dd class="rarr">&rarr;</dd>
                                                            <dd>
                                                                <a title="<ww:text name="'admin.workflows.view.workflow.steps'"/>" href="<ww:url value="'/secure/admin/workflows/ViewWorkflowSteps.jspa'" atltoken="false"><ww:param name="'workflowMode'" value="'live'" /><ww:param name="'workflowName'"><ww:property value="actualDefaultWorkflow"/></ww:param></ww:url>">
                                                                    <ww:property value="actualDefaultWorkflow"/>
                                                                </a>
                                                            </dd>
                                                        </dl>
                                                    </li>
                                                    <ww:iterator value="mappings">
                                                        <ww:if test="key != null">
                                                            <li>
                                                                <dl>
                                                                    <dt>
                                                                        <ww:property value="/issueType(key)">
                                                                            <ww:component name="'issuetype'" template="constanticon.jsp">
                                                                                <ww:param name="'contextPath'"><%= request.getContextPath() %></ww:param>
                                                                                <ww:param name="'iconurl'" value="./iconUrl" />
                                                                                <ww:param name="'alt'"><ww:property value="./nameTranslation" /></ww:param>
                                                                                <ww:param name="'title'"><ww:property value="./nameTranslation" /> - <ww:property value="./descTranslation" /></ww:param>
                                                                            </ww:component>
                                                                            <ww:property value="./nameTranslation" />
                                                                        </ww:property>
                                                                    </dt>
                                                                    <dd class="rarr">&rarr;</dd>
                                                                    <dd>
                                                                        <a title="<ww:text name="'admin.workflows.view.workflow.steps'"/>" href="<ww:url value="'/secure/admin/workflows/ViewWorkflowSteps.jspa'" atltoken="false"><ww:param name="'workflowMode'" value="'live'" /><ww:param name="'workflowName'" value="value" /></ww:url>"><ww:property value="value" /></a>
                                                                    </dd>
                                                                </dl>
                                                            </li>
                                                        </ww:if>
                                                    </ww:iterator>
                                                </ul>
                                            </td>
                                            <td class="workflow-scheme-operations">
                                                <ul class="operations-list">
                                                    <li><a id="edit_<ww:property value="id"/>" href="<ww:url page="EditWorkflowScheme.jspa" atltoken="false"><ww:param name="'schemeId'" value="id"/></ww:url>" title="<ww:text name="'admin.schemes.workflow.assign.issue.types'"/>"><ww:text name="'common.words.edit'"/></a></li>
                                                    <li><a id="cp_<ww:property value="id"/>" href="<ww:url page="CopyWorkflowScheme.jspa"><ww:param name="'schemeId'" value="id"/></ww:url>" title="<ww:text name="'admin.schemes.workflow.create.a.copy'"/>"><ww:text name="'common.words.copy'"/></a></li>
                                                </ul>
                                            </td>
                                        </tr>
                                    </ww:iterator>
                                </tbody>
                            </table>
                        </ww:else>
                    </div>
                </div>
                <div class="module toggle-wrap collapsed">
                    <div class="mod-header">
                        <h3 class="toggle-title"><ww:text name="'admin.common.words.inactive'"/></h3>
                    </div>
                    <div class="mod-content">
                        <ww:if test="inactiveWorkflowSchemes/empty == true">
                            <p><ww:text name="'admin.schemes.workflow.no.inactive'"/></p>
                        </ww:if>
                        <ww:else>
                            <table class="aui aui-table-rowhover list-workflow-table">
                                <thead>
                                    <tr>
                                        <th class="workflow-scheme-name">
                                            <ww:text name="'common.words.name'"/>
                                        </th>
                                        <th class="workflow-scheme-projects">
                                            <ww:text name="'common.concepts.projects'"/>
                                        </th>
                                        <th class="workflow-scheme-details">
                                            <ul class="item-details">
                                                <li>
                                                    <dl>
                                                        <dt>
                                                            <ww:text name="'admin.issue.constant.issuetype'"/>
                                                        </dt>
                                                        <dd class="rarr"></dd>
                                                        <dd>
                                                            <ww:text name="'admin.common.words.workflow'"/>
                                                        </dd>
                                                    </dl>
                                                </li>
                                            </ul>
                                        </th>
                                        <th class="workflow-scheme-operations">
                                            <ww:text name="'common.words.operations'"/>
                                        </th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <ww:iterator value="inactiveWorkflowSchemes">
                                        <tr>
                                            <td class="workflow-scheme-name cell-type-value">
                                                <strong><ww:property value="name"/></strong>
                                                <ww:property value="/draftFor(.)">
                                                    <ww:if test=".">
                                                        <span class="aui-icon aui-icon-small aui-iconfont-info icon-draft"
                                                             title="<ww:text name="'admin.schemes.workflow.draft.information.tooltip'"><ww:param name="'value0'"><ww:property value="lastModifiedUser/displayName"/></ww:param><ww:param name="'value1'"><ww:property  value="lastModifiedDate"/></ww:param></ww:text>"></span>
                                                    </ww:if>
                                                </ww:property>
                                                <div class="secondary-text"><ww:property value="description"/></div>
                                            </td>
                                            <td class="workflow-scheme-projects cell-type-value">&nbsp;</td>
                                            <td class="workflow-scheme-details cell-type-value">
                                                <ul class="item-details">
                                                    <li>
                                                        <dl>
                                                            <dt>
                                                                <img src="<%= request.getContextPath() %>/images/icons/issuetypes/all_unassigned.png" height="16" width="16" alt="<ww:text name="'admin.schemes.issuesecurity.unassigned.types'"/>"/> <ww:text name="'admin.schemes.issuesecurity.unassigned.types'"/>
                                                            </dt>
                                                            <dd class="rarr">&rarr;</dd>
                                                            <dd>
                                                                <a title="<ww:text name="'admin.workflows.view.workflow.steps'"/>" href="<ww:url atltoken="false" value="'/secure/admin/workflows/ViewWorkflowSteps.jspa'" ><ww:param name="'workflowMode'" value="'live'" /><ww:param name="'workflowName'"><ww:property value="actualDefaultWorkflow"/></ww:param></ww:url>">
                                                                    <ww:property value="actualDefaultWorkflow"/>
                                                                </a>
                                                            </dd>
                                                        </dl>
                                                    </li>
                                                    <ww:iterator value="mappings">
                                                        <ww:if test="key != null">
                                                            <li>
                                                                <dl>
                                                                    <dt>
                                                                        <ww:property value="/issueType(key)">
                                                                            <ww:component name="'issuetype'" template="constanticon.jsp">
                                                                                <ww:param name="'contextPath'"><%= request.getContextPath() %></ww:param>
                                                                                <ww:param name="'iconurl'" value="./iconUrl" />
                                                                                <ww:param name="'alt'"><ww:property value="./nameTranslation" /></ww:param>
                                                                                <ww:param name="'title'"><ww:property value="./nameTranslation" /> - <ww:property value="./descTranslation" /></ww:param>
                                                                            </ww:component>
                                                                            <ww:property value="./nameTranslation" />
                                                                        </ww:property>
                                                                    </dt>
                                                                    <dd class="rarr">&rarr;</dd>
                                                                    <dd>
                                                                        <a title="<ww:text name="'admin.workflows.view.workflow.steps'"/>" href="<ww:url atltoken="false" value="'/secure/admin/workflows/ViewWorkflowSteps.jspa'" ><ww:param name="'workflowMode'" value="'live'" /><ww:param name="'workflowName'" value="value" /></ww:url>"><ww:property value="value" /></a>
                                                                    </dd>
                                                                </dl>
                                                            </li>
                                                        </ww:if>
                                                    </ww:iterator>
                                                </ul>
                                            </td>
                                            <td class="workflow-scheme-operations">
                                                <ul class="operations-list">
                                                    <li><a id="edit_<ww:property value="id"/>" href="<ww:url page="EditWorkflowScheme.jspa" atltoken="false"><ww:param name="'schemeId'" value="id"/></ww:url>" title="<ww:text name="'admin.schemes.workflow.assign.issue.types'"/>"><ww:text name="'common.words.edit'"/></a></li>
                                                    <li><a id="cp_<ww:property value="id"/>" href="<ww:url page="CopyWorkflowScheme.jspa"><ww:param name="'schemeId'" value="id"/></ww:url>" title="<ww:text name="'admin.schemes.workflow.create.a.copy'"/>"><ww:text name="'common.words.copy'"/></a></li>
                                                    <li><a class="trigger-dialog" id="del_<ww:property value="id"/>" href="<ww:url page="DeleteWorkflowScheme!default.jspa" atltoken="false"><ww:param name="'schemeId'" value="id"/></ww:url>"><ww:text name="'common.words.delete'"/></a></li>
                                                </ul>
                                            </td>
                                        </tr>
                                    </ww:iterator>
                                </tbody>
                            </table>
                        </ww:else>
                    </div>
                </div>
            </ww:if>
        </aui:param>
    </aui:component>
</body>
</html>
