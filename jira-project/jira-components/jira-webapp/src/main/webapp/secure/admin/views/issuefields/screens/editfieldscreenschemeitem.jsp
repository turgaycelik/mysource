<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/screens_section"/>
    <meta name="admin.active.tab" content="field_screen_scheme"/>
	<title><ww:text name="'admin.issuefields.screenschemes.edit.screen.scheme.item'"/></title>
</head>
<body>
<page:applyDecorator name="jiraform">
    <page:param name="action">EditFieldScreenSchemeItem.jspa</page:param>
    <page:param name="submitId">update_submit</page:param>
    <page:param name="submitName"><ww:text name="'common.forms.update'"/></page:param>
    <page:param name="cancelURI"><ww:url page="ConfigureFieldScreenScheme.jspa"><ww:param name="'id'" value="/id" /></ww:url></page:param>
    <page:param name="title"><ww:text name="'admin.issuefields.screenschemes.edit.screen.scheme.item'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="description">
        <p>
            <ww:text name="'admin.issuefields.screenschemes.edit.instruction'">
                <ww:param name="'value0'"><b><ww:if test="/issueOperation"><ww:property value="/issueOperation/name" /></ww:if><ww:else><ww:text name="'admin.common.words.default'"/></ww:else></b></ww:param>
            </ww:text>
        </p>
    </page:param>

    <ui:select label="text('admin.common.words.screen')" name="'fieldScreenId'" list="/fieldScreens" listKey="'./id'" listValue="'./name'">
        <ui:param name="'description'"><ww:text name="'admin.issuefields.screenschemes.edit.the.screen.to.show.for.this.issue.operation'"/></ui:param>
    </ui:select>

    <ui:component name="'issueOperationId'" template="hidden.jsp" theme="'single'" />

    <ui:component name="'id'" template="hidden.jsp" theme="'single'" />
</page:applyDecorator>
</body>
</html>
