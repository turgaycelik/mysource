<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflows"/>        
	<title><ww:text name="'admin.workflows.delete'"/></title>
</head>

<body>

    <page:applyDecorator id="delete-workflow" name="auiform">
        <page:param name="action">DeleteWorkflow.jspa</page:param>
        <page:param name="submitButtonText"><ww:if test="$workflowMode == 'draft'"><ww:text name="'common.words.discard'"/></ww:if><ww:else><ww:text name="'common.words.delete'"/></ww:else></page:param>
        <page:param name="submitButtonName">Delete</page:param>
        <page:param name="cancelLinkURI">ListWorkflows.jspa</page:param>

        <aui:component template="formHeading.jsp" theme="'aui'">
            <ww:if test="$workflowMode == 'draft'">
                <aui:param name="'text'"><ww:text name="'admin.workflows.delete.draft'"/></aui:param>
            </ww:if>
            <ww:else>
                <aui:param name="'text'"><ww:text name="'admin.workflows.delete'"/></aui:param>
            </ww:else>
        </aui:component>

        <aui:component template="formDescriptionBlock.jsp" theme="'aui'">
            <aui:param name="'messageHtml'">
                <p>
                    <ww:if test="$workflowMode == 'draft'">
                        <ww:text name="'admin.workflows.delete.draft.confirmation'">
                            <ww:param name="'value0'"><strong></ww:param>
                            <ww:param name="'value1'"><ww:property value="/workflowName"/></ww:param>
                            <ww:param name="'value2'"></strong></ww:param>
                        </ww:text>
                    </ww:if>
                    <ww:else>
                        <ww:text name="'admin.workflows.delete.confirmation'">
                            <ww:param name="'value0'"><strong></ww:param>
                            <ww:param name="'value1'"><ww:property value="/workflowName"/></ww:param>
                            <ww:param name="'value2'"></strong></ww:param>
                        </ww:text>
                    </ww:else>
                </p>
            </aui:param>
        </aui:component>

        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">warning</aui:param>
            <aui:param name="'messageHtml'">
                <p>
                    <ww:if test="/systemAdministrator == true">
                        <ww:text name="'admin.workflows.delete.warning'">
                            <ww:param name="'value0'"><a href="<%=request.getContextPath()%>/secure/admin/XmlBackup!default.jspa"></ww:param>
                            <ww:param name="'value1'"></a></ww:param>
                            <ww:param name="'value2'"><a href="<ww:url page="/secure/admin/workflows/ViewWorkflowXml.jspa"><ww:param name="'workflowMode'" value="$workflowMode" /><ww:param name="'workflowName'" value="/workflowName" /></ww:url>"></ww:param>
                            <ww:param name="'value3'"></a></ww:param>
                        </ww:text>
                    </ww:if>
                    <ww:else>
                        <ww:text name="'admin.workflows.delete.warning.admin'">
                            <ww:param name="'value0'"> </ww:param>
                            <ww:param name="'value1'"> </ww:param>
                            <ww:param name="'value2'"><a href="<ww:url page="/secure/admin/workflows/ViewWorkflowXml.jspa"><ww:param name="'workflowMode'" value="$workflowMode" /><ww:param name="'workflowName'" value="/workflowName" /></ww:url>"></ww:param>
                            <ww:param name="'value3'"></a></ww:param>
                        </ww:text>
                    </ww:else>
                </p>
            </aui:param>
        </aui:component>

        <aui:component name="'workflowName'" template="hidden.jsp" theme="aui" />
        <aui:component name="'workflowMode'" value="$workflowMode" template="hidden.jsp" theme="aui" />
        <aui:component name="'project'" template="hidden.jsp" theme="aui" />
        <aui:component name="'issueType'" template="hidden.jsp" theme="aui" />
        <aui:component name="'confirmedDelete'" value="'true'" template="hidden.jsp" theme="aui"  />
    </page:applyDecorator>

</body>
</html>
