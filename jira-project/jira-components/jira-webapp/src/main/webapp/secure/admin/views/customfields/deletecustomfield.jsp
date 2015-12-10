
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>


<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/fields_section"/>
    <meta name="admin.active.tab" content="view_custom_fields"/>
	<title><ww:text name="'admin.issuefields.customfields.delete'">
	    <ww:param name="'value0'"><ww:property value="customField/name" /></ww:param>
	</ww:text></title>
</head>
<body>

<page:applyDecorator name="jiraform">
	<page:param name="title"><ww:text name="'admin.issuefields.customfields.delete'">
	    <ww:param name="'value0'"><ww:property value="customField/name" /></ww:param>
	</ww:text></page:param>
	<page:param name="autoSelectFirst">false</page:param>
	<page:param name="description">
        <ww:if test='/fieldLocked == false'>
            <ww:if test='/fieldManaged == true'>
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">warning</aui:param>
                    <aui:param name="'messageHtml'">
                        <p><ww:text name="/managedFieldDescriptionKey" /></p>
                    </aui:param>
                </aui:component>
            </ww:if>
            <p><ww:text name="'admin.issuefields.customfields.confirmation'"/></p>
            <p><ww:text name="'admin.issuefields.customfields.deletion.note'">
                <ww:param name="'value0'"><font color=#990000></ww:param>
                <ww:param name="'value1'"></font></ww:param>
            </ww:text></p>
        </ww:if>
	</page:param>

    <page:param name="width">100%</page:param>
	<page:param name="action">DeleteCustomField.jspa</page:param>
    <ww:if test='/fieldLocked == false'>
        <page:param name="submitId">delete_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.words.delete'"/></page:param>
    </ww:if>
	<page:param name="cancelURI">ViewCustomFields.jspa</page:param>

	<ui:component name="'id'" template="hidden.jsp" theme="'single'"  />
</page:applyDecorator>

</body>
</html>
