<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
    <title>Import Issues from a Jira XML File</title>
</head>
<body>

<page:applyDecorator name="jiraform">
    <page:param name="action">JiraXMLImporter.jspa</page:param>
    <page:param name="submitId">import_submit</page:param>
    <page:param name="submitName">Import</page:param>
    <page:param name="autoSelectFirst">true</page:param>
    <page:param name="title">Import Issues from a Jira XML file</page:param>
    <page:param name="width">100%</page:param>
	<page:param name="instructions">Write the full path to the Jira XML file and select "import"</page:param>

    <ui:textfield label="importLocation" name="'importLocation'">
		<ui:param name="'size'">50</ui:param>
		<ui:param name="'description'">full path to the jira xml file</ui:param>
		<ui:param name="'mandatory'">true</ui:param>
	</ui:textfield>

</page:applyDecorator>


</body>
</html>
