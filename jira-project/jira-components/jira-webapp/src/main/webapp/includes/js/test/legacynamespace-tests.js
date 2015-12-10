AJS.test.require("jira.webresources:jira-global");
AJS.test.require("jira.webresources:jqlautocomplete");
AJS.test.require("jira.webresources:ajax-favourite-control");
AJS.test.require("jira.webresources:userprofile");


var ensureOldNamespaceCallsNew = function (oldNamespaceStr, newNamespaceStr) {
    ok(eval(oldNamespaceStr + "===" + newNamespaceStr + " && " + oldNamespaceStr),
            "Expected [" + oldNamespaceStr + "] to invoke [" +  newNamespaceStr + "]")
};


test("Test mapping of legacy namespaces to new", function () {

    ensureOldNamespaceCallsNew("jira.app.issue.getIssueId", "JIRA.Issue.getIssueId");
    ensureOldNamespaceCallsNew("jira.app.issuenavigator.getSelectedIssueId", "JIRA.IssueNavigator.getSelectedIssueId");
    ensureOldNamespaceCallsNew("jira.app.issuenavigator.shortcuts.selectNextIssue", "JIRA.IssueNavigator.shortcuts.selectNextIssue");
    ensureOldNamespaceCallsNew("jira.widget.dropdown.Standard", "JIRA.Dropdown.Standard");

    ensureOldNamespaceCallsNew("AJS.containDropdown", "JIRA.containDropdown");
    ensureOldNamespaceCallsNew("AJS.SelectMenu", "AJS.DropdownSelect");
    ensureOldNamespaceCallsNew("AJS.SecurityLevel", "AJS.SecurityLevelSelect");
    ensureOldNamespaceCallsNew("AJS.QueryableDropdown", "AJS.QueryableDropdownSelect");
    ensureOldNamespaceCallsNew("AJS.IssuePicker", "JIRA.IssuePicker");
    ensureOldNamespaceCallsNew("AJS.LabelPicker", "JIRA.LabelPicker");
    ensureOldNamespaceCallsNew("AJS.FlexiPopup", "JIRA.Dialog");
    ensureOldNamespaceCallsNew("AJS.FormPopup", "JIRA.FormDialog");
    ensureOldNamespaceCallsNew("AJS.LabelsPopup", "JIRA.LabelsDialog");
    ensureOldNamespaceCallsNew("AJS.UserProfilePopup", "JIRA.UserProfileDialog");
    ensureOldNamespaceCallsNew("jira.app.attachments.screenshot.ScreenshotWindow", "JIRA.ScreenshotDialog");
    ensureOldNamespaceCallsNew("jira.widget.autocomplete", "JIRA.AutoComplete");
    ensureOldNamespaceCallsNew("jira.widget.autocomplete.JQL", "JIRA.JQLAutoComplete");
    ensureOldNamespaceCallsNew("jira.ajax", "JIRA.SmartAjax");
    ensureOldNamespaceCallsNew("jira.app.wikiPreview", "JIRA.wikiPreview");
    ensureOldNamespaceCallsNew("jira.app.userhover", "JIRA.userhover");
    ensureOldNamespaceCallsNew("jira.app.session.storage", "JIRA.SessionStorage");
    ensureOldNamespaceCallsNew("jira.xsrf", "JIRA.XSRF");

});