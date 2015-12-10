<%@ page import="com.atlassian.jira.plugin.navigation.HeaderFooterRendering" %>
<%@ page import="static com.atlassian.jira.component.ComponentAccessor.*" %>
<section class="footer-body">
<%
    //
    // IDEA gives you a warning below because it cant resolve JspWriter.  I don't know why but its harmless
    //
    getComponent(HeaderFooterRendering.class).includeFooters(out, request);
%>
    <div id="footer-logo"><a href="http://www.atlassian.com/">Atlassian</a></div>
</section>
<jsp:include page="/includes/decorators/global-translations.jsp" />