<%@ taglib uri="webwork" prefix="ui" %><ui:soy moduleKey="'jira.webresources:issue-statuses'" template="'JIRA.Template.Util.Issue.Status.issueStatusResolver'">
    <ui:param name="'issueStatus'" value="./parameters['issueStatus']/simpleStatus"/>
    <ui:param name="'isSubtle'" value="./parameters['isSubtle']"/>
    <ui:param name="'isCompact'" value="./parameters['isCompact']"/>
    <ui:param name="'maxWidth'" value="./parameters['maxWidth']"/>
</ui:soy>