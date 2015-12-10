<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<%  // don't show ANYTHING to the user if they come here looking for trouble
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
    <meta name="ajs-server-id" content="<ww:property value='/serverId'/>" />
</head>

<body class="jira-setup-choice-page">
<page:applyDecorator id="jira-setupwizard" name="auiform">
    <page:param name="action">SetupProductBundle.jspa</page:param>
    <page:param name="useCustomButtons">true</page:param>

    <aui:component template="formHeading.jsp" theme="'aui'">
        <aui:param name="'text'"><ww:text name="'setup.bundle.title'" /></aui:param>
    </aui:component>

    <p class="jira-setup-product-bundle-description"><ww:text name="'setup.product.bundle.description'"/></p>

    <div class="jira-setup-choice-box" data-choice-value="TRACKING">
        <div class="jira-setup-choice-left">
            <h2 class="jira-setup-choice-box-header"><ww:text name="'setup.bundle.tracking.label.new'"/></h2>
            <p class="jira-setup-choice-box-description"><ww:text name="'setup.bundle.tracking.content.new'"/></p>
        </div>
        <div class="jira-setup-choice-right">
            <img src="<%=request.getContextPath()%>/images/setup/logos/logo-jira.png" width="160" height="50">
        </div>
    </div>

    <div class="jira-setup-choice-box" data-choice-value="DEVELOPMENT">
        <div class="jira-setup-choice-left">
            <h2 class="jira-setup-choice-box-header jira-setup-has-asterisk"><ww:text name="'setup.bundle.development.label.new'"/></h2>
            <p class="jira-setup-choice-box-description"><ww:text name="'setup.bundle.development.content.new'"/></p>
        </div>
        <div class="jira-setup-choice-right">
            <img src="<%=request.getContextPath()%>/images/setup/logos/logo-jira-jira-agile.png" width="160" height="82">
        </div>
    </div>

    <div class="jira-setup-choice-box" data-choice-value="SERVICEDESK">
        <div class="jira-setup-choice-left">
            <h2 class="jira-setup-choice-box-header jira-setup-has-asterisk"><ww:text name="'setup.bundle.servicedesk.label.new'"/></h2>
            <p class="jira-setup-choice-box-description"><ww:text name="'setup.bundle.servicedesk.content.new'"/></p>
        </div>
        <div class="jira-setup-choice-right">
            <img src="<%=request.getContextPath()%>/images/setup/logos/logo-jira-jira-service-desk.png" width="160" height="82">
        </div>
    </div>

    <aui:component name="'selectedBundle'" id="'choice-value'" template="hidden.jsp" theme="'aui'" />

    <p class="jira-setup-product-bundle-annotation jira-setup-has-asterisk-before"><ww:text name="'setup.product.bundle.annotation'"/></p>

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
            <div class="hidden throbber-message">
                <span class="aui-icon aui-icon-wait"></span>
                <ww:text name="'setup.spinner.message'" />
            </div>
        </page:applyDecorator>
    </page:applyDecorator>
</page:applyDecorator>



</body>
</html>
<% } %>
