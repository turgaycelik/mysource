<%--
All changes in this jsp must be mirrored in head-common.jsp
--%>
<%@ page import="com.atlassian.jira.plugin.navigation.HeaderFooterRendering" %>
<%@ page import="static com.atlassian.jira.component.ComponentAccessor.*" %>
<%@ page import="com.atlassian.jira.web.pagebuilder.JspDecoratorUtils" %>
<%@ taglib uri="sitemesh-decorator" prefix="decorator" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%
    //
    // IDEA gives you a warning below because it cant resolve JspWriter.  I don't know why but its harmless
    //
    HeaderFooterRendering headerFooterRendering = getComponent(HeaderFooterRendering.class);
%>
<title><%= headerFooterRendering.getPageTitle(JspDecoratorUtils.getHead()) %></title>
<link rel="search" type="application/opensearchdescription+xml" href="<%= request.getContextPath()%>/osd.jsp" title="<%= headerFooterRendering.getPageTitle(JspDecoratorUtils.getHead()) %>"/>

