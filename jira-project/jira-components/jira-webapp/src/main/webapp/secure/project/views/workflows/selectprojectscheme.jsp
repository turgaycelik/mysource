<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib prefix="jira" uri="jiratags" %>

<html>
<head>
    <title><ww:text name="'admin.selectworkflowscheme.select.workflow.scheme'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflow_schemes"/>
</head>

<body>

<ww:if test="schemes == null || schemes/size == 0">
    <page:applyDecorator name="jirapanel">
        <page:param name="title">
            <ww:text name="'admin.selectworkflowscheme.associate.scheme.to.project'"/>: <ww:property value="project/string('name')" />
        </page:param>
        <page:param name="width">100%</page:param>

        <p>
        <ww:text name="'admin.selectworkflowscheme.currently.no.workflows'"/>
        </p>
        <p>
            <ww:text name="'admin.selectworkflowscheme.currently.no.workflows.action'">
                <ww:param name="'value0'"><a href="<%= request.getContextPath() %>/secure/admin/ViewWorkflowSchemes.jspa"></ww:param>
                <ww:param name="'value1'"></a></ww:param>
            </ww:text>
        </p>
    </page:applyDecorator>
</ww:if>
<ww:else>
    <page:applyDecorator name="jiraform">
        <page:param name="title">
            <ww:text name="'admin.selectworkflowscheme.associate.scheme.to.project'"/>
        </page:param>
        <page:param name="description">
            <p><ww:text name="'admin.selectworkflowscheme.step1'">
                <ww:param name="'value0'"><b></ww:param>
                <ww:param name="'value1'"></b></ww:param>
            </ww:text></p>
            <p>
                <ww:if test="/systemAdministrator == true">
                    <ww:text name="'admin.selectworkflowscheme.backupnote'">
                        <ww:param name="'value0'"><span class="note"></ww:param>
                        <ww:param name="'value1'"></span></ww:param>
                        <ww:param name="'value2'"><a href="<%= request.getContextPath() %>/secure/admin/XmlBackup!default.jspa"></ww:param>
                        <ww:param name="'value3'"></a></ww:param>
                    </ww:text>
                </ww:if>
                <ww:else>
                    <ww:text name="'admin.selectworkflowscheme.backupnote.admin'">
                        <ww:param name="'value0'"><span class="note"></ww:param>
                        <ww:param name="'value1'"></span></ww:param>
                        <ww:param name="'value2'"> </ww:param>
                        <ww:param name="'value3'"> </ww:param>
                    </ww:text>
                </ww:else>
            </p>
        </page:param>
        <ww:if test="currentTask">
             <tr bgcolor="#ffffff"><td colspan="2">
             <ww:text name="'admin.selectworkflowscheme.blocked.by.user'">
                 <ww:param name="'value0'"><jira:formatuser userName="currentTask/user/name" type="'profileLink'" id="'user_profile'"/></ww:param>
             </ww:text>

             <ww:text name="'admin.selectworkflowscheme.goto.progressbar'">
                 <ww:param name="'value0'"><a href="<ww:property value="currentTask/progressURL"/>"></ww:param>
                 <ww:param name="'value1'"><ww:text name="'common.words.here'"/></ww:param>
                 <ww:param name="'value2'"></a></ww:param>
             </ww:text>
             </td></tr>
        </ww:if>
        <ww:else>
            <page:param name="width">100%</page:param>
            <page:param name="action">SelectProjectWorkflowSchemeStep2!default.jspa</page:param>
            <page:param name="submitId">associate_submit</page:param>
            <page:param name="submitName"><ww:text name="'admin.projects.schemes.associate'"/></page:param>

            <ui:select label="text('admin.common.words.scheme')" name="'schemeId'" value="schemeIds[0]" list="schemes" listKey="'string('id')'" listValue="'string('name')'" template="selectmap.jsp">
                <ui:param name="'headerrow'"><ww:text name="'admin.common.words.default'"/></ui:param>
                <ui:param name="'headervalue'" value="''" />
            </ui:select>
            <ui:component name="'projectId'" template="hidden.jsp"/>
        </ww:else>
        <page:param name="cancelURI"><%= request.getContextPath() %>/plugins/servlet/project-config/<ww:property value="/project/string('key')"/>/workflows</page:param>
    </page:applyDecorator>
</ww:else>

</body>
</html>
