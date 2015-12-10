
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>


<html>
<head>
	<title><ww:text name="'admin.issuesettings.resolutions.delete.resolution'"/>: <ww:property value="constant/string('name')" /></title>
    <meta name="admin.active.section" content="admin_issues_menu/issue_attributes"/>
    <meta name="admin.active.tab" content="resolutions"/>     
</head>
<body>

<page:applyDecorator name="jiraform">
	<page:param name="title"><ww:text name="'admin.issuesettings.resolutions.delete.resolution'"/>: <ww:property value="constant/string('name')" /></page:param>
	<page:param name="description">
        <p><ww:text name="'admin.issuesettings.resolutions.delete.confirm'"/></p>
		<p><ww:text name="'admin.issuesettings.resolutions.delete.current.matching.issues'">
		    <ww:param name="'value0'"><b><ww:property value="matchingIssues/size" /></b></ww:param>
		</ww:text></p>
	</page:param>

	<page:param name="autoSelectFirst">false</page:param>
    <page:param name="width">100%</page:param>
	<page:param name="action">DeleteResolution.jspa</page:param>
	<page:param name="submitId">delete_submit</page:param>
	<page:param name="submitName"><ww:text name="'common.words.delete'"/></page:param>
	<page:param name="cancelURI">ViewResolutions.jspa</page:param>

    <ui:select name="'newId'" label="text('admin.issuesettings.resolutions.delete.new.resolution.for.matching.issues')" list="newConstants"
        listKey="'string('id')'" listValue="'string('name')'" />
	<ui:component name="'id'" template="hidden.jsp" theme="'single'"  />
        <ui:component name="'confirm'" value="'true'" template="hidden.jsp" theme="'single'"  />

</page:applyDecorator>

</body>
</html>
