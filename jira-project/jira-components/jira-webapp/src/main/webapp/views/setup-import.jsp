<%@ page import="com.atlassian.jira.web.util.HelpUtil" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<% HelpUtil helpUtil = new HelpUtil();
   HelpUtil.HelpPath homeDirectoryHelpPath = helpUtil.getHelpPath("setup.import.homedirectory");


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
    <title><ww:text name="'setup.title'"/></title>
</head>

<body>

<page:applyDecorator id="jira-setupwizard" name="auiform">
    <page:param name="action">SetupImport.jspa</page:param>
    <page:param name="useCustomButtons">true</page:param>

    <ww:if test="/hasSpecificErrors == true && /specificErrors/errorMessages/empty == false">
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">error</aui:param>
            <aui:param name="'messageHtml'">
                <ww:iterator value="specificErrors/errorMessages">
                    <p><ww:property escape="false"/></p>
                </ww:iterator>
            </aui:param>
        </aui:component>
    </ww:if>

    <aui:component template="formHeading.jsp" theme="'aui'">
        <aui:param name="'text'"><ww:text name="'setup.import.title'"/></aui:param>
    </aui:component>

    <img class="setup-banner" src="<%=request.getContextPath()%>/images/setup/setup-import-banner.png"/>

    <p>
        <ww:text name="'setup.import.desc.line2'">
            <ww:param name="'value0'"><a href="SetupApplicationProperties!default.jspa"></ww:param>
            <ww:param name="'value1'"></a></ww:param>
        </ww:text>
    </p>

    <page:applyDecorator name="auifieldgroup">
        <aui:textfield label="text('setup.filename.label')" name="'filename'" theme="'aui'" size="'long'"/>
        <page:param name="description">
            <ww:text name="'setup.filename.desc'"/>
        </page:param>
    </page:applyDecorator>

    <page:applyDecorator name="auifieldgroup">
        <aui:component label="text('setup.indexpath.label')" template="formFieldValue.jsp" theme="'aui'">
            <aui:param name="'texthtml'"><ww:property value="/defaultIndexPath" /></aui:param>
        </aui:component>
        <page:param name="description">
            <ww:text name="'setup.import.index.path.msg'">
                <aui:param name="'value0'"><a href="<%=homeDirectoryHelpPath.getUrl()%>" target="_blank"></aui:param>
                <aui:param name="'value1'"></a></aui:param>
            </ww:text>
        </page:param>
    </page:applyDecorator>

    <page:applyDecorator name="auifieldgroup">
        <aui:textarea label="text('admin.import.license.if.required')" name="'license'" rows="12" theme="'aui'" size="'long'"/>
        <page:param name="description">
            <ww:text name="'admin.import.enter.a.license'"/>
            <br />
            <ww:text name="'setup.license.description.generate.eval'">
                <ww:param name="'value0'"><a id="fetchLicense" data-url="SetupImport!fetchLicense.jspa" href="<ww:property value="/requestLicenseURL"/>"></ww:param>
                <ww:param name="'value1'"></a></ww:param>
            </ww:text>
            <br />
            <ww:text name="'setup.license.description.retrieve'">
                <ww:param name="'value0'"><a target="_blank" href="<ww:component name="'external.link.jira.licenses'" template="externallink.jsp"/>"></ww:param>
                <ww:param name="'value1'"></a></ww:param>
            </ww:text>
        </page:param>
    </page:applyDecorator>

    <ww:if test="/outgoingMailModifiable == true">
        <page:applyDecorator name="auifieldset">
            <page:param name="type">group</page:param>
            <page:param name="legend"><ww:text name="'admin.import.outgoing.mail.setting.label'"/></page:param>
            <aui:radio id="'outgoing-mail'" label="text('admin.import.outgoing.mail.setting.label')" list="/outgoingMailOptions"
                       listKey="'key'" listValue="'value'" name="'outgoingEmail'" theme="'aui'" />
        </page:applyDecorator>
    </ww:if>
    <ww:else>
        <page:applyDecorator name="auifieldgroup">
            <ww:component template="formFieldValue.jsp" label="text('admin.import.outgoing.mail.setting.label')" theme="'aui'">
                <aui:param name="'texthtml'">
                    <ww:text name="'admin.import.outgoing.mail.setting.set.on.jira.start'">
                        <ww:param name="'value0'"><code></ww:param>
                        <ww:param name="'value1'"></code></ww:param>
                    </ww:text>
                </aui:param>
            </ww:component>
        </page:applyDecorator>
    </ww:else>

    <aui:component name="'useDefaultPaths'" value="'false'" template="hidden.jsp" theme="'aui'" />
    <aui:component name="'downgradeAnyway'" value="/downgradeAnyway" template="hidden.jsp" theme="'aui'" />

    <page:applyDecorator name="auifieldgroup">
        <page:param name="type">buttons-container</page:param>
        <page:applyDecorator name="auifieldgroup">
            <page:param name="type">buttons</page:param>
            <aui:component template="formSubmit.jsp" theme="'aui'">
                <aui:param name="'submitButtonName'">import</aui:param>
                <aui:param name="'submitButtonText'"><ww:text name="'common.forms.import'" /></aui:param>
                <aui:param name="'submitButtonCssClass'">aui-button-primary</aui:param>
            </aui:component>
        </page:applyDecorator>
    </page:applyDecorator>
</page:applyDecorator>

</body>
</html>
<% } %>
