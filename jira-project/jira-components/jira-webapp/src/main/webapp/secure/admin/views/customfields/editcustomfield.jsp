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
</head>

<body>

	<page:applyDecorator name="jiraform">
		<page:param name="title"><ww:text name="'admin.issuefields.customfields.edit.details2'"/></page:param>
		<page:param name="action">EditCustomField.jspa</page:param>
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
                <ww:text name="'admin.issuefields.customfields.edit.reindexing.note'"/>
            </page:param>
            <page:param name="submitId">update_submit</page:param>
            <page:param name="submitName"><ww:text name="'common.forms.update'"/></page:param>
        </ww:if>
		<page:param name="width">100%</page:param>
		<page:param name="cancelURI">ViewCustomFields.jspa</page:param>

        <ww:if test='/fieldLocked == false'>
            <ui:textfield label="text('admin.issuefields.field.name')" name="'name'" />

            <tr>
                <td class="fieldLabelArea">
                    <ww:property value="text('common.words.description')"/>
                </td>
                <td class="fieldValueArea">
                    <ww:property value="/customField/untranslatedDescriptionProperty/editHtml('description')" escape="false"/>
                    <div class="fieldDescription">
                        <ww:property value="/customField/descriptionProperty/descriptionHtml" escape="false"/>
                    </div>
                </td>
            </tr>

            <ww:if test="/searchers != null && /searchers/empty == false">
                <ui:select label="text('admin.issuefields.customfields.search.template')" name="'searcher'" list="/searchers" listKey="'descriptor/completeKey'" listValue="'descriptor/name'" value="/searcher">
                    <ui:param name="'description'" value="text('admin.issuefields.customfields.search.change.requires.reindex')"/>
                    <ui:param name="'headerrow'" value="text('common.words.none')" />
                    <ui:param name="'headervalue'" value="'-1'" />
                </ui:select>
            </ww:if>
            <ww:else>
                <ui:component label="text('admin.issuefields.customfields.search.template')" value="text('admin.issuefields.customfields.no.search.templates')" template="textlabel.jsp" />
            </ww:else>
        </ww:if>

        <ui:component name="'id'" template="hidden.jsp" theme="'single'"  />
        <ui:component name="'fieldType'" template="hidden.jsp" theme="'single'"  />
        <%-- record what page to redirect after success --%>
        <ui:component name="'redirectURI'" template="hidden.jsp" theme="'single'"  />
	</page:applyDecorator>

</body>
</html>
