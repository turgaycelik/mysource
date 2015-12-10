<%@ page import="com.atlassian.jira.component.ComponentAccessor" %>
<%@ page import="com.atlassian.jira.plugin.navigation.HeaderFooterRendering" %>

<!--[if IE]><![endif]--><%-- Leave this here - it stops IE blocking resource downloads - see http://www.phpied.com/conditional-comments-block-downloads/ --%>
<script type="text/javascript">var contextPath = '<%=request.getContextPath()%>';</script>
<%
    //
    // IDEA gives you a warning below because it cant resolve JspWriter.  I don't know why but its harmless
    //
    HeaderFooterRendering headerAndFooter = ComponentAccessor.getComponent(HeaderFooterRendering.class);

    headerAndFooter.includeHeadResources(out);
%>
<script type="text/javascript" src="<%=headerAndFooter.getKeyboardShortCutScript(request) %>"></script>
<%
    headerAndFooter.includeWebPanels(out, "atl.header.after.scripts");
%>
