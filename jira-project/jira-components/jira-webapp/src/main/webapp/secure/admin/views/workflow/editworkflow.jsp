<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/workflows_section"/>
    <meta name="admin.active.tab" content="workflows"/>        
	<title>
        <ww:text name="'admin.workflows.edit'"/>
    </title>
</head>
<body>
    <page:applyDecorator id="edit-workflow" name="auiform">
        <page:param name="action">EditWorkflow.jspa</page:param>
        <page:param name="submitButtonText"><ww:text name="'common.words.update'"/></page:param>
        <page:param name="submitButtonName">Update</page:param>
        <page:param name="cancelLinkURI">ListWorkflows.jspa</page:param>

        <%--<page:param name="submitId">edit_submit</page:param>--%>
        <aui:component template="formHeading.jsp" theme="'aui'">
            <aui:param name="'text'"><ww:text name="'admin.workflows.edit'"/></aui:param>
        </aui:component>

        <ww:if test="/workflow/draftWorkflow == false">
            <page:applyDecorator name="auifieldgroup">
                <aui:textfield label="text('common.words.name')" name="'newWorkflowName'" size="'50'" maxlength="255" mandatory="'true'" theme="'aui'"/>
            </page:applyDecorator>
        </ww:if>
        <page:applyDecorator name="auifieldgroup">
            <aui:textfield label="text('common.words.description')" name="'description'" size="'50'" maxlength="255" mandatory="'false'" theme="'aui'"/>
        </page:applyDecorator>

        <ww:property value="'aui'">
            <%@ include file="basicworkflowhiddenparameters.jsp" %>
        </ww:property>
    </page:applyDecorator>
</body>
</html>