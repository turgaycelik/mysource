<%@ page import="com.atlassian.jira.component.ComponentAccessor" %>
<%@ page import="com.atlassian.plugin.webresource.UrlMode" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/fields_section"/>
    <meta name="admin.active.tab" content="view_custom_fields"/>
	<title><ww:text name="'admin.issuefields.customfields.create.custom.field'"/></title>
</head>
<body>
<page:applyDecorator name="jiraform">
    <page:param name="title"><ww:text name="'admin.issuefields.customfields.create.custom.field'"/></page:param>
    <page:param name="instructions">
        <p>
            <ww:text name="'admin.issuefields.customfields.choose.the.field.type'">
                <ww:param name="'value0'"><ww:property value="fieldTypes/size" /></ww:param>
            </ww:text>
        </p>
        <ww:if test="/onDemand == false">
        <p>
            <img src="<%= ComponentAccessor.getWebResourceUrlProvider().getStaticResourcePrefix(UrlMode.AUTO) %>/images/icons/marketplace-ico.png" width="16" height="16" border="0" />
            <ww:text name="'admin.issuefields.customfields.extentions'">
                <ww:param name="'value0'"><a href="<%=request.getContextPath()%>/plugins/servlet/upm/marketplace/popular?category=Custom+Fields&source=custom_fields_create" style="vertical-align: text-bottom"></ww:param>
                <ww:param name="'value1'"></a></ww:param>
            </ww:text>
        </p>
        </ww:if>
    </page:param>
    <page:param name="width">100%</page:param>
    <page:param name="action">CreateCustomField.jspa</page:param>
    <page:param name="cancelURI">ViewCustomFields.jspa</page:param>
    <page:param name="helpURL">addingcustomfields</page:param>
    <page:param name="wizard">true</page:param>
    <tr>
        <td>
        <ww:if test="fieldTypes/size > 0">
        <table class="aui custom-field-types">
            <ww:iterator value="fieldTypes" status="'status'">
                <ww:if test="@status/odd == true">
                    <tr>
                </ww:if>
                    <td id="cell<ww:property value="key"/>" onclick="selectCellRadioBox(this.id)">
                        <div class="field-group">
                            <input type="radio" name="fieldType" value="<ww:property value="key"/>" id="<ww:property value="key"/>_id">
                            <label for="<ww:property value="key"/>_id"><ww:property value="name"/></label>
                            <ww:if test="./descriptor/typeManaged == true">
                                <span class="aui-lozenge status-managed" title="<ww:text name="./descriptor/managedDescriptionKey"/>"><ww:text name="'admin.managed.configuration.items.managed'" /></span>
                            </ww:if>
                            <div class="description"><ww:property value="description"/></div>
                        </div>
                    </td>
                <ww:if test="@status/even == true">
                    </tr>
                </ww:if>
            </ww:iterator>
        </table>
        </ww:if>
        <ww:else>
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">info</aui:param>
                <aui:param name="'messageHtml'"><ww:text name="'admin.issuefields.customfields.you.have.no.custom.field.types.available'"/></aui:param>
            </aui:component>
        </ww:else>
        </td>
    </tr>
    <ui:component template="multihidden.jsp" >
        <ui:param name="'fields'">fieldName,description,searcher,global</ui:param>
        <ui:param name="'multifields'">projects,issuetypes</ui:param>
    </ui:component>
</page:applyDecorator>


<script language="javascript" type="text/javascript">

    <ww:if test="/fieldTypeValid == 'true'">
        <!--
        selectCellRadioBox('cell<ww:property value="/fieldType" />');
        //-->
    </ww:if>

    var selected;
    function selectCellRadioBox(cellId)
    {
        var id = cellId.substring(4, cellId.length);
        document.forms['jiraform'].elements[id + '_id'].checked = true;
    }

</script>
</body>
</html>
