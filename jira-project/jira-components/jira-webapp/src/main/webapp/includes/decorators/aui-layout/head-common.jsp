<%--
All changes in this jsp must be mirrored in head-common-nodecorator-pre/post.jsp
--%>
<%@ page import="static com.atlassian.jira.component.ComponentAccessor.*" %>
<%@ taglib uri="sitemesh-decorator" prefix="decorator" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<%@ taglib uri="webwork" prefix="ww" %>
<decorator:usePage id="originalPage"/>
<%
    //
    // IDEA gives you a warning below because it cant resolve JspWriter.  I don't know why but its harmless
    //
    HeaderFooterRendering headerFooterRendering = getComponent(HeaderFooterRendering.class);
%>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="<%= headerFooterRendering.getXUACompatible(originalPage) %>"/>
<title><%= headerFooterRendering.getPageTitle(originalPage) %></title>
<%

    // include version meta information
    headerFooterRendering.includeVersionMetaTags(out);


    // writes the <meta> tags into the page head
    headerFooterRendering.includeMetadata(out);

    // include web panels
    headerFooterRendering.includeWebPanels(out, "atl.header");
%>
<%@ include file="/includes/decorators/xsrftoken.jsp" %>

<link rel="shortcut icon" href="<%= headerFooterRendering.getRelativeResourcePrefix()%>/favicon.ico">
<link rel="search" type="application/opensearchdescription+xml" href="<%= request.getContextPath()%>/osd.jsp" title="<%= headerFooterRendering.getPageTitle(originalPage) %>"/>
