<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'bulkworkflowtransition.title'"/></title>
</head>
<body>
    <!-- Step 3 - Bulk Operation: Operation Details -->
    <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pageHeader'">
        <ui:param name="'content'">
            <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pageHeaderMain'">
                <ui:param name="'content'">
                    <h1><ww:text name="'bulkedit.title'"/></h1>
                </ui:param>
            </ui:soy>
        </ui:param>
    </ui:soy>
    <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanel'">
        <ui:param name="'id'" value="'stepped-process'" />
        <ui:param name="'content'">
            <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanelNav'">
                <ui:param name="'content'">
                    <jsp:include page="/secure/views/bulkedit/bulkedit_leftpane.jsp" flush="false" />
                </ui:param>
            </ui:soy>
            <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanelContent'">
                <ui:param name="'content'">
                    <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pageHeader'">
                        <ui:param name="'content'">
                            <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pageHeaderMain'">
                                <ui:param name="'content'">
                                    <h2><ww:text name="'bulkedit.step3'"/>: <ww:text name="'bulkedit.step3.title'"/></h2>
                                </ui:param>
                            </ui:soy>
                        </ui:param>
                    </ui:soy>
                    <p><ww:text name="'bulkworkflowtransition.select.action'" /></p>


                    <page:applyDecorator id="bulk-transition-details" name="auiform">
                        <page:param name="action">BulkWorkflowTransitionDetailsValidation.jspa</page:param>
                        <page:param name="useCustomButtons">true</page:param>

                        <%@include file="/secure/views/bulkedit/includes/bulkworkflowtransition_transitionmapping.jsp"%>
                        <%@include file="bulkchooseaction_submit_buttons.jsp"%>
                    </page:applyDecorator>
                </ui:param>
            </ui:soy>
        </ui:param>
    </ui:soy>
</body>
</html>
