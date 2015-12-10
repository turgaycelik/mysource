<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.issuefields.fieldconfigurations.renderer.edit.confirmation'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/fields_section"/>
    <meta name="admin.active.tab" content="field_configuration"/>
</head>

<body>
<page:applyDecorator name="jiraform">
    <page:param name="action">EditFieldLayoutItemRenderer.jspa</page:param>
    <page:param name="submitId">update_submit</page:param>
    <page:param name="submitName"><ww:text name="'common.forms.update'"/></page:param>
    <page:param name="cancelURI"><ww:property value="/cancelUrl"/></page:param>
    <page:param name="title"><ww:text name="'admin.issuefields.fieldconfigurations.renderer.edit.confirmation2'">
        <ww:param name="'value0'"><ww:property value="/fieldName"/></ww:param>
    </ww:text></page:param>
    <page:param name="description">
        <p><ww:text name="'admin.issuefields.fieldconfigurations.renderer.description'"/></p>
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">warning</aui:param>
            <aui:param name="'messageHtml'">
                <p>
                    <ww:text name="'admin.issuefields.fieldconfigurations.renderer.edit.confirmation.question'">
                        <ww:param name="'value0'"><strong><ww:property value="/fieldName" /></strong></ww:param>
                        <ww:param name="'value1'"><strong><ww:property value="/rendererDisplayName(/selectedRendererType)" /></strong></ww:param>
                    </ww:text>
                </p>
            </aui:param>
        </aui:component>
    </page:param>
    <page:param name="width">100%</page:param>
    <ui:component name="'rendererEdit'" template="hidden.jsp" />
    <ui:component name="'selectedRendererType'" template="hidden.jsp" />
    <ui:component name="'id'" template="hidden.jsp" />
    <input type="hidden" name="confirmed" value="true">
</page:applyDecorator>
</body>

</html>
