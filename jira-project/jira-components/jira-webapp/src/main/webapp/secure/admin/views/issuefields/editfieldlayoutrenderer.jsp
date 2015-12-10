<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.issuefields.fieldconfigurations.renderer.edit.field.layout.items.renderer'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/fields_section"/>
    <meta name="admin.active.tab" content="field_configuration"/>
</head>
<body>

<page:applyDecorator name="jiraform">
    <page:param name="action">EditFieldLayoutItemRendererConfirmation.jspa</page:param>
    <ww:if test="/selectedFieldLocked == false">
        <page:param name="submitId">update_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.forms.update'"/></page:param>
    </ww:if>
    <page:param name="cancelURI"><ww:property value="/cancelUrl"/></page:param>
    <page:param name="title"><ww:text name="'admin.issuefields.fieldconfigurations.renderer.edit.field.renderer'" />: <ww:property value="/fieldName"/></page:param>
    <page:param name="description">
        <p><ww:text name="'admin.issuefields.fieldconfigurations.renderer.description'"/></p>
        <p><ww:text name="'admin.issuefields.fieldconfigurations.renderer.update.for.field'">
            <ww:param name="'value0'">'<ww:property value="/fieldName"/>'</ww:param>
        </ww:text></p>
        <ww:if test="/selectedFieldLocked == false && /effectedIssuesCount != 0" >
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">warning</aui:param>
                <aui:param name="'messageHtml'">
                    <p><ww:text name="'admin.issuefields.fieldconfigurations.renderer.warning'">
                            <ww:param name="'value0'"><a href="<%= request.getContextPath() %>/secure/IssueNavigator.jspa?reset=true&<ww:text name="/effectedIssuesQueryString"/>"><ww:property value="/effectedIssuesCount"/></ww:param>
                            <ww:param name="'value1'"></a></ww:param>
                        </ww:text>
                    </p>
                </aui:param>
            </aui:component>
        </ww:if>
    </page:param>
    <page:param name="width">100%</page:param>

    <ww:if test="/selectedFieldLocked == false">
        <ui:select label="text('admin.issuefields.fieldconfigurations.renderer.active.renderer')" name="'selectedRendererType'" list="/allActiveRenderers" listKey="'.'" listValue="'/rendererDisplayName(.)'" value="/currentRendererType">
            <ui:param name="'description'">
                <ww:text name="'admin.issuefields.fieldconfigurations.renderer.description'"/>
            </ui:param>
        </ui:select>
    </ww:if>

    <ui:component name="'rendererEdit'" template="hidden.jsp" />
    <ui:component name="'id'" template="hidden.jsp" />
    <input type="hidden" name="fieldName" value="<ww:property value="/fieldName"/>">
</page:applyDecorator>
</body>
</html>
