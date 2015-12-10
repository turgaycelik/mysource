<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="ui" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib prefix="page" uri="sitemesh-page" %>
<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.web.action.util.FieldsResourceIncluder" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<html>
<head>
    <ww:if test="/ableToCreateIssueInSelectedProject == true">
        <title><ww:text name="'createissue.title'"/></title>
        <content tag="section">find_link</content>
        <%
            final FieldsResourceIncluder fieldResourceIncluder = ComponentManager.getComponentInstanceOfType(FieldsResourceIncluder.class);
            fieldResourceIncluder.includeFieldResourcesForCurrentUser();

            final WebResourceManager wrm = ComponentManager.getInstance().getWebResourceManager();
            wrm.requireResourcesForContext("jira.create.issue");
        %>
    </ww:if>
    <ww:else>
        <title><ww:text name="'common.words.error'"/></title>
        <meta name="decorator" content="message" />
    </ww:else>
</head>
<body class="aui-page-focused aui-page-focused-large">
<ww:if test="/ableToCreateIssueInSelectedProject == true">
    <ui:soy moduleKey="'jira.webresources:soy-templates'" template="'JIRA.Templates.Headers.pageHeader'">
        <ui:param name="'mainContent'">
            <h1><ww:text name="'createissue.title'"/></h1>
        </ui:param>
    </ui:soy>
    <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanel'">
        <ui:param name="'content'">
            <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanelContent'">
                <ui:param name="'content'">

            <page:applyDecorator id="issue-create" name="auiform">
                <page:param name="action">CreateIssueDetails.jspa</page:param>
                <page:param name="submitButtonName">Create</page:param>
                <page:param name="submitButtonText"><ww:property value="submitButtonName" escape="false" /></page:param>
                <page:param name="cancelLinkURI"><ww:url value="'default.jsp'" atltoken="false"/></page:param>
                <page:param name="isMultipart">true</page:param>

                <aui:component name="'pid'" template="hidden.jsp" theme="'aui'" />
                <aui:component name="'issuetype'" template="hidden.jsp" theme="'aui'" />
                <aui:component name="'formToken'" template="hidden.jsp" theme="'aui'" />

                <page:applyDecorator name="auifieldgroup">
                    <aui:component id="'project-name'" label="text('issue.field.project')" name="'project/string('name')'" template="formFieldValue.jsp" theme="'aui'" />
                </page:applyDecorator>

                <page:applyDecorator name="auifieldgroup">
                    <aui:component id="'issue-type'" label="text('issue.field.issuetype')" name="'issueType'" template="formIssueType.jsp" theme="'aui'">
                        <aui:param name="'issueType'" value="/constantsManager/issueType(issuetype)" />
                    </aui:component>
                </page:applyDecorator>

                <ww:component template="issuefields.jsp" name="'createissue'">
                    <ww:param name="'displayParams'" value="/displayParams"/>
                    <ww:param name="'issue'" value="/issueObject"/>
                    <ww:param name="'tabs'" value="/fieldScreenRenderTabs"/>
                    <ww:param name="'errortabs'" value="/tabsWithErrors"/>
                    <ww:param name="'selectedtab'" value="/selectedTab"/>
                    <ww:param name="'ignorefields'" value="/ignoreFieldIds"/>
                    <ww:param name="'create'" value="'true'"/>
                </ww:component>

                <jsp:include page="/includes/panels/updateissue_comment.jsp" />

            </page:applyDecorator>

                </ui:param>
            </ui:soy>
        </ui:param>
    </ui:soy>
</ww:if>
<ww:else>
    <div class="form-body">
        <header>
            <h1><ww:text name="'common.words.error'"/></h1>
        </header>
        <%@ include file="/includes/createissue-notloggedin.jsp" %>
    </div>
</ww:else>
</body>
</html>
