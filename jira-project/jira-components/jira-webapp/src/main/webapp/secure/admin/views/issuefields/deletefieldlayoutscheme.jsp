<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.issuefields.fieldconfigschemes.delete.field.configuration.scheme'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/fields_section"/>
    <meta name="admin.active.tab" content="issue_fields"/>
</head>
<body>

<page:applyDecorator name="jiraform">
    <page:param name="action">DeleteFieldLayoutScheme.jspa</page:param>
    <page:param name="submitId">delete_submit</page:param>
    <page:param name="submitName"><ww:text name="'common.words.delete'"/></page:param>
    <page:param name="cancelURI">ViewFieldLayoutSchemes.jspa</page:param>
    <page:param name="title"><ww:text name="'admin.issuefields.fieldconfigschemes.delete.field.configuration.scheme'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="helpURL">view_field_layout_schemes</page:param>
    <page:param name="description">
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">warning</aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:text name="'admin.issuefields.fieldconfigschemes.delete.confirmation'">
                    <ww:param name="'value0'"><b><ww:property value="/fieldLayoutScheme/name"/></b></ww:param>
                </ww:text></p>
            </aui:param>
        </aui:component>


        <ww:component name="'id'" template="hidden.jsp"/>

        <ui:component name="'confirm'" value="'true'" template="hidden.jsp" theme="'single'" />
    </page:param>
</page:applyDecorator>

</body>
</html>
