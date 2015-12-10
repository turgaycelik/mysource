<% if (p.isPropertySet("localHelpAction")) {
    String action = p.getProperty("localHelpAction");
 %>
<ww:soy moduleKey="'jira.webresources:soy-templates'" template="'JIRA.Templates.Links.helpLink'">
    <ww:param name="'isLocal'" value="true"/>
    <ww:param name="'url'"><%=action%></ww:param>
    <ww:param name="'title'">this</ww:param>
    <ww:param name="'extraAttributes'">style="float:right;"</ww:param>
</ww:soy>
<%
}
if (p.isPropertySet("helpURL")) {
    String helpUrl = "'" + p.getProperty("helpURL") + "'";
    String helpURLFragment = "";
    if (p.isPropertySet("helpURLFragment"))
        helpURLFragment = p.getProperty("helpURLFragment"); %>
    <ww:component template="help.jsp" name="<%= helpUrl %>" >
        <ww:param name="'helpURLFragment'"><%= helpURLFragment %></ww:param>
    </ww:component>
<% } %>
