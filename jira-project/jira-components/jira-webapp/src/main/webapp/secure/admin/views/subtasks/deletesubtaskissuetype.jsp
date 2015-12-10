
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>


<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/issue_types_section"/>
    <meta name="admin.active.tab" content="subtasks"/>     
	<title><ww:text name="'admin.subtasks.delete.issuetype'">
	    <ww:param name="'value0'"><ww:property value="constant/string('name')" /></ww:param>
	</ww:text></title>
</head>
<body>

<page:applyDecorator name="jiraform">
	<page:param name="title"><ww:text name="'admin.subtasks.delete.issuetype'">
	    <ww:param name="'value0'"><ww:property value="constant/string('name')" /></ww:param>
	</ww:text></page:param>
	<page:param name="autoSelectFirst">false</page:param>
	<page:param name="description">
        <ww:property value="matchingIssues">
            <ww:if test="./empty == true">
                <p><ww:text name="'admin.subtasks.delete.issuetype.confirmation'"/></p>
		        <p><ww:text name="'admin.subtasks.delete.no.matching.issues'"/></p>
            </ww:if>
            <ww:elseIf test="./empty == false && ../availableIssueTypes/empty == true">
		        <p><ww:text name="'admin.subtasks.delete.note'">
		            <ww:param name="'value0'"><span class="warning"></ww:param>
		            <ww:param name="'value1'"></span></ww:param>
		            <ww:param name="'value2'"><b><ww:property value="./size" /></b></ww:param>
		        </ww:text></p>
            </ww:elseIf>
            <ww:else>
                <p><ww:text name="'admin.subtasks.delete.issuetype.confirm.and.specify'"/></p>
                <p><ww:text name="'admin.subtasks.delete.currently.x.matching.issues'">
                    <ww:param name="'value0'"><b><ww:property value="./size" /></b></ww:param>
                </ww:text></p>
            </ww:else>
        </ww:property>
	</page:param>

	<page:param name="action">DeleteSubTaskIssueType.jspa</page:param>
    <page:param name="width">100%</page:param>
    <ww:if test="availableIssueTypes/empty == false || matchingIssues/empty == true">
	    <page:param name="submitId">delete_submit</page:param>
	    <page:param name="submitName"><ww:text name="'common.words.delete'"/></page:param>
    </ww:if>
	<page:param name="cancelURI">ManageSubTasks.jspa</page:param>

    <ww:if test="matchingIssues/empty == false && availableIssueTypes/empty == false">
        <ui:select name="'newId'" label="'New type for matching issues'" list="availableIssueTypes"
            listKey="'string('id')'" listValue="'string('name')'" />
    </ww:if>
	<ui:component name="'id'" template="hidden.jsp" theme="'single'"  />
        <ui:component name="'confirm'" value="'true'" template="hidden.jsp" theme="'single'"  />

</page:applyDecorator>

</body>
</html>
