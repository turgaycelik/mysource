<%@ page import="com.atlassian.jira.component.ComponentAccessor" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="sitemesh-decorator" prefix="decorator" %>
<html lang="<%= ComponentAccessor.getJiraAuthenticationContext().getI18nHelper().getLocale().getLanguage() %>">
<head>
    <style>
        .module + .module {
            border-top: none;
            padding-top: 0;
            margin-top: 50px;
        }
    </style>
</head>
<body>
<ui:soy moduleKey="'jira.webresources:soy-templates'" template="'JIRA.Templates.Headers.pageHeader'">
    <ui:param name="'mainContent'">
        <h1>
            <ww:if test="/bulkEditBean/singleMode == false">
                <ww:text name="'bulkedit.title'"/>
            </ww:if>
            <ww:else>
               <ww:text name="'moveissue.title'"/>: <ww:property value="/bulkEditBean/singleIssueKey" />
            </ww:else>
        </h1>
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
                <ui:soy moduleKey="'jira.webresources:soy-templates'" template="'JIRA.Templates.Headers.pageHeader'">
                    <ui:param name="'mainContent'">
                        <h2><decorator:getProperty property="title" /></h2>
                    </ui:param>
                    <ui:param name="'helpContent'">
                        <ww:if test="/bulkEditBean/singleMode == false">
                            <ui:component template="help.jsp" name="'bulkoperations'" >
                                <ww:param name="'helpURLFragment'">#Bulk+Move</ww:param>
                            </ui:component>
                        </ww:if>
                    </ui:param>
                </ui:soy>

                <decorator:getProperty property="instructions" />


                <page:applyDecorator id="bulkedit" name="auiform">
                    <page:param name="action"><decorator:getProperty property="action" /></page:param>
                    <page:param name="useCustomButtons">true</page:param>

                    <%@include file="/secure/views/bulkedit/bulkchooseaction_submit_buttons.jsp"%>

                    <decorator:body />

                    <%@include file="/secure/views/bulkedit/bulkchooseaction_submit_buttons.jsp"%>
                    <!-- Hidden field placed here so as not affect the buttons -->
                    <ww:if test="/canDisableMailNotifications() == false">
                        <ui:component name="'sendBulkNotification'" template="hidden.jsp" theme="'single'" value="'true'" />
                    </ww:if>

                </page:applyDecorator>
            </ui:param>
        </ui:soy>
    </ui:param>
</ui:soy>
</body>
</html>