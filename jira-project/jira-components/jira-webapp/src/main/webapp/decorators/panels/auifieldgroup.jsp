<%@ taglib uri="sitemesh-decorator" prefix="decorator" %>
<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%--

Optional Parameters:
    * id                        - the ID attribute of the wrapping div (computed and inherited by child components)
    * type                      - [checkbox || radio || matrix || submit]
    * cssClass                  - appended to the class attribute of the div
    * description               - the description\ describing the group of fields
    * hideContainer             - prevents the <div> from displaying

Notes: See http://confluence.atlassian.com/display/AUI/Forms#Forms-fieldset for the explanations of fieldset types

--%>
<decorator:usePage id="p" />
<% if (!p.getBooleanProperty("hideContainer")) { %><div class="<decorator:getProperty property="type" default="field-group" /><% if (p.isPropertySet("cssClass")) { %> <decorator:getProperty property="cssClass" /><% } %>"<% if (p.isPropertySet("id")) { %> id="<decorator:getProperty property="id" />"<% } %>><% } %>
    <decorator:body />
    <% if (p.isPropertySet("description")) { %><div class="description"><decorator:getProperty property="description" /></div><% } %>
<% if (!p.getBooleanProperty("hideContainer")) { %></div><% } %>