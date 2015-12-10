<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib prefix="jira" uri="jiratags" %>

<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/fields_section"/>
    <meta name="admin.active.tab" content="view_custom_fields"/>
	<title><ww:text name="'admin.createcustomfield.details.title'"/></title>
    <jira:web-resource-require modules="jira.webresources:jira-fields" />
</head>

<body>
	<page:applyDecorator name="jiraform">
        <page:param name="jiraformId">customfield-details</page:param>
		<page:param name="title"><ww:text name="'admin.createcustomfield.details.title'"/></page:param>
		<page:param name="instructions"><ww:text name="'admin.createcustomfield.details.instructions'"/></page:param>
		<page:param name="action">CreateCustomField.jspa</page:param>
		<page:param name="width">100%</page:param>
    	<page:param name="cancelURI">ViewCustomFields.jspa</page:param>
        <page:param name="helpURL">addingcustomfields</page:param>
        <page:param name="wizard">true</page:param>

        <ui:component template="multihidden.jsp" >
            <ui:param name="'fields'">fieldType</ui:param>
        </ui:component>

        <ui:component name="'customfield-type'" label="text('admin.issuefields.customfields.field.type')" value="/customFieldType/name" template="textlabel.jsp" />

        <ui:textfield label="text('admin.issuefields.field.name')" name="'fieldName'" >
            <ui:param name="'description'"><ww:text name="'admin.createcustomfield.details.name.for.custom.field'"/></ui:param>
            <ui:param name="'mandatory'">true</ui:param>
        </ui:textfield>

        <tr>
            <td class="fieldLabelArea">
                <ww:property value="text('common.words.description')"/>
            </td>
            <td class="fieldValueArea">
                <ww:property value="/descriptionProperty/editHtml('description')" escape="false"/>
                <div class="fieldDescription">
                    <ww:property value="/descriptionProperty/descriptionHtml" escape="false"/>
                </div>
            </td>
        </tr>

        <ui:component label="text('admin.createcustomfield.details.choose.search.template')" template="sectionbreak.jsp">
        </ui:component>

        <ww:if test="/searchers != null && /searchers/empty == false">
            <ui:select label="text('admin.createcustomfield.details.search.template')" name="'searcher'" list="/searchers" listKey="'descriptor/completeKey'" listValue="'descriptor/name'"  value="/searcher">
                <ui:param name="'summary'" value="'./description'"/>
                <ui:param name="'description'">
                    <ww:text name="'admin.createcustomfield.details.searcher.desc'"/>
                </ui:param>
                <ui:param name="'headerrow'" value="'None'" />
                <ui:param name="'headervalue'" value="'-1'" />
            </ui:select>
        </ww:if>
        <ww:else>
            <ui:component label="text('admin.createcustomfield.details.search.template')" value="text('admin.createcustomfield.details.searcher.template.msg')" template="textlabel.jsp" />
        </ww:else>

        <jsp:include page="addcontext.jsp" />

	</page:applyDecorator>

</body>
</html>
