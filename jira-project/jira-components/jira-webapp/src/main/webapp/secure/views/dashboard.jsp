<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.web.util.ExternalLinkUtilImpl" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%
    // Plugins 2.5 allows us to perform context-based resource inclusion. This defines the context "atl.dashboard"
    final WebResourceManager wrm = ComponentManager.getInstance().getWebResourceManager();
    wrm.requireResourcesForContext("atl.dashboard");
    wrm.requireResourcesForContext("jira.dashboard");
%>
<html>
<head>
    <title><ww:property value="/dashboardTitle"/></title>
    <content tag="section">home_link</content>
    <script type="text/javascript">
        AJS.$(function() {
            AJS.warnAboutFirebug(AJS.params.firebugWarning);
        });
    </script>
</head>
<body class="page-type-dashboard">
    <ww:if test="/warningMessage != null && /warningMessage/length != 0">
        <aui:component id="dashmsg" template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">warning</aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:property value="/warningMessage" escape="false"/></p>
            </aui:param>
        </aui:component>
    </ww:if>
    <ww:if test="/installationMessage != null && /installationMessage/length != 0">
        <aui:component id="dashmsg" template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">success</aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:property value="/installationMessage" escape="false"/></p>
            </aui:param>
        </aui:component>
    </ww:if>
    <ww:render value="/dashboardRenderable"/>
    <fieldset class="hidden parameters">
        <input type="hidden" id="firebugWarning" value="<ww:text name="'firebug.performance.warning'">
        <ww:param name="'value0'"><a href='<%=ExternalLinkUtilImpl.getInstance().getProperty("external.link.jira.firebug.warning")%>'></ww:param>
        <ww:param name="'value1'"></a></ww:param>
        </ww:text>">
    </fieldset>
</body>
</html>
