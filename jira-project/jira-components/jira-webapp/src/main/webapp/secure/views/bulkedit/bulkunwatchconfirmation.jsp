<%@ page import="com.atlassian.jira.util.BrowserUtils"%>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'bulkedit.title'"/></title>
</head>
<body>
    <!-- Step 4 - Bulk Operation: Confirmation for Unwatch -->
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
                                    <h2><ww:text name="'bulkedit.step4'"/>: <ww:text name="'bulkedit.step4.title'"/></h2>
                                </ui:param>
                            </ui:soy>
                        </ui:param>
                    </ui:soy>

                    <p>
                        <ww:text name="'bulk.unwatch.confirmation.line1'">
                            <ww:param name="'value0'"><strong><ww:property value="/bulkEditBean/selectedIssues/size"/></strong></ww:param>
                        </ww:text>
                    </p>

                    <page:applyDecorator id="bulkunwatch" name="auiform">
                        <page:param name="action">BulkUnwatchPerform.jspa</page:param>
                        <page:param name="useCustomButtons">true</page:param>

                        <p>
                            <input class="aui-button" id="confirm" type="submit" name="<ww:text name="'common.forms.confirm'"/>" value="<ww:text name="'common.forms.confirm'"/>"
                                accessKey="<ww:text name="'common.forms.submit.accesskey'"/>"
                                title="<ww:text name="'common.forms.submit.tooltip'">
                                <ww:param name="'value0'"><ww:text name="'common.forms.submit.accesskey'"/></ww:param>
                                <ww:param name="'value1'"><%=BrowserUtils.getModifierKey()%></ww:param>
                                </ww:text>"
                            />
                            <a class="aui-button aui-button-link" id="cancel" href="<%= request.getContextPath() %>/secure/BulkCancelWizard.jspa"><ww:text name="'common.forms.cancel'"/></a>
                        </p>

                        <ww:component name="'atl_token'" value="/xsrfToken" template="hidden.jsp"/>
                    </page:applyDecorator>
                </ui:param>
            </ui:soy>
        </ui:param>
    </ui:soy>
</body>
</html>
