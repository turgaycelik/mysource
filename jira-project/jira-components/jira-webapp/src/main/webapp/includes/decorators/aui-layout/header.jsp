<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="sitemesh-decorator" prefix="decorator" %>
<decorator:usePage id="p"/>
<%
    //
    // IDEA gives you a warning below because it cant resolve JspWriter.  I don't know why but its harmless
    //
    ComponentAccessor.getComponent(HeaderFooterRendering.class).includeTopNavigation(out, request, p);
%>