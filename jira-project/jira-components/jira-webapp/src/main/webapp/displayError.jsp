<%@ taglib uri="webwork" prefix="ui" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ page import="com.opensymphony.util.TextUtils" %>
<%@ page import="com.atlassian.jira.security.JiraAuthenticationContext" %>
<%@ page import="com.atlassian.jira.util.I18nHelper" %>
<%@ page import="org.apache.commons.httpclient.HttpStatus" %>
<%@ page import="com.atlassian.jira.component.ComponentAccessor" %>
<%@ page import="com.atlassian.jira.web.util.ProductVersionDataBeanProvider" %>
<html>
<%
    // include the relevant contexts
    WebResourceManager webResourceManager = ComponentAccessor.getComponent(WebResourceManager.class);
    webResourceManager.requireResourcesForContext("atl.general");
    webResourceManager.requireResourcesForContext("jira.general");
    webResourceManager.requireResourcesForContext("atl.global");
    webResourceManager.requireResourcesForContext("jira.global");

    // figure out the status code
    Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
    String statusText = HttpStatus.getStatusText(statusCode);

    I18nHelper i18nBean = ComponentAccessor.getComponent(JiraAuthenticationContext.class).getI18nHelper();

%>
<head>
    <title><%=TextUtils.htmlEncode(statusText)%> (<%=statusCode%>)</title>
    <%@ include file="/includes/decorators/aui-layout/head-resources.jsp" %>
    <%= ComponentAccessor.getComponent(ProductVersionDataBeanProvider.class).get().getMetaTags() %>
</head>
<body id="jira" class="aui-layout aui-style-default page-type-message"  <%= ComponentAccessor.getComponent(ProductVersionDataBeanProvider.class).get().getBodyHtmlAttributes() %> >
    <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanel'">
        <ui:param name="'content'">
            <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanelContent'">
                <ui:param name="'content'">
                    <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pageHeader'">
                        <ui:param name="'content'">
                            <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pageHeaderMain'">
                                <ui:param name="'content'">
                                    <h1><%=TextUtils.htmlEncode(statusText)%> (<%=statusCode%>)</h1>
                                </ui:param>
                            </ui:soy>
                        </ui:param>
                    </ui:soy>
                    <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.message.warning'">
                        <ui:param name="'content'">
                            <p><%=i18nBean.getText("http.generic.error.message", TextUtils.htmlEncode("\"" + statusCode + " - " + statusText + "\""))%></p>
                            <p><a href="<%=request.getContextPath()%>/secure/MyJiraHome.jspa"><%=TextUtils.htmlEncode(i18nBean.getText("admin.keyboard.shortcut.goto.homr.desc"))%></a></p>
                        </ui:param>
                    </ui:soy>
                </ui:param>
            </ui:soy>
        </ui:param>
    </ui:soy>
</body>
</html>
