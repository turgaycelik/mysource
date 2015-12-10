<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<%
    // don't show ANYTHING to the user if they come here looking for trouble
    if (com.atlassian.jira.util.JiraUtils.isSetup())
    {
%>
<%--
Leave this as a raw HTML. Do not use response.getWriter() or response.getOutputStream() here as this will fail
on Orion. Let the application server figure out how it want to output this text.
--%>
JIRA has already been set up.
<%
}
else
{
%>
<html>
<head>
    <title><ww:text name="'setup.title'"/></title>
</head>

<body class="jira-setup-choice-page">
<page:applyDecorator id="jira-setupwizard" name="auiform">
    <page:param name="action">SetupWelcome.jspa</page:param>
    <page:param name="hideToken">true</page:param>
    <page:param name="useCustomButtons">true</page:param>

    <aui:component template="formHeading.jsp" theme="'aui'">
        <aui:param name="'text'"><ww:text name="'setup.welcome.page.title'"/></aui:param>
    </aui:component>

    <p class="jira-setup-welcome-description"><ww:text name="'setup.welcome.page.description'"/></p>

    <div class="jira-setup-choice-box" data-choice-value="instant">
        <div class="jira-setup-choice-wrapper">
            <h2 class="jira-setup-choice-box-header"><ww:text name="'setup.welcome.page.instant.header'"/></h2>
            <p class="jira-setup-choice-box-description"><ww:text name="'setup.welcome.page.instant.description'"/></p>
        </div>
    </div>

    <div class="jira-setup-choice-box" data-choice-value="classic">
        <div class="jira-setup-choice-wrapper">
            <h2 class="jira-setup-choice-box-header"><ww:text name="'setup.welcome.page.classic.header'"/></h2>
            <p class="jira-setup-choice-box-description"><ww:text name="'setup.welcome.page.classic.description'"/></p>
        </div>
    </div>

    <aui:component name="'setupOption'" value="'classic'" id="'choice-value'" template="hidden.jsp" theme="'aui'" />

    <page:applyDecorator name="auifieldgroup">
        <page:param name="type">buttons-container</page:param>
        <page:applyDecorator name="auifieldgroup">
            <page:param name="type">buttons</page:param>
            <aui:component theme="'aui'" template="formSubmit.jsp">
                <aui:param name="'id'">jira-setupwizard-submit</aui:param>
                <aui:param name="'submitButtonName'">next</aui:param>
                <aui:param name="'submitButtonText'"><ww:text name="'common.words.next'"/></aui:param>
                <aui:param name="'submitButtonCssClass'">aui-button-primary</aui:param>
            </aui:component>
        </page:applyDecorator>
    </page:applyDecorator>

</page:applyDecorator>
</body>
</html>
<% } %>
