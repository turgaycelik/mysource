<%@ page import="com.atlassian.jira.component.ComponentAccessor" %>
<%@ page import="com.atlassian.jira.config.properties.APKeys" %>
<%@ page import="com.atlassian.jira.config.properties.ApplicationProperties" %>
<%@ page import="com.atlassian.jira.config.properties.LookAndFeelBean" %>
<%@ page import="com.atlassian.jira.web.util.ProductVersionDataBeanProvider" %>
<%@ page import="com.atlassian.plugin.webresource.UrlMode" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ page import="com.opensymphony.util.TextUtils" %>
<%@ taglib uri="sitemesh-decorator" prefix="decorator" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%
    ApplicationProperties ap = ComponentAccessor.getComponent(ApplicationProperties.class);
    final LookAndFeelBean lAndF = LookAndFeelBean.getInstance(ap);
    String applicationID = lAndF.getApplicationID();
%>
<!DOCTYPE html>
<html lang="<%= ComponentAccessor.getJiraAuthenticationContext().getI18nHelper().getLocale().getLanguage() %>">
<head>
    <title><%= TextUtils.htmlEncode(ap.getDefaultBackedString(APKeys.JIRA_TITLE)) %> - <decorator:title default="New Generation Issue Tracking" /></title>
    <meta http-equiv="Content-Type" content="<%= ap.getContentType() %>" />
    <link rel="shortcut icon" href="<%=request.getContextPath()%>/favicon.ico" />
    <decorator:head/>
<%
    WebResourceManager webResourceManager = ComponentAccessor.getComponent(WebResourceManager.class);
    webResourceManager.requireResource("jira.webresources:jira-setup-ondemand");
    webResourceManager.includeResources(out, UrlMode.RELATIVE);
%>
    <%= ComponentAccessor.getComponent(ProductVersionDataBeanProvider.class).get().getMetaTags() %>
</head>
<body id="jira" class="aui-layout aui-theme-default jira-style-setup <decorator:getProperty property="body.class" />" <%= ComponentAccessor.getComponent(ProductVersionDataBeanProvider.class).get().getBodyHtmlAttributes() %>>
<div id="page">
    <header id="header" role="banner">
        <div class="global">
            <div class="primary"></div>
            <div class="secondary">
                <ul>
                    <li>
                       <a id="log_out" href="<ww:url value="'/secure/Logout.jspa'" />"><ww:text name="'common.concepts.logout'"/></a>
                    </li>
                </ul>
            </div>
        </div>
        <div class="local"></div>
    </header>
    <section id="content" role="main">
        <div id="ondemand-setup">
            <div class="setup-header">
                <h1><ww:text name="'setup.ondemand.title'" /></h1>
            </div>
            <div class="setup-panel">
                <div class="setup-active-area">
                    <%@ include file="/includes/decorators/unsupported-browsers.jsp" %>
                    <decorator:body />
                </div>
            </div>
        </div>
    </section>
</div>
</body>
</html>
