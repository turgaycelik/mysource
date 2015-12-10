<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <title><ww:text name="'setup.title'"/></title>
    <meta name="ajs-server-id" content="<ww:property value='/serverId'/>" />
</head>

<body>

<page:applyDecorator id="jira-setupwizard" name="auiform">
    <page:param name="action">SetupProductBundleReport.jspa</page:param>
    <page:param name="useCustomButtons">true</page:param>

    <aui:component template="formHeading.jsp" theme="'aui'">
        <aui:param name="'text'"><ww:text name="'setup.cross.selling.report.page.header'" /></aui:param>
    </aui:component>

    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">info</aui:param>
        <aui:param name="'titleText'">
            <ww:property value="text('setup.cross.selling.report.message.header')"/>
        </aui:param>
        <aui:param name="'messageHtml'">
            <p>
                <ww:text name="'setup.cross.selling.report.message.body'">
                    <ww:param name="'value0'"><a href="<ww:property value="upmUrlForPlugin"/>" target="_blank"><ww:text name="'setup.cross.selling.report.upm'"/></a></ww:param>
                    <ww:param name="'value1'"><a href="http://<ww:text name="'setupLicense.mac.url'"/>" target="_blank"><ww:text name="'setupLicense.mac.url'"/></a></ww:param>
                </ww:text>
            </p>
        </aui:param>
    </aui:component>

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
