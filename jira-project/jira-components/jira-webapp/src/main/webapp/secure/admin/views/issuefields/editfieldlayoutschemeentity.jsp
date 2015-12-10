<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.issuefields.fieldconfigschemes.edit.config.scheme.entry'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/fields_section"/>
    <meta name="admin.active.tab" content="issue_fields"/>
</head>
<body>
<page:applyDecorator name="jiraform">
    <page:param name="action">EditFieldLayoutSchemeEntity.jspa</page:param>
    <page:param name="title"><ww:text name="'admin.issuefields.fieldconfigschemes.edit.config.scheme.entry'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="helpURL">fieldscreenschemes</page:param>
    <page:param name="submitId">update_submit</page:param>
    <page:param name="submitName"><ww:text name="'common.words.update'"/></page:param>
    <page:param name="cancelURI"><ww:url page="ConfigureFieldLayoutScheme.jspa"><ww:param name="'id'" value="/id" /></ww:url></page:param>
    <page:param name="description">
        <p>
            <ww:if test="/issueTypeId">
                <ww:text name="'admin.issuefields.fieldconfigschemes.edit.instruction.a'">
                    <ww:param name="'value0'"><b><ww:property value="/issueType/string('name')" /></b></ww:param>
                    <ww:param name="'value1'"><b><ww:property value="/fieldLayoutScheme/name" /></b></ww:param>
                    <ww:param name="'value2'"><b></ww:param>
                    <ww:param name="'value3'"></b></ww:param>
                </ww:text>
            </ww:if>
            <ww:else>
                <ww:text name="'admin.issuefields.fieldconfigschemes.edit.instruction.b'">
                    <ww:param name="'value0'"><b></ww:param>
                    <ww:param name="'value1'"></b></ww:param>
                    <ww:param name="'value2'"><b><ww:property value="/fieldLayoutScheme/name" /></b></ww:param>
                    <ww:param name="'value3'"><b></ww:param>
                    <ww:param name="'value3'"></b></ww:param>
                </ww:text>
            </ww:else>
        </p>
    </page:param>

    <ui:select label="text('admin.issuefields.fieldconfigschemes.field.configuration')" name="'fieldConfigurationId'" list="/fieldLayouts" listKey="'/fieldLayoutId(.)'" listValue="'./name'">
        <ui:param name="'description'"><ww:text name="'admin.issuefields.fieldconfigschemes.field.configuration.description'"/></ui:param>
    </ui:select>

    <ww:if test="/issueTypeId">
        <ui:component name="'issueTypeId'" template="hidden.jsp" theme="'single'"/>
    </ww:if>

    <ui:component name="'id'" template="hidden.jsp" theme="'single'"/>

    <ui:component name="'edited'" value="'true'" template="hidden.jsp" theme="'single'"/>

</page:applyDecorator>
</body>
</html>
