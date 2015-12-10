<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/screens_section"/>
    <meta name="admin.active.tab" content="issue_type_screen_scheme"/>
	<title><ww:text name="'admin.edit.itss.entry.title'"/></title>
</head>
<body>
<page:applyDecorator name="jiraform">
    <page:param name="action">EditIssueTypeScreenSchemeEntity.jspa</page:param>
    <page:param name="title"><ww:text name="'admin.edit.itss.entry.title'"/></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="helpURL">fieldscreenschemes</page:param>
    <page:param name="submitId">update_submit</page:param>
    <page:param name="submitName"><ww:text name="'common.forms.update'"/></page:param>
    <page:param name="cancelURI"><ww:url page="ConfigureIssueTypeScreenScheme.jspa"><ww:param name="'id'" value="/id" /></ww:url></page:param>
    <page:param name="description">
        <p>
            <ww:if test="/issueTypeId">
                <ww:text name="'admin.edit.itss.entry.instructions1'">
                    <ww:param name="'value0'"><b><ww:property value="/issueType/string('name')" /></b></ww:param>
                    <ww:param name="'value1'"><b><ww:property value="/issueTypeScreenScheme/name" /></b></ww:param>
                    <ww:param name="'value2'"><b></ww:param>
                    <ww:param name="'value3'"><ww:text name="'common.forms.update'"/></ww:param>
                    <ww:param name="'value4'"></b></ww:param>
                </ww:text>
            </ww:if>
            <ww:else>
                <ww:text name="'admin.edit.itss.entry.instructions1'">
                    <ww:param name="'value0'"><b><ww:text name="'admin.common.words.default'"/></b></ww:param>
                    <ww:param name="'value1'"><b><ww:property value="/issueTypeScreenScheme/name" /></b></ww:param>
                    <ww:param name="'value2'"><b></ww:param>
                    <ww:param name="'value3'"><ww:text name="'common.forms.update'"/></ww:param>
                    <ww:param name="'value4'"></b></ww:param>
                </ww:text>
            </ww:else>
        </p>
    </page:param>

    <ui:select label="text('admin.menu.issuefields.screen.scheme')" name="'fieldScreenSchemeId'" list="/fieldScreenSchemes" listKey="'./id'" listValue="'./name'">
        <ui:param name="'mandatory'">true</ui:param>
        <ui:param name="'description'"><ww:text name="'admin.edit.itss.entry.description'"/></ui:param>
    </ui:select>

    <ui:component name="'id'" template="hidden.jsp" theme="'single'"/>

    <ww:if test="/issueTypeId">
        <ui:component name="'issueTypeId'" template="hidden.jsp" theme="'single'"/>
    </ww:if>

    <ui:component name="'edited'" value="'true'" template="hidden.jsp" theme="'single'"/>

</page:applyDecorator>
</body>
</html>
