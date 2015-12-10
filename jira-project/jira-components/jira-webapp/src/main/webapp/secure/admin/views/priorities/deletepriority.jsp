
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>


<html>
<head>
	<title><ww:text name="'admin.issuesettings.priorities.delete.priority'"/>: <ww:property value="constant/string('name')" /></title>
    <meta name="admin.active.section" content="admin_issues_menu/issue_attributes"/>
    <meta name="admin.active.tab" content="priorities"/>
</head>
<body>

<page:applyDecorator name="jiraform">
	<page:param name="title"><ww:text name="'admin.issuesettings.priorities.delete.priority'"/>: <ww:property value="constant/string('name')" /></page:param>
	<page:param name="autoSelectFirst">false</page:param>
	<page:param name="description">
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">warning</aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:text name="'admin.issuesettings.priorities.delete.confirm'"/></p>
                <p><ww:text name="'admin.issuesettings.priorities.delete.currently.issues.attached'">
                    <ww:param name="'value0'"><b><ww:property value="matchingIssues/size" /></b></ww:param>
                </ww:text></p>
            </aui:param>
        </aui:component>
	</page:param>

	<page:param name="action">DeletePriority.jspa</page:param>
    <page:param name="width">100%</page:param>
	<page:param name="submitId">delete_submit</page:param>
	<page:param name="submitName"><ww:text name="'common.words.delete'"/></page:param>
	<page:param name="cancelURI">ViewPriorities.jspa</page:param>

    <ui:select name="'newId'" label="text('admin.issuesettings.priorities.delete.new.priorities.for.matching.issues')" list="newConstants"
        listKey="'string('id')'" listValue="'string('name')'" />
	<ui:component name="'id'" template="hidden.jsp" />
	<input type="hidden" name="confirm" value="true">
</page:applyDecorator>

</body>
</html>
