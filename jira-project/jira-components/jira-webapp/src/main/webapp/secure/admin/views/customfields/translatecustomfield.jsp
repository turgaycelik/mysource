<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib prefix="jira" uri="jiratags" %>

<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/fields_section"/>
    <meta name="admin.active.tab" content="view_custom_fields"/>
    <title><ww:text name="'admin.issuefields.customfields.edit.details'"/></title>
    <jira:web-resource-require modules="jira.webresources:jira-fields" />
    <jira:web-resource-require modules="jira.webresources:translatecustomfield" />
</head>

<body>

<page:applyDecorator name="jiraform">
    <page:param name="title">
        <ww:text name="'admin.issuefields.customfields.translate.title'">
            <ww:param><ww:property value="/customField/untranslatedName"/></ww:param>
        </ww:text>
    </page:param>
    <page:param name="submitId">update_submit</page:param>
    <page:param name="submitName"><ww:text name="'common.forms.update'"/></page:param>
    <page:param name="action">TranslateCustomField.jspa</page:param>

    <ww:if test='/fieldLocked == false'>
        <page:param name="description">
            <ww:if test='/fieldManaged == true'>
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">warning</aui:param>
                    <aui:param name="'messageHtml'">
                        <p><ww:text name="/managedFieldDescriptionKey" /></p>
                    </aui:param>
                </aui:component>
            </ww:if>
        </page:param>
    </ww:if>
    <page:param name="width">100%</page:param>
    <page:param name="cancelURI">ViewCustomFields.jspa</page:param>

    <ui:select label="text('admin.issuefields.customfields.translate.choose.language')" name="'selectedLocale'" list="installedLocales" listKey="'key'" listValue="'value'"/>

    <ui:textfield label="text('admin.issuefields.field.name')" name="'name'" value="name"/>

    <tr>
        <td class="fieldLabelArea">
            <ww:property value="text('common.words.description')"/>
        </td>
        <td class="fieldValueArea">
            <ww:property value="/descriptionProperty/editHtml('description')" escape="false"/>
            <div class="fieldDescription">
                <ww:property value="/customField/descriptionProperty/descriptionHtml" escape="false"/>
            </div>
        </td>
    </tr>

    <ui:component name="'id'" template="hidden.jsp" theme="'single'"  />
    <ui:component name="'fieldType'" template="hidden.jsp" theme="'single'"  />
    <%-- record what page to redirect after success --%>
    <ui:component name="'redirectURI'" template="hidden.jsp" theme="'single'"  />
</page:applyDecorator>

</body>

</html>
