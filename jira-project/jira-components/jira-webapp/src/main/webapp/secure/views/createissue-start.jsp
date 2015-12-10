<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="ui" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib prefix="page" uri="sitemesh-page" %>
<html>
<head>
    <ww:if test="allowedProjects/size > 0">
        <title><ww:text name="'createissue.title'" /></title>
        <content tag="section">find_link</content>
    </ww:if>
    <ww:else>
        <title><ww:text name="'common.words.error'"/></title>
        <meta name="decorator" content="message" />
    </ww:else>
</head>
<body class="aui-page-focused aui-page-focused-large">
<ww:if test="allowedProjects/size > 0">
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
                <page:param name="action">CreateIssue.jspa</page:param>
                <page:param name="submitButtonName">Next</page:param>
                <page:param name="submitButtonText"><ww:text name="'common.forms.next'" /></page:param>
                <page:param name="cancelLinkURI"><ww:url value="'default.jsp'" atltoken="false"/></page:param>

                <aui:component name="'formToken'" template="hidden.jsp" theme="'aui'" />

                <ww:property value="/field('project')/createHtml(null, /, /, /issueObject, /displayParams)" escape="'false'" />

                <ww:property value="/field('issuetype')/createHtml(null, /, /, /issueObject, /displayParams)" escape="'false'" />

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
