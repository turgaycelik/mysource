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

<body>
<page:applyDecorator id="jira-setupwizard" name="auiform">
    <page:param name="action">SetupLicense.jspa</page:param>
    <page:param name="useCustomButtons">true</page:param>

    <aui:component template="formHeading.jsp" theme="'aui'">
        <aui:param name="'text'"><ww:text name="'setup.cross.selling.license.page.header'"/></aui:param>
    </aui:component>

    <img class="setup-banner" src="<%=request.getContextPath()%>/images/setup/setup-license-banner.png"/>

    <div>
        <p>
            <ww:text name="'setup.cross.selling.license.page.description'">
                <ww:param name="'value0'"><a href="http://<ww:text name="'setupLicense.mac.url'"/>" target="_blank"><ww:text name="'setupLicense.mac.url'"/></a></ww:param>
            </ww:text>
        </p>
    </div>

    <div id="license-radio-options" class="inline-radio-options">
        <page:applyDecorator name="auifieldset">
            <aui:select id="'licenseSetupSelector'" label="text('setupLicense.options')" name="'licenseSetupSelector'" theme="'aui'" template="radiomap.jsp" list="/licenseSetupOptions" listKey="'key'" listValue="'value'" />
        </page:applyDecorator>
    </div>

    <div id="no-connection-warning" class="aui-message warning shadowed hidden">
        <p class="title">
            <span class="aui-icon icon-warning"></span>
            <strong><ww:text name="'setupLicense.error.noconnection.title'"/></strong>
        </p>
        <p>
            <ww:text name="'setupLicense.error.noconnection.desc'">
                <ww:param name="'value0'"><a href='https://id.atlassian.com' target='_blank'></ww:param>
                <ww:param name="'value1'"></a></ww:param>
            </ww:text>
        </p>
    </div>

    <div id="license-input-container" class="license-input-container hidden">
        <%-- content gets added via soy template call in setup-license.js --%>
    </div>

</page:applyDecorator>

<div id="hidden-license-setup" class="hidden">
    <form method="post" id="setupLicenseForm" action="SetupLicense.jspa">
        <input name="setupLicenseKey" id="setupLicenseKey" />
        <input name="setupFirstName" id="setupFirstName" />
        <input name="setupLastName" id="setupLastName" />
        <input name="setupEmail" id="setupEmail" />
        <input name="setupPluginLicenseKey" id="setupPluginLicenseKey" />

        <aui:component theme="'aui'" template="formSubmit.jsp">
            <aui:param name="'id'">jira-setupwizard-submit</aui:param>
            <aui:param name="'submitButtonName'">next</aui:param>
            <aui:param name="'submitButtonCssClass'">aui-button-primary</aui:param>
        </aui:component>
    </form>
</div>
</body>
</html>
<% } %>
