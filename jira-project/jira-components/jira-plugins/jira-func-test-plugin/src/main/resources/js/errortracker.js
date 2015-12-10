JIRA = JIRA || {};
JIRA.DevMode = JIRA.DevMode || {};

JIRA.DevMode.Errors = JIRA.DevMode.Errors || [];

window.onerror = function(message, url, lineNumber) {
    JIRA.DevMode.Errors.push({
        message: message,
        url: url,
        lineNumber: lineNumber
    });
    return false;
};