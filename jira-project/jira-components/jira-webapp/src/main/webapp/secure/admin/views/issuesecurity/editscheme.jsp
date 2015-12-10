
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.schemes.issuesecurity.edit.issue.security.scheme'"/></title>
    <meta name="admin.active.section" content="admin_issues_menu/misc_schemes_section"/>
    <meta name="admin.active.tab" content="security_schemes"/>
</head>

<body>

<page:applyDecorator name="jiraform">
    <page:param name="action">EditIssueSecurityScheme.jspa</page:param>
    <page:param name="submitId">update_submit</page:param>
    <page:param name="submitName"><ww:text name="'common.forms.update'"/></page:param>
    <page:param name="cancelURI">ViewIssueSecuritySchemes.jspa</page:param>
    <page:param name="title"><ww:text name="'admin.schemes.issuesecurity.edit.issue.security.scheme'"/>: <ww:property value="scheme/string('name')" /></page:param>
    <page:param name="width">100%</page:param>

    <ui:textfield label="text('common.words.name')" name="'name'" size="'30'" />
    <ui:textarea label="text('common.words.description')" name="'description'" cols="'30'" rows="'3'" />

    <ww:property value="securityLevels" >
        <ui:select label="text('admin.schemes.issuesecurity.default.security.level')" name="'defaultLevel'" list="keySet"
            listKey="'.'" listValue="'../(.)'">
            <ui:param name="'headerrow'" value="'None'" />
            <ui:param name="'headervalue'" value="'-1'" />
        </ui:select>
            <tr>
                <td class="fieldLabelArea">&nbsp;</td>
                <td class="fieldValueArea">
                    <div class="description">
                        <p><ww:text name="'admin.schemes.issuesecurity.settings.the.default.level'"/></p>
                        <ww:text name="'admin.schemes.issuesecurity.note'"/>
                    </div>
                </td>
            </tr>
    </ww:property>

    <ui:component name="'schemeId'" template="hidden.jsp" />
</page:applyDecorator>

</body>
</html>
