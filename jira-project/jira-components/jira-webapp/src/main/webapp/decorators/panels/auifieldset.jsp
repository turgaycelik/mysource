<%@ taglib uri="sitemesh-decorator" prefix="decorator" %>
<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%--

Optional Attributes:
    * id                                - ID attribute for the FIELDSET (computed with parent IDs and inherited by child components)

Required Parameters:
    * legend                            - i18n text for the LEGEND element

Optional Parameters:
    * type                              - [group || date-select || inline ]
                                            NOTE: if using inline ensure you set useCustomButtons to true on the
                                            auiform decorator
    * cssClass                          - additional classes to be appended to the fieldset after the type

Code example:
    <page:applyDecorator id="personal-details" name="personalDetails">
        <page:param name="type">inline</page:param>
        <page:param name="cssClass">custom-class</page:param>
        <page:param name="legend"><ww:text name="'my.custom.fieldset.legend.text'"/></page:param>
        ...
    </page:applyDecorator>

Notes: See http://confluence.atlassian.com/display/AUI/Forms#Forms-fieldset for the explanations of fieldset types

--%>
<decorator:usePage id="p" />
<fieldset<% if (p.isPropertySet("id")) { %> id="<decorator:getProperty property="id" />"<% } %><% if (p.isPropertySet("type") || p.isPropertySet("cssClass")) { %> class="<decorator:getProperty property="type" /> <decorator:getProperty property="cssClass" />"<% } %>>
    <% if (p.isPropertySet("legend")) { %><legend<% if (p.isPropertySet("legendCssClass")) { %> class="<decorator:getProperty property="legendCssClass" />"<% } %>><span<% if (p.isPropertySet("legendSpanCssClass")) { %> class="<decorator:getProperty property="legendSpanCssClass" />"<% } %>><decorator:getProperty property="legend" /><% if (p.isPropertySet("mandatory") && p.getProperty("mandatory").equals("true")) { %><span class="aui-icon icon-required"> <ww:text name="'AUI.form.label.text.required'"/></span><% } %></span></legend><% } %>
    <decorator:body />
</fieldset><% if (p.isPropertySet("type") || p.isPropertySet("cssClass") || p.isPropertySet("id")) { %> <!-- // <% if (p.isPropertySet("type")) { %>.<decorator:getProperty property="type" /><% } %><% if (p.isPropertySet("cssClass")) { %>.<decorator:getProperty property="cssClass" /><% } %><% if (p.isPropertySet("id")) { %> #<decorator:getProperty property="id" /><% } %> --><% } %>