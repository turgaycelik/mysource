<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/fields_section"/>
    <meta name="admin.active.tab" content="view_custom_fields"/>
	<title><ww:text name="'admin.issuefields.customfields.configure.custom.field'">
	    <ww:param name="'value0'"><ww:property value="/customField/name" /></ww:param>
	</ww:text></title>
</head>
<body>
<style type="text/css">
<!--
.fieldLabelArea
{
    width: 30%;
}
-->
</style>
	<page:applyDecorator name="jirapanel">
		<page:param name="title">
            <ww:text name="'admin.issuefields.customfields.configure.custom.field'">
	            <ww:param name="'value0'"><ww:property value="/customField/name" /></ww:param>
	        </ww:text>
            <ww:if test='/fieldLocked == true'>
                <span class="aui-lozenge status-locked" title="<ww:text name="/managedFieldDescriptionKey"/>"><ww:text name="'admin.managed.configuration.items.locked'"/></span>
            </ww:if>
            <ww:elseIf test='/fieldManaged == true'>
                <span class="aui-lozenge status-managed" title="<ww:text name="/managedFieldDescriptionKey"/>"><ww:text name="'admin.managed.configuration.items.managed'"/></span>
            </ww:elseIf>
        </page:param>
		<page:param name="instructions">
        <p>
            <ww:text name="'admin.issuefields.customfields.configure.description'"/>
        </p>
        <ul class="square">
            <ww:if test='/fieldLocked == false'>
                <li><a id="add_new_context" title="<ww:text name="'admin.issuefields.customfields.add.new.context'"/>" href="<ww:url value="'ManageConfigurationScheme!default.jspa'"><ww:param name="'customFieldId'" value="/customFieldId" /><ww:param name="'returnUrl'">ConfigureCustomField!default.jspa?customFieldId=<ww:property value="/customFieldId" /></ww:param></ww:url>"><ww:text name="'admin.issuefields.customfields.add.new.context'"/></a></li>
            </ww:if>
            <li><a title="<ww:text name="'admin.issuefields.customfields.view.custom.fields'"/>" href="<ww:url value="'ViewCustomFields.jspa'"></ww:url>"><ww:text name="'admin.issuefields.customfields.view.custom.fields'"/></a></li>
        </ul>
        </page:param>
        <page:param name="helpURL">configcustomfield</page:param>
		<page:param name="width">100%</page:param>
        <ww:iterator value="/configs" status="'status'">
        <ww:if test="id == /fieldConfigSchemeId">
            <div class="highlighted">
        </ww:if>
        <page:applyDecorator name="jiraform">
            <page:param name="width">100%</page:param>
            <page:param name="pretitle">
                <ww:if test='/fieldLocked == false'>
                    <div class="toolbar">
                        <a id="edit_<ww:property value="id" />" href="<ww:url page="/secure/admin/ManageConfigurationScheme!default.jspa"><ww:param name="'customFieldId'" value="/customFieldId"/><ww:param name="'fieldConfigSchemeId'" value="./id" /></ww:url>" title="<ww:text name="'admin.issuefields.customfields.edit.scheme'"/>"><img src="<%= request.getContextPath() %>/images/icons/confg_16.gif" /></a>
                        <a id="delete_<ww:property value="id" />" href="<ww:url page="/secure/admin/ManageConfigurationScheme!remove.jspa"><ww:param name="'customFieldId'" value="/customFieldId"/><ww:param name="'fieldConfigSchemeId'" value="./id" /></ww:url>" title="<ww:text name="'admin.issuefields.customfields.delete.scheme'"/>"><img src="<%= request.getContextPath() %>/images/icons/trash_16.gif" /></a>
                    </div>
                </ww:if>
            </page:param>
            <page:param name="title"><ww:property value="name" /></page:param>
            <page:param name="jiraformId">configscheme<ww:property value="id" /></page:param>
            <page:param name="instructions"><ww:property value="description" ><ww:if test="."><ww:property value="." /></ww:if></ww:property></page:param>

            <tr>
                <td class="fieldLabelArea">
                    <ww:text name="'admin.issuefields.customfields.applicable.contexts'"/>
                </td>
                <td class="fieldValueArea">
                    <ww:if test='/fieldLocked == false'>
                        <a class="config actionLinks subText" href="<ww:url value="'ManageConfigurationScheme!default.jspa'"><ww:param name="'fieldConfigSchemeId'" value="id" /><ww:param name="'customFieldId'" value="/customFieldId" /><ww:param name="'returnUrl'">ConfigureCustomField!default.jspa?customFieldId=<ww:property value="/customFieldId" /></ww:param></ww:url>" title="<ww:text name="'common.words.edit'"/>"><ww:text name="'admin.common.phrases.edit.configuration'"/></a>
                    </ww:if>

                   <jsp:include page="contexts.jsp" flush="true"/>
                </td>
            </tr>

            <ww:if test="./basicMode == true">
                <ww:iterator value="./configsByConfig" status="'status'">

                <ww:iterator value="./key/configItems" status="'rowStatus'">
                <tr>
                    <td class="fieldLabelArea"><ww:text name="displayNameKey" />:</td>
                    <td class="fieldValueArea" id="customfield_<ww:property value="/customFieldId"/>-value-<ww:property value="objectKey"/>">
                        <ww:if test="baseEditUrl && /fieldLocked == false"><a id="customfield_<ww:property value="/customFieldId"/>-edit-<ww:property value="objectKey"/>" class="actionLinks subText" title="<ww:text name="'admin.issuefields.customfields.edit'">
                            <ww:param name="'value0'"><ww:text name="displayNameKey" /></ww:param>
                        </ww:text>" href="<ww:url value="baseEditUrl"><ww:param name="'fieldConfigSchemeId'" value="../id" /><ww:param name="'fieldConfigId'" value="../key/id" /><ww:param name="'customFieldId'" value="/customFieldId" /><ww:param name="'returnUrl'">ConfigureCustomField!default.jspa?customFieldId=<ww:property value="/customFieldId" /></ww:param></ww:url>"><ww:text name="'admin.customfields.edit.value'">
                            <ww:param name="'value0'"><ww:text name="displayNameKey" /></ww:param>
                        </ww:text></a></ww:if>
                        <ww:property value="viewHtml(null)" escape="false" />
                    </td>
                </tr>
                </ww:iterator>
                </ww:iterator>

            </ww:if>
        </page:applyDecorator>
        <ww:if test="id == /fieldConfigSchemeId">
            </div>
        </ww:if>
        </ww:iterator>
    </page:applyDecorator>
</body>
</html>
