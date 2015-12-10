<%@ taglib uri="webwork" prefix="ww" %>
<tr><td bgcolor=#ffffff>
    Indexing is currently not configured so no issue searching can be performed.<br>&nbsp;<br>
    <ww:if test="/hasPermission('admin') == true">
        To configure indexing please click <a href="<%= request.getContextPath() %>/secure/admin/jira/IndexAdmin.jspa">here</a>
    </ww:if>
    <ww:else>
        Please contact your administrator to get indexing configured
    </ww:else>
</td></tr>
