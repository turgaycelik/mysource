<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%
// don't show ANYTHING to the user if they come here looking for trouble
if (com.atlassian.jira.util.JiraUtils.isSetup()) {
%>
<%--
Leave this as a raw HTML. Do not use response.getWriter() or response.getOutputStream() here as this will fail
on Orion. Let the application server figure out how it want to output this text.
--%>
JIRA has already been set up.
<%
} else {
%>
<html>
<head>
    <title><ww:text name="'setup.title'" /></title>
    <meta name="ajs-server-id" content="<ww:property value='/serverId'/>" />
    <ww:if test="/instantSetupAlreadyChosen == false">
        <meta name="ajs-instant-setup" content="not-chosen" />
    </ww:if>
    <ww:else>
        <meta name="ajs-instant-setup" content="<ww:property value="/instantSetup"/>" />
    </ww:else>
</head>

<body class="jira-setup-page-application-properties">

<page:applyDecorator id="jira-setupwizard" name="auiform">
    <page:param name="action">SetupApplicationProperties.jspa</page:param>
    <page:param name="useCustomButtons">true</page:param>

    <aui:component template="formHeading.jsp" theme="'aui'">
        <aui:param name="'text'"><ww:text name="'setup.step1'"/></aui:param>
    </aui:component>

    <img class="setup-banner" src="<%=request.getContextPath()%>/images/setup/setup-properties-banner.png"/>

    <p>
        <ww:text name="'setup.existingimport'">
            <ww:param name="'value0'"><strong></ww:param>
            <ww:param name="'value1'"></strong></ww:param>
            <ww:param name="'value2'"><a href="SetupImport!default.jspa"></ww:param>
            <ww:param name="'value3'"></a></ww:param>
        </ww:text>
    </p>

    <page:applyDecorator name="auifieldgroup">
        <aui:textfield label="text('setup.applicationtitle.label')" name="'title'" theme="'aui'">
            <aui:param name="'size'">long</aui:param>
        </aui:textfield>
        <page:param name="description">
            <ww:text name="'setup.applicationtitle.desc'"/>
        </page:param>
    </page:applyDecorator>

    <page:applyDecorator name="auifieldset">
        <page:param name="type">group</page:param>
        <page:param name="legend"><ww:text name="'setup.mode.label'" /></page:param>
        <page:applyDecorator name="auifieldgroup">
            <page:param name="type">radio</page:param>
            <page:param name="description">
                <ww:text name="'setup.mode.public.desc'" />
            </page:param>
            <aui:radio id="'mode-public'" label="text('setup.mode.public.title')" list="null" name="'mode'" theme="'aui'">
                <aui:param name="'customValue'">public</aui:param>
                <ww:if test="mode == 'public'">
                    <aui:param name="'checked'">true</aui:param>
                </ww:if>
            </aui:radio>
        </page:applyDecorator>

        <page:applyDecorator name="auifieldgroup">
            <page:param name="type">radio</page:param>
            <page:param name="description">
                <ww:text name="'setup.mode.private.desc'" />
            </page:param>
            <aui:radio id="'mode-private'" label="text('setup.mode.private.title')" list="null" name="'mode'" theme="'aui'">
                <aui:param name="'customValue'">private</aui:param>
                <ww:if test="mode == 'private'">
                    <aui:param name="'checked'">true</aui:param>
                </ww:if>
            </aui:radio>
        </page:applyDecorator>
    </page:applyDecorator>

    <page:applyDecorator name="auifieldgroup">
        <aui:textfield label="text('setup.baseurl.label')" name="'baseURL'" theme="'aui'">
            <aui:param name="'size'">long</aui:param>
        </aui:textfield>
        <page:param name="description">
            <ww:text name="'setup.baseurl.desc.line1'" />
            <br />
            <ww:text name="'setup.baseurl.desc.line2'" />
        </page:param>
    </page:applyDecorator>

    <aui:component name="'nextStep'" value="null" template="hidden.jsp" theme="'aui'" />

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
