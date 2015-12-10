<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<ww:bean name="'com.atlassian.jira.util.JiraDateUtils'" id="dateUtils" />
<html>
<head>
	<title><ww:text name="'admin.project.import.progress.title'"/></title>
    <ww:if test="hasErrorMessages == 'false'">
        <meta http-equiv="refresh" content="5">
    </ww:if>
</head>
<body>
    <page:applyDecorator name="jiraform">
        <page:param name="columns">1</page:param>
        <page:param name="action"><ww:property value="/submitUrl"/></page:param>
        <page:param name="method">post</page:param>
        <ww:if test="hasErrorMessages == 'false'">
            <page:param name="submitId">refresh_submit</page:param>
            <page:param name="submitName"><ww:text name="'admin.common.words.refresh'"/></page:param>
        </ww:if>
        <ww:else>
            <page:param name="cancelURI">ProjectImportSelectBackup!default.jspa</page:param>
        </ww:else>
        <page:param name="width">100%</page:param>
        <page:param name="title"><ww:text name="'admin.project.import.progress.title'"/></page:param>
        <tr bgcolor="#ffffff"><td>
            <ui:component template="taskdescriptor.jsp" name="'/ourTask'"/>
        </td></tr>
        <ui:component name="'redirectOnComplete'" template="hidden.jsp"/>
    </page:applyDecorator>
</body>
</html>
