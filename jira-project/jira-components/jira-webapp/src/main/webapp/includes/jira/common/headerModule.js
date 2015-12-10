define('jira/common/header', [
    'jquery',
    'exports'
], function(
        jQuery,
        exports
        ) {
    var messageBuffer = {};

    var getProjectAdminLink = function(issueKey, section) {
        var projectKey = issueKey.substr(0, issueKey.indexOf("-"));
        return AJS.format('<a href="{0}" target="_blank">', AJS.contextPath() + "/plugins/servlet/project-config/" + projectKey + "/" + section);
    };

    var showMessage = function(html) {
        if(html) {
            JIRA.Messages.showSuccessMsg(html, {
                closeable: true
            });
        }
        //clear out the buffer.
        messageBuffer = {};
    };

    var getEditMessage = function(versionMsg, compnentMsg) {
        var html = "";
        if(messageBuffer.versionCreated) {
            html = "<p>" + versionMsg + "</p>";
        }
        if(messageBuffer.componentCreated) {
            html += "<p>" + compnentMsg + "</p>";
        }
        return html;
    };

    /**
     * Initialised the header module.  Binds to various events that will result in
     * global messages being shown.
     */
    exports.initialize = function() {
        JIRA.bind("QuickCreateIssue.sessionComplete", function (e, issues) {
            var issue = [].concat(issues).pop(); //get the last issue created

            var html = JIRA.Issue.issueCreatedMessage(issue);
            html += getEditMessage(AJS.I18n.getText("jira.version.created.quick.create", getProjectAdminLink(issue.issueKey, "versions"), "</a>"),
                    AJS.I18n.getText("jira.component.created.quick.create", getProjectAdminLink(issue.issueKey, "components"), "</a>"))
            showMessage(html);
        });

        if(JIRA.Issues && JIRA.Issues.Application) {
            JIRA.Issues.Application.on("issueEditor:saveSuccess", function(saveDetails) {
                // Only the latest version of the issueEditor includes the issueKey in the event.  For older versions we fall back to JIRA.Issue.getIssueKey() which could be buggy if the user
                // has already moved on to another issue when the saveSuccess comes back.  Eventually we can just rely on the key from the event once we're on the latest version of KA.
                var issueKey = saveDetails && saveDetails.issueKey ? saveDetails.issueKey : JIRA.Issue.getIssueKey();
                var msg = getEditMessage(AJS.I18n.getText("jira.version.created", getProjectAdminLink(issueKey, "versions"), "</a>"),
                        AJS.I18n.getText("jira.component.created", getProjectAdminLink(issueKey, "components"), "</a>"));
                showMessage(msg);
            });
        }

        JIRA.bind("Issue.Version.new.selected", function() {
            messageBuffer.versionCreated = true;
        });

        JIRA.bind("Issue.Component.new.selected", function() {
            messageBuffer.componentCreated = true;
        });
    };

    /**
     * Unbinds any events the header module listens for.
     */
    exports.unbind = function() {
        JIRA.unbind("QuickCreateIssue.sessionComplete");
        JIRA.unbind("Issue.Version.new.selected");
        JIRA.unbind("Issue.Component.new.selected");
        if(JIRA.Issues && JIRA.Issues.Application) {
            JIRA.Issues.Application.unbind("issueEditor:saveSuccess");
        }
    }
});