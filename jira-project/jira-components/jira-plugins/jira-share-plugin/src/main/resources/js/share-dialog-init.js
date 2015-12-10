// There may be multiple share button on the page, for example split view
// This is to ensure both works within its own scope and not affect each other
JIRA.JiraSharePlugin.viewIssueDialog = new JIRA.JiraSharePlugin.SharePluginDialog(AJS.$);
JIRA.JiraSharePlugin.issueNavDialog = new JIRA.JiraSharePlugin.SharePluginDialog(AJS.$);

AJS.toInit(_.bind(JIRA.JiraSharePlugin.viewIssueDialog._initShareDialog, JIRA.JiraSharePlugin.viewIssueDialog, "share-entity-popup-viewissue", "#jira-share-trigger.viewissue-share"));
AJS.toInit(_.bind(JIRA.JiraSharePlugin.issueNavDialog._initShareDialog, JIRA.JiraSharePlugin.issueNavDialog, "share-entity-popup-issuenav", "#jira-share-trigger.issuenav-share"));