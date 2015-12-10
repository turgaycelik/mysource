
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section/issue_features"/>
    <meta name="admin.active.tab" content="linking"/>
	<title><ww:text name="'admin.issuelinking.edit.link.type'"/></title>
</head>

<body>

<page:applyDecorator name="jiraform">
    <page:param name="action">EditLinkType.jspa</page:param>
    <page:param name="submitId">edit_link_type</page:param>
    <page:param name="submitName"><ww:text name="'common.forms.update'"/></page:param>
    <page:param name="cancelURI">ViewLinkTypes!default.jspa</page:param>
    <page:param name="title"><ww:text name="'admin.issuelinking.edit.link.type'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="autoSelectFirst">false</page:param>

    <ui:textfield label="text('common.words.name')" name="'name'" size="'30'">
        <ui:param name="'description'"><ww:text name="'admin.issuelinking.name.eg'"/></ui:param>
    </ui:textfield>

    <ui:textfield label="text('admin.issuelinking.outward.description')" name="'outward'" size="'30'">
        <ui:param name="'description'"><ww:text name="'admin.issuelinking.outward.description.eg'"/></ui:param>
    </ui:textfield>

    <ui:textfield label="text('admin.issuelinking.inward.description')" name="'inward'" size="'30'">
        <ui:param name="'description'"><ww:text name="'admin.issuelinking.inward.description.eg'"/></ui:param>
    </ui:textfield>

    <ui:component name="'id'" template="hidden.jsp" />

</page:applyDecorator>

</body>
</html>
