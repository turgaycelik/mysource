<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.jira.web.action.util.FieldsResourceIncluder" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%
    final FieldsResourceIncluder fieldResourceIncluder = ComponentManager.getComponentInstanceOfType(FieldsResourceIncluder.class);
    fieldResourceIncluder.includeFieldResourcesForCurrentUser();
%>
<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/fields_section"/>
    <meta name="admin.active.tab" content="view_custom_fields"/>
	<title><ww:text name="'admin.issuefields.customfields.create.custom.field.defaults'"/></title>
</head>
<body>
    <!-- JRA-4345 - need to statically include the javascript to address IE6 refresh issue -->

	<page:applyDecorator name="jiraform">
		<page:param name="title"><ww:text name="'admin.issuefields.customfields.set.custom.field.defaults'"/></page:param>
		<page:param name="description"><ww:text name="'admin.issuefields.customfields.set.defaults.description'">
		   <ww:param name="'value0'"><ww:property value="customField/name" /></ww:param>
		</ww:text></page:param>
		<page:param name="action">EditCustomFieldDefaults.jspa</page:param>
		<page:param name="width">100%</page:param>
        <ww:if test='/fieldLocked == false'>
            <page:param name="submitId">set_defaults_submit</page:param>
            <page:param name="submitName"><ww:text name="'admin.issuefields.customfields.set.defaults'"/></page:param>
        </ww:if>
        <page:param name="cancelURI">ViewCustomFields.jspa</page:param>
        <page:param name="helpURL">customfields</page:param>

        <ui:component name="'fieldConfigId'" template="hidden.jsp" theme="'single'"  />
        <ui:component name="'fieldConfigSchemeId'" template="hidden.jsp" theme="'single'"  />
        <ww:if test='/fieldLocked == false'>
            <ww:property value="/customFieldHtml" escape="false" />
        </ww:if>
    </page:applyDecorator>
</body>
</html>
