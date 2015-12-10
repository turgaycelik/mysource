
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>


<html>
<head>
    <meta name="admin.active.section" content="admin_issues_menu/element_options_section/issue_types_section"/>
    <meta name="admin.active.tab" content="issue_types"/>
	<title><ww:text name="'admin.issuesettings.deleteissuetype.title'">
	    <ww:param name="'value0'"><ww:property value="constant/string('name')" /></ww:param>
	</ww:text></title>
</head>
<body>

<page:applyDecorator name="jiraform">
	<page:param name="title"><ww:text name="'admin.issuesettings.deleteissuetype.title'">
	    <ww:param name="'value0'"><ww:property value="constant/string('name')" /></ww:param>
	</ww:text></page:param>
	<page:param name="autoSelectFirst">false</page:param>
	<page:param name="instructions">
        <ww:property value="matchingIssues">
            <ww:if test="./empty == true">
                <p><ww:text name="'admin.issuesettings.deleteissuetype.confirmation'"/></p>
		        <p><ww:text name="'admin.issuesettings.deleteissuetype.no.matching.issues'"/></p>
                <p><ww:text name="'admin.issuesettings.deleteissuetype.note1'">
                    <ww:param name="'value0'"><span class="warning"></ww:param>
                    <ww:param name="'value1'"></span></ww:param>
                </ww:text></p>
            </ww:if>
            <ww:elseIf test="./empty == false && ../availableIssueTypes/empty == true">
		        <p><ww:text name="'admin.issuesettings.deleteissuetype.note2'">
		            <ww:param name="'value0'"><span class="warning"></ww:param>
		            <ww:param name="'value1'"></span></ww:param>
		            <ww:param name="'value2'"><a href="<%=request.getContextPath()%>/secure/IssueNavigator.jspa?reset=true&type=<ww:property value="/id"/>"><b><ww:property value="./size" /></b></a></ww:param>
                </ww:text></p>
                <p><ww:text name="'admin.issuesettings.deleteissuetype.different.schemes'"/></p>
            </ww:elseIf>
            <ww:else>
                <p><ww:text name="'admin.issuesettings.deleteissuetype.confirmation2'"/></p>
                <p><ww:text name="'admin.issuesettings.deleteissuetype.note1'">
                    <ww:param name="'value0'"><span class="warning"></ww:param>
                    <ww:param name="'value1'"></span></ww:param>
                </ww:text></p>
                <p><ww:text name="'admin.issuesettings.deleteissuetype.matching.issues'">
                    <ww:param name="'value0'"><b><ww:property value="./size" /></b></ww:param>
                </ww:text></p>
            </ww:else>
        </ww:property>
	</page:param>

	<page:param name="action">DeleteIssueType.jspa</page:param>
    <page:param name="width">100%</page:param>
    <ww:if test="availableIssueTypes/empty == false || matchingIssues/empty == true">
        <page:param name="submitId">delete_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.words.delete'"/></page:param>
    </ww:if>
	<page:param name="cancelURI">ViewIssueTypes.jspa</page:param>

    <ww:if test="matchingIssues/empty == false && availableIssueTypes/empty == false">
        <ui:select name="'newId'" label="text('admin.issuesettings.deleteissuetype.new.type.for.matching.issues')" list="availableIssueTypes"
            listKey="'id()'" listValue="'name()'" />
    </ww:if>

	<ui:component name="'id'" template="hidden.jsp" theme="'single'"  />
    <ui:component name="'confirm'" value="'true'" template="hidden.jsp" theme="'single'"  />

</page:applyDecorator>

</body>
</html>
