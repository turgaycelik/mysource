<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<ww:if test="config/id != null">
    <ww:property id="command" value="text('admin.common.words.modify')" />
</ww:if>
<ww:else>
    <ww:property id="command" value="text('common.forms.add')" />
</ww:else>

<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/fields_section"/>
    <meta name="admin.active.tab" content="view_custom_fields"/>
    <title><ww:text name="'admin.issuefields.customfields.perform.action.on.configuration.scheme.context'">
        <ww:param name="'value0'"><ww:property value="@command" /></ww:param>
    </ww:text></title>
</head>

<body>

<page:applyDecorator name="jiraform">
    <page:param name="helpURL">configcustomfield</page:param>
    <page:param name="helpURLFragment">#Managing+multiple+configuration+schemes</page:param>
    <page:param name="title"><ww:text name="'admin.issuefields.customfields.perform.action.on.configuration.scheme.context'">
        <ww:param name="'value0'"><ww:property value="@command" /></ww:param>
    </ww:text></page:param>
    <page:param name="instructions">
        <ww:text name="'admin.issuefields.configuration.contexts.description'"/>
    </page:param>
    <page:param name="action">ManageConfigurationScheme.jspa</page:param>
    <page:param name="width">100%</page:param>
    <page:param name="cancelURI">ViewCustomFields.jspa</page:param>

    <ui:component template="multihidden.jsp" >
        <ui:param name="'fields'">fieldConfigSchemeId,customFieldId,basicMode</ui:param>
        <ui:param name="'multifields'">fieldConfigIds</ui:param>
    </ui:component>

</page:applyDecorator>

</body>
</html>
