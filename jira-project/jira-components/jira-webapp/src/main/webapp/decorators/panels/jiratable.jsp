<%@ page import="com.opensymphony.util.TextUtils" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-decorator" prefix="decorator" %>

<%--
PARAMETERS: (all are optional)
title			- a title for this form (HTML)
width		    - the width of the border table (HTML)
helpURL		    - the URL of a help link related to this panel
--%>
<decorator:usePage id="p" />
<div<% if (p.isPropertySet("id")) { %> id="<%=p.getProperty("id")%>"<% } %><% if (p.isPropertySet("cssClass")) { %> class="<%=p.getProperty("cssClass")%>"<% } %>>
    <% if (p.isPropertySet("title") && TextUtils.stringSet(p.getProperty("title"))) { %>
        <%@ include file="/includes/decorators/helplink.jsp" %>
        <h3 class="formtitle"><decorator:getProperty property="title" /></h3>
        <% if (p.isPropertySet("description")) { %>
            <p><decorator:getProperty property="description" /></p>
        <% } %>
    <% } %>
    <table<% if (p.isPropertySet("id")){ %> id="table-<%=p.getProperty("id")%>" <% } %> class="aui aui-table-rowhover">
        <decorator:body />
    </table>
</div>