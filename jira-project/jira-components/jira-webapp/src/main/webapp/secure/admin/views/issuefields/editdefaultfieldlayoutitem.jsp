<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.issuefields.fieldconfigurations.edit.field.layout.item'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/fields_section"/>
    <meta name="admin.active.tab" content="field_configuration"/>
</head>
<body>
<page:applyDecorator name="jiraform">
    <page:param name="action">EditDefaultFieldLayoutItem.jspa</page:param>
    <ww:if test="/fieldLocked == false">
        <page:param name="submitId">update_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.forms.update'"/></page:param>
    </ww:if>
    <page:param name="cancelURI">ViewIssueFields.jspa</page:param>
    <page:param name="title"><ww:text name="'admin.issuefields.fieldconfigurations.edit.field.description'"/>: <ww:property value="/fieldName"/></page:param>
    <page:param name="description">
        <p><ww:text name="'admin.issuefields.fieldconfigurations.the.description.appears'"/></p>
        <p><ww:text name="'admin.issuefields.fieldconfigurations.update.the.description.for.the.field'">
            <ww:param name="'value0'">'<ww:property value="/fieldName"/>'</ww:param>
        </ww:text></p>
    </page:param>
    <page:param name="width">100%</page:param>

    <ww:if test="/fieldLocked == false">
        <ui:textarea rows="3" label="text('common.words.description')" name="'description'">
            <ui:param name="'style'" value="'width: 95%;'" />
        </ui:textarea>

        <ui:component name="'position'" template="hidden.jsp" />
    </ww:if>
</page:applyDecorator>
</body>
</html>
